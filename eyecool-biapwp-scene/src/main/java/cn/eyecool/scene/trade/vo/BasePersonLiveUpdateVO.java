package cn.eyecool.scene.trade.vo;

import java.util.Map;

import cn.eyecool.basedata.vo.BasePersonInfoVO;

/**
 * 人员实时同步VO.
 * 
 * @author admin
 * @date 2020年2月25日
 */
public class BasePersonLiveUpdateVO extends BasePersonInfoVO {

    private static final long serialVersionUID = 1L;

    /** 人脸开通状态(0-不开通 1-开通) */
    private String faceMode;
    /** 指纹开通状态(0-不开通 1-开通) */
    private String fingerMode;
    /** 虹膜开通状态(0-不开通 1-开通) */
    private String irisMode;
    /** 指静脉开通状态(0-不开通 1-开通) */
    private String fveinMode;
    /** 人脸虹膜多模态开通状态(0-不开通 1-开通) */
    private String faceIrisMode;
    /** 是否锁定(Y:是，N:否) */
    private String locked;
    /** 所属子场景ID */
    private String subTreasuryId;
    /** 人员拓展属性JSON */
    private String extAttrs;
    /** 身份证信息 */
    private Map<String, Object> idCardInfo;
    /** 是否设备管理员 */
    private String deviceMgr;

    /** 通行开始时间 */
    private String permitStartDate;
    /** 通行结束时间 */
    private String permitEndDate;
    /** 每日通行时间段 */
    private String permitInoutTime;

    public String getFaceMode() {
        return faceMode;
    }

    public void setFaceMode(String faceMode) {
        this.faceMode = faceMode;
    }

    public String getFingerMode() {
        return fingerMode;
    }

    public void setFingerMode(String fingerMode) {
        this.fingerMode = fingerMode;
    }

    public String getIrisMode() {
        return irisMode;
    }

    public void setIrisMode(String irisMode) {
        this.irisMode = irisMode;
    }

    public String getFveinMode() {
        return fveinMode;
    }

    public void setFveinMode(String fveinMode) {
        this.fveinMode = fveinMode;
    }

    public String getFaceIrisMode() {
        return faceIrisMode;
    }

    public void setFaceIrisMode(String faceIrisMode) {
        this.faceIrisMode = faceIrisMode;
    }

    public String getLocked() {
        return locked;
    }

    public void setLocked(String locked) {
        this.locked = locked;
    }

    public String getSubTreasuryId() {
        return subTreasuryId;
    }

    public void setSubTreasuryId(String subTreasuryId) {
        this.subTreasuryId = subTreasuryId;
    }

    public String getExtAttrs() {
        return extAttrs;
    }

    public void setExtAttrs(String extAttrs) {
        this.extAttrs = extAttrs;
    }

    public Map<String, Object> getIdCardInfo() {
        return idCardInfo;
    }

    public void setIdCardInfo(Map<String, Object> idCardInfo) {
        this.idCardInfo = idCardInfo;
    }

    public String getDeviceMgr() {
        return deviceMgr;
    }

    public void setDeviceMgr(String deviceMgr) {
        this.deviceMgr = deviceMgr;
    }

    public String getPermitStartDate() {
        return permitStartDate;
    }

    public void setPermitStartDate(String permitStartDate) {
        this.permitStartDate = permitStartDate;
    }

    public String getPermitEndDate() {
        return permitEndDate;
    }

    public void setPermitEndDate(String permitEndDate) {
        this.permitEndDate = permitEndDate;
    }

    public String getPermitInoutTime() {
        return permitInoutTime;
    }

    public void setPermitInoutTime(String permitInoutTime) {
        this.permitInoutTime = permitInoutTime;
    }
}
