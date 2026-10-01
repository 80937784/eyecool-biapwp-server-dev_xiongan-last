package cn.eyecool.web.controller.system;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.constant.Constants;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.entity.SysMenu;
import cn.eyecool.common.core.domain.entity.SysTenant;
import cn.eyecool.common.core.domain.entity.SysUser;
import cn.eyecool.common.core.domain.model.BioLoginBody;
import cn.eyecool.common.core.domain.model.LoginBody;
import cn.eyecool.common.core.domain.model.LoginUser;
import cn.eyecool.common.utils.ServletUtils;
import cn.eyecool.framework.web.service.SysLoginService;
import cn.eyecool.framework.web.service.SysPermissionService;
import cn.eyecool.framework.web.service.TokenService;
import cn.eyecool.system.service.ISysMenuService;

/**
 * 登录验证
 * 
 * @author admin
 */
@RestController
public class SysLoginController {

    @Autowired
    private SysLoginService loginService;
    @Autowired
    private ISysMenuService menuService;
    @Autowired
    private SysPermissionService permissionService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private TenantProperties tenantProperties;

    /**
     * 登录方法
     * 
     * @param loginBody 登录信息
     * @return 结果
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginBody loginBody) {
        AjaxResult ajax = AjaxResult.success();
        // 生成令牌
        String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(),
            loginBody.getUuid());
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }

    @PostMapping("/bioLogin")
    public AjaxResult bioLogin(@RequestBody BioLoginBody bioLoginBody) {
        AjaxResult ajax = AjaxResult.success();
        String token = loginService.bioLogin(bioLoginBody.getBioUsername(), bioLoginBody.getBioData(),
            bioLoginBody.getBioLoginType());
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }

    /**
     * 获取用户信息
     * 
     * @return 用户信息
     */
    @GetMapping("getInfo")
    public AjaxResult getInfo() {
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        SysUser user = loginUser.getUser();
        SysTenant tenant = loginUser.getTenant();
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(user);
        // 权限集合
        Set<String> permissions = permissionService.getMenuPermission(user);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("user", user);
        ajax.put("tenant", tenant);
        ajax.put("roles", roles);
        ajax.put("permissions", permissions);
        ajax.put("tenantEnabled", tenantProperties.getEnabled());
        return ajax;
    }

    /**
     * 获取路由信息
     * 
     * @return 路由信息
     */
    @GetMapping("getRouters")
    public AjaxResult getRouters() {
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        // 用户信息
        SysUser user = loginUser.getUser();
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(user.getUserId());
        return AjaxResult.success(menuService.buildMenus(menus, null));
    }
}
