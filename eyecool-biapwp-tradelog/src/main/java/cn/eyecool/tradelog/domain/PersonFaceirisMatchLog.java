package cn.eyecool.tradelog.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.annotation.Excel.Type;
import cn.eyecool.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 人脸虹膜多模态比对日志对象
 * 
 * @author admin
 * @date 2021-12-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PersonFaceirisMatchLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 业务流水号 */
    @Excel(name = "domain.person.faceiris.busi.req")
    private String receivedSeq;

    /** 健康码日志ID */
    private String healthcodeLogId;

    /** 人员标识 */
    @Excel(name = "domain.person.faceiris.uniqueid")
    private String uniqueId;

    /** 部门ID */
    @Excel(name = "domain.person.faceiris.deptid")
    private Long deptId;

    /** 人员唯一编号 */
    @Excel(name = "domain.person.faceiris.personname")
    private String personName;

    /** 部门名称 */
    @Excel(name = "domain.person.faceiris.deptname")
    private String deptName;

    /** 场景编码 */
    @Excel(name = "channel.info.channelcode")
    private String channelCode;

    /** 人脸现场照路径 */
    private String sceneFaceImage;

    /** 虹膜现场照路径 */
    private String sceneIrisImage;

    /** 底库人脸照路径 */
    private String stockFaceImage;

    /** 底库虹膜照路径 */
    private String stockIrisImage;

    /** 人脸比对分数 */
    @Excel(name = "domain.person.faceiris.face.matchscore")
    private Double sceneFaceScore;

    /** 虹膜比对分数 */
    @Excel(name = "domain.person.faceiris.iris.matchscore")
    private Double sceneIrisScore;

    /** 比对分数 */
    @Excel(name = "domain.person.faceiris.matchscore")
    private Double matchScore;

    /** 检活分值 */
    @Excel(name = "domain.person.faceiris.checklive.score")
    private Double checkliveScore;

    /** 现场照件检活结果(0通过，1未通过) */
    @Excel(name = "domain.person.faceiris.checklive.result", dictType = "bio_result")
    private String checkliveResult;

    /** 比对结果(0通过，1未通过) */
    @Excel(name = "domain.person.faceiris.match.result", dictType = "bio_result")
    private String result;

    /** 交易时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "domain.person.face.request.time", dateFormat = "yyyy-MM-dd HH:mm:ss", type = Type.EXPORT)
    private Date receivedTime;

    /** 比对用时ms */
    @Excel(name = "domain.person.faceiris.timeused")
    private Long timeUsed;

    /** 人脸比对用时ms */
    @Excel(name = "domain.person.faceiris.face.timeused")
    private Long faceTimeUsed;

    /** 虹膜比对用时ms */
    @Excel(name = "domain.person.faceiris.iris.timeused")
    private Long irisTimeUsed;

    /** 温度 */
    @Excel(name = "domain.person.faceiris.temperature")
    private Double temperature;

    /** 温度阈值下限 */
    @Excel(name = "domain.person.faceiris.temperature.floor")
    private Double temperatureFloor;

    /** 温度阈值上限制 */
    @Excel(name = "domain.person.faceiris.temperature.top")
    private Double temperatureTop;

    /** 测温结果 */
    private String temperatureResult;

    /** 设备标识 */
    @Excel(name = "domain.person.faceiris.devicesn")
    private String deviceSn;

    /** 设备型号编码 */
    @Excel(name = "domain.person.faceiris.devicemodel")
    private String deviceModel;

    /** 设备名称 */
    @Excel(name = "domain.person.faceiris.devicename")
    private String deviceName;

    /** 设备IP */
    @Excel(name = "domain.person.faceiris.deviceip")
    private String deviceIp;

    /** 设备经度(东经) */
    @Excel(name = "domain.person.faceiris.device.longitude")
    private Double deviceLongitude;

    /** 设备维度(北纬) */
    @Excel(name = "domain.person.faceiris.device.latitue")
    private Double deviceDimension;

    /** 设备方向(IN:进，OUT:出，UNKNOWN：未知) */
    @Excel(name = "domain.person.faceiris.device.direction", dictType = "device_direction")
    private String deviceDirection;

    /** 设备地址 */
    private String deviceAddr;

    /** 服务器标识 */
    private String serverId;

    /** 厂商 */
    private String vendorCode;

    /** 算法版本 */
    private String algsVersion;

    /** 人脸现场照Base64 */
    private String sceneFaceImageBase64;

    /** 虹膜现场照Base64 */
    private String sceneIrisImageBase64;

    /** 底库人脸照Base64 */
    private String stockFaceImageBase64;

    /** 底库虹膜照Base64 */
    private String stockIrisImageBase64;

    /** 定时任务执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date batchDate;

    /** 租户ID */
    private String tenantId;

    /** 比对模式 */
    private String matchMode;

    /** 备注 */
    private String remark;

    /** 健康码查询日志 */
    private PersonHealthCodeLog healthCodeLog;

}
