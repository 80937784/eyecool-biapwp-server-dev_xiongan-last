package cn.eyecool.tradelog.domain;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

/**
 * 实时交易日志
 * 
 * @author mawj
 * @date 2021/08/30
 */
@Getter
@Setter
public class RealtimeTradeLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 请求流水号 */
    private String receivedSeq;
    /** 请求时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date receivedTime;
    /** 识别结果 0：通过 1：未通过 */
    private String result;
    /** 唯一标识 */
    private String uniqueId;
    /** 人员姓名 */
    private String personName;
    /** 设备编号 */
    private String deviceNo;
    /** 设备名称 */
    private String deviceName;
    /** 设备地址 */
    private String deviceAddr;
    /** 生物类型 */
    private String bioType;
    /** 业务类型 */
    private String busiType;
    /** 租户ID */
    private String tenantId;

}
