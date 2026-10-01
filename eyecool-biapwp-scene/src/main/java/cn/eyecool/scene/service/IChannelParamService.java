package cn.eyecool.scene.service;

import java.util.List;
import cn.eyecool.scene.domain.ChannelParam;

/**
 * 场景参数Service接口
 * 
 * @author admin
 * @date 2021-03-22
 */
public interface IChannelParamService 
{
    /**
     * 查询场景参数
     * 
     * @param id 场景参数ID
     * @return 场景参数
     */
    public ChannelParam selectChannelParamById(String id);

    /**
     * 查询场景参数列表
     * 
     * @param channelParam 场景参数
     * @return 场景参数集合
     */
    public List<ChannelParam> selectChannelParamList(ChannelParam channelParam);

    /**
     * 新增场景参数
     * 
     * @param channelParam 场景参数
     * @return 结果
     */
    public int insertChannelParam(ChannelParam channelParam);

    /**
     * 修改场景参数
     * 
     * @param channelParam 场景参数
     * @return 结果
     */
    public int updateChannelParam(ChannelParam channelParam);

    /**
     * 批量删除场景参数
     * 
     * @param ids 需要删除的场景参数ID
     * @return 结果
     */
    public int deleteChannelParamByIds(String[] ids);

    /**
     * 删除场景参数信息
     * 
     * @param id 场景参数ID
     * @return 结果
     */
    public int deleteChannelParamById(String id);
}
