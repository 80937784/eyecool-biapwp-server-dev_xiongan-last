package cn.eyecool.msg.domain;

import com.alibaba.fastjson.JSON;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 消息日志对象 msg_log
 * 
 * @author admin
 * @date 2021-04-15
 */
public class MsgLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 业务流水号 */
    @Excel(name = "msg.log.receivedseq")
    private String receivedSeq;

    /** 通知方式 */
    @Excel(name = "msg.log.noticemethod", dictType = "msg_notice_method")
    private String noticeMethod;

    /** 消息主题 */
    @Excel(name = "msg.log.msgsubject")
    private String msgSubject;

    /** 消息内容 */
    @Excel(name = "msg.log.msgcontent")
    private String msgContent;

    /** 是否包含附件 */
    @Excel(name = "msg.log.hasannext", dictType = "sys_yes_no")
    private String hasAnnex;

    /** 发信人标识 */
    @Excel(name = "msg.log.fromuser")
    private String fromUser;

    /** 收件人标识(多个使用“,”分隔) */
    @Excel(name = "msg.log.touser")
    private String toUser;

    /** 公众(企业)号标识 */
    private String officalAccountId;

    /** 公众(企业)号名称 */
    @Excel(name = "msg.log.offical.accountname")
    private String officalAccountName;

    /** 场景标识 */
    @Excel(name = "msg.log.scene.remark")
    private String sceneRemark;

    /** 发送状态 */
    @Excel(name = "msg.log.result.status", dictType = "msg_result")
    private String resultStatus;

    /** 错误信息 */
    @Excel(name = "msg.log.errmsg")
    private String errMsg;

    /** 发送结果json */
    @Excel(name = "msg.log.json.response")
    private String jsonResponse;

    /** 租户ID */
    private String tenantId;

    public MsgLog() {
        super();
    }

    public MsgLog(String receivedSeq, String noticeMethod, String msgSubject, String msgContent, String hasAnnex,
        String fromUser, String officalAccountId, String officalAccountName, String toUser, String sceneRemark,
        String resultStatus, String errMsg, String jsonResponse) {
        super();
        this.receivedSeq = receivedSeq;
        this.noticeMethod = noticeMethod;
        this.msgSubject = msgSubject;
        this.msgContent = msgContent;
        this.hasAnnex = hasAnnex;
        this.fromUser = fromUser;
        this.officalAccountId = officalAccountId;
        this.officalAccountName = officalAccountName;
        this.toUser = toUser;
        this.sceneRemark = sceneRemark;
        this.resultStatus = resultStatus;
        this.errMsg = errMsg;
        this.jsonResponse = jsonResponse;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setReceivedSeq(String receivedSeq) {
        this.receivedSeq = receivedSeq;
    }

    public String getReceivedSeq() {
        return receivedSeq;
    }

    public void setNoticeMethod(String noticeMethod) {
        this.noticeMethod = noticeMethod;
    }

    public String getNoticeMethod() {
        return noticeMethod;
    }

    public void setMsgSubject(String msgSubject) {
        this.msgSubject = msgSubject;
    }

    public String getMsgSubject() {
        return msgSubject;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public void setHasAnnex(String hasAnnex) {
        this.hasAnnex = hasAnnex;
    }

    public String getHasAnnex() {
        return hasAnnex;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public String getToUser() {
        return toUser;
    }

    public void setOfficalAccountId(String officalAccountId) {
        this.officalAccountId = officalAccountId;
    }

    public String getOfficalAccountId() {
        return officalAccountId;
    }

    public void setOfficalAccountName(String officalAccountName) {
        this.officalAccountName = officalAccountName;
    }

    public String getOfficalAccountName() {
        return officalAccountName;
    }

    public void setSceneRemark(String sceneRemark) {
        this.sceneRemark = sceneRemark;
    }

    public String getSceneRemark() {
        return sceneRemark;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setJsonResponse(String jsonResponse) {
        this.jsonResponse = jsonResponse;
    }

    public String getJsonResponse() {
        return jsonResponse;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
