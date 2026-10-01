package cn.eyecool.device.vo;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 设备升级任务信息
 * 
 * @author mawj
 * @date 2021/10/29
 */
@Data
public class UpgradeTaskInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty(value = "id")
    private Long id;
    @JsonProperty(value = "method")
    private String method;
    @JsonProperty(value = "params")
    private JSONObject params;
}
