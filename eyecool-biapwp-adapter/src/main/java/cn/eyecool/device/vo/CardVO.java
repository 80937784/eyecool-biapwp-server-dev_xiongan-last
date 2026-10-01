package cn.eyecool.device.vo;

import java.io.Serializable;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;

/**
 * <p>
 * CardVO 卡信息
 * <p>
 *
 * @Author 段存明
 * @Since 2021-02-24
 */
@Data
public class CardVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @JSONField(name = "CardType")
    private String cardType;
    @JSONField(name = "CardNo")
    private String cardNo;
    @JSONField(name = "AccessResult")
    private String accessResult;
    @JSONField(name = "IdInfo")
    private IDCardInfoVO idInfo;
    @JSONField(name = "Code")
    private String code;
}
