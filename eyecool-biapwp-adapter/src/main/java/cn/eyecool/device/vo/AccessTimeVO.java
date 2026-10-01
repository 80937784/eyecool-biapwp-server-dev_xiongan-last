package cn.eyecool.device.vo;

import java.io.Serializable;

import lombok.Data;

/**
 * <p>
 * AccessTimeVO 访问时间
 * <p>
 *
 * @Author 段存明
 * @Since 2021-02-24
 */
@Data
public class AccessTimeVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long from;
    private Long to;
}
