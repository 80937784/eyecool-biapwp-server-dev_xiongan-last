package cn.eyecool.device.vo;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * <p>
 * MemoInfoVO 设备信息
 * <p>
 *
 * @Author 段存明
 * @Since 2021-02-24
 */
@Data
public class MemoInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty(value = "Entrance")
    private String entrance;
}
