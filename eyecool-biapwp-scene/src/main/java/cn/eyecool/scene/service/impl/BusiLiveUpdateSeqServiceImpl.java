package cn.eyecool.scene.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.scene.mapper.ChannelBusinessMapper;
import cn.eyecool.scene.mapper.ChannelSubtreasuryBusiMapper;
import cn.eyecool.scene.service.IBusiLiveUpdateSeqService;
import lombok.extern.slf4j.Slf4j;

/**
 * 实时同步更新数据标识流水码服务实现
 * 
 * @author admin
 * @date 2019年12月6日
 */
@Service
@Slf4j
public class BusiLiveUpdateSeqServiceImpl implements IBusiLiveUpdateSeqService {

    /** 人员信息更新场景人员自增标识缓存KEY */
    private static final String LIVE_UPDATE_CHANNEL_BUSI_SEQ_CACHE_KEY = "liveupdate:channelbusi_seq";
    /** 人员信息更新子场景人员自增标识缓存KEY */
    private static final String LIVE_UPDATE_SUB_BUSI_SEQ_CACHE_KEY = "liveupdate:subbusi_seq";

    @Autowired
    private RedisCache redisCache;
    @Autowired
    private ChannelBusinessMapper channelBusinessMapper;
    @Autowired
    private ChannelSubtreasuryBusiMapper channelSubtreasuryBusiMapper;

    /**
     * 流水码类型定义
     * 
     * @author mawj
     * @date 2020/10/14
     */
    enum serialNumType {
        CHANNEL_BUSI, SUBTREASURY_BUSI
    }

    @Override
    public Long incrementAndGetChannelBusiSeqNum() {
        return incrementAndGetSerialNum(LIVE_UPDATE_CHANNEL_BUSI_SEQ_CACHE_KEY, serialNumType.CHANNEL_BUSI);
    }

    @Override
    public Long incrementAndGetSubtreasuryBusiSeqNum() {
        return incrementAndGetSerialNum(LIVE_UPDATE_SUB_BUSI_SEQ_CACHE_KEY, serialNumType.SUBTREASURY_BUSI);
    }

    /**
     * 查询最大更新标识流水码
     * 
     * @param key
     * @param type
     * @return
     */
    private Long incrementAndGetSerialNum(String key, serialNumType type) {
        Long seq = redisCache.incrementAndGet(key);
        if (null != seq) {
            if (log.isDebugEnabled()) {
                String typedesc = type.equals(serialNumType.CHANNEL_BUSI) ? MessageUtils.message("busi.live.update.service.scene.staff") : MessageUtils.message("busi.live.update.service.subscene.staff");
                log.debug("Get the current maximum [{}] data from the cache, real-time synchronization identification serial number: [{}]", typedesc, seq);
            }
            return seq;
        }
        synchronized (BusiLiveUpdateSeqServiceImpl.class) {
            seq = redisCache.incrementAndGet(key);
            if (null == seq) {
                Long maxSeriaNum = null;
                switch (type) {
                    case CHANNEL_BUSI:
                        maxSeriaNum = channelBusinessMapper.selectLiveUpdateMaxSeriaNum();
                        break;
                    case SUBTREASURY_BUSI:
                        maxSeriaNum = channelSubtreasuryBusiMapper.selectLiveUpdateMaxSeriaNum();
                        break;
                    default:
                        break;
                }
                if (null == maxSeriaNum) {
                    maxSeriaNum = 1L;
                }
                seq = redisCache.addAndGetLong(key, maxSeriaNum);
            }
        }
        return seq;
    }

}
