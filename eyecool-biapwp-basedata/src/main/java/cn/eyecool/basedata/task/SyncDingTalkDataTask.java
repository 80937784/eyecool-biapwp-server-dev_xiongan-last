package cn.eyecool.basedata.task;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.validator.routines.LongValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dingtalk.api.response.OapiV2DepartmentGetResponse;
import com.dingtalk.api.response.OapiV2DepartmentGetResponse.DeptGetResponse;
import com.dingtalk.api.response.OapiV2DepartmentListsubResponse;
import com.dingtalk.api.response.OapiV2DepartmentListsubResponse.DeptBaseResponse;
import com.dingtalk.api.response.OapiV2UserListResponse;
import com.dingtalk.api.response.OapiV2UserListResponse.ListUserResponse;
import com.dingtalk.api.response.OapiV2UserListResponse.PageResult;
import com.google.common.collect.Lists;
import com.taobao.api.ApiException;

import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.basedata.event.PersonChangeEventPublishService;
import cn.eyecool.basedata.mapper.BasePersonInfoMapper;
import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.domain.entity.SysDept;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.msg.configure.ding.DingTalkInstanceCache;
import cn.eyecool.msg.configure.ding.PlatformDingTalk;
import cn.eyecool.msg.domain.MsgDingApplication;
import cn.eyecool.msg.service.IMsgDingApplicationService;
import cn.eyecool.system.domain.SysConfig;
import cn.eyecool.system.mapper.SysDeptMapper;
import cn.eyecool.system.service.ISysConfigService;

/**
 * 从钉钉同步部门和人员信息
 * 
 * @author mawj
 * @date 2021/03/17
 */
@Component("syncDingTalkDataTask")
public class SyncDingTalkDataTask {

    private static final Logger LOG = LoggerFactory.getLogger(SyncDingTalkDataTask.class);

    // 钉钉跟部门ID
    private static final Long DING_TALK_ROOT_DEPT_ID = 1L;

    @Autowired
    private ISysConfigService configService;
    @Autowired
    private SysDeptMapper sysDeptMapper;
    @Autowired
    private BasePersonInfoMapper basePersonInfoMapper;
    @Autowired
    private IMsgDingApplicationService dingApplicationService;
    @Autowired
    private PersonChangeEventPublishService personChangeEventPublishService;
    @Autowired
    private TenantProperties tenantProperties;

    /**
     * 同步部门和人员信息
     */
    public void executeSync() {
        List<SysConfig> configList = getSyncdataDingTalkConfigList();
        if (CollectionUtils.isEmpty(configList)) {
            return;
        }
        for (SysConfig config : configList) {
            try {
                if (tenantProperties.getEnabled()) {// 开启多租户
                    TenantContextHolder.setTenantId(config.getTenantId());
                }
                syncSingleTenantInfo(config);
            } catch (Exception e) {
                LOG.error("Synchronized data information from DingTalk is abnormal, tenant Id:[{}], appKey:[{}]", config.getTenantId(), config.getConfigValue(), e);
            } finally {
                TenantContextHolder.clear();
            }
        }
    }

    /**
     * 处理单个租户的部门信息同步
     * 
     * @param config 参数配置信息，获取租户ID和对接钉钉的appKey
     */
    private void syncSingleTenantInfo(SysConfig config) {
        String appKey = config.getConfigValue();
        MsgDingApplication appCondition = new MsgDingApplication();
        appCondition.setAppKey(appKey);
        List<MsgDingApplication> list = dingApplicationService.selectMsgDingApplicationList(appCondition);
        if (CollectionUtils.isEmpty(list)) {
            LOG.error("DingTalk micro application is not configured on the platform,tenantId=[{}],appKey=[{}]");
            return;
        }
        // 查询所有的组织机构（数据量不大）
        List<SysDept> deptList = sysDeptMapper.selectDeptList(new SysDept());
        // Map(部门编码:部门信息)
        Map<String, SysDept> deptCodeMap = new HashMap<String, SysDept>();
        // Map(parentId:maxOrderNum)
        Map<Long, String> orderNumMap = new HashMap<Long, String>();
        for (SysDept it : deptList) {
            // 设置部门编码为key值的部门信息Map
            if (it.getParentId() == 0L) {
                deptCodeMap.put(null, it);// 顶级部门可能code不存在，设置key为null(hashMap只能有一个key为null)
            } else if (StringUtils.isNotBlank(it.getDeptCode())) {
                deptCodeMap.put(it.getDeptCode(), it);
            }
            String orderNum = it.getOrderNum();
            if (StringUtils.isBlank(orderNum)) {
                continue;
            }
            String maxorderNum = orderNumMap.get(it.getParentId());
            maxorderNum = StringUtils.isBlank(maxorderNum) ? orderNum
                : String.valueOf(Math.max(Integer.valueOf(maxorderNum), Integer.valueOf(orderNum)));
            orderNumMap.put(it.getParentId(), maxorderNum);
        }
        MsgDingApplication msgDingApplication = list.get(0);
        // 查询根部门信息
        getRootDeptDetai(msgDingApplication, deptCodeMap);
        // 同步保存部门信息
        syncDeptInfo(msgDingApplication, deptCodeMap, orderNumMap, DING_TALK_ROOT_DEPT_ID);
        // 同步人员信息
        syncAllPersonInfo(msgDingApplication, deptCodeMap);
    }

