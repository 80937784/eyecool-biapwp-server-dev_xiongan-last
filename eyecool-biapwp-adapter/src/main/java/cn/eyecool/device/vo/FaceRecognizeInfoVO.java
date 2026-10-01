package cn.eyecool.device.vo;

import java.io.Serializable;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;

/**
 * <p>
 * FaceRecognizeInfoVO 封装人员信息
 * <p>
 *
 * @Author 段存明
 * @Since 2021-02-24
 */
@Data
public class FaceRecognizeInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @JSONField(name = "LivenessScore")
    private Double livenessScore;
    @JSONField(name = "PersonInfo")
    private ResultPersonInfoVO personInfo;
    private AccessInfoVO accessInfo;
    private Double searchScore;
    // "SearchScore": 97.2937011718750,
}
