package cn.eyecool.statistic.controller;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Maps;

import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.AESUtils;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.device.constant.RedisKeyConstants;
import cn.eyecool.device.domain.DeviceInfo;
import cn.eyecool.statistic.constant.StatisticConstants;
import cn.eyecool.statistic.domain.AreaLogNumInfo;
import cn.eyecool.statistic.domain.DeviceLogNumInfo;
import cn.eyecool.statistic.domain.StatisticDevice;
import cn.eyecool.statistic.domain.StatisticFaceRecogLog;
import cn.eyecool.statistic.domain.StatisticIdcardVerifyLog;
import cn.eyecool.statistic.domain.StatisticTradePerson;
import cn.eyecool.statistic.service.IAreaStatisticService;
import cn.eyecool.statistic.service.IDeviceStatisticService;
import cn.eyecool.statistic.service.IPersonStatisticService;
import cn.eyecool.statistic.service.ITradeLogStatisticService;
import cn.eyecool.tradelog.domain.FaceRealtimeTradeLog;
import lombok.extern.slf4j.Slf4j;

/**
 * 大屏统计请求
 * 
 * @author mawj
 * @date 2021/09/23
 */
@Slf4j
@RestController
@RequestMapping("/api/bigscreen/v1")
public class BigScreenStatisticController {

    @Autowired
    private ITradeLogStatisticService tradeLogStatisticService;
    @Autowired
    private IPersonStatisticService personStatisticService;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private TenantProperties tenantProperties;
    @Autowired
    private IDeviceStatisticService deviceStatisticService;
    @Autowired
    private IAreaStatisticService areaStatisticService;

    /**
     * 查询通行总量(只有人脸)
     * 
     * @return
     */
    @GetMapping("countAllTradeLog")
    public AjaxResult countAllTradeLog(HttpServletRequest request) {
        checkToken(request);
        int countAllFaceTradeLog = tradeLogStatisticService.countAllFaceTradeLog(DictConstants.BioResult.PASS);
        return AjaxResult.success(countAllFaceTradeLog);
    }

    /**
     * 查询今日通行总量
     * 
     * @return
     */
    @GetMapping("countTodayTradeLog")
    public AjaxResult countTodayTradeLog(HttpServletRequest request) {
        checkToken(request);
        Map<String, Integer> map = tradeLogStatisticService.countTodayFaceTradeLog(DictConstants.BioResult.PASS);
        return AjaxResult.success(map);
    }

    /**
     * 根据时间单位查询通行人员
     * 
     * @return
     */
    @GetMapping("countPassPerson")
    public AjaxResult countPassPersonByUnit(HttpServletRequest request) {
        checkToken(request);
        Map<String, Integer> map = personStatisticService.countFacePassPersonByUnit();
        return AjaxResult.success(map);
    }

