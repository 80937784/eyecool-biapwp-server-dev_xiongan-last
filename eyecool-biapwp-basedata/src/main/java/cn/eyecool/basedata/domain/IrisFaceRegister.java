package cn.eyecool.basedata.domain;

/**
 * 人脸虹膜多模态Http注册bean
 *
 * @author zfx
 * @since 2021/1/14 11:26
 **/
public class IrisFaceRegister {

    /** 设备编码 */
    private String deviceCode;
    /** 人员唯一编号 */
    private String uniqueId;
    /** 人员姓名 */
    private String name;
    /** 身份证号 */
    private String idCardNo;
    /** 卡号 */
    private String cardNo;
    /** 手机号 */
    private String phone;
    /** 人脸照片base64 */
    private String faceBase64Img;
    /** 虹膜照片base64 */
    private String irisBase64Img;
    /** 人脸特征base64 */
    private String faceFeatureBase64;
    /** 虹膜特征base64 */
    private String irisFeatureBase64;
    /** 数据来源 可以是设备编号，三方标识等 */
    private String dataDescribe;
    /** 操作类型 新增 add 修改update 删除 delete */
    private String optionType;
    /** 人员拓展属性JSON */
    private String extAttrs;

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdCardNo() {
        return idCardNo;
    }

    public void setIdCardNo(String idCardNo) {
        this.idCardNo = idCardNo;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFaceBase64Img() {
        return faceBase64Img;
    }

    public void setFaceBase64Img(String faceBase64Img) {
        this.faceBase64Img = faceBase64Img;
    }

    public String getIrisBase64Img() {
        return irisBase64Img;
    }

    public void setIrisBase64Img(String irisBase64Img) {
        this.irisBase64Img = irisBase64Img;
    }

    public String getFaceFeatureBase64() {
        return faceFeatureBase64;
    }

    public void setFaceFeatureBase64(String faceFeatureBase64) {
        this.faceFeatureBase64 = faceFeatureBase64;
    }

    public String getIrisFeatureBase64() {
        return irisFeatureBase64;
    }

    public void setIrisFeatureBase64(String irisFeatureBase64) {
        this.irisFeatureBase64 = irisFeatureBase64;
    }

    public String getDataDescribe() {
        return dataDescribe;
    }

    public void setDataDescribe(String dataDescribe) {
        this.dataDescribe = dataDescribe;
    }

    public String getOptionType() {
        return optionType;
    }

    public void setOptionType(String optionType) {
        this.optionType = optionType;
    }

    public String getExtAttrs() {
        return extAttrs;
    }

    public void setExtAttrs(String extAttrs) {
        this.extAttrs = extAttrs;
    }

}
