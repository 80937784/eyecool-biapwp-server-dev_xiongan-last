package cn.eyecool.tradelog.domain;

import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.annotation.Excel.Type;
import cn.eyecool.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 人脸虹膜搜索日志对象 person_faceiris_search_log
 * 
 * @author admin
 * @date 2021-05-08
 */
@Getter
@Setter
public class PersonFaceirisSearchLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 业务流水号 */
    @Excel(name = "domain.person.faceiris.busi.req")
    private String receivedSeq;

    /** 健康码日志ID */
    private String healthcodeLogId;

    /** 类型(数据字典 1：基础信息入库1:N，2：比对搜索接口1:N) */
    @Excel(name = "domain.person.face.scene.type", dictType = "search_log_type")
    private String sceneType;

    /** 人员唯一编号 */
    @Excel(name = "domain.person.faceiris.uniqueid")
    private String uniqueId;

    /** 部门ID */
    private String deptId;

    /** 人员唯一编号 */
    @Excel(name = "domain.person.faceiris.personname")
    private String personName;

    /** 部门名称 */
    @Excel(name = "domain.person.faceiris.deptname")
    private String deptName;

    /** 场景编码 */
    @Excel(name = "channel.info.channelcode")
    private String channelCode;

    /** 子场景编码 */
    @Excel(name = "device.sub.channel.code")
    private String subTreasuryCode;

    /** 人脸现场照路径 */
    private String sceneFaceImage;

    /** 虹膜现场照路径 */
    private String sceneIrisImage;

    /** 人脸比对分数 */
    @Excel(name = "domain.person.faceiris.face.matchscore")
    private Double sceneFaceScore;

    /** 虹膜比对分数 */
    @Excel(name = "domain.person.faceiris.iris.matchscore")
    private Double sceneIrisScore;

    /** 比对模式字典matchMode */
    @Excel(name = "domain.person.faceiris.match.mode", dictType = "match_mode")
    private String matchMode;

    /** 底库人脸照路径 */
    private String stockFaceImage;

    /** 底库虹膜照路径 */
    private String stockIrisImage;

    /** 现场照检活分值 */
    @Excel(name = "domain.person.faceiris.checklive.score")
    private Double checkliveScore;

    /** 现场照检活结果 */
    @Excel(name = "domain.person.faceiris.checklive.result", dictType = "bio_result")
    private String checkliveResult;

    /** 比对分数 */
    @Excel(name = "domain.person.faceiris.matchscore")
    private Double matchScore;

    /** 结果 */
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

    /** 服务器标识 */
    private String serverId;

    /** 定时任务执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date batchDate;

    /** 租户ID */
    private String tenantId;

    /** 人脸现场照Base64 */
    private String sceneFaceImageBase64;

    /** 虹膜现场照Base64 */
    private String sceneIrisImageBase64;

    /** 底库人脸照Base64 */
    private String stockFaceImageBase64;

    /** 底库虹膜照Base64 */
    private String stockIrisImageBase64;

    /** 健康码查询日志 */
    private PersonHealthCodeLog healthCodeLog;

    /** 人脸现场照路径编码 */
    private String sceneFaceImageId;

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
