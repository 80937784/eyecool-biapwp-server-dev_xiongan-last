package cn.eyecool.tradelog.domain;

import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.annotation.Excel.Type;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 指纹比对日志对象 person_finger_match_log
 * 
 * @author admin
 * @date 2021-05-06
 */
public class PersonFingerMatchLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 业务流水号 */
    @Excel(name = "domain.person.faceiris.busi.req")
    private String receivedSeq;

    /** 人员标识 */
    @Excel(name = "domain.person.faceiris.uniqueid")
    private String uniqueId;

    /** 部门ID */
    @Excel(name = "domain.person.faceiris.deptid")
    private Long deptId;

    /** 人员唯一编号 */
    @Excel(name = "domain.person.faceiris.personname")
    private String personName;

    /** 部门名称 */
    @Excel(name = "domain.person.faceiris.deptname")
    private String deptName;

    /** 场景编码 */
    @Excel(name = "channel.info.channelcode")
    private String channelCode;

    /** 手指编码 */
    @Excel(name = "domain.person.finger.fingerno", dictType = "bio_finger_code")
    private String fingerNo;

    /** 现场照路径 */
    private String sceneImage;

    /** 底库照路径 */
    private String stockImage;

    /** 现场照与库底库照比对分值 */
    @Excel(name = "domain.person.face.scenestockscore")
    private Double sceneStockScore;

    /** 现场照与库底库照比对结果 */
    @Excel(name = "domain.person.finger.scenestockresult", dictType = "bio_result")
    private String sceneStockResult;

    /** 比对结果 */
    @Excel(name = "domain.person.faceiris.match.result", dictType = "bio_result")
    private String result;

    /** 请求时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "domain.person.face.request.time", dateFormat = "yyyy-MM-dd HH:mm:ss", type = Type.EXPORT)
    private Date receivedTime;

    /** 耗时(ms) */
    @Excel(name = "domain.person.faceiris.timeused")
    private Long timeUsed;

    /** 服务器标识 */
    private String serverId;

    /** 厂商 */
    private String vendorCode;

    /** 算法版本 */
    private String algsVersion;

    /** 定时任务执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date batchDate;

    /** 租户ID */
    private String tenantId;

    /** 现场照Base64 */
    private String sceneImageBase64;

    /** 底库照Base64 */
    private String stockImageBase64;

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

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Long getDeptId() {
        return deptId;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setFingerNo(String fingerNo) {
        this.fingerNo = fingerNo;
    }

    public String getFingerNo() {
        return fingerNo;
    }

    public void setSceneImage(String sceneImage) {
        this.sceneImage = sceneImage;
    }

    public String getSceneImage() {
        return sceneImage;
    }

    public void setStockImage(String stockImage) {
        this.stockImage = stockImage;
    }

    public String getStockImage() {
        return stockImage;
    }

    public void setSceneStockScore(Double sceneStockScore) {
        this.sceneStockScore = sceneStockScore;
    }

    public Double getSceneStockScore() {
        return sceneStockScore;
    }

    public void setSceneStockResult(String sceneStockResult) {
        this.sceneStockResult = sceneStockResult;
    }

    public String getSceneStockResult() {
        return sceneStockResult;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    public void setReceivedTime(Date receivedTime) {
        this.receivedTime = receivedTime;
    }

    public Date getReceivedTime() {
        return receivedTime;
    }

    public void setTimeUsed(Long timeUsed) {
        this.timeUsed = timeUsed;
    }

    public Long getTimeUsed() {
        return timeUsed;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getServerId() {
        return serverId;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public String getVendorCode() {
        return vendorCode;
    }

    public void setAlgsVersion(String algsVersion) {
        this.algsVersion = algsVersion;
    }

    public String getAlgsVersion() {
        return algsVersion;
    }

    public void setBatchDate(Date batchDate) {
        this.batchDate = batchDate;
    }

    public Date getBatchDate() {
        return batchDate;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getSceneImageBase64() {
        return sceneImageBase64;
    }

    public void setSceneImageBase64(String sceneImageBase64) {
        this.sceneImageBase64 = sceneImageBase64;
    }

    public String getStockImageBase64() {
        return stockImageBase64;
    }

    public void setStockImageBase64(String stockImageBase64) {
        this.stockImageBase64 = stockImageBase64;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
