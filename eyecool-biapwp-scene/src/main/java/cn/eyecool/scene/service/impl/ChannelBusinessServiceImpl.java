package cn.eyecool.scene.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;

import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.basedata.manager.IPersonDataManagerLogicService;
import cn.eyecool.basedata.mapper.BasePersonInfoMapper;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.text.Convert;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.bean.BeanUtils;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.common.utils.sql.SqlUtil;
import cn.eyecool.scene.constant.OperateLock;
import cn.eyecool.scene.domain.ChannelBusiParam;
import cn.eyecool.scene.domain.ChannelBusiParam.BindTypeEnum;
import cn.eyecool.scene.domain.ChannelBusiness;
import cn.eyecool.scene.domain.ChannelInfo;
import cn.eyecool.scene.domain.ChannelSubtreasuryBusi;
import cn.eyecool.scene.domain.ChannelSubtreasuryInfo;
import cn.eyecool.scene.mapper.ChannelBusinessMapper;
import cn.eyecool.scene.mapper.ChannelInfoMapper;
import cn.eyecool.scene.mapper.ChannelSubtreasuryBusiMapper;
import cn.eyecool.scene.mapper.ChannelSubtreasuryInfoMapper;
import cn.eyecool.scene.service.IBusiLiveUpdateSeqService;
import cn.eyecool.scene.service.IChannelBusinessService;
import lombok.extern.slf4j.Slf4j;

/**
 * 场景人员Service业务层处理
 * 
 * @author admin
 * @date 2021-03-22
 */
@Service
@Slf4j
public class ChannelBusinessServiceImpl implements IChannelBusinessService {

    @Autowired
    private BasePersonInfoMapper basePersonInfoMapper;
    @Autowired
    private ChannelInfoMapper channelInfoMapper;
    @Autowired
    private ChannelSubtreasuryInfoMapper subtreasuryInfoMapper;
    @Autowired
    private ChannelBusinessMapper channelBusinessMapper;
    @Autowired
    private IPersonDataManagerLogicService personDataManagerLogicService;
    @Autowired
    private IBusiLiveUpdateSeqService busiLiveUpdateSeqService;
    @Autowired
    private ChannelSubtreasuryBusiMapper subtreasuryBusiMapper;
    /** 自己注入自己实现同一个类内部方法调用事务起作用，spring框架解决了循环注入问题，但注入自己这种方式要慎用 */
    @Autowired
    private IChannelBusinessService channelBusinessService;

    /**
     * 查询场景人员
     * 
     * @param id 场景人员ID
     * @return 场景人员
     */
    @Override
    public ChannelBusiness selectChannelBusinessById(String id) {
        return channelBusinessMapper.selectChannelBusinessById(id);
    }

    /**
     * 查询场景人员列表
     * 
     * @param channelBusiness 场景人员
     * @return 场景人员
     */
    @Override
    public List<ChannelBusiness> selectChannelBusinessList(ChannelBusiness channelBusiness) {
        return channelBusinessMapper.selectChannelBusinessList(channelBusiness);
    }

    /**
     * 新增场景人员
     * 
     * @param channelBusiness 场景人员
     * @return 结果
     */
    @Override
    public int insertChannelBusiness(ChannelBusiParam param) {
        BindTypeEnum bindTypeEnum = BindTypeEnum.parse(param.getBindType());
        switch (bindTypeEnum) {
            case BIND_ALL:
                return bindAllPersonByCondition(param, false);
            case BIND_BY_DEPT:
                return bindAllPersonByCondition(param, true);
            case BIND_SELF_DEFINE:
                return bindPersonBySelfDefine(param);
            default:
                throw new CustomException(MessageUtils.message("channel.busi.service.unknown.type"));
        }
    }

