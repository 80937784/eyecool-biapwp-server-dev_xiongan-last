package cn.eyecool.system.domain;

import com.alibaba.fastjson.JSON;

/**
 * 租户和菜单关联 sys_tenant_menu
 * 
 * @author admin
 */
public class SysTenantMenu
{
    /** 租户ID */
    private String tenantId;
    
    /** 菜单ID */
    private Long menuId;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Long getMenuId()
    {
        return menuId;
    }

    public void setMenuId(Long menuId)
    {
        this.menuId = menuId;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
