/*******************************************************************************
 * 系统名称 ： 后台管理系统 开发部门 ： 山东眼神智能科技有限公司 文件名称 : PersonMulitSearchVO
 ******************************************************************************/
package cn.eyecool.tradelog.vo;

import java.util.Date;

/**
 * 多模态日志查询返回对象
 *
 * @author zfx
 * @since 2021/2/26 10:12
 **/
public class PersonMulitSearchVO {
    /** 主键 */
    private String logId;
    /** 业务流水号 */
    private String receivedSeq;
    /** 现场人脸照ID 预留 */
    private String sceneFirImageId;
    /** 现场虹膜照ID 预留 */
    private String sceneSecImageId;
    /** 底库人脸照ID */
    private String stockFirImageId;
    /** 底库虹膜照ID */
    private String stockSecImageId;
    /** 分值 */
    private Double sceneStockScore;
    /** 人员标识 */
    private String uniqueId;
    /** 比对结果(1：通过，0未通过) */
    private String result;
    /** 场景编码 */
    private String channelCode;
    /** 子场景编码 */
    private String subTreasuryCode;
    /** 子场景名称 */
    private String subTreasuryName;
    /** 比对模式 */
    private String matchMode;
    /** 设备编码 */
    private String deviceCode;
    /** 设备名称 */
    private String deviceName;
    /** 请求时间 */
    private Date receivedTime;
    /** 耗时(ms) */
    private Long timeUsed;
    /** 温度 */
    private Double temperature;
    /** 设备型号 */
    private String deviceModel;

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

    public String getSceneFirImageId() {
        return sceneFirImageId;
    }

    public void setSceneFirImageId(String sceneFirImageId) {
        this.sceneFirImageId = sceneFirImageId;
    }

    public String getSceneSecImageId() {
        return sceneSecImageId;
    }

    public void setSceneSecImageId(String sceneSecImageId) {
        this.sceneSecImageId = sceneSecImageId;
    }

    public String getStockFirImageId() {
        return stockFirImageId;
    }

    public void setStockFirImageId(String stockFirImageId) {
        this.stockFirImageId = stockFirImageId;
    }

    public String getStockSecImageId() {
        return stockSecImageId;
    }

    public void setStockSecImageId(String stockSecImageId) {
        this.stockSecImageId = stockSecImageId;
    }

    public Double getSceneStockScore() {
        return sceneStockScore;
    }

    public void setSceneStockScore(Double sceneStockScore) {
        this.sceneStockScore = sceneStockScore;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
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

    public String getMatchMode() {
        return matchMode;
    }

    public void setMatchMode(String matchMode) {
        this.matchMode = matchMode;
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

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

}
