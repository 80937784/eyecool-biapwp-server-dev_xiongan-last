package cn.eyecool.device.vo;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * <p>
 * SyncDataInfo 同步信息
 * <p>
 *
 * @Author 段存明
 * @Since 2021-02-24
 */
@Data
public class SyncDataInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty(value = "id")
    private Long id;
    @JsonProperty(value = "method")
    private String method;
    @JsonProperty(value = "params")
    private List<Object> params;
}
