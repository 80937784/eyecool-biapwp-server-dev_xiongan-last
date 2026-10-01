package cn.eyecool.area.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.eyecool.area.domain.AreaModel;

/**
 * 区域Mapper接口
 * 
 * @author admin
 * @date 2021-03-26
 */
public interface AreaModelMapper {
    /**
     * 查询区域
     * 
     * @param id 区域ID
     * @return 区域
     */
    public AreaModel selectAreaModelById(Long id);

    /**
     * 查询区域列表
     * 
     * @param areaModel 区域
     * @return 区域集合
     */
    public List<AreaModel> selectAreaModelList(AreaModel areaModel);

    /**
     * 新增区域
     * 
     * @param areaModel 区域
     * @return 结果
     */
    public int insertAreaModel(AreaModel areaModel);

    /**
     * 修改区域
     * 
     * @param areaModel 区域
     * @return 结果
     */
    public int updateAreaModel(AreaModel areaModel);

    /**
     * 删除区域
     * 
     * @param id 区域ID
     * @return 结果
     */
    public int deleteAreaModelById(Long id);

    /**
     * 批量删除区域
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteAreaModelByIds(Long[] ids);

    /**
     * 校验区域名称唯一性
     * 
     * @param areaName
     * @param parentId
     * @return
     */
    public AreaModel checkAreaNameUnique(@Param("areaName") String areaName, @Param("parentId") Long parentId);

    /**
     * 根据ID查询所有子区域数量
     * 
     * @param id
     * @param status
     * @return
     */
    public int selectChildrenCountById(@Param("id") Long id, @Param("status") String status);

    /**
     * 修改所在区域的父级区域状态
     * 
     * @param areaModel
     */
    public void updateAreaStatus(AreaModel areaModel);

    /**
     * 查询子区域
     * 
     * @param areaId
     * @return
     */
    public List<AreaModel> selectChildrenAreaById(Long areaId);

    /**
     * 修改子元素关系
     * 
     * @param areas 子元素
     * @return 结果
     */
    public int updateAreaChildren(@Param("areas") List<AreaModel> areas);
}
