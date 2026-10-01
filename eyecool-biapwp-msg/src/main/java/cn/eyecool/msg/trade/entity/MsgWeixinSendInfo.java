package cn.eyecool.msg.trade.entity;

import java.io.Serializable;
import java.util.List;

import org.weixin4j.model.message.template.TemplateData;

/**
 * 微信发送消息实体类
 * 
 * @author mawenjun
 * @version 1.0
 * @date 2020年3月22日
 *
 */
public class MsgWeixinSendInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务流水号 */
    private String receivedSeq;

    /** 场景标识 */
    private String sceneRemark;

    /** 消息类型（1：普通消息，2：模板消息） */
    private String msgType;

    /** 公众号AppId */
    private String appId;

    /** 公众号AppSecrect */
    private String appSecrect;

    /** 粉丝openId集合 */
    private String[] openIds;

    /** 文本消息内容 */
    private String txtContent;

    /** 模板ID（模板消息启用） */
    private String templateId;

    /** 模板数据（模板消息启用） */
    private List<TemplateData> templateData;

    /** 模板跳转链接(模板消息启用) */
    private String redirectUrl;

    /** 粉丝openId拼接字符串(使用,分割) */
    private String openIdStr;

    /** 消息主题(消息说明，平台保存，不作为消息内容发送) */
    private String msgSubject;

    /** 粉丝微信绑定手机号(使用,分割) */
    private String phoneStr;

    /** 模板数据字符串 */
    private String templateDataStr;

    /** 媒体文件Id */
    private String mediaId;

    public MsgWeixinSendInfo() {
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

    public String[] getOpenIds() {
        return openIds;
    }

    public void setOpenIds(String[] openIds) {
        this.openIds = openIds;
    }

    public String getTxtContent() {
        return txtContent;
    }

    public void setTxtContent(String txtContent) {
        this.txtContent = txtContent;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public List<TemplateData> getTemplateData() {
        return templateData;
    }

    public void setTemplateData(List<TemplateData> templateData) {
        this.templateData = templateData;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getOpenIdStr() {
        return openIdStr;
    }

    public void setOpenIdStr(String openIdStr) {
        this.openIdStr = openIdStr;
    }

    public String getMsgSubject() {
        return msgSubject;
    }

    public void setMsgSubject(String msgSubject) {
        this.msgSubject = msgSubject;
    }

    public String getPhoneStr() {
        return phoneStr;
    }

    public void setPhoneStr(String phoneStr) {
        this.phoneStr = phoneStr;
    }

    public String getTemplateDataStr() {
        return templateDataStr;
    }

    public void setTemplateDataStr(String templateDataStr) {
        this.templateDataStr = templateDataStr;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

}
