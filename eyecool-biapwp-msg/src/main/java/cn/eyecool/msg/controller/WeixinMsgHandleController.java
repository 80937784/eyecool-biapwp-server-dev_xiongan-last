package cn.eyecool.msg.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.validator.routines.LongValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.weixin4j.WeixinException;
import org.weixin4j.http.HttpsClient;
import org.weixin4j.http.Response;

import com.alibaba.fastjson.JSONObject;

import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.ServletUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.msg.constant.MsgRedisCacheConstants;
import cn.eyecool.msg.domain.MsgOfficalAccount;
import cn.eyecool.msg.domain.MsgOfficalAccountUser;
import cn.eyecool.msg.service.IMsgOfficalAccountService;
import cn.eyecool.msg.service.IMsgOfficalAccountUserService;
import cn.eyecool.msg.trade.entity.MsgSendResult;
import cn.eyecool.msg.trade.entity.MsgSmsSendInfo;
import cn.eyecool.msg.trade.handler.WeixinMessageHandler;
import cn.eyecool.msg.trade.service.IMsgSendService;

/**
 * 微信消息事件处理器
 * 
 * @author admin
 * @date 2020年3月25日
 */
@RestController
@RequestMapping(WeixinMsgHandleController.API_PREFIX)
public class WeixinMsgHandleController extends BaseController {

    private static final Logger LOG = LoggerFactory.getLogger(WeixinMsgHandleController.class);

    protected static final String API_PREFIX = "/api/mp";

    @Value("${weixin.oauth.redirecturl}")
    private String redirectUrl;
    @Value("${weixin.bind.webRouter}")
    private String bindPhoneWebRouter;
    @Value("${weixin.bind.captcha.sms.templateId}")
    private String smsTemplateId;
    @Value("${weixin.bind.captcha.sms.sign}")
    private String smsSign;
    @Value("${common.sms.appid}")
    private String smsAppId;
    @Value("${common.sms.appSecrect:default}")
    private String smsAppSecrect;
    @Autowired
    private WeixinMessageHandler weixinMsgHandler;
    @Autowired
    private IMsgOfficalAccountService officalAccountService;
    @Autowired
    private IMsgOfficalAccountUserService officalAccountUserService;
    @Autowired
    private IMsgSendService msgSendService;
    @Autowired
    private TenantProperties tenantProperties;
    @Autowired
    private RedisCache redisCache;

    /**
     * 校验信息是否是从微信服务器发出，处理消息
     * <p>
     * 【 开发者提交信息后，微信服务器将发送GET请求到填写的服务器地址URL上，GET请求携带参数如下表所示： 1、signature
     * 微信加密签名，signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数。 2、timestamp 时间戳 3、nonce 随机数 4、echostr 随机字符串 】
     * </p>
     * <p>
     * 注意，在微信公众平台配置该地址的时候，一定要拼接?appid=公众号AppId 目前消息只支持明文，选择兼容模式和安全模式需要提前配置好相关加解密代码。 如果需要加解密， URL中增加 2个参数分别是
     * encrypt_type（加密类型，为 aes）和 msg_signature（消息体签名，用于验证消息体的正确性）
     * </p>
     * 
     * @param request
     * @param out
     * @throws IOException
     */
    @RequestMapping(value = "/callback", method = {RequestMethod.GET, RequestMethod.POST})
    public String callback() throws Exception {
        HttpServletRequest request = ServletUtils.getRequest();
        String appId = request.getParameter("appid");// 公众号AppId
        logger.info("Get WeChat public account appid:{}", appId);
        try {
            if (StringUtils.isNotBlank(appId)) {
                MsgOfficalAccount officalAccount = getOfficalAccountByAppId(appId);
                if (tenantProperties.getEnabled() && null != officalAccount) {
                    String tenantId = officalAccount.getTenantId();
                    TenantContextHolder.setTenantId(tenantId);
                }
            }
            return weixinMsgHandler.invoke(request.getInputStream(), appId);
        } finally {
            TenantContextHolder.clear();
        }
    }

