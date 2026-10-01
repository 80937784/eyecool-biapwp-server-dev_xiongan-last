package cn.eyecool.common.core.domain.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.alibaba.fastjson.JSON;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 租户角色表 sys_tenant_role
 * 
 * @author admin
 */
public class SysTenantRole extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 角色ID */
    @Excel(name = "sys.tenant.role.id")
    private String id;

    /** 角色名称 */
    @Excel(name = "sys.tenant.role.name")
    private String roleName;

    /** 角色权限 */
    @Excel(name = "sys.tenant.role.key")
    private String roleKey;

    /** 角色排序 */
    @Excel(name = "sys.tenant.role.sort")
    private String roleSort;

    /** 菜单树选择项是否关联显示（ 0：父子不互相关联显示 1：父子互相关联显示） */
    private boolean menuCheckStrictly;

    /** 角色状态（0正常 1停用） */
    @Excel(name = "sys.tenant.role.status", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 用户是否存在此角色标识 默认不存在 */
    private boolean flag = false;

    /** 菜单组 */
    private Long[] menuIds;

    public SysTenantRole() {

    }

    public SysTenantRole(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isTrialRole() {
        return isTrialRole(this.roleKey);
    }

    public static boolean isTrialRole(String roleKey) {
        return "TRIAL".equals(roleKey);
    }

    @NotBlank(message = "sys.tenant.role.name.empty")
    @Size(min = 0, max = 30, message = "sys.tenant.role.name.max.length.limit")
    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @NotBlank(message = "sys.tenant.role.key.empty")
    @Size(min = 0, max = 100, message = "sys.tenant.role.key.max.length.limit")
    public String getRoleKey() {
        return roleKey;
    }

    public void setRoleKey(String roleKey) {
        this.roleKey = roleKey;
    }

    @NotBlank(message = "sys.tenant.role.sort.empty")
    public String getRoleSort() {
        return roleSort;
    }

    public void setRoleSort(String roleSort) {
        this.roleSort = roleSort;
    }

    public boolean isMenuCheckStrictly() {
        return menuCheckStrictly;
    }

    public void setMenuCheckStrictly(boolean menuCheckStrictly) {
        this.menuCheckStrictly = menuCheckStrictly;
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

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public Long[] getMenuIds() {
        return menuIds;
    }

    public void setMenuIds(Long[] menuIds) {
        this.menuIds = menuIds;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
