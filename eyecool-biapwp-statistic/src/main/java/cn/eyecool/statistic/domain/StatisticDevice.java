package cn.eyecool.statistic.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * 设备数量统计信息
 * 
 * @author mawj
 * @date 2021/08/31
 */
@Getter
@Setter
public class StatisticDevice {

    /** 主键 */
    private String id;
    /** 设备数量 */
    private Integer deviceNum;
    /** 在线设备数量 */
    private Integer onlineNum;
    /** 离线设备数量 */
    private Integer offlineNum;
    /** 统计日期 */
    private String statisticDate;
    /** 租户ID */
    private String tenantId;
}