    /**
     * 查询跟部门信息
     * 
     * @param msgDingApplication
     * @param deptCodeMap
     */
    private void getRootDeptDetai(MsgDingApplication msgDingApplication, Map<String, SysDept> deptCodeMap) {
        String tenantId = TenantContextHolder.getTenantId();
        String agentId = msgDingApplication.getAgentId();
        String appKey = msgDingApplication.getAppKey();
        String appSecrect = msgDingApplication.getAppSecrect();
        PlatformDingTalk dingTalk = DingTalkInstanceCache.get(Long.parseUnsignedLong(agentId), appKey, appSecrect);
        try {
            OapiV2DepartmentGetResponse response = dingTalk.deptDetail(DING_TALK_ROOT_DEPT_ID);
            if (response.getErrcode() != 0) {
                LOG.error("DingTalk query root department information is abnormal, tenant ID:[{}],errorCode:[{}], errmsg:[{}]", tenantId, response.getErrcode(),
                    response.getErrmsg());
                return;
            }
            DeptGetResponse result = response.getResult();
            Long deptCode = result.getDeptId();
            SysDept rootDept = deptCodeMap.get(null);
            rootDept.setDeptCode(String.valueOf(deptCode));
            rootDept.setDeptName(result.getName());
            rootDept.setUpdateTime(new Date());
            sysDeptMapper.updateDept(rootDept);
            deptCodeMap.put(null, rootDept);
        } catch (ApiException e) {
            LOG.error("DingTalk query root department information is abnormal,errcode:[{}],errmsg:[{}]", e.getErrCode(), e.getErrMsg(), e);
        }
    }

    /**
     * 查询并保存部门信息
     * 
     * @param msgDingApplication 钉钉微应用信息
     * @param deptCodeMap 部门Map(部门编码：部门信息)
     * @param orderNumMap 部门排序Map(父部门ID：父部门下最大排序序号)
     * @param parentId 钉钉查询子部门的父部门ID
     */
    private void syncDeptInfo(MsgDingApplication msgDingApplication, Map<String, SysDept> deptCodeMap,
        Map<Long, String> orderNumMap, Long parentId) {
        String tenantId = TenantContextHolder.getTenantId();
        String agentId = msgDingApplication.getAgentId();
        String appKey = msgDingApplication.getAppKey();
        String appSecrect = msgDingApplication.getAppSecrect();
        PlatformDingTalk dingTalk = DingTalkInstanceCache.get(Long.parseUnsignedLong(agentId), appKey, appSecrect);
        try {
            OapiV2DepartmentListsubResponse response = dingTalk.listSubDept(parentId);
            if (response.getErrcode() != 0) {
                LOG.error("DingTalk query department information is abnormal, tenant ID:[{}],errorCode:[{}], errmsg:[{}]", tenantId, response.getErrcode(),
                    response.getErrmsg());
                return;
            }
            List<DeptBaseResponse> resutList = response.getResult();
            if (CollectionUtils.isEmpty(resutList)) {
                LOG.info("DingTalk department list is empty, tenant ID:[{}],appKey:[{}],DingTalk parent department ID:[{}]", tenantId, appKey, parentId);
                return;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("DingTalk department information synchronization tenant ID:[{}],appKey:[{}],DingTalk parent department ID:[{}],result:[{}]", tenantId, appKey, parentId,
                    response.getBody());
            }
            resutList.forEach(item -> {
                // 钉钉的部门ID对应部门表的部门编码
                String deptCode = String.valueOf(item.getDeptId());
                String parentCode = String.valueOf(item.getParentId());
                // 从部门表查询部门信息
                SysDept dept = deptCodeMap.get(deptCode);
                SysDept parentDept = deptCodeMap.get(parentCode);
                if (DING_TALK_ROOT_DEPT_ID.equals(item.getParentId())) {// 1标识是钉钉的根部门下的子部门，需要放在顶级部门下
                    parentDept = deptCodeMap.get(null);
                }
                boolean isInsert = false;
                if (null == dept) {// 部门不存在，直接新增,主键自增
                    dept = new SysDept();
                    dept.setCreateTime(new Date());
                    isInsert = true;
                }
                dept.setDeptCode(deptCode);
                dept.setParentId(null == parentDept ? 0L : parentDept.getDeptId());
                dept.setDeptName(item.getName());
                dept.setDelFlag("0");
                // 设置排序
                String maxOrderNum = orderNumMap.get(dept.getParentId());
                maxOrderNum = StringUtils.isBlank(maxOrderNum) ? "1" : String.valueOf(Integer.valueOf(maxOrderNum) + 1);
                // 设置祖级别列表
                dept.setAncestors(null == parentDept ? "0" : parentDept.getAncestors() + "," + parentDept.getDeptId());
                if (isInsert) {
                    dept.setOrderNum(maxOrderNum);
                    sysDeptMapper.insertDept(dept);
                } else {
                    dept.setUpdateTime(new Date());
                    sysDeptMapper.updateDept(dept);
                }
                deptCodeMap.put(deptCode, dept);// key相同，value值会直接替换
                orderNumMap.put(dept.getParentId(), maxOrderNum);
                // 递归查询子部门
                syncDeptInfo(msgDingApplication, deptCodeMap, orderNumMap, item.getDeptId());
            });
        } catch (ApiException e) {
            LOG.error(e.getErrCode() + ":" + e.getErrMsg(), e);
        }
    }

