package cn.eyecool.statistic.controller;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import cn.eyecool.common.constant.Constants;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.model.LoginBody;
import cn.eyecool.common.core.domain.model.LoginUser;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.exception.user.CaptchaException;
import cn.eyecool.common.exception.user.CaptchaExpireException;
import cn.eyecool.common.exception.user.UserPasswordNotMatchException;
import cn.eyecool.common.utils.AESUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.statistic.constant.StatisticConstants;

/**
 * 大屏登录请求
 * 
 * @author mawj
 * @date 2021/09/26
 */
@RestController
@RequestMapping("/api/bigscreen")
public class BigScreenLoginController {

    @Autowired
    private RedisCache redisCache;
    @Resource
    private AuthenticationManager authenticationManager;

    /**
     * 大屏登录
     * 
     * @param loginBody
     * @return
     * @throws UnsupportedEncodingException
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody(required = false) LoginBody loginBody) {
        AjaxResult ajax = AjaxResult.success();
        // 生成令牌
        JSONObject jsonObject =
            login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(), loginBody.getUuid());
        String tenantId = jsonObject.getString("tenantId");
        String token = jsonObject.getString("token");
        String cacheKey = StatisticConstants.BIGSCREEN_LOGIN_TOKEN_CACHE_PREFIX + ":" + tenantId;
        List<String> tenantCacheList = redisCache.getCacheList(cacheKey);
        if (CollectionUtils.isEmpty(tenantCacheList) || !tenantCacheList.contains(token)) {
            redisCache.setCacheList(cacheKey, Lists.newArrayList(token));
        }
        ajax.put(AjaxResult.DATA_TAG, token);
        return ajax;
    }

    /**
     * 登录验证
     * 
     * @param username
     * @param password
     * @param code
     * @param uuid
     * @return
     * @throws UnsupportedEncodingException
     */
    private JSONObject login(String username, String password, String code, String uuid) {
        String verifyKey = Constants.CAPTCHA_CODE_KEY + uuid;
        String captcha = redisCache.getCacheObject(verifyKey);
        redisCache.deleteObject(verifyKey);
        if (captcha == null) {
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha)) {
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
                throw new UserPasswordNotMatchException();
            } else {
                throw new CustomException(e.getMessage());
            }
        }
        LoginUser loginUser = (LoginUser)authentication.getPrincipal();
        String tenantId = loginUser.getUser().getTenantId();
        // 生成token
        String token = AESUtils.encryptAES(tenantId + ":" + loginUser.getUsername() + ":" + IdWorker.getNextStringId());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", new String(Base64.getEncoder().encode(token.getBytes())));
        jsonObject.put("tenantId", tenantId);
        return jsonObject;
    }

}
