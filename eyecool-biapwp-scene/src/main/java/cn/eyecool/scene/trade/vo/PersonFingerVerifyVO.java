package cn.eyecool.scene.trade.vo;

import java.io.Serializable;

import cn.eyecool.common.utils.StringUtils;

/**
 * 纹1:1认证结果VO
 * 
 * @author admin
 * @date 2019年11月20日
 */
public class PersonFingerVerifyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 认证结果(0:成功， 1：失败) */
    private String result;
    /** 手指号 */
    private String fingerNo = StringUtils.EMPTY;
    /** 现场照和底库照比对得分 */
    private String sceneStockScore = StringUtils.EMPTY;
    /** 现场照和底库照比对结果(0:成功， 1：失败) */
    private String sceneStockResult = StringUtils.EMPTY;
    /** 手指ID */
    private String fingerId;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getFingerNo() {
        return fingerNo;
    }

    public void setFingerNo(String fingerNo) {
        this.fingerNo = fingerNo;
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

    public String getFingerId() {
        return fingerId;
    }

    public void setFingerId(String fingerId) {
        this.fingerId = fingerId;
    }

}
