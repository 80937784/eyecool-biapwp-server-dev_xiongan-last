package cn.eyecool.basedata.mapper;

import java.util.List;

import cn.eyecool.basedata.domain.BasePersonIris;

/**
 * 虹膜图像信息Mapper接口
 * 
 * @author mawj
 * @date 2021-01-27
 */
public interface BasePersonIrisMapper {
    /**
     * 查询虹膜图像信息
     * 
     * @param id 虹膜图像信息ID
     * @return 虹膜图像信息
     */
    public BasePersonIris selectBasePersonIrisById(String id);

    /**
     * 查询虹膜图像信息列表
     * 
     * @param basePersonIris 虹膜图像信息
     * @return 虹膜图像信息集合
     */
    public List<BasePersonIris> selectBasePersonIrisList(BasePersonIris basePersonIris);

    /**
     * 新增虹膜图像信息
     * 
     * @param basePersonIris 虹膜图像信息
     * @return 结果
     */
    public int insertBasePersonIris(BasePersonIris basePersonIris);

    /**
     * 修改虹膜图像信息
     * 
     * @param basePersonIris 虹膜图像信息
     * @return 结果
     */
    public int updateBasePersonIris(BasePersonIris basePersonIris);

    /**
     * 删除虹膜图像信息
     * 
     * @param id 虹膜图像信息ID
     * @return 结果
     */
    public int deleteBasePersonIrisById(String id);

    /**
     * 批量删除虹膜图像信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteBasePersonIrisByIds(String[] ids);

    /**
     * 根据人员ID修改虹膜信息
     * 
     * @param basePersonIris
     * @return
     */
    public int updateBasePersonIrisByPersonId(BasePersonIris basePersonIris);

    /**
     * 查询人员有效虹膜数量
     * 
     * @param personId
     * @return
     */
    public int countEnabledIrisByPersonId(String personId);
}
