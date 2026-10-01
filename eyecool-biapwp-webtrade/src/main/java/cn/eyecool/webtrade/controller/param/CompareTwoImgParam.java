package cn.eyecool.webtrade.controller.param;

import java.io.Serializable;

/**
 * 图片比对参数
 * 
 * @author mawj
 * @date 2021/05/24
 */
public class CompareTwoImgParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 图片1base64 */
    private String image1Base64;
    /** 图片2base64 */
    private String image2Base64;
    /** 比对阈值 */
    private Double threshold;

    public String getImage1Base64() {
        return image1Base64;
    }

    public void setImage1Base64(String image1Base64) {
        this.image1Base64 = image1Base64;
    }

    public String getImage2Base64() {
        return image2Base64;
    }

    public void setImage2Base64(String image2Base64) {
        this.image2Base64 = image2Base64;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

}