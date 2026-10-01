package cn.eyecool.common.core.domain.entity;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.annotation.Excel.ColumnType;
import cn.eyecool.common.annotation.Excel.Type;
import cn.eyecool.common.annotation.Excels;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 用户对象 sys_user
 * 
 * @author admin
 */
public class SysUser extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @Excel(name = "sys.user.userid", cellType = ColumnType.NUMERIC, prompt = "sys.user.userid.prompt")
    private Long userId;

    /** 部门ID */
    @Excel(name = "sys.user.deptid", type = Type.IMPORT)
    private Long deptId;

    /** 用户账号 */
    @Excel(name = "sys.user.username")
    private String userName;

    /** 用户昵称 */
    @Excel(name = "sys.user.nickname")
    private String nickName;

    /** 用户邮箱 */
    @Excel(name = "sys.user.email")
    private String email;

    /** 手机号码 */
    @Excel(name = "sys.user.phone")
    private String phonenumber;

    /** 用户性别 */
    @Excel(name = "sys.user.sex", readConverterExp = "0=sys.user.sex.male,1=sys.user.sex.female,2=sys.user.sex.unknown")
    private String sex;

    /** 用户头像 */
    private String avatar;

    /** 密码 */
    private String password;

    /** 盐加密 */
    private String salt;

    /** 帐号状态（0正常 1停用） */
    @Excel(name = "sys.user.status", readConverterExp = "0=sys.user.status.normal,1=sys.user.status.disabled")
    private String status;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 最后登录IP */
    private String loginIp;

    /** 最后登录时间 */
    private Date loginDate;

    /** 部门对象 */
    @Excels({@Excel(name = "sys.user.deptname", targetAttr = "deptName", type = Type.EXPORT),
        @Excel(name = "sys.user.deptleader", targetAttr = "leader", type = Type.EXPORT)})
    private SysDept dept;

    /** 角色对象 */
    private List<SysRole> roles;

    /** 角色组 */
    private Long[] roleIds;

    /** 岗位组 */
    private Long[] postIds;

    /** 租户ID */
    private String tenantId;

    /** 是否租户管理员 */
    private String tenantAdmin;

    public SysUser() {

    }

    public SysUser(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isAdmin() {
        return isAdmin(this.userId);
    }

    public static boolean isAdmin(Long userId) {
        return userId != null && 1L == userId;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    @Size(min = 0, max = 30, message = "sys.user.nickname.max.length.limit")
    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    @NotBlank(message = "sys.user.username.empty")
    @Size(min = 0, max = 30, message = "sys.user.username.max.length.limit")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Email(message = "sys.user.email.format.wrong")
    @Size(min = 0, max = 50, message = "sys.user.email.max.length.limit")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Size(min = 0, max = 11, message = "sys.user.phone.max.length.limit")
    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @JsonIgnore
    @JsonProperty
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getLoginIp() {
        return loginIp;
    }

    public void setLoginIp(String loginIp) {
        this.loginIp = loginIp;
    }

    public Date getLoginDate() {
        return loginDate;
    }

    public void setLoginDate(Date loginDate) {
        this.loginDate = loginDate;
    }

    public SysDept getDept() {
        return dept;
    }

    public void setDept(SysDept dept) {
        this.dept = dept;
    }

    public List<SysRole> getRoles() {
        return roles;
    }

    public void setRoles(List<SysRole> roles) {
        this.roles = roles;
    }

    public Long[] getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(Long[] roleIds) {
        this.roleIds = roleIds;
    }

    public Long[] getPostIds() {
        return postIds;
    }

    public void setPostIds(Long[] postIds) {
        this.postIds = postIds;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantAdmin() {
        return tenantAdmin;
    }

    public void setTenantAdmin(String tenantAdmin) {
        this.tenantAdmin = tenantAdmin;
    }

    public boolean isTenantSuperUser() {
        return isTenantSuperUser(this.tenantAdmin);
    }

    public static boolean isTenantSuperUser(String tenantAdmin) {
        return DictConstants.YesOrNoState.YES.equalsIgnoreCase(tenantAdmin);
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
