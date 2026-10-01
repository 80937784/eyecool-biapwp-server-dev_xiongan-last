package cn.eyecool.basedata.vo;

import java.io.Serializable;

/**
 * 人员虹膜VO
 * 
 * @author admin
 * @date 2019年11月12日
 */
public class BasePersonIrisVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 图片Base64 */
    private String imageBase64;
    /** 特征 */
    private String feature;
    /** 状态(0-有效，1-无效) */
    private String status;
    /** 图像质量得分 */
    private Double qualityScore;
    /** 厂商 */
    private String vendorCode;
    /** 算法版本 */
    private String algsVersion;
    /** 图片拓展名 */
    private String suffix;
    /** 特征MD5 */
    private String featureMd5;

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getQualityScore() {
        return qualityScore;
    }

    public void setQualityScore(Double qualityScore) {
        this.qualityScore = qualityScore;
    }

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public String getAlgsVersion() {
        return algsVersion;
    }

    public void setAlgsVersion(String algsVersion) {
        this.algsVersion = algsVersion;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getFeatureMd5() {
        return featureMd5;
    }

    public void setFeatureMd5(String featureMd5) {
        this.featureMd5 = featureMd5;
    }

}
