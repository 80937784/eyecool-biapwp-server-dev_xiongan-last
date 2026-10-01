package cn.eyecool.basedata.domain;

import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 虹膜人脸多模态对象 base_person_iris_face
 * 
 * @author mawj
 * @date 2021-01-27
 */
public class BasePersonIrisFace extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 关联人员主键(基础信息_人员信息表ID) */
    private String personId;

    /** 人员标识 */
    @Excel(name = "base.person.iris.face.unique")
    private String uniqueId;

    /** 多模态融合特征 */
    @Excel(name = "base.person.iris.face.fusion.feature")
    private String fusionFeature;

    /** 多模态特征md5 */
    @Excel(name = "base.person.iris.face.fusion.feature.md5")
    private String fusionFeatureMd5;

    /** 多模态人脸特征 */
    @Excel(name = "base.person.iris.face.face.feature")
    private String faceFeature;

    /** 多模态人脸特征md5 */
    @Excel(name = "base.person.iris.face.face.feature.md5")
    private String faceFeatureMd5;

    /** 人脸照片路径 */
    private String faceImageUrl;

    /** 人脸照片质量分数 */
    @Excel(name = "base.person.iris.face.face.quality.score")
    private Double faceQuality;

    /** 多模态虹膜特征 */
    @Excel(name = "base.person.iris.face.iris.feature")
    private String irisFeature;

    /** 多模态虹膜特征md5 */
    @Excel(name = "base.person.iris.face.iris.feature.md5")
    private String irisFeatureMd5;

    /** 虹膜照片路径 */
    private String irisImageUrl;

    /** 虹膜照片质量分数 */
    @Excel(name = "base.person.iris.face.iris.quality.score")
    private Double irisQuality;

    /** 是否加密： 1-加密 0-不加密 */
    @Excel(name = "base.person.iris.face.iris.encrypted", dictType = "apply_encrypted")
    private String encrypted;

    /** 状态：0-有效 1:无效 */
    @Excel(name = "base.person.iris.face.iris.status", dictType = "sys_normal_disable")
    private String status;

    /** 数据来源：INTERFACE-内部接口 IMP-导入 HTTP-HTTP接口 */
    @Excel(name = "base.person.iris.face.iris.data.source", dictType = "apply_data_source")
    private String datasource;

    /** 有效期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date validityDate;

    /** 数据描述 数据来源于哪个采集设备等 */
    @Excel(name = "base.person.iris.face.iris.data.description")
    private String dataDescribe;

    /** 租户ID */
    private String tenantId;

    /** 人脸base64 */
    private String faceImgBase64;

    /** 虹膜base64 */
    private String irisImgBase64;

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

    public void setFusionFeature(String fusionFeature) {
        this.fusionFeature = fusionFeature;
    }

    public String getFusionFeature() {
        return fusionFeature;
    }

    public void setFusionFeatureMd5(String fusionFeatureMd5) {
        this.fusionFeatureMd5 = fusionFeatureMd5;
    }

    public String getFusionFeatureMd5() {
        return fusionFeatureMd5;
    }

    public void setFaceFeature(String faceFeature) {
        this.faceFeature = faceFeature;
    }

    public String getFaceFeature() {
        return faceFeature;
    }

    public void setFaceFeatureMd5(String faceFeatureMd5) {
        this.faceFeatureMd5 = faceFeatureMd5;
    }

    public String getFaceFeatureMd5() {
        return faceFeatureMd5;
    }

    public void setFaceImageUrl(String faceImageUrl) {
        this.faceImageUrl = faceImageUrl;
    }

    public String getFaceImageUrl() {
        return faceImageUrl;
    }

    public void setIrisFeature(String irisFeature) {
        this.irisFeature = irisFeature;
    }

    public String getIrisFeature() {
        return irisFeature;
    }

    public void setIrisFeatureMd5(String irisFeatureMd5) {
        this.irisFeatureMd5 = irisFeatureMd5;
    }

    public String getIrisFeatureMd5() {
        return irisFeatureMd5;
    }

    public void setIrisImageUrl(String irisImageUrl) {
        this.irisImageUrl = irisImageUrl;
    }

    public String getIrisImageUrl() {
        return irisImageUrl;
    }

    public Double getFaceQuality() {
        return faceQuality;
    }

    public void setFaceQuality(Double faceQuality) {
        this.faceQuality = faceQuality;
    }

    public Double getIrisQuality() {
        return irisQuality;
    }

    public void setIrisQuality(Double irisQuality) {
        this.irisQuality = irisQuality;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEncrypted() {
        return encrypted;
    }

    public void setEncrypted(String encrypted) {
        this.encrypted = encrypted;
    }

    public String getStatus() {
        return status;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDataDescribe(String dataDescribe) {
        this.dataDescribe = dataDescribe;
    }

    public String getDataDescribe() {
        return dataDescribe;
    }

    public void setValidityDate(Date validityDate) {
        this.validityDate = validityDate;
    }

    public Date getValidityDate() {
        return validityDate;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getFaceImgBase64() {
        return faceImgBase64;
    }

    public void setFaceImgBase64(String faceImgBase64) {
        this.faceImgBase64 = faceImgBase64;
    }

    public String getIrisImgBase64() {
        return irisImgBase64;
    }

    public void setIrisImgBase64(String irisImgBase64) {
        this.irisImgBase64 = irisImgBase64;
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
