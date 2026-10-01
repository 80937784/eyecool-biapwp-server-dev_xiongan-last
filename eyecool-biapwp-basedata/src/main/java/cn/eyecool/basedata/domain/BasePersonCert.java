package cn.eyecool.basedata.domain;

import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.annotation.Excel.Type;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 人员证件信息对象 base_person_cert
 * 
 * @author mawj
 * @date 2021-01-27
 */
public class BasePersonCert extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 人员标识 */
    @Excel(name = "base.person.cert.unique")
    private String uniqueId;

    /** 证件类型（字典国标） */
    @Excel(name = "base.person.cert.type", dictType = "sys_cert_type")
    private String certType;

    /** 证件号码 */
    @Excel(name = "base.person.cert.number")
    private String certNum;

    /** 证件姓名 */
    @Excel(name = "base.person.cert.name")
    private String certName;

    /** 证件有效期 */
    @Excel(name = "base.person.cert.validity")
    private String certValidity;

    /** 性别：0-男 1-女 2-未知 */
    @Excel(name = "base.person.cert.sex", dictType = "sys_user_sex")
    private String gender;

    /** 出生日期 */
    @Excel(name = "base.person.cert.birthday")
    private String bthDate;

    /** 民族 */
    @Excel(name = "base.person.cert.nation", dictType = "sys_nation")
    private String nation;

    /** 家庭住址 */
    @Excel(name = "base.person.cert.address")
    private String address;

    /** 发证机关 */
    @Excel(name = "base.person.cert.authority")
    private String certAuthority;

    /** 证件照 */
    private String certImg;

    /** 是否加密： 1-加密 0-不加密 */
    @Excel(name = "base.person.cert.encrypted", dictType = "apply_encrypted", type = Type.EXPORT)
    private String encrypted;

    /** 入学照片 */
    private String enterschoolImg;

    /** 在校照片 */
    private String inschoolImg;

    /** 毕业照片 */
    private String graduateImg;

    /** 定时任务执行时间(执行定时任务时使用此字段) */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date batchDate;

    /** 租户ID */
    private String tenantId;

    /** 证件照base64 */
    private String imgBase64;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setCertType(String certType) {
        this.certType = certType;
    }

    public String getCertType() {
        return certType;
    }

    public void setCertNum(String certNum) {
        this.certNum = certNum;
    }

    public String getCertNum() {
        return certNum;
    }

    public void setCertName(String certName) {
        this.certName = certName;
    }

    public String getCertName() {
        return certName;
    }

    public void setCertValidity(String certValidity) {
        this.certValidity = certValidity;
    }

    public String getCertValidity() {
        return certValidity;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGender() {
        return gender;
    }

    public void setBthDate(String bthDate) {
        this.bthDate = bthDate;
    }

    public String getBthDate() {
        return bthDate;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public String getNation() {
        return nation;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setCertAuthority(String certAuthority) {
        this.certAuthority = certAuthority;
    }

    public String getCertAuthority() {
        return certAuthority;
    }

    public void setCertImg(String certImg) {
        this.certImg = certImg;
    }

    public String getCertImg() {
        return certImg;
    }

    public void setEncrypted(String encrypted) {
        this.encrypted = encrypted;
    }

    public String getEncrypted() {
        return encrypted;
    }

    public void setEnterschoolImg(String enterschoolImg) {
        this.enterschoolImg = enterschoolImg;
    }

    public String getEnterschoolImg() {
        return enterschoolImg;
    }

    public void setInschoolImg(String inschoolImg) {
        this.inschoolImg = inschoolImg;
    }

    public String getInschoolImg() {
        return inschoolImg;
    }

    public void setGraduateImg(String graduateImg) {
        this.graduateImg = graduateImg;
    }

    public String getGraduateImg() {
        return graduateImg;
    }

    public void setBatchDate(Date batchDate) {
        this.batchDate = batchDate;
    }

    public Date getBatchDate() {
        return batchDate;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getImgBase64() {
        return imgBase64;
    }

    public void setImgBase64(String imgBase64) {
        this.imgBase64 = imgBase64;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
