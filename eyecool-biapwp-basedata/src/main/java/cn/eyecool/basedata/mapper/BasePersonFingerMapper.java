package cn.eyecool.basedata.mapper;

import java.util.List;

import cn.eyecool.basedata.domain.BasePersonFinger;

/**
 * 指纹图像信息Mapper接口
 * 
 * @author mawj
 * @date 2021-01-27
 */
public interface BasePersonFingerMapper {
    /**
     * 查询指纹图像信息
     * 
     * @param id 指纹图像信息ID
     * @return 指纹图像信息
     */
    public BasePersonFinger selectBasePersonFingerById(String id);

    /**
     * 查询指纹图像信息列表
     * 
     * @param basePersonFinger 指纹图像信息
     * @return 指纹图像信息集合
     */
    public List<BasePersonFinger> selectBasePersonFingerList(BasePersonFinger basePersonFinger);

    /**
     * 新增指纹图像信息
     * 
     * @param basePersonFinger 指纹图像信息
     * @return 结果
     */
    public int insertBasePersonFinger(BasePersonFinger basePersonFinger);

    /**
     * 修改指纹图像信息
     * 
     * @param basePersonFinger 指纹图像信息
     * @return 结果
     */
    public int updateBasePersonFinger(BasePersonFinger basePersonFinger);

    /**
     * 删除指纹图像信息
     * 
     * @param id 指纹图像信息ID
     * @return 结果
     */
    public int deleteBasePersonFingerById(String id);

    /**
     * 批量删除指纹图像信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteBasePersonFingerByIds(String[] ids);

    /**
     * 根据人员Id修改指纹信息
     * 
     * @param basePersonFinger
     * @return
     */
    public int updateBasePersonFingerByPersonId(BasePersonFinger basePersonFinger);

    /**
     * 查询人员有效指纹数量
     * 
     * @param personId
     * @return
     */
    public int countEnabledFingerByPersonId(String personId);
}
