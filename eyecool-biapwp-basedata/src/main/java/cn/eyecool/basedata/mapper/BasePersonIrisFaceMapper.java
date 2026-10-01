package cn.eyecool.basedata.mapper;

import java.util.List;

import cn.eyecool.basedata.domain.BasePersonIrisFace;

/**
 * 虹膜人脸多模态Mapper接口
 * 
 * @author mawj
 * @date 2021-01-27
 */
public interface BasePersonIrisFaceMapper {
    /**
     * 查询虹膜人脸多模态
     * 
     * @param id 虹膜人脸多模态ID
     * @return 虹膜人脸多模态
     */
    public BasePersonIrisFace selectBasePersonIrisFaceById(String id);

    /**
     * 查询虹膜人脸多模态列表
     * 
     * @param basePersonIrisFace 虹膜人脸多模态
     * @return 虹膜人脸多模态集合
     */
    public List<BasePersonIrisFace> selectBasePersonIrisFaceList(BasePersonIrisFace basePersonIrisFace);

    /**
     * 新增虹膜人脸多模态
     * 
     * @param basePersonIrisFace 虹膜人脸多模态
     * @return 结果
     */
    public int insertBasePersonIrisFace(BasePersonIrisFace basePersonIrisFace);

    /**
     * 修改虹膜人脸多模态
     * 
     * @param basePersonIrisFace 虹膜人脸多模态
     * @return 结果
     */
    public int updateBasePersonIrisFace(BasePersonIrisFace basePersonIrisFace);

    /**
     * 根据人员ID修改虹膜信息
     * 
     * @param basePersonIrisFace
     * @return
     */
    public int updateBasePersonIrisFaceByPersonId(BasePersonIrisFace basePersonIrisFace);

    /**
     * 删除虹膜人脸多模态
     * 
     * @param id 虹膜人脸多模态ID
     * @return 结果
     */
    public int deleteBasePersonIrisFaceById(String id);

    /**
     * 批量删除虹膜人脸多模态
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteBasePersonIrisFaceByIds(String[] ids);

    /**
     * 查询人员有效虹膜人脸多模态数量
     * 
     * @param personId
     * @return
     */
    public int countEnabledFaceIrisByPersonId(String personId);
}
