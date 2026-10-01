package cn.eyecool.scene.trade.entity;

import java.io.Serializable;

/**
 * 虹膜1:1认证参数
 * 
 * @author admin
 * @date 2019年11月20日
 */
public class PersonIrisVerify implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 唯一标识 */
    private String uniqueId;
    /** 业务流水号 */
    private String receivedSeq;
    /** 现场虹膜图片 */
    private String sceneImage;
    /** 现场特征 */
    private String sceneFeature;
    /** 场景编码 */
    private String channelCode;
    /** 1:1比对阈值 */
    private String compareThreshold;

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

    public String getSceneImage() {
        return sceneImage;
    }

    public void setSceneImage(String sceneImage) {
        this.sceneImage = sceneImage;
    }

    public String getSceneFeature() {
        return sceneFeature;
    }

    public void setSceneFeature(String sceneFeature) {
        this.sceneFeature = sceneFeature;
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

}
