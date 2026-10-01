package cn.eyecool.device.vo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * <p>
 * ResponseInfoVO 人员信息
 * <p>
 *
 * @Author 段存明
 * @Since 2021-02-24
 */
@Data
public class ResponseInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String method;
    private boolean result;
    private List<ResultInfoVO> params;
}
