package cn.eyecool.tradelog.service.impl;

import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.system.service.ISysConfigService;
import cn.eyecool.tradelog.domain.police.NotifyListObject;
import cn.eyecool.tradelog.domain.police.SubscribeObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 通知推送：向下达订阅的上级平台 POST /VIID/SubscribeNotifications
 */
@Component
public class NotifyPusher {

    private static final Logger log = LoggerFactory.getLogger(NotifyPusher.class);
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String NOTIFY_PATH = "/VIID/SubscribeNotifications";


    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate rest = newRestTemplate();
    private final ExecutorService pool = Executors.newFixedThreadPool(8);

    @Autowired
    private ISysConfigService sysConfigService;
    private static RestTemplate newRestTemplate() {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(5000);
        f.setReadTimeout(10000);
        return new RestTemplate(f);
    }

    /** 异步广播：命中该资源类型的所有生效订阅都会收到通知 */
    public void broadcastAsync(List<SubscribeObject> subs, String resourceType, Map<String, Object> payload) {
        if (subs == null || subs.isEmpty()) return;
        for (SubscribeObject sub : subs) {
            pool.submit(() -> push(sub, resourceType, payload));
        }
    }

    /** 同步推送单条，返回是否成功 */
    public boolean push(SubscribeObject sub, String resourceType, Map<String, Object> payload) {
        String url = buildUrl(sub);
        if (url == null) {
            log.warn("[通知] 订阅 {} 无 ReceiveAddr，跳过", sub.SubscribeID);
            return false;
        }

        NotifyListObject notify = new NotifyListObject();
        notify.NotificationID = UUID.randomUUID().toString().replace("-", "").substring(0, 32);
        notify.SubscribeID = sub.SubscribeID;
        notify.NotificationTime = LocalDateTime.now().format(TS);
        notify.ResourceType = resourceType;
        if (payload != null) {
            for (Map.Entry<String, Object> e : payload.entrySet()) {
                notify.put(e.getKey(), e.getValue());
            }
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("NotifyListObject", notify);

        String deviceId = sysConfigService.selectConfigByKey(SysConfigConstants.XA_POLICE_DEVICE_ID);
        try {
            HttpHeaders h = new HttpHeaders();
            h.setContentType(MediaType.valueOf("application/VIID+JSON"));
            h.set("User-Identify", deviceId);
            String json = mapper.writeValueAsString(body);
            log.info("[通知] -> {} 订阅={} 大小={}B", url, sub.SubscribeID, json.length());

            HttpEntity<String> req = new HttpEntity<>(json, h);
            ResponseEntity<String> resp = rest.postForEntity(url, req, String.class);
            log.info("[通知] <- HTTP {} {}", resp.getStatusCodeValue(), resp.getBody());
            return resp.getStatusCode().is2xxSuccessful();
        } catch (Exception ex) {
            log.error("[通知] 推送失败 订阅={} url={} err={}", sub.SubscribeID, url, ex.getMessage());
            // 标准要求：推送失败应重传，生产环境请落库后由重试任务补偿
            return false;
        }
    }

    /** 由 ReceiveAddr / ReceivePort 拼出通知地址 */
    private String buildUrl(SubscribeObject sub) {
        String addr = sub.ReceiveAddr;
        if (addr == null || addr.trim().isEmpty()) return null;
        addr = addr.trim();

        if (!addr.startsWith("http://") && !addr.startsWith("https://")) {
            String port = (sub.ReceivePort != null && sub.ReceivePort > 0) ? (":" + sub.ReceivePort) : "";
            addr = "http://" + addr + port;
        }
        // 没带具体路径就补标准通知路径
        if (!addr.contains("/VIID/")) {
            addr = addr + NOTIFY_PATH;
        }
        return addr;
    }
}
