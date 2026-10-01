package cn.eyecool.basedata.domain;

/**
 * 人员虹膜入库信息
 * 
 * @author mawj
 * @date 2021/02/23
 */
public class BasePersonIrisPutInfo {

    private String feature;
    /** 图片Base64 */
    private String imageBase64;
    /** 是否加密(1:加密,0不加密) */
    private String encrypted;

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getEncrypted() {
        return encrypted;
    }

    public void setEncrypted(String encrypted) {
        this.encrypted = encrypted;
    }

}