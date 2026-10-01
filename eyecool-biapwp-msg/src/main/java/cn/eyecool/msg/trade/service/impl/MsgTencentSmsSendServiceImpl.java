package cn.eyecool.msg.trade.service.impl;

import java.util.ArrayList;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.github.qcloudsms.SmsMultiSender;
import com.github.qcloudsms.SmsMultiSenderResult;
import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.google.common.collect.Lists;

import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.msg.trade.entity.MsgSendResult;
import cn.eyecool.msg.trade.entity.MsgSmsSendInfo;
import cn.eyecool.msg.trade.service.IMsgSmsSendService;

/**
 * 腾讯云短信发送接口实现
 * 
 * @author admin
 * @date 2020年3月19日
 */
@Service
public class MsgTencentSmsSendServiceImpl implements IMsgSmsSendService {

    private static final Logger LOG = LoggerFactory.getLogger(MsgTencentSmsSendServiceImpl.class);

    @Value("${sms.tencent.nationcode}")
    private String nationCode;

    /**
     * 短信发送接口实现
     */
    @Override
    public MsgSendResult sendSmsMessage(MsgSmsSendInfo sendInfo) throws Exception {
        String[] toUserPhones = sendInfo.getToUserPhones();
        String toUserPhoneStr = sendInfo.getToUserPhoneStr();
        String msgType = sendInfo.getMsgType();
        toUserPhones = null != toUserPhones ? toUserPhones
            : StringUtils.isBlank(toUserPhoneStr) ? null : toUserPhoneStr.split(SPLIT);
        if (null == toUserPhones) {
            throw new CustomException(MessageUtils.message("msg.sms.send.service.touser.empty"));
        }
        // 普通消息单发
        if (DictConstants.MsgType.NORMAL_MSG.equals(msgType) && toUserPhones.length == 1) {
            return sendSingleNormalSms(sendInfo);
        }
        // 普通消息群发
        if (DictConstants.MsgType.NORMAL_MSG.equals(msgType) && toUserPhones.length > 1) {
            return sendMultiNormalSms(sendInfo);
        }
        // 模板消息单发
        if (DictConstants.MsgType.TEMPLATE_MSG.equals(msgType) && toUserPhones.length == 1) {
            return sendSingleTemplateSms(sendInfo);
        }
        // 模板消息群发
        if (DictConstants.MsgType.TEMPLATE_MSG.equals(msgType) && toUserPhones.length > 1) {
            return sendMultiTemplateSms(sendInfo);
        }
        throw new CustomException(MessageUtils.message("msg.sms.send.service.msgtype.unsupported",  msgType ));
    }

