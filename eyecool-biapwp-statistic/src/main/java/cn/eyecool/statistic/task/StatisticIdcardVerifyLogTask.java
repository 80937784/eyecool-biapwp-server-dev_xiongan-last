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
import cn.eyecool.statistic.domain.StatisticIdcardVerifyLog;
import cn.eyecool.statistic.domain.TenantTradeLogCount;
import cn.eyecool.statistic.mapper.TradeLogStatisticMapper;
import cn.eyecool.system.service.ISysTenantService;
import lombok.extern.slf4j.Slf4j;

/**
 * 统计身份证比对日志定时任务
 * 
 * @author mawj
 * @date 2021/08/31
 */
@Component("statisticIdcardVerifyLogTask")
@Slf4j
public class StatisticIdcardVerifyLogTask {

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
            StatisticIdcardVerifyLog statisticTradeLog = tradeLogStatisticMapper
                .getStatistictIdcardVerifyLogByDate(statisticDate, StatisticConstants.StatisticUnit.DAY, null);
            if (null != statisticTradeLog) {
                log.info("ID card comparison statistics already exist, statisticDate:[{}]", statisticDate);
                return;
            }
        }
        List<TenantTradeLogCount> face1v1PassTradeLog = tradeLogStatisticMapper
            .countFace1v1TradeLogByTenantId(yestodayStartTime, todayStartTime, DictConstants.BioResult.PASS);
        List<TenantTradeLogCount> face1v1NotPassTradeLog = tradeLogStatisticMapper
            .countFace1v1TradeLogByTenantId(yestodayStartTime, todayStartTime, DictConstants.BioResult.NOTPASS);
        List<TenantTradeLogCount> face1vNPassTradeLog = tradeLogStatisticMapper.countFace1vNTradeLogByTenantId(
            yestodayStartTime, todayStartTime, DictConstants.BioResult.PASS, DictConstants.PassValidType.ID_CARD);
        List<TenantTradeLogCount> face1vNNotPassTradeLog = tradeLogStatisticMapper.countFace1vNTradeLogByTenantId(
            yestodayStartTime, todayStartTime, DictConstants.BioResult.NOTPASS, DictConstants.PassValidType.ID_CARD);
        if (!tenantEnabled) {
            // 未开启租户情况下统计
            int passNum = 0;
            int notpassNum = 0;
            if (CollectionUtils.isNotEmpty(face1v1PassTradeLog)) {
                passNum += face1v1PassTradeLog.get(0).getTradeNum();
            }
            if (CollectionUtils.isNotEmpty(face1vNPassTradeLog)) {
                passNum += face1vNPassTradeLog.get(0).getTradeNum();
            }
            if (CollectionUtils.isNotEmpty(face1v1NotPassTradeLog)) {
                notpassNum += face1v1NotPassTradeLog.get(0).getTradeNum();
            }
            if (CollectionUtils.isNotEmpty(face1vNNotPassTradeLog)) {
                notpassNum += face1vNNotPassTradeLog.get(0).getTradeNum();
            }
            int tradeNum = passNum + notpassNum;
            StatisticIdcardVerifyLog log = new StatisticIdcardVerifyLog();
            log.setId(IdWorker.getNextStringId());
            log.setTradeNum(tradeNum);
            log.setPassNum(passNum);
            log.setNotpassNum(notpassNum);
            log.setStatisticUnit(StatisticConstants.StatisticUnit.DAY);
            log.setStatisticDate(statisticDate);
            tradeLogStatisticMapper.insertStatisticIdcardVerifyLog(log);
            return;
        }
        // 多租户模式下统计
        Map<String, Integer> face1v1PassTradeLogMap = face1v1PassTradeLog.stream()
            .collect(Collectors.toMap(TenantTradeLogCount::getTenantId, TenantTradeLogCount::getTradeNum));
        Map<String, Integer> face1v1NotPassTradeLogMap = face1v1NotPassTradeLog.stream()
            .collect(Collectors.toMap(TenantTradeLogCount::getTenantId, TenantTradeLogCount::getTradeNum));
        Map<String, Integer> face1vNPassTradeLogMap = face1vNPassTradeLog.stream()
            .collect(Collectors.toMap(TenantTradeLogCount::getTenantId, TenantTradeLogCount::getTradeNum));
        Map<String, Integer> face1vNNotPassTradeLogMap = face1vNNotPassTradeLog.stream()
            .collect(Collectors.toMap(TenantTradeLogCount::getTenantId, TenantTradeLogCount::getTradeNum));
        SysTenant tenantCondition = new SysTenant();
        tenantCondition.setTenantState(DictConstants.TenantState.NORMAL);
        List<SysTenant> tenantList = sysTenantService.selectSysTenantList(tenantCondition);
        for (SysTenant tenant : tenantList) {
            String tenantId = tenant.getTenantId();
            StatisticIdcardVerifyLog statisticTradeLog = tradeLogStatisticMapper
                .getStatistictIdcardVerifyLogByDate(statisticDate, StatisticConstants.StatisticUnit.DAY, tenantId);
            if (null != statisticTradeLog) {
                log.info("ID card comparison statistics already exist,tenantId:[{}], statisticDate:[{}]", tenantId, statisticDate);
                continue;
            }
            Integer face1v1PassLogNum = face1v1PassTradeLogMap.get(tenantId);
            face1v1PassLogNum = null == face1v1PassLogNum ? 0 : face1v1PassLogNum;
            Integer face1v1NotPassLogNum = face1v1NotPassTradeLogMap.get(tenantId);
            face1v1NotPassLogNum = null == face1v1NotPassLogNum ? 0 : face1v1NotPassLogNum;
            Integer face1vNPassLogNum = face1vNPassTradeLogMap.get(tenantId);
            face1vNPassLogNum = null == face1vNPassLogNum ? 0 : face1vNPassLogNum;
            Integer face1vNNotPassLogNum = face1vNNotPassTradeLogMap.get(tenantId);
            face1vNNotPassLogNum = null == face1vNNotPassLogNum ? 0 : face1vNNotPassLogNum;
            int passNum = face1v1PassLogNum + face1vNPassLogNum;
            int notpassNum = face1v1NotPassLogNum + face1vNNotPassLogNum;
            int tradeNum = passNum + notpassNum;
            StatisticIdcardVerifyLog tradeLog = new StatisticIdcardVerifyLog();
            tradeLog.setId(IdWorker.getNextStringId());
            tradeLog.setTradeNum(tradeNum);
            tradeLog.setPassNum(passNum);
            tradeLog.setNotpassNum(notpassNum);
            tradeLog.setStatisticUnit(StatisticConstants.StatisticUnit.DAY);
            tradeLog.setStatisticDate(statisticDate);
            tradeLog.setTenantId(tenantId);
            tradeLogStatisticMapper.insertStatisticIdcardVerifyLog(tradeLog);
        }
    }

}
