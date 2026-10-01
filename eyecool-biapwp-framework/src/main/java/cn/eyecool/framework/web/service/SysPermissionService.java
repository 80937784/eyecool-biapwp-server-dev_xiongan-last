package cn.eyecool.framework.web.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.constant.UserConstants;
import cn.eyecool.common.core.domain.entity.SysTenant;
import cn.eyecool.common.core.domain.entity.SysUser;
import cn.eyecool.system.service.ISysMenuService;
import cn.eyecool.system.service.ISysRoleService;
import cn.eyecool.system.service.ISysTenantService;

/**
 * 用户权限处理
 * 
 * @author admin
 */
@Component
public class SysPermissionService {
    @Autowired
    private ISysRoleService roleService;
    @Autowired
    private ISysMenuService menuService;
    @Autowired
    private ISysTenantService tenantService;
    @Autowired
    private TenantProperties tenantProperties;

    /**
     * 获取角色数据权限
     * 
     * @param user 用户信息
     * @return 角色权限信息
     */
    public Set<String> getRolePermission(SysUser user) {
        Set<String> roles = new HashSet<String>();
        // 管理员拥有所有权限
        if (user.isAdmin() || user.isTenantSuperUser()) {
            roles.add("admin");
        } else {
            roles.addAll(roleService.selectRolePermissionByUserId(user.getUserId()));
        }
        return roles;
    }

    /**
     * 获取菜单数据权限
     * 
     * @param user 用户信息
     * @return 菜单权限信息
     */
    public Set<String> getMenuPermission(SysUser user) {
        Set<String> perms = new HashSet<String>();
        // 管理员拥有所有权限
        if (user.isAdmin()) {
            perms.add("*:*:*");
        } else if (user.isTenantSuperUser()) {
            SysTenant tenant = tenantService.selectSysTenantByTenantId(user.getTenantId());
            perms.addAll(menuService.selectMenuPermsByTenantRoleId(tenant.getTenantRoleId()));
        } else {
            Set<String> userPerms = menuService.selectMenuPermsByUserId(user.getUserId());
            // 防止租户下的用户角色权限大于当前租户本身的角色权限，先进行一个权限过滤，只返回包含在租户本身角色权限范围内的权限
            if (tenantProperties.getEnabled() && !UserConstants.SUPER_TENANT.equals(user.getTenantId())) {
                SysTenant tenant = tenantService.selectSysTenantByTenantId(user.getTenantId());
                Set<String> tenantRolePerms = menuService.selectMenuPermsByTenantRoleId(tenant.getTenantRoleId());
                userPerms.retainAll(tenantRolePerms);
            }
            perms.addAll(userPerms);
        }
        return perms;
    }
}
