package cn.eyecool.statistic.mapper;

import java.util.List;

import cn.eyecool.area.domain.AreaModel;

/**
 * 区域信息统计数据层
 * 
 * @author mawj
 * @date 2021/08/26
 */
public interface AreaStatisticMapper {

    /**
     * 查询1级区域列表
     * 
     * @return
     */
    public List<AreaModel> getLevel1AreaModel();

    /**
     * 根据节点ID查询所有子节点
     * 
     * @return
     */
    public List<AreaModel> getAllChildrenById(Long id);

}
