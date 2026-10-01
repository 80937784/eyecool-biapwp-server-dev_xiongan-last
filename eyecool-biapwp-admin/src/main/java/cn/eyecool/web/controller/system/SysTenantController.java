package cn.eyecool.web.controller.system;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.eyecool.common.annotation.RepeatSubmit;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.UserConstants;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.entity.SysTenant;
import cn.eyecool.common.core.domain.entity.SysUser;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.msg.trade.entity.MsgSendResult;
import cn.eyecool.msg.trade.entity.MsgSmsSendInfo;
import cn.eyecool.msg.trade.service.IMsgSendService;
import cn.eyecool.system.service.ISysTenantService;
import cn.eyecool.system.service.ISysUserService;

/**
 * 租户信息Controller
 * 
 * @author admin
 * @date 2020-11-04
 */
@RestController
@RequestMapping("/system/tenant")
public class SysTenantController extends BaseController {

    private static final String TENANT_REGISTER_CAPTCHA_REDIS_KEY_PREFIX = "tenant:register:captcha-";

    @Autowired
    private ISysTenantService sysTenantService;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private IMsgSendService msgSendService;

    @Value("${common.sms.appid}")
    private String smsAppId;
    @Value("${common.sms.appSecrect:default}")
    private String smsAppSecrect;
    @Value("${tenant.register.captcha.sms.templateId}")
    private String smsTemplateId;
    @Value("${tenant.register.captcha.sms.sign}")
    private String smsSign;

    /**
     * 注册租户信息
     */
    @PostMapping("/register")
    @RepeatSubmit
    public AjaxResult register(@RequestBody SysTenant sysTenant) {
        String captcha =
            (String)redisCache.getCacheObject(TENANT_REGISTER_CAPTCHA_REDIS_KEY_PREFIX + sysTenant.getPhone());
        if (StringUtils.isBlank(captcha)) {
            String msg = MessageUtils.message("verification.code.expired");
            return AjaxResult.error(msg);
        }
        if (!sysTenant.getCaptcha().equals(captcha)) {
            String msg = MessageUtils.message("verification.code.wrong");
            return AjaxResult.error(msg);
        }
        sysTenant.setEffectiveTime(new Date());
        sysTenant.setExpireTime(DateUtils.addDays(new Date(), 7));
        sysTenant.setCreateSource(DictConstants.TenantSource.REGIST);
        sysTenant.setTenantType(DictConstants.TenantType.TRIAL);
        sysTenant.setTenantState(DictConstants.TenantState.FROZEN);
        AjaxResult ajaxResult = toAjax(sysTenantService.insertSysTenant(sysTenant));
        redisCache.deleteObject(TENANT_REGISTER_CAPTCHA_REDIS_KEY_PREFIX + sysTenant.getPhone());
        return ajaxResult;
    }

    /**
     * 获取注册租户验证码
     */
    @PostMapping("/register/captcha/{phone}")
    public AjaxResult register(@PathVariable("phone") String phone) {
        String captcha = (String)redisCache.getCacheObject(TENANT_REGISTER_CAPTCHA_REDIS_KEY_PREFIX + phone);
        if (StringUtils.isNotBlank(captcha)) {
            String msg = MessageUtils.message("verification.code.not.try");
            return AjaxResult.error(msg);
        }
        captcha = RandomStringUtils.random(4, "0123456789");
        MsgSmsSendInfo sendInfo = new MsgSmsSendInfo();
        sendInfo.setAppId(smsAppId);
        sendInfo.setAppSecrect(smsAppSecrect);
        String msg = MessageUtils.message("verification.code.sms.title");
        sendInfo.setMsgSubject(msg);
        sendInfo.setToUserPhoneStr(phone);
        sendInfo.setMsgType(DictConstants.MsgType.TEMPLATE_MSG);
        sendInfo.setTemplateParamStr(captcha + ",5");
        sendInfo.setSign(smsSign);
        sendInfo.setTemplateId(smsTemplateId);
        MsgSendResult sendResult = msgSendService.sendSmsMessage(sendInfo);
        if (sendResult.getStatusCode().equals(DictConstants.MsgResult.FAIL)) {
            String emsg =MessageUtils.message("verification.code.send.fail");
            return AjaxResult.error(emsg);
        } else {
            redisCache.setCacheObject(TENANT_REGISTER_CAPTCHA_REDIS_KEY_PREFIX + phone, captcha, 5, TimeUnit.MINUTES);
            String emsg =MessageUtils.message("verification.code.sent.tip");
            return AjaxResult.error(emsg);
        }
    }

    /**
     * 校验注册手机号唯一性
     */
    @PostMapping("/register/checkPhoneUnique/{phone}")
    public AjaxResult checkPhoneUnique(@PathVariable("phone") String phone) {
        SysUser user = new SysUser();
        user.setPhonenumber(phone);
        String phoneUnique = sysUserService.checkPhoneUnique(user);
        return AjaxResult.success(UserConstants.UNIQUE.equals(phoneUnique));
    }

    /**
     * 校验注册邮箱唯一性
     */
    @PostMapping("/register/checkEmailUnique/{email}")
    public AjaxResult checkEmailUnique(@PathVariable("email") String email) {
        SysUser user = new SysUser();
        user.setEmail(email);
        String phoneUnique = sysUserService.checkEmailUnique(user);
        return AjaxResult.success(UserConstants.UNIQUE.equals(phoneUnique));
    }

    /**
     * 校验租户名称唯一性D
     */
    @PostMapping("/register/checkTenantNameUnique/{tenantName}")
    public AjaxResult checkTenantNameUnique(@PathVariable("tenantName") String tenantName) {
        boolean tenantNameUnique = sysTenantService.checkTenantNameUnique(null, tenantName);
        return AjaxResult.success(tenantNameUnique);
    }

}
