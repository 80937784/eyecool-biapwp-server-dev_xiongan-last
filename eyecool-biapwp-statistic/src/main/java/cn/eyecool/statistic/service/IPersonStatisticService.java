package cn.eyecool.statistic.service;

import java.util.List;
import java.util.Map;

import cn.eyecool.statistic.domain.StatisticTradePerson;

/**
 * 人员统计业务层
 * 
 * @author mawj
 * @date 2021/08/26
 */
public interface IPersonStatisticService {

    /**
     * 查询所有人员数量
     * 
     * @return
     */
    public int countPerson();

    /**
     * 查询今日通过人员数量
     * 
     * @return
     */
    public int countTodayPassPerson();

    /**
     * 查询今日通过时间趋势
     * 
     * @return
     */
    public Map<String, Integer> todayPassTrend();

    /**
     * 查询通行人员[日|月]数量趋势
     * 
     * @param statisticUnit
     * @param startDate
     * @param endDate
     * @return
     */
    public List<StatisticTradePerson> getTradePersonTrend(String statisticUnit, String startDate, String endDate);

    /**
     * 根据时间单位返回通行人员数量统计
     * 
     * @return
     */
    public Map<String, Integer> countFacePassPersonByUnit();

}
