package cn.eyecool.device.domain;

import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 升级日志对象 device_upgrade_log
 * 
 * @author admin
 * @date 2021-04-09
 */
public class DeviceUpgradeLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 任务主键 */
    private String taskId;

    /** 设备编号 */
    @Excel(name = "device.code")
    private String deviceNo;

    /** 设备名称 */
    @Excel(name = "device.name")
    private String deviceName;

    /** 升级前APP名 */
    @Excel(name = "devcie.upgrade.log.before.name")
    private String beforeAppName;

    /** 升级后APP名 */
    @Excel(name = "devcie.upgrade.log.after.name")
    private String afterAppName;

    /** 升级前版本 */
    @Excel(name = "devcie.upgrade.log.before.version")
    private String beforeVersion;

    /** 升级后版本 */
    @Excel(name = "devcie.upgrade.log.after.version")
    private String afterVersion;

    /** 更新状态(1:待下载，2：待更新，3：更新成功，4：更新失败，5：跳过) */
    @Excel(name = "devcie.upgrade.log.upgrade.status", dictType = "device_upgrade_status")
    private String upgradeStatus;

    /** 升级时前端时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "devcie.upgrade.log.upgrade.client.time", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date clientTime;

    /** 升级时后端时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "devcie.upgrade.log.upgrade.server.time", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date serverTime;

    /** 升级耗时(单位s) */
    @Excel(name = "device.upgrade.time.used")
    private Long timeUsed;

    /** 失败原因 */
    @Excel(name = "device.upgrade.faile.reason")
    private String failReason;

    /** 定时任务执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date batchDate;

    /** 租户ID */
    private String tenantId;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setDeviceNo(String deviceNo) {
        this.deviceNo = deviceNo;
    }

    public String getDeviceNo() {
        return deviceNo;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setBeforeAppName(String beforeAppName) {
        this.beforeAppName = beforeAppName;
    }

    public String getBeforeAppName() {
        return beforeAppName;
    }

    public void setAfterAppName(String afterAppName) {
        this.afterAppName = afterAppName;
    }

    public String getAfterAppName() {
        return afterAppName;
    }

    public void setBeforeVersion(String beforeVersion) {
        this.beforeVersion = beforeVersion;
    }

    public String getBeforeVersion() {
        return beforeVersion;
    }

    public void setAfterVersion(String afterVersion) {
        this.afterVersion = afterVersion;
    }

    public String getAfterVersion() {
        return afterVersion;
    }

    public void setUpgradeStatus(String upgradeStatus) {
        this.upgradeStatus = upgradeStatus;
    }

    public String getUpgradeStatus() {
        return upgradeStatus;
    }

    public void setClientTime(Date clientTime) {
        this.clientTime = clientTime;
    }

    public Date getClientTime() {
        return clientTime;
    }

    public void setServerTime(Date serverTime) {
        this.serverTime = serverTime;
    }

    public Date getServerTime() {
        return serverTime;
    }

    public void setTimeUsed(Long timeUsed) {
        this.timeUsed = timeUsed;
    }

    public Long getTimeUsed() {
        return timeUsed;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public String getFailReason() {
        return failReason;
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

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
