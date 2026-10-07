package cn.eyecool.tradelog.task;

import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.tradelog.service.IXAPoliceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zfx
 * @ClassName PoliceTask
 * @description policeTask
 * @since 2026/9/29 15:28
 **/
@Component("policeTask")
@Slf4j
public class PoliceTask {
    @Autowired
    private IXAPoliceService ixaPoliceService;

    /**
     * 设备注册缓存 key:deviceId
     */
    private static final Map<String, Long> deviceCacheMap = new ConcurrentHashMap<>();

    public void keepAlive() {
        log.info("PoliceTask 调用公安保活接口[执行了没]");
        String policeUrl = ixaPoliceService.getPoliceHeadUrl();
        if (StringUtils.isEmpty(policeUrl)) {
            return;
        }
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : deviceCacheMap.entrySet()) {
            String deviceId = entry.getKey();
            Long lastTs = entry.getValue();
            if (lastTs == null) {
                continue;
            }
            long diffSeconds = (now - lastTs) / 1000L;
            //小于10秒跳过保活
            if (diffSeconds < 10) {
                log.debug("PoliceTask 距离上次上报不足10s，跳过keepalive deviceId:{},diff:{}s", deviceId, diffSeconds);
                continue;
            }
            try {
                AjaxResult ajaxResult = ixaPoliceService.keepaliveToPolice(policeUrl, deviceId);
                if (ajaxResult.isSuccess()) {
                    deviceCacheMap.put(deviceId, now);
                    log.debug("PoliceTask keepalive保活成功 deviceId:{}", deviceId);
                } else {
                    log.warn("PoliceTask keepalive保活失败 deviceId:{} msg:{}", deviceId, ajaxResult.getMsg());
                }
            } catch (Exception e) {
                log.error("PoliceTask keepalive调用异常 deviceId:{}", deviceId, e);
            }
        }
    }
}
