package cn.eyecool.tradelog.domain;

import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 接口交易请求记录对象 trade_req_record
 * 
 * @author admin
 * @date 2021-05-13
 */
public class TradeReqRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 交易码 */
    private String transCode;

    /** 交易标题 */
    private String transTitle;

    /** 请求时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date receivedTime;

    /** 客户端IP */
    private String clientIp;

    /** 响应时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date sendTime;

    /** 耗时(ms) */
    private Long timeUsed;

    /** 状态码 */
    private String statusCode;

    /** 交易请求路径 */
    private String transUrl;

    /** 处理方法 */
    private String classMethod;

    /** 场景编码 */
    private String channelCode;

    /** 租户ID */
    private String tenantId;

    /** 详情文件路径 */
    private String detailFilePath;

    /** 记录详情 */
    private TradeReqRecordDetail recordDetail;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setTransCode(String transCode) {
        this.transCode = transCode;
    }

    public String getTransCode() {
        return transCode;
    }

    public void setTransTitle(String transTitle) {
        this.transTitle = transTitle;
    }

    public String getTransTitle() {
        return transTitle;
    }

    public void setReceivedTime(Date receivedTime) {
        this.receivedTime = receivedTime;
    }

    public Date getReceivedTime() {
        return receivedTime;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setTimeUsed(Long timeUsed) {
        this.timeUsed = timeUsed;
    }

    public Long getTimeUsed() {
        return timeUsed;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setTransUrl(String transUrl) {
        this.transUrl = transUrl;
    }

    public String getTransUrl() {
        return transUrl;
    }

    public void setClassMethod(String classMethod) {
        this.classMethod = classMethod;
    }

    public String getClassMethod() {
        return classMethod;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getDetailFilePath() {
        return detailFilePath;
    }

    public void setDetailFilePath(String detailFilePath) {
        this.detailFilePath = detailFilePath;
    }

    public TradeReqRecordDetail getRecordDetail() {
        return recordDetail;
    }

    public void setRecordDetail(TradeReqRecordDetail recordDetail) {
        this.recordDetail = recordDetail;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
