package cn.eyecool.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import cn.eyecool.common.constant.HttpStatus;
import cn.eyecool.common.constant.UserConstants;
import cn.eyecool.common.core.domain.entity.SysTenant;
import cn.eyecool.common.core.domain.model.LoginUser;
import cn.eyecool.common.exception.CustomException;

/**
 * 安全服务工具类
 * 
 * @author admin
 */
public class SecurityUtils {
    private static final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);

    /**
     * 获取用户账户
     **/
    public static String getUsername() {
        try {
            return getLoginUser().getUsername();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new CustomException(MessageUtils.message("security.utils.get.user.error"), HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 获取用户
     **/
    public static LoginUser getLoginUser() {
        try {
            Authentication auth = getAuthentication();
            return (LoginUser)auth.getPrincipal();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new CustomException(MessageUtils.message("security.utils.get.user.error"), HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 获取Authentication
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 生成BCryptPasswordEncoder密码
     *
     * @param password 密码
     * @return 加密字符串
     */
    public static String encryptPassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    /**
     * 判断密码是否相同
     *
     * @param rawPassword 真实密码
     * @param encodedPassword 加密后字符
     * @return 结果
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 是否为管理员(系统超级管理员)
     * 
     * @param userId 用户ID
     * @return 结果
     */
    public static boolean isAdmin(Long userId) {
        return userId != null && 1L == userId;
    }

    /**
     * 是否是普通租户用户
     * 
     * @return
     */
    public static boolean isTenantUser() {
        SysTenant tenant = getLoginUser().getTenant();
        return StringUtils.isNotNull(tenant) && !UserConstants.SUPER_TENANT.equals(tenant.getTenantId());
    }

    /**
     * 是否是租户管理员
     * 
     * @return
     */
    public static boolean isTenantAdmin() {
        return isTenantUser() && getLoginUser().getUser().isTenantSuperUser();
    }

}
