package cn.eyecool.tradelog.domain.police.notice;

/**
 * @author zfx
 * @ClassName SubscribeNotificationObject
 * @description
 * @since 2026/9/30 10:05
 **/
public class SubscribeNotificationObject {
    public String NotificationID;
    public String SubscribeID;
    public String Title;
    public Integer ExecuteOperation; //1增加 2修改 3删除
    public String InfoIDs;
    public String TriggerTime;
    public DeviceList DeviceList;
}
