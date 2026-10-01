package cn.eyecool.statistic.task;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.domain.entity.SysTenant;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.device.constant.RedisKeyConstants;
import cn.eyecool.device.domain.DeviceInfo;
import cn.eyecool.statistic.domain.StatisticDevice;
import cn.eyecool.statistic.domain.TenantDeviceCount;
import cn.eyecool.statistic.mapper.DeviceStatisticMapper;
import cn.eyecool.system.service.ISysTenantService;
import lombok.extern.slf4j.Slf4j;

/**
 * 设备数量信息统计定时任务
 * 
 * @author mawj
 * @date 2021/09/01
 */
@Component("statisticDeviceTask")
@Slf4j
public class StatisticDeviceTask {

    @Autowired
    private ISysTenantService sysTenantService;
    @Autowired
    private TenantProperties tenantProperties;
    @Autowired
    private DeviceStatisticMapper deviceStatisticMapper;
    @Autowired
    private RedisCache redisCache;

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
        String statisticDate = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, todayStartTime);
        if (!tenantEnabled) {
            // 未开启租户情况下，判断是否已经统计过，统计过则不再重复统计
            StatisticDevice statisticDevice = deviceStatisticMapper.getStatisticDeviceByDate(statisticDate, null);
            if (null != statisticDevice) {
                log.info("Device stats already exist, statisticDate:[{}]", statisticDate);
                return;
            }
        }
        Collection<String> keys = redisCache.keys(RedisKeyConstants.ONLINE_DEVICE_KEY_PREFIX + "*");
        if (!tenantEnabled) {
            // 未开启租户情况下统计
            int deviceNum = deviceStatisticMapper.countDevice();
            int onlineNum = keys.size();
            int offlineNum = deviceNum - onlineNum;
            StatisticDevice statisticDevice = new StatisticDevice();
            statisticDevice.setId(IdWorker.getNextStringId());
            statisticDevice.setDeviceNum(deviceNum);
            statisticDevice.setOnlineNum(onlineNum);
            statisticDevice.setOfflineNum(offlineNum);
            statisticDevice.setStatisticDate(statisticDate);
            deviceStatisticMapper.insertStatisticDevice(statisticDevice);
            return;
        }
        // 多租户模式下统计
        List<TenantDeviceCount> deviceCounts = deviceStatisticMapper.countDeviceByTenantId();
        Map<String, Integer> deviceCountMap = deviceCounts.stream()
            .collect(Collectors.toMap(TenantDeviceCount::getTenantId, TenantDeviceCount::getDeviceNum));
        SysTenant tenantCondition = new SysTenant();
        tenantCondition.setTenantState(DictConstants.TenantState.NORMAL);
        List<SysTenant> tenantList = sysTenantService.selectSysTenantList(tenantCondition);
        for (SysTenant tenant : tenantList) {
            String tenantId = tenant.getTenantId();
            StatisticDevice statisticInfo = deviceStatisticMapper.getStatisticDeviceByDate(statisticDate, tenantId);
            if (null != statisticInfo) {
                log.info("Device stats already exist,tenantId:[{}], statisticDate:[{}]", tenantId, statisticDate);
                continue;
            }
            Integer deviceNum = deviceCountMap.get(tenantId);
            deviceNum = null == deviceNum ? 0 : deviceNum;
            int onlineNum = 0;
            for (String key : keys) {
                DeviceInfo device = (DeviceInfo)redisCache.getCacheObject(key);
                if (tenantId.equals(device.getTenantId())) {
                    onlineNum++;
                }
            }
            int offlineNum = deviceNum - onlineNum;
            StatisticDevice statisticDevice = new StatisticDevice();
            statisticDevice.setId(IdWorker.getNextStringId());
            statisticDevice.setDeviceNum(deviceNum);
            statisticDevice.setOnlineNum(onlineNum);
            statisticDevice.setOfflineNum(offlineNum);
            statisticDevice.setStatisticDate(statisticDate);
            statisticDevice.setTenantId(tenantId);
            deviceStatisticMapper.insertStatisticDevice(statisticDevice);
        }
    }
}
