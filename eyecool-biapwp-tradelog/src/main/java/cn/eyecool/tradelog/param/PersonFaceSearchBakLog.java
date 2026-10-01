package cn.eyecool.tradelog.param;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * 人脸识别回传日志
 * 
 * @author admin
 * @date 2019年12月10日
 */
@Getter
@Setter
public class PersonFaceSearchBakLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务流水号 */
    private String receivedSeq;
    /** 健康码日志ID */
    private String healthcodeLogId;
    /** 人员标识 */
    private String uniqueId;
    /** 场景编码（业务系统编码） */
    private String channelCode;
    /** 子场景编码 */
    private String subtreasuryCode;
    /** 子场景名称 */
    private String subtreasuryName;
    /** 现场照Base64 */
    private String sceneImage;
    /** 底库照Base64 */
    private String stockImage;
    /** 抓拍图1_Base64 */
    private String takePhoto1;
    /** 抓拍图2_Base64 */
    private String takePhoto2;
    /** 分值 */
    private String sceneStockScore;
    /** 现场照检活分数 */
    private String checkliveScore;
    /** 现场照件检活结果(0;通过，1未通过) */
    private String checkliveResult;
    /** 比对结果(0;通过，1未通过) */
    private String result;
    /** 交易发起人 */
    private String broker;
    /** 设备编码 */
    private String deviceCode;
    /** 设备名称 */
    private String deviceName;
    /** 设备IP */
    private String deviceIp;
    /** 设备经度(东经) */
    private String deviceLongitude;
    /** 设备维度(北纬) */
    private String deviceDimension;
    /** 请求时间 */
    private String receivedTime;
    /** 耗时(ms) */
    private String timeUsed;
    /** 服务器标识 */
    private String serverId;
    /** 厂商 */
    private String vendorCode;
    /** 算法版本 */
    private String algsVersion;
    /** 部门编码 */
    private String deptCode;
    /** 温度 */
    private String temperature;
    /** 温度阈值下限 */
    private String temperatureFloor;
    /** 温度阈值上限 */
    private String temperatureTop;
    /** 是否生物识别(Y:是，N：否) */
    private String bioRecognized;
    /** 设备型号编码 */
    private String deviceModel;
    /** 设备进出方向 */
    private String deviceDirection;
    /** 设备地址 */
    private String deviceAddr;
    /** 测温结果 */
    private String temperatureResult;
    /** 核验方式 01:身份证核验 02：刷脸核验 03：健康码核验 04：刷卡核验 */
    private String validType;
    /** 卡号 */
    private String cardNo;

}