    /**
     * 查询需要和钉钉进行数据交互的的参数配置
     * 
     * @return
     */
    private List<SysConfig> getSyncdataDingTalkConfigList() {
        // 未开启多租户
        if (!tenantProperties.getEnabled()) {
            String configValue = configService.selectConfigByKey(SysConfigConstants.DINGTALK_SYNCDATA_APPKEY);
            if (StringUtils.isBlank(configValue)) {
                LOG.info("There is no parameter [{}] configuration, and there is no need for data interaction and data synchronization with DingTalk", SysConfigConstants.DINGTALK_SYNCDATA_APPKEY);
                return Collections.emptyList();
            }
            SysConfig config = new SysConfig();
            config.setConfigKey(SysConfigConstants.DINGTALK_SYNCDATA_APPKEY);
            config.setConfigValue(configValue);
            return Lists.newArrayList(config);
        }
        // 开启多租户
        List<SysConfig> configList =
            configService.getTenantConfigListByConfigKey(SysConfigConstants.DINGTALK_SYNCDATA_APPKEY);
        configList = configList.stream().filter(it -> null != it.getConfigValue()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(configList)) {
            LOG.info("There is no parameter [{}] configuration for any tenant, and there is no need for data interaction and data synchronization with DingTalk", SysConfigConstants.DINGTALK_SYNCDATA_APPKEY);
            return Collections.emptyList();
        }
        String tenanIds =
            configList.stream().map(SysConfig::getTenantId).reduce((el1, el2) -> el1 + "," + el2).orElse(null);
        LOG.info("Tenants who need to interact with DingTalk:[{}]", tenanIds);
        return configList;
    }

    /**
     * 同步所有人员信息
     * 
     * @param deptCodeMap
     * @param msgDingApplication
     */
    private void syncAllPersonInfo(MsgDingApplication msgDingApplication, Map<String, SysDept> deptCodeMap) {
        if (null == deptCodeMap || deptCodeMap.size() == 0) {
            LOG.info("Tenant[{}]'s department for synchronizing personnel data from DingTalk is empty");
            return;
        }
        deptCodeMap.values().stream().filter(item -> {
            return StringUtils.isNotBlank(item.getDeptCode())
                && LongValidator.getInstance().isValid(item.getDeptCode());
        }).forEach(item -> {
            syncSingleDeptPersonInfo(msgDingApplication, item.getDeptId(), Long.valueOf(item.getDeptCode()), 0L);
        });
    }

