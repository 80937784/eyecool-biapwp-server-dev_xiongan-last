package cn.eyecool.scene.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.annotation.SqlParser;

import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.scene.domain.ChannelSubtreasuryBusi;

/**
 * 子场景人员Mapper接口
 * 
 * @author admin
 * @date 2021-03-22
 */
@SuppressWarnings("deprecation")
public interface ChannelSubtreasuryBusiMapper {
    /**
     * 查询子场景人员
     * 
     * @param id 子场景人员ID
     * @return 子场景人员
     */
    public ChannelSubtreasuryBusi selectChannelSubtreasuryBusiById(String id);

    /**
     * 查询子场景人员列表
     * 
     * @param channelSubtreasuryBusi 子场景人员
     * @return 子场景人员集合
     */
    public List<ChannelSubtreasuryBusi> selectChannelSubtreasuryBusiList(ChannelSubtreasuryBusi channelSubtreasuryBusi);

    /**
     * 新增子场景人员
     * 
     * @param channelSubtreasuryBusi 子场景人员
     * @return 结果
     */
    public int insertChannelSubtreasuryBusi(ChannelSubtreasuryBusi channelSubtreasuryBusi);

    /**
     * 修改子场景人员
     * 
     * @param channelSubtreasuryBusi 子场景人员
     * @return 结果
     */
    public int updateChannelSubtreasuryBusi(ChannelSubtreasuryBusi channelSubtreasuryBusi);

    /**
     * 删除子场景人员
     * 
     * @param id 子场景人员ID
     * @return 结果
     */
    public int deleteChannelSubtreasuryBusiById(String id);

    /**
     * 批量删除子场景人员
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteChannelSubtreasuryBusiByIds(String[] ids);

    /**
     * 查询最大实时更新业务序列
     * 
     * @return
     */
    @SqlParser(filter = true)
    public Long selectLiveUpdateMaxSeriaNum();

    /**
     * 根据场景Ids删除场景子场景人员
     * 
     * @param channelIds
     * @return
     */
    public int deleteChannelSubtreasuryBusiByChannelIds(String[] channelIds);

    /**
     * 根据子场景ID删除场景子场景人员
     * 
     * @param subtreasuryIds
     * @return
     */
    public int deleteChannelSubtreasuryBusiBySubIds(String[] subtreasuryIds);

    /**
     * 查询未绑定子场景的人员列表
     * 
     * @param channelId 场景主键
     * @param subTreasuryId 子场景主键
     * @param basePersonInfo 人员信息
     * @return
     */
    public List<BasePersonInfo> selectUnbindPersonInfoList(@Param("channelId") String channelId,
        @Param("subTreasuryId") String subTreasuryId, @Param("basePersonInfo") BasePersonInfo basePersonInfo);

}
