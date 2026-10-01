package cn.eyecool.device.vo;

import java.io.Serializable;

import lombok.Data;

/**
 * <p>
 * MatchLogVO 识别日志
 * <p>
 *
 * @Author 段存明
 * @Since 2021-02-24
 */
@Data
public class MatchLogVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String receivedSeq;
    private String sceneImage;
    private String stockImage;
    private String takePhoto1;
    private String takePhoto2;
    private String sceneStockScore;
    private String checkliveResult;
    private String result;
    private String uniqueId;
    private String broker;
    private String channelCode;
    private String subtreasuryCode;
    private String subtreasuryName;
    private String deviceCode;
    private String deviceName;
    private String deviceIp;
    private String receivedTime;
    private String timeUsed;
    private String serverId;
    private String vendorCode;
    private String algsVersion;
    private String deviceLongitude;
    private String deviceDimension;
    private String deptCode;
    private String temperature;
    private String temperatureFloor;
    private String temperatureTop;
    private String bioRecognized;
    private String healthcodeLogId;
    private String temperatureResult;
    private String validType;
}
