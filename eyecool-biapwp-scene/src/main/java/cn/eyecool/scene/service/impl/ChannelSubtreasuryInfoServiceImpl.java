package cn.eyecool.scene.service.impl;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import cn.eyecool.basedata.manager.IPersonDataManagerLogicService;
import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.scene.domain.ChannelSubtreasuryInfo;
import cn.eyecool.scene.event.ISubTreasuryEventCallback;
import cn.eyecool.scene.event.SubtreasuryEventPublishlService;
import cn.eyecool.scene.mapper.ChannelSubtreasuryBusiMapper;
import cn.eyecool.scene.mapper.ChannelSubtreasuryInfoMapper;
import cn.eyecool.scene.service.IChannelSubtreasuryInfoService;
import lombok.extern.slf4j.Slf4j;

/**
 * 子场景信息Service业务层处理
 * 
 * @author admin
 * @date 2021-03-22
 */
@Service
@Slf4j
public class ChannelSubtreasuryInfoServiceImpl implements IChannelSubtreasuryInfoService {

    /** 场景编码最大值缓存key前缀 */
    public static final String SUBSCENE_MAX_CODE_KEY_PREFIX = "subscene:maxnum";

    @Autowired
    private ChannelSubtreasuryInfoMapper channelSubtreasuryInfoMapper;
    @Autowired
    private ChannelSubtreasuryBusiMapper channelSubtreasuryBusiMapper;
    @Autowired
    private IPersonDataManagerLogicService personDataManagerLogicService;
    @Autowired
    private SubtreasuryEventPublishlService subtreasuryEventPublishlService;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private TenantProperties tenantProperties;

    /**
     * 查询子场景信息
     * 
     * @param id 子场景信息ID
     * @return 子场景信息
     */
    @Override
    public ChannelSubtreasuryInfo selectChannelSubtreasuryInfoById(String id) {
        return channelSubtreasuryInfoMapper.selectChannelSubtreasuryInfoById(id);
    }

    /**
     * 查询子场景信息列表
     * 
     * @param channelSubtreasuryInfo 子场景信息
     * @return 子场景信息
     */
    @Override
    public List<ChannelSubtreasuryInfo>
        selectChannelSubtreasuryInfoList(ChannelSubtreasuryInfo channelSubtreasuryInfo) {
        return channelSubtreasuryInfoMapper.selectChannelSubtreasuryInfoList(channelSubtreasuryInfo);
    }

    /**
     * 新增子场景信息
     * 
     * @param channelSubtreasuryInfo 子场景信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertChannelSubtreasuryInfo(ChannelSubtreasuryInfo channelSubtreasuryInfo) {
        ChannelSubtreasuryInfo condition = new ChannelSubtreasuryInfo();
        condition.setSubTreasuryCode(channelSubtreasuryInfo.getSubTreasuryCode());
        List<ChannelSubtreasuryInfo> list = channelSubtreasuryInfoMapper.selectChannelSubtreasuryInfoList(condition);
        if (CollectionUtils.isNotEmpty(list)) {
            throw new CustomException(MessageUtils.message("channel.sub.info.service.sub.scenecode.exists", channelSubtreasuryInfo.getSubTreasuryCode()));
        }
        channelSubtreasuryInfo.setId(IdWorker.getNextStringId());
        try {
            channelSubtreasuryInfo.setCreateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        channelSubtreasuryInfo.setCreateTime(DateUtils.getNowDate());
        return channelSubtreasuryInfoMapper.insertChannelSubtreasuryInfo(channelSubtreasuryInfo);
    }

    /**
     * 修改子场景信息
     * 
     * @param channelSubtreasuryInfo 子场景信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateChannelSubtreasuryInfo(ChannelSubtreasuryInfo channelSubtreasuryInfo) {
        channelSubtreasuryInfo.setUpdateTime(DateUtils.getNowDate());
        try {
            channelSubtreasuryInfo.setUpdateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        return channelSubtreasuryInfoMapper.updateChannelSubtreasuryInfo(channelSubtreasuryInfo);
    }

    /**
     * 批量删除子场景信息
     * 
     * @param ids 需要删除的子场景信息ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteChannelSubtreasuryInfoByIds(String[] ids) {
        int result = 0;
        for (String id : ids) {
            result += deleteChannelSubtreasuryInfoById(id);
        }
        return result;
    }

    /**
     * 删除子场景信息信息
     * 
     * @param id 子场景信息ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteChannelSubtreasuryInfoById(String id) {
        // 删除子场景人员
        channelSubtreasuryBusiMapper.deleteChannelSubtreasuryBusiBySubIds(new String[] {id});
        ChannelSubtreasuryInfo subtreasuryInfo = channelSubtreasuryInfoMapper.selectChannelSubtreasuryInfoById(id);
        int result = channelSubtreasuryInfoMapper.deleteChannelSubtreasuryInfoById(id);
        // 删除场景datamanager库
        personDataManagerLogicService.deleteLibrary(subtreasuryInfo.getSubTreasuryCode());
        // 通过Spring的事件收发机制发布时间，通知设备模块更新全量拉取标志
        subtreasuryEventPublishlService.delSubtreasuryPublish(Lists.newArrayList(subtreasuryInfo.getSubTreasuryCode()),
            new ISubTreasuryEventCallback() {
                @Override
                public void onError(String errmsg) {
                    log.error("Delete sub-scene event publishing processing failed:[]", errmsg);
                }
            });
        return result;
    }

    /**
     * 生成子场景编码
     * 
     * @param sceneCode 所属场景编码
     * @return
     */
    @Override
    public String generateSubsceneCode(String sceneCode) {
        String tenantId = TenantContextHolder.getTenantId();
        String redisKey = SUBSCENE_MAX_CODE_KEY_PREFIX;
        if (tenantProperties.getEnabled()) {
            redisKey = redisKey + ":" + tenantId;
        }
        Long seq = redisCache.incrementAndGet(redisKey);
        if (null != seq) {
            if (log.isDebugEnabled()) {
                log.debug("Get the current maximum sub-scene encoding sequence number from the cache:[{}],tenantId: [{}]", seq, tenantId);
            }
            return sceneCode + "_ZCJ" + String.format("%06d", seq);
        }
        synchronized (ChannelSubtreasuryInfoServiceImpl.class) {
            seq = redisCache.incrementAndGet(redisKey);
            if (null == seq) {
                String maxSubsceneCodeIndex = channelSubtreasuryInfoMapper.selectMaxSubsceneCodeIndex();
                if (StringUtils.isBlank(maxSubsceneCodeIndex)) {
                    seq = 1L;
                } else {
                    seq = Long.valueOf(maxSubsceneCodeIndex);
                }
                seq = redisCache.addAndGetLong(redisKey, seq);
            }
        }
        return sceneCode + "_ZCJ" + String.format("%06d", seq);
    }
}
