package cn.eyecool.msg.trade.entity;

import java.io.Serializable;

import com.alibaba.fastjson.JSON;

import cn.eyecool.common.constant.DictConstants;

/**
 * 钉钉发送消息实体类
 * 
 * @author mawenjun
 * @version 1.0
 * @date 2020年3月22日
 *
 */
public class MsgDingTalkSendInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务流水号 */
    private String receivedSeq;

    /** 场景标识 */
    private String sceneRemark;

    /** 消息类型（text：文本消息，link：链接消息，markdown:markdown消息，file: 文件消息...） */
    private String msgType;

    /** 企业(团队)corpId */
    private String corpId;

    /** 应用AgentId */
    private String agentId;

    /** 应用AppKey */
    private String appKey;

    /** 应用AppSecrect */
    private String appSecrect;

    /** 用户Id集合(使用，分割) */
    private String userIds;

    /** 部门Id集合(使用，分割) */
    private String deptIds;

    /** 消息主题(消息说明，平台保存，不作为消息内容发送) */
    private String msgSubject;

    /** 用户手机号集合(使用,分割) */
    private String phoneStr;

    /** 消息内容 */
    private String text;

    /** 消息标题 */
    private String title;

    /** 消息跳转链接 */
    private String messageUrl;

    /** 图片地址(mediaId) */
    private String picUrl;

    /** 媒体文件Id */
    private String mediaId;

    /** 媒体文件类型（image：图片，voice：语音，file: 文件） */
    private String mediaType;

    /** 是否发送全部用户 */
    private String toAllUser = DictConstants.YesOrNoState.NO;

    /** 语音时长(单位s) */
    private String duration;

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

    public String getCorpId() {
        return corpId;
    }

    public void setCorpId(String corpId) {
        this.corpId = corpId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAppSecrect() {
        return appSecrect;
    }

    public void setAppSecrect(String appSecrect) {
        this.appSecrect = appSecrect;
    }

    public String getUserIds() {
        return userIds;
    }

    public void setUserIds(String userIds) {
        this.userIds = userIds;
    }

    public String getDeptIds() {
        return deptIds;
    }

    public void setDeptIds(String deptIds) {
        this.deptIds = deptIds;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessageUrl() {
        return messageUrl;
    }

    public void setMessageUrl(String messageUrl) {
        this.messageUrl = messageUrl;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getToAllUser() {
        return toAllUser;
    }

    public void setToAllUser(String toAllUser) {
        this.toAllUser = toAllUser;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

}
