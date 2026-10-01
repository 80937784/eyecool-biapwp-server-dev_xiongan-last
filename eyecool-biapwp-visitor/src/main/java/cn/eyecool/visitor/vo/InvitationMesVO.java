package cn.eyecool.visitor.vo;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 邀请信息VO
 * 
 * @Author Administrator
 * @create 2021/10/21 14:57
 */
@Data
public class InvitationMesVO {

    /** 邀请人 **/
    private String inviterName;
    /** 邀请生效时间 **/
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date effectiveBeginTime;
    /** 邀请结束时间 **/
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date effectiveEndTime;
    /** 拜访区域 **/
    private String[] subSceneInfoIds;
    /** 访客名称 **/
    private String visitorName;
    /** 访客手机号 **/
    private String visitorPhone;
    /** 访客身份证号 **/
    private String visitorIDCard;
    /** 访客所在公司 **/
    private String visitorCompanyName;
    /** 访客在其公司的职位 **/
    private String visitorPosition;
    /** 访客人脸图片base64数据 **/
    private String visitorFaceBase64;

}
