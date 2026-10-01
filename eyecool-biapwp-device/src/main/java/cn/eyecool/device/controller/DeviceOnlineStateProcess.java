package cn.eyecool.device.controller;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.device.constant.RedisKeyConstants;
import cn.eyecool.device.domain.DeviceActionLog;
import cn.eyecool.device.domain.DeviceInfo;
import cn.eyecool.device.service.IDeviceActionLogService;
import cn.eyecool.device.service.IDeviceInfoService;
import lombok.extern.slf4j.Slf4j;

/**
 * 设备在线状态处理
 * 
 * @author mawj
 * @date 2021/06/01
 */
@RestController
@RequestMapping("/api/device")
@Slf4j
public class DeviceOnlineStateProcess {

    @Autowired
    private IDeviceInfoService deviceInfoService;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private IDeviceActionLogService deviceActionLogService;

    /**
     * 无感设备心跳信息接口
     * 
     * @param serialNo
     * @param request
     */
    @RequestMapping(value = "/heartbeatProcess/{serialNo}", method = {RequestMethod.GET, RequestMethod.POST})
    public void deviceHeartbeatProcessController(@PathVariable(value = "serialNo") String serialNo,
        HttpServletRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("serialNo:[{}] heartbeat get", serialNo);
        }
        Date receiveTime = DateUtils.getNowDate();
        String remoteHost = request.getRemoteHost();
        if (StringUtils.isBlank(serialNo)) {
            if (log.isDebugEnabled()) {
                log.debug("remoteHost:[{}],receiveTime:[{}],serialNo is null,return.", remoteHost,
                    DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, receiveTime));
            }
            return;
        }
        DeviceInfo deviceCondition = new DeviceInfo();
        deviceCondition.setDeviceNo(serialNo);
        List<DeviceInfo> deviceInfoList = deviceInfoService.selectDeviceInfoList(deviceCondition);
        if (CollectionUtils.isEmpty(deviceInfoList)) {
            log.error("Device [{}] does not exist, heartbeat information cannot be maintained", serialNo);
            return;
        }
        DeviceInfo deviceInfo = deviceInfoList.get(0);
        Object redisObj = redisCache.getCacheObject(RedisKeyConstants.ONLINE_DEVICE_KEY_PREFIX + serialNo);
        // 判断是否是重新上线
        boolean reOnline = null == redisObj;
        // 将在线设备缓存在redis中，并设置过期时间，同时redis开启过期事件监听，key过期即可知道设备已经离线
        redisCache.setCacheObject(RedisKeyConstants.ONLINE_DEVICE_KEY_PREFIX + serialNo, deviceInfo, 120,
            TimeUnit.SECONDS);
        if (!reOnline) {
            return;
        }
        // 记录设备上线日志
        CompletableFuture.runAsync(() -> {
            try {
                TenantContextHolder.setTenantId(deviceInfo.getTenantId());
                String actionType = DictConstants.DeviceActionLogType.ONLINE;
                log.info("Record device online action log, deviceNo:[{}], actionType：[{}]", serialNo, actionType);
                saveDeviceActionLog(deviceInfo, actionType);
            } finally {
                TenantContextHolder.clear();
            }
        });
    }

    /**
     * 保存设备动作日志
     * 
     * @param deviceInfo
     * @param actionType
     */
    private void saveDeviceActionLog(DeviceInfo deviceInfo, String actionType) {
        // 保存设备动作日志
        DeviceActionLog log = new DeviceActionLog();
        log.setDeviceNo(deviceInfo.getDeviceNo());
        log.setDeviceName(deviceInfo.getDeviceName());
        log.setActionType(actionType);
        log.setCreateTime(DateUtils.getNowDate());
        log.setModelCode(deviceInfo.getDeviceModelCode());
        log.setChannelCode(deviceInfo.getChannelCode());
        log.setId(IdWorker.getNextStringId());
        deviceActionLogService.insertDeviceActionLog(log);
    }
}
