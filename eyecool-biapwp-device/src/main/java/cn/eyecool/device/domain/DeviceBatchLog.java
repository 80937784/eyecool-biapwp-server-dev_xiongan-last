package cn.eyecool.device.domain;

import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 设备批次对象 device_batch_log
 * 
 * @author admin
 * @date 2021-04-07
 */
public class DeviceBatchLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 批次号 */
    @Excel(name = "device.batch.num")
    private String batchNum;

    /** 批次说明 */
    @Excel(name = "device.batch.description")
    private String batchDesc;

    /** 是否回滚 */
    @Excel(name = "device.batch.rollback")
    private Boolean rollbacked;

    /** 回滚时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "device.batch.rollback.time", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date rollbackTime;

    /** 导入租户ID */
    private String tenantId;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setBatchNum(String batchNum) {
        this.batchNum = batchNum;
    }

    public String getBatchNum() {
        return batchNum;
    }

    public void setBatchDesc(String batchDesc) {
        this.batchDesc = batchDesc;
    }

    public String getBatchDesc() {
        return batchDesc;
    }

    public Boolean getRollbacked() {
        return rollbacked;
    }

    public void setRollbacked(Boolean rollbacked) {
        this.rollbacked = rollbacked;
    }

    public void setRollbackTime(Date rollbackTime) {
        this.rollbackTime = rollbackTime;
    }

    public Date getRollbackTime() {
        return rollbackTime;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
