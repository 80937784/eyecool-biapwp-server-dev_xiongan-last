package cn.eyecool.framework.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

@Service
public class BioDataUserDetailsServiceImpl implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(BioDataUserDetailsServiceImpl.class);

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
            throw new UsernameNotFoundException(MessageUtils.message("bio.data.user.account.not.exists", username));
        } else if (UserStatus.DELETED.getCode().equals(user.getDelFlag())) {
            log.info("Login user: {} has been deleted.", username);
            throw new BaseException(MessageUtils.message("bio.data.user.account.deleted", username));
        } else if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            log.info("Login user: {} has been disabled.", username);
            throw new BaseException(MessageUtils.message("bio.data.user.account.disabled", username));
        }
        // 如果没有启用多租户拦截 或者 用户未配置租户(即系统用户)
        if (!tenantProperties.getEnabled()) {
            return createLoginUser(null, user);
        }
        if (StringUtils.isBlank(user.getTenantId())) {
            log.info("Login user: {} does not belong to a tenant.", username);
            throw new BaseException(MessageUtils.message("bio.data.user.account.no.tenant", username));
        }
        // 查询租户信息
        SysTenant tenant = tenantService.selectSysTenantByTenantId(user.getTenantId());
        // 超级租户不做判断，永远有效
        if (!UserConstants.SUPER_TENANT.equals(user.getTenantId())) {
            if (StringUtils.isNull(tenant)) {
                log.info("Login user: The tenant to which {} belongs does not exist.", username);
                throw new BaseException(MessageUtils.message("bio.data.user.account.tenant.not.exists", username));
            }
            if (DictConstants.TenantState.FROZEN.equalsIgnoreCase(tenant.getTenantState())) {
                log.info("Login user: The tenant [{}] to which {} belongs is frozen.", username, tenant.getTenantId());
                throw new BaseException(MessageUtils.message("bio.data.user.account.tenant.disabled", username));
            }
            if (System.currentTimeMillis() < tenant.getEffectiveTime().getTime()) {
                log.info("Login user: The tenant [{}] to which {} belongs has not yet taken effect.", username, tenant.getTenantId());
                throw new BaseException(MessageUtils.message("bio.data.user.account.tenant.invalid", username));
            }
            if (System.currentTimeMillis() > tenant.getExpireTime().getTime()) {
                log.info("Login user: {} belongs to tenant [{}] has expired.", username, tenant.getTenantId());
                throw new BaseException(MessageUtils.message("bio.data.user.account.tenant.expired", username));
            }
        }
        return createLoginUser(tenant, user);
    }

    public UserDetails createLoginUser(SysTenant tenant, SysUser user) {
        return new LoginUser(tenant, user, permissionService.getMenuPermission(user));
    }

}
