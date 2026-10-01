package cn.eyecool.statistic.domain;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * 租户设备数量信息
 * 
 * @author mawj
 * @date 2021/08/31
 */
@Getter
@Setter
public class TenantDeviceCount implements Serializable {
    private static final long serialVersionUID = 1L;

    private String tenantId;

    private Integer deviceNum;
}
