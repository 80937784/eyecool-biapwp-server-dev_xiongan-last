package cn.eyecool.statistic.service;

import java.util.List;
import java.util.Map;

import cn.eyecool.statistic.domain.StatisticFaceRecogLog;
import cn.eyecool.statistic.domain.StatisticIdcardVerifyLog;
import cn.eyecool.statistic.domain.StatisticTradeLog;
import cn.eyecool.tradelog.domain.FaceRealtimeTradeLog;
import cn.eyecool.tradelog.domain.RealtimeTradeLog;

/**
 * 设备统计业务层
 * 
 * @author mawj
 * @date 2021/08/26
 */
public interface ITradeLogStatisticService {

    /**
     * 查询今日日志数量(根据结果)
     * 
     * @return
     */
    public int countToday1vNTradeLog(String result);

    /**
     * 查询实时交易
     * 
     * @return
     */
    public List<RealtimeTradeLog> getRealtimeTradeLog();

    /**
     * 获取交易日志[日|月]数量趋势
     * 
     * @param statisticUnit
     * @param startDate
     * @param endDate
     * @return
     */
    public List<StatisticTradeLog> getTradeLogTrend(String statisticUnit, String startDate, String endDate);

    /**
     * 查询人脸日志总量
     * 
     * @param result
     * @return
     */
    public int countAllFaceTradeLog(String result);

    /**
     * 查询今天人脸交易日志数量
     * 
     * @param result
     * @return
     */
    public Map<String, Integer> countTodayFaceTradeLog(String result);

    /**
     * 查询人脸识别日志[日|月]数量趋势
     * 
     * @param statisticUnit
     * @param startDate
     * @param endDate
     * @return
     */
    public List<StatisticFaceRecogLog> getFaceRecogLogTrend(String statisticUnit, String startDate, String endDate);

    /**
     * 查询身份证比对日志[日|月]数量趋势
     * 
     * @param statisticUnit
     * @param startDate
     * @param endDate
     * @return
     */
    public List<StatisticIdcardVerifyLog> getIdcardVerifyLogTrend(String statisticUnit, String startDate,
        String endDate);

    /**
     * 查询人脸实时交易
     * 
     * @return
     */
    public List<FaceRealtimeTradeLog> getFaceRealtimeTradeLog();

    /**
     * 查询异常人脸实时交易
     * 
     * @return
     */
    public List<FaceRealtimeTradeLog> getFaceAbnormalRealtimeTrade();

}
