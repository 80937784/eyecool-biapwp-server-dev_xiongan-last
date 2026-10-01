package cn.eyecool.system.service.impl;

import cn.eyecool.common.config.DigitalSystemConfig;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.UserConstants;
import cn.eyecool.common.core.domain.entity.SysDept;
import cn.eyecool.common.core.domain.entity.SysTenant;
import cn.eyecool.common.core.domain.entity.SysUser;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.*;
import cn.eyecool.common.utils.http.HttpClientUtil;
import cn.eyecool.common.utils.sign.EyecoolPmSign;
import cn.eyecool.system.constant.RedisKeyConstants;
import cn.eyecool.system.mapper.SysDeptMapper;
import cn.eyecool.system.mapper.SysTenantMapper;
import cn.eyecool.system.mapper.SysUserMapper;
import cn.eyecool.system.service.ISysTenantService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 租户信息Service业务层处理
 *
 * @author admin
 * @date 2020-11-04
 */
@Service
public class SysTenantServiceImpl implements ISysTenantService {

    private static final Logger LOG = LoggerFactory.getLogger(SysTenantServiceImpl.class);

    @Autowired
    private SysTenantMapper sysTenantMapper;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private SysDeptMapper deptMapper;
    @Autowired
    private RedisCache redisCache;

    @Autowired
    private DigitalSystemConfig digitalSystemConfig;

    /**
     * 查询租户信息
     *
     * @param id 租户信息ID
     * @return 租户信息
     */
    @Override
    public SysTenant selectSysTenantById(Long id) {
        return sysTenantMapper.selectSysTenantById(id);
    }

    /**
     * 查询租户信息列表
     *
     * @param sysTenant 租户信息
     * @return 租户信息
     */
    @Override
    public List<SysTenant> selectSysTenantList(SysTenant sysTenant) {
        return sysTenantMapper.selectSysTenantList(sysTenant);
    }

