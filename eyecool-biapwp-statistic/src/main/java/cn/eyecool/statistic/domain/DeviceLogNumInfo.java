package cn.eyecool.statistic.domain;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * 设备日志信息
 * 
 * @author mawj
 * @date 2021/08/30
 */
@Getter
@Setter
public class DeviceLogNumInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 设备编码 */
    private String deviceNo;
    /** 设备名称 */
    private String deviceName;
    /** 日志数量 */
    private Integer logNum;
    /** 设备型号编码 */
    private String deviceModelCode;
    /** 设备型号名称 */
    private String deviceModelName;

}
