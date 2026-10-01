package cn.eyecool.scene.mapper;

import java.util.List;

import cn.eyecool.scene.domain.ChannelInfo;

/**
 * 场景信息Mapper接口
 * 
 * @author admin
 * @date 2021-03-22
 */
public interface ChannelInfoMapper {
    /**
     * 查询场景信息
     * 
     * @param id 场景信息ID
     * @return 场景信息
     */
    public ChannelInfo selectChannelInfoById(String id);

    /**
     * 查询场景信息列表
     * 
     * @param channelInfo 场景信息
     * @return 场景信息集合
     */
    public List<ChannelInfo> selectChannelInfoList(ChannelInfo channelInfo);

    /**
     * 新增场景信息
     * 
     * @param channelInfo 场景信息
     * @return 结果
     */
    public int insertChannelInfo(ChannelInfo channelInfo);

    /**
     * 修改场景信息
     * 
     * @param channelInfo 场景信息
     * @return 结果
     */
    public int updateChannelInfo(ChannelInfo channelInfo);

    /**
     * 删除场景信息
     * 
     * @param id 场景信息ID
     * @return 结果
     */
    public int deleteChannelInfoById(String id);

    /**
     * 批量删除场景信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteChannelInfoByIds(String[] ids);

    /**
     * 查询最大场景编码索引
     * 
     * @return
     */
    public String selectMaxSceneCodeIndex();
}
