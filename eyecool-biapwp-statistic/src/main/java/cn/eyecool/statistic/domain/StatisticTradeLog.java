package cn.eyecool.statistic.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * 交易日志统计信息
 * 
 * @author mawj
 * @date 2021/08/31
 */
@Getter
@Setter
public class StatisticTradeLog {

    /** 主键 */
    private String id;
    /** 交易数量 */
    private Integer tradeNum;
    /** 通过数量 */
    private Integer passNum;
    /** 未通过数量 */
    private Integer notpassNum;
    /** 统计日期 */
    private String statisticDate;
    /** 统计单位 */
    private String statisticUnit;
    /** 租户ID */
    private String tenantId;
}
