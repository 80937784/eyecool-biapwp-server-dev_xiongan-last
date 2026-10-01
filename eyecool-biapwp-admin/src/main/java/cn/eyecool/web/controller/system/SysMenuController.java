package cn.eyecool.web.controller.system;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.eyecool.common.annotation.Log;
import cn.eyecool.common.constant.Constants;
import cn.eyecool.common.constant.UserConstants;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.entity.SysMenu;
import cn.eyecool.common.core.domain.model.LoginUser;
import cn.eyecool.common.enums.BusinessType;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.ServletUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.framework.web.service.TokenService;
import cn.eyecool.system.service.ISysMenuService;

/**
 * 菜单信息
 * 
 * @author admin
 */
@RestController
@RequestMapping("/system/menu")
public class SysMenuController extends BaseController {
    @Autowired
    private ISysMenuService menuService;
    @Autowired
    private TokenService tokenService;

    /**
     * 获取菜单列表
     */
    @PreAuthorize("@ss.hasPermi('system:menu:list')")
    @GetMapping("/list")
    public AjaxResult list(SysMenu menu) {
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        Long userId = loginUser.getUser().getUserId();
        List<SysMenu> menus = menuService.selectMenuList(menu, userId);
        return AjaxResult.success(menus);
    }

    /**
     * 根据菜单编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:menu:query')")
    @GetMapping(value = "/{menuId}")
    public AjaxResult getInfo(@PathVariable Long menuId) {
        return AjaxResult.success(menuService.selectMenuById(menuId));
    }

    /**
     * 获取菜单下拉树列表
     */
    @GetMapping("/treeselect")
    public AjaxResult treeselect(SysMenu menu) {
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        Long userId = loginUser.getUser().getUserId();
        List<SysMenu> menus = menuService.selectMenuList(menu, userId);
        return AjaxResult.success(menuService.buildMenuTreeSelect(menus));
    }

    /**
     * 加载对应角色菜单列表树
     */
    @GetMapping(value = {"/roleMenuTreeselect/{roleId}", "/tenantRoleMenuTreeselect/{tenantRoleId}"})
    public AjaxResult roleMenuTreeselect(@PathVariable(value = "roleId", required = false) Long roleId,
        @PathVariable(value = "tenantRoleId", required = false) String tenantRoleId) {
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        List<SysMenu> menus = menuService.selectMenuList(loginUser.getUser().getUserId());
        AjaxResult ajax = AjaxResult.success();
        List<Long> checkedKeys = Collections.emptyList();
        if (StringUtils.isNull(roleId)) {
            checkedKeys = menuService.selectMenuListByTenantRoleId(tenantRoleId);
        } else {
            checkedKeys = menuService.selectMenuListByRoleId(roleId);
        }
        ajax.put("checkedKeys", checkedKeys);
        ajax.put("menus", menuService.buildMenuTreeSelect(menus));
        return ajax;
    }

    /**
     * 新增菜单
     */
    @PreAuthorize("@ss.hasPermi('system:menu:add')")
    @Log(title = "menu.management", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysMenu menu) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            String msg = MessageUtils.message("tenant_no_operation_auth");
            return AjaxResult.error(msg);
        }
        if (UserConstants.NOT_UNIQUE.equals(menuService.checkMenuNameUnique(menu))) {
            String msg = MessageUtils.message("menu.add.fail.name.exist", menu.getMenuName());
            return AjaxResult.error(msg);
        } else if (UserConstants.YES_FRAME.equals(menu.getIsFrame())
            && !StringUtils.startsWithAny(menu.getPath(), Constants.HTTP, Constants.HTTPS)) {
            String msg = MessageUtils.message("menu.add.fail.start.https", menu.getMenuName());
            return AjaxResult.error(msg);
        }
        menu.setCreateBy(SecurityUtils.getUsername());
        return toAjax(menuService.insertMenu(menu));
    }

    /**
     * 修改菜单
     */
    @PreAuthorize("@ss.hasPermi('system:menu:edit')")
    @Log(title = "menu.management", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysMenu menu) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            String msg = MessageUtils.message("tenant_no_operation_auth");
            return AjaxResult.error(msg);
        }
        if (UserConstants.NOT_UNIQUE.equals(menuService.checkMenuNameUnique(menu))) {
            String msg = MessageUtils.message("menu.update.fail.name.exist", menu.getMenuName());
            return AjaxResult.error(msg);
        } else if (UserConstants.YES_FRAME.equals(menu.getIsFrame())
            && !StringUtils.startsWithAny(menu.getPath(), Constants.HTTP, Constants.HTTPS)) {
            String msg = MessageUtils.message("menu.update.fail.start.https", menu.getMenuName());
            return AjaxResult.error(msg);
        } else if (menu.getMenuId().equals(menu.getParentId())) {
            String msg = MessageUtils.message("menu.update.fail.parent.oneself", menu.getMenuName());
            return AjaxResult.error(msg);
        }
        menu.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(menuService.updateMenu(menu));
    }

    /**
     * 删除菜单
     */
    @PreAuthorize("@ss.hasPermi('system:menu:remove')")
    @Log(title = "menu.management", businessType = BusinessType.DELETE)
    @DeleteMapping("/{menuId}")
    public AjaxResult remove(@PathVariable("menuId") Long menuId) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            String msg = MessageUtils.message("tenant_no_operation_auth");
            return AjaxResult.error(msg);
        }
        if (menuService.hasChildByMenuId(menuId)) {
            String msg = MessageUtils.message("menu.delete.fail.has.children");
            return AjaxResult.error(msg);
        }
        if (menuService.checkMenuExistRole(menuId) || menuService.checkMenuExistsTenantRole(menuId)) {
            String msg = MessageUtils.message("menu.delete.fail.has.assign");
            return AjaxResult.error(msg);
        }
        return toAjax(menuService.deleteMenuById(menuId));
    }
}