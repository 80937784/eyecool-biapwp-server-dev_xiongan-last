package cn.eyecool.statistic.controller;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Maps;

import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.entity.SysDictData;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.device.constant.RedisKeyConstants;
import cn.eyecool.device.domain.DeviceInfo;
import cn.eyecool.statistic.domain.DeviceLogNumInfo;
import cn.eyecool.statistic.domain.StatisticDevice;
import cn.eyecool.statistic.service.IDeviceStatisticService;
import cn.eyecool.system.service.ISysDictDataService;

/**
 * 设备信息统计请求处理
 * 
 * @author mawj
 * @date 2021/08/26
 */
@RestController
@RequestMapping("/statistic/device")
public class DeviceStatisticController extends BaseController {

    @Autowired
    private RedisCache redisCache;
    @Autowired
    private TenantProperties tenantProperties;
    @Autowired
    private IDeviceStatisticService deviceStatisticService;
    @Autowired
    private ISysDictDataService dictDataService;

    /**
     * 统计设备数量
     * 
     * @return
     */
    @GetMapping("/countAll")
    public AjaxResult countAllDevice() {
        int count = deviceStatisticService.countDevice();
        return AjaxResult.success(count);
    }

    /**
     * 查询今日设备数量信息
     * 
     * @return
     */
    @GetMapping("/countToday")
    public AjaxResult countTodayDevice() {
        int count = deviceStatisticService.countDevice();
        Collection<String> keys = redisCache.keys(RedisKeyConstants.ONLINE_DEVICE_KEY_PREFIX + "*");
        int onlineCount = 0;
        boolean tenantEnabled = Boolean.TRUE.equals(tenantProperties.getEnabled());
        for (String key : keys) {
            DeviceInfo device = (DeviceInfo)redisCache.getCacheObject(key);
            if (tenantEnabled && TenantContextHolder.getTenantId().equals(device.getTenantId()) || !tenantEnabled) {
                onlineCount++;
            }
        }
        Map<String, Object> result = Maps.newHashMap();
        result.put("count", count);
        result.put("onlineCount", onlineCount);
        result.put("offlineCount", count - onlineCount);
        return AjaxResult.success(result);
    }

    /**
     * 查询今日日志数量信息
     * 
     * @return
     */
    @GetMapping("/todayLogNumInfo")
    public AjaxResult getTodayDeviceLogNumInfo() {
        List<DeviceLogNumInfo> list = deviceStatisticService.getTodaySortedDeviceLogNum();
        return AjaxResult.success(list);
    }

    /**
     * 在线设备型号分析
     * 
     * @return
     */
    @GetMapping("/onlineModelAnalysis")
    public AjaxResult onlineDeviceModelAnalysis() {
        Collection<String> keys = redisCache.keys(RedisKeyConstants.ONLINE_DEVICE_KEY_PREFIX + "*");
        Map<String, Integer> result = Maps.newHashMap();
        boolean tenantEnabled = Boolean.TRUE.equals(tenantProperties.getEnabled());
        for (String key : keys) {
            DeviceInfo device = (DeviceInfo)redisCache.getCacheObject(key);
            if (tenantEnabled && !TenantContextHolder.getTenantId().equals(device.getTenantId())) {
                continue;
            }
            String modelCode = device.getDeviceModelCode();
            if (StringUtils.isBlank(modelCode)) {
                continue;
            }
            Integer num = result.get(modelCode) == null ? 0 : result.get(modelCode);
            result.put(modelCode, ++num);
        }
        // 补充一下没有的设备型号
        if (!result.keySet().contains("ECF201")) {
            result.put("ECF201", 0);
        }
        if (!result.keySet().contains("ECF203")) {
            result.put("ECF203", 0);
        }
        if (!result.keySet().contains("ECX332")) {
            result.put("ECX332", 0);
        }
        if (!result.keySet().contains("ECX333")) {
            result.put("ECX333", 0);
        }
        return AjaxResult.success(result);
    }

    /**
     * 在线设备类型分析
     * 
     * @return
     */
    @GetMapping("/onlineTypeAnalysis")
    public AjaxResult onlineDeviceTypeAnalysis() {
        Collection<String> keys = redisCache.keys(RedisKeyConstants.ONLINE_DEVICE_KEY_PREFIX + "*");
        Map<String, Integer> result = Maps.newHashMap();
        boolean tenantEnabled = Boolean.TRUE.equals(tenantProperties.getEnabled());
        for (String key : keys) {
            DeviceInfo device = (DeviceInfo)redisCache.getCacheObject(key);
            if (tenantEnabled && !TenantContextHolder.getTenantId().equals(device.getTenantId())) {
                continue;
            }
            String deviceType = device.getDeviceType();
            if (StringUtils.isBlank(deviceType)) {
                continue;
            }
            Integer num = result.get(deviceType) == null ? 0 : result.get(deviceType);
            result.put(deviceType, ++num);
        }
        SysDictData dictDataCondition = new SysDictData();
        dictDataCondition.setDictType(DictConstants.DEVICE_TYPE_DICT_TYPE);
        List<SysDictData> dataList = dictDataService.selectDictDataList(dictDataCondition);
        if (CollectionUtils.isNotEmpty(dataList)) {
            Set<String> keySet = result.keySet();
            dataList.stream().forEach(it -> {
                String dictValue = it.getDictValue();
                if (!keySet.contains(dictValue)) {
                    result.put(dictValue, 0);
                }
            });
        }
        return AjaxResult.success(result);
    }

    /**
     * 查询设备数量变化趋势信息
     * 
     * @return
     */
    @GetMapping("/numTrend")
    public AjaxResult getDeviceNumTrend(String startDate, String endDate) {
        List<StatisticDevice> list = deviceStatisticService.getDeviceNumTrend(startDate, endDate);
        return AjaxResult.success(list);
    }
}
