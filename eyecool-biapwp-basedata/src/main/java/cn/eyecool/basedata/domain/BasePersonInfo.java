package cn.eyecool.basedata.domain;

import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.annotation.Excel.Type;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 人员基础信息对象 base_person_info
 * 
 * @author mawj
 * @date 2021-01-27
 */
public class BasePersonInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 人员标识 */
    @Excel(name = "base.person.info.unique")
    private String uniqueId;

    /** 姓名 */
    @Excel(name = "base.person.info.name")
    private String name;

    /** 性别：0-男 1-女 2-未知 */
    @Excel(name = "base.person.info.sex", dictType = "sys_user_sex")
    private String sex;

    /** 手机 */
    @Excel(name = "base.person.info.phone")
    private String phone;

    /** 卡号 */
    @Excel(name = "base.person.info.card.number")
    private String cardNo;

    /** 账号 */
    @Excel(name = "base.person.info.account")
    private String account;

    /** 邮箱 */
    @Excel(name = "base.person.info.email")
    private String email;

    /** 部门ID */
    @Excel(name = "base.person.info.dept.id", type = Type.EXPORT)
    private Long deptId;

    /** 部门编码 */
    @Excel(name = "base.person.info.dept.code")
    private String deptCode;

    /** 部门名称 */
    @Excel(name = "base.person.info.dept.name", type = Type.EXPORT)
    private String deptName;

    /** 数据来源：INTERFACE-内部接口 IMP-导入 HTTP-HTTP接口 */
    @Excel(name = "base.person.info.data.source", dictType = "apply_data_source", type = Type.EXPORT)
    private String datasource;

    /** 人员标记：1-正常 2-红名单 3-黑名单 */
    @Excel(name = "base.person.info.person.mark", dictType = "apply_person_flag")
    private String flag;

    /** 状态：0-有效 1:无效 */
    @Excel(name = "base.person.info.status", dictType = "sys_normal_disable", type = Type.EXPORT)
    private String status;

    /** 定时任务执行时间(执行定时任务时使用此字段) */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date batchDate;

    /** 租户ID */
    private String tenantId;

    /** 拓展属性JSON */
    private String extAttrs;

    /** 是否有人脸数据 */
    private Boolean hasFace;
    /** 是否有指纹数据 */
    private Boolean hasFinger;
    /** 是否有虹膜数据 */
    private Boolean hasIris;
    /** 是否有指静脉数据 */
    private Boolean hasFvein;
    /** 是否有人脸虹膜多模态数据 */
    private Boolean hasFaceIris;

    /** 访客系统添加字段-开始 */
    /** 人员类型：user-人员 visitor-访客(用于访客系统) */
    private String personType;
    /** 被访人id(用于访客系统) */
    private String inviterId;
    /** 访客状态：0-启用 1-禁用(用于访客系统) */
    private String visitorStatus;
    /** 生效开始时间(用于访客系统) */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "base.person.info.effective.beginTime", dateFormat="yyyy-MM-dd HH:mm:ss")
    private Date effectiveBeginTime;
    /** 生效结束时间(用户访客系统) */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "base.person.info.effective.endTime", dateFormat="yyyy-MM-dd HH:mm:ss")
    private Date effectiveEndTime;

    /** 访客系统添加字段-结束 */

    /** 绑定的场景编码列表，用,分割 */
    private String channels;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getSex() {
        return sex;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAccount() {
        return account;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Long getDeptId() {
        return deptId;
    }

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getFlag() {
        return flag;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setBatchDate(Date batchDate) {
        this.batchDate = batchDate;
    }

    public Date getBatchDate() {
        return batchDate;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getExtAttrs() {
        return extAttrs;
    }

    public void setExtAttrs(String extAttrs) {
        this.extAttrs = extAttrs;
    }

    public Boolean getHasFace() {
        return hasFace;
    }

    public void setHasFace(Boolean hasFace) {
        this.hasFace = hasFace;
    }

    public Boolean getHasFinger() {
        return hasFinger;
    }

    public void setHasFinger(Boolean hasFinger) {
        this.hasFinger = hasFinger;
    }

    public Boolean getHasIris() {
        return hasIris;
    }

    public void setHasIris(Boolean hasIris) {
        this.hasIris = hasIris;
    }

    public Boolean getHasFvein() {
        return hasFvein;
    }

    public void setHasFvein(Boolean hasFvein) {
        this.hasFvein = hasFvein;
    }

    public Boolean getHasFaceIris() {
        return hasFaceIris;
    }

    public void setHasFaceIris(Boolean hasFaceIris) {
        this.hasFaceIris = hasFaceIris;
    }

    public String getPersonType() {
        return personType;
    }

    public void setPersonType(String personType) {
        this.personType = personType;
    }

    public String getInviterId() {
        return inviterId;
    }

    public void setInviterId(String inviterId) {
        this.inviterId = inviterId;
    }

    public String getVisitorStatus() {
        return visitorStatus;
    }

    public void setVisitorStatus(String visitorStatus) {
        this.visitorStatus = visitorStatus;
    }

    public Date getEffectiveBeginTime() {
        return effectiveBeginTime;
    }

    public void setEffectiveBeginTime(Date effectiveBeginTime) {
        this.effectiveBeginTime = effectiveBeginTime;
    }

    public Date getEffectiveEndTime() {
        return effectiveEndTime;
    }

    public void setEffectiveEndTime(Date effectiveEndTime) {
        this.effectiveEndTime = effectiveEndTime;
    }

    public String getChannels() {
        return channels;
    }

    public void setChannels(String channels) {
        this.channels = channels;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
