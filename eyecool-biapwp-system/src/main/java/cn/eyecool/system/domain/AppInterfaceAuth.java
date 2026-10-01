package cn.eyecool.system.domain;

import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 应用接口授权对象 app_interface_auth
 * 
 * @author admin
 * @date 2021-04-14
 */
public class AppInterfaceAuth extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 应用主键 */
    private String appId;

    /** 应用名称 */
    @Excel(name = " app.info.appdesc.name")
    private String appDesc;

    /** 接口交易码 */
    @Excel(name = "app.auth.transcode.name")
    private String transCode;

    /** 交易有效截止时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "app.auth.transendtime.name", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date transEndTime;

    /** 租户ID */
    private String tenantId;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppId() {
        return appId;
    }

    public String getAppDesc() {
        return appDesc;
    }

    public void setAppDesc(String appDesc) {
        this.appDesc = appDesc;
    }

    public void setTransCode(String transCode) {
        this.transCode = transCode;
    }

    public String getTransCode() {
        return transCode;
    }

    public void setTransEndTime(Date transEndTime) {
        this.transEndTime = transEndTime;
    }

    public Date getTransEndTime() {
        return transEndTime;
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
