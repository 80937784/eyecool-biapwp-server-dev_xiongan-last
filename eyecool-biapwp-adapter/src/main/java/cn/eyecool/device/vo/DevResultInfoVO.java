package cn.eyecool.device.vo;

import java.io.Serializable;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;

/**
 * <p>
 * DevResultInfoVO 设备信息
 * <p>
 *
 * @Author 段存明
 * @Since 2021-02-24
 */
@Data
public class DevResultInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @JSONField(name = "AccessInfo")
    private AccessInfoVO accessInfo;
    @JSONField(name = "CommInfo")
    private CommInfoVO commInfo;
    @JSONField(name = "Code")
    private String code;
    @JSONField(name = "UTC")
    private Long utc;
    @JSONField(name = "UTCMS")
    private int utcms;
    @JSONField(name = "TimeZone")
    private String timeZone;
    @JSONField(name = "ObjectID")
    private int objectID;
    @JSONField(name = "Object")
    private FaceInfoVO object;
    @JSONField(name = "Pass")
    private Integer pass;
    @JSONField(name = "SearchScore")
    private Double SearchScore;
    @JSONField(name = "RecognizeResults")
    private List<FaceRecognizeInfoVO> recognizeResults;
    @JSONField(name = "IdCardsInfo")
    private IDCardInfoVO idCardsInfo;
    @JSONField(name = "Card")
    private CardVO card;
    @JSONField(name = "CardsInfo")
    private CardsInfoVO cardsInfo;
    @JSONField(name = "Image")
    private String image;

}
