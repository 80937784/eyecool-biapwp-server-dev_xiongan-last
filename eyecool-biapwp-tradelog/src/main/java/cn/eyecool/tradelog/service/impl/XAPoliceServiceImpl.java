package cn.eyecool.tradelog.service.impl;

import cn.eyecool.common.constant.Constants;
import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.common.utils.uuid.IdUtils;
import cn.eyecool.system.domain.SysConfig;
import cn.eyecool.system.service.ISysConfigService;
import cn.eyecool.tradelog.ViidIdGenerator;
import cn.eyecool.tradelog.domain.PersonFaceSearchLog;
import cn.eyecool.tradelog.domain.police.*;
import cn.eyecool.tradelog.domain.police.face.*;
import cn.eyecool.tradelog.domain.police.notice.APEObject;
import cn.eyecool.tradelog.domain.police.notice.DeviceList;
import cn.eyecool.tradelog.domain.police.notice.SubscribeNotificationListObject;
import cn.eyecool.tradelog.domain.police.notice.SubscribeNotificationObject;
import cn.eyecool.tradelog.service.IPersonFaceSearchLogService;
import cn.eyecool.tradelog.service.IXAPoliceService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.logging.log4j.util.Strings;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author zfx
 * @ClassName XAPoliceServiceImpl
 * @description 雄安公安接口对接类
 * @since 2026/8/20 9:46
 **/
