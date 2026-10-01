package cn.eyecool.device.event.listener;

import java.util.Base64;
import java.util.concurrent.TimeUnit;

import javax.websocket.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.utils.AESUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.device.constant.AdapterConstants;
import cn.eyecool.device.constant.AdapterConstants.OperMethod;
import cn.eyecool.device.controller.WebsocketController;
import cn.eyecool.device.event.ECF203UpgradeTaskEventAttrs;
import cn.eyecool.device.event.EFC203UpgradeTaskEvent;
import cn.eyecool.device.vo.UpgradeTaskInfoVO;
import lombok.extern.slf4j.Slf4j;

/**
 * 203设备升级任务事件监听类
 * 
 * @author mawj
 * @date 2021/01/06
 */
@Component
@Slf4j
public class ECF203UpgradeTaskEventListener {

    @Value("${eyecool.serverDomain}")
    private String serverDomain;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    private RedisCache redisCache;
    @Value("${device.ecf203.upgradeLink.expireTime:30}")
    private int upgrade203LinkExpireTime;

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    public void processECF203UpgradeTaskEvent(EFC203UpgradeTaskEvent event) {
        ECF203UpgradeTaskEventAttrs eventAttrs = event.getEventAttrs();
        log.info("203 Device upgrade task release event monitoring and processing,eventAttrs:[{}]", eventAttrs.toString());
        Session session = WebsocketController.getSessionPool().get(eventAttrs.getDeviceNo());
        if (null == session || !session.isOpen()) {
            log.warn("203 The device [{}] has no online access or the session is closed, and the online upgrade of the device cannot be performed", eventAttrs.getDeviceNo());
            event.getCallback().onError(MessageUtils.message("device.203.cannot.upgrade.online",  eventAttrs.getDeviceNo()));
            return;
        }
        try {
            UpgradeTaskInfoVO upgradeTaskVO = new UpgradeTaskInfoVO();
            upgradeTaskVO.setId(IdWorker.getNextLongId());
            upgradeTaskVO.setMethod(OperMethod.UPGRADE);
            JSONObject params = new JSONObject();
            // 生成随机链接
            String encryptFlag = AESUtils
                .encryptAES(eventAttrs.getTaskId() + ":" + eventAttrs.getDeviceNo() + ":" + IdWorker.getNextStringId());
            String base64EncryptFlag = new String(Base64.getEncoder().encode(encryptFlag.getBytes()));
            // 下载接口地址
            params.put("URL", serverDomain + contextPath + "/api/device/version/download/" + base64EncryptFlag);
            // 文件大小
            params.put("FirmwareSize", eventAttrs.getFileSize());
            // 版本号
            params.put("HardwareVersion", eventAttrs.getVersion());
            upgradeTaskVO.setParams(params);
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            String str = mapper.writeValueAsString(upgradeTaskVO);
            session.getAsyncRemote().sendText(str);
            // 缓存任务,默认30分钟有效时间
            redisCache.setCacheObject(AdapterConstants.DEVICE_UPGRADE_TASK_CACHE_PREFIX + base64EncryptFlag, eventAttrs,
                upgrade203LinkExpireTime, TimeUnit.MINUTES);
            // 发布成功回调
            event.getCallback().onSuccess();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // 发布失败回调
            event.getCallback().onError(e.getMessage());
        }
    }

}
