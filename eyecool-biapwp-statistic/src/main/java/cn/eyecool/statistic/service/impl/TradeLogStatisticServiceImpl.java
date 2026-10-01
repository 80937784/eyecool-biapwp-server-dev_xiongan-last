package cn.eyecool.statistic.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.beust.jcommander.internal.Maps;

import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.statistic.domain.StatisticFaceRecogLog;
import cn.eyecool.statistic.domain.StatisticIdcardVerifyLog;
import cn.eyecool.statistic.domain.StatisticTradeLog;
import cn.eyecool.statistic.mapper.TradeLogStatisticMapper;
import cn.eyecool.statistic.service.ITradeLogStatisticService;
import cn.eyecool.tradelog.domain.FaceRealtimeTradeLog;
import cn.eyecool.tradelog.domain.RealtimeTradeLog;

/**
 * 交易日志统计业务层
 * 
 * @author mawj
 * @date 2021/08/26
 */
@Service
public class TradeLogStatisticServiceImpl implements ITradeLogStatisticService {

    @Autowired
    private TradeLogStatisticMapper tradeLogStatisticMapper;

    /**
     * 查询今日日志数量
     * 
     * @param result
     * @return
     */
    @Override
    public int countToday1vNTradeLog(String result) {
        Date startTime = DateUtils.getTodayStartTime();
        Date endTime = DateUtils.getNowDate();
        int face1vNTradeLog = tradeLogStatisticMapper.countFace1vNTradeLog(startTime, endTime, result, null);
        int finger1vNTradeLog = tradeLogStatisticMapper.countFinger1vNTradeLog(startTime, endTime, result);
        int iris1vNTradeLog = tradeLogStatisticMapper.countIris1vNTradeLog(startTime, endTime, result);
        int irisFace1vNTradeLog = tradeLogStatisticMapper.countIrisFace1vNTradeLog(startTime, endTime, result);
        return face1vNTradeLog + finger1vNTradeLog + iris1vNTradeLog + irisFace1vNTradeLog;
    }

    /**
     * 查询实时交易
     * 
     * @return
     */
    @Override
    public List<RealtimeTradeLog> getRealtimeTradeLog() {
        return tradeLogStatisticMapper.getRealtimeTradeLog();
    }

    /**
     * 获取交易日志[日|月]数量趋势
     * 
     * @param statisticUnit
     * @param startDate
     * @param endDate
     * @return
     */
    @Override
    public List<StatisticTradeLog> getTradeLogTrend(String statisticUnit, String startDate, String endDate) {
        return tradeLogStatisticMapper.getTradeLogTrend(statisticUnit, startDate, endDate);
    }

    /**
     * 查询所有人脸日志数量
     * 
     * @param result
     * @return
     */
    @Override
    public int countAllFaceTradeLog(String result) {
        int face1v1TradeLog = tradeLogStatisticMapper.countFace1v1TradeLog(null, null, result);
        int face1vNTradeLog = tradeLogStatisticMapper.countFace1vNTradeLog(null, null, result, null);
        return face1v1TradeLog + face1vNTradeLog;
    }

    /**
     * 查询所有人脸日志数量
     * 
     * @param result
     * @param validType
     * @return
     */
    @Override
    public Map<String, Integer> countTodayFaceTradeLog(String result) {
        Map<String, Integer> map = Maps.newHashMap();
        Date startTime = DateUtils.getTodayStartTime();
        Date endTime = DateUtils.getNowDate();
        int face1v1TradeLog = tradeLogStatisticMapper.countFace1v1TradeLog(startTime, endTime, result);
        int face1vNTradeLog =
            tradeLogStatisticMapper.countFace1vNTradeLog(startTime, endTime, result, DictConstants.PassValidType.FACE);
        int idCard1vNTradeLog = tradeLogStatisticMapper.countFace1vNTradeLog(startTime, endTime, result,
            DictConstants.PassValidType.ID_CARD);
        int healthCode1vNTradeLog = tradeLogStatisticMapper.countFace1vNTradeLog(startTime, endTime, result,
            DictConstants.PassValidType.HEALTH_CODE);
        map.put("todayFaceTradeLog", face1v1TradeLog + face1vNTradeLog + idCard1vNTradeLog + healthCode1vNTradeLog);
        map.put("todayFace1vNLog", face1vNTradeLog);
        map.put("todayIdCardLog", face1v1TradeLog + idCard1vNTradeLog);
        map.put("todayHealthCodeLog", healthCode1vNTradeLog);
        return map;
    }

    /**
     * 查询人脸识别日志[日|月]数量趋势
     * 
     * @param statisticUnit
     * @param startDate
     * @param endDate
     * @return
     */
    @Override
    public List<StatisticFaceRecogLog> getFaceRecogLogTrend(String statisticUnit, String startDate, String endDate) {
        return tradeLogStatisticMapper.getFaceRecogLogTrend(statisticUnit, startDate, endDate);
    }

    /**
     * 查询身份证比对日志[日|月]数量趋势
     * 
     * @param statisticUnit
     * @param startDate
     * @param endDate
     * @return
     */
    @Override
    public List<StatisticIdcardVerifyLog> getIdcardVerifyLogTrend(String statisticUnit, String startDate,
        String endDate) {
        return tradeLogStatisticMapper.getIdcardVerifyLogTrend(statisticUnit, startDate, endDate);
    }

    /**
     * 查询人脸实时交易
     * 
     * @return
     */
    @Override
    public List<FaceRealtimeTradeLog> getFaceRealtimeTradeLog() {
        return tradeLogStatisticMapper.getFaceRealtimeTradeLog();
    }

    /**
     * 查询异常人脸实时交易
     * 
     * @return
     */
    @Override
    public List<FaceRealtimeTradeLog> getFaceAbnormalRealtimeTrade() {
        return tradeLogStatisticMapper.getFaceAbnormalRealtimeTrade();
    }
}
