package cn.eyecool.tradelog.controller;

import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.common.utils.uuid.IdUtils;
import cn.eyecool.system.service.ISysConfigService;
import cn.eyecool.tradelog.domain.PersonFaceSearchLog;
import cn.eyecool.tradelog.domain.police.DevicePolice;
import cn.eyecool.tradelog.domain.police.PoliceFace;
import cn.eyecool.tradelog.service.IXAPoliceService;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zfx
 * @ClassName TradeTestController
 * @description 测试
 * @since 2026/8/21 15:34
 **/
@Slf4j
@RestController
@RequestMapping("/api/standard/test")
public class TradeTestController {
    @Autowired
    private IXAPoliceService xaPoliceService;
    @Autowired
    private ISysConfigService sysConfigService;


    @RequestMapping(value = "/register", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxResult registerToPolice() {
        String policeHeadUrl = xaPoliceService.getPoliceHeadUrl();
        log.info("policeHeadUrl: {}", policeHeadUrl);
        String deviceId = sysConfigService.selectConfigByKey(SysConfigConstants.XA_POLICE_DEVICE_ID);
        return xaPoliceService.registerToPolice(policeHeadUrl,deviceId);
    }
    @RequestMapping(value = "/unRegister", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxResult unRegisterToPolice() {
        String policeHeadUrl = xaPoliceService.getPoliceHeadUrl();
        log.info("policeHeadUrl: {}", policeHeadUrl);
        String deviceId = sysConfigService.selectConfigByKey(SysConfigConstants.XA_POLICE_DEVICE_ID);
        return xaPoliceService.unRegisterToPolice(policeHeadUrl,deviceId);
    }
    @RequestMapping(value = "/keepalive", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxResult keepaliveToPolice() {
        String policeHeadUrl = xaPoliceService.getPoliceHeadUrl();
        log.info("policeHeadUrl: {}", policeHeadUrl);
        String deviceId = sysConfigService.selectConfigByKey(SysConfigConstants.XA_POLICE_DEVICE_ID);
        return xaPoliceService.keepaliveToPolice(policeHeadUrl,deviceId);
    }
    @RequestMapping(value = "/uploadImage", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxResult uploadImageToPolice() {
        String policeHeadUrl = xaPoliceService.getPoliceHeadUrl();
        log.info("policeHeadUrl: {}", policeHeadUrl);
        String base64 = PlatformFileUtils.readImageToRfc2045Base64("/test/test.jpg");
        String imageId = IdUtils.fastUUID();
        log.info("imageId: {}", imageId);
        return xaPoliceService.uploadImageToPolice(policeHeadUrl,base64,imageId);
    }
    @RequestMapping(value = "/uploadFace", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxResult uploadFaceToPolice() {
        String policeHeadUrl = xaPoliceService.getPoliceHeadUrl();
        log.info("policeHeadUrl: {}", policeHeadUrl);
        PoliceFace face = new PoliceFace();
        face.setFaceId("testImage");
        return xaPoliceService.uploadFaceToPolice(policeHeadUrl,face);
    }
    @RequestMapping(value = "/uploadFaces", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxResult uploadFacesToPolice(String id) {
        String policeHeadUrl = xaPoliceService.getPoliceHeadUrl();
        log.info("policeHeadUrls: {}", policeHeadUrl);
        PersonFaceSearchLog faceLog = new PersonFaceSearchLog();
        String base64 = PlatformFileUtils.getImageBase64("/test/test.jpg");
        String base642045 = PlatformFileUtils.readImageToRfc2045Base64("/test/test.jpg");
        String sceneImage = PlatformFileUtils.toRfc2045MimeBase64(base64);
        faceLog.setSceneImage(sceneImage);
        if (StringUtils.isEmpty(id)){
            id = "0";
        }
        faceLog.setId("150716443507517440"+id);
        return xaPoliceService.uploadFacesToPolice(policeHeadUrl,faceLog);
    }
    @RequestMapping(value = "/sendToPolice", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxResult sendToPolice(String id,String deviceCode) {
        log.info("sendToPolice: {}", DateUtils.getTime());
        PersonFaceSearchLog faceLog = new PersonFaceSearchLog();
        String base64 = PlatformFileUtils.getImageBase64("/test/test.jpg");
        String base642045 = PlatformFileUtils.readImageToRfc2045Base64("/test/test.jpg");
        String sceneImage = PlatformFileUtils.toRfc2045MimeBase64(base64);
        faceLog.setSceneImageBase64(base64);
        if (StringUtils.isEmpty(deviceCode)){
            deviceCode = "133F002007800001";
        }
        faceLog.setResult("0");
        faceLog.setReceivedTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS,"2026-09-30 22:02:32"));
        faceLog.setDeviceCode(deviceCode);
        return xaPoliceService.sendToPolice(faceLog);
    }
    @RequestMapping(value = "/sendDeviceToPolice", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxResult sendDeviceToPolice(String type,String apeId,String deviceCode) {
        log.info("sendToPolice: {}", DateUtils.getTime());

        String subscribeID = sysConfigService.selectConfigByKey(SysConfigConstants.XA_POLICE_DEVICE_SUB_SCRIBEID);

//        String receiveAddr = "http://10.245.64.69:14681/VIID/SubscribeNotifications";
        String receiveAddr = sysConfigService.selectConfigByKey(SysConfigConstants.XA_POLICE_DEVICE_URL);
        String userIdentify = sysConfigService.selectConfigByKey(SysConfigConstants.XA_POLICE_DEVICE_SUB_USER_IDENTIFY);
        String deviceId = sysConfigService.selectConfigByKey(SysConfigConstants.XA_POLICE_DEVICE_ID);
        if (StringUtils.isEmpty(type)){
            type = "2";
        }
        List<DevicePolice> devList;
        if ("3".equals(type)){
            devList = new ArrayList<>();
            if (StringUtils.isEmpty(apeId)){
                return AjaxResult.error("删除时候,apeId 不能为空");
            }
            DevicePolice devicePolice = new DevicePolice();
            devicePolice.setDeviceNo("133F002007800000");
            devicePolice.setDeviceName("生物识别平台");
            devicePolice.setApeId(apeId);
            devicePolice.setLatitude(115.91344183782071);
            devicePolice.setLongitude(39.04847870301173);
            devList.add(devicePolice);
        }else{
            DevicePolice dev = new DevicePolice();
            dev.setIsUpload("Y");
            dev.setDeviceNo(deviceCode);
            devList = xaPoliceService.selectDevicePoliceList(dev);
        }
        return xaPoliceService.sendNotifyAfterSubscribeSuccess(subscribeID,receiveAddr,userIdentify,devList,type);
    }
}
