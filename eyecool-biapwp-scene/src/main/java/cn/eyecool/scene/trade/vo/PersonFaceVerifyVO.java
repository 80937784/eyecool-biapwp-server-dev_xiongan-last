package cn.eyecool.scene.trade.vo;

import java.io.Serializable;

import cn.eyecool.common.utils.StringUtils;

/**
 * 人脸1:1认证VO
 * 
 * @author admin
 * @date 2019年11月13日
 */
public class PersonFaceVerifyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 认证结果(0:成功， 1：失败) */
    private String result;
    /** 现场照和底库照比对得分 */
    private String sceneStockScore = StringUtils.EMPTY;
    /** 现场照和底库照比对结果(0:成功， 1：失败) */
    private String sceneStockResult = StringUtils.EMPTY;
    /** 现场照和联网照比对得分 */
    private String sceneOnlineScore = StringUtils.EMPTY;
    /** 现场照和联网照比对结果(0:成功， 1：失败) */
    private String sceneOnlineResult = StringUtils.EMPTY;
    /** 现场照和芯片照比对得分 */
    private String sceneChipScore = StringUtils.EMPTY;
    /** 现场照和芯片照比对结果(0:成功， 1：失败) */
    private String sceneChipResult = StringUtils.EMPTY;
    /** 联网照和芯片照比对得分 */
    private String onlineChipScore = StringUtils.EMPTY;
    /** 联网照和芯片照比对结果(0:成功，1：失败) */
    private String onlineChipResult = StringUtils.EMPTY;
    /** 现场照检活结果(0:成功， 1：失败) */
    private String liveDetectionResult = StringUtils.EMPTY;
    /** 现场照检活得分 */
    private String liveDetectionScore = StringUtils.EMPTY;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getSceneStockScore() {
        return sceneStockScore;
    }

    public void setSceneStockScore(String sceneStockScore) {
        this.sceneStockScore = sceneStockScore;
    }

    public String getSceneStockResult() {
        return sceneStockResult;
    }

    public void setSceneStockResult(String sceneStockResult) {
        this.sceneStockResult = sceneStockResult;
    }

    public String getSceneOnlineScore() {
        return sceneOnlineScore;
    }

    public void setSceneOnlineScore(String sceneOnlineScore) {
        this.sceneOnlineScore = sceneOnlineScore;
    }

    public String getSceneOnlineResult() {
        return sceneOnlineResult;
    }

    public void setSceneOnlineResult(String sceneOnlineResult) {
        this.sceneOnlineResult = sceneOnlineResult;
    }

    public String getSceneChipScore() {
        return sceneChipScore;
    }

    public void setSceneChipScore(String sceneChipScore) {
        this.sceneChipScore = sceneChipScore;
    }

    public String getSceneChipResult() {
        return sceneChipResult;
    }

    public void setSceneChipResult(String sceneChipResult) {
        this.sceneChipResult = sceneChipResult;
    }

    public String getOnlineChipScore() {
        return onlineChipScore;
    }

    public void setOnlineChipScore(String onlineChipScore) {
        this.onlineChipScore = onlineChipScore;
    }

    public String getOnlineChipResult() {
        return onlineChipResult;
    }

    public void setOnlineChipResult(String onlineChipResult) {
        this.onlineChipResult = onlineChipResult;
    }

    public String getLiveDetectionResult() {
        return liveDetectionResult;
    }

    public void setLiveDetectionResult(String liveDetectionResult) {
        this.liveDetectionResult = liveDetectionResult;
    }

    public String getLiveDetectionScore() {
        return liveDetectionScore;
    }

    public void setLiveDetectionScore(String liveDetectionScore) {
        this.liveDetectionScore = liveDetectionScore;
    }

}
