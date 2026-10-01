package cn.eyecool.msg.trade.entity;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 短信发送信息实体类
 * 
 * @author mawenjun
 * @version 1.0
 * @date 2020年3月22日
 *
 */
public class MsgSmsSendInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务流水号 */
    private String receivedSeq;

    /** 场景标识 */
    private String sceneRemark;

    /** 发送模式(1：普通发送，2：模板发送) */
    private String msgType;

    /** 短信内容(普通消息使用) */
    private String msgContent;

    /** 收信人手机号 */
    private String[] toUserPhones;

    /** 模板ID（官方模板ID，模板发送使用） */
    private String templateId;

    /** 短信签名（模板发送使用） */
    private String sign;

    /** 消息模板数据(模板发送使用) */
    private ArrayList<String> templateParams;

    /** 收信人手机号拼接字符串(多个使用,分割) */
    private String toUserPhoneStr;

    /** 消息模板数据拼接字符串(多个使用,分割) */
    private String templateParamStr;

    /** 消息主题(消息说明，平台保存，不作为短信内容发送) */
    private String msgSubject;

    /** 短信云账户AppId */
    private String appId;

    /** 短信云账户AppSecrect */
    private String appSecrect;

    public MsgSmsSendInfo() {
        super();
    }

    public String getReceivedSeq() {
        return receivedSeq;
    }

    public void setReceivedSeq(String receivedSeq) {
        this.receivedSeq = receivedSeq;
    }

    public String getSceneRemark() {
        return sceneRemark;
    }

    public void setSceneRemark(String sceneRemark) {
        this.sceneRemark = sceneRemark;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }

    public String[] getToUserPhones() {
        return toUserPhones;
    }

    public void setToUserPhones(String[] toUserPhones) {
        this.toUserPhones = toUserPhones;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public ArrayList<String> getTemplateParams() {
        return templateParams;
    }

    public void setTemplateParams(ArrayList<String> templateParams) {
        this.templateParams = templateParams;
    }

    public String getToUserPhoneStr() {
        return toUserPhoneStr;
    }

    public void setToUserPhoneStr(String toUserPhoneStr) {
        this.toUserPhoneStr = toUserPhoneStr;
    }

    public String getTemplateParamStr() {
        return templateParamStr;
    }

    public void setTemplateParamStr(String templateParamStr) {
        this.templateParamStr = templateParamStr;
    }

    public String getMsgSubject() {
        return msgSubject;
    }

    public void setMsgSubject(String msgSubject) {
        this.msgSubject = msgSubject;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppSecrect() {
        return appSecrect;
    }

    public void setAppSecrect(String appSecrect) {
        this.appSecrect = appSecrect;
    }

}
