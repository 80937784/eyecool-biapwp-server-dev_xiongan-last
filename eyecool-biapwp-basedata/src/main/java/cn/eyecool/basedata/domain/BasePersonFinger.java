package cn.eyecool.basedata.domain;

import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 指纹图像信息对象 base_person_finger
 * 
 * @author mawj
 * @date 2021-01-27
 */
public class BasePersonFinger extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 关联人员主键(基础信息_人员信息表ID) */
    private String personId;

    /** 人员标识 */
    @Excel(name = "base.person.finger.unique")
    private String uniqueId;

    /** 手指编号 */
    @Excel(name = "base.person.finger.serial.number", dictType = "bio_finger_code")
    private String fingerNo;

    /** 胁迫位 */
    @Excel(name = "base.person.finger.duress.bit")
    private String coercivePosition;

    /** 指纹特征 */
    @Excel(name = "base.person.finger.feature")
    private String feature;

    /** 特征MD5 */
    @Excel(name = "base.person.finger.feature.md5")
    private String featureMd5;

    /** 图像质量得分 */
    @Excel(name = "base.person.finger.quality.score")
    private Double qualityScore;

    /** 指纹图像路径 */
    private String imageUrl;

    /** 厂商 */
    private String vendorCode;

    /** 算法版本 */
    @Excel(name = "base.person.finger.alg.version")
    private String algsVersion;

    /** 是否加密： 1-加密 0-不加密 */
    @Excel(name = "base.person.finger.encrypted", dictType = "apply_encrypted")
    private String encrypted;

    /** 数据来源：INTERFACE-内部接口 IMP-导入 HTTP-HTTP接口 */
    @Excel(name = "base.person.finger.data.source", dictType = "apply_data_source")
    private String datasource;

    /** 状态：0-有效 1:无效 */
    @Excel(name = "base.person.finger.status", dictType = "sys_normal_disable")
    private String status;

    /** 定时任务执行时间(执行定时任务时使用此字段) */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date batchDate;

    /** 租户ID */
    private String tenantId;

    /** 指纹base64 */
    private String imgBase64;

    /** 人员部门信息 */
    private Long deptId;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getPersonId() {
        return personId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setFingerNo(String fingerNo) {
        this.fingerNo = fingerNo;
    }

    public String getFingerNo() {
        return fingerNo;
    }

    public void setCoercivePosition(String coercivePosition) {
        this.coercivePosition = coercivePosition;
    }

    public String getCoercivePosition() {
        return coercivePosition;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeatureMd5(String featureMd5) {
        this.featureMd5 = featureMd5;
    }

    public String getFeatureMd5() {
        return featureMd5;
    }

    public Double getQualityScore() {
        return qualityScore;
    }

    public void setQualityScore(Double qualityScore) {
        this.qualityScore = qualityScore;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
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

    public void setEncrypted(String encrypted) {
        this.encrypted = encrypted;
    }

    public String getEncrypted() {
        return encrypted;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
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

    public String getImgBase64() {
        return imgBase64;
    }

    public void setImgBase64(String imgBase64) {
        this.imgBase64 = imgBase64;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
