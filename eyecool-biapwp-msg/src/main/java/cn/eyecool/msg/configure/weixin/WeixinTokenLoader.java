package cn.eyecool.msg.configure.weixin;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weixin4j.model.base.Token;

import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.utils.spring.SpringUtils;
import cn.eyecool.msg.constant.MsgRedisCacheConstants;

/**
 * 微信Token加载器
 * 
 * @author admin
 * @date 2020年3月24日
 */
public class WeixinTokenLoader {

    private static final Logger LOG = LoggerFactory.getLogger(WeixinTokenLoader.class);

    /** Token过期时间(单位：s) */
    public static final int expires_in = 7200;

    /**
     * 根据AppId获取Token
     * 
     * @param appId
     * @return
     */
    public Token get(String appId) {
        if (StringUtils.isBlank(appId)) {
            LOG.error("appId is null or empty");
            throw new IllegalArgumentException("appId is null or empty");
        }
        RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
        Token token = redisCache.getCacheObject(key(appId));
        if (null != token) {
            LOG.info("wechat access_token:{}", token.toString());
        }
        return token;
    }

    /**
     * 根据AppId刷新Token
     * 
     * @param token
     * @param appId
     * @return
     */
    public void refresh(Token token, String appId) {
        if (StringUtils.isBlank(appId)) {
            LOG.error("appId is null or empty");
            throw new IllegalArgumentException("appId is null or empty");
        }
        if (null == token || StringUtils.isBlank(token.getAccess_token())) {
            throw new IllegalStateException("access_token is null or empty");
        }
        if (token.isExprexpired()) {
            throw new IllegalStateException("access_token is expired");
        }
        RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
        redisCache.setCacheObject(key(appId), token, expires_in - 60, TimeUnit.SECONDS);
    }

    /**
     * 删除Token
     * 
     * @param appId
     */
    public void removeToken(String appId) {
        if (StringUtils.isBlank(appId)) {
            LOG.error("appId is null or empty");
            throw new IllegalArgumentException("appId is null or empty");
        }
        RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
        redisCache.deleteObject(key(appId));
    }

    /**
     * 缓存Key
     * 
     * @param appId
     * @return
     */
    private static String key(String appId) {
        return MsgRedisCacheConstants.MSG_WECHAT_TOKEN_CACHE_KEY + ":" + appId;
    }
}
