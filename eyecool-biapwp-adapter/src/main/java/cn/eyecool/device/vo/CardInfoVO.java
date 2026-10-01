package cn.eyecool.device.vo;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * <p>
 * CardInfoVO 卡信息
 * <p>
 *
 * @Author 段存明
 * @Since 2021-02-24
 */
@Data
public class CardInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty(value = "ID")
    private String id;
    @JsonProperty(value = "Type")
    private int type;
    @JsonProperty(value = "Validity")
    private List<String> validity;
    @JsonProperty(value = "ValidityTime")
    private List<String> validityTime;
    @JsonProperty(value = "Memo")
    private MemoInfoVO memo;
}
