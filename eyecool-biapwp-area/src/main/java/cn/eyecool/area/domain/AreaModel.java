package cn.eyecool.area.domain;

import com.alibaba.fastjson.JSON;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.TreeEntity;

/**
 * 区域对象 area_model
 * 
 * @author admin
 * @date 2021-03-26
 */
public class AreaModel extends TreeEntity {
    private static final long serialVersionUID = 1L;

    /** 区域id */
    private Long id;

    /** 区域名称 */
    @Excel(name = "area.name")
    private String areaName;

    /** 区域类型 */
    @Excel(name = "area.type", dictType = "area_type")
    private String areaType;

    /** 区域状态（0正常 1停用） */
    @Excel(name = "area.status", readConverterExp = "0=area.status.normal,1=area.status.disabled")
    private String status;

    /** 租户ID */
    private String tenantId;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaType(String areaType) {
        this.areaType = areaType;
    }

    public String getAreaType() {
        return areaType;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
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
