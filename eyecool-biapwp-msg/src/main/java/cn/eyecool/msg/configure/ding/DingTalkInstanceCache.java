package cn.eyecool.msg.configure.ding;

import org.apache.commons.lang.StringUtils;

import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.utils.spring.SpringUtils;
import cn.eyecool.msg.constant.MsgRedisCacheConstants;

/**
 * 钉钉实例缓存
 * 
 * @author mawenjun
 * @version 1.0
 * @date 2020年3月26日
 *
 */
public class DingTalkInstanceCache {

    /**
     * 获取钉钉实例
     * 
     * @param agentId
     * @param appKey
     * @param appSecrect
     * @return
     */
    public static PlatformDingTalk get(Long agentId, String appKey, String appSecrect) {
        RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
        PlatformDingTalk dingtalk = redisCache.getCacheObject(key(appKey));
        if (StringUtils.isBlank(appSecrect)) {
            return dingtalk;
        }
        if (null == dingtalk) {
            synchronized (DingTalkInstanceCache.class) {
                dingtalk = redisCache.getCacheObject(key(appKey));
                if (dingtalk == null) {
                    dingtalk = put(agentId, appKey, appSecrect);
                }
            }
        }
        return dingtalk;
    }

    /**
     * 添加钉钉实例到缓存
     * 
     * @param appKey
     * @param appSecrect
     * @return
     */
    private static PlatformDingTalk put(Long agentId, String appKey, String appSecrect) {
        PlatformDingTalk dingTalk = new PlatformDingTalk(agentId, appKey, appSecrect);
        RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
        redisCache.setCacheObject(key(appKey), dingTalk);
        return dingTalk;
    }

    /**
     * 移除钉钉实例
     * 
     * @param appKey
     */
    public static void remove(String appKey) {
        RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
        // 直接删除缓存的Token,下次使用Token直接重新获取并存入缓存
        PlatformDingTalk dingtalk = redisCache.getCacheObject(key(appKey));
        if (null != dingtalk) {
            dingtalk.getTokenLoader().removeToken(appKey);
        }
        redisCache.deleteObject(key(appKey));
    }

    /**
     * 刷新钉钉实例(appSecrect改变)
     * 
     * @param agentId
     * @param appKey
     * @param appSecrect
     */
    public static void refresh(Long agentId, String appKey, String appSecrect) {
        PlatformDingTalk dingtalk = get(agentId, appKey, appSecrect);
        if (dingtalk.getSecret().equals(appSecrect)) {
            return;
        }
        synchronized (dingtalk) {// 对当前对象加锁
            // 直接删除掉微信对象
            remove(appKey);
        }
    }

    /**
     * 缓存Key
     * 
     * @param appKey
     * @return
     */
    private static String key(String appKey) {
        return MsgRedisCacheConstants.MSG_DINGTALK_INSTANCE_CACHE_KEY + ":" + appKey;
    }

}
