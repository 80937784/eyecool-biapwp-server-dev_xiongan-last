package cn.eyecool.system.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.system.service.IPlatformConcurrentService;
import cn.eyecool.system.service.ISysConfigService;

/**
 * 平台并发量控制service实现
 * 
 * @author admin
 * @date 2019年11月18日
 */
@Service
public class PlatformConcurrentServiceImpl implements IPlatformConcurrentService {

    private static final transient Logger LOG = LoggerFactory.getLogger(PlatformConcurrentServiceImpl.class);

    @Autowired
    private RedisCache redisCache;
    @Autowired
    private ISysConfigService configService;
    /** 系统接口并发量缓存KEY */
    public static final String CONCURRENT_PLATFORM_SEMAPHORE_CACHE_KEY = "concurrent:platform.semaphore";
    public static final Object lockObj = new Object();

    /**
     * 清空并发信号
     */
    @Override
    public void clearSemaphore() {
        redisCache.deleteObject(CONCURRENT_PLATFORM_SEMAPHORE_CACHE_KEY);
    }

    /**
     * 获取并发信号量许可
     * 
     * @return
     */
    @Override
    public boolean acquireSemaphore() {
        boolean result = false;
        Long value = redisCache.decrementAndGet(CONCURRENT_PLATFORM_SEMAPHORE_CACHE_KEY);
        if (null != value) {
            LOG.info("Get the currently available number of licenses from the cache:[{}]", value + 1);
            result = value >= 0;
            if (!result) {
                redisCache.incrementAndGet(CONCURRENT_PLATFORM_SEMAPHORE_CACHE_KEY);
            }
            return result;
        }
        synchronized (lockObj) {
            value = redisCache.decrementAndGet(CONCURRENT_PLATFORM_SEMAPHORE_CACHE_KEY);
            if (null == value) {
                value = putSemaphore();
            }
            result = value >= 0;
            if (!result) {
                redisCache.incrementAndGet(CONCURRENT_PLATFORM_SEMAPHORE_CACHE_KEY);
            }
            return result;
        }
    }

    /**
     * 释放并发许可证数量
     */
    @Override
    public void releseSemaphore() {
        Long value = redisCache.incrementAndGet(CONCURRENT_PLATFORM_SEMAPHORE_CACHE_KEY);
        LOG.info("Release platform concurrent licenses, and the available quantity after release is:[{}]", value);
    }

    /**
     * 设置并发许数量到redis
     * 
     * @return
     */
    private Long putSemaphore() {
        String concurrentStr = configService.selectConfigByKey(SysConfigConstants.PLATFORM_INTERFACE_CONCURRENT_KEY);
        if (StringUtils.isBlank(concurrentStr)) {
            throw new CustomException(MessageUtils.message("platform.service.concurrent.empty", SysConfigConstants.PLATFORM_INTERFACE_CONCURRENT_KEY));
        }
        try {
            Long concurrentNum = Long.valueOf(concurrentStr);
            Long value = redisCache.addAndGetLong(CONCURRENT_PLATFORM_SEMAPHORE_CACHE_KEY, concurrentNum - 1);
            return value;
        } catch (Exception e) {
            throw new CustomException(MessageUtils.message("platform.service.concurrent.format.error", SysConfigConstants.PLATFORM_INTERFACE_CONCURRENT_KEY));
        }
    }
}
