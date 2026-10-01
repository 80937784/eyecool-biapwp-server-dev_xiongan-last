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
 * 人脸搜索日志对象 person_face_search_log
 * 
 * @author admin
 * @date 2021-04-29
 */
@Getter
@Setter
public class PersonFaceSearchLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 业务流水号 */
    @Excel(name = "domain.person.faceiris.busi.req")
    private String receivedSeq;

    /** 健康码日志ID */
    private String healthcodeLogId;

    /** 类型 */
    @Excel(name = "domain.person.face.scene.type", dictType = "search_log_type")
    private String sceneType;

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

    /** 子场景编码 */
    @Excel(name = "device.sub.channel.code")
    private String subTreasuryCode;

    /** 子场景名称 */
    @Excel(name = "channel.sub.subchannelname")
    private String subTreasuryName;

    /** 现场照路径 */
    private String sceneImage;

    /** 底库照路径 */
    private String stockImage;

    /** 抓拍图1路径 */
    private String takePhoto1;

    /** 抓拍图2路径 */
    private String takePhoto2;

    /** 现场视频路径 */
    private String sceneVideo;

    /** 分值 */
    @Excel(name = "domain.person.faceiris.matchscore")
    private Double sceneStockScore;

    /** 现场照检活分值 */
    @Excel(name = "domain.person.faceiris.checklive.score")
    private Double checkliveScore;

    /** 现场照检活结果 */
    @Excel(name = "domain.person.face.checklive.result", dictType = "bio_result")
    private String checkliveResult;

    /** 温度 */
    @Excel(name = "domain.person.faceiris.temperature")
    private Double temperature;

    /** 温度阈值下限 */
    @Excel(name = "domain.person.faceiris.temperature.floor")
    private Double temperatureFloor;

    /** 温度阈值上限制 */
    @Excel(name = "domain.person.faceiris.temperature.top")
    private Double temperatureTop;

    /** 是否生物识别 */
    @Excel(name = "domain.person.face.bio.recognized", dictType = "sys_yes_no")
    private String bioRecognized;

    /** 结果 */
    @Excel(name = "domain.person.face.search.result", dictType = "bio_result")
    private String result;

    /** 设备编码 */
    @Excel(name = "domain.person.faceiris.devicesn")
    private String deviceCode;

    /** 设备名称 */
    @Excel(name = "domain.person.faceiris.devicename")
    private String deviceName;

    /** 设备型号编码 */
    @Excel(name = "domain.person.faceiris.devicemodel")
    private String deviceModel;

    /** 设备IP */
    @Excel(name = "domain.person.faceiris.deviceip")
    private String deviceIp;

    /** 设备经度(东经) */
    @Excel(name = "domain.person.faceiris.device.longitude")
    private Double deviceLongitude;

    /** 设备维度(北纬) */
    @Excel(name = "domain.person.faceiris.device.latitue")
    private Double deviceDimension;

    /** 设备方向 */
    @Excel(name = "domain.person.faceiris.device.direction", dictType = "device_direction")
    private String deviceDirection;

    /** 请求时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "domain.person.face.request.time", dateFormat = "yyyy-MM-dd HH:mm:ss", type = Type.EXPORT)
    private Date receivedTime;

    /** 耗时(ms) */
    @Excel(name = "domain.person.faceiris.timeused")
    private Long timeUsed;

    /** 服务器标识 */
    private String serverId;

    /** 厂商 */
    private String vendorCode;

    /** 算法版本 */
    private String algsVersion;

    /** 定时任务执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date batchDate;

    /** 租户ID */
    private String tenantId;

    /** 现场照Base64 */
    private String sceneImageBase64;

    /** 底库照Base64 */
    private String stockImageBase64;

    /** 抓拍图1Base64 */
    private String takePhoto1Base64;

    /** 抓拍图2Base64 */
    private String takePhoto2Base64;

    /** 健康码查询日志 */
    private PersonHealthCodeLog healthCodeLog;

    /** 现场照路径编码 */
    private String sceneImageId;

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
