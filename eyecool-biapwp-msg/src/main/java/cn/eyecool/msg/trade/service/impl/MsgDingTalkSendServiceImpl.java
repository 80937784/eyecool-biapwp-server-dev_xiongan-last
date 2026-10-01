package cn.eyecool.msg.trade.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dingtalk.api.response.OapiMediaUploadResponse;
import com.dingtalk.api.response.OapiMessageCorpconversationGetsendprogressResponse.AsyncSendProgress;
import com.dingtalk.api.response.OapiMessageCorpconversationGetsendresultResponse.AsyncSendResult;
import com.taobao.api.ApiException;

import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.core.text.Convert;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.msg.configure.ding.DingTalkInstanceCache;
import cn.eyecool.msg.configure.ding.DingTalkMsgInfo;
import cn.eyecool.msg.configure.ding.DingTalkMsgInfo.DingTalkLinkMsgInfo;
import cn.eyecool.msg.configure.ding.DingTalkMsgInfo.DingTalkMarkdownMsgInfo;
import cn.eyecool.msg.configure.ding.PlatformDingTalk;
import cn.eyecool.msg.trade.entity.MsgDingTalkSendInfo;
import cn.eyecool.msg.trade.entity.MsgSendResult;
import cn.eyecool.msg.trade.service.IMsgDingTalkSendService;

/**
 * 钉钉消息发送服务实现
 * 
 * @author admin
 * @date 2020年3月31日
 */
@Service
public class MsgDingTalkSendServiceImpl implements IMsgDingTalkSendService {

    private static final Logger LOG = LoggerFactory.getLogger(MsgDingTalkSendServiceImpl.class);

    /**
     * 发送钉钉消息
     */
    @Override
    public MsgSendResult sendDingTalkMessage(MsgDingTalkSendInfo sendInfo) throws Exception {
        String msgType = sendInfo.getMsgType();
        if (DictConstants.DingtalkType.TEXT.equals(msgType)) {
            return sendTextContent(sendInfo);
        } else if (DictConstants.DingtalkType.LINK.equals(msgType)) {
            return sendLinkMessage(sendInfo);
        } else if (DictConstants.DingtalkType.MARKDOWN.equals(msgType)) {
            return sendMarkdownMessage(sendInfo);
        } else if (DictConstants.DingtalkType.IMAGE.equals(msgType)) {
            return sendImageMessage(sendInfo);
        } else if (DictConstants.DingtalkType.VOICE.equals(msgType)) {
            return sendVoiceMessage(sendInfo);
        } else if (DictConstants.DingtalkType.FILE.equals(msgType)) {
            return sendFileMessage(sendInfo);
        }
        String msg = MessageUtils.message("msg.ding.send.service.msgtype.unsupported", msgType);
        throw new CustomException(msg);
    }

    /**
     * 发送文本消息
     */
    @Override
    public MsgSendResult sendTextContent(MsgDingTalkSendInfo sendInfo) throws Exception {
        this.checkDingtalkMsg(sendInfo);
        PlatformDingTalk dingtalk = DingTalkInstanceCache.get(Long.parseUnsignedLong(sendInfo.getAgentId()),
            sendInfo.getAppKey(), sendInfo.getAppSecrect());
        String userIds = getUserIdsByPhones(sendInfo.getPhoneStr(), dingtalk);
        sendInfo.setUserIds(StringUtils.isBlank(sendInfo.getUserIds()) ? userIds : sendInfo.getUserIds());
        String toAllUser = sendInfo.getToAllUser();
        boolean isToAllUser = DictConstants.YesOrNoState.YES.equals(toAllUser);
        Long taskId =
            dingtalk.sendTextMessage(isToAllUser, sendInfo.getUserIds(), sendInfo.getDeptIds(), sendInfo.getText());
        MsgSendResult result = new MsgSendResult(DictConstants.MsgResult.SUCCESS, null, null);
        result.getData().put("taskId", taskId);
        return result;
    }

    /**
     * 发送链接消息
     */
    @Override
    public MsgSendResult sendLinkMessage(MsgDingTalkSendInfo sendInfo) throws Exception {
        this.checkDingtalkMsg(sendInfo);
        PlatformDingTalk dingtalk = DingTalkInstanceCache.get(Long.parseUnsignedLong(sendInfo.getAgentId()),
            sendInfo.getAppKey(), sendInfo.getAppSecrect());
        String userIds = getUserIdsByPhones(sendInfo.getPhoneStr(), dingtalk);
        sendInfo.setUserIds(StringUtils.isBlank(sendInfo.getUserIds()) ? userIds : sendInfo.getUserIds());
        DingTalkLinkMsgInfo dingTalkLinkMsgInfo = new DingTalkMsgInfo.DingTalkLinkMsgInfo(sendInfo.getTitle(),
            sendInfo.getText(), sendInfo.getMessageUrl(), sendInfo.getPicUrl());
        String toAllUser = sendInfo.getToAllUser();
        boolean isToAllUser = DictConstants.YesOrNoState.YES.equals(toAllUser);
        Long taskId =
            dingtalk.sendLinkMessage(isToAllUser, sendInfo.getUserIds(), sendInfo.getDeptIds(), dingTalkLinkMsgInfo);
        MsgSendResult result = new MsgSendResult(DictConstants.MsgResult.SUCCESS, null, null);
        result.getData().put("taskId", taskId);
        return result;
    }

