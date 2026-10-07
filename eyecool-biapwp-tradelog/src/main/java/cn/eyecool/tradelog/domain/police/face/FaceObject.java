package cn.eyecool.tradelog.domain.police.face;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author zfx
 * @ClassName FaceObject
 * @description
 * @since 2026/9/29 16:23
 **/
public
class FaceObject {
    @JSONField(name = "FaceID")
    private String faceID;
    @JSONField(name = "InfoKind")
    private Integer infoKind;
    @JSONField(name = "SourceID")
    private String sourceID;
    @JSONField(name = "DeviceID")
    private String deviceID;
    @JSONField(name = "ShotTime")
    private String shotTime;
    @JSONField(name = "LocationMarkTime")
    private String locationMarkTime;
    @JSONField(name = "FaceDisAppearTime")
    private String faceDisAppearTime;
    @JSONField(name = "LeftTopX")
    private Integer leftTopX;
    @JSONField(name = "LeftTopY")
    private Integer leftTopY;
    @JSONField(name = "RightBtmX")
    private Integer rightBtmX;
    @JSONField(name = "RightBtmY")
    private Integer rightBtmY;
    @JSONField(name = "SubImageList")
    private SubImageList subImageList;

    // getter setter
    public String getFaceID() { return faceID; }
    public void setFaceID(String faceID) { this.faceID = faceID; }
    public Integer getInfoKind() { return infoKind; }
    public void setInfoKind(Integer infoKind) { this.infoKind = infoKind; }
    public String getSourceID() { return sourceID; }
    public void setSourceID(String sourceID) { this.sourceID = sourceID; }
    public String getDeviceID() { return deviceID; }
    public void setDeviceID(String deviceID) { this.deviceID = deviceID; }
    public String getShotTime() { return shotTime; }
    public void setShotTime(String shotTime) { this.shotTime = shotTime; }
    public Integer getLeftTopX() { return leftTopX; }
    public void setLeftTopX(Integer leftTopX) { this.leftTopX = leftTopX; }
    public Integer getLeftTopY() { return leftTopY; }
    public void setLeftTopY(Integer leftTopY) { this.leftTopY = leftTopY; }
    public Integer getRightBtmX() { return rightBtmX; }
    public void setRightBtmX(Integer rightBtmX) { this.rightBtmX = rightBtmX; }
    public Integer getRightBtmY() { return rightBtmY; }
    public void setRightBtmY(Integer rightBtmY) { this.rightBtmY = rightBtmY; }
    public SubImageList getSubImageList() { return subImageList; }
    public void setSubImageList(SubImageList subImageList) { this.subImageList = subImageList; }
}