    /**
     * 新增租户信息
     *
     * @param sysTenant 租户信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertSysTenant(SysTenant sysTenant) {
        // 校验租户名称是否唯一
        if (!checkTenantNameUnique(null, sysTenant.getTenantName())) {
            throw new CustomException(MessageUtils.message("systenant.service.tenantname.duplicate"));
        }
        // 校验手机号是否唯一
        if (!checkPhoneUnique(null, sysTenant.getPhone())) {
            throw new CustomException(MessageUtils.message("systenant.service.tenantphone.used"));
        }
        // 校验是否存在和租户手机号重复的用户名
        int count = sysUserMapper.checkUserNameUnique(sysTenant.getPhone());
        if (count > 0) {
            LOG.error("The mobile phone number reserved by the new tenant [{}] has been occupied by other users",
                sysTenant.getPhone());
            throw new CustomException(
                MessageUtils.message("systenant.service.tenantphone.used.need.update", sysTenant.getPhone()));
        }
        SysUser info = sysUserMapper.checkPhoneUnique(sysTenant.getPhone());
        if (null != info) {
            LOG.error("The mobile phone number [{}] reserved by the new tenant has been occupied by other users [{}]",
                sysTenant.getPhone(), info.getUserName());
            throw new CustomException(
                MessageUtils.message("systenant.service.tenantphone.used.need.update", sysTenant.getPhone()));
        }
        if (StringUtils.isNotBlank(sysTenant.getEmail())) {
            SysUser userInfo = sysUserMapper.checkEmailUnique(sysTenant.getEmail());
            if (null != userInfo) {
                LOG.error("The newly added tenant reserved mailbox [{}] has been occupied by other users [{}]",
                    sysTenant.getEmail(), userInfo.getUserName());
                throw new CustomException(
                    MessageUtils.message("systenant.service.tenantemail.used", sysTenant.getEmail()));
            }
        }
        sysTenant.setCreateTime(DateUtils.getNowDate());
        sysTenant.setUpdateTime(DateUtils.getNowDate());
        // 生成租户ID
        String tenantId = generateTenantId();
        sysTenant.setTenantId(tenantId);
        int row = sysTenantMapper.insertSysTenant(sysTenant);
        if (row > 0) {
            // 新增顶级部门
            SysDept dept = new SysDept();
            dept.setDeptName(sysTenant.getTenantName());
            dept.setParentId(0L);
            dept.setAncestors("0");
            dept.setDeptCode(sysTenant.getTenantId());
            dept.setTenantId(sysTenant.getTenantId());
            deptMapper.insertDept(dept);
            SysDept defaultDept = new SysDept();
            defaultDept.setDeptName(MessageUtils.message("systenant.service.default.deptname"));
            defaultDept.setParentId(dept.getDeptId());
            defaultDept.setAncestors("0," + dept.getDeptId());
            defaultDept.setDeptCode(UserConstants.DEFAULT_DEPT_CODE);
            defaultDept.setTenantId(sysTenant.getTenantId());
            deptMapper.insertDept(defaultDept);
            // 新增租户下超级用户
            SysUser user = new SysUser();
            user.setUserName(sysTenant.getPhone());
            user.setNickName(sysTenant.getTenantName());
            user.setPhonenumber(sysTenant.getPhone());
            user.setEmail(sysTenant.getEmail());
            user.setTenantAdmin(DictConstants.YesOrNoState.YES);
            user.setTenantId(sysTenant.getTenantId());
            user.setPassword(SecurityUtils.encryptPassword("123456"));
            user.setDeptId(defaultDept.getDeptId());
            sysUserMapper.insertUser(user);
            // 新增商机
            addBusiOpportunity(sysTenant);
            redisCache.setCacheObject(RedisKeyConstants.TENANT_REDIS_KEY_PREFIX + sysTenant.getTenantId(), sysTenant);
        }
        return row;
    }

    /**
     * 新增商机
     *
     * @param sysTenant
     */
    private void addBusiOpportunity(SysTenant sysTenant) {
        if (Boolean.FALSE.equals(digitalSystemConfig.getEnabled())) {
            LOG.info(
                "Tenant registration does not turn on the docking switch of the business opportunity interface of the digital integrated management system");
            return;
        }
        if (!DictConstants.TenantSource.REGIST.equals(sysTenant.getCreateSource())) {
            LOG.info(
                "Non-official website self-registration tenants [{}] do not need to connect with the business opportunity interface of the digital integrated management system",
                sysTenant.getTenantId());
            return;
        }
        Map<String, String> header = Maps.newHashMap();
        Map<String, String> param = Maps.newHashMap();
        String transCode = "BUSINESS_INFO_INSERT";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = IdWorker.getNextStringId();
        param.put("appKey", digitalSystemConfig.getAppKey());
        param.put("transCode", transCode);
        param.put("timestamp", timestamp);
        param.put("nonce", nonce);
        param.put("sign", EyecoolPmSign.generateSign(digitalSystemConfig.getAppKey(), transCode, timestamp, nonce,
            digitalSystemConfig.getAppSecrect()));
        JSONObject bizObj = new JSONObject();
        bizObj.put("cusName", sysTenant.getTenantName());
        bizObj.put("contactName", sysTenant.getContactName());
        bizObj.put("contactPhone", sysTenant.getPhone());
        bizObj.put("contactEmail", sysTenant.getEmail());
        String bizContent = JSON.toJSONString(bizObj);
        param.put("bizContent", bizContent);
        header.put("Content-Type", "application/x-www-form-urlencoded");
        LOG.info(
            "Invoke digital integrated management system to add business opportunity interface ,url:[{}],param:[{}]",
            digitalSystemConfig.getBaseUrl(), JSON.toJSONString(param));
        String response = HttpClientUtil.doPost(digitalSystemConfig.getBaseUrl(), param, header);
        LOG.info("Invoke digital integrated management system to add business opportunity interface,response:[{}]",
            response);
        if (StringUtils.isBlank(response)) {
            throw new CustomException(MessageUtils.message("systenant.service.system.exception"));
        }
        JSONObject parseObject = JSON.parseObject(response);
        if (!"0".equals(parseObject.getString("code"))) {
            throw new CustomException(parseObject.getString("msg"));
        }
    }

