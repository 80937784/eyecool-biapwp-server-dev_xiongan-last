package cn.eyecool.scene.service.impl;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import cn.eyecool.scene.domain.ChannelInfo;
import cn.eyecool.scene.domain.ChannelSubtreasuryInfo;
import cn.eyecool.scene.mapper.ChannelBusinessMapper;
import cn.eyecool.scene.mapper.ChannelInfoMapper;
import cn.eyecool.scene.mapper.ChannelParamMapper;
import cn.eyecool.scene.mapper.ChannelSubtreasuryInfoMapper;
import cn.eyecool.scene.service.IChannelInfoService;
import lombok.extern.slf4j.Slf4j;

/**
 * 场景信息Service业务层处理
 * 
 * @author admin
 * @date 2021-03-22
 */
@Slf4j
@Service
public class ChannelInfoServiceImpl implements IChannelInfoService {

    /** 场景编码最大值缓存key前缀 */
    public static final String SCENE_MAX_CODE_KEY_PREFIX = "scene:maxnum";

    @Autowired
    private ChannelInfoMapper channelInfoMapper;
    @Autowired
    private ChannelSubtreasuryInfoMapper channelSubtreasuryInfoMapper;
    @Autowired
    private ChannelBusinessMapper channelBusinessMapper;
    @Autowired
    private ChannelParamMapper channelParamMapper;
    @Autowired
    private IPersonDataManagerLogicService personDataManagerLogicService;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private TenantProperties tenantProperties;

    /**
     * 查询场景信息
     * 
     * @param id 场景信息ID
     * @return 场景信息
     */
    @Override
    public ChannelInfo selectChannelInfoById(String id) {
        return channelInfoMapper.selectChannelInfoById(id);
    }

    /**
     * 查询场景信息列表
     * 
     * @param channelInfo 场景信息
     * @return 场景信息
     */
    @Override
    public List<ChannelInfo> selectChannelInfoList(ChannelInfo channelInfo) {
        return channelInfoMapper.selectChannelInfoList(channelInfo);
    }

    /**
     * 新增场景信息
     * 
     * @param channelInfo 场景信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertChannelInfo(ChannelInfo channelInfo) {
        // 校验场景编码是否存在
        ChannelInfo condition = new ChannelInfo();
        condition.setChannelCode(channelInfo.getChannelCode());
        List<ChannelInfo> list = channelInfoMapper.selectChannelInfoList(condition);
        if (CollectionUtils.isNotEmpty(list)) {
            throw new CustomException(MessageUtils.message("channel.info.service.scenecode.exists", channelInfo.getChannelCode()));
        }
        channelInfo.setId(IdWorker.getNextStringId());
        channelInfo.setCreateTime(DateUtils.getNowDate());
        try {
            channelInfo.setCreateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        return channelInfoMapper.insertChannelInfo(channelInfo);
    }

    /**
     * 修改场景信息
     * 
     * @param channelInfo 场景信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateChannelInfo(ChannelInfo channelInfo) {
        channelInfo.setUpdateTime(DateUtils.getNowDate());
        try {
            channelInfo.setUpdateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        int result = channelInfoMapper.updateChannelInfo(channelInfo);
        return result;
    }

    /**
     * 批量删除场景信息
     * 
     * @param ids 需要删除的场景信息ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteChannelInfoByIds(String[] ids) {
        int result = 0;
        for (String id : ids) {
            result += deleteChannelInfoById(id);
        }
        return result;
    }

    /**
     * 删除场景信息信息
     * 
     * @param id 场景信息ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteChannelInfoById(String id) {
        ChannelSubtreasuryInfo condition = new ChannelSubtreasuryInfo();
        condition.setChannelId(id);
        List<ChannelSubtreasuryInfo> subtreasuryInfoList =
            channelSubtreasuryInfoMapper.selectChannelSubtreasuryInfoList(condition);
        if (CollectionUtils.isNotEmpty(subtreasuryInfoList)) {
            throw new CustomException(MessageUtils.message("channel.info.service.deletescene.subscene.exists"));
        }
        // 删除场景人员
        channelBusinessMapper.deleteChannelBusinessByChannelIds(new String[] {id});
        // 删除场景参数
        channelParamMapper.deleteChannelParamByChannelIds(new String[] {id});
        ChannelInfo channelInfo = channelInfoMapper.selectChannelInfoById(id);
        int result = channelInfoMapper.deleteChannelInfoById(id);
        // 删除场景datamanager库
        personDataManagerLogicService.deleteLibrary(channelInfo.getChannelCode());
        return result;
    }

    /**
     * 生成场景编码
     * 
     * @return
     */
    @Override
    public String generateSceneCode() {
        String tenantId = TenantContextHolder.getTenantId();
        String redisKey = SCENE_MAX_CODE_KEY_PREFIX;
        if (tenantProperties.getEnabled()) {
            redisKey = redisKey + ":" + tenantId;
        }
        Long seq = redisCache.incrementAndGet(redisKey);
        if (null != seq) {
            if (log.isDebugEnabled()) {
                log.debug("Get the current maximum scene encoding sequence number from the cache:[{}],tenantId: [{}]", seq, tenantId);
            }
            return "CJ" + String.format("%04d", seq);
        }
        synchronized (ChannelInfoServiceImpl.class) {
            seq = redisCache.incrementAndGet(redisKey);
            if (null == seq) {
                String maxSceneCodeIndex = channelInfoMapper.selectMaxSceneCodeIndex();
                if (StringUtils.isBlank(maxSceneCodeIndex)) {
                    seq = 1L;
                } else {
                    seq = Long.valueOf(maxSceneCodeIndex.substring(2));
                }
                seq = redisCache.addAndGetLong(redisKey, seq);
            }
        }
        return "CJ" + String.format("%04d", seq);
    }

}
