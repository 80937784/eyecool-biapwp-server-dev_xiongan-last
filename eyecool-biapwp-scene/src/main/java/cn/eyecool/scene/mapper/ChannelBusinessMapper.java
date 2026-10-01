package cn.eyecool.scene.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.annotation.SqlParser;

import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.scene.domain.BasePersonLiveUpdateInfo;
import cn.eyecool.scene.domain.ChannelBusiness;

/**
 * 场景人员Mapper接口
 * 
 * @author admin
 * @date 2021-03-22
 */
@SuppressWarnings("deprecation")
public interface ChannelBusinessMapper {
    /**
     * 查询场景人员
     * 
     * @param id 场景人员ID
     * @return 场景人员
     */
    public ChannelBusiness selectChannelBusinessById(String id);

    /**
     * 查询场景人员列表
     * 
     * @param channelBusiness 场景人员
     * @return 场景人员集合
     */
    public List<ChannelBusiness> selectChannelBusinessList(ChannelBusiness channelBusiness);

    /**
     * 新增场景人员
     * 
     * @param channelBusiness 场景人员
     * @return 结果
     */
    public int insertChannelBusiness(ChannelBusiness channelBusiness);

    /**
     * 修改场景人员
     * 
     * @param channelBusiness 场景人员
     * @return 结果
     */
    public int updateChannelBusiness(ChannelBusiness channelBusiness);

    /**
     * 删除场景人员
     * 
     * @param id 场景人员ID
     * @return 结果
     */
    public int deleteChannelBusinessById(String id);

    /**
     * 批量删除场景人员
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteChannelBusinessByIds(String[] ids);

    /**
     * 查询未绑定场景的人员列表
     * 
     * @param basePersonInfo 人员信息
     * @param channelId 场景主键
     * @return
     */
    public List<BasePersonInfo> selectUnbindPersonInfoList(@Param("channelId") String channelId,
        @Param("basePersonInfo") BasePersonInfo basePersonInfo);

    /**
     * 查询最大实时更新业务序列
     * 
     * @return
     */
    @SqlParser(filter = true)
    public Long selectLiveUpdateMaxSeriaNum();

    /**
     * 根据场景主键删除场景人员
     * 
     * @param channelIds
     */
    public void deleteChannelBusinessByChannelIds(String[] channelIds);

    /**
     * 查询实时同步更新人员列表
     * 
     * @param map 人员信息
     * @return 人员集合
     */
    public List<BasePersonLiveUpdateInfo> selectLiveUpdateBasePersonInfoList(Map<String, Object> map);
}
