package cn.eyecool.statistic.task;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.domain.entity.SysTenant;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.statistic.constant.StatisticConstants;
import cn.eyecool.statistic.domain.StatisticTradeLog;
import cn.eyecool.statistic.domain.TenantTradeLogCount;
import cn.eyecool.statistic.mapper.TradeLogStatisticMapper;
import cn.eyecool.system.service.ISysTenantService;
import lombok.extern.slf4j.Slf4j;

/**
 * 统计通行人员定时任务
 * 
 * @author mawj
 * @date 2021/08/31
 */
@Component("statisticTradeLogTask")
@Slf4j
public class StatisticTradeLogTask {

    @Autowired
    private TradeLogStatisticMapper tradeLogStatisticMapper;
    @Autowired
    private ISysTenantService sysTenantService;
    @Autowired
    private TenantProperties tenantProperties;

    /**
     * 执行统计
     */
    public void execute() {
        boolean tenantEnabled = Boolean.TRUE.equals(tenantProperties.getEnabled());
        if (tenantEnabled) {
            // 定时任务需要对所有租户进行处理，线程上下文不能携带租户信息，租户需要业务单独设置
            TenantContextHolder.clear();
        }
        Date todayStartTime = DateUtils.getTodayStartTime();
        Date yestodayStartTime = DateUtils.addDays(todayStartTime, -1);
        String statisticDate = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, yestodayStartTime);
        if (!tenantEnabled) {
            // 未开启租户情况下，判断是否已经统计过，统计过则不再重复统计
            StatisticTradeLog statisticTradeLog = tradeLogStatisticMapper.getStatistictTradeLogByDate(statisticDate,
                StatisticConstants.StatisticUnit.DAY, null);
            if (null != statisticTradeLog) {
                log.info("Pass log stats already exist, statisticDate:[{}]", statisticDate);
                return;
            }
        }
        List<TenantTradeLogCount> face1vNPassTradeLog = tradeLogStatisticMapper
            .countFace1vNTradeLogByTenantId(yestodayStartTime, todayStartTime, DictConstants.BioResult.PASS, null);
        List<TenantTradeLogCount> face1vNNotPassTradeLog = tradeLogStatisticMapper
            .countFace1vNTradeLogByTenantId(yestodayStartTime, todayStartTime, DictConstants.BioResult.NOTPASS, null);
        List<TenantTradeLogCount> finger1vNPassTradeLog = tradeLogStatisticMapper
            .countFinger1vNTradeLogByTenantId(yestodayStartTime, todayStartTime, DictConstants.BioResult.PASS);
        List<TenantTradeLogCount> finger1vNNotPassTradeLog = tradeLogStatisticMapper
            .countFinger1vNTradeLogByTenantId(yestodayStartTime, todayStartTime, DictConstants.BioResult.NOTPASS);
        List<TenantTradeLogCount> iris1vNPassTradeLog = tradeLogStatisticMapper
            .countIris1vNTradeLogByTenantId(yestodayStartTime, todayStartTime, DictConstants.BioResult.PASS);
        List<TenantTradeLogCount> iris1vNNotPassTradeLog = tradeLogStatisticMapper
            .countIris1vNTradeLogByTenantId(yestodayStartTime, todayStartTime, DictConstants.BioResult.NOTPASS);
        List<TenantTradeLogCount> irisFace1vNPassTradeLog = tradeLogStatisticMapper
            .countIrisFace1vNTradeLogByTenantId(yestodayStartTime, todayStartTime, DictConstants.BioResult.PASS);
        List<TenantTradeLogCount> irisFace1vNNotPassTradeLog = tradeLogStatisticMapper
            .countIrisFace1vNTradeLogByTenantId(yestodayStartTime, todayStartTime, DictConstants.BioResult.NOTPASS);
        if (!tenantEnabled) {
            // 未开启租户情况下统计
            int passNum = 0;
            int notpassNum = 0;
            int tradeNum = 0;
            if (CollectionUtils.isNotEmpty(face1vNPassTradeLog)) {
                passNum += face1vNPassTradeLog.get(0).getTradeNum();
            }
            if (CollectionUtils.isNotEmpty(face1vNNotPassTradeLog)) {
                notpassNum += face1vNNotPassTradeLog.get(0).getTradeNum();
            }
            if (CollectionUtils.isNotEmpty(finger1vNPassTradeLog)) {
                passNum += finger1vNPassTradeLog.get(0).getTradeNum();
            }
            if (CollectionUtils.isNotEmpty(finger1vNNotPassTradeLog)) {
                notpassNum += finger1vNNotPassTradeLog.get(0).getTradeNum();
            }
            if (CollectionUtils.isNotEmpty(iris1vNPassTradeLog)) {
                passNum += iris1vNPassTradeLog.get(0).getTradeNum();
            }
            if (CollectionUtils.isNotEmpty(iris1vNNotPassTradeLog)) {
                notpassNum += iris1vNNotPassTradeLog.get(0).getTradeNum();
            }
            if (CollectionUtils.isNotEmpty(irisFace1vNPassTradeLog)) {
                passNum += irisFace1vNPassTradeLog.get(0).getTradeNum();
            }
            if (CollectionUtils.isNotEmpty(irisFace1vNNotPassTradeLog)) {
                notpassNum += irisFace1vNNotPassTradeLog.get(0).getTradeNum();
            }
            tradeNum = passNum + notpassNum;
            StatisticTradeLog tradeLog = new StatisticTradeLog();
            tradeLog.setId(IdWorker.getNextStringId());
            tradeLog.setTradeNum(tradeNum);
            tradeLog.setPassNum(passNum);
            tradeLog.setNotpassNum(notpassNum);
            tradeLog.setStatisticUnit(StatisticConstants.StatisticUnit.DAY);
            tradeLog.setStatisticDate(statisticDate);
            tradeLogStatisticMapper.insertStatisticTradeLog(tradeLog);
            return;
        }
        // 多租户模式下统计
        Map<String, Integer> face1vNPassTradeLogMap = face1vNPassTradeLog.stream()
            .collect(Collectors.toMap(TenantTradeLogCount::getTenantId, TenantTradeLogCount::getTradeNum));
        Map<String, Integer> face1vNNotPassTradeLogMap = face1vNNotPassTradeLog.stream()
            .collect(Collectors.toMap(TenantTradeLogCount::getTenantId, TenantTradeLogCount::getTradeNum));
        Map<String, Integer> finger1vNPassTradeLogMap = finger1vNPassTradeLog.stream()
            .collect(Collectors.toMap(TenantTradeLogCount::getTenantId, TenantTradeLogCount::getTradeNum));
        Map<String, Integer> finger1vNNotPassTradeLogMap = finger1vNNotPassTradeLog.stream()
            .collect(Collectors.toMap(TenantTradeLogCount::getTenantId, TenantTradeLogCount::getTradeNum));
        Map<String, Integer> iris1vNPassTradeLogMap = iris1vNPassTradeLog.stream()
            .collect(Collectors.toMap(TenantTradeLogCount::getTenantId, TenantTradeLogCount::getTradeNum));
        Map<String, Integer> iris1vNNotPassTradeLogMap = iris1vNNotPassTradeLog.stream()
            .collect(Collectors.toMap(TenantTradeLogCount::getTenantId, TenantTradeLogCount::getTradeNum));
        Map<String, Integer> irisFace1vNPassTradeLogMap = irisFace1vNPassTradeLog.stream()
            .collect(Collectors.toMap(TenantTradeLogCount::getTenantId, TenantTradeLogCount::getTradeNum));
        Map<String, Integer> irisFace1vNNotPassTradeLogMap = irisFace1vNNotPassTradeLog.stream()
            .collect(Collectors.toMap(TenantTradeLogCount::getTenantId, TenantTradeLogCount::getTradeNum));
        SysTenant tenantCondition = new SysTenant();
        tenantCondition.setTenantState(DictConstants.TenantState.NORMAL);
        List<SysTenant> tenantList = sysTenantService.selectSysTenantList(tenantCondition);
        for (SysTenant tenant : tenantList) {
            String tenantId = tenant.getTenantId();
            StatisticTradeLog statisticTradeLog = tradeLogStatisticMapper.getStatistictTradeLogByDate(statisticDate,
                StatisticConstants.StatisticUnit.DAY, tenantId);
            if (null != statisticTradeLog) {
                log.info("Pass log stats already exist,tenantId:[{}], statisticDate:[{}]", tenantId, statisticDate);
                continue;
            }
            Integer face1vNPassLogNum = face1vNPassTradeLogMap.get(tenantId);
            face1vNPassLogNum = null == face1vNPassLogNum ? 0 : face1vNPassLogNum;
            Integer face1vNNotPassLogNum = face1vNNotPassTradeLogMap.get(tenantId);
            face1vNNotPassLogNum = null == face1vNNotPassLogNum ? 0 : face1vNNotPassLogNum;
            Integer finger1vNPassLogNum = finger1vNPassTradeLogMap.get(tenantId);
            finger1vNPassLogNum = null == finger1vNPassLogNum ? 0 : finger1vNPassLogNum;
            Integer finger1vNNotPassLogNum = finger1vNNotPassTradeLogMap.get(tenantId);
            finger1vNNotPassLogNum = null == finger1vNNotPassLogNum ? 0 : finger1vNNotPassLogNum;
            Integer iris1vNPassLogNum = iris1vNPassTradeLogMap.get(tenantId);
            iris1vNPassLogNum = null == iris1vNPassLogNum ? 0 : iris1vNPassLogNum;
            Integer iris1vNNotPassLogNum = iris1vNNotPassTradeLogMap.get(tenantId);
            iris1vNNotPassLogNum = null == iris1vNNotPassLogNum ? 0 : iris1vNNotPassLogNum;
            Integer irisFace1vNPassLogNum = irisFace1vNPassTradeLogMap.get(tenantId);
            irisFace1vNPassLogNum = null == irisFace1vNPassLogNum ? 0 : irisFace1vNPassLogNum;
            Integer irisFace1vNNotPassLogNum = irisFace1vNNotPassTradeLogMap.get(tenantId);
            irisFace1vNNotPassLogNum = null == irisFace1vNNotPassLogNum ? 0 : irisFace1vNNotPassLogNum;

            int passNum = face1vNPassLogNum + finger1vNPassLogNum + iris1vNPassLogNum + irisFace1vNPassLogNum;
            int notpassNum =
                face1vNNotPassLogNum + finger1vNNotPassLogNum + iris1vNNotPassLogNum + irisFace1vNNotPassLogNum;
            int tradeNum = passNum + notpassNum;
            StatisticTradeLog tradeLog = new StatisticTradeLog();
            tradeLog.setId(IdWorker.getNextStringId());
            tradeLog.setTradeNum(tradeNum);
            tradeLog.setPassNum(passNum);
            tradeLog.setNotpassNum(notpassNum);
            tradeLog.setStatisticUnit(StatisticConstants.StatisticUnit.DAY);
            tradeLog.setStatisticDate(statisticDate);
            tradeLog.setTenantId(tenantId);
            tradeLogStatisticMapper.insertStatisticTradeLog(tradeLog);
        }
    }

}
