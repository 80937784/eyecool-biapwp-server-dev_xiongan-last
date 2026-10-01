package cn.eyecool.basedata.domain;

import java.util.List;

/**
 * 人员入库信息
 * 
 * @author admin
 * @date 2019年10月25日
 */
public class BasePersonPutInfo extends BasePersonInfo {
    private static final long serialVersionUID = 1L;

    /** 业务流水号(用户HTTP请求) */
    private String receivedSeq;
    /** 部门编码 */
    private String deptCode;
    /** 人脸入库信息 */
    private BasePersonFacePutInfo facePutInfo;
    /** 指纹入库信息列表 */
    private List<BasePersonFingerPutInfo> fingerPutInfoList;
    /** 虹膜入库信息 */
    private BasePersonIrisPutInfo irisPutInfo;
    /** 虹膜人脸入库信息 */
    private BasePersonIrisFacePutInfo irisFacePutInfo;

    public String getReceivedSeq() {
        return receivedSeq;
    }

    public void setReceivedSeq(String receivedSeq) {
        this.receivedSeq = receivedSeq;
    }

    @Override
    public String getDeptCode() {
        return deptCode;
    }

    @Override
    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public BasePersonFacePutInfo getFacePutInfo() {
        return facePutInfo;
    }

    public void setFacePutInfo(BasePersonFacePutInfo facePutInfo) {
        this.facePutInfo = facePutInfo;
    }

    public List<BasePersonFingerPutInfo> getFingerPutInfoList() {
        return fingerPutInfoList;
    }

    public void setFingerPutInfoList(List<BasePersonFingerPutInfo> fingerPutInfoList) {
        this.fingerPutInfoList = fingerPutInfoList;
    }

    public BasePersonIrisPutInfo getIrisPutInfo() {
        return irisPutInfo;
    }

    public void setIrisPutInfo(BasePersonIrisPutInfo irisPutInfo) {
        this.irisPutInfo = irisPutInfo;
    }

    public BasePersonIrisFacePutInfo getIrisFacePutInfo() {
        return irisFacePutInfo;
    }

    public void setIrisFacePutInfo(BasePersonIrisFacePutInfo irisFacePutInfo) {
        this.irisFacePutInfo = irisFacePutInfo;
    }

}
