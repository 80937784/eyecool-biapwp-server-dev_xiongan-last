package cn.eyecool.statistic.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.beust.jcommander.internal.Maps;

import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.statistic.domain.StatisticTradeLog;
import cn.eyecool.statistic.service.ITradeLogStatisticService;
import cn.eyecool.tradelog.domain.RealtimeTradeLog;

/**
 * 交易日志信息统计请求处理
 * 
 * @author mawj
 * @date 2021/08/26
 */
@RestController
@RequestMapping("/statistic/tradelog")
public class TradeLogStatisticController extends BaseController {

    @Autowired
    private ITradeLogStatisticService tradeLogStatisticService;

    /**
     * 查询今日日志数量
     * 
     * @return
     */
    @GetMapping("/countToday")
    public AjaxResult countTodaLog() {
        int passCount = tradeLogStatisticService.countToday1vNTradeLog(DictConstants.BioResult.PASS);
        int unPassCount = tradeLogStatisticService.countToday1vNTradeLog(DictConstants.BioResult.NOTPASS);
        Map<String, Object> result = Maps.newHashMap();
        result.put("passCount", passCount);
        result.put("unPassCount", unPassCount);
        result.put("count", passCount + unPassCount);
        return AjaxResult.success(result);
    }

    /**
     * 查询今日日志数量
     * 
     * @return
     */
    @GetMapping("/realtimeTrade")
    public AjaxResult realtimeTrade() {
        List<RealtimeTradeLog> realtimeTradeLog = tradeLogStatisticService.getRealtimeTradeLog();
        return AjaxResult.success(realtimeTradeLog);
    }

    /**
     * 查询交易日志[日|月]数量趋势
     * 
     * @param statisticUnit
     * @param startDate
     * @param endDate
     * @return
     */
    @GetMapping("/trend")
    public AjaxResult getTradeLogTrend(String statisticUnit, String startDate, String endDate) {
        List<StatisticTradeLog> list = tradeLogStatisticService.getTradeLogTrend(statisticUnit, startDate, endDate);
        return AjaxResult.success(list);
    }
}
