package cn.eyecool.scene.trade.entity;

import java.io.Serializable;

import lombok.Data;

/**
 * 人脸虹膜多模态1:1认证参数
 * 
 * @author mawj
 * @date 2021/12/06
 */
@Data
public class PersonIrisFaceVerify implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 唯一标识 */
    private String uniqueId;
    /** 业务流水号 */
    private String receivedSeq;
    /** 场景编码 */
    private String channelCode;
    /** 认证比对方式 0 虹膜比对,1 人脸比对，2 虹膜和人脸同时比对， 3 虹膜或人脸比对， 4 虹膜人脸多模态比对 */
    private String verifyType;
    /** 人脸照片Base64 */
    private String faceBase64Img;
    /** 虹膜照片Base64 */
    private String irisBase64Img;
    /** 人脸特征Base64 */
    private String faceFeatureBase64;
    /** 虹膜特征Base64 */
    private String irisFeatureBase64;
    /** 是否检活 */
    private String liveDetection;
    /** 检活阈值 */
    private String liveDetectionThreshold;
    /** 虹膜1:1比对阈值 */
    private String irisCompareThreshold;
    /** 人脸1:1比对阈值 */
    private String faceCompareThreshold;
    /** 融合特征1:1比对阈值 */
    private String fusionCompareThreshold;
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
    /** 设备方向 */
    private String deviceDirection;
    /** 设备型号编码 */
    private String deviceModel;
    /** 设备位置 */
    private String deviceAddr;
    /** 温度（前端测温+后端识别情况，用于日志保存） */
    private String temperature;
    /** 温度阈值下限（前端测温+后端识别情况，用于日志保存） */
    private String temperatureFloor;
    /** 温度阈值上限（前端测温+后端识别情况，用于日志保存） */
    private String temperatureTop;
    /** 现场测温结果(0通过，1未通过) */
    private String temperatureResult;
}
