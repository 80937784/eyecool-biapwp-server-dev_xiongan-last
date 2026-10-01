package cn.eyecool.web.controller.system;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.entity.SysUser;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.msg.trade.entity.MsgSendResult;
import cn.eyecool.msg.trade.entity.MsgSmsSendInfo;
import cn.eyecool.msg.trade.service.IMsgSendService;
import cn.eyecool.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户密码维护控制器
 * 
 * @author mawj
 * @date 2021/08/09
 */
@Slf4j
@RestController
@RequestMapping("/system/userSecurity")
public class SysUserPwdController {

    @Autowired
    private ISysUserService userService;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private IMsgSendService msgSendService;

    @Value("${common.sms.appid:default}")
    private String smsAppId;
    @Value("${common.sms.appSecrect:default}")
    private String smsAppSecrect;
    @Value("${userpwd.getback.smscaptcha.templateId:test}")
    private String smsTemplateId;
    @Value("${userpwd.getback.smscaptcha.sign:test}")
    private String smsSign;

    /** 密码找回手机验证码缓存key */
    static final String USERPWD_GETBACK_CAPTCHA_REDIS_KEY_PREFIX = "userpwd:getback:captcha-";

    /**
     * 校验用户名是否合法
     * 
     * @param userName
     * @return
     */
    @PostMapping("/validUserName")
    public AjaxResult validUserName(String userName) {
        SysUser sysUser = userService.selectUserByUserName(userName);
        return AjaxResult.success(!StringUtils.isNull(sysUser));
    }

    /**
     * 升级密码
     * 
     * @param userName
     * @param oldPassword
     * @param newPassword
     * @return
     */
    @PostMapping("/upgradePwd")
    public AjaxResult upgradePwd(String userName, String oldPassword, String newPassword) {
        SysUser sysUser = userService.selectUserByUserName(userName);
        if (StringUtils.isNull(sysUser)) {
            log.error("The user [{}] upgrade the password failed,the user is not exist", userName);
            String msg = MessageUtils.message("upgrade.password.fail.user.not.exist");
            return AjaxResult.error(msg);
        }
        String password = sysUser.getPassword();
        if (!SecurityUtils.matchesPassword(oldPassword, password)) {
            log.error("The user [{}] upgrade the password failed,the old password is wrong", userName);
            String msg = MessageUtils.message("password.update.fail.old.wrong");
            return AjaxResult.error(msg);
        }
        if (SecurityUtils.matchesPassword(newPassword, password)) {
            log.error("The user [{}] upgrade the password failed,the new password can not be the same with the old one.", userName);
            String msg = MessageUtils.message("password.update.fail.not.equal");
            return AjaxResult.error(msg);
        }
        // TODO 验证密码等级是否符合要求
        if (userService.resetUserPwd(userName, SecurityUtils.encryptPassword(newPassword)) > 0) {
            log.info("The user [{}] upgrade the password", userName);
            return AjaxResult.success();
        }
        return AjaxResult.error(MessageUtils.message("password.update.fail.msg"));
    }

    /**
     * 找回密码获取验证码
     */
    @PostMapping("/getbackPwd/catpcha")
    public AjaxResult getbackPwdCatpcha(String userName, String phone) {
        SysUser sysUser = userService.selectUserByUserName(userName);
        if (StringUtils.isNull(sysUser)) {
            log.error("The user [{}] get the verification code failed for wanted to get the password,the user is not exist", userName);
            return AjaxResult.error(MessageUtils.message("verification.code.failed.user.not.exist"));
        }
        if (!phone.equals(sysUser.getPhonenumber())) {
            log.error("The phone number[{}] does not match the number[{}] bound to the user[{}].", phone, sysUser.getPhonenumber(), userName);
            return AjaxResult.error(MessageUtils.message("phone.not.match.bound"));
        }
        String redisKey = USERPWD_GETBACK_CAPTCHA_REDIS_KEY_PREFIX + phone;
        String captcha = (String)redisCache.getCacheObject(redisKey);
        if (StringUtils.isNotBlank(captcha)) {
            return AjaxResult.error(MessageUtils.message("verification.code.doesnot.repeat"));
        }
        captcha = RandomStringUtils.random(4, "0123456789");
        MsgSmsSendInfo sendInfo = new MsgSmsSendInfo();
        sendInfo.setAppId(smsAppId);
        sendInfo.setAppSecrect(smsAppSecrect);
        sendInfo.setMsgSubject(MessageUtils.message("password.back.cms.subject"));
        sendInfo.setToUserPhoneStr(phone);
        sendInfo.setMsgType(DictConstants.MsgType.TEMPLATE_MSG);
        sendInfo.setTemplateParamStr(captcha);
        sendInfo.setSign(smsSign);
        sendInfo.setTemplateId(smsTemplateId);
        MsgSendResult sendResult = msgSendService.sendSmsMessage(sendInfo);
        if (sendResult.getStatusCode().equals(DictConstants.MsgResult.FAIL)) {
            String msg = MessageUtils.message("verification.code.sent.failed");
            return AjaxResult.error(msg);
        } else {
            redisCache.setCacheObject(redisKey, captcha, 5, TimeUnit.MINUTES);
            String msg = MessageUtils.message("verification.code.sent.msg");
            return AjaxResult.success(msg);
        }
    }

    /**
     * 找回密码
     */
    @PostMapping("/getbackPwd/reset")
    public AjaxResult getbackPwd(String userName, String phone, String captcha, String newPassword) {
        SysUser sysUser = userService.selectUserByUserName(userName);
        if (StringUtils.isNull(sysUser)) {
            log.error("The user [{}] find the password back failed,the user is not exist", userName);
            String msg = MessageUtils.message("password.back.fail.user.not.exist");
            return AjaxResult.error(msg);
        }
        if (!phone.equals(sysUser.getPhonenumber())) {
           // log.error("手机号[{}]与该用户[{}]绑定手机号[{}]不一致，请检查！", phone, userName, sysUser.getPhonenumber());
            log.error("The phone number[{}] does not match the number[{}] bound to the user[{}].", phone, sysUser.getPhonenumber(), userName);

            String msg = MessageUtils.message("phone.not.match.bound");
            return AjaxResult.error(msg);
        }
        String redisKey = USERPWD_GETBACK_CAPTCHA_REDIS_KEY_PREFIX + phone;
        String captchaCache = (String)redisCache.getCacheObject(redisKey);
        if (!captcha.equals(captchaCache)) {
            //log.error("用户[{}]找回密码失败，手机号[{}]验证码[{}]与缓存验证码[{}]不一致", userName, phone, captcha, captchaCache);
            log.error("The user [{}] found the password back failed, the phone number [{}] , the verification code [{}] is different with that [{}] in cache.", userName, phone, captcha, captchaCache);

            String msg = MessageUtils.message("password.back.fail.verification.code.wrong");
            return AjaxResult.error(msg);
        }
        // TODO 验证密码是否符合规则
        if (userService.resetUserPwd(userName, SecurityUtils.encryptPassword(newPassword)) > 0) {
            log.info("The user [{}] has found the password back and updated it", userName);
            redisCache.deleteObject(redisKey);
            return AjaxResult.success();
        }
        return AjaxResult.error(MessageUtils.message("password.update.fail.msg"));
    }
}
