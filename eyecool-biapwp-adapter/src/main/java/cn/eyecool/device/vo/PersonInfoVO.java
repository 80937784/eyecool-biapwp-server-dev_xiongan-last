package cn.eyecool.device.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * <p>
 * PersonInfoVO 人员信息
 * <p>
 *
 * @Author 段存明
 * @Since 2021-02-24
 */
@Data
public class PersonInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty(value = "Type")
    private Integer type;
    @JsonProperty(value = "Code")
    private String code;
    @JsonProperty(value = "CredentialNo")
    private String credentialNo;
    @JsonProperty(value = "GroupName")
    private String groupName;
    @JsonProperty(value = "Name")
    private String name;
    @JsonProperty(value = "Sex")
    private String sex;
    @JsonProperty(value = "Birthday")
    private String birthday;
    @JsonProperty(value = "GuestInfo")
    private GuestInfoVO guestInfo;
    @JsonProperty(value = "URL")
    private List<String> url;
    @JsonProperty(value = "Images")
    private List<String> images = new ArrayList<>(0);
    @JsonProperty(value = "Cards")
    private List<CardInfoVO> cards = new ArrayList<>(0);
}
