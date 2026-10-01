package cn.eyecool.msg.trade.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.weixin4j.WeixinException;
import org.weixin4j.component.MessageComponent;
import org.weixin4j.model.message.template.TemplateData;

import com.alibaba.fastjson.JSON;

import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.msg.configure.weixin.PlatformWeiXin;
import cn.eyecool.msg.configure.weixin.WeixinInstanceCache;
import cn.eyecool.msg.trade.entity.MsgSendResult;
import cn.eyecool.msg.trade.entity.MsgWeixinSendInfo;
import cn.eyecool.msg.trade.service.IMsgWeixinSendService;

/**
 * 微信消息发送服务实现类
 * 
 * @author admin
 * @date 2020年3月24日
 */
@Service
public class MsgWeixinSendServiceImpl implements IMsgWeixinSendService {

    private static final Logger LOG = LoggerFactory.getLogger(MsgWeixinSendServiceImpl.class);

    /**
     * 发送微信消息
     */
    @Override
    public MsgSendResult sendWeixinMessage(MsgWeixinSendInfo sendInfo) throws Exception {
        String msgType = sendInfo.getMsgType();
        String[] openIds = sendInfo.getOpenIds();
        String openIdStr = sendInfo.getOpenIdStr();
        openIds = null != openIds ? openIds : StringUtils.isBlank(openIdStr) ? null : openIdStr.split(SPLIT);
        if (null == openIds) {
            throw new CustomException(MessageUtils.message("msg.weixin.send.service.officalaccount.empty"));
        }
        if (DictConstants.MsgType.NORMAL_MSG.equals(msgType) && openIds.length == 1) {
            return customSendContent(sendInfo);
        } else if (DictConstants.MsgType.NORMAL_MSG.equals(msgType) && openIds.length > 1) {
            return massSendContent(sendInfo);
        } else if (DictConstants.MsgType.TEMPLATE_MSG.equals(msgType) && openIds.length == 1) {
            return customSendTemplateMsg(sendInfo);
        } else if (DictConstants.MsgType.TEMPLATE_MSG.equals(msgType) && openIds.length > 1) {
            return massSendTemplateMsg(sendInfo);
        }
        throw new CustomException(MessageUtils.message("msg.weixin.send.service.msgtype.unsupported"));
    }

    /**
     * 根据openId单发文本消息
     */
    @Override
    public MsgSendResult customSendContent(MsgWeixinSendInfo sendInfo) throws Exception {
        this.checkWeixin(sendInfo, false);
        MessageComponent message = getWeixinMsgComp(sendInfo);
        message.customSendContent(sendInfo.getOpenIds()[0], sendInfo.getTxtContent());
        MsgSendResult result = new MsgSendResult(DictConstants.MsgResult.SUCCESS, null, null);
        return result;
    }

    /**
     * 根据openId列表群发文本消息
     */
    @Override
    public MsgSendResult massSendContent(MsgWeixinSendInfo sendInfo) throws Exception {
        this.checkWeixin(sendInfo, true);
        MessageComponent message = getWeixinMsgComp(sendInfo);
        message.massSendContent(sendInfo.getOpenIds(), sendInfo.getTxtContent());
        MsgSendResult result = new MsgSendResult(DictConstants.MsgResult.SUCCESS, null, null);
        return result;
    }

    /**
     * 根据openId单发模板消息
     */
    @Override
    public MsgSendResult customSendTemplateMsg(MsgWeixinSendInfo sendInfo) throws Exception {
        this.checkWeixin(sendInfo, false);
        MessageComponent message = getWeixinMsgComp(sendInfo);
        message.sendTemplateMessage(sendInfo.getOpenIds()[0], sendInfo.getTemplateId(), sendInfo.getTemplateData(),
            sendInfo.getRedirectUrl());
        MsgSendResult result = new MsgSendResult(DictConstants.MsgResult.SUCCESS, null, null);
        return result;
    }

