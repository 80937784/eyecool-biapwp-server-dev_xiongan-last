package cn.eyecool.device.vo;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * <p>
 * CardsInfoVO 卡信息
 * <p>
 *
 * @Author 段存明
 * @Since 2021-02-24
 */
@Data
public class CardsInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "Address")
    private String address;
    @JsonProperty(value = "Birthday")
    private String birthday;
    @JsonProperty(value = "Height")
    private String height;
    @JsonProperty(value = "Name")
    private String name;
    @JsonProperty(value = "Nation")
    private String nation;

    @JsonProperty(value = "Number")
    private String number;
    @JsonProperty(value = "Office")
    private String office;
    @JsonProperty(value = "ProfilePic")
    private String profilePic;
    @JsonProperty(value = "Sex")
    private String sex;
    @JsonProperty(value = "ValidTimeStart")
    private String validTimeStart;

    @JsonProperty(value = "ValidTimeStop")
    private String validTimeStop;
    @JsonProperty(value = "Width")
    private String width;
}
