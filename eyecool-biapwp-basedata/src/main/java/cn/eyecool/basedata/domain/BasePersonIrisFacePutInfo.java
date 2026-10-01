package cn.eyecool.basedata.domain;

/**
 * 人员虹膜人脸多模态入库信息
 * 
 * @author mawj
 * @date 2021/08/05
 */
public class BasePersonIrisFacePutInfo {

    /** 人脸base64 */
    private String faceImgBase64;

    /** 虹膜base64 */
    private String irisImgBase64;

    /** 虹膜特征 */
    private String irisFeature;

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

    public String getIrisFeature() {
        return irisFeature;
    }

    public void setIrisFeature(String irisFeature) {
        this.irisFeature = irisFeature;
    }

}