    /**
     * 公众号菜单跳转页面绑定手机号,地址重定向 注意，在微信公众平台配置该地址的时候，一定要拼接?appid=公众号AppId
     * 
     * @param request
     * @return
     */
    @GetMapping("/redirect/bind")
    public void wechatRedirect(HttpServletRequest request) {
        String url = null;
        String appId = request.getParameter("appid");// 公众号AppId
        logger.info("公众号appid is:" + appId);
        try {
            // 这个是我们回调的地址, 在这里进行出来获取到用户的openId
            url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + appId + "&redirect_uri="
                + URLEncoder.encode(redirectUrl + API_PREFIX + "/bind?appid=" + appId, "utf-8") + "&response_type=code"
                + "&scope=snsapi_base" + "&state=123#wechat_redirect";
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage(), e);
        }
        try {
            // 必须重定向，否则不能成功
            logger.info("--forward redirect address-------:" + url);
            ServletUtils.getResponse().sendRedirect(url);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * 用户手机号绑定页面
     * 
     * @param modelMap
     * @param httpServletRequest
     * @param httpServletResponse
     * @return
     */
    @GetMapping("/bind")
    public void bindPhone(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.getParameterMap().forEach((key, value) -> logger.info("key:{},value:{}", key, value));
        String code = request.getParameter("code");
        logger.info("Authorization return code information---------:{}", code);
        String appId = request.getParameter("appid");
        if (StringUtils.isBlank(appId)) {
            logger.error("Failed to get official account appId");
            throw new WeixinException("appid get failed！");
        }
        MsgOfficalAccount officalAccount = getOfficalAccountByAppId(appId);
        if (null == officalAccount) {
            logger.error("The official account corresponding to appId[{}] is not maintained on the platform", appId);
            throw new WeixinException(MessageUtils.message("weixin.msg.handler.account.not.exists"));
        }
        try {
            if (tenantProperties.getEnabled()) {
                String tenantId = officalAccount.getTenantId();
                TenantContextHolder.setTenantId(tenantId);
            }
            String appSecrect = officalAccount.getAppSecrect();
            String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + appId + "&secret=" + appSecrect
                + "&code=" + code + "&grant_type=authorization_code";
            HttpsClient http = new HttpsClient();
            Response res = http.get(url);
            JSONObject jsonObj = res.asJSONObject();
            String openId = jsonObj.getString("openid");
            logger.info("appid:{},put openid:{}", appId, jsonObj.getString("openid"));
            // 查询此openId是否已经绑定手机号
            MsgOfficalAccountUser userInfo = getOfficalAccountUser(appId, openId, null);
            if (null == userInfo) {
                throw new WeixinException(MessageUtils.message("weixin.msg.handler.bind.account"));
            }
            String phone = userInfo.getPhone();
            try {
                String webUrl = redirectUrl + bindPhoneWebRouter + "?appId=" + appId + "&openId=" + openId;
                if (StringUtils.isNotBlank(phone)) {
                    webUrl += "&phone=" + phone;
                }
                ServletUtils.getResponse().sendRedirect(webUrl);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        } finally {
            TenantContextHolder.clear();
        }
    }

    /**
     * 执行手机号绑定
     * 
     * @param appId
     * @param openId
     * @param phone
     * @param captchaContent
     * @return
     */
    @PostMapping("/bind")
    public AjaxResult wechatBindtoScene(String appId, String openId, String phone, String captchaContent) {
        logger.info("openId:{},phone:{},captchaContent:{}", openId, phone, captchaContent);
        if (StringUtils.isBlank(appId) || StringUtils.isBlank(openId)) {
            logger.error("appId is empty or openId is empty!");
            return AjaxResult.error(MessageUtils.message("weixin.msg.handler.bind.account"));
        }
        if (StringUtils.isBlank(phone)) {
            logger.error("bind phone is empty!");
            return AjaxResult.error(MessageUtils.message("weixin.msg.handler.phone.empty"));
        }
        MsgOfficalAccount officalAccount = getOfficalAccountByAppId(appId);
        if (null == officalAccount) {
            logger.error("The official account corresponding to appId[{}] is not maintained on the platform", appId);
            return AjaxResult.error(MessageUtils.message("weixin.msg.handler.account.not.exists"));
        }
        try {
            if (tenantProperties.getEnabled()) {
                String tenantId = officalAccount.getTenantId();
                TenantContextHolder.setTenantId(tenantId);
            }
            MsgOfficalAccountUser userInfo = getOfficalAccountUser(appId, null, phone);
            if (null != userInfo) {
                if (userInfo.getOpenId().equals(openId)) {
                    return AjaxResult.error(MessageUtils.message("weixin.msg.handler.phone.been.bind"));
                } else {
                    return AjaxResult.error(MessageUtils.message("weixin.msg.handler.phone.been.bind.another"));
                }
            }
            // 查询是否关注公众号
            userInfo = getOfficalAccountUser(appId, openId, null);
            if (null == userInfo) {
                return AjaxResult.error(MessageUtils.message("weixin.msg.handler.bind.account"));
            }
            String captcha = (String)redisCache
                .getCacheObject(MsgRedisCacheConstants.WECHAT_PHONE_BIND_CAPTCHAT_CACHE_KEY_PREFIX + phone);
            if (StringUtils.isBlank(captcha) || !captcha.equals(captchaContent)) {
                return AjaxResult.error(MessageUtils.message("weixin.msg.handler.captcha.wrong"));
            }
            userInfo.setPhone(phone);
            officalAccountUserService.updateMsgOfficalAccountUser(userInfo);
            redisCache.deleteObject(MsgRedisCacheConstants.WECHAT_PHONE_BIND_CAPTCHAT_CACHE_KEY_PREFIX + phone);
            return AjaxResult.success(MessageUtils.message("weixin.msg.handler.bind.success"));
        } finally {
            TenantContextHolder.clear();
        }

    }

    /**
     * 获取验证码
     * 
     * @param appId 微信公众号AppId
     * @param openId
     * @param phone
     * @return
     */
    @GetMapping("/captcha")
    public AjaxResult captcha(String appId, String openId, String phone) {
        if (StringUtils.isBlank(appId) || StringUtils.isBlank(openId)) {
            logger.error("appId is empty or openId is empty!");
            return AjaxResult.error(MessageUtils.message("weixin.msg.handler.bind.account"));
        }
        if (StringUtils.isBlank(phone)) {
            logger.error("bind phone is empty!");
            return AjaxResult.error(MessageUtils.message("weixin.msg.handler.input.phone"));
        }
        if (phone.length() != 11 || !LongValidator.getInstance().isValid(phone)) {
            logger.error("bind phone=[{}] is not valid!", phone);
            return AjaxResult.error(MessageUtils.message("weixin.msg.handler.input.right.phone"));
        }
        // 验证AppId和openId，防止浪费短信条数
        MsgOfficalAccount officalAccount = getOfficalAccountByAppId(appId);
        if (null == officalAccount) {
            logger.error("The official account corresponding to appId[{}] is not maintained on the platform", appId);
            return AjaxResult.error(MessageUtils.message("weixin.msg.handler.account.not.exists"));
        }
        try {
            if (tenantProperties.getEnabled()) {
                String tenantId = officalAccount.getTenantId();
                TenantContextHolder.setTenantId(tenantId);
            }
            // 验证AppId和openId，防止浪费短信条数
            MsgOfficalAccountUser accountUser = getOfficalAccountUser(appId, openId, null);
            if (null == accountUser) {
                return AjaxResult.error(MessageUtils.message("weixin.msg.handler.bind.account"));
            }
            String captcha = (String)redisCache
                .getCacheObject(MsgRedisCacheConstants.WECHAT_PHONE_BIND_CAPTCHAT_CACHE_KEY_PREFIX + phone);
            if (StringUtils.isNotBlank(captcha)) {
                return AjaxResult.error(MessageUtils.message("weixin.msg.handler.captcha.valid.msg"));
            }
            captcha = RandomStringUtils.random(4, "0123456789");
            MsgSmsSendInfo sendInfo = new MsgSmsSendInfo();
            sendInfo.setAppId(smsAppId);
            sendInfo.setAppSecrect(smsAppSecrect);
            sendInfo.setMsgSubject(MessageUtils.message("weixin.msg.handler.msg.subject"));
            sendInfo.setToUserPhoneStr(phone);
            sendInfo.setMsgType(DictConstants.MsgType.TEMPLATE_MSG);
            sendInfo.setTemplateParamStr(captcha + ",5");
            sendInfo.setSign(smsSign);
            sendInfo.setTemplateId(smsTemplateId);
            MsgSendResult sendResult = msgSendService.sendSmsMessage(sendInfo);
            if (sendResult.getStatusCode().equals(DictConstants.MsgResult.FAIL)) {
                return AjaxResult.error(MessageUtils.message("weixin.msg.handler.captcha.send.failed"));
            } else {
                redisCache.setCacheObject(MsgRedisCacheConstants.WECHAT_PHONE_BIND_CAPTCHAT_CACHE_KEY_PREFIX + phone,
                    captcha, 5, TimeUnit.MINUTES);
                return AjaxResult.success(MessageUtils.message("weixin.msg.handler.captcha.send.success"));
            }
        } finally {
            TenantContextHolder.clear();
        }

    }

    /**
     * 查询公众号用户信息
     * 
     * @param appId
     * @param openId
     * @param phone
     * @return
     */
    private MsgOfficalAccountUser getOfficalAccountUser(String appId, String openId, String phone) {
        MsgOfficalAccountUser userCondition = new MsgOfficalAccountUser();
        userCondition.setAppId(appId);
        userCondition.setOpenId(openId);
        userCondition.setPhone(phone);
        List<MsgOfficalAccountUser> userList = officalAccountUserService.selectMsgOfficalAccountUserList(userCondition);
        if (CollectionUtils.isEmpty(userList)) {
            return null;
        }
        return userList.get(0);
    }

    /**
     * 根据appId查询设备信息
     * 
     * @param appId
     * @return
     */
    private MsgOfficalAccount getOfficalAccountByAppId(String appId) {
        MsgOfficalAccount condition = new MsgOfficalAccount();
        condition.setAppId(appId);
        List<MsgOfficalAccount> accountList = officalAccountService.selectMsgOfficalAccountList(condition);
        if (CollectionUtils.isEmpty(accountList)) {
            return null;
        }
        return accountList.get(0);
    }

}
