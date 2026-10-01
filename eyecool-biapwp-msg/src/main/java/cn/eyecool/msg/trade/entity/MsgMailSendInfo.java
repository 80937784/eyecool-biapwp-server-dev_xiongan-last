package cn.eyecool.msg.trade.entity;

import java.io.Serializable;
import java.util.List;

import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.web.multipart.MultipartFile;

/**
 * 邮件发送信息实体类
 * 
 * @author mawenjun
 * @version 1.0
 * @date 2020年3月22日
 *
 */
public class MsgMailSendInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务流水号 */
    private String receivedSeq;

    /** 场景标识 */
    private String sceneRemark;

    /** 邮件主题 */
    private String subject;

    /** 邮件内容 */
    private String content;

    /** 发信人邮箱 */
    private String from;

    /** 收件人邮箱列表 */
    private String[] tos;

    /** 抄送人邮箱列表 */
    private String[] ccs;

    /** 消息附件 */
    private List<MultipartFile> multipartFiles;

    /** 邮件类型 (1：文本邮件,2:HTML邮件) */
    private String mailType;

    /** 图片路径 */
    private String rscPath;

    /** 图片ID */
    private String rscId;

    /** 邮箱属性配置 */
    private MailProperties mailProperties;

    /** 收件人邮箱拼接字符串(多个使用,分割) */
    private String toMailStr;

    /** 抄送人邮箱拼接字符串(多个使用,分割) */
    private String ccMailStr;

    public MsgMailSendInfo() {
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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String[] getTos() {
        return tos;
    }

    public void setTos(String[] tos) {
        this.tos = tos;
    }

    public String[] getCcs() {
        return ccs;
    }

    public void setCcs(String[] ccs) {
        this.ccs = ccs;
    }

    public List<MultipartFile> getMultipartFiles() {
        return multipartFiles;
    }

    public void setMultipartFiles(List<MultipartFile> multipartFiles) {
        this.multipartFiles = multipartFiles;
    }

    public String getMailType() {
        return mailType;
    }

    public void setMailType(String mailType) {
        this.mailType = mailType;
    }

    public MailProperties getMailProperties() {
        return mailProperties;
    }

    public void setMailProperties(MailProperties mailProperties) {
        this.mailProperties = mailProperties;
    }

    public String getRscPath() {
        return rscPath;
    }

    public void setRscPath(String rscPath) {
        this.rscPath = rscPath;
    }

    public String getRscId() {
        return rscId;
    }

    public void setRscId(String rscId) {
        this.rscId = rscId;
    }

    public String getToMailStr() {
        return toMailStr;
    }

    public void setToMailStr(String toMailStr) {
        this.toMailStr = toMailStr;
    }

    public String getCcMailStr() {
        return ccMailStr;
    }

    public void setCcMailStr(String ccMailStr) {
        this.ccMailStr = ccMailStr;
    }

}
