package cn.eyecool.basedata.domain;

import lombok.Data;

/**
 * 人脸注册
 * 
 * @author mawj
 * @date 2023/04/20
 */
@Data
public class FaceRegister {
    /** 设备编码 */
    private String deviceCode;
    /** 场景编码 */
    private String channelCode;
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
    /** 人脸特征base64 */
    private String faceFeatureBase64;
    /** 操作类型 新增 add 修改update 删除 delete */
    private String optionType;
    /** 备注 */
    private String remark;
}
