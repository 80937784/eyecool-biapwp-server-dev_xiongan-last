package cn.eyecool.device.domain;

import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 升级任务对象 device_upgrade_task
 * 
 * @author admin
 * @date 2021-04-08
 */
public class DeviceUpgradeTask extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 版本主键 */
    private String versionId;

    /** 设备主键 */
    private String deviceId;

    /** 设备编号SN */
    @Excel(name = "device.code")
    private String deviceNo;

    /** 设备名称 */
    @Excel(name = "device.name")
    private String deviceName;

    /** APP名称 */
    @Excel(name = "device.upgrade.app.name")
    private String appName;

    /** APP版本 */
    @Excel(name = "device.upgrade.app.version")
    private String appVersion;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "device.upgrade.time", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date upgradeTime;

    /** 升级次数上限 */
    @Excel(name = "device.upgrade.upper.limit")
    private Long upgradeCountLimit;

    /** 执行次数 */
    @Excel(name = "device.upgrade.execute.count")
    private Long executeCount;

    /** 发布次数 */
    @Excel(name = "device.upgrade.publish.count")
    private Long pubCount;

    /** 任务排序 */
    @Excel(name = "device.upgrade.task.sort")
    private Long taskIndex;

    /** 执行结果(1:待执行，2：成功，3：失败，4：跳过) */
    @Excel(name = "device.upgrade.execute.result", dictType = "device_upgrade_result")
    private String executeResult;

    /** 是否降级安装 */
    @Excel(name = "device.upgrade.rollback.install")
    private Boolean rollbackInstall;

    /** 定时任务执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date batchDate;

    /** 租户ID */
    private String tenantId;

    /** 版本文件MD5 */
    private String md5;

    /** 版本文件大小（B） */
    private Long fileSize;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceNo() {
        return deviceNo;
    }

    public void setDeviceNo(String deviceNo) {
        this.deviceNo = deviceNo;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public void setUpgradeTime(Date upgradeTime) {
        this.upgradeTime = upgradeTime;
    }

    public Date getUpgradeTime() {
        return upgradeTime;
    }

    public void setUpgradeCountLimit(Long upgradeCountLimit) {
        this.upgradeCountLimit = upgradeCountLimit;
    }

    public Long getUpgradeCountLimit() {
        return upgradeCountLimit;
    }

    public void setExecuteCount(Long executeCount) {
        this.executeCount = executeCount;
    }

    public Long getExecuteCount() {
        return executeCount;
    }

    public void setPubCount(Long pubCount) {
        this.pubCount = pubCount;
    }

    public Long getPubCount() {
        return pubCount;
    }

    public void setTaskIndex(Long taskIndex) {
        this.taskIndex = taskIndex;
    }

    public Long getTaskIndex() {
        return taskIndex;
    }

    public void setExecuteResult(String executeResult) {
        this.executeResult = executeResult;
    }

    public String getExecuteResult() {
        return executeResult;
    }

    public Boolean getRollbackInstall() {
        return rollbackInstall;
    }

    public void setRollbackInstall(Boolean rollbackInstall) {
        this.rollbackInstall = rollbackInstall;
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

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
