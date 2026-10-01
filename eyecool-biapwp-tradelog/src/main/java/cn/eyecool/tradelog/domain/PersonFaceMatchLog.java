package cn.eyecool.tradelog.domain;

import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.annotation.Excel.Type;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 人脸比对日志对象 person_face_match_log
 * 
 * @author admin
 * @date 2021-04-29
 */
public class PersonFaceMatchLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 业务流水号 */
    @Excel(name = "domain.person.faceiris.busi.req")
    private String receivedSeq;

    /** 健康码日志ID */
    private String healthcodeLogId;

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

    /** 现场照路径 */
    private String sceneImage;

    /** 联网核查照路径 */
    private String onlineImage;

    /** 芯片照路径 */
    private String chipImage;

    /** 底库照路径 */
    private String stockImage;

    /** 现场视频路径 */
    private String sceneVideo;

    /** 现场照与联网核查照比对分值 */
    @Excel(name = "domain.person.face.sceneonlinescore")
    private Double sceneOnlineScore;

    /** 现场照与芯片照比对分值 */
    @Excel(name = "domain.person.face.scenechipscore")
    private Double sceneChipScore;

    /** 现场照与库底库照比对分值 */
    @Excel(name = "domain.person.face.scenestockscore")
    private Double sceneStockScore;

    /** 联网核查照与芯片照比对分值 */
    @Excel(name = "domain.person.face.onlinechipscore")
    private Double onlineChipScore;

    /** 检活分值 */
    @Excel(name = "domain.person.faceiris.checklive.score")
    private Double checkliveScore;

    /** 现场照与联网核查照比对结果(0通过，1未通过) */
    @Excel(name = "domain.person.face.sceneonlineresult", dictType = "bio_result")
    private String sceneOnlineResult;

    /** 现场照与芯片照比对结果(0通过，1未通过) */
    @Excel(name = "domain.person.face.scenechipresult", dictType = "bio_result")
    private String sceneChipResult;

    /** 现场照与库底库照比对结果(0通过，1未通过) */
    @Excel(name = "domain.person.finger.scenestockresult", dictType = "bio_result")
    private String sceneStockResult;

    /** 联网核查照与芯片照比对结果(0通过，1未通过) */
    @Excel(name = "domain.person.face.onlinechipresult", dictType = "bio_result")
    private String onlineChipResult;

    /** 现场照件检活结果(0通过，1未通过) */
    @Excel(name = "domain.person.face.checkliveresult", dictType = "bio_result")
    private String checkliveResult;

    /** 比对结果(0通过，1未通过) */
    @Excel(name = "domain.person.faceiris.match.result", dictType = "bio_result")
    private String result;

    /** 温度 */
    @Excel(name = "domain.person.faceiris.temperature")
    private Double temperature;

    /** 温度阈值下限 */
    @Excel(name = "domain.person.faceiris.temperature.floor")
    private Double temperatureFloor;

    /** 温度阈值上限 */
    @Excel(name = "domain.person.faceiris.temperature.top")
    private Double temperatureTop;

    /** 设备编码 */
    @Excel(name = "domain.person.faceiris.devicesn")
    private String deviceCode;

    /** 设备名称 */
    @Excel(name = "domain.person.faceiris.devicename")
    private String deviceName;

    /** 设备型号编码 */
    @Excel(name = "domain.person.faceiris.devicemodel")
    private String deviceModel;

    /** 设备IP */
    @Excel(name = "domain.person.faceiris.deviceip")
    private String deviceIp;

    /** 设备经度(东经) */
    @Excel(name = "domain.person.faceiris.device.longitude")
    private Double deviceLongitude;

    /** 设备维度(北纬) */
    @Excel(name = "domain.person.faceiris.device.latitue")
    private Double deviceDimension;

    /** 设备方向(IN:进，OUT:出，UNKNOWN:未知) */
    @Excel(name = "domain.person.faceiris.device.direction", dictType = "device_direction")
    private String deviceDirection;

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

    /** 联网核查照Base64 */
    private String onlineImageBase64;

    /** 芯片照Base64 */
    private String chipImageBase64;

    /** 底库照Base64 */
    private String stockImageBase64;

    /** 健康码查询日志 */
    private PersonHealthCodeLog healthCodeLog;

    /** 现场照路径编码 */
    private String sceneImageId;

    /** 设备地址 */
    private String deviceAddr;

    /** 测温结果 */
    private String temperatureResult;

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

    public void setHealthcodeLogId(String healthcodeLogId) {
        this.healthcodeLogId = healthcodeLogId;
    }

    public String getHealthcodeLogId() {
        return healthcodeLogId;
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

    public void setSceneImage(String sceneImage) {
        this.sceneImage = sceneImage;
    }

    public String getSceneImage() {
        return sceneImage;
    }

    public void setOnlineImage(String onlineImage) {
        this.onlineImage = onlineImage;
    }

    public String getOnlineImage() {
        return onlineImage;
    }

    public void setChipImage(String chipImage) {
        this.chipImage = chipImage;
    }

    public String getChipImage() {
        return chipImage;
    }

    public void setStockImage(String stockImage) {
        this.stockImage = stockImage;
    }

    public String getStockImage() {
        return stockImage;
    }

    public void setSceneVideo(String sceneVideo) {
        this.sceneVideo = sceneVideo;
    }

    public String getSceneVideo() {
        return sceneVideo;
    }

    public void setSceneOnlineScore(Double sceneOnlineScore) {
        this.sceneOnlineScore = sceneOnlineScore;
    }

    public Double getSceneOnlineScore() {
        return sceneOnlineScore;
    }

    public void setSceneChipScore(Double sceneChipScore) {
        this.sceneChipScore = sceneChipScore;
    }

    public Double getSceneChipScore() {
        return sceneChipScore;
    }

    public void setSceneStockScore(Double sceneStockScore) {
        this.sceneStockScore = sceneStockScore;
    }

    public Double getSceneStockScore() {
        return sceneStockScore;
    }

    public void setOnlineChipScore(Double onlineChipScore) {
        this.onlineChipScore = onlineChipScore;
    }

    public Double getOnlineChipScore() {
        return onlineChipScore;
    }

    public void setCheckliveScore(Double checkliveScore) {
        this.checkliveScore = checkliveScore;
    }

    public Double getCheckliveScore() {
        return checkliveScore;
    }

    public void setSceneOnlineResult(String sceneOnlineResult) {
        this.sceneOnlineResult = sceneOnlineResult;
    }

    public String getSceneOnlineResult() {
        return sceneOnlineResult;
    }

    public void setSceneChipResult(String sceneChipResult) {
        this.sceneChipResult = sceneChipResult;
    }

    public String getSceneChipResult() {
        return sceneChipResult;
    }

    public void setSceneStockResult(String sceneStockResult) {
        this.sceneStockResult = sceneStockResult;
    }

    public String getSceneStockResult() {
        return sceneStockResult;
    }

    public void setOnlineChipResult(String onlineChipResult) {
        this.onlineChipResult = onlineChipResult;
    }

    public String getOnlineChipResult() {
        return onlineChipResult;
    }

    public void setCheckliveResult(String checkliveResult) {
        this.checkliveResult = checkliveResult;
    }

    public String getCheckliveResult() {
        return checkliveResult;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperatureFloor(Double temperatureFloor) {
        this.temperatureFloor = temperatureFloor;
    }

    public Double getTemperatureFloor() {
        return temperatureFloor;
    }

    public void setTemperatureTop(Double temperatureTop) {
        this.temperatureTop = temperatureTop;
    }

    public Double getTemperatureTop() {
        return temperatureTop;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceIp(String deviceIp) {
        this.deviceIp = deviceIp;
    }

    public String getDeviceIp() {
        return deviceIp;
    }

    public void setDeviceLongitude(Double deviceLongitude) {
        this.deviceLongitude = deviceLongitude;
    }

    public Double getDeviceLongitude() {
        return deviceLongitude;
    }

    public void setDeviceDimension(Double deviceDimension) {
        this.deviceDimension = deviceDimension;
    }

    public Double getDeviceDimension() {
        return deviceDimension;
    }

    public void setDeviceDirection(String deviceDirection) {
        this.deviceDirection = deviceDirection;
    }

    public String getDeviceDirection() {
        return deviceDirection;
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

    public PersonHealthCodeLog getHealthCodeLog() {
        return healthCodeLog;
    }

    public void setHealthCodeLog(PersonHealthCodeLog healthCodeLog) {
        this.healthCodeLog = healthCodeLog;
    }

    public String getSceneImageId() {
        return sceneImageId;
    }

    public void setSceneImageId(String sceneImageId) {
        this.sceneImageId = sceneImageId;
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

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
