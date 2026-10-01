package cn.eyecool.basedata.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 人员详细信息VO.
 * 
 * @author admin
 * @date 2019年11月12日
 */
public class BasePersonInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 人员标识 */
    private String uniqueId;
    /** 部门编码 */
    private String deptCode;
    /** 部门名称 */
    private String deptName;
    /** 姓名 */
    private String name;
    /** 性别：0-男 1-女 2-未知 */
    private String sex;
    /** 手机 */
    private String phone;
    /** 卡号 */
    private String cardNo;
    /** 账号 */
    private String account;
    /** 邮箱 */
    private String email;
    /** 人员标记：1-正常 2-红名单 3-黑名单 */
    private String flag;
    /** 状态：0-有效 1:无效 */
    private String status;
    /** 人脸信息 */
    private List<BasePersonFaceVO> basePersonFaceVOList;
    /** 指纹信息 */
    private List<BasePersonFingerVO> basePersonFingerVOList;
    /** 虹膜信息 */
    private List<BasePersonIrisVO> basePersonIrisVOList;
    /** 人脸虹膜多模态信息 */
    private List<BasePersonFaceIrisVO> basePersonFaceIrisVOList;

    /** 人员类型：user-人员 visitor-访客(用于访客系统) */
    private String personType;
    /** 生效开始时间(用于访客系统) */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date effectiveBeginTime;
    /** 生效结束时间(用户访客系统) */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date effectiveEndTime;

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<BasePersonFaceVO> getBasePersonFaceVOList() {
        return basePersonFaceVOList;
    }

    public void setBasePersonFaceVOList(List<BasePersonFaceVO> basePersonFaceVOList) {
        this.basePersonFaceVOList = basePersonFaceVOList;
    }

    public List<BasePersonFingerVO> getBasePersonFingerVOList() {
        return basePersonFingerVOList;
    }

    public void setBasePersonFingerVOList(List<BasePersonFingerVO> basePersonFingerVOList) {
        this.basePersonFingerVOList = basePersonFingerVOList;
    }

    public List<BasePersonIrisVO> getBasePersonIrisVOList() {
        return basePersonIrisVOList;
    }

    public void setBasePersonIrisVOList(List<BasePersonIrisVO> basePersonIrisVOList) {
        this.basePersonIrisVOList = basePersonIrisVOList;
    }

    public List<BasePersonFaceIrisVO> getBasePersonFaceIrisVOList() {
        return basePersonFaceIrisVOList;
    }

    public void setBasePersonFaceIrisVOList(List<BasePersonFaceIrisVO> basePersonFaceIrisVOList) {
        this.basePersonFaceIrisVOList = basePersonFaceIrisVOList;
    }

    public String getPersonType() {
        return personType;
    }

    public void setPersonType(String personType) {
        this.personType = personType;
    }

    public Date getEffectiveBeginTime() {
        return effectiveBeginTime;
    }

    public void setEffectiveBeginTime(Date effectiveBeginTime) {
        this.effectiveBeginTime = effectiveBeginTime;
    }

    public Date getEffectiveEndTime() {
        return effectiveEndTime;
    }

    public void setEffectiveEndTime(Date effectiveEndTime) {
        this.effectiveEndTime = effectiveEndTime;
    }
}
