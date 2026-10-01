package cn.eyecool.scene.trade.entity;

import java.io.Serializable;

import com.alibaba.fastjson.JSON;

/**
 * 身份核验对象
 * 
 * @author admin
 * @date 2021-03-02
 */
public class PersonIdentityVerification implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 业务流水号 */
    private String receivedSeq;
    /** 人员标识 */
    private String uniqueId;
    /** 姓名 */
    private String name;
    /** 现场照 */
    private String sceneImageBase64;
    /** 温度 */
    private String temperature;
    /** 温度下限 */
    private String temperatureFloor;
    /** 温度上限 */
    private String temperatureTop;
    /** 现场测温结果(0通过，1未通过) */
    private String temperatureResult;
    /** 场景编码 */
    private String channelCode;

    /** 设备编码 */
    private String deviceCode;
    /** 设备型号 */
    private String deviceModel;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSceneImageBase64() {
        return sceneImageBase64;
    }

    public void setSceneImageBase64(String sceneImageBase64) {
        this.sceneImageBase64 = sceneImageBase64;
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

    public String getTemperatureResult() {
        return temperatureResult;
    }

    public void setTemperatureResult(String temperatureResult) {
        this.temperatureResult = temperatureResult;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
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

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

}