    /**
     * 查询今日设备数量信息
     * 
     * @return
     */
    @GetMapping("/countTodayDevice")
    public AjaxResult countTodayDevice(HttpServletRequest request) {
        checkToken(request);
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
     * 查询今日日志量排名前5的设备信息
     * 
     * @return
     */
    @GetMapping("/todayTop5DeviceLogNum")
    public AjaxResult todayTop5DeviceLogNum(HttpServletRequest request) {
        checkToken(request);
        List<DeviceLogNumInfo> list = deviceStatisticService.getTodaySortedDeviceFaceLogNum();
        return AjaxResult.success(list);
    }

    /**
     * 查询今日日志量排名前5的区域信息
     * 
     * @return
     */
    @GetMapping("/todayTop5AreaLogNum")
    public AjaxResult todayTop5AreaLogNum(HttpServletRequest request) {
        checkToken(request);
        List<AreaLogNumInfo> list = areaStatisticService.todayTop5AreaFaceLogNum();
        return AjaxResult.success(list);
    }

    /**
     * 在线设备型号分析
     * 
     * @return
     */
    @GetMapping("/onlineDevModelAnalysis")
    public AjaxResult onlineDeviceModelAnalysis(HttpServletRequest request) {
        checkToken(request);
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
        return AjaxResult.success(result);
    }

    /**
     * 查询通行人员[日|月]数量趋势
     * 
     * @param statisticUnit
     * @param startDate
     * @param endDate
     * @return
     */
    @GetMapping("/passPersonTrend")
    public AjaxResult getTradePersonTrend(String statisticUnit, String startDate, String endDate,
        HttpServletRequest request) {
        checkToken(request);
        List<StatisticTradePerson> list = personStatisticService.getTradePersonTrend(statisticUnit, startDate, endDate);
        return AjaxResult.success(list);
    }

    /**
     * 查询设备数量变化趋势信息
     * 
     * @param startDate
     * @param endDate
     * @return
     */
    @GetMapping("/deviceNumTrend")
    public AjaxResult getDeviceNumTrend(String startDate, String endDate, HttpServletRequest request) {
        checkToken(request);
        List<StatisticDevice> list = deviceStatisticService.getDeviceNumTrend(startDate, endDate);
        return AjaxResult.success(list);
    }

    /**
     * 查询人脸识别日志[日|月]数量趋势
     * 
     * @param statisticUnit
     * @param startDate
     * @param endDate
     * @return
     */
    @GetMapping("/faceRecogLogTrend")
    public AjaxResult getFaceRecogLogTrend(String statisticUnit, String startDate, String endDate,
        HttpServletRequest request) {
        checkToken(request);
        List<StatisticFaceRecogLog> list =
            tradeLogStatisticService.getFaceRecogLogTrend(statisticUnit, startDate, endDate);
        return AjaxResult.success(list);
    }

    /**
     * 查询人脸识别日志[日|月]数量趋势
     * 
     * @param statisticUnit
     * @param startDate
     * @param endDate
     * @return
     */
    @GetMapping("/idcardVerifyLogTrend")
    public AjaxResult getIdcardVerifyLogTrend(String statisticUnit, String startDate, String endDate,
        HttpServletRequest request) {
        checkToken(request);
        List<StatisticIdcardVerifyLog> list =
            tradeLogStatisticService.getIdcardVerifyLogTrend(statisticUnit, startDate, endDate);
        return AjaxResult.success(list);
    }

    /**
     * 查询今日日志实时交易
     * 
     * @return
     */
    @GetMapping("/realtimeTrade")
    public AjaxResult realtimeTrade(HttpServletRequest request) {
        checkToken(request);
        List<FaceRealtimeTradeLog> realtimeTradeLog = tradeLogStatisticService.getFaceRealtimeTradeLog();
        realtimeTradeLog.stream().forEach(it -> {
            it.setHealthCodeState(transHealthCode(it.getHealthMessage()));
            it.setHealthMessage(handleHealthMessage(it.getHealthMessage()));
        });
        return AjaxResult.success(realtimeTradeLog);
    }

    /**
     * 查询今日日志异常实时交易
     * 
     * @return
     */
    @GetMapping("/abnormalRealtimeTrade")
    public AjaxResult abnormalRealtimeTrade(HttpServletRequest request) {
        checkToken(request);
        List<FaceRealtimeTradeLog> realtimeTradeLog = tradeLogStatisticService.getFaceAbnormalRealtimeTrade();
        realtimeTradeLog.stream().forEach(it -> {
            it.setHealthCodeState(transHealthCode(it.getHealthMessage()));
            it.setHealthMessage(handleHealthMessage(it.getHealthMessage()));
        });
        return AjaxResult.success(realtimeTradeLog);
    }

    /**
     * 校验token
     * 
     * @param request
     */
    private void checkToken(HttpServletRequest request) {
        String token = request.getHeader("bigscreen-token");
        if (StringUtils.isEmpty(token)) {
            throw new CustomException(MessageUtils.message("big.screen.stat.user.not.login"), 403);
        }
        // 验证连接合法性
        try {
            String tokenStr = AESUtils.decryptAES(new String(Base64.getDecoder().decode(token)));
            String tenantId = tokenStr.split(":")[0];
            List<String> cacheList =
                redisCache.getCacheList(StatisticConstants.BIGSCREEN_LOGIN_TOKEN_CACHE_PREFIX + ":" + tenantId);
            if (CollectionUtils.isEmpty(cacheList) || !cacheList.contains(token)) {
                log.error("Unknown big screen login token:[{}],tenantId:[{}]", token, tenantId);
                throw new CustomException(MessageUtils.message("big.screen.stat.user.invalid"), 403);
            }
            TenantContextHolder.setTenantId(tenantId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new CustomException(MessageUtils.message("big.screen.stat.user.invalid"), 403);
        }
    }

    /**
     * 处理健康码信息
     * 
     * @param message
     * @return
     */
    private String handleHealthMessage(String message) {
        boolean contains = Arrays.asList(MessageUtils.message("big.screen.stat.healthcode.blue"), MessageUtils.message("big.screen.stat.healthcode.yellow"), MessageUtils.message("big.screen.stat.healthcode.red")).contains(message);
        if (contains)
            return message;
        return MessageUtils.message("big.screen.stat.get.exception");
    }

    /**
     * 健康码状态转换
     * 
     * @param message
     * @return
     */
    private String transHealthCode(String message) {
        String code = null;
        if (StringUtils.isEmpty(message)) {
            return code;
        }
        switch (message) {
            case "绿码":
                code = "0";
                break;
            case "黄码":
                code = "1";
                break;
            case "红码":
                code = "2";
                break;
            default:
                code = "-1";
                break;
        }
        return code;
    }
}
