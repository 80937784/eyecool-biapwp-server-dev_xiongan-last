package cn.eyecool.statistic.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Maps;

import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.statistic.domain.StatisticTradePerson;
import cn.eyecool.statistic.service.IPersonStatisticService;

/**
 * 人员信息统计请求处理
 * 
 * @author mawj
 * @date 2021/08/26
 */
@RestController
@RequestMapping("/statistic/person")
public class PersonStatisticController extends BaseController {

    @Autowired
    private IPersonStatisticService personStatisticService;

    /**
     * 统计人员数量
     * 
     * @return
     */
    @GetMapping("/countAll")
    public AjaxResult countAllPerson() {
        int count = personStatisticService.countPerson();
        return AjaxResult.success(count);
    }

    /**
     * 统计今日通行人员数量
     * 
     * @return
     */
    @GetMapping("/countTodayPass")
    public AjaxResult countTodayPassPerson() {
        int count = personStatisticService.countTodayPassPerson();
        Map<String, Object> result = Maps.newHashMap();
        result.put("employee", count);
        result.put("visitor", 0);
        return AjaxResult.success(result);
    }

    /**
     * 统计今日通行人员数量
     * 
     * @return
     */
    @GetMapping("/todayPassTrend")
    public AjaxResult todayPassTrend() {
        Map<String, Integer> trend = personStatisticService.todayPassTrend();
        return AjaxResult.success(trend);
    }

    /**
     * 查询通行人员[日|月]数量趋势
     * 
     * @param statisticUnit
     * @param startDate
     * @param endDate
     * @return
     */
    @GetMapping("/passTrend")
    public AjaxResult getTradePersonTrend(String statisticUnit, String startDate, String endDate) {
        List<StatisticTradePerson> list = personStatisticService.getTradePersonTrend(statisticUnit, startDate, endDate);
        return AjaxResult.success(list);
    }
}
