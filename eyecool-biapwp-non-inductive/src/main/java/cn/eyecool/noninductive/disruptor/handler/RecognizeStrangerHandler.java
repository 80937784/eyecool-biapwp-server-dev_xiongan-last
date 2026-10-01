package cn.eyecool.noninductive.disruptor.handler;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.lmax.disruptor.EventHandler;

import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.device.constant.RedisKeyConstants;
import cn.eyecool.device.domain.DeviceInfo;
import cn.eyecool.device.service.IDeviceInfoService;
import cn.eyecool.noninductive.disruptor.event.RecognizeStrangerEvent;

/**
 * Created by kl on 2018/8/24. Content :进程日志事件处理器
 */
@Component
public class RecognizeStrangerHandler implements EventHandler<RecognizeStrangerEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecognizeStrangerHandler.class);
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private IDeviceInfoService deviceInfoService;

    @Override
    public void onEvent(RecognizeStrangerEvent pushEvent, long l, boolean b) {
        String deviceSn = pushEvent.getResult().getDeviceNo();
        DeviceInfo deviceInfoCondition = new DeviceInfo();
        deviceInfoCondition.setDeviceNo(deviceSn);
        List<DeviceInfo> deviceInfos = deviceInfoService.selectDeviceInfoList(deviceInfoCondition);
        if (CollectionUtils.isEmpty(deviceInfos)) {
            return;
        }
        DeviceInfo deviceInfo = deviceInfos.get(0);
        String tenantId = deviceInfo.getTenantId();
        if (StringUtils.isEmpty(tenantId)) {
            LOGGER.info("RecognizeStrangerPushEvent {} deviceNo {} tenantId is null", pushEvent,
                deviceInfo.getDeviceNo());
            return;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("RecognizeStrangerEventHandler event {},tenantId {}", pushEvent.getResult(), tenantId);
        }
        try {
            List<String> tokenCache =
                redisCache.getCacheList(RedisKeyConstants.NONINDUCTIVE_SOCKET_CACHE_KEY + ":" + tenantId);
            tokenCache.forEach(
                token -> messagingTemplate.convertAndSend("/topic/pullstrangerresult/" + token, pushEvent.getResult()));
        } catch (Exception e) {
            throw new CustomException(e.getMessage(), e);
        } finally {
            TenantContextHolder.clear();
        }
    }
}
