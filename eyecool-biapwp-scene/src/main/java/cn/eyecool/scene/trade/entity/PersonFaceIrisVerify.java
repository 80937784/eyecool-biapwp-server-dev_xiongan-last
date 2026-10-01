package cn.eyecool.scene.trade.entity;

import java.io.Serializable;

/**
 * 人脸虹膜1v1认证参数
 * 
 * @author mawj
 * @date 2021/05/28
 */
public class PersonFaceIrisVerify implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 唯一标识 */
    private String uniqueId;
    /** 业务流水号 */
    private String receivedSeq;
    /** 现场人脸图片 */
    private String faceSceneImage;
    /** 现场虹膜图片 */
    private String irisSceneImage;
    /** 现场虹膜特征 */
    private String irisSceneFeature;
    /** 场景编码 */
    private String channelCode;
    /** 1:1比对阈值 */
    private String compareThreshold;
    /** 比对模式 */
    private String matchMode;

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getReceivedSeq() {
        return receivedSeq;
    }

    public void setReceivedSeq(String receivedSeq) {
        this.receivedSeq = receivedSeq;
    }

    public String getFaceSceneImage() {
        return faceSceneImage;
    }

    public void setFaceSceneImage(String faceSceneImage) {
        this.faceSceneImage = faceSceneImage;
    }

    public String getIrisSceneImage() {
        return irisSceneImage;
    }

    public void setIrisSceneImage(String irisSceneImage) {
        this.irisSceneImage = irisSceneImage;
    }

    public String getIrisSceneFeature() {
        return irisSceneFeature;
    }

    public void setIrisSceneFeature(String irisSceneFeature) {
        this.irisSceneFeature = irisSceneFeature;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getCompareThreshold() {
        return compareThreshold;
    }

    public void setCompareThreshold(String compareThreshold) {
        this.compareThreshold = compareThreshold;
    }

    public String getMatchMode() {
        return matchMode;
    }

    public void setMatchMode(String matchMode) {
        this.matchMode = matchMode;
    }

}
