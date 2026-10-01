package cn.eyecool.device.domain;

import com.alibaba.fastjson.JSON;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 设备型号信息对象 client_device_model
 * 
 * @author admin
 * @date 2021-03-29
 */
public class DeviceModel extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 设备型号编码 */
    @Excel(name = "device.model.code")
    private String modelCode;

    /** 设备型号名称 */
    @Excel(name = "device.model.name")
    private String modelName;

    /** 设备外观图片 */
    private String exteriorImage;

    /** 设备型号描述 */
    @Excel(name = "device.model.description")
    private String modelDesc;

    /** 租户ID */
    private String tenantId;

    /** 设备外观图片Base64 */
    private String imageBase64;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setModelCode(String modelCode) {
        this.modelCode = modelCode;
    }

    public String getModelCode() {
        return modelCode;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelName() {
        return modelName;
    }

    public void setExteriorImage(String exteriorImage) {
        this.exteriorImage = exteriorImage;
    }

    public String getExteriorImage() {
        return exteriorImage;
    }

    public void setModelDesc(String modelDesc) {
        this.modelDesc = modelDesc;
    }

    public String getModelDesc() {
        return modelDesc;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
