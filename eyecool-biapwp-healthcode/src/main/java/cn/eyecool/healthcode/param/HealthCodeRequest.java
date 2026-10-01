/*******************************************************************************
 * 系统名称 ： 后台管理系统 开发部门 ： 山东眼神智能科技有限公司 文件名称 : HealthCodeRequest
 ******************************************************************************/
package cn.eyecool.healthcode.param;

/**
 * 健康码查询请求对象
 *
 * @author zfx
 * @since 2021/1/29 16:50
 **/
public class HealthCodeRequest {
    /**
     * 身份证号
     */
    private String idCarNo;
    /**
     * 姓名
     */
    private String name;
    /**
     * 性别
     */
    private String gender;
    /**
     * 民族
     */
    private String nation;
    /**
     * 出生日期 （YYYY-MM-DD）
     */
    private String birthday;
    /**
     * 户籍地
     */
    private String regAddress;
    /**
     * 身份证照片 (Base64)
     */
    private String photo;
    /**
     * 设备编码
     */
    private String deviceCode;
    /**
     * 刷卡站点
     */
    private String site;
    /**
     * 人脸测温温度
     */
    private String temperature;
    /**
     * 区域 字典
     */
    private String region;
    /**
     * 请求时间yyyy-MM-dd HH:mm:ss
     */
    private String requestTime;
    /**
     * 请求流水号
     */
    private String requestSeq;
    /**
     * 身份证有效期
     */
    private String certValidity;
    /**
     * 发证机关
     */
    private String certAuthority;
    /**
     * 车牌号
     */
    private String carNo;
    /** 济南公共数据平台中对一个申请接入审核通过后的签名认证令牌 */
    private String xClientId;
    private String xCientSecert;

    public String getIdCarNo() {
        return idCarNo;
    }

    public void setIdCarNo(String idCarNo) {
        this.idCarNo = idCarNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getRegAddress() {
        return regAddress;
    }

    public void setRegAddress(String regAddress) {
        this.regAddress = regAddress;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public String getCarNo() {
        return carNo;
    }

    public void setCarNo(String carNo) {
        this.carNo = carNo;
    }

    public String getRequestSeq() {
        return requestSeq;
    }

    public void setRequestSeq(String requestSeq) {
        this.requestSeq = requestSeq;
    }

    public String getCertValidity() {
        return certValidity;
    }

    public void setCertValidity(String certValidity) {
        this.certValidity = certValidity;
    }

    public String getCertAuthority() {
        return certAuthority;
    }

    public void setCertAuthority(String certAuthority) {
        this.certAuthority = certAuthority;
    }

    public String getxClientId() {
        return xClientId;
    }

    public void setxClientId(String xClientId) {
        this.xClientId = xClientId;
    }

    public String getxCientSecert() {
        return xCientSecert;
    }

    public void setxCientSecert(String xCientSecert) {
        this.xCientSecert = xCientSecert;
    }
}
