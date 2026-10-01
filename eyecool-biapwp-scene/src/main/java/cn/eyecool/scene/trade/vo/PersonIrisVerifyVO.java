package cn.eyecool.scene.trade.vo;

import java.io.Serializable;

import cn.eyecool.common.utils.StringUtils;

/**
 * 虹膜1:1认证结果VO
 * 
 * @author admin
 * @date 2019年11月20日
 */
public class PersonIrisVerifyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 认证结果(0:成功， 1：失败) */
    private String result;
    /** 现场照和底库照比对得分 */
    private String sceneStockScore = StringUtils.EMPTY;

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

}
