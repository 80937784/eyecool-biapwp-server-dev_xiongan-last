package cn.eyecool.scene.trade.entity;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

/**
 * 人脸识别(1:N)参数
 * 
 * @author admin
 * @date 2019年11月14日
 */
public class PersonFaceRecog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务流水号 */
    private String receivedSeq;
    /** 现场照 */
    private String sceneImage;
    /** 是否检活 */
    private String liveDetection;
    /** 场景编码 */
    private String channelCode;
    /** 检活阈值 */
    private String liveDetectionThreshold;
    /** TOP查询数量 */
    private String topN;
    /** 子场景编码 */
    private String subTreasury;
    /** 1-N搜索阈值 */
    private String searchNThreshold;
    /** 现场媒体类型(1:照片，2：视频) */
    private String sceneMediaType;
    /** 现场视频信息 */
    private MultipartFile video;
    /** 设备编码 */
    private String deviceCode;
    /** 设备名称 */
    private String devicName;
    /** 设备IP */
    private String deviceIp;
    /** 设备经度(东经) */
    private String deviceLongitude;
    /** 设备维度(北纬) */
    private String deviceDimension;
    /** 设备方向 */
    private String deviceDirection;
    /** 设备型号编码 */
    private String deviceModel;
    /** 温度（前端测温+后端识别情况，用于日志保存） */
    private String temperature;
    /** 温度阈值下限（前端测温+后端识别情况，用于日志保存） */
    private String temperatureFloor;
    /** 温度阈值上限（前端测温+后端识别情况，用于日志保存） */
    private String temperatureTop;
    /** 现场测温结果(0通过，1未通过) */
    private String temperatureResult;
    /** 比对去重时长(ms) */
    private String duplicateTime;

    public String getReceivedSeq() {
        return receivedSeq;
    }

    public void setReceivedSeq(String receivedSeq) {
        this.receivedSeq = receivedSeq;
    }

    public String getSceneImage() {
        return sceneImage;
    }

    public void setSceneImage(String sceneImage) {
        this.sceneImage = sceneImage;
    }

    public String getLiveDetection() {
        return liveDetection;
    }

    public void setLiveDetection(String liveDetection) {
        this.liveDetection = liveDetection;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getLiveDetectionThreshold() {
        return liveDetectionThreshold;
    }

    public void setLiveDetectionThreshold(String liveDetectionThreshold) {
        this.liveDetectionThreshold = liveDetectionThreshold;
    }

    public String getTopN() {
        return topN;
    }

    public void setTopN(String topN) {
        this.topN = topN;
    }

    public String getSubTreasury() {
        return subTreasury;
    }

    public void setSubTreasury(String subTreasury) {
        this.subTreasury = subTreasury;
    }

    public String getSearchNThreshold() {
        return searchNThreshold;
    }

    public void setSearchNThreshold(String searchNThreshold) {
        this.searchNThreshold = searchNThreshold;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public String getDevicName() {
        return devicName;
    }

    public void setDevicName(String devicName) {
        this.devicName = devicName;
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

    public String getDeviceDirection() {
        return deviceDirection;
    }

    public void setDeviceDirection(String deviceDirection) {
        this.deviceDirection = deviceDirection;
    }

    public String getSceneMediaType() {
        return sceneMediaType;
    }

    public void setSceneMediaType(String sceneMediaType) {
        this.sceneMediaType = sceneMediaType;
    }

    public MultipartFile getVideo() {
        return video;
    }

    public void setVideo(MultipartFile video) {
        this.video = video;
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

    public String getTemperatureResult() {
        return temperatureResult;
    }

    public void setTemperatureResult(String temperatureResult) {
        this.temperatureResult = temperatureResult;
    }

    public String getDuplicateTime() {
        return duplicateTime;
    }

    public void setDuplicateTime(String duplicateTime) {
        this.duplicateTime = duplicateTime;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

}
