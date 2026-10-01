package cn.eyecool.statistic.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.annotation.SqlParser;

import cn.eyecool.statistic.domain.StatisticTradePerson;

/**
 * 人员信息统计数据层
 * 
 * @author mawj
 * @date 2021/08/26
 */
@SuppressWarnings("deprecation")
public interface PersonStatisticMapper {

    /**
     * 查询人员数量
     * 
     * @return
     */
    public int countPerson();

    /**
     * 获取通行人员数量趋势
     * 
     * @param statisticUnit
     * @param startDate
     * @param endDate
     * @return
     */
    public List<StatisticTradePerson> getTradePersonTrend(@Param("statisticUnit") String statisticUnit,
        @Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 插入人员统计信息
     * 
     * @param statisticTradePerson
     * @return
     */
    @SqlParser(filter = true)
    public int insertStatisticTradePerson(StatisticTradePerson statisticTradePerson);

    /**
     * 根据统计日期查询人员统计信息
     * 
     * @param statisticDate
     * @param statisticUnit
     * @param tenantId
     * @return
     */
    @SqlParser(filter = true)
    public StatisticTradePerson getStatisticPersonByDate(@Param("statisticDate") String statisticDate,
        @Param("statisticUnit") String statisticUnit, @Param("tenantId") String tenantId);

}
