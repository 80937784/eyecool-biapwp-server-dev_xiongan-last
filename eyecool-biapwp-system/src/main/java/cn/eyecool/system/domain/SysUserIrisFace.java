package cn.eyecool.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 用户虹膜人脸多模态信息对象 sys_user_iris_face
 * 
 * @author admin
 * @date 2021-05-31
 */
public class SysUserIrisFace extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 用户主键 */
    @Excel(name = "sys.user.irisface.userid")
    private Long userId;

    /** 融合特征 */
    @Excel(name = "sys.user.irisface.fusionfeature")
    private String fusionFeature;

    /** 人脸特征 */
    @Excel(name = "sys.user.face.feature")
    private String faceFeature;

    /** 虹膜特征 */
    @Excel(name = "sys.user.iris.feature")
    private String irisFeature;

    /** 融合特征md5 */
    @Excel(name = "sys.user.irisface.fusionfeature.md5")
    private String fusionFeatureMd5;

    /** 人脸特征md5 */
    @Excel(name = "sys.user.face.featuremd5")
    private String faceFeatureMd5;

    /** 人脸照片路径 */
    @Excel(name = "sys.user.face.imageurl")
    private String faceImageUrl;

    /** 人脸质量分数 */
    @Excel(name = "sys.user.irisface.face.quality")
    private Double faceQuality;

    /** 虹膜特征md5 */
    @Excel(name = "sys.user.iris.featuremd5")
    private String irisFeatureMd5;

    /** 虹膜照片路径 */
    @Excel(name = "虹膜照片路径")
    private String irisImageUrl;

    /** 虹膜质量分数 */
    @Excel(name = "sys.user.iris.imageurl")
    private Double irisQuality;

    /** 状态：0-有效 1-无效 */
    @Excel(name = "sys.user.face.status")
    private String status;

    /** 租户ID */
    private String tenantId;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setFusionFeature(String fusionFeature) {
        this.fusionFeature = fusionFeature;
    }

    public String getFusionFeature() {
        return fusionFeature;
    }

    public void setFaceFeature(String faceFeature) {
        this.faceFeature = faceFeature;
    }

    public String getFaceFeature() {
        return faceFeature;
    }

    public void setIrisFeature(String irisFeature) {
        this.irisFeature = irisFeature;
    }

    public String getIrisFeature() {
        return irisFeature;
    }

    public void setFusionFeatureMd5(String fusionFeatureMd5) {
        this.fusionFeatureMd5 = fusionFeatureMd5;
    }

    public String getFusionFeatureMd5() {
        return fusionFeatureMd5;
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

    public void setFaceQuality(Double faceQuality) {
        this.faceQuality = faceQuality;
    }

    public Double getFaceQuality() {
        return faceQuality;
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

    public void setIrisQuality(Double irisQuality) {
        this.irisQuality = irisQuality;
    }

    public Double getIrisQuality() {
        return irisQuality;
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
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("id", getId())
            .append("userId", getUserId()).append("fusionFeature", getFusionFeature())
            .append("faceFeature", getFaceFeature()).append("irisFeature", getIrisFeature())
            .append("fusionFeatureMd5", getFusionFeatureMd5()).append("faceFeatureMd5", getFaceFeatureMd5())
            .append("faceImageUrl", getFaceImageUrl()).append("faceQuality", getFaceQuality())
            .append("irisFeatureMd5", getIrisFeatureMd5()).append("irisImageUrl", getIrisImageUrl())
            .append("irisQuality", getIrisQuality()).append("status", getStatus()).append("createBy", getCreateBy())
            .append("updateBy", getUpdateBy()).append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime()).append("tenantId", getTenantId()).toString();
    }
}
