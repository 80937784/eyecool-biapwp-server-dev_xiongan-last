package cn.eyecool.tradelog.vo;

import java.io.Serializable;
import java.util.Date;

import com.alibaba.fastjson.JSON;

/**
 * 人脸搜索日志VO
 * 
 * @author admin
 * @date 2019-11-27
 */
public class PersonFaceSearchLogVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String logId;

    /** 业务流水号 */
    private String receivedSeq;

    /** 人员标识 */
    private String uniqueId;

    /** 场景编码 */
    private String channelCode;

    /** 子场景编码 */
    private String subTreasuryCode;

    /** 子场景名称 */
    private String subTreasuryName;

    /** 现场照ID */
    private String sceneImageId;

    /** 底库照ID */
    private String stockImageId;

    /** 抓拍图1ID */
    private String takePhoto1Id;

    /** 抓拍图2ID */
    private String takePhoto2Id;

    /** 分值 */
    private Double sceneStockScore;

    /** 现场照件检活得分 */
    private Double checkliveScore;

    /** 现场照件检活结果(0;通过，1未通过) */
    private String checkliveResult;

    /** 比对结果(0;通过，1未通过) */
    private String result;

    /** 设备编码 */
    private String deviceCode;

    /** 设备名称 */
    private String deviceName;

    /** 设备IP */
    private String deviceIp;

    /** 设备经度 */
    private Double deviceLongitude;

    /** 设备维度 */
    private Double deviceDimension;

    /** 请求时间 */
    private Date receivedTime;

    /** 耗时(ms) */
    private Long timeUsed;

    /** 温度 */
    private Double temperature;

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getReceivedSeq() {
        return receivedSeq;
    }

    public void setReceivedSeq(String receivedSeq) {
        this.receivedSeq = receivedSeq;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getSubTreasuryCode() {
        return subTreasuryCode;
    }

    public void setSubTreasuryCode(String subTreasuryCode) {
        this.subTreasuryCode = subTreasuryCode;
    }

    public String getSubTreasuryName() {
        return subTreasuryName;
    }

    public void setSubTreasuryName(String subTreasuryName) {
        this.subTreasuryName = subTreasuryName;
    }

    public String getSceneImageId() {
        return sceneImageId;
    }

    public void setSceneImageId(String sceneImageId) {
        this.sceneImageId = sceneImageId;
    }

    public String getStockImageId() {
        return stockImageId;
    }

    public void setStockImageId(String stockImageId) {
        this.stockImageId = stockImageId;
    }

    public String getTakePhoto1Id() {
        return takePhoto1Id;
    }

    public void setTakePhoto1Id(String takePhoto1Id) {
        this.takePhoto1Id = takePhoto1Id;
    }

    public String getTakePhoto2Id() {
        return takePhoto2Id;
    }

    public void setTakePhoto2Id(String takePhoto2Id) {
        this.takePhoto2Id = takePhoto2Id;
    }

    public Double getSceneStockScore() {
        return sceneStockScore;
    }

    public void setSceneStockScore(Double sceneStockScore) {
        this.sceneStockScore = sceneStockScore;
    }

    public Double getCheckliveScore() {
        return checkliveScore;
    }

    public void setCheckliveScore(Double checkliveScore) {
        this.checkliveScore = checkliveScore;
    }

    public String getCheckliveResult() {
        return checkliveResult;
    }

    public void setCheckliveResult(String checkliveResult) {
        this.checkliveResult = checkliveResult;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceIp() {
        return deviceIp;
    }

    public void setDeviceIp(String deviceIp) {
        this.deviceIp = deviceIp;
    }

    public Double getDeviceLongitude() {
        return deviceLongitude;
    }

    public void setDeviceLongitude(Double deviceLongitude) {
        this.deviceLongitude = deviceLongitude;
    }

    public Double getDeviceDimension() {
        return deviceDimension;
    }

    public void setDeviceDimension(Double deviceDimension) {
        this.deviceDimension = deviceDimension;
    }

    public Date getReceivedTime() {
        return receivedTime;
    }

    public void setReceivedTime(Date receivedTime) {
        this.receivedTime = receivedTime;
    }

    public Long getTimeUsed() {
        return timeUsed;
    }

    public void setTimeUsed(Long timeUsed) {
        this.timeUsed = timeUsed;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
