package cn.eyecool.device.vo;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * <p>
 * 人员信息 ResultPersonInfoVO
 * <p>
 *
 * @Author 段存明
 * @Since 2021-02-24
 */
@Data
public class ResultPersonInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty(value = "Name")
    private String name;
    @JsonProperty(value = "Birthday")
    private String birthday;
    @JsonProperty(value = "Sex")
    private String sex;
    @JsonProperty(value = "CertificateType")
    private String certificateType;
    @JsonProperty(value = "Id")
    private String id;
    @JsonProperty(value = "Country")
    private String country;
    @JsonProperty(value = "Province")
    private String province;
    @JsonProperty(value = "City")
    private String city;
    @JsonProperty(value = "PersonType")
    private String personType;
    @JsonProperty(value = "HealthCode")
    private String healthCode;

}