    /**
     * 发送markdown消息
     */
    @Override
    public MsgSendResult sendMarkdownMessage(MsgDingTalkSendInfo sendInfo) throws Exception {
        this.checkDingtalkMsg(sendInfo);
        PlatformDingTalk dingtalk = DingTalkInstanceCache.get(Long.parseUnsignedLong(sendInfo.getAgentId()),
            sendInfo.getAppKey(), sendInfo.getAppSecrect());
        String userIds = getUserIdsByPhones(sendInfo.getPhoneStr(), dingtalk);
        sendInfo.setUserIds(StringUtils.isBlank(sendInfo.getUserIds()) ? userIds : sendInfo.getUserIds());
        DingTalkMarkdownMsgInfo dingTalkMarkdownMsgInfo =
            new DingTalkMsgInfo.DingTalkMarkdownMsgInfo(sendInfo.getTitle(), sendInfo.getText());
        String toAllUser = sendInfo.getToAllUser();
        boolean isToAllUser = DictConstants.YesOrNoState.YES.equals(toAllUser);
        Long taskId = dingtalk.sendMarkdownMessage(isToAllUser, sendInfo.getUserIds(), sendInfo.getDeptIds(),
            dingTalkMarkdownMsgInfo);
        MsgSendResult result = new MsgSendResult(DictConstants.MsgResult.SUCCESS, null, null);
        result.getData().put("taskId", taskId);
        return result;
    }

    /**
     * 发送图片消息
     */
    @Override
    public MsgSendResult sendImageMessage(MsgDingTalkSendInfo sendInfo) throws Exception {
        this.checkDingtalkMsg(sendInfo);
        PlatformDingTalk dingtalk = DingTalkInstanceCache.get(Long.parseUnsignedLong(sendInfo.getAgentId()),
            sendInfo.getAppKey(), sendInfo.getAppSecrect());
        String userIds = getUserIdsByPhones(sendInfo.getPhoneStr(), dingtalk);
        sendInfo.setUserIds(StringUtils.isBlank(sendInfo.getUserIds()) ? userIds : sendInfo.getUserIds());
        String toAllUser = sendInfo.getToAllUser();
        boolean isToAllUser = DictConstants.YesOrNoState.YES.equals(toAllUser);
        Long taskId =
            dingtalk.sendImageMessage(isToAllUser, sendInfo.getUserIds(), sendInfo.getDeptIds(), sendInfo.getMediaId());
        MsgSendResult result = new MsgSendResult(DictConstants.MsgResult.SUCCESS, null, null);
        result.getData().put("taskId", taskId);
        return result;
    }

    /**
     * 发送语音消息
     */
    @Override
    public MsgSendResult sendVoiceMessage(MsgDingTalkSendInfo sendInfo) throws Exception {
        this.checkDingtalkMsg(sendInfo);
        PlatformDingTalk dingtalk = DingTalkInstanceCache.get(Long.parseUnsignedLong(sendInfo.getAgentId()),
            sendInfo.getAppKey(), sendInfo.getAppSecrect());
        String userIds = getUserIdsByPhones(sendInfo.getPhoneStr(), dingtalk);
        sendInfo.setUserIds(StringUtils.isBlank(sendInfo.getUserIds()) ? userIds : sendInfo.getUserIds());
        String toAllUser = sendInfo.getToAllUser();
        boolean isToAllUser = DictConstants.YesOrNoState.YES.equals(toAllUser);
        Long taskId = dingtalk.sendVoiceMessage(isToAllUser, sendInfo.getUserIds(), sendInfo.getDeptIds(),
            sendInfo.getMediaId(), sendInfo.getDuration());
        MsgSendResult result = new MsgSendResult(DictConstants.MsgResult.SUCCESS, null, null);
        result.getData().put("taskId", taskId);
        return result;
    }

    /**
     * 发送普通文件消息
     */
    @Override
    public MsgSendResult sendFileMessage(MsgDingTalkSendInfo sendInfo) throws Exception {
        this.checkDingtalkMsg(sendInfo);
        PlatformDingTalk dingtalk = DingTalkInstanceCache.get(Long.parseUnsignedLong(sendInfo.getAgentId()),
            sendInfo.getAppKey(), sendInfo.getAppSecrect());
        String userIds = getUserIdsByPhones(sendInfo.getPhoneStr(), dingtalk);
        sendInfo.setUserIds(StringUtils.isBlank(sendInfo.getUserIds()) ? userIds : sendInfo.getUserIds());
        String toAllUser = sendInfo.getToAllUser();
        boolean isToAllUser = DictConstants.YesOrNoState.YES.equals(toAllUser);
        Long taskId =
            dingtalk.sendFileMessage(isToAllUser, sendInfo.getUserIds(), sendInfo.getDeptIds(), sendInfo.getMediaId());
        MsgSendResult result = new MsgSendResult(DictConstants.MsgResult.SUCCESS, null, null);
        result.getData().put("taskId", taskId);
        return result;
    }

