package cn.eyecool.tradelog.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * 人脸1v1比对日志VO
 * 
 * @author mawj
 * @date 2021/04/21
 */
public class PersonFaceMatchLogVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String logId;

    /** 业务流水号 */
    private String receivedSeq;

    /** 人员标识 */
    private String uniqueId;

    /** 场景编码 */
    private String channelCode;

    /** 现场照路径 */
    private String sceneImageId;

    /** 联网核查照路径 */
    private String onlineImageId;

    /** 芯片照路径 */
    private String chipImageId;

    /** 底库照路径 */
    private String stockImageId;

    /** 现场照与联网核查照比对分值 */
    private Double sceneOnlineScore;

    /** 现场照与芯片照比对分值 */
    private Double sceneChipScore;

    /** 现场照与库底库照比对分值 */
    private Double sceneStockScore;

    /** 联网核查照与芯片照比对分值 */
    private Double onlineChipScore;

    /** 现场照与联网核查照比对结果 */
    private String sceneOnlineResult;

    /** 现场照与芯片照比对结果 */
    private String sceneChipResult;

    /** 现场照与库底库照比对结果 */
    private String sceneStockResult;

    /** 联网核查照与芯片照比对结果 */
    private String onlineChipResult;

    /** 现场照件检活得分 */
    private Double checkliveScore;

    /** 现场照检活结果 */
    private String checkliveResult;

    /** 比对结果 */
    private String result;

    /** 请求时间 */
    private Date receivedTime;

    /** 耗时(ms) */
    private Long timeUsed;

    /** 设备编码 */
    private String deviceCode;

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

    public String getSceneImageId() {
        return sceneImageId;
    }

    public void setSceneImageId(String sceneImageId) {
        this.sceneImageId = sceneImageId;
    }

    public String getOnlineImageId() {
        return onlineImageId;
    }

    public void setOnlineImageId(String onlineImageId) {
        this.onlineImageId = onlineImageId;
    }

    public String getChipImageId() {
        return chipImageId;
    }

    public void setChipImageId(String chipImageId) {
        this.chipImageId = chipImageId;
    }

    public String getStockImageId() {
        return stockImageId;
    }

    public void setStockImageId(String stockImageId) {
        this.stockImageId = stockImageId;
    }

    public Double getSceneOnlineScore() {
        return sceneOnlineScore;
    }

    public void setSceneOnlineScore(Double sceneOnlineScore) {
        this.sceneOnlineScore = sceneOnlineScore;
    }

    public Double getSceneChipScore() {
        return sceneChipScore;
    }

    public void setSceneChipScore(Double sceneChipScore) {
        this.sceneChipScore = sceneChipScore;
    }

    public Double getSceneStockScore() {
        return sceneStockScore;
    }

    public void setSceneStockScore(Double sceneStockScore) {
        this.sceneStockScore = sceneStockScore;
    }

    public Double getOnlineChipScore() {
        return onlineChipScore;
    }

    public void setOnlineChipScore(Double onlineChipScore) {
        this.onlineChipScore = onlineChipScore;
    }

    public String getSceneOnlineResult() {
        return sceneOnlineResult;
    }

    public void setSceneOnlineResult(String sceneOnlineResult) {
        this.sceneOnlineResult = sceneOnlineResult;
    }

    public String getSceneChipResult() {
        return sceneChipResult;
    }

    public void setSceneChipResult(String sceneChipResult) {
        this.sceneChipResult = sceneChipResult;
    }

    public String getSceneStockResult() {
        return sceneStockResult;
    }

    public void setSceneStockResult(String sceneStockResult) {
        this.sceneStockResult = sceneStockResult;
    }

    public String getOnlineChipResult() {
        return onlineChipResult;
    }

    public void setOnlineChipResult(String onlineChipResult) {
        this.onlineChipResult = onlineChipResult;
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

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

}
