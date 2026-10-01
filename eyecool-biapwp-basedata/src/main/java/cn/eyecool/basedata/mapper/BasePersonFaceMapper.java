package cn.eyecool.basedata.mapper;

import java.util.List;

import cn.eyecool.basedata.domain.BasePersonFace;

/**
 * 人脸图像信息Mapper接口
 * 
 * @author mawj
 * @date 2021-01-27
 */
public interface BasePersonFaceMapper {
    /**
     * 查询人脸图像信息
     * 
     * @param id 人脸图像信息ID
     * @return 人脸图像信息
     */
    public BasePersonFace selectBasePersonFaceById(String id);

    /**
     * 查询人脸图像信息列表
     * 
     * @param basePersonFace 人脸图像信息
     * @return 人脸图像信息集合
     */
    public List<BasePersonFace> selectBasePersonFaceList(BasePersonFace basePersonFace);

    /**
     * 新增人脸图像信息
     * 
     * @param basePersonFace 人脸图像信息
     * @return 结果
     */
    public int insertBasePersonFace(BasePersonFace basePersonFace);

    /**
     * 修改人脸图像信息
     * 
     * @param basePersonFace 人脸图像信息
     * @return 结果
     */
    public int updateBasePersonFace(BasePersonFace basePersonFace);

    /**
     * 删除人脸图像信息
     * 
     * @param id 人脸图像信息ID
     * @return 结果
     */
    public int deleteBasePersonFaceById(String id);

    /**
     * 批量删除人脸图像信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteBasePersonFaceByIds(String[] ids);

    /**
     * 根据人员ID修改人脸信息
     * 
     * @param basePersonFace
     * @return
     */
    public int updateBasePersonFaceByPersonId(BasePersonFace basePersonFace);

    /**
     * 查询人员有效人脸数量
     * 
     * @param personId
     * @return
     */
    public int countEnabledFaceByPersonId(String personId);
}