    /**
     * 查询并保存人员信息
     * 
     * @param msgDingApplication 钉钉微应用信息
     * @param sysDeptId 部门主键
     * @param dingDeptId 钉钉部门ID（对应系统部门编码）
     */
    private void syncSingleDeptPersonInfo(MsgDingApplication msgDingApplication, Long sysDeptId, Long dingDeptId,
        Long cursor) {
        String tenantId = TenantContextHolder.getTenantId();
        String agentId = msgDingApplication.getAgentId();
        String appKey = msgDingApplication.getAppKey();
        String appSecrect = msgDingApplication.getAppSecrect();
        PlatformDingTalk dingTalk = DingTalkInstanceCache.get(Long.parseUnsignedLong(agentId), appKey, appSecrect);
        try {
            // 每次分页查询50条
            OapiV2UserListResponse response = dingTalk.listPersonDetail(dingDeptId, cursor, 50L);
            if (response.getErrcode() != 0) {
                LOG.error("DingTalk query personnel information is abnormal,errorCode:[{}], errmsg:[{}]", response.getErrcode(), response.getErrmsg());
                return;
            }
            PageResult pageResult = response.getResult();
            Boolean hasMore = pageResult.getHasMore();
            List<ListUserResponse> list = pageResult.getList();
            Long nextCursor = pageResult.getNextCursor();
            if (CollectionUtils.isEmpty(list)) {
                LOG.info("DingTalk synchronization personnel list is empty, tenant ID:[{}],appKey:[{}],钉钉部门ID:[{}]", tenantId, appKey, dingDeptId);
                return;
            }
            list.stream().forEach(resp -> {
                // 保存人员信息
                savePersonInfo(resp, sysDeptId);
            });
            if (Boolean.TRUE.equals(hasMore)) {
                syncSingleDeptPersonInfo(msgDingApplication, sysDeptId, dingDeptId, nextCursor);
            }
        } catch (ApiException e) {
            LOG.error(e.getErrCode() + ":" + e.getErrMsg(), e);
        }
    }

    /**
     * 保存人员信息
     * 
     * @param response
     * @param sysDeptId
     */
    private void savePersonInfo(ListUserResponse response, Long sysDeptId) {
        if (StringUtils.isBlank(response.getJobNumber())) {
            LOG.error("Dingding synchronization personnel [{}] has no job number, tenant Id:[{}]", response.getName(), TenantContextHolder.getTenantId());
            return;
        }
        BasePersonInfo condition = new BasePersonInfo();
        condition.setUniqueId(response.getJobNumber());
        List<BasePersonInfo> list = basePersonInfoMapper.selectBasePersonInfoList(condition);
        if (CollectionUtils.isEmpty(list)) {
            // 新增人员
            BasePersonInfo personInfo = new BasePersonInfo();
            String personId = IdWorker.getNextStringId();
            personInfo.setId(personId);
            personInfo.setName(response.getName());
            personInfo.setUniqueId(response.getJobNumber());
            personInfo.setPhone(response.getMobile());
            personInfo.setEmail(response.getEmail());
            personInfo.setDeptId(sysDeptId);
            personInfo.setDatasource(DictConstants.DataSource.SYNC_UPDATE);
            personInfo.setCreateTime(DateUtils.getNowDate());
            basePersonInfoMapper.insertBasePersonInfo(personInfo);
            // 发布人员信息改变事件
            personChangeEventPublishService.personAddPublish(personId, response.getJobNumber(), false, false, false,
                false, false, null);
            return;
        }
        BasePersonInfo personInfo = list.get(0);
        boolean shouldUpdate = checkPersonShoudBeUpdate(response, personInfo);
        if (shouldUpdate) {
            personInfo.setStatus(DictConstants.Status.ENABLE);
            personInfo.setUpdateTime(DateUtils.getNowDate());
            personInfo.setName(response.getName());
            personInfo.setPhone(response.getMobile());
            personInfo.setEmail(response.getEmail());
            personInfo.setDeptId(sysDeptId);
            basePersonInfoMapper.updateBasePersonInfo(personInfo);
            // 发布人员信息改变事件
            personChangeEventPublishService.personChangePublish(personInfo.getId(), response.getJobNumber(), null);
        }
    }

    /**
     * 校验人员信息是否需要同步更新
     * 
     * @param response
     * @param personInfo
     * @return
     */
    private boolean checkPersonShoudBeUpdate(ListUserResponse response, BasePersonInfo personInfo) {
        String name = response.getName();
        String email = response.getEmail();
        String mobile = response.getMobile();
        // 已存在人员如果被逻辑删除掉了，也认为需要同步
        if (DictConstants.Status.DISABLE.equals(personInfo.getStatus())) {
            return true;
        }
        if (StringUtils.isNotBlank(name) && !name.equals(personInfo.getName())) {
            return true;
        }
        if (StringUtils.isNotBlank(mobile) && !mobile.equals(personInfo.getPhone())) {
            return true;
        }
        if (StringUtils.isNotBlank(email) && !mobile.equals(personInfo.getEmail())) {
            return true;
        }
        return false;
    }

}
