package cn.eyecool.statistic.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.beust.jcommander.internal.Maps;

import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.statistic.domain.StatisticTradePerson;
import cn.eyecool.statistic.mapper.PersonStatisticMapper;
import cn.eyecool.statistic.mapper.TradeLogStatisticMapper;
import cn.eyecool.statistic.service.IPersonStatisticService;

/**
 * 人员统计业务层
 * 
 * @author mawj
 * @date 2021/08/26
 */
@Service
public class PersonStatisticServiceImpl implements IPersonStatisticService {

    @Autowired
    private PersonStatisticMapper personStatisticMapper;
    @Autowired
    private TradeLogStatisticMapper tradeLogStatisticMapper;

    /**
     * 查询所有人员数量
     * 
     * @return
     */
    @Override
    public int countPerson() {
        return personStatisticMapper.countPerson();
    }

    /**
     * 查询今日通过人员数量
     * 
     * @return
     */
    @Override
    public int countTodayPassPerson() {
        Date startTime = DateUtils.getTodayStartTime();
        Date endTime = DateUtils.getNowDate();
        int face1vNPassPerson = tradeLogStatisticMapper.countFace1vNPassPerson(startTime, endTime);
        int finger1vNPassPerson = tradeLogStatisticMapper.countFinger1vNPassPerson(startTime, endTime);
        int iris1vNPassPerson = tradeLogStatisticMapper.countIris1vNPassPerson(startTime, endTime);
        int irisFace1vNPassPerson = tradeLogStatisticMapper.countIrisFace1vNPassPerson(startTime, endTime);
        return face1vNPassPerson + finger1vNPassPerson + iris1vNPassPerson + irisFace1vNPassPerson;
    }

    /**
     * 通行人员今日同行时间曲线
     * 
     * @return
     */
    @Override
    public Map<String, Integer> todayPassTrend() {
        Map<String, Integer> result = Maps.newHashMap();
        Date startTime = DateUtils.getTodayStartTime();
        GregorianCalendar calendar = new GregorianCalendar();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        for (int i = 0; i < 12; i++) {
            if (hour / 2 >= i) {
                int countFace1vNPassPerson = tradeLogStatisticMapper.countFace1vNPassPerson(
                    DateUtils.addHours(startTime, 2 * i), DateUtils.addHours(startTime, 2 * i + 2));
                int countFinger1vNPassPerson = tradeLogStatisticMapper.countFinger1vNPassPerson(
                    DateUtils.addHours(startTime, 2 * i), DateUtils.addHours(startTime, 2 * i + 2));
                int countIris1vNPassPerson = tradeLogStatisticMapper.countIris1vNPassPerson(
                    DateUtils.addHours(startTime, 2 * i), DateUtils.addHours(startTime, 2 * i + 2));
                int countIrisFace1vNPassPerson = tradeLogStatisticMapper.countIrisFace1vNPassPerson(
                    DateUtils.addHours(startTime, 2 * i), DateUtils.addHours(startTime, 2 * i + 2));
                int count = countFace1vNPassPerson + countFinger1vNPassPerson + countIris1vNPassPerson
                    + countIrisFace1vNPassPerson;
                result.put(String.valueOf(2 * i + 1), count);
            }
        }
        return result;
    }

    /**
     * 查询通行人员[日|月]数量趋势
     * 
     * @param statisticUnit
     * @param startDate
     * @param endDate
     * @return
     */
    @Override
    public List<StatisticTradePerson> getTradePersonTrend(String statisticUnit, String startDate, String endDate) {
        return personStatisticMapper.getTradePersonTrend(statisticUnit, startDate, endDate);
    }

    /**
     * 根据时间单位返回人脸通行人员数量统计
     * 
     * @return
     */
    @Override
    public Map<String, Integer> countFacePassPersonByUnit() {
        Map<String, Integer> map = Maps.newHashMap();
        // 今日通过
        Date todayStartTime = DateUtils.getTodayStartTime();
        Date nowTime = DateUtils.getNowDate();
        int todayFace1vNPassPerson = tradeLogStatisticMapper.countFace1vNPassPerson(todayStartTime, nowTime);
        int todayFace1v1PassPerson = tradeLogStatisticMapper.countFace1v1PassPerson(todayStartTime, nowTime);
        map.put("todayPassPerson", todayFace1vNPassPerson + todayFace1v1PassPerson);
        // 昨日通过
        Date yestodayStartTime = DateUtils.addDays(todayStartTime, -1);
        int yestodayFace1vNPassPerson =
            tradeLogStatisticMapper.countFace1vNPassPerson(yestodayStartTime, todayStartTime);
        int yestodayFace1v1PassPerson =
            tradeLogStatisticMapper.countFace1v1PassPerson(yestodayStartTime, todayStartTime);
        map.put("yestodayPassPerson", yestodayFace1vNPassPerson + yestodayFace1v1PassPerson);
        // 本周通过
        Date weekStartTime = DateUtils.getWeekStartTime();
        int weekFace1vNPassPerson = tradeLogStatisticMapper.countFace1vNPassPerson(weekStartTime, nowTime);
        int weekFace1v1PassPerson = tradeLogStatisticMapper.countFace1v1PassPerson(weekStartTime, nowTime);
        map.put("weekPassPerson", weekFace1vNPassPerson + weekFace1v1PassPerson);
        // 本月通过
        Date monthStartTime = DateUtils.getMonthStartTime();
        int monthFace1vNPassPerson = tradeLogStatisticMapper.countFace1vNPassPerson(monthStartTime, nowTime);
        int monthFace1v1PassPerson = tradeLogStatisticMapper.countFace1v1PassPerson(monthStartTime, nowTime);
        map.put("monthPassPerson", monthFace1vNPassPerson + monthFace1v1PassPerson);
        return map;
    }

}
