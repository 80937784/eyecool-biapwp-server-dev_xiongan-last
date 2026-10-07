package cn.eyecool.tradelog.domain.police;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 订阅对象 SubscribeObject（GA/T 1400.4-2017 7.2.20）
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(
        fieldVisibility   = JsonAutoDetect.Visibility.ANY,
        getterVisibility  = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility= JsonAutoDetect.Visibility.NONE,
        setterVisibility  = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE
)
public class SubscribeObject {

    /** 订阅ID，唯一 */
    public String SubscribeID;

    /** 订阅标题 */
    public String Title;

    /** 订阅条件（JSON 字符串，各厂商自定义） */
    public String SubscribeDetail;

    /** 订阅的资源URI，如 /VIID/Faces、/VIID/MotorVehicles */
    public String ResourceURI;

    /** 申请人姓名 */
    public String ApplicantName;

    /** 申请单位 */
    public String ApplicantOrg;

    /** 申请人邮箱 */
    public String ApplicantMail;

    /** 申请人电话 */
    public String ApplicantPhone;

    /** 订阅开始时间 YYYYMMDDHHMMSS */
    public String BeginTime;

    /** 订阅结束时间 YYYYMMDDHHMMSS */
    public String EndTime;

    /** 接收通知的地址（IP 或完整URL） */
    public String ReceiveAddr;

    /** 接收通知的端口 */
    public Integer ReceivePort;

    /** 上报条件 */
    public String ReportCondition;

    /** 操作类型：0-增加 1-删除 2-修改 */
    public Integer OperateType;

    /** 触发时间 */
    public String TriggeringTime;

    /** 订阅状态：0-正常 1-暂停 2-失效（本实现内部使用） */
    public Integer SubscribeStatus;
    // 用来保存本次订阅请求header里的User-Identify
    public String userIdentify;
}
