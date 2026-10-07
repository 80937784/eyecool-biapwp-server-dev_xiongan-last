package cn.eyecool.tradelog.domain.police.face;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author zfx
 * @ClassName SubImageInfoObject
 * @description
 * @since 2026/9/29 16:22
 **/
public class SubImageInfoObject {
    @JSONField(name = "ImageID")
    private String imageID;
    @JSONField(name = "EventSort")
    private Integer eventSort;
    @JSONField(name = "DeviceID")
    private String deviceID;
    @JSONField(name = "Type")
    private String type;
    @JSONField(name = "FileFormat")
    private String fileFormat;
    @JSONField(name = "ShotTime")
    private String shotTime;
    @JSONField(name = "Width")
    private Integer width;
    @JSONField(name = "Height")
    private Integer height;
    @JSONField(name = "Data")
    private String data;

    // getter setter
    public String getImageID() { return imageID; }
    public void setImageID(String imageID) { this.imageID = imageID; }
    public Integer getEventSort() { return eventSort; }
    public void setEventSort(Integer eventSort) { this.eventSort = eventSort; }
    public String getDeviceID() { return deviceID; }
    public void setDeviceID(String deviceID) { this.deviceID = deviceID; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getFileFormat() { return fileFormat; }
    public void setFileFormat(String fileFormat) { this.fileFormat = fileFormat; }
    public String getShotTime() { return shotTime; }
    public void setShotTime(String shotTime) { this.shotTime = shotTime; }
    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }
    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
}
