package cn.eyecool.scene.service;

import java.util.List;

import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.scene.domain.ChannelSubBusiParam;
import cn.eyecool.scene.domain.ChannelSubtreasuryBusi;

/**
 * 子场景人员Service接口
 * 
 * @author admin
 * @date 2021-03-22
 */
public interface IChannelSubtreasuryBusiService {
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
     * @param param 子场景人员
     * @return 结果
     */
    public int insertChannelSubtreasuryBusi(ChannelSubBusiParam param);

    /**
     * 修改子场景人员
     * 
     * @param channelSubtreasuryBusi 子场景人员
     * @return 结果
     */
    public int updateChannelSubtreasuryBusi(ChannelSubtreasuryBusi channelSubtreasuryBusi);

    /**
     * 批量删除子场景人员
     * 
     * @param ids 需要删除的子场景人员ID
     * @return 结果
     */
    public int deleteChannelSubtreasuryBusiByIds(String[] ids);

    /**
     * 删除子场景人员信息
     * 
     * @param id 子场景人员ID
     * @return 结果
     */
    public int deleteChannelSubtreasuryBusiById(String id);

    /**
     * 查询未绑定子场景的人员列表
     * 
     * @param basePersonInfo 人员信息
     * @param subTreasuryId 子场景主键
     * @param channelId 场景主键
     * @return
     */
    public List<BasePersonInfo> selectUnbindPersonInfoList(BasePersonInfo basePersonInfo, String subTreasuryId,
        String channelId);

    /**
     * 单个人库关系绑定
     * 
     * @param targetBusi 子场景人员
     * @param subTreasuryCode 子场景编码
     */
    public void insertSingleSubBusiData(ChannelSubtreasuryBusi targetBusi, String subTreasuryCode);

    /**
     * 同步人库关系信息到Datamanager
     * 
     * @param sbusi
     * @return
     */
    public AjaxResult syncdata(ChannelSubtreasuryBusi sbusi);

    /**
     * 清空子场景数据
     * 
     * @param subtreasuryIds
     */
    public void clearSubData(String[] subtreasuryIds);
}
