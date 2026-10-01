package cn.eyecool.scene.trade.entity;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

/**
 * 人脸1:1认证参数
 * 
 * @author admin
 * @date 2019年11月13日
 */
public class PersonFaceVerify implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 唯一标识 */
    private String uniqueId;
    /** 业务流水号 */
    private String receivedSeq;
    /** 联网核查照 */
    private String onlineImage;
    /** 芯片照 */
    private String chipImage;
    /** 现场照 */
    private String sceneImage;
    /** 是否检活 */
    private String liveDetection;
    /** 场景编码 */
    private String channelCode;
    /** 检活阈值 */
    private String liveDetectionThreshold;
    /** 1:1比对阈值 */
    private String compareThreshold;
    /** 现场媒体类型(1:照片，2：视频) */
    private String sceneMediaType;
    /** 现场视频信息 */
    private MultipartFile video;
    /** 设备编码 */
    private String deviceCode;
    /** 设备名称 */
    private String deviceName;
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

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getReceivedSeq() {
        return receivedSeq;
    }

    public void setReceivedSeq(String receivedSeq) {
        this.receivedSeq = receivedSeq;
    }

    public String getOnlineImage() {
        return onlineImage;
    }

    public void setOnlineImage(String onlineImage) {
        this.onlineImage = onlineImage;
    }

    public String getChipImage() {
        return chipImage;
    }

    public void setChipImage(String chipImage) {
        this.chipImage = chipImage;
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

    public String getCompareThreshold() {
        return compareThreshold;
    }

    public void setCompareThreshold(String compareThreshold) {
        this.compareThreshold = compareThreshold;
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

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
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

}
