package cn.eyecool.tradelog.controller;

import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.system.domain.SysConfig;
import cn.eyecool.system.service.ISysConfigService;
import cn.eyecool.tradelog.domain.PersonFaceSearchLog;
import cn.eyecool.tradelog.domain.police.*;
import cn.eyecool.tradelog.service.IXAPoliceService;
import cn.eyecool.tradelog.service.impl.SubscribeStore;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 订阅通知接口（GA/T 1400.4-2017 5.4 级联接口 / 7.2.20）
 * <p>
 * 批量订阅              POST   /VIID/Subscribes
 * 批量修改、删除订阅任务  PUT    /VIID/Subscribes
 * 批量删除订阅任务       DELETE /VIID/Subscribes
 * 取消订阅              PUT    /VIID/Subscribes/{SubscribeID}
 */
@RestController
//@RequestMapping("/api")
public class SubscribeController {

    private static final Logger log = LoggerFactory.getLogger(SubscribeController.class);
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String CT = "application/VIID+JSON";
    private final String UP_BASIC_USER = "spyzw1";
    private final String UP_BASIC_PWD = "xaxq2026";
    @Autowired
    private SubscribeStore store;
    //    @Autowired
//    private ViidNotifyClient viidNotifyClient;
    @Autowired
    private IXAPoliceService xaPoliceService;
    @Autowired
    private ISysConfigService configService;

    /**
     * 批量订阅（也兼容 OperateType 0/1/2 混合提交）
     */
    @PostMapping(value = "/VIID/Subscribes",
            consumes = {CT, "application/json"},
            produces = CT + ";charset=UTF-8")
    public ViidResponse subscribe(@RequestBody String request, HttpServletRequest http) {
        log.info("[订阅] 批量订阅 identify={}req[{}]", http.getHeader("User-Identify"), request);
        SubscribeRequest req = JSONObject.parseObject(request, SubscribeRequest.class);
        return handle(req, http, "POST");
    }

    /**
     * 批量修改、删除订阅任务
     */
    @PutMapping(value = "/VIID/Subscribes",
            consumes = {CT, "application/json"},
            produces = CT + ";charset=UTF-8")
    public ViidResponse modify(@RequestBody String request, HttpServletRequest http) {
        log.info("批量修改、删除订阅任务 identify={}req[{}]", http.getHeader("User-Identify"), request);
        SubscribeRequest req = JSONObject.parseObject(request, SubscribeRequest.class);
        return handle(req, http, "PUT");
    }

    /**
     * 批量删除订阅任务
     */
    @DeleteMapping(value = "/VIID/Subscribes",
            consumes = {CT, "application/json"},
            produces = CT + ";charset=UTF-8")
    public ViidResponse delete(@RequestBody String request, HttpServletRequest http) {
        log.info("批量删除订阅任务 identify={}req[{}]", http.getHeader("User-Identify"), request);
        SubscribeRequest req = JSONObject.parseObject(request, SubscribeRequest.class);
        return handle(req, http, "DELETE");
    }

    /**
     * 取消订阅（按 SubscribeID）
     */
    @PutMapping(value = "/VIID/Subscribes/{SubscribeID}",
            produces = CT + ";charset=UTF-8")
    public ViidResponse cancel(@PathVariable("SubscribeID") String subscribeId, HttpServletRequest http) {
        log.info("[订阅] 取消订阅 id={} identify={}", subscribeId, http.getHeader("User-Identify"));
        SubscribeObject removed = store.remove(subscribeId);
        ResponseStatusObject st = (removed != null)
                ? ResponseStatusObject.ok(subscribeId)
                : ResponseStatusObject.err(3, "订阅不存在");
        st.RequestURL = http.getRequestURI();
        st.LocalTime = LocalDateTime.now().format(TS);
        return wrap(st);
    }

    // ------------------------------------------------------------------

