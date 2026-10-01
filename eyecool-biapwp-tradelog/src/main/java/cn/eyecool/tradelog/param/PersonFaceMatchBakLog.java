package cn.eyecool.tradelog.param;

import java.io.Serializable;

/**
 * 人脸比对日志回传对象
 * 
 * @author admin
 * @date 2019-11-26
 */
public class PersonFaceMatchBakLog implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 业务流水号 */
    private String receivedSeq;
    /** 健康码日志ID */
    private String healthcodeLogId;
    /** 人员标识 */
    private String uniqueId;
    /** 场景编码 */
    private String channelCode;
    /** 现场照 */
    private String sceneImageBase64;
    /** 联网核查照 */
    private String onlineImageBase64;
    /** 芯片照 */
    private String chipImageBase64;
    /** 底库照 */
    private String stockImageBase64;
    /** 现场照与联网核查照比对分值 */
    private String sceneOnlineScore;
    /** 现场照与芯片照比对分值 */
    private String sceneChipScore;
    /** 现场照与库底库照比对分值 */
    private String sceneStockScore;
    /** 联网核查照与芯片照比对分值 */
    private String onlineChipScore;
    /** 现场照与联网核查照比对结果 */
    private String sceneOnlineResult;
    /** 现场照与芯片照比对结果 */
    private String sceneChipResult;
    /** 现场照与库底库照比对结果 */
    private String sceneStockResult;
    /** 联网核查照与芯片照比对结果 */
    private String onlineChipResult;
    /** 现场照检活分数 */
    private String checkliveScore;
    /** 现场照检活结果 */
    private String checkliveResult;
    /** 比对结果 */
    private String result;
    /** 请求时间 */
    private String receivedTime;
    /** 耗时(ms) */
    private String timeUsed;
    /** 厂商 */
    private String vendorCode;
    /** 算法版本 */
    private String algsVersion;
    /** 设备编码 */
    private String deviceCode;
    /** 温度 */
    private String temperature;
    /** 温度下限 */
    private String temperatureFloor;
    /** 温度上限 */
    private String temperatureTop;
    /** 设备名称 */
    private String deviceName;
    /** 设备IP */
    private String deviceIp;
    /** 设备经度(东经) */
    private String deviceLongitude;
    /** 设备维度(北纬) */
    private String deviceDimension;
    /** 设备型号编码 */
    private String deviceModel;
    /** 设备进出方向 */
    private String deviceDirection;
    /** 设备地址 */
    private String deviceAddr;
    /** 测温结果 */
    private String temperatureResult;

    public String getReceivedSeq() {
        return receivedSeq;
    }

    public void setReceivedSeq(String receivedSeq) {
        this.receivedSeq = receivedSeq;
    }

    public String getHealthcodeLogId() {
        return healthcodeLogId;
    }

    public void setHealthcodeLogId(String healthcodeLogId) {
        this.healthcodeLogId = healthcodeLogId;
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

    public String getSceneImageBase64() {
        return sceneImageBase64;
    }

    public void setSceneImageBase64(String sceneImageBase64) {
        this.sceneImageBase64 = sceneImageBase64;
    }

    public String getOnlineImageBase64() {
        return onlineImageBase64;
    }

    public void setOnlineImageBase64(String onlineImageBase64) {
        this.onlineImageBase64 = onlineImageBase64;
    }

    public String getChipImageBase64() {
        return chipImageBase64;
    }

    public void setChipImageBase64(String chipImageBase64) {
        this.chipImageBase64 = chipImageBase64;
    }

    public String getStockImageBase64() {
        return stockImageBase64;
    }

    public void setStockImageBase64(String stockImageBase64) {
        this.stockImageBase64 = stockImageBase64;
    }

    public String getSceneOnlineScore() {
        return sceneOnlineScore;
    }

    public void setSceneOnlineScore(String sceneOnlineScore) {
        this.sceneOnlineScore = sceneOnlineScore;
    }

    public String getSceneChipScore() {
        return sceneChipScore;
    }

    public void setSceneChipScore(String sceneChipScore) {
        this.sceneChipScore = sceneChipScore;
    }

    public String getSceneStockScore() {
        return sceneStockScore;
    }

    public void setSceneStockScore(String sceneStockScore) {
        this.sceneStockScore = sceneStockScore;
    }

    public String getOnlineChipScore() {
        return onlineChipScore;
    }

    public void setOnlineChipScore(String onlineChipScore) {
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

    public String getCheckliveScore() {
        return checkliveScore;
    }

    public void setCheckliveScore(String checkliveScore) {
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

    public String getReceivedTime() {
        return receivedTime;
    }

    public void setReceivedTime(String receivedTime) {
        this.receivedTime = receivedTime;
    }

    public String getTimeUsed() {
        return timeUsed;
    }

    public void setTimeUsed(String timeUsed) {
        this.timeUsed = timeUsed;
    }

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public String getAlgsVersion() {
        return algsVersion;
    }

    public void setAlgsVersion(String algsVersion) {
        this.algsVersion = algsVersion;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getTemperatureFloor() {
        return temperatureFloor;
    }

    public void setTemperatureFloor(String temperatureFloor) {
        this.temperatureFloor = temperatureFloor;
    }

    public String getTemperatureTop() {
        return temperatureTop;
    }

    public void setTemperatureTop(String temperatureTop) {
        this.temperatureTop = temperatureTop;
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

    public String getDeviceLongitude() {
        return deviceLongitude;
    }

    public void setDeviceLongitude(String deviceLongitude) {
        this.deviceLongitude = deviceLongitude;
    }

    public String getDeviceDimension() {
        return deviceDimension;
    }

    public void setDeviceDimension(String deviceDimension) {
        this.deviceDimension = deviceDimension;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getDeviceDirection() {
        return deviceDirection;
    }

    public void setDeviceDirection(String deviceDirection) {
        this.deviceDirection = deviceDirection;
    }

    public String getDeviceAddr() {
        return deviceAddr;
    }

    public void setDeviceAddr(String deviceAddr) {
        this.deviceAddr = deviceAddr;
    }

    public String getTemperatureResult() {
        return temperatureResult;
    }

    public void setTemperatureResult(String temperatureResult) {
        this.temperatureResult = temperatureResult;
    }
}
