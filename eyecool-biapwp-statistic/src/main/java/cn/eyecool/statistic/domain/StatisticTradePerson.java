package cn.eyecool.statistic.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * 交易人员统计信息
 * 
 * @author mawj
 * @date 2021/08/31
 */
@Getter
@Setter
public class StatisticTradePerson {

    /** 主键 */
    private String id;
    /** 人员数量 */
    private Integer passNum;
    /** 统计日期 */
    private String statisticDate;
    /** 统计单位 */
    private String statisticUnit;
    /** 租户ID */
    private String tenantId;
}
