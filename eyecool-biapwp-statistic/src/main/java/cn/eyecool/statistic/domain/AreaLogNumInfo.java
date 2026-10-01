package cn.eyecool.statistic.domain;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * 区域日志信息
 * 
 * @author mawj
 * @date 2021/08/30
 */
@Getter
@Setter
public class AreaLogNumInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 区域Id */
    private Long areaId;
    /** 区域名称 */
    private String areaName;
    /** 日志数量 */
    private Integer logNum;

    public AreaLogNumInfo(Long areaId, String areaName, Integer logNum) {
        super();
        this.areaId = areaId;
        this.areaName = areaName;
        this.logNum = logNum;
    }

    public AreaLogNumInfo() {
        super();
    }

}