    /**
     * 指定模板单发
     */
    @Override
    public MsgSendResult sendSingleTemplateSms(MsgSmsSendInfo sendInfo) throws Exception {
        checkAppIdAndSecrect(sendInfo, false);
        SmsSingleSender smsSingleSender =
            new SmsSingleSender(Integer.parseInt(sendInfo.getAppId()), sendInfo.getAppSecrect());
        SmsSingleSenderResult smsSingleSenderResult = smsSingleSender.sendWithParam(nationCode,
            sendInfo.getToUserPhones()[0], Integer.parseInt(sendInfo.getTemplateId()), sendInfo.getTemplateParams(),
            sendInfo.getSign(), null, null);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Template SMS Single Sending Results:{}", JSON.toJSONString(smsSingleSenderResult));
        }
        MsgSendResult result = new MsgSendResult();
        result.setStatusCode(
            smsSingleSenderResult.result == 0 ? DictConstants.MsgResult.SUCCESS : DictConstants.MsgResult.FAIL);
        result.setErrmsg(smsSingleSenderResult.result + ":" + smsSingleSenderResult.errMsg);
        result.setJsonResponse(JSON.toJSONString(smsSingleSenderResult));
        return result;

    }

    /**
     * 普通单发
     */
    @Override
    public MsgSendResult sendSingleNormalSms(MsgSmsSendInfo sendInfo) throws Exception {
        checkAppIdAndSecrect(sendInfo, false);
        SmsSingleSender smsSingleSender =
            new SmsSingleSender(Integer.parseInt(sendInfo.getAppId()), sendInfo.getAppSecrect());
        SmsSingleSenderResult smsSingleSenderResult =
            smsSingleSender.send(0, nationCode, sendInfo.getToUserPhones()[0], sendInfo.getMsgContent(), null, null);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Normal SMS Single Sending Results:{}", JSON.toJSONString(smsSingleSenderResult));
        }
        MsgSendResult result = new MsgSendResult();
        result.setStatusCode(
            smsSingleSenderResult.result == 0 ? DictConstants.MsgResult.SUCCESS : DictConstants.MsgResult.FAIL);
        result.setErrmsg(smsSingleSenderResult.result + ":" + smsSingleSenderResult.errMsg);
        result.setJsonResponse(JSON.toJSONString(smsSingleSenderResult));
        return result;
    }

    /**
     * 指定模板群发
     */
    @Override
    public MsgSendResult sendMultiTemplateSms(MsgSmsSendInfo sendInfo) throws Exception {
        checkAppIdAndSecrect(sendInfo, true);
        SmsMultiSender smsMultiSender =
            new SmsMultiSender(Integer.parseInt(sendInfo.getAppId()), sendInfo.getAppSecrect());
        SmsMultiSenderResult smsMultiSenderResult = smsMultiSender.sendWithParam(nationCode,
            Lists.newArrayList(sendInfo.getToUserPhones()), Integer.parseInt(sendInfo.getTemplateId()),
            sendInfo.getTemplateParams(), sendInfo.getSign(), null, null);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Template SMS Bulk Sending Results:{}", JSON.toJSONString(smsMultiSenderResult));
        }
        MsgSendResult result = new MsgSendResult();
        result.setStatusCode(
            smsMultiSenderResult.result == 0 ? DictConstants.MsgResult.SUCCESS : DictConstants.MsgResult.FAIL);
        result.setErrmsg(smsMultiSenderResult.result + ":" + smsMultiSenderResult.errMsg);
        result.setJsonResponse(JSON.toJSONString(smsMultiSenderResult));
        return result;
    }

    /**
     * 普通群发
     */
    @Override
    public MsgSendResult sendMultiNormalSms(MsgSmsSendInfo sendInfo) throws Exception {
        checkAppIdAndSecrect(sendInfo, true);
        SmsMultiSender smsMultiSender =
            new SmsMultiSender(Integer.parseInt(sendInfo.getAppId()), sendInfo.getAppSecrect());
        SmsMultiSenderResult smsMultiSenderResult =
            smsMultiSender.send(0, nationCode, sendInfo.getToUserPhones(), sendInfo.getMsgContent(), null, null);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Ordinary SMS group sending results:{}", JSON.toJSONString(smsMultiSenderResult));
        }
        MsgSendResult result = new MsgSendResult();
        result.setStatusCode(
            smsMultiSenderResult.result == 0 ? DictConstants.MsgResult.SUCCESS : DictConstants.MsgResult.FAIL);
        result.setErrmsg(smsMultiSenderResult.result + ":" + smsMultiSenderResult.errMsg);
        result.setJsonResponse(JSON.toJSONString(smsMultiSenderResult));
        return result;
    }

    /**
     * 校验短信消息参数
     * 
     * @param sendInfo
     * @param isMass
     */
    private void checkAppIdAndSecrect(MsgSmsSendInfo sendInfo, boolean isMass) {
        String appId = sendInfo.getAppId();
        String appSecrect = sendInfo.getAppSecrect();
        String[] toUserPhones = sendInfo.getToUserPhones();
        String toUserPhoneStr = sendInfo.getToUserPhoneStr();
        String templateId = sendInfo.getTemplateId();
        ArrayList<String> templateParams = sendInfo.getTemplateParams();
        String templateParamStr = sendInfo.getTemplateParamStr();
        String sign = sendInfo.getSign();
        toUserPhones = null != toUserPhones ? toUserPhones
            : StringUtils.isBlank(toUserPhoneStr) ? null : toUserPhoneStr.split(SPLIT);
        templateParams = CollectionUtils.isNotEmpty(templateParams) ? templateParams
            : StringUtils.isBlank(templateParamStr) ? null : Lists.newArrayList(templateParamStr.split(SPLIT));
        sendInfo.setToUserPhones(toUserPhones);
        sendInfo.setTemplateParams(templateParams);
        if (StringUtils.isBlank(appId)) {
            throw new CustomException(MessageUtils.message("msg.sms.send.service.appid.empty"));
        }
        if (StringUtils.isBlank(appSecrect)) {
            throw new CustomException(MessageUtils.message("msg.sms.send.service.appsecret.empty"));
        }
        try {
            Integer.parseInt(sendInfo.getAppId());
        } catch (Exception e) {
            throw new CustomException(MessageUtils.message("msg.sms.send.service.appid.need.number"));
        }
        if (null == toUserPhones) {
            throw new CustomException(MessageUtils.message("msg.sms.send.service.touser.empty"));
        }
        if (!isMass && toUserPhones.length > 1) {
            throw new CustomException(MessageUtils.message("msg.sms.send.service.touser.need.single"));
        }
        if (DictConstants.MsgType.TEMPLATE_MSG.equals(sendInfo.getMsgType())) {
            if (StringUtils.isBlank(templateId)) {
                throw new CustomException(MessageUtils.message("msg.sms.send.service.templateid.empty"));
            }
            if (CollectionUtils.isEmpty(templateParams)) {
                throw new CustomException(MessageUtils.message("msg.sms.send.service.templatedata.empty"));
            }
            if (StringUtils.isBlank(sign)) {
                throw new CustomException(MessageUtils.message("msg.sms.send.service.smssign.empty"));
            }
            try {
                Integer.parseInt(templateId);
            } catch (Exception e) {
                throw new CustomException(MessageUtils.message("msg.sms.send.service.templateid.need.number"));
            }
        }
    }

}