    /**
     * 根据条件查询人员添加到场景库
     * 
     * @param param
     * @param byDept
     * @return
     */
    private int bindAllPersonByCondition(ChannelBusiParam param, boolean byDept) {
        ChannelInfo channelInfo = channelInfoMapper.selectChannelInfoById(param.getChannelId());
        String channelCode = channelInfo.getChannelCode();
        BasePersonInfo basePersonInfo = new BasePersonInfo();
        if (byDept) {
            basePersonInfo.setDeptId(param.getDeptId());
        }
        long total = 0; // 数据总量
        int pageNum = 1;// pageNum一直是第一页，因为中间查询待加入数量已经排除掉了已加入人员,分页一直查询第一页就可以
        int pageSize = 1000;// 分页数量
        String orderBy = SqlUtil.escapeOrderBySql("base.id asc, base.unique_id asc");
        // 序列号原子对象
        final AtomicInteger successNum = new AtomicInteger(0);// 更新成功数量原子对象
        final AtomicInteger failNum = new AtomicInteger(0);// 更新失败数量原子对象
        do {
            PageHelper.startPage(pageNum, pageSize, orderBy);
            List<BasePersonInfo> list =
                channelBusinessMapper.selectUnbindPersonInfoList(param.getChannelId(), basePersonInfo);
            total = new PageInfo<BasePersonInfo>(list).getTotal();
            if (total == 0) {
                break;
            }
            list.stream().forEach(it -> {
                // 注意：如果使用并行流操作，禁止在外部创建对象或者使用同一引用
                ChannelBusiness busi = new ChannelBusiness();
                busi.setPersonId(it.getId());
                busi.setUniqueId(it.getUniqueId());
                busi.setChannelId(param.getChannelId());
                busi.setFaceIrisMode(param.getFaceMode());
                busi.setIrisMode(param.getIrisMode());
                busi.setFaceIrisMode(param.getFaceIrisMode());
                busi.setFingerMode(param.getFingerMode());
                busi.setFveinMode(param.getFveinMode());
                busi.setRemark(param.getRemark());
                try {
                    channelBusinessService.insertSingleChannelBusiData(busi, channelCode);
                    successNum.getAndIncrement();
                } catch (Exception e) {
                    log.error("添Failed to add personnel to scene library,chanelCode:[{}],uiqueId:[{}]", channelCode, it.getUniqueId(), e);
                    failNum.getAndIncrement();
                }
            });
        } while (pageSize < total);
        log.info(MessageUtils.message("channel.busi.service.bind.person.scene.success", successNum.get()));
        if (failNum.get() > 0) {
            String msg = MessageUtils.message("channel.busi.service.bind.person.scene.failed", failNum.get());
            log.info(msg);
            throw new CustomException(msg);
        }
        return successNum.get() > 0 ? successNum.get() : 1;
    }

