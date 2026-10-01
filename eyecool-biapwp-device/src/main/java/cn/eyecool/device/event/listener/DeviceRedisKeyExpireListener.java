package cn.eyecool.device.event.listener;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.device.constant.RedisKeyConstants;
import cn.eyecool.device.domain.DeviceActionLog;
import cn.eyecool.device.domain.DeviceInfo;
import cn.eyecool.device.service.IDeviceActionLogService;
import cn.eyecool.device.service.IDeviceInfoService;
import lombok.extern.slf4j.Slf4j;

/**
 * 设备相关redis-key过期监听
 * 
 * @author mawj
 * @date 2021/06/01
 */
@Slf4j
@Component
public class DeviceRedisKeyExpireListener extends KeyExpirationEventMessageListener {

    @Autowired
    private IDeviceActionLogService deviceActionLogService;
    @Autowired
    private IDeviceInfoService deviceInfoService;

    public DeviceRedisKeyExpireListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.info("redis expiredKey=========" + expiredKey);
        if (!expiredKey.startsWith(RedisKeyConstants.ONLINE_DEVICE_KEY_PREFIX)) {
            return;
        }
        // 修改设备在线状态
        String deviceNo =
            expiredKey.substring(RedisKeyConstants.ONLINE_DEVICE_KEY_PREFIX.length() + 1, expiredKey.length());
        log.info("device [{}] is offline", deviceNo);
        // 记录设备下线日志
        CompletableFuture.runAsync(() -> {
            try {
                DeviceInfo condition = new DeviceInfo();
                condition.setDeviceNo(deviceNo);
                List<DeviceInfo> deviceInfoList = deviceInfoService.selectDeviceInfoList(condition);
                if (CollectionUtils.isEmpty(deviceInfoList)) {
                    log.warn("offline device [{}] is not exists", deviceNo);
                    return;
                }
                DeviceInfo deviceInfo = deviceInfoList.get(0);
                TenantContextHolder.setTenantId(deviceInfo.getTenantId());
                String actionType = DictConstants.DeviceActionLogType.OFFLINE;
                log.info("Record equipment offline action log, deviceNo:[{}], actionType：[{}]", deviceNo, actionType);
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