    /**
     * 根据openId列表群发模板消息
     */
    @Override
    public MsgSendResult massSendTemplateMsg(MsgWeixinSendInfo sendInfo) throws Exception {
        this.checkWeixin(sendInfo, true);
        MessageComponent message = getWeixinMsgComp(sendInfo);
        AtomicLong succ = new AtomicLong(0L);
        AtomicLong fail = new AtomicLong(0L);
        Map<String, String> errMap = new ConcurrentHashMap<>();
        Arrays.stream(sendInfo.getOpenIds()).parallel().forEach(openId -> {
            try {
                message.sendTemplateMessage(openId, sendInfo.getTemplateId(), sendInfo.getTemplateData(),
                    sendInfo.getRedirectUrl());
                succ.incrementAndGet();
            } catch (WeixinException e) {
                fail.incrementAndGet();
                errMap.put("openId", e.getMessage());
                LOG.error("WeChat template message failed to send,openId:{}", openId, e);
            }
        });
        String statusCode = fail.get() > 0 ? DictConstants.MsgResult.FAIL : DictConstants.MsgResult.SUCCESS;
        MsgSendResult result = new MsgSendResult(statusCode, null, JSON.toJSONString(errMap));
        return result;
    }

    /**
     * 获取微信基础支持对象消息组件
     * 
     * @param sendInfo
     * @return
     */
    private MessageComponent getWeixinMsgComp(MsgWeixinSendInfo sendInfo) {
        PlatformWeiXin weixin = WeixinInstanceCache.get(sendInfo.getAppId(), sendInfo.getAppSecrect());
        return weixin.message();
    }

    /**
     * 校验微信消息参数
     * 
     * @param sendInfo
     */
    private void checkWeixin(MsgWeixinSendInfo sendInfo, boolean isMass) {
        String appId = sendInfo.getAppId();
        String appSecrect = sendInfo.getAppSecrect();
        String[] openIds = sendInfo.getOpenIds();
        String openIdStr = sendInfo.getOpenIdStr();
        openIds = null != openIds ? openIds : StringUtils.isBlank(openIdStr) ? null : openIdStr.split(SPLIT);
        sendInfo.setOpenIds(openIds);
        String msgType = sendInfo.getMsgType();
        String templateId = sendInfo.getTemplateId();
        String txtContent = sendInfo.getTxtContent();
        List<TemplateData> data = sendInfo.getTemplateData();
        String dataStr = sendInfo.getTemplateDataStr();
        if (StringUtils.isBlank(appId)) {
            throw new CustomException(MessageUtils.message("msg.weixin.send.service.appid.empty"));
        }
        if (StringUtils.isBlank(appSecrect)) {
            throw new CustomException(MessageUtils.message("msg.weixin.send.service.appsecret.empty"));
        }
        if (null == openIds) {
            throw new CustomException(MessageUtils.message("msg.weixin.send.service.openid.empty"));
        }
        if (DictConstants.MsgType.NORMAL_MSG.equals(msgType) && StringUtils.isBlank(txtContent)) {
            throw new CustomException(MessageUtils.message("msg.weixin.send.service.msg.content.empty"));
        }
        if (DictConstants.MsgType.TEMPLATE_MSG.equals(msgType)) {
            if (StringUtils.isBlank(templateId)) {
                throw new CustomException(MessageUtils.message("msg.weixin.send.service.templateid.empty"));
            }
            if (CollectionUtils.isEmpty(data) && StringUtils.isBlank(dataStr)) {
                throw new CustomException(MessageUtils.message("msg.weixin.send.service.templatedata.empty"));
            }
            if (CollectionUtils.isEmpty(data)) {
                try {
                    data = JSON.parseArray(dataStr, TemplateData.class);
                    sendInfo.setTemplateData(data);
                } catch (Exception e) {
                    LOG.error("WeChat data conversion error: {}", dataStr);
                    throw new CustomException(MessageUtils.message("msg.weixin.send.service.string.format.wrong"));
                }
            }
        }
        if (!isMass && openIds.length > 1) {
            throw new CustomException(MessageUtils.message("msg.weixin.send.service.fan.openid.more.one"));
        }
    }

}
