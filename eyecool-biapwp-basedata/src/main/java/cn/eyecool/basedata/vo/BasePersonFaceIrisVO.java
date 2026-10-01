package cn.eyecool.basedata.vo;

import java.io.Serializable;

public class BasePersonFaceIrisVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 多模态融合特征 */
    private String fusionFeature;
    /** 多模态特征md5 */
    private String fusionFeatureMd5;
    /** 多模态人脸特征 */
    private String faceFeature;
    /** 人脸特征MD5 */
    private String faceFeatureMd5;
    /** 人脸照片 */
    private String faceImageBase64;
    /** 人脸照片质量分数 */
    private Double faceQuality;
    /** 多模态虹膜特征 */
    private String irisFeature;
    /** 虹膜特征MD5 */
    private String irisFeatureMd5;
    /** 虹膜照片 */
    private String irisImageBase64;
    /** 虹膜照片质量分数 */
    private Double irisQuality;
    /** 状态1-有效 0:无效 */
    private String status;
    /** 有效期 */
    private String expireTime;

    public String getFusionFeature() {
        return fusionFeature;
    }

    public void setFusionFeature(String fusionFeature) {
        this.fusionFeature = fusionFeature;
    }

    public String getFusionFeatureMd5() {
        return fusionFeatureMd5;
    }

    public void setFusionFeatureMd5(String fusionFeatureMd5) {
        this.fusionFeatureMd5 = fusionFeatureMd5;
    }

    public String getFaceFeature() {
        return faceFeature;
    }

    public void setFaceFeature(String faceFeature) {
        this.faceFeature = faceFeature;
    }

    public String getFaceFeatureMd5() {
        return faceFeatureMd5;
    }

    public void setFaceFeatureMd5(String faceFeatureMd5) {
        this.faceFeatureMd5 = faceFeatureMd5;
    }

    public String getFaceImageBase64() {
        return faceImageBase64;
    }

    public void setFaceImageBase64(String faceImageBase64) {
        this.faceImageBase64 = faceImageBase64;
    }

    public Double getFaceQuality() {
        return faceQuality;
    }

    public void setFaceQuality(Double faceQuality) {
        this.faceQuality = faceQuality;
    }

    public String getIrisFeature() {
        return irisFeature;
    }

    public void setIrisFeature(String irisFeature) {
        this.irisFeature = irisFeature;
    }

    public String getIrisFeatureMd5() {
        return irisFeatureMd5;
    }

    public void setIrisFeatureMd5(String irisFeatureMd5) {
        this.irisFeatureMd5 = irisFeatureMd5;
    }

    public String getIrisImageBase64() {
        return irisImageBase64;
    }

    public void setIrisImageBase64(String irisImageBase64) {
        this.irisImageBase64 = irisImageBase64;
    }

    public Double getIrisQuality() {
        return irisQuality;
    }

    public void setIrisQuality(Double irisQuality) {
        this.irisQuality = irisQuality;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(String expireTime) {
        this.expireTime = expireTime;
    }

}
