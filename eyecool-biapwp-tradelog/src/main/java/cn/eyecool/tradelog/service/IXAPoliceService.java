package cn.eyecool.tradelog.service;

import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.system.domain.SysConfig;
import cn.eyecool.tradelog.domain.PersonFaceSearchLog;
import cn.eyecool.tradelog.domain.police.PoliceFace;
import com.alibaba.fastjson.JSONObject;

/**
 * @author
 * @description 雄安公安调用
 * @since 2026/8/20 9:45
 **/
public interface IXAPoliceService {
    /**
     * 推送给雄安
     * @param faceSearchLog
     * @return
     */
    AjaxResult sendToPolice(PersonFaceSearchLog faceSearchLog);
    /** 应支持注册、保活、注销、校时。注册失败时,应延迟300s内的随机时间后重新注册。注册成功后，在90s内未交互信息则进行心跳保活 */
    /** 注册 */
    AjaxResult registerToPolice(String policeUrl, String deviceId);
    /** 注销 */
    AjaxResult unRegisterToPolice(String policeUrl, String deviceId);
    /** 保活 */
    AjaxResult keepaliveToPolice(String policeUrl, String deviceId);
    /** 上传图片base64 */
    AjaxResult uploadImageToPolice(String policeUrl, String imageBase64,String faceId);
    /** 上传人脸图片 */
    AjaxResult uploadFaceToPolice(String policeUrl, PoliceFace face);
    /** 获取请求地址前缀 */
    String getPoliceHeadUrl();
    AjaxResult uploadFacesToPolice(String policeHeadUrl, PersonFaceSearchLog faceLog);

    AjaxResult sendSubscribeNotification(String defaultUrl,String receiveAddr, String userIdentify, JSONObject root);

    int insertSysConfig(String configKey, String configVal);
    /** 发送设备消息 */
    void sendNotifyAfterSubscribeSuccess(String subscribeID, String receiveAddr, String userIdentify);

    AjaxResult sendImageNotifyAfterSubscribeSuccess(String url, String receiveAddr, String subscribeID, String userIdentify,PersonFaceSearchLog faceSearchLog);

    SysConfig selectConfigByKey(String configKey);
}
