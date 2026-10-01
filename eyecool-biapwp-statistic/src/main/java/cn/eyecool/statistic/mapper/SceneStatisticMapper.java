package cn.eyecool.statistic.mapper;

/**
 * 场景信息统计数据层
 * 
 * @author mawj
 * @date 2021/08/26
 */
public interface SceneStatisticMapper {

    /**
     * 查询场景数量
     * 
     * @return
     */
    public int countScene();

    /**
     * 查询子场景数量
     * 
     * @return
     */
    public int countSubScene();

}
