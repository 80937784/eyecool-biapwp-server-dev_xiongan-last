package cn.eyecool.system.domain;

import java.util.Date;

import com.alibaba.fastjson.JSON;

import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 租户和接口关联对象 sys_tenant_interface
 * 
 * @author admin
 * @date 2021-02-22
 */
public class SysTenantInterface extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 租户ID */
    private String tenantId;

    /** 接口交易码 */
    private String transcode;

    /** 过期时间 */
    private Date expireTime;

    /** 接口名称 */
    private String transname;

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTranscode(String transcode) {
        this.transcode = transcode;
    }

    public String getTranscode() {
        return transcode;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public String getTransname() {
        return transname;
    }

    public void setTransname(String transname) {
        this.transname = transname;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