    /**
     * 生成租户ID
     *
     * @return
     */
    private String generateTenantId() {
        Long seq = redisCache.incrementAndGet(RedisKeyConstants.TENANT_MAX_ID_KEY);
        if (null != seq) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Get the current maximum tenant ID sequence number from the cache:[{}]", seq);
            }
            return "ZH" + String.format("%08d", seq);
        }
        synchronized (SysTenantServiceImpl.class) {
            seq = redisCache.incrementAndGet(RedisKeyConstants.TENANT_MAX_ID_KEY);
            if (null == seq) {
                String maxTenantId = sysTenantMapper.selectMaxTenantId();
                if (StringUtils.isBlank(maxTenantId)) {
                    seq = 1L;
                } else {
                    seq = Long.valueOf(maxTenantId.substring(2));
                }
                seq = redisCache.addAndGetLong(RedisKeyConstants.TENANT_MAX_ID_KEY, seq);
            }
        }
        return "ZH" + String.format("%08d", seq);
    }

    /**
     * 修改租户信息
     *
     * @param sysTenant 租户信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateSysTenant(SysTenant sysTenant) {
        // 校验租户名称是否唯一
        if (!checkTenantNameUnique(sysTenant.getTenantId(), sysTenant.getTenantName())) {
            throw new CustomException(MessageUtils.message("systenant.service.tenantname.duplicate"));
        }
        // 校验租户联系手机号是否唯一
        if (!checkPhoneUnique(sysTenant.getTenantId(), sysTenant.getPhone())) {
            throw new CustomException(MessageUtils.message("systenant.service.tenantphone.used"));
        }
        sysTenant.setUpdateTime(DateUtils.getNowDate());
        int row = sysTenantMapper.updateSysTenant(sysTenant);
        if (row > 0) {
            redisCache.setCacheObject(RedisKeyConstants.TENANT_REDIS_KEY_PREFIX + sysTenant.getTenantId(), sysTenant);
        }
        return row;
    }

    /**
     * 批量删除租户信息
     *
     * @param ids 需要删除的租户信息ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteSysTenantByIds(Long[] ids) {
        int count = 0;
        for (Long id : ids) {
            count += deleteSysTenantById(id);
        }
        return count;
    }

    /**
     * 删除租户信息信息
     *
     * @param id 租户信息ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteSysTenantById(Long id) {
        SysTenant tenant = sysTenantMapper.selectSysTenantById(id);
        if (UserConstants.SUPER_TENANT.equals(tenant.getTenantId())) {
            throw new CustomException(MessageUtils.message("systenant.service.supertenant.prohibit.delete"));
        }
        redisCache.deleteObject(RedisKeyConstants.TENANT_REDIS_KEY_PREFIX + tenant.getTenantId());
        return sysTenantMapper.deleteSysTenantById(id);
    }

    /**
     * 校验租户名称是否唯一
     *
     * @param excludeTenantId
     * @param tenantName
     * @return
     */
    @Override
    public boolean checkTenantNameUnique(String excludeTenantId, String tenantName) {
        String tmpName = StringUtils.isBlank(tenantName) ? StringUtils.EMPTY : tenantName;
        SysTenant info = sysTenantMapper.selectSysTenantByTenantName(tmpName);
        return StringUtils.isNull(info) || (StringUtils.isNotEmpty(excludeTenantId) && info.getTenantId()
            .equals(excludeTenantId));
    }

    /**
     * 校验租户预留手机号是否唯一
     *
     * @param excludeTenantId
     * @param phone
     * @return
     */
    @Override
    public boolean checkPhoneUnique(String excludeTenantId, String phone) {
        String tmpPhone = StringUtils.isBlank(phone) ? StringUtils.EMPTY : phone;
        SysTenant info = sysTenantMapper.selectSysTenantByPhone(tmpPhone);
        return StringUtils.isNull(info) || (StringUtils.isNotEmpty(excludeTenantId) && info.getTenantId()
            .equals(excludeTenantId));
    }

    /**
     * 查询租户信息
     *
     * @param tenantId
     * @return
     * @see cn.eyecool.system.service.ISysTenantService#selectSysTenantByTenantId(java.lang.String)
     */
    @Override
    public SysTenant selectSysTenantByTenantId(String tenantId) {
        SysTenant tenant = (SysTenant)redisCache.getCacheObject(RedisKeyConstants.TENANT_REDIS_KEY_PREFIX + tenantId);
        if (null == tenant) {
            tenant = sysTenantMapper.selectSysTenantByTenantId(tenantId);
            if (null != tenant) {
                redisCache.setCacheObject(RedisKeyConstants.TENANT_REDIS_KEY_PREFIX + tenantId, tenant);
            }
        }
        return tenant;
    }
}
