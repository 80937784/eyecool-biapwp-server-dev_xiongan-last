package cn.eyecool.statistic.service;

import java.util.List;

import cn.eyecool.statistic.domain.AreaLogNumInfo;

/**
 * 区域统计业务层
 * 
 * @author mawj
 * @date 2021/08/26
 */
public interface IAreaStatisticService {

    /**
     * 查询今日业务交易排名前5的区域交易数量
     * 
     * @return
     */
    public List<AreaLogNumInfo> todayTop5AreaFaceLogNum();

}
