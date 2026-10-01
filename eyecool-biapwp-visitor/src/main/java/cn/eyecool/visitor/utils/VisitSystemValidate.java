package cn.eyecool.visitor.utils;

import java.util.Date;

import org.apache.commons.validator.routines.LongValidator;

import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.http.HttpAjaxResult;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.visitor.vo.InvitationMesVO;
import cn.eyecool.visitor.vo.InviterInfoVO;

/**
 * 访客系统相关验证工具类
 * 
 * @Author Administrator
 * @create 2021/10/21 18:08
 */
public class VisitSystemValidate {

    /**
     * 验证一下邀请人信息
     *
     * @return
     */
    public static AjaxResult validateInviterInfo(InviterInfoVO inviterInfo) {
        String msg = null;
        AjaxResult ajax = AjaxResult.success();

        // 验证手机号
        String phone = inviterInfo.getPhone();
        if (StringUtils.isBlank(phone)) {
            msg = "手机号[phone]不能为空";
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 手机号不为空时，长度必须为11，且符合手机号校验规则
        if (StringUtils.isNotBlank(phone) && (phone.length() != 11 || !LongValidator.getInstance().isValid(phone))) {
            msg = "手机号[phone]长度必须为11且必须是数字";
            return HttpAjaxResult.businessDataValidError(msg);
        }

        // 验证姓名
        String username = inviterInfo.getUsername();
        if (StringUtils.isBlank(username)) {
            msg = "人员姓名[username]不能为空";
            return HttpAjaxResult.businessDataValidError(msg);
        }
        if (StringUtils.isNotBlank(username) && username.length() > 60) {
            msg = "人员姓名[username]长度不能大于60";
            return HttpAjaxResult.businessDataValidError(msg);
        }

        // 验证码校验
        String verifyCode = inviterInfo.getVerifyCode();
        if (StringUtils.isBlank(verifyCode)) {
            msg = "验证码[verifyCode]不能为空";
            return HttpAjaxResult.businessDataValidError(msg);
        }
        if (StringUtils.isNotBlank(verifyCode) && verifyCode.length() > 6) {
            msg = "验证码[verifyCode]长度不能大于6";
            return HttpAjaxResult.businessDataValidError(msg);
        }
        return ajax;
    }

    /**
     * 验证一下邀请信息格式是否正确
     *
     * @return
     */
    public static AjaxResult validateInvitationMes(InvitationMesVO invitationMesVO) {
        String msg = null;
        AjaxResult ajax = AjaxResult.success();

        // 验证被访人
        String inviterName = invitationMesVO.getInviterName();
        if (StringUtils.isBlank(inviterName)) {
            msg = "邀请人姓名[inviterName]不能为空";
            return HttpAjaxResult.businessDataValidError(msg);
        }
        if (StringUtils.isNotBlank(inviterName) && inviterName.length() > 60) {
            msg = "邀请人姓名[inviterName]长度不能大于60";
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 验证来访开始时间
        Date effectiveBeginTime = invitationMesVO.getEffectiveBeginTime();
        if (null == effectiveBeginTime) {
            msg = "来访开始时间[effectiveBeginTime]不能为空";
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 验证来访结束时间
        Date effectiveEndTime = invitationMesVO.getEffectiveEndTime();

        if (null == effectiveEndTime) {
            msg = "来访结束时间[effectiveEndTime]不能为空";
            return HttpAjaxResult.businessDataValidError(msg);
        }
        if (effectiveEndTime.getTime() < System.currentTimeMillis()
            || effectiveEndTime.getTime() < effectiveBeginTime.getTime()) {
            msg = "来访结束时间[effectiveEndTime]不能小于当前时间且不能早于来访开始时间";
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 验证拜访区域
        String[] subSceneInfoIds = invitationMesVO.getSubSceneInfoIds();
        if (null == subSceneInfoIds || subSceneInfoIds.length == 0) {
            msg = "拜访区域[subSceneInfoIds]不能为空";
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 验证访客名称
        String visitorName = invitationMesVO.getVisitorName();
        if (StringUtils.isBlank(visitorName)) {
            msg = "访客姓名[visitorName]不能为空";
            return HttpAjaxResult.businessDataValidError(msg);
        }
        if (StringUtils.isNotBlank(visitorName) && visitorName.length() > 60) {
            msg = "访客姓名[visitorName]长度不能大于60";
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 验证访客手机号
        String visitorPhone = invitationMesVO.getVisitorPhone();
        if (StringUtils.isBlank(visitorPhone)) {
            msg = "访客手机号[visitorPhone]不能为空";
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 手机号不为空时，长度必须为11，且符合手机号校验规则
        if (StringUtils.isNotBlank(visitorPhone)
            && (visitorPhone.length() != 11 || !LongValidator.getInstance().isValid(visitorPhone))) {
            msg = "访客手机号[visitorPhone]长度必须为11且必须是数字";
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 身份证、公司、职位非必填
        return ajax;
    }

    /**
     * 验证一下访客填写的信息格式是否正确
     *
     * @return
     */
    public static AjaxResult validateVisitUpdateMes(InvitationMesVO invitationMesVO) {
        String msg = null;
        AjaxResult ajax = AjaxResult.success();

        // 验证邀请人姓名
        String inviterName = invitationMesVO.getInviterName();
        if (StringUtils.isBlank(inviterName)) {
            msg = "邀请人姓名不能为空";
            return AjaxResult.error(msg);
        }
        if (StringUtils.isNotBlank(inviterName) && inviterName.length() > 60) {
            msg = "邀请人姓名长度不能大于60";
            return AjaxResult.error(msg);
        }
        // 验证访客姓名
        String visitorName = invitationMesVO.getVisitorName();
        if (StringUtils.isBlank(visitorName)) {
            msg = "访客姓名不能为空";
            return AjaxResult.error(msg);
        }
        if (StringUtils.isNotBlank(visitorName) && visitorName.length() > 60) {
            msg = "访客姓名长度不能大于60";
            return AjaxResult.error(msg);
        }
        // 验证访客手机号
        String visitorPhone = invitationMesVO.getVisitorPhone();
        if (StringUtils.isBlank(visitorPhone)) {
            msg = "访客手机号不能为空";
            return AjaxResult.error(msg);
        }
        // 手机号不为空时，长度必须为11，且符合手机号校验规则
        if (StringUtils.isNotBlank(visitorPhone)
            && (visitorPhone.length() != 11 || !LongValidator.getInstance().isValid(visitorPhone))) {
            msg = "访客手机号长度必须为11且必须是数字";
            return AjaxResult.error(msg);
        }
        // 访客的人脸base64信息
        String visitorFaceBase64 = invitationMesVO.getVisitorFaceBase64();
        if (StringUtils.isBlank(visitorFaceBase64)) {
            msg = "访客人脸信息不能为空";
            return AjaxResult.error(msg);
        }
        return ajax;
    }

}