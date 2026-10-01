package cn.eyecool.basedata.mapper;

import java.util.List;

import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.basedata.vo.BaseVisitorInfoVO;
import org.apache.ibatis.annotations.Param;

/**
 * 人员基础信息Mapper接口
 * 
 * @author mawj
 * @date 2021-01-27
 */
public interface BasePersonInfoMapper {
    /**
     * 查询人员基础信息
     * 
     * @param id 人员基础信息ID
     * @return 人员基础信息
     */
    public BasePersonInfo selectBasePersonInfoById(String id);

    /**
     * 查询人员基础信息列表
     * 
     * @param basePersonInfo 人员基础信息
     * @return 人员基础信息集合
     */
    public List<BasePersonInfo> selectBasePersonInfoList(BasePersonInfo basePersonInfo);

    /**
     * 查询访客基础信息列表
     *
     * @param baseVisitorInfoVO 访客信息
     * @return 访客基础信息集合
     */
    public List<BasePersonInfo> selectBaseVisitorInfoList(BaseVisitorInfoVO baseVisitorInfoVO);

    /**
     * 新增人员基础信息
     * 
     * @param basePersonInfo 人员基础信息
     * @return 结果
     */
    public int insertBasePersonInfo(BasePersonInfo basePersonInfo);

    /**
     * 修改人员基础信息
     * 
     * @param basePersonInfo 人员基础信息
     * @return 结果
     */
    public int updateBasePersonInfo(BasePersonInfo basePersonInfo);

    /**
     * 删除人员基础信息
     * 
     * @param id 人员基础信息ID
     * @return 结果
     */
    public int deleteBasePersonInfoById(String id);

    /**
     * 批量删除人员基础信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteBasePersonInfoByIds(String[] ids);

    /**
     * 查询人员数量
     * 
     * @param basePersonInfo
     * @return
     */
    public int selectBasePersonCount(BasePersonInfo basePersonInfo);

    /**
     * 根据人员唯一标识或姓名查询人员列表
     * 
     * @param basePersonInfo
     * @return
     */
    public List<BasePersonInfo> selectByUidOrName(BasePersonInfo basePersonInfo);

    /**
     * 根据人员唯一标识更新人员标志
     * 
     * @param uniqueIdArray
     * @param flag
     * @return
     */
    public int updatePersonFlagByUniqueIds(String[] uniqueIds, String flag);

    /**
     *  获取场景人员最大序列流水码
     * @return 最大流水码
     */
    Long selectMaxSeriaNumForChannelBusiness();

    /**
     *  获取子场景人员最大序列流水码
     * @return 最大流水码
     */
    Long selectMaxSeriaNumForchannelSubBusi();

    /**
     * 更新设置场景人员为最大序列流水码
     * @param id 人员id
     * @param channelBusiSeqNum 最大场景人员流水码
     */
    void updateChannelBusiSeq(@Param("id") String id,@Param("channelBusiSeqNum") Long channelBusiSeqNum);

    /**
     * 更新设置子场景人员为最大序列流水码
     * @param id 人员id
     * @param subBusiSeqNum 最大子场景人员流水码
     */
    void updateChannelSubBusiSeq(@Param("id") String id,@Param("subBusiSeqNum") Long subBusiSeqNum);
}