    /**
     * 自定义选择人员添加到场景库
     * 
     * @param channelBusiness
     * @return
     */
    private int bindPersonBySelfDefine(ChannelBusiParam channelBusiness) {
        String[] personIds = Convert.toStrArray(channelBusiness.getPersonId());
        String[] uniqueIds = Convert.toStrArray(channelBusiness.getUniqueId());
        // 序列号原子对象
        final AtomicInteger successNum = new AtomicInteger(0);// 更新成功数量原子对象
        final AtomicInteger failNum = new AtomicInteger(0);// 更新失败数量原子对象
        ChannelBusiness targetBusi = new ChannelBusiness();
        BeanUtils.copyBeanProp(targetBusi, channelBusiness);
        try {
            targetBusi.setCreateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        ChannelInfo channelInfo = channelInfoMapper.selectChannelInfoById(channelBusiness.getChannelId());
        String channelCode = channelInfo.getChannelCode();
        for (int i = 0; i < personIds.length; i++) {
            try {
                targetBusi.setPersonId(personIds[i]);
                targetBusi.setUniqueId(uniqueIds[i]);
                channelBusinessService.insertSingleChannelBusiData(targetBusi, channelCode);
                successNum.getAndIncrement();
            } catch (Exception e) {
                log.error("Failed to add person to scene library,chanelCode:[{}],uiqueId:[{}]", channelCode, uniqueIds[i], e);
                failNum.getAndIncrement();
            }
        }
        log.info(MessageUtils.message("channel.busi.service.bind.person.scene.success", successNum.get()));
        if (failNum.get() > 0) {
            String msg = MessageUtils.message("channel.busi.service.bind.person.scene.failed", failNum.get());
            log.info(msg);
            throw new CustomException(msg);
        }
        return successNum.get();
    }

    /**
     * 添加单条人员数据到场景库
     * 
     * @param targetBusi
     * @param channelCode
     */
    @Override
    @Transactional
    public void insertSingleChannelBusiData(ChannelBusiness targetBusi, String channelCode) {
        targetBusi.setId(IdWorker.getNextStringId());
        ChannelBusiness condition = new ChannelBusiness();
        condition.setPersonId(targetBusi.getPersonId());
        condition.setChannelId(targetBusi.getChannelId());
        List<ChannelBusiness> list = channelBusinessMapper.selectChannelBusinessList(condition);
        if (CollectionUtils.isEmpty(list)) {
            targetBusi.setId(IdWorker.getNextStringId());
            targetBusi.setCreateTime(DateUtils.getNowDate());
            // 更新标识序列号
            Long updateSeriaNum = busiLiveUpdateSeqService.incrementAndGetChannelBusiSeqNum();
            targetBusi.setUpdateSeriaNum(updateSeriaNum);
            channelBusinessMapper.insertChannelBusiness(targetBusi);
            personDataManagerLogicService.addLibraryPerson(channelCode, targetBusi.getUniqueId());
            return;
        }
        ChannelBusiness existObject = list.get(0);
        boolean isDisabled = DictConstants.Status.DISABLE.equals(existObject.getStatus());
        BeanUtils.copyProperties(targetBusi, existObject, "id");
        existObject.setStatus(DictConstants.Status.ENABLE);
        existObject.setUpdateTime(DateUtils.getNowDate());
        // 更新标识序列号
        Long updateSeriaNum = busiLiveUpdateSeqService.incrementAndGetChannelBusiSeqNum();
        existObject.setUpdateSeriaNum(updateSeriaNum);
        channelBusinessMapper.updateChannelBusiness(existObject);
        if (isDisabled) {// 之前存在无效的信息，修改状态后需要重新加入datamanager
            personDataManagerLogicService.addLibraryPerson(channelCode, targetBusi.getUniqueId());
        }
    }

    /**
     * 修改场景人员
     * 
     * @param channelBusiness 场景人员
     * @return 结果
     */
    @Override
    @Transactional
    public int updateChannelBusiness(ChannelBusiness channelBusiness) {
        Long updateSeriaNum = busiLiveUpdateSeqService.incrementAndGetChannelBusiSeqNum();
        channelBusiness.setUpdateSeriaNum(updateSeriaNum);
        channelBusiness.setUpdateTime(DateUtils.getNowDate());
        try {
            channelBusiness.setUpdateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        return channelBusinessMapper.updateChannelBusiness(channelBusiness);
    }

    /**
     * 批量删除场景人员
     * 
     * @param ids 需要删除的场景人员ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteChannelBusinessByIds(String[] ids) {
        int result = 0;
        for (String id : ids) {
            result += deleteChannelBusinessById(id);
        }
        return result;
    }

    /**
     * 删除场景人员信息
     * 
     * @param id 场景人员ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteChannelBusinessById(String id) {
        ChannelBusiness channelBusiness = channelBusinessMapper.selectChannelBusinessById(id);
        channelBusiness.setStatus(DictConstants.Status.DISABLE);
        // 更新标识序列号
        Long updateSeriaNum = busiLiveUpdateSeqService.incrementAndGetChannelBusiSeqNum();
        channelBusiness.setUpdateSeriaNum(updateSeriaNum);
        channelBusiness.setUpdateTime(DateUtils.getNowDate());
        try {
            channelBusiness.setUpdateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        int result = channelBusinessMapper.updateChannelBusiness(channelBusiness);
        ChannelInfo channel = channelInfoMapper.selectChannelInfoById(channelBusiness.getChannelId());
        personDataManagerLogicService.deleteLibraryPerson(channel.getChannelCode(), channelBusiness.getUniqueId());
        deleteSubBusiCascade(channel.getId(), channelBusiness.getPersonId());
        return result;
    }

    /**
     * 级联删除场景子场景人员
     * 
     * @param channelId
     * @param personId
     */
    private void deleteSubBusiCascade(String channelId, String personId) {
        // 逻辑删除子场景信息
        ChannelSubtreasuryBusi subtreasuryBusi = new ChannelSubtreasuryBusi();
        subtreasuryBusi.setChannelId(channelId);
        subtreasuryBusi.setPersonId(personId);
        List<ChannelSubtreasuryBusi> subtreasuryBusiList =
            subtreasuryBusiMapper.selectChannelSubtreasuryBusiList(subtreasuryBusi);
        if (CollectionUtils.isNotEmpty(subtreasuryBusiList)) {
            subtreasuryBusiList.stream().forEach(item -> {
                item.setStatus(DictConstants.Status.DISABLE);
                item.setUpdateTime(DateUtils.getNowDate());
                try {
                    item.setUpdateBy(SecurityUtils.getUsername());
                } catch (Exception e) {
                }
                // 子场景人员更新标识序列号
                Long subSeqNum = busiLiveUpdateSeqService.incrementAndGetSubtreasuryBusiSeqNum();
                item.setUpdateSeriaNum(subSeqNum);
                subtreasuryBusiMapper.updateChannelSubtreasuryBusi(item);
                ChannelSubtreasuryInfo subtreasuryInfo =
                    subtreasuryInfoMapper.selectChannelSubtreasuryInfoById(item.getSubTreasuryId());
                personDataManagerLogicService.deleteLibraryPerson(subtreasuryInfo.getSubTreasuryCode(),
                    item.getUniqueId());
            });
        }
    }

    /**
     * 查询未绑定场景的人员列表
     * 
     * @param basePersonInfo 人员信息
     * @param channelId 场景主键
     * @return
     */
    @Override
    public List<BasePersonInfo> selectUnbindPersonInfoList(BasePersonInfo basePersonInfo, String channelId) {
        return channelBusinessMapper.selectUnbindPersonInfoList(channelId, basePersonInfo);
    }

    /**
     * 同步关系到datamanager
     * 
     * @param channelBusiness
     * @return
     */
    @Override
    public AjaxResult syncdata(ChannelBusiness channelBusiness) {
        boolean tryLock = OperateLock.synclock.tryLock();
        if (!tryLock) {
            throw new CustomException(MessageUtils.message("channel.busi.service.syn.taks.busy"));
        }
        try {
            long total = 0; // 数据总量
            int pageNum = 1;// 分页
            int pageSize = 1000;// 分页数量
            String orderBy = SqlUtil.escapeOrderBySql("busi.create_time asc, busi.id asc");
            // 序列号原子对象
            final AtomicInteger successNum = new AtomicInteger(0);// 更新成功数量原子对象
            final AtomicInteger failNum = new AtomicInteger(0);// 更新失败数量原子对象
            do {
                PageHelper.startPage(pageNum, pageSize, orderBy);
                List<ChannelBusiness> list = channelBusinessMapper.selectChannelBusinessList(new ChannelBusiness());
                list.stream().forEach(info -> {
                    if (handleSyncSingleData(info)) {
                        successNum.getAndIncrement();
                    } else {
                        failNum.getAndIncrement();
                    }
                });
                total = new PageInfo<ChannelBusiness>(list).getTotal();
                pageNum++;
            } while ((pageNum - 1) * pageSize < total);
            Map<String, Integer> map = Maps.newHashMap();
            map.put("failNum", failNum.get());
            map.put("successNum", successNum.get());
            String msg = "";
            if (failNum.get() > 0) {
                msg = MessageUtils.message("channel.busi.service.syn.scene.sumary", successNum.get(),failNum.get());
                return AjaxResult.error(msg, map);
            } else {
                msg = MessageUtils.message("channel.busi.service.syn.scene.all.success");
                return AjaxResult.success(msg, map);
            }
        } finally {
            OperateLock.synclock.unlock();
        }
    }

    /**
     * 同步单条场景库人员数据到Datamanager
     * 
     * @param channelBusiness
     */
    private boolean handleSyncSingleData(ChannelBusiness channelBusiness) {
        try {
            if (DictConstants.Status.ENABLE.equals(channelBusiness.getStatus())) {
                personDataManagerLogicService.addLibraryPerson(channelBusiness.getChannelCode(),
                    channelBusiness.getUniqueId());
            } else {
                personDataManagerLogicService.deleteLibraryPerson(channelBusiness.getChannelCode(),
                    channelBusiness.getUniqueId());
            }
            return true;
        } catch (Exception e) {
            log.error("Syncing scene library personnel data to Datamanager is abnormal,channelCode:[{}],uniqueId:[{}]", channelBusiness.getChannelCode(),
                channelBusiness.getUniqueId(), e);
            return false;
        }
    }

    /**
     * 场景人员导入
     * 
     * @param excelFile
     * @param channelId
     * @param updateSupport
     * @return
     * @throws Exception
     * @throws IOException
     */
    @Override
    public String saveImportData(MultipartFile excelFile, String channelId, Boolean updateSupport)
        throws IOException, Exception {
        return saveImportData(excelFile.getInputStream(), channelId, updateSupport);
    }

    /**
     * 保存导入数据
     * 
     * @param inputStream
     * @param channelId
     * @param updateSupport
     * @return
     * @throws Exception
     * @throws IOException
     */
    @Override
    public String saveImportData(InputStream inputStream, String channelId, Boolean updateSupport)
        throws IOException, Exception {
        // 查询场景信息
        ChannelInfo channelInfo = channelInfoMapper.selectChannelInfoById(channelId);
        if (null == channelInfo) {
            throw new CustomException(MessageUtils.message("channel.busi.service.scene.not.exist"));
        }
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        ExcelUtil<ChannelBusiness> util = new ExcelUtil<ChannelBusiness>(ChannelBusiness.class);
        List<ChannelBusiness> infoList = util.importExcel(inputStream);;
        Map<String, Object> importResMap = importBusiInfoData(infoList, updateSupport, channelInfo);
        Integer importFailNum = (Integer)importResMap.get("failureNum");
        if (null != importFailNum && importFailNum > 0) {
            failureMsg.append(importResMap.get("failureMsg")).append("<br/>");
        }
        if (failureMsg.length() > 0) {
            throw new CustomException(failureMsg.toString());
        }
        String importSuccMsg = (String)importResMap.get("successMsg");
        if (StringUtils.isNotBlank(importSuccMsg)) {
            successMsg = successMsg.append(StringUtils.nvl(importSuccMsg, "")).append("<br/>");
        }
        return successMsg.toString();
    }

    /**
     * 执行导入数据保存
     * 
     * @param infoList
     * @param isUpdateSupport
     * @param channelInfo
     * @return
     */
    private Map<String, Object> importBusiInfoData(List<ChannelBusiness> importBusiList, boolean isUpdateSupport,
        ChannelInfo channelInfo) {
        if (CollectionUtils.isEmpty(importBusiList)) {
            log.error("Import scene personnel data is empty!");
            throw new CustomException(MessageUtils.message("channel.busi.service.scene.person.empty"));
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (ChannelBusiness busi : importBusiList) {
            try {
                // 进行字段合法性校验
                String uniqueId = busi.getUniqueId();// 唯一标识
                if (StringUtils.isBlank(uniqueId) || uniqueId.length() > 48
                    || !Pattern.compile("[\u4e00-\u9fa5\\w]+").matcher(uniqueId).matches()) {
                    throw new CustomException(MessageUtils.message("channel.busi.service.scene.person.uniqueid.format.invalid"));
                }
                BasePersonInfo infoCondition = new BasePersonInfo();
                infoCondition.setUniqueId(busi.getUniqueId());
                infoCondition.setStatus(DictConstants.Status.ENABLE);
                List<BasePersonInfo> personList = basePersonInfoMapper.selectBasePersonInfoList(infoCondition);
                if (CollectionUtils.isEmpty(personList)) {
                    failureNum++;
                    String msg = MessageUtils.message("channel.busi.service.import.person.baseinfo.not.exists", busi.getUniqueId());
                    failureMsg.append("<br/>" + failureNum + msg);
                    continue;
                }
                BasePersonInfo person = personList.get(0);
                // 查询场景人员是否已经存在(有效或者无效)
                boolean existsFlag = false;
                ChannelBusiness busiCondition = new ChannelBusiness();
                busiCondition.setChannelId(channelInfo.getId());
                busiCondition.setPersonId(person.getId());
                List<ChannelBusiness> businessList = channelBusinessMapper.selectChannelBusinessList(busiCondition);
                existsFlag = CollectionUtils.isNotEmpty(businessList);
                String loginName = null;
                try {
                    loginName = SecurityUtils.getUsername();
                } catch (Exception e) {
                }
                Date now = new Date();
                busi.setDatasource(DictConstants.DataSource.IMP);
                busi.setChannelId(channelInfo.getId());
                busi.setPersonId(person.getId());
                busi.setFaceMode(channelInfo.getFaceMode());
                busi.setFingerMode(channelInfo.getFingerMode());
                busi.setIrisMode(channelInfo.getIrisMode());
                busi.setFveinMode(channelInfo.getFveinMode());
                busi.setFaceIrisMode(channelInfo.getFaceIrisMode());
                if (!existsFlag) {// 不存在, 直接入库保存
                    busi.setId(IdWorker.getNextStringId());
                    busi.setCreateBy(loginName);
                    busi.setCreateTime(now);
                    // 更新标识序列号
                    Long updateSeriaNum = busiLiveUpdateSeqService.incrementAndGetChannelBusiSeqNum();
                    busi.setUpdateSeriaNum(updateSeriaNum);
                    channelBusinessMapper.insertChannelBusiness(busi);
                    personDataManagerLogicService.addLibraryPerson(channelInfo.getChannelCode(), busi.getUniqueId());
                    successNum++;
                    continue;
                }
                // 有效人员是否存在
                boolean anyMatch =
                    businessList.stream().anyMatch(it -> DictConstants.Status.ENABLE.equals(it.getStatus()));
                if (anyMatch && isUpdateSupport || !anyMatch) {// 存在有效且支持更新,或者只是存在无效人员信息
                    busi.setId(businessList.get(0).getId());
                    busi.setCreateTime(anyMatch ? null : now);
                    busi.setCreateBy(anyMatch ? null : loginName);
                    busi.setUpdateBy(loginName);
                    busi.setUpdateTime(now);
                    busi.setStatus(DictConstants.Status.ENABLE);
                    // 更新标识序列号
                    Long updateSeriaNum = busiLiveUpdateSeqService.incrementAndGetChannelBusiSeqNum();
                    busi.setUpdateSeriaNum(updateSeriaNum);
                    channelBusinessMapper.updateChannelBusiness(busi);
                    personDataManagerLogicService.addLibraryPerson(channelInfo.getChannelCode(), busi.getUniqueId());
                    successNum++;
                } else {// 存在有效但是不支持更新
                    failureNum++;
                    String msg = MessageUtils.message("channel.busi.service.import.person.baseinfo.exists", busi.getUniqueId());
                    failureMsg.append("<br/>" + failureNum + msg);
                }
            } catch (Exception e) {
                failureNum++;
                String t = MessageUtils.message("channel.busi.service.import.person.failed", busi.getUniqueId(),e.getMessage());
                String msg = "<br/>" + failureNum + t;
                failureMsg.append(msg);
                log.error(msg, e);
            }
        }
        if (failureNum > 0) {
            String msg = MessageUtils.message("channel.busi.service.import.person.failed.sumary", failureNum);
            failureMsg.insert(0, msg);
            log.info(failureMsg.toString());
        } else {
            String msg = MessageUtils.message("channel.busi.service.import.person.success.sumary", successNum);
            successMsg.insert(0, msg);
            log.info(successMsg.toString());
        }
        Map<String, Object> resMap = new HashMap<>();
        resMap.put("failureNum", failureNum);
        resMap.put("failureMsg", failureMsg.toString());
        resMap.put("successNum", successNum);
        resMap.put("successMsg", successMsg.toString());
        return resMap;
    }

}
