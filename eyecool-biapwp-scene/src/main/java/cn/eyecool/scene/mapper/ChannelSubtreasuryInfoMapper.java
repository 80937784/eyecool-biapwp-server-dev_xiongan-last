package cn.eyecool.scene.mapper;

import java.util.List;

import cn.eyecool.scene.domain.ChannelSubtreasuryInfo;

/**
 * 子场景信息Mapper接口
 * 
 * @author admin
 * @date 2021-03-22
 */
public interface ChannelSubtreasuryInfoMapper {
    /**
     * 查询子场景信息
     * 
     * @param id 子场景信息ID
     * @return 子场景信息
     */
    public ChannelSubtreasuryInfo selectChannelSubtreasuryInfoById(String id);

    /**
     * 查询子场景信息列表
     * 
     * @param channelSubtreasuryInfo 子场景信息
     * @return 子场景信息集合
     */
    public List<ChannelSubtreasuryInfo> selectChannelSubtreasuryInfoList(ChannelSubtreasuryInfo channelSubtreasuryInfo);

    /**
     * 新增子场景信息
     * 
     * @param channelSubtreasuryInfo 子场景信息
     * @return 结果
     */
    public int insertChannelSubtreasuryInfo(ChannelSubtreasuryInfo channelSubtreasuryInfo);

    /**
     * 修改子场景信息
     * 
     * @param channelSubtreasuryInfo 子场景信息
     * @return 结果
     */
    public int updateChannelSubtreasuryInfo(ChannelSubtreasuryInfo channelSubtreasuryInfo);

    /**
     * 删除子场景信息
     * 
     * @param id 子场景信息ID
     * @return 结果
     */
    public int deleteChannelSubtreasuryInfoById(String id);

    /**
     * 批量删除子场景信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteChannelSubtreasuryInfoByIds(String[] ids);

    /**
     * 查询最大子场景编码索引
     * 
     * @return
     */
    public String selectMaxSubsceneCodeIndex();
}
