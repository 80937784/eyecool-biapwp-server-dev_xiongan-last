package cn.eyecool.framework.web.service;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import cn.eyecool.common.constant.Constants;
import cn.eyecool.common.core.domain.model.LoginUser;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.exception.user.CaptchaException;
import cn.eyecool.common.exception.user.CaptchaExpireException;
import cn.eyecool.common.exception.user.UserPasswordNotMatchException;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.framework.manager.AsyncManager;
import cn.eyecool.framework.manager.factory.AsyncFactory;
import cn.eyecool.framework.security.token.BioDataAuthenticationToken;

/**
 * 登录校验方法
 * 
 * @author admin
 */
@Component
public class SysLoginService {
    @Autowired
    private TokenService tokenService;

    @Resource
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisCache redisCache;

    /**
     * 登录验证
     * 
     * @param username 用户名
     * @param password 密码
     * @param code 验证码
     * @param uuid 唯一标识
     * @return 结果
     */
    public String login(String username, String password, String code, String uuid) {
        String verifyKey = Constants.CAPTCHA_CODE_KEY + uuid;
        String captcha = redisCache.getCacheObject(verifyKey);
        redisCache.deleteObject(verifyKey);
        if (captcha == null) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(null, username, Constants.LOGIN_FAIL,
                MessageUtils.message("user.jcaptcha.expire")));
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha)) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(null, username, Constants.LOGIN_FAIL,
                MessageUtils.message("user.jcaptcha.error")));
            throw new CaptchaException();
        }
        // 用户验证
        Authentication authentication = null;
        try {
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (Exception e) {
            if (e instanceof BadCredentialsException) {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(null, username, Constants.LOGIN_FAIL,
                    MessageUtils.message("user.password.not.match")));
                throw new UserPasswordNotMatchException();
            } else {
                AsyncManager.me()
                    .execute(AsyncFactory.recordLogininfor(null, username, Constants.LOGIN_FAIL, e.getMessage()));
                throw new CustomException(e.getMessage());
            }
        }
        LoginUser loginUser = (LoginUser)authentication.getPrincipal();
        String tenantId = loginUser.getUser().getTenantId();
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(tenantId, username, Constants.LOGIN_SUCCESS,
            MessageUtils.message("user.login.success")));
        // 生成token
        return tokenService.createToken(loginUser);
    }

    public String bioLogin(String username, String bioData, String sdkType) {
        if (sdkType == null) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(null, username, Constants.LOGIN_FAIL,
                MessageUtils.message("user.jcaptcha.expire")));
            throw new CaptchaExpireException();
        }

        // 用户验证
        Authentication authentication = null;
        try {
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication =
                authenticationManager.authenticate(new BioDataAuthenticationToken(username, bioData, sdkType));
        } catch (Exception e) {
            if (e instanceof BadCredentialsException) {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(null, username, Constants.LOGIN_FAIL,
                    MessageUtils.message("user.password.not.match")));
                throw new UserPasswordNotMatchException();
            } else {
                AsyncManager.me()
                    .execute(AsyncFactory.recordLogininfor(null, username, Constants.LOGIN_FAIL, e.getMessage()));
                throw new CustomException(e.getMessage());
            }
        }
        LoginUser loginUser = (LoginUser)authentication.getPrincipal();
        String tenantId = loginUser.getUser().getTenantId();
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(tenantId, username, Constants.LOGIN_SUCCESS,
            MessageUtils.message("user.login.success")));
        // 生成token
        return tokenService.createToken(loginUser);
    }
}