    /**
     * 消息发送进度查询
     */
    @Override
    public AsyncSendProgress getTaskProgress(MsgDingTalkSendInfo sendInfo, Long taskId) throws Exception {
        this.checkAuthField(sendInfo);
        PlatformDingTalk dingtalk = DingTalkInstanceCache.get(Long.parseUnsignedLong(sendInfo.getAgentId()),
            sendInfo.getAppKey(), sendInfo.getAppSecrect());
        AsyncSendProgress progress = dingtalk.getTaskProgress(taskId);
        return progress;
    }

    /**
     * 查询消息发送结果
     * 
     * @throws ApiException
     */
    @Override
    public AsyncSendResult getTaskResult(MsgDingTalkSendInfo sendInfo, Long taskId) throws Exception {
        this.checkAuthField(sendInfo);
        PlatformDingTalk dingtalk = DingTalkInstanceCache.get(Long.parseUnsignedLong(sendInfo.getAgentId()),
            sendInfo.getAppKey(), sendInfo.getAppSecrect());
        AsyncSendResult taskResult = dingtalk.getTaskResult(taskId);
        return taskResult;
    }

    /**
     * 撤回消息
     */
    @Override
    public void revokeTask(MsgDingTalkSendInfo sendInfo, Long taskId) throws Exception {
        this.checkAuthField(sendInfo);
        PlatformDingTalk dingtalk = DingTalkInstanceCache.get(Long.parseUnsignedLong(sendInfo.getAgentId()),
            sendInfo.getAppKey(), sendInfo.getAppSecrect());
        dingtalk.revokeTask(taskId);
    }

    /**
     * 钉钉参数校验
     * 
     * @param sendInfo
     */
    private void checkDingtalkMsg(MsgDingTalkSendInfo sendInfo) {
        String toAllUser = sendInfo.getToAllUser();
        String phoneStr = sendInfo.getPhoneStr();
        String userIds = sendInfo.getUserIds();
        this.checkAuthField(sendInfo);
        if (DictConstants.YesOrNoState.NO.equals(toAllUser) && StringUtils.isBlank(userIds)
            && StringUtils.isBlank(phoneStr)) {
            throw new CustomException(MessageUtils.message("msg.ding.send.service.user.phone.all.empty"));
        }
    }

    /**
     * 权限相关参数校验
     * 
     * @param sendInfo
     */
    private void checkAuthField(MsgDingTalkSendInfo sendInfo) {
        if (StringUtils.isBlank(sendInfo.getCorpId())) {
            throw new CustomException(MessageUtils.message("msg.ding.send.service.corpid.empty"));
        }
        if (StringUtils.isBlank(sendInfo.getAgentId())) {
            throw new CustomException(MessageUtils.message("msg.ding.send.service.agentid.empty"));
        }
        if (StringUtils.isBlank(sendInfo.getAppKey())) {
            throw new CustomException(MessageUtils.message("msg.ding.send.service.appkey.empty"));
        }
        if (StringUtils.isBlank(sendInfo.getAppSecrect())) {
            throw new CustomException(MessageUtils.message("msg.ding.send.service.appsecret.empty"));
        }
    }

    /**
     * 根据手机号查询用户UserId
     * 
     * @param phoneStr
     * @param dingtalk
     * @return
     */
    private String getUserIdsByPhones(String phoneStr, PlatformDingTalk dingtalk) {
        if (StringUtils.isBlank(phoneStr)) {
            return null;
        }
        String[] phoneArray = Convert.toStrArray(phoneStr);
        List<String> list = Arrays.asList(phoneArray).parallelStream().map(phone -> {
            try {
                return dingtalk.getUserIdByMobile(phone);
            } catch (Exception e) {
                LOG.error("Dingding gets userId exception based on mobile phone number {}", phone, e);
                return null;
            }
        }).filter(it -> it != null).collect(Collectors.toList());
        return list.stream().reduce((e1, e2) -> e1 + "," + e2).orElse(null);
    }

    /**
     * 上传钉钉媒体文件
     */
    @Override
    public String uploadMedia(MsgDingTalkSendInfo sendInfo, MultipartFile multipartFile) throws Exception {
        this.checkAuthField(sendInfo);
        String mediaType = sendInfo.getMediaType();
        PlatformDingTalk dingtalk = DingTalkInstanceCache.get(Long.parseUnsignedLong(sendInfo.getAgentId()),
            sendInfo.getAppKey(), sendInfo.getAppSecrect());
        OapiMediaUploadResponse response =
            dingtalk.uploadMedia(multipartFile.getOriginalFilename(), mediaType, multipartFile.getBytes());
        if (response.getErrcode() == 0) {
            return response.getMediaId();
        }
        throw new CustomException(MessageUtils.message("msg.ding.send.service.media.upload.failed", response.getErrmsg()));
    }

}
