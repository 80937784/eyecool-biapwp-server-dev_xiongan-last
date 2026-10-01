package cn.eyecool.framework.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.UserConstants;
import cn.eyecool.common.core.domain.entity.SysTenant;
import cn.eyecool.common.core.domain.entity.SysUser;
import cn.eyecool.common.core.domain.model.LoginUser;
import cn.eyecool.common.enums.UserStatus;
import cn.eyecool.common.exception.BaseException;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.system.service.ISysTenantService;
import cn.eyecool.system.service.ISysUserService;

/**
 * 用户验证处理
 *
 * @author admin
 */
@Primary
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private ISysUserService userService;
    @Autowired
    private SysPermissionService permissionService;
    @Autowired
    private ISysTenantService tenantService;
    @Autowired
    private TenantProperties tenantProperties;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userService.selectUserByUserName(username);
        if (StringUtils.isNull(user)) {
            log.info("Login user: {} does not exist.", username);
            throw new UsernameNotFoundException(MessageUtils.message("user.detail.account.no.exists"));
        } else if (UserStatus.DELETED.getCode().equals(user.getDelFlag())) {
            log.info("Login user: {} has been deleted.", username);
            throw new BaseException(MessageUtils.message("user.detail.account.deleted",username));
        } else if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            log.info("Login user: {} has been disabled.", username);
            throw new BaseException(MessageUtils.message("user.detail.account.disabled", username));
        }
        // 如果没有启用多租户拦截 或者 用户未配置租户(即系统用户)
        if (!tenantProperties.getEnabled()) {
            return createLoginUser(null, user);
        }
        if (StringUtils.isBlank(user.getTenantId())) {
            log.info("登录用户：{} 没有所属租户.", username);
            throw new BaseException(MessageUtils.message("user.detail.account.no.tenant", username));
        }
        // 开启租户的情况下，super租户下的用户是禁止登录的，关于一切租户的配置，均在后台实现
        // FIXME 调试阶段暂时注释，开发完毕后放开此代码
        if (UserConstants.SUPER_TENANT.equals(user.getTenantId()) && !"admin".equals(user.getUserName())) {
            log.info("Login user: The tenant [{}] of {} does not allow login.", username, user.getTenantId());
            throw new BaseException(MessageUtils.message("user.detail.account.not.allow.login", username));
        }
        // 查询租户信息
        SysTenant tenant = tenantService.selectSysTenantByTenantId(user.getTenantId());
        // 超级租户不做判断，永远有效
        if (StringUtils.isNull(tenant)) {
            log.info("Login user: The tenant to which {} belongs does not exist.", username);
            throw new BaseException(MessageUtils.message("user.detail.account.tenant.not.exists", username));
        }
        if (DictConstants.TenantState.FROZEN.equalsIgnoreCase(tenant.getTenantState())) {
            log.info("Login user: The tenant [{}] to which {} belongs is frozen.", username, tenant.getTenantId());
            throw new BaseException(MessageUtils.message("user.detail.account.tenant.disabled", username));
        }
        if (System.currentTimeMillis() < tenant.getEffectiveTime().getTime()) {
            log.info("Login user: The tenant [{}] to which {} belongs has not yet taken effect.", username, tenant.getTenantId());
            throw new BaseException(MessageUtils.message("user.detail.account.tenant.invalid", username));
        }
        if (System.currentTimeMillis() > tenant.getExpireTime().getTime()) {
            log.info("Login user: {} belongs to tenant [{}] has expired.", username, tenant.getTenantId());
            throw new BaseException(MessageUtils.message("user.detail.account.tenant.expired", username));
        }
        return createLoginUser(tenant, user);
    }

    public UserDetails createLoginUser(SysTenant tenant, SysUser user) {
        return new LoginUser(tenant, user, permissionService.getMenuPermission(user));
    }
}
