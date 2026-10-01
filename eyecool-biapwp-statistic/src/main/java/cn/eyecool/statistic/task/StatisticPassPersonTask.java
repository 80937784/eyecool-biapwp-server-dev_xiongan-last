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
import cn.eyecool.statistic.domain.StatisticTradePerson;
import cn.eyecool.statistic.domain.TenantPassPersonCount;
import cn.eyecool.statistic.mapper.PersonStatisticMapper;
import cn.eyecool.statistic.mapper.TradeLogStatisticMapper;
import cn.eyecool.system.service.ISysTenantService;
import lombok.extern.slf4j.Slf4j;

/**
 * 统计通行人员定时任务
 * 
 * @author mawj
 * @date 2021/08/31
 */
@Component("statisticPassPersonTask")
@Slf4j
public class StatisticPassPersonTask {

    @Autowired
    private PersonStatisticMapper personStatisticMapper;
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
            StatisticTradePerson statisticPerson = personStatisticMapper.getStatisticPersonByDate(statisticDate,
                StatisticConstants.StatisticUnit.DAY, null);
            if (null != statisticPerson) {
                log.info("People statistics already exist, statisticDate:[{}]", statisticDate);
                return;
            }
        }
        List<TenantPassPersonCount> face1vNPassPerson =
            tradeLogStatisticMapper.countFace1vNPassPersonByTenantId(yestodayStartTime, todayStartTime);
        List<TenantPassPersonCount> finger1vNPassPerson =
            tradeLogStatisticMapper.countFinger1vNPassPersonByTenantId(yestodayStartTime, todayStartTime);
        List<TenantPassPersonCount> iris1vNPassPerson =
            tradeLogStatisticMapper.countIris1vNPassPersonByTenantId(yestodayStartTime, todayStartTime);
        List<TenantPassPersonCount> irisFace1vNPassPerson =
            tradeLogStatisticMapper.countIrisFace1vNPassPersonByTenantId(yestodayStartTime, todayStartTime);
        if (!tenantEnabled) {
            // 未开启租户情况下统计
            int passNum = 0;
            if (CollectionUtils.isNotEmpty(face1vNPassPerson)) {
                passNum += face1vNPassPerson.get(0).getPassNum();
            }
            if (CollectionUtils.isNotEmpty(finger1vNPassPerson)) {
                passNum += finger1vNPassPerson.get(0).getPassNum();
            }
            if (CollectionUtils.isNotEmpty(iris1vNPassPerson)) {
                passNum += iris1vNPassPerson.get(0).getPassNum();
            }
            if (CollectionUtils.isNotEmpty(irisFace1vNPassPerson)) {
                passNum += irisFace1vNPassPerson.get(0).getPassNum();
            }
            StatisticTradePerson tradePerson = new StatisticTradePerson();
            tradePerson.setId(IdWorker.getNextStringId());
            tradePerson.setPassNum(passNum);
            tradePerson.setStatisticUnit(StatisticConstants.StatisticUnit.DAY);
            tradePerson.setStatisticDate(statisticDate);
            personStatisticMapper.insertStatisticTradePerson(tradePerson);
            return;
        }
        // 多租户模式下统计
        Map<String, Integer> face1vNPassPersonMap = face1vNPassPerson.stream()
            .collect(Collectors.toMap(TenantPassPersonCount::getTenantId, TenantPassPersonCount::getPassNum));
        Map<String, Integer> finger1vNPassPersonMap = finger1vNPassPerson.stream()
            .collect(Collectors.toMap(TenantPassPersonCount::getTenantId, TenantPassPersonCount::getPassNum));
        Map<String, Integer> iris1vNPassPersonMap = iris1vNPassPerson.stream()
            .collect(Collectors.toMap(TenantPassPersonCount::getTenantId, TenantPassPersonCount::getPassNum));
        Map<String, Integer> irisFace1vNPassPersonMap = irisFace1vNPassPerson.stream()
            .collect(Collectors.toMap(TenantPassPersonCount::getTenantId, TenantPassPersonCount::getPassNum));
        SysTenant tenantCondition = new SysTenant();
        tenantCondition.setTenantState(DictConstants.TenantState.NORMAL);
        List<SysTenant> tenantList = sysTenantService.selectSysTenantList(tenantCondition);
        for (SysTenant tenant : tenantList) {
            String tenantId = tenant.getTenantId();
            StatisticTradePerson statisticPerson = personStatisticMapper.getStatisticPersonByDate(statisticDate,
                StatisticConstants.StatisticUnit.DAY, tenantId);
            if (null != statisticPerson) {
                log.info("People statistics already exist,tenantId:[{}], statisticDate:[{}]", tenantId, statisticDate);
                continue;
            }
            Integer face1vNPassPersonNum = face1vNPassPersonMap.get(tenantId);
            face1vNPassPersonNum = null == face1vNPassPersonNum ? 0 : face1vNPassPersonNum;
            Integer finger1vNPassPersonNum = finger1vNPassPersonMap.get(tenantId);
            finger1vNPassPersonNum = null == finger1vNPassPersonNum ? 0 : finger1vNPassPersonNum;
            Integer iris1vNPassPersonNum = iris1vNPassPersonMap.get(tenantId);
            iris1vNPassPersonNum = null == iris1vNPassPersonNum ? 0 : iris1vNPassPersonNum;
            Integer irisFace1vNPassPersonNum = irisFace1vNPassPersonMap.get(tenantId);
            irisFace1vNPassPersonNum = null == irisFace1vNPassPersonNum ? 0 : irisFace1vNPassPersonNum;
            int passNum =
                face1vNPassPersonNum + finger1vNPassPersonNum + iris1vNPassPersonNum + irisFace1vNPassPersonNum;
            StatisticTradePerson tradePerson = new StatisticTradePerson();
            tradePerson.setId(IdWorker.getNextStringId());
            tradePerson.setPassNum(passNum);
            tradePerson.setStatisticUnit(StatisticConstants.StatisticUnit.DAY);
            tradePerson.setTenantId(tenantId);
            tradePerson.setStatisticDate(statisticDate);
            personStatisticMapper.insertStatisticTradePerson(tradePerson);
        }
    }

}
