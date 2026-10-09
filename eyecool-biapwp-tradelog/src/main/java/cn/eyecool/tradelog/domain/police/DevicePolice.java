package cn.eyecool.tradelog.domain.police;

import cn.eyecool.common.annotation.Excel;
import lombok.Data;

/**
 * @author zfx
 * @ClassName DevicePolice
 * @description 往公安推送设备对象
 * @since 2026/10/8 20:17
 **/
@Data
public class DevicePolice {
    private String id;
    /** 设备编号 */
    @Excel(name = "device.info.deviceno")
    private String deviceNo;

    /** 设备名称 */
    @Excel(name = "device.name")
    private String deviceName;

    /** 安装地点 */
    private String deviceAddr;

    /** 型号编码 */
    private String deviceModelCode;

    /** 设备IP */
    private String deviceIp;

    /** 设备Mac */
    private String deviceMac;

    /** 设备经度 */
    private Double longitude;

    /** 设备纬度 */
    private Double latitude;

    /** 设备APEID */
    private String apeId;
    /**是否上传 Y上传 N不上传*/
    private String isUpload;

}
