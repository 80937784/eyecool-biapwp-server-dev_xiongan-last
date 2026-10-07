package cn.eyecool.tradelog.controller;

import cn.eyecool.tradelog.domain.police.SubscribeObject;
import cn.eyecool.tradelog.service.impl.NotifyPusher;
import cn.eyecool.tradelog.service.impl.SubscribeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据上报侧：收到采集数据后，向命中的订阅方推送通知。
 *
 * 真实业务里，把 broadcast() 这一行挂到你现有的入库/转发流程即可。
 */
@RestController
//@RequestMapping("/api")
public class DataReportController {

    private static final Logger log = LoggerFactory.getLogger(DataReportController.class);
    private static final String CT = "application/VIID+JSON";

    @Autowired
    private SubscribeStore store;

    @Autowired
    private NotifyPusher pusher;

    /** 上传自动采集人脸（7.2.12.1）—— 同时触发订阅通知 */
    @PostMapping(value = "/VIID/Faces",
            consumes = {CT, "application/json"},
            produces = CT + ";charset=UTF-8")
    public Map<String, Object> faces(@RequestBody Map<String, Object> body) {
        log.info("[数据] 收到人脸上报，顶层字段={}", body.keySet());
        broadcast("Faces", body);
        return ok();
    }

    /** 上传自动采集（7.2.13.1） */
    @PostMapping(value = "/VIID/MotorVehicles",
            consumes = {CT, "application/json"},
            produces = CT + ";charset=UTF-8")
    public Map<String, Object> motorVehicles(@RequestBody Map<String, Object> body) {
        log.info("[数据] 收到上报，顶层字段={}", body.keySet());
        broadcast("MotorVehicles", body);
        return ok();
    }

    /** 上传自动采集图像（7.2.9.1） */
    @PostMapping(value = "/VIID/Images",
            consumes = {CT, "application/json"},
            produces = CT + ";charset=UTF-8")
    public Map<String, Object> images(@RequestBody Map<String, Object> body) {
        log.info("[数据] 收到图像上报，顶层字段={}", body.keySet());
        broadcast("Images", body);
        return ok();
    }

    /** 手动触发一次通知（测试用）：POST /dev/notify/Faces */
    @PostMapping(value = "/dev/notify/{resourceType}",
            consumes = {CT, "application/json"},
            produces = CT + ";charset=UTF-8")
    public Map<String, Object> manualNotify(@PathVariable("resourceType") String resourceType,
                                            @RequestBody(required = false) Map<String, Object> body) {
        broadcast(resourceType, body);
        return ok();
    }

    /** 查看当前订阅（开发调试用） */
    @GetMapping("/dev/subscribes")
    public List<SubscribeObject> list() {
        return new java.util.ArrayList<>(store.all());
    }

    // ------------------------------------------------------------------

    private void broadcast(String resourceType, Map<String, Object> payload) {
        List<SubscribeObject> subs = store.matchByResource(resourceType);
        log.info("[通知] 资源={} 命中订阅数={}", resourceType, subs.size());
        pusher.broadcastAsync(subs, resourceType, payload);
    }

    private Map<String, Object> ok() {
        Map<String, Object> r = new java.util.LinkedHashMap<>();
        Map<String, Object> s = new java.util.LinkedHashMap<>();
        s.put("StatusCode", 0);
        s.put("StatusString", "正常");
        s.put("LocalTime", java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        r.put("ResponseStatusObject", s);
        return r;
    }
}
