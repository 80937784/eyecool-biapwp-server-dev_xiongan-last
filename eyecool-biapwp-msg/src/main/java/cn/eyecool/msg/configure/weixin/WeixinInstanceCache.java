package cn.eyecool.msg.configure.weixin;

import org.apache.commons.lang.StringUtils;
import org.weixin4j.WeixinConfig;

import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.utils.spring.SpringUtils;
import cn.eyecool.msg.constant.MsgRedisCacheConstants;

/**
 * 微信实例缓存
 * 
 * @author mawenjun
 * @version 1.0
 * @date 2020年3月26日
 *
 */
public class WeixinInstanceCache {

    /**
     * 获取微信实例，不存在则添加到缓存
     * 
     * @param appId
     * @param appSecrect
     * @return
     */
    public static PlatformWeiXin get(String appId, String appSecrect) {
        RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
        PlatformWeiXin weixin = redisCache.getCacheObject(key(appId));
        if (StringUtils.isBlank(appSecrect)) {
            return weixin;
        }
        if (null == weixin) {
            synchronized (WeixinInstanceCache.class) {
                weixin = redisCache.getCacheObject(key(appId));
                if (weixin == null) {
                    weixin = put(appId, appSecrect);
                }
            }
        }
        return weixin;
    }

    /**
     * 添加微信实例到缓存
     * 
     * @author mawenjun
     * @param appId
     * @param appSecrect
     * @return
     * @date 2020年3月26日
     *
     */
    public static PlatformWeiXin put(String appId, String appSecrect) {
        WeixinConfig config = new WeixinConfig();
        config.setAppid(appId);
        config.setSecret(appSecrect);
        PlatformWeiXin weixin = new PlatformWeiXin(config);
        RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
        redisCache.setCacheObject(key(appId), weixin);
        return weixin;
    }

    /**
     * 移除微信实例
     * 
     * @author mawenjun
     * @param appId
     * @date 2020年3月26日
     *
     */
    public static void remove(String appId) {
        RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
        // 直接删除缓存的Token,下次使用Token直接重新获取并存入缓存
        PlatformWeiXin weixin = redisCache.getCacheObject(key(appId));
        if (null != weixin) {
            weixin.getTokenLoder().removeToken(appId);
        }
        redisCache.deleteObject(key(appId));
    }

    /**
     * 刷新微信实例(appSecrect改变)
     * 
     * @param appId
     * @param appSecrect
     */
    public static void refresh(String appId, String appSecrect) {
        PlatformWeiXin weixin = get(appId, appSecrect);
        if (weixin.getWeixinConfig().getSecret().equals(appSecrect)) {
            return;
        }
        synchronized (weixin) {// 对当前对象加锁
            // 直接删除掉微信对象
            remove(appId);
        }
    }

    /**
     * 缓存Key
     * 
     * @param appId
     * @return
     */
    private static String key(String appId) {
        return MsgRedisCacheConstants.MSG_WECHAT_INSTANCE_CACHE_KEY + ":" + appId;
    }

}
