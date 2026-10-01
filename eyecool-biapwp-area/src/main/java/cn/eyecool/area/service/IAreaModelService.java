package cn.eyecool.area.service;

import java.util.List;

import cn.eyecool.area.domain.AreaModel;
import cn.eyecool.common.core.domain.TreeSelect;

/**
 * 区域Service接口
 * 
 * @author admin
 * @date 2021-03-26
 */
public interface IAreaModelService {
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
     * 批量删除区域
     * 
     * @param ids 需要删除的区域ID
     * @return 结果
     */
    public int deleteAreaModelByIds(Long[] ids);

    /**
     * 删除区域信息
     * 
     * @param id 区域ID
     * @return 结果
     */
    public int deleteAreaModelById(Long id);

    /**
     * 校验区域名称是否唯一
     * 
     * @param areaModel
     * @return
     */
    public boolean checkAreaNameUnique(AreaModel areaModel);

    /**
     * 查询未停用的子区域数量
     * 
     * @param id
     * @return
     */
    public int selectNormalChildrenCountById(Long id);

    /**
     * 查询子区域数量
     * 
     * @param id
     * @return
     */
    public int selectChildrenCount(Long id);

    /**
     * 构建前端所需要下拉树结构
     * 
     * @param list 区域列表
     * @return 下拉树结构列表
     */
    public List<TreeSelect> buildAreaTreeSelect(List<AreaModel> list);

    /**
     * 构建前端所需要树结构
     * 
     * @param list 区域列表
     * @return 树结构列表
     */
    List<AreaModel> buildAreaTree(List<AreaModel> list);
}