@Slf4j
@Service
public class XAPoliceServiceImpl implements IXAPoliceService {
    @Autowired
    private ISysConfigService sysConfigService;
    @Autowired
    private IPersonFaceSearchLogService personFaceSearchLogService;
    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(60)).writeTimeout(Duration.ofSeconds(60))
            .authenticator(new DigestAuthenticator("spyzw1", "xaxq2026"))
            .readTimeout(Duration.ofSeconds(60)).retryOnConnectionFailure(true).build();

    private static final SerializeConfig VIID_SERIALIZE_CONFIG = new SerializeConfig(true);
    private static final MediaType MEDIA_TYPE_VIID_JSON = MediaType.parse("application/VIID+JSON; charset=utf-8");
    private String policeHeadUrl = Strings.EMPTY;
    /**
     * 设备注册缓存 key:deviceId
     */
    private static final Map<String, Long> deviceCacheMap = new ConcurrentHashMap<>();

    /**
     * 注册失败延迟重试线程池，单线程
     */
    private final ScheduledExecutorService registerRetryExecutor = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void initRegister() {
        String policeUrl = getPoliceHeadUrl();
        if (StringUtils.isEmpty(policeUrl)) {
            log.error("调用公安接口注册地址配置为空，跳过启动注册");
            return;
        }
        String deviceId = sysConfigService.selectConfigByKey(SysConfigConstants.XA_POLICE_DEVICE_ID);
        if (StringUtils.isEmpty(deviceId)) {
            log.error("调用公安接口注册deviceId配置为空，跳过启动注册");
            return;
        }
        doRegister(policeUrl, deviceId);
    }

    /**
     * 执行注册逻辑，失败则300s后重试
     */
    private void doRegister(String policeUrl, String deviceId) {
        AjaxResult result;
        try {
            result = registerToPolice(policeUrl, deviceId);
        } catch (Exception e) {
            log.error("调用公安接口注册注册调用异常 deviceId:{}", deviceId, e);
            result = AjaxResult.error(e.getMessage());
        }

        if (result.isSuccess()) {
            log.info("调用公安接口注册设备注册成功 deviceId:{}", deviceId);
            //注册成功放入map，存入当前时间戳
            deviceCacheMap.put(deviceId, System.currentTimeMillis());
        } else {
            log.warn("调用公安接口注册注册失败，300s后重试 deviceId:{},msg:{}", deviceId, result);
            //300秒后重试注册
            registerRetryExecutor.schedule(() -> doRegister(policeUrl, deviceId), 300, TimeUnit.SECONDS);
        }
    }

    @Override
    public AjaxResult sendToPolice(PersonFaceSearchLog faceSearchLog) {
        String deviceId = sysConfigService.selectConfigByKey(SysConfigConstants.XA_POLICE_DEVICE_ID);
//        if (StringUtils.isNotEmpty(deviceId) && deviceCacheMap.containsKey(deviceId)) {
//            deviceCacheMap.put(deviceId, System.currentTimeMillis());
//        }
//        String sceneImage = PlatformFileUtils.readImageToRfc2045Base64(faceSearchLog.getSceneImage());
//        faceSearchLog.setSceneImage(sceneImage);
        if (StringUtils.isEmpty(faceSearchLog.getId())) {
            faceSearchLog.setId(IdWorker.getNextStringId());
        }
        String defalutUrl = sysConfigService.selectConfigByKey(SysConfigConstants.XA_POLICE_DATA_IMAGE_URL);
        String subscribeId = sysConfigService.selectConfigByKey(SysConfigConstants.XA_POLICE_DATA_SUB_SCRIBEID);
        String userIdentify = sysConfigService.selectConfigByKey(SysConfigConstants.XA_POLICE_DATA_SUB_USER_IDENTIFY);
        AjaxResult ajaxResult = sendImageNotifyAfterSubscribeSuccess(defalutUrl, "", subscribeId, userIdentify, faceSearchLog);
        if (ajaxResult.isSuccess()) {
            PersonFaceSearchLog fs = new PersonFaceSearchLog();
            fs.setId(faceSearchLog.getId());
            fs.setToPolice("Y");
            personFaceSearchLogService.updatePersonFaceSearchLog(fs);
            return ajaxResult;
        } else {
            log.warn("上传图片失败，error[{}]", ajaxResult.getMsg());
            return ajaxResult;
        }
    }

    /**
     * 注册
     */
    @Override
    public AjaxResult registerToPolice(String policeUrl, String deviceId) {
        ViidSystemReq req = new ViidSystemReq();
        ViidSystemReq.RegisterObject register = new ViidSystemReq.RegisterObject();
        register.setDeviceID(deviceId);
        req.setRegisterObject(register);
        String uri = policeUrl + "/VIID/System/Register";
        AjaxResult ajaxResult = doPostRegister(uri, JSONObject.toJSONString(req, VIID_SERIALIZE_CONFIG));
        if (!ajaxResult.isSuccess()) {
            log.error("调用公安接口注册注册失败， deviceId:{},msg:{}", deviceId, ajaxResult);
        }
        return ajaxResult;
    }

    @Override
    public AjaxResult unRegisterToPolice(String policeUrl, String deviceId) {
        ViidSystemReq req = new ViidSystemReq();
        ViidSystemReq.UnRegisterObject unRegister = new ViidSystemReq.UnRegisterObject();
        unRegister.setDeviceID(deviceId);
        req.setUnRegisterObject(unRegister);
        String uri = policeUrl + "/VIID/System/UnRegister";
        return doPost(uri, JSONObject.toJSONString(req, VIID_SERIALIZE_CONFIG));
    }

    @Override
    public AjaxResult keepaliveToPolice(String policeUrl, String deviceId) {
        ViidSystemReq req = new ViidSystemReq();
        ViidSystemReq.KeepaliveObject keepAlive = new ViidSystemReq.KeepaliveObject();
        keepAlive.setDeviceID(deviceId);
        req.setKeepaliveObject(keepAlive);
        String uri = policeUrl + "/VIID/System/Keepalive";
        log.info("keepaliveToPolice url[{}]reqBody [{}]", uri, JSONObject.toJSONString(req, VIID_SERIALIZE_CONFIG));
        return doPost(uri, JSONObject.toJSONString(req, VIID_SERIALIZE_CONFIG));
    }

    @Override
    public AjaxResult uploadImageToPolice(String policeUrl, String imageBase64, String faceId) {
//        String rfc2045Base64 = Base64.getMimeEncoder().encodeToString(imageBytes);
        String uri = policeUrl + "/VIID/Images/" + faceId + "/Data";
        MediaType MEDIA_TYPE_STREAM = MediaType.parse("application/octet-stream; charset=utf-8");
        RequestBody body = RequestBody.create(MEDIA_TYPE_STREAM, imageBase64);
        Request request = new Request.Builder()
                .url(uri)
                .post(body)
                .build();

        log.info("url[{}]reqBody [{}]", uri, body);
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("http status:" + response.code());
            }
            String respBody = response.body().string();
            log.info("VIID image upload xml response[{}]", respBody);
            // todo: 解析xml respBody，封装返回AjaxResult
            return AjaxResult.success(respBody);
        } catch (Exception e) {
            log.error("调用调用公安接口注册图片上传接口异常 url:{}", uri, e);
            throw new RuntimeException("调用调用公安接口注册图片上传接口异常 url:" + uri, e);
        }
    }

    @Override
    public AjaxResult uploadFaceToPolice(String policeUrl, PoliceFace face) {
        JSONArray faceList = new JSONArray();
        JSONObject faceReq = new JSONObject();
        faceReq.put("FaceObject", face);
        faceList.add(faceReq);
        JSONObject req = new JSONObject();
        req.put("FaceList", faceList);
        String uri = policeUrl + "/VIID/Faces";
        return doPost(uri, JSONObject.toJSONString(req, VIID_SERIALIZE_CONFIG));
    }

    /**
     * 获取请求的地址前缀url
     */
    @Override
    public String getPoliceHeadUrl() {
        if (StringUtils.isEmpty(policeHeadUrl)) {
            String configKey = SysConfigConstants.XA_POLICE_URL;
            policeHeadUrl = sysConfigService.selectConfigByKey(configKey);
        }
        return policeHeadUrl;
    }

    @Override
    public AjaxResult uploadFacesToPolice(String policeHeadUrl, PersonFaceSearchLog faceLog) {
        String base64Img = faceLog.getSceneImage();
        String deviceId = sysConfigService.selectConfigByKey(SysConfigConstants.XA_POLICE_DEVICE_ID);
// 2.构建子图数组：14人脸特写、11场景图
        String imageId11 = IdUtils.fastUUID();
        String imageId14 = IdUtils.fastUUID();

        SubImageInfoObject img11 = new SubImageInfoObject();
        img11.setImageID(imageId11);
        img11.setEventSort(10);
        img11.setDeviceID(deviceId);
        img11.setType("11");
        img11.setFileFormat("Jpeg");
        String receivedTime = DateUtils.parseDateToStr(DateUtils.YYYYMMDDHHMMSS, faceLog.getReceivedTime() == null ? new Date() : faceLog.getReceivedTime());
        img11.setShotTime(receivedTime);
        img11.setWidth(200);
        img11.setHeight(200);
        img11.setData(base64Img);

        SubImageList subImageList = new SubImageList();
        List imgList = new ArrayList();
//        imgList.add(img14);
        imgList.add(img11);
        subImageList.setSubImageInfoObject(imgList);

        String randNum16 = ViidIdGenerator.getRandomDigitStr(16, Boolean.TRUE);
        String sourceId = faceLog.getId() + "000000" + randNum16;
        String faceId = faceLog.getReceivedSeq() + randNum16;
        // 3.人脸对象
        FaceObject faceObj = new FaceObject();
        faceObj.setFaceID(faceId);
        faceObj.setInfoKind(1);
        faceObj.setSourceID(sourceId);
        faceObj.setDeviceID(deviceId);
        faceObj.setShotTime(receivedTime);
        faceObj.setLeftTopX(100);
        faceObj.setLeftTopY(100);
        faceObj.setRightBtmX(300);
        faceObj.setRightBtmY(300);
        faceObj.setSubImageList(subImageList);

        FaceListObject faceList = new FaceListObject();
        List fList = new ArrayList();
        fList.add(faceObj);
        faceList.setFaceObject(fList);

        FaceListObjectRoot root = new FaceListObjectRoot();
        root.setFaceListObject(faceList);

        // 序列化为VIID请求报文（reqBody）
        String reqBody = JSON.toJSONString(root);
        System.out.println(reqBody);

        // 直接调用你写好的doPost上报接口
        String url = policeHeadUrl + "/VIID/Faces";
        AjaxResult result = doPostRegister(url, reqBody);
        return result;
    }

    /**
     * 通用POST，参数校验 + 设置Content‑Type: application/VIID+JSON
     */
    private AjaxResult doPost(String url, String reqBody) {
        RequestBody body = RequestBody.create(MEDIA_TYPE_VIID_JSON, reqBody);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        log.info("url[{}]reqBody [{}]", url, reqBody);
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("http status:" + response.code());
            }
            String respBody = response.body().string();
            log.info("doPost response result[{}]", respBody);
            AjaxResult result = convertToAjaxResult(respBody);
            return result;
        } catch (Exception e) {
            log.error("调用调用公安接口接口异常 url:{}", url, e);
            throw new RuntimeException("调用调用公安接口接口异常 url:" + url, e);
        }
    }

    private AjaxResult doPostRegister(String url, String reqBody) {
        String userIdentify = sysConfigService.selectConfigByKey(SysConfigConstants.XA_POLICE_DEVICE_ID);
        RequestBody body = RequestBody.create(MEDIA_TYPE_VIID_JSON, reqBody);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("User-Identify", userIdentify)
                .build();
        log.info("doPostRegister url[{}]reqBody [{}]", url, reqBody);
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("http status:" + response.code());
            }
            String respBody = response.body().string();
            log.info("doPostRegister response result[{}]", respBody);
            // {"ResponseStatusObject":{"Id":"13310001111206232323","StatusString":"注册成功","StatusCode":0,"RequestURL":"/VIID/System/Register","LocalTime":"20260930142220"}}
            AjaxResult result = convertToAjaxResult(respBody);
            return result;
        } catch (Exception e) {
            log.error("调用调用公安接口注册接口异常 url:{}", url, e);
            throw new RuntimeException("调用调用公安接口注册接口异常 url:" + url, e);
        }
    }

    private AjaxResult convertToAjaxResult(String respBody) {
        if (StringUtils.isEmpty(respBody)) {
            return AjaxResult.error("调用公安接口返回应答为空");
        }
        JSONObject jsonObject = JSONObject.parseObject(respBody);
        if (jsonObject.containsKey("ResponseStatusObject")) {
            String statusObject = jsonObject.getString("ResponseStatusObject");
            ViidResponse.ResponseStatus viidResponse = JSON.parseObject(statusObject, ViidResponse.ResponseStatus.class);
            if (viidResponse == null || viidResponse.getStatusCode() == null) {
                return AjaxResult.error("ResponseStatusObject 为空");
            }
            if (Constants.STATUS_ZERO.equals(viidResponse.getStatusCode())) {
                return AjaxResult.success(viidResponse.getStatusCode());
            }
            return AjaxResult.error(viidResponse.getStatusCode() + viidResponse.getStatusString());
        }
        return AjaxResult.success(respBody);
    }

    /**
     * 定时任务：每80s执行一次保活逻辑
     * 规则：当前时间 - lastTs <10s →跳过keepalive；调用保活成功更新时间戳
     */
    @Scheduled(fixedRate = 80 * 1000)
    public void scheduledKeepAlive() {
        String policeUrl = getPoliceHeadUrl();
        log.info("调用公安保活接口[执行了没呢][{}]policeUrl[{}]", DateUtils.getTime(), policeUrl);
        if (StringUtils.isEmpty(policeUrl)) {
            return;
        }
        long now = System.currentTimeMillis();
        if (StringUtils.isEmpty(deviceCacheMap)) {
            String deviceId = sysConfigService.selectConfigByKey(SysConfigConstants.XA_POLICE_DEVICE_ID);
            AjaxResult ajaxResult = registerToPolice(policeUrl, deviceId);
            if (ajaxResult.isSuccess()) {
                log.info("scheduledKeepAlive调用公安接口注册设备注册成功 deviceId:{}", deviceId);
                //注册成功放入map，存入当前时间戳
                deviceCacheMap.put(deviceId, System.currentTimeMillis());
            } else {
                log.error("scheduledKeepAlive调用公安接口注册注册失败， deviceId:{},msg:{}", deviceId, ajaxResult);
            }
        } else {
            for (Map.Entry<String, Long> entry : deviceCacheMap.entrySet()) {
                String deviceId = entry.getKey();
                Long lastTs = entry.getValue();
                if (lastTs == null) {
                    continue;
                }
                long diffSeconds = (now - lastTs) / 1000L;
                log.info("keepalive deviceId:{},now{},lastTs{},diff:{}s", deviceId, now, lastTs, diffSeconds);
                //小于10秒跳过保活
                if (diffSeconds < 10) {
                    log.debug("距离上次上报不足10s，跳过keepalive deviceId:{},diff:{}s", deviceId, diffSeconds);
                    continue;
                }
                try {
                    AjaxResult ajaxResult = keepaliveToPolice(policeUrl, deviceId);
                    if (ajaxResult.isSuccess()) {
                        deviceCacheMap.put(deviceId, now);
                        log.debug("keepalive保活成功 deviceId:{}", deviceId);
                    } else {
                        log.warn("keepalive保活失败 deviceId:{} msg:{}", deviceId, ajaxResult.getMsg());
                    }
                } catch (Exception e) {
                    log.error("keepalive调用异常 deviceId:{}", deviceId, e);
                }
            }
        }

    }

    // 发送通知
    @Override
    public AjaxResult sendSubscribeNotification(String defaultUrl, String notifyUrl, String userIdentify, JSONObject notifyObj) {
        String reqBody = JSONObject.toJSONString(notifyObj);
        RequestBody body = RequestBody.create(MEDIA_TYPE_VIID_JSON, reqBody);
        Request request = new Request.Builder()
                .url(StringUtils.isEmpty(defaultUrl) ? notifyUrl : defaultUrl)
                .header("User-Identify", userIdentify)
                .post(body)
                .build();
        log.info("sendSubscribeNotification url[{}]reqBody [{}]", notifyUrl, reqBody);
        try (Response response = okHttpClient.newCall(request).execute()) {
            String respBody = response.body() != null ? response.body().string() : "";
            log.info("[推送SubscribeNotifications] url={}, req={}, resp={}", StringUtils.isEmpty(defaultUrl) ? notifyUrl : defaultUrl, reqBody, respBody);
            if (StringUtils.isNotEmpty(respBody)) {
                //resp={"ResponseStatusListObject":{"ResponseStatusObject":[{"RequestURL":"/VIID/SubscribeNotifications","StatusCode":0,"StatusString":"操作成功","Id":"500000000000041790761253508011153","LocalTime":"20260930174043"}]}}
                ViidResponse viidResponse = JSONObject.parseObject(respBody, ViidResponse.class);
                ResponseStatusListObject responseStatusListObject = null;
                if (viidResponse != null){
                    responseStatusListObject = viidResponse.ResponseStatusListObject;
                }else{
                    return AjaxResult.error("respBody is empty");
                }
                if (responseStatusListObject != null && responseStatusListObject.ResponseStatusObject != null) {
                    ResponseStatusObject responseStatusObject = responseStatusListObject.ResponseStatusObject.get(0);
                    if (responseStatusObject != null) {
                        if (responseStatusObject.StatusCode == 0) {
                            return AjaxResult.success(responseStatusObject.Id);
                        }
                        return AjaxResult.error(responseStatusObject.StatusString);
                    } else {
                        return AjaxResult.error("responseStatusListObject.ResponseStatusObject is empty");
                    }
                }else{
                    return AjaxResult.error("ResponseStatusObject not exists");
                }
            }else{
                return AjaxResult.error("ResponseStatusListObject is empty");
            }
        } catch (Exception e) {
            log.error("[推送SubscribeNotifications失败] url={}", notifyUrl, e);
            //继续往 http://10.245.64.69:14681/VIID/SubscribeNotifications 发送
            if (StringUtils.isNotEmpty(notifyUrl)){
                sendSubscribeNotification("", notifyUrl, userIdentify, notifyObj);
            }
            log.info("[推送SubscribeNotifications失败]往http://10.245.64.69:14681/VIID/SubscribeNotifications失败 再次推送 url={}", notifyUrl);
            return AjaxResult.error("调用调用公安接口发送通知接口异常 url:" + notifyUrl, e);
        }
    }

    @Override
    public int insertSysConfig(String configKey, String configVal) {
        if (StringUtils.isNotEmpty(configVal)) {
            SysConfig config = new SysConfig();
            config.setConfigKey(configKey);
            config.setConfigValue(configVal);
            config.setConfigType("N");
            config.setTenantMaintain("N");
            int i = sysConfigService.insertConfig(config);
            return i;
        }
        return 0;
    }

    /**
     * 组装截图格式的SubscribeNotifications报文，推送给上级平台 SubscribeObject sub
     */
    @Override
    public AjaxResult sendNotifyAfterSubscribeSuccess(String subscribeID, String receiveAddr, String userIdentify,String deviceId) {
        JSONObject subject = new JSONObject();
        SubscribeNotificationListObject root = new SubscribeNotificationListObject();
        SubscribeNotificationObject item = new SubscribeNotificationObject();
        if (StringUtils.isEmpty(deviceId)){
            deviceId = sysConfigService.selectConfigByKey(SysConfigConstants.XA_POLICE_DEVICE_ID);
        }
        // 唯一ID，自行生成
        item.NotificationID = "50000000000004" + System.currentTimeMillis() + ViidIdGenerator.getRandomDigitStr(6, Boolean.TRUE);
        item.SubscribeID = subscribeID; //复用订阅的SubscribeID
        item.Title = "市民服务中心订阅设备";
        item.ExecuteOperation = 1; //1=增加订阅通知
        item.InfoIDs = deviceId; //你之前GET拿到的ApeID
        item.TriggerTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        // 点位设备信息，和截图保持一致
        DeviceList deviceList = new DeviceList();
        APEObject ape = new APEObject();
        ape.ApeID = deviceId;
        ape.Name = "生物识别平台";
        ape.Model = "BioServer";
        ape.IPAddr = "10.1.47.251";
        ape.Port = 8701;
        ape.Place = "平台服务";
        ape.CapDirection = 0;
        ape.MonitorAreaDesc = "";
        ape.OwnerApsID = "";
        ape.UserId = "admin";
        ape.Password = "admin123";
        ape.PlaceCode = "500231";
        ape.OrgCode = "";
        ape.MonitorDirection = "";
        ape.IsOnline = "1";
        ape.FunctionType = "2";
        ape.PositionType = "";
        ape.Longitude = new BigDecimal(116.403874);
        ape.Latitude = new BigDecimal(39.031049);
        List apeList = new ArrayList<>();
        apeList.add(ape);
        deviceList.APEObject = apeList;
        item.DeviceList = deviceList;

        List itemList = new ArrayList<>();
        itemList.add(item);
        root.SubscribeNotificationObject = itemList;
        subject.put("SubscribeNotificationListObject", root);
        String defaultUrl = sysConfigService.selectConfigByKey(SysConfigConstants.XA_POLICE_DEVICE_URL);
        if (StringUtils.isEmpty(defaultUrl)){
            defaultUrl = "http://10.245.64.69:14681/VIID/SubscribeNotifications";
        }
        log.info("准备回复通知设备消息defaultUrl[{}]",defaultUrl);
//        xaPoliceService.sendSubscribeNotification(defaultUrl,sub.ReceiveAddr, sub.userIdentify, subject);
        AjaxResult ajaxResult = this.sendSubscribeNotification(defaultUrl, receiveAddr, userIdentify, subject);
        return ajaxResult;
    }

    @Override
    public AjaxResult sendImageNotifyAfterSubscribeSuccess(String defalutUrl, String receiveAddr, String subscribeID, String userIdentify, PersonFaceSearchLog faceLog) {
        // deviceId 20位
        String deviceId = sysConfigService.selectConfigByKey(SysConfigConstants.XA_POLICE_DEVICE_ID);
        String faceBase64 = faceLog.getSceneImageBase64();
        String faceRfcBase64 = "";
        if (StringUtils.isNotEmpty(faceBase64)) {
            faceRfcBase64 = PlatformFileUtils.toRfc2045MimeBase64(faceBase64);
        } else {
            if (StringUtils.isNotEmpty(faceLog.getSceneImage())) {
                String stringBase64 = PlatformFileUtils.getImageBase64(faceLog.getSceneImage());
                if (StringUtils.isNotBlank(stringBase64)) {
                    stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                    faceRfcBase64 = PlatformFileUtils.toRfc2045MimeBase64(stringBase64);
                }
            }
        }
        if (StringUtils.isEmpty(faceRfcBase64)) {
            return AjaxResult.error("图片为空");
        }

        Date receiveTime = faceLog.getReceivedTime() == null ? new Date() : faceLog.getReceivedTime();
        String receiveTimeStr = DateUtils.parseDateToStr("yyyyMMddHHmmss", receiveTime);
        /** 组装 FaceObject */
        // 41位 deviceId 20 +01 +14 + 5位随机数
        // 50023190031190000072 02 2019091719463700112
        String sourceId = deviceId + "01" + receiveTimeStr + ViidIdGenerator.getRandomDigitStr(5, Boolean.TRUE);
        // 3.人脸对象
        PoliceFace face = new PoliceFace();
        // faceId 为48位 sourceId 为41位  + 7加随机数
        String faceId = sourceId + ViidIdGenerator.getRandomDigitStr(7, Boolean.TRUE);
        face.setFaceId(faceId);
        face.setInfoKind(2);
        face.setSourceID(sourceId);
        face.setDeviceID(deviceId);
        face.setName(faceLog.getDeviceName());
        face.setUsedName(faceLog.getPersonName());
        face.setAlias(faceLog.getDeviceCode());
        face.setIDNumber(faceLog.getUniqueId());
        face.setLocationMarkTime(receiveTimeStr);
        face.setFaceAppearTime(receiveTimeStr);
        face.setFaceDisAppearTime(DateUtils.parseDateToStr("yyyyMMddHHmmss",DateUtils.addSeconds(receiveTime,10)));

        SubImageList subImageList = new SubImageList();
        List<SubImageInfoObject> subImageInfoList = new ArrayList<>();
        SubImageInfoObject subImageInfo = new SubImageInfoObject();
        subImageInfo.setImageID(sourceId);
        subImageInfo.setEventSort(1);
        subImageInfo.setDeviceID(deviceId);
        subImageInfo.setType("14");
        subImageInfo.setFileFormat("Jpeg");
        subImageInfo.setShotTime(receiveTimeStr);
        subImageInfo.setWidth(640);
        subImageInfo.setHeight(480);
        subImageInfo.setData(faceRfcBase64);
        subImageInfoList.add(subImageInfo);
        subImageList.setSubImageInfoObject(subImageInfoList);

        SubImageInfoObject subImageInfo11 = new SubImageInfoObject();
        subImageInfo11.setImageID(sourceId);
        subImageInfo11.setEventSort(2);
        subImageInfo11.setDeviceID(deviceId);
        subImageInfo11.setType("11");
        subImageInfo11.setFileFormat("Jpeg");
        subImageInfo11.setShotTime(receiveTimeStr);
        subImageInfo11.setWidth(640);
        subImageInfo11.setHeight(480);
        subImageInfo11.setData(faceRfcBase64);
        subImageInfoList.add(subImageInfo11);

        subImageList.setSubImageInfoObject(subImageInfoList);
        face.setSubImageList(subImageList);


        JSONObject root = new JSONObject();
        JSONObject subscribeNotificationListObject = new JSONObject();

        JSONObject subscribeNotificationObject = new JSONObject();
        String notificationID = "50000000000004" + System.currentTimeMillis() + ViidIdGenerator.getRandomDigitStr(6, Boolean.TRUE);
        subscribeNotificationObject.put("NotificationID", notificationID);
        subscribeNotificationObject.put("SubscribeID", subscribeID);
        subscribeNotificationObject.put("Title", "市民服务中心人脸数据");
        subscribeNotificationObject.put("ExecuteOperation", 1);
        subscribeNotificationObject.put("InfoIDs", faceId);
        subscribeNotificationObject.put("TriggerTime", receiveTime);
        JSONObject faceObjectList = new JSONObject();
        List FaceObjectList = new ArrayList<>();
        FaceObjectList.add(face);
        faceObjectList.put("FaceObject", FaceObjectList);
        subscribeNotificationObject.put("FaceObjectList", faceObjectList);
        List notificationObjectList = new ArrayList<>();
        notificationObjectList.add(subscribeNotificationObject);
        subscribeNotificationListObject.put("SubscribeNotificationObject", notificationObjectList);
        root.put("SubscribeNotificationListObject", subscribeNotificationListObject);
        AjaxResult ajaxResult = this.sendSubscribeNotification(defalutUrl, receiveAddr, userIdentify, root);
        return ajaxResult;
    }

    @Override
    public SysConfig selectConfigByKey(String configKey) {
        if (StringUtils.isEmpty(configKey)){
            return null;
        }
        SysConfig config = new SysConfig();
        config.setConfigKey(configKey);
        List<SysConfig> sysConfigs = sysConfigService.selectConfigList(config);
        return sysConfigs.get(0);
    }
}
