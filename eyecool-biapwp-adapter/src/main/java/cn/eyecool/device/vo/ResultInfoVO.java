package cn.eyecool.device.vo;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * <p>
 * ResultInfoVO 回复结果
 * <p>
 *
 * @Author 段存明
 * @Since 2021-02-24
 */
@Data
public class ResultInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty(value = "CertificateType")
    private String certificateType;
    @JsonProperty(value = "Code")
    private String code;
    @JsonProperty(value = "Result")
    private boolean result;
    @JsonProperty(value = "ErrorCode")
    private int errorCode;
    @JsonProperty(value = "ErrorCodePic")
    private List<Object> errorCodePic;
    @JsonProperty(value = "ErrorMessage")
    private String errorMessage;

}