    private ViidResponse handle(SubscribeRequest req, HttpServletRequest http, String method) {
        List<ResponseStatusObject> result = new ArrayList<>();
        String url = http.getRequestURI();
        String now = LocalDateTime.now().format(TS);
        String identify = http.getHeader("User-Identify");
        log.info("[订阅] {} {} identify={}", method, url, identify);
        if (req == null || req.SubscribeListObject == null
                || req.SubscribeListObject.SubscribeObject == null
                || req.SubscribeListObject.SubscribeObject.isEmpty()) {
            ResponseStatusObject st = ResponseStatusObject.err(3, "参数错误");
            st.RequestURL = url;
            st.LocalTime = now;
            return wrap(st);
        }

        for (SubscribeObject sub : req.SubscribeListObject.SubscribeObject) {
            sub.userIdentify = identify;
            ResponseStatusObject st;
            try {
                st = one(sub, method);
            } catch (Exception e) {
                log.error("[订阅] 处理异常 id={} err={}", sub.SubscribeID, e.getMessage());
                st = ResponseStatusObject.err(2, "其他错误:" + e.getMessage());
            }
            st.RequestURL = url;
            st.LocalTime = now;
            result.add(st);
        }
        return new ViidResponse(new ResponseStatusListObject(result));
    }

    /**
     * 单条订阅处理：OperateType 0-增加 1-删除 2-修改
     */
    private ResponseStatusObject one(SubscribeObject sub, String method) {
        if (sub.SubscribeID == null || sub.SubscribeID.trim().isEmpty()) {
            return ResponseStatusObject.err(3, "参数错误:SubscribeID 必填");
        }

        int op;
        if (sub.OperateType != null) {
            op = sub.OperateType;
        } else {
            op = "DELETE".equals(method) ? 1 : 0;   // DELETE 方法默认视为删除
        }

        switch (op) {
            case 0:   // 增加
                if (sub.ReceiveAddr == null || sub.ReceiveAddr.trim().isEmpty()) {
                    log.error("[订阅] 新增失败 id={} err=ReceiveAddr 为空", sub.SubscribeID);
                    return ResponseStatusObject.err(3, "参数错误:ReceiveAddr 必填");
                }
                if (sub.SubscribeStatus == null) sub.SubscribeStatus = 0;
                store.save(sub);
                log.info("[订阅] 新增成功 准备发送点位 id={} uri={} -> {}",
                        sub.SubscribeID, sub.ResourceURI, sub.ReceiveAddr);
                // ==========【新增】订阅成功，组装截图报文，向上推送 SubscribeNotifications ==========
                if ("PUT".equals( method)){
                    // 订阅 回复设备信息
//                    sendNotifyAfterSubscribeSuccess(sub);
                    //更新参数表中的参数
                    SysConfig subscribeConfig = xaPoliceService.selectConfigByKey(SysConfigConstants.XA_POLICE_DEVICE_SUB_SCRIBEID);
                    if (subscribeConfig == null){
                        xaPoliceService.insertSysConfig(SysConfigConstants.XA_POLICE_DEVICE_SUB_SCRIBEID,sub.SubscribeID);
                    }else if (!subscribeConfig.getConfigValue().equals(sub.SubscribeID)){
                        subscribeConfig.setConfigValue(sub.SubscribeID);
                        configService.updateConfig(subscribeConfig);
                    }
                    SysConfig userIdentifyConfig = xaPoliceService.selectConfigByKey(SysConfigConstants.XA_POLICE_DEVICE_SUB_USER_IDENTIFY);
                    if (userIdentifyConfig == null){
                        xaPoliceService.insertSysConfig(SysConfigConstants.XA_POLICE_DEVICE_SUB_USER_IDENTIFY,sub.userIdentify);
                    }else if (!subscribeConfig.getConfigValue().equals(sub.userIdentify)){
                        userIdentifyConfig.setConfigValue(sub.userIdentify);
                        configService.updateConfig(userIdentifyConfig);
                    }
                    DevicePolice devicePolice = new DevicePolice();
                    devicePolice.setDeviceNo("133F002007800000");
                    devicePolice.setDeviceName("生物识别平台");
                    devicePolice.setApeId("13310001111316230000");
                    devicePolice.setLatitude(115.91344183782071);
                    devicePolice.setLongitude(39.04847870301173);
                    List<DevicePolice> devList = new ArrayList<>();
                    devList.add(devicePolice);
                    xaPoliceService.sendNotifyAfterSubscribeSuccess(sub.SubscribeID,sub.ReceiveAddr,sub.userIdentify,devList,"1");
                }else if ("POST".equals( method)){
                    // 订阅数据 订阅后 可以传照片
                    //更新参数表中的参数
                    SysConfig subscribeConfig = xaPoliceService.selectConfigByKey(SysConfigConstants.XA_POLICE_DATA_SUB_SCRIBEID);
                    if (subscribeConfig == null){
                        xaPoliceService.insertSysConfig(SysConfigConstants.XA_POLICE_DATA_SUB_SCRIBEID,sub.SubscribeID);
                    }else if (!subscribeConfig.getConfigValue().equals(sub.SubscribeID)){
                        subscribeConfig.setConfigValue(sub.SubscribeID);
                        configService.updateConfig(subscribeConfig);
                    }
                    SysConfig userIdentifyConfig = xaPoliceService.selectConfigByKey(SysConfigConstants.XA_POLICE_DATA_SUB_USER_IDENTIFY);
                    if (userIdentifyConfig == null){
                        xaPoliceService.insertSysConfig(SysConfigConstants.XA_POLICE_DATA_SUB_USER_IDENTIFY,sub.userIdentify);
                    }else if (!subscribeConfig.getConfigValue().equals(sub.userIdentify)){
                        userIdentifyConfig.setConfigValue(sub.userIdentify);
                        configService.updateConfig(userIdentifyConfig);
                    }
                    log.info("订阅图片数据 准备发送消息");
                    String url = configService.selectConfigByKey(SysConfigConstants.XA_POLICE_DATA_IMAGE_URL);
                    PersonFaceSearchLog faceSearchLog = new PersonFaceSearchLog();
                    String base64 = PlatformFileUtils.getImageBase64("/test/test.jpg");
                    faceSearchLog.setSceneImageBase64(base64);
                    faceSearchLog.setPersonName("ys");
                    String apeId = "13310001111316230000";
                    xaPoliceService.sendImageNotifyAfterSubscribeSuccess(url,sub.ReceiveAddr,sub.SubscribeID,sub.userIdentify,faceSearchLog,apeId);
                }

                return ResponseStatusObject.ok(sub.SubscribeID);

            case 1:   // 删除
                SubscribeObject removed = store.remove(sub.SubscribeID);
                log.info("[订阅] 删除 id={} exists={}", sub.SubscribeID, removed != null);
                return removed != null
                        ? ResponseStatusObject.ok(sub.SubscribeID)
                        : ResponseStatusObject.err(3, "订阅不存在");
            case 2:   // 修改
                SubscribeObject old = store.get(sub.SubscribeID);
                if (old == null) return ResponseStatusObject.err(3, "订阅不存在");
                merge(old, sub);
                store.save(old);
                log.info("[订阅] 修改成功 id={}", sub.SubscribeID);
                return ResponseStatusObject.ok(sub.SubscribeID);
            default:
                return ResponseStatusObject.err(3, "参数错误:未知 OperateType " + op);
        }
    }
    /**
     * 修改时只覆盖非空字段
     */
    private void merge(SubscribeObject target, SubscribeObject patch) {
        if (patch.Title != null) target.Title = patch.Title;
        if (patch.SubscribeDetail != null) target.SubscribeDetail = patch.SubscribeDetail;
        if (patch.ResourceURI != null) target.ResourceURI = patch.ResourceURI;
        if (patch.ApplicantName != null) target.ApplicantName = patch.ApplicantName;
        if (patch.ApplicantOrg != null) target.ApplicantOrg = patch.ApplicantOrg;
        if (patch.ApplicantMail != null) target.ApplicantMail = patch.ApplicantMail;
        if (patch.ApplicantPhone != null) target.ApplicantPhone = patch.ApplicantPhone;
        if (patch.BeginTime != null) target.BeginTime = patch.BeginTime;
        if (patch.EndTime != null) target.EndTime = patch.EndTime;
        if (patch.ReceiveAddr != null) target.ReceiveAddr = patch.ReceiveAddr;
        if (patch.ReceivePort != null) target.ReceivePort = patch.ReceivePort;
        if (patch.ReportCondition != null) target.ReportCondition = patch.ReportCondition;
        if (patch.SubscribeStatus != null) target.SubscribeStatus = patch.SubscribeStatus;
    }

    private ViidResponse wrap(ResponseStatusObject... items) {
        List<ResponseStatusObject> list = new ArrayList<>();
        for (ResponseStatusObject o : items) list.add(o);
        return new ViidResponse(new ResponseStatusListObject(list));
    }
}
