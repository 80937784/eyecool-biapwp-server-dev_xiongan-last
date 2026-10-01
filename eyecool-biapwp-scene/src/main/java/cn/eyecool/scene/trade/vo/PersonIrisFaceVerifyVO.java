package cn.eyecool.scene.trade.vo;

import java.io.Serializable;

import cn.eyecool.common.utils.StringUtils;
import lombok.Data;

/**
 * 虹膜人脸多模态1:1认证VO
 * 
 * @author mawj
 * @date 2021/12/06
 */
@Data
public class PersonIrisFaceVerifyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 认证结果(0:成功， 1：失败) */
    private String result;
    /** 虹膜比对阈值 */
    private Double irisCompareThreshold;
    /** 人脸比对阈值 */
    private Double faceCompareThreshold;
    /** 融合特征比对阈值 */
    private Double fusionCompareThreshold;
    /** 虹膜比对得分 */
    private Double irisScore;
    /** 人脸比对得分 */
    private Double faceScore;
    /** 融合特征比对得分 */
    private Double fusionScore;
    /** 现场照检活结果(0:成功， 1：失败) */
    private String liveDetectionResult = StringUtils.EMPTY;
    /** 现场照检活阈值 */
    private Double liveDetectionThreshold;
    /** 现场照检活得分 */
    private Double liveDetectionScore;
}
