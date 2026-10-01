package cn.eyecool.tradelog.param;

import java.io.Serializable;

import com.alibaba.fastjson.JSON;

import lombok.Getter;
import lombok.Setter;

/**
 * 多模态搜索日志回传对象
 *
 * @author admin
 * @date 2020-12-03
 */
@Setter
@Getter
public class PersonFaceIrisMultiBakLog implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 业务流水号 */
    private String receivedSeq;
    /** 健康码日志ID */
    private String healthcodeLogId;
    /** 人员唯一编号 */
    private String uniqueId;
    /** 场景编码 */
    private String channelCode;
    /** 子场景编码 */
    private String subTreasuryCode;
    /** 现场照片对应人脸照片 */
    private String sceneFirAlgoImageBase64;
    /** 现场照片对应虹膜照片 */
    private String sceneSecAlgoImageBase64;
    /** 人脸比对分数 */
    private String sceneFirAlgoScore;
    /** 虹膜比对分数 */
    private String sceneSecAlgoScore;
    /** 比对模式字典matchMode */
    private String matchMode;
    /** 底库数据人脸 */
    private String stockFaceImageBase64;
    /** 底库数据虹膜 */
    private String stockIrisImBase64;
    /** 现场照检活分数 */
    private String checkliveScore;
    /** 现场照件检活结果(0;通过，1未通过) */
    private String checkliveResult;
    /** 比对分数，比对通过的分数或者融合分数 */
    private String matchScore;
    /** 结果(1通过，0未通过) */
    private String result;
    /** 交易时间 */
    private String receivedTime;
    /** 用时ms */
    private String timeUsed;
    /** 人脸比对用时ms */
    private String firTimeUsed;
    /** 虹膜比对用时ms */
    private String secTimeUsed;
    /** 设备标识(设备编码) */
    private String deviceSn;
    /** 温度 */
    private String temperature;
    /** 温度阈值下限 */
    private String temperatureFloor;
    /** 温度阈值上限 */
    private String temperatureTop;
    /** 设备型号编码 */
    private String deviceModel;
    /** 设备名称 */
    private String deviceName;
    /** 设备类型 */
    private String deviceType;
    /** 设备IP */
    private String deviceIp;
    /** 设备经度(东经) */
    private String deviceLongitude;
    /** 设备维度(北纬) */
    private String deviceDimension;
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

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
