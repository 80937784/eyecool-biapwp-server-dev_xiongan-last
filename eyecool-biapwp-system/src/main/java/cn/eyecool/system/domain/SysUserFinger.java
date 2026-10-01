package cn.eyecool.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 用户指纹信息对象 sys_user_finger
 * 
 * @author admin
 * @date 2021-04-16
 */
public class SysUserFinger extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 用户主键(关联人员表) */
    @Excel(name = "sys.user.face.userid")
    private Long userId;

    /** 手指编号 */
    @Excel(name = "sys.user.finger.fingerno")
    private String fingerNo;

    /** 指纹特征 */
    @Excel(name = "sys.user.finger.feature")
    private String feature;

    /** 指纹特征MD5 */
    @Excel(name = "sys.user.finger.featuremd5")
    private String featureMd5;

    /** 图像质量得分 */
    @Excel(name = "sys.user.face.qualityscore")
    private Long qualityScore;

    /** 指纹图像路径 */
    @Excel(name = "sys.user.finger.imageurl")
    private String imageUrl;

    /** 厂商 */
    @Excel(name = "sys.user.face.vendorcode")
    private String vendorCode;

    /** 算法版本 */
    @Excel(name = "sys.user.face.algversion")
    private String algsVersion;

    /** 状态：0-有效 1:无效 */
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

    public void setFingerNo(String fingerNo) {
        this.fingerNo = fingerNo;
    }

    public String getFingerNo() {
        return fingerNo;
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

    public void setQualityScore(Long qualityScore) {
        this.qualityScore = qualityScore;
    }

    public Long getQualityScore() {
        return qualityScore;
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
            .append("userId", getUserId()).append("fingerNo", getFingerNo()).append("feature", getFeature())
            .append("featureMd5", getFeatureMd5()).append("qualityScore", getQualityScore())
            .append("imageUrl", getImageUrl()).append("vendorCode", getVendorCode())
            .append("algsVersion", getAlgsVersion()).append("status", getStatus()).append("remark", getRemark())
            .append("createBy", getCreateBy()).append("updateBy", getUpdateBy()).append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime()).append("tenantId", getTenantId()).toString();
    }
}
