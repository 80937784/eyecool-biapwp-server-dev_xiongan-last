package cn.eyecool.device.vo;

import java.io.Serializable;

import lombok.Data;

/**
 * <p>
 * AccessInfoVO 通入结果
 * <p>
 *
 * @Author 段存明
 * @Since 2021-02-24
 */
@Data
public class AccessInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private int passResult;
}
