package cn.eyecool.scene.trade.entity;

import java.io.Serializable;

/**
 * 指纹1:1认证参数
 * 
 * @author admin
 * @date 2019年11月20日
 */
public class PersonFingerVerify implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 唯一标识 */
    private String uniqueId;
    /** 指纹编号 */
    private String fingerNo;
    /** 业务流水号 */
    private String receivedSeq;
    /** 现场指纹图片 */
    private String sceneImage;
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

    public String getFingerNo() {
        return fingerNo;
    }

    public void setFingerNo(String fingerNo) {
        this.fingerNo = fingerNo;
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
