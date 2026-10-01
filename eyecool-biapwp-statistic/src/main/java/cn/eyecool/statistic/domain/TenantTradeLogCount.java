package cn.eyecool.statistic.domain;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * 租户识别交易日志数量信息
 * 
 * @author mawj
 * @date 2021/08/31
 */
@Getter
@Setter
public class TenantTradeLogCount implements Serializable {

    private static final long serialVersionUID = 1L;

    private String tenantId;

    private Integer tradeNum;

}
