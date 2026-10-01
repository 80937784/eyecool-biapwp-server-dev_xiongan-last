package cn.eyecool.scene.service.impl;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.scene.domain.ChannelParam;
import cn.eyecool.scene.mapper.ChannelParamMapper;
import cn.eyecool.scene.service.IChannelParamService;

/**
 * 场景参数Service业务层处理
 * 
 * @author admin
 * @date 2021-03-22
 */
@Service
public class ChannelParamServiceImpl implements IChannelParamService {
    @Autowired
    private ChannelParamMapper channelParamMapper;

    /**
     * 查询场景参数
     * 
     * @param id 场景参数ID
     * @return 场景参数
     */
    @Override
    public ChannelParam selectChannelParamById(String id) {
        return channelParamMapper.selectChannelParamById(id);
    }

    /**
     * 查询场景参数列表
     * 
     * @param channelParam 场景参数
     * @return 场景参数
     */
    @Override
    public List<ChannelParam> selectChannelParamList(ChannelParam channelParam) {
        return channelParamMapper.selectChannelParamList(channelParam);
    }

    /**
     * 新增场景参数
     * 
     * @param channelParam 场景参数
     * @return 结果
     */
    @Override
    @Transactional
    public int insertChannelParam(ChannelParam channelParam) {
        ChannelParam condition = new ChannelParam();
        condition.setChannelId(channelParam.getChannelId());
        condition.setParamCode(channelParam.getParamCode());
        condition.setBioAttestType(channelParam.getBioAttestType());
        List<ChannelParam> paramList = channelParamMapper.selectChannelParamList(condition);
        if (CollectionUtils.isNotEmpty(paramList)) {
            throw new CustomException(MessageUtils.message("channel.param.service.param.exists"));
        }
        channelParam.setId(IdWorker.getNextStringId());
        try {
            channelParam.setCreateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        channelParam.setCreateTime(DateUtils.getNowDate());
        return channelParamMapper.insertChannelParam(channelParam);
    }

    /**
     * 修改场景参数
     * 
     * @param channelParam 场景参数
     * @return 结果
     */
    @Override
    @Transactional
    public int updateChannelParam(ChannelParam channelParam) {
        channelParam.setUpdateTime(DateUtils.getNowDate());
        try {
            channelParam.setUpdateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        return channelParamMapper.updateChannelParam(channelParam);
    }

    /**
     * 批量删除场景参数
     * 
     * @param ids 需要删除的场景参数ID
     * @return 结果
     */
    @Override
    public int deleteChannelParamByIds(String[] ids) {
        return channelParamMapper.deleteChannelParamByIds(ids);
    }

    /**
     * 删除场景参数信息
     * 
     * @param id 场景参数ID
     * @return 结果
     */
    @Override
    public int deleteChannelParamById(String id) {
        return channelParamMapper.deleteChannelParamById(id);
    }
}
