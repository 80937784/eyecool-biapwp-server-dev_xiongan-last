package cn.eyecool.msg.configure.ding;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.api.ApiException;

import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.utils.spring.SpringUtils;
import cn.eyecool.msg.constant.MsgRedisCacheConstants;

/**
 * 钉钉Token加载器
 * 
 * @author admin
 * @date 2020年3月31日
 */
public class DingTokenLoader {

    private static final Logger LOG = LoggerFactory.getLogger(DingTokenLoader.class);
    /** Token过期时间(单位：s) */
    public static final int expires_in = 7200;

    /**
     * 根据appKey获取Token
     * 
     * @param appKey
     * @return
     * @throws ApiException
     */
    public DingTalkToken get(String appKey) {
        if (StringUtils.isBlank(appKey)) {
            LOG.error("appKey is null or empty");
            throw new IllegalArgumentException("appKey is null or empty");
        }
        RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
        DingTalkToken token = redisCache.getCacheObject(key(appKey));
        if (null != token) {
            LOG.info("dingtalk access_token:{}", token.toString());
        }
        return token;
    }

    /**
     * 根据AppId刷新Token
     * 
     * @param token
     * @param appKey
     * @return
     */
    public void refresh(DingTalkToken token, String appKey) {
        if (StringUtils.isBlank(appKey)) {
            LOG.error("appKey is null or empty");
            throw new IllegalArgumentException("appKey is null or empty");
        }
        if (null == token || StringUtils.isBlank(token.getAccess_token())) {
            throw new IllegalStateException("access_token is null or empty");
        }
        if (token.isExprexpired()) {
            throw new IllegalStateException("access_token is expired");
        }
        RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
        redisCache.setCacheObject(key(appKey), token, expires_in - 60, TimeUnit.SECONDS);
    }

    /**
     * 删除Token
     * 
     * @param appKey
     */
    public void removeToken(String appKey) {
        if (StringUtils.isBlank(appKey)) {
            LOG.error("appKey is null or empty");
            throw new IllegalArgumentException("appKey is null or empty");
        }
        RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
        redisCache.deleteObject(key(appKey));
    }

    /**
     * 缓存Key
     * 
     * @param appKey
     * @return
     */
    private static String key(String appKey) {
        return MsgRedisCacheConstants.MSG_DINGTALK_TOKEN_CACHE_KEY + ":" + appKey;
    }
}
