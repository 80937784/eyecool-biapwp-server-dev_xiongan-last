package cn.eyecool.scene.trade.entity;

import java.io.Serializable;

/**
 * 虹膜识别(1:N)参数
 * 
 * @author admin
 * @date 2019年11月14日
 */
public class PersonIrisRecog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务流水号 */
    private String receivedSeq;
    /** 现场照 */
    private String sceneImage;
    /** 现场特征 */
    private String sceneFeature;
    /** 场景编码 */
    private String channelCode;
    /** TOP查询数量 */
    private String topN;
    /** 子场景编码 */
    private String subTreasury;
    /** 1-N搜索阈值 */
    private String searchNThreshold;

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

    public String getTopN() {
        return topN;
    }

    public void setTopN(String topN) {
        this.topN = topN;
    }

    public String getSubTreasury() {
        return subTreasury;
    }

    public void setSubTreasury(String subTreasury) {
        this.subTreasury = subTreasury;
    }

    public String getSearchNThreshold() {
        return searchNThreshold;
    }

    public void setSearchNThreshold(String searchNThreshold) {
        this.searchNThreshold = searchNThreshold;
    }

}
