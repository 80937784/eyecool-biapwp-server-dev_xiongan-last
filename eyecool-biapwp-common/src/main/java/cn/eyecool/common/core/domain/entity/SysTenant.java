package cn.eyecool.common.core.domain.entity;

import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.constant.UserConstants;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 租户信息对象 sys_tenant
 * 
 * @author admin
 * @date 2020-11-04
 */
public class SysTenant extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 租户ID */
    @Excel(name = "sys.tenant.id")
    private String tenantId;

    /** 租户名称 */
    @Excel(name = "sys.tenant.name")
    private String tenantName;

    /** 租户描述 */
    @Excel(name = "sys.tenant.description")
    private String tenantDesc;

    /** 租户状态(NORMAL: 正常， FROZEN: 冻结) */
    @Excel(name = "sys.tenant.status", dictType = "sys_tenant_state")
    private String tenantState;

    /** 创建来源(REGIST: 注册，BG_CREATE: 后台创建 ) */
    @Excel(name = "sys.tenant.source", dictType = "sys_tenant_source")
    private String createSource;

    /** 生效时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "sys.tenant.active.time", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date effectiveTime;

    /** 失效时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "sys.tenant.inactive.time", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    /** 租户类型 */
    @Excel(name = "sys.tenant.type", dictType = "sys_tenant_type")
    private String tenantType;

    /** 联系人 */
    @Excel(name = "sys.tenant.contact.name")
    private String contactName;

    /** 联系电话 */
    @Excel(name = "sys.tenant.contact.phone")
    private String phone;

    /** 电子邮箱 */
    @Excel(name = "sys.tenant.contact.email")
    private String email;

    /** 租户角色Id */
    private String tenantRoleId;

    /** 注册验证码 */
    private String captcha;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantDesc(String tenantDesc) {
        this.tenantDesc = tenantDesc;
    }

    public String getTenantDesc() {
        return tenantDesc;
    }

    public void setTenantState(String tenantState) {
        this.tenantState = tenantState;
    }

    public String getTenantState() {
        return tenantState;
    }

    public void setCreateSource(String createSource) {
        this.createSource = createSource;
    }

    public String getCreateSource() {
        return createSource;
    }

    public void setEffectiveTime(Date effectiveTime) {
        this.effectiveTime = effectiveTime;
    }

    public Date getEffectiveTime() {
        return effectiveTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public boolean isSuperTenant() {
        return UserConstants.SUPER_TENANT.equals(this.tenantId);
    }

    public String getTenantType() {
        return tenantType;
    }

    public void setTenantType(String tenantType) {
        this.tenantType = tenantType;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTenantRoleId() {
        return tenantRoleId;
    }

    public void setTenantRoleId(String tenantRoleId) {
        this.tenantRoleId = tenantRoleId;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
