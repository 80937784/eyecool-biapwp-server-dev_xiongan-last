package cn.eyecool.device.vo;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * <p>
 * GuestInfoVO 访客信息
 * <p>
 *
 * @Author 段存明
 * @Since 2021-02-24
 */
@Data
public class GuestInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty(value = "Corp")
    private String corp;
    @JsonProperty(value = "Phone")
    private String phone;
    @JsonProperty(value = "CarLicense")
    private String carLicense;
    @JsonProperty(value = "Partner")
    private int partner;
    @JsonProperty(value = "Host")
    private String host;
    @JsonProperty(value = "AccessTime")
    private List<AccessTimeVO> accessTime;
}
