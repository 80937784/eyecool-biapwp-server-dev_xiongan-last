package cn.eyecool.server.handler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.LongValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dingtalk.api.response.OapiMessageCorpconversationGetsendresultResponse.AsyncSendResult;

import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.http.HttpAjaxResult;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.msg.trade.entity.MsgDingTalkSendInfo;
import cn.eyecool.msg.trade.entity.MsgMailSendInfo;
import cn.eyecool.msg.trade.entity.MsgSendResult;
import cn.eyecool.msg.trade.entity.MsgSmsSendInfo;
import cn.eyecool.msg.trade.entity.MsgWeixinSendInfo;
import cn.eyecool.msg.trade.service.IMsgSendService;

/**
 * 消息推送HTTP请求处理器
 * 
 * @author mawenjun
 * @version 1.0
 * @date 2020年3月20日
 *
 */
@Component
public class MsgSendHttpHandler {

    private static final Logger LOG = LoggerFactory.getLogger(MsgSendHttpHandler.class);

    @Autowired
    private IMsgSendService msgSendService;

    /**
     * 发送短信消息
     * 
     * @author mawenjun
     * @param bizContent
     * @return
     * @date 2020年3月22日
     *
     */
    public AjaxResult sendSmsMessage(String bizContent) {
        // json转换
        MsgSmsSendInfo smsSendInfo = null;
        try {
            smsSendInfo = JSONObject.parseObject(bizContent, MsgSmsSendInfo.class);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        // 参数校验
        AjaxResult ajaxResult = validateSmsSendInfo(smsSendInfo);
        if (!HttpAjaxResult.HTTP_SUCC_CODE.equals(ajaxResult.get(AjaxResult.CODE_TAG))) {
            return ajaxResult;
        }
        try {
            MsgSendResult sendResult = msgSendService.sendSmsMessage(smsSendInfo);
            String statusCode = sendResult.getStatusCode();
            if (DictConstants.MsgResult.SUCCESS.equals(statusCode)) {
                return HttpAjaxResult.httpSuccess();
            }
            return HttpAjaxResult.businessError(sendResult.getErrmsg());
        } catch (CustomException e) {
            LOG.error("message failed to send", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("message failed to send", e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 短信参数校验
     * 
     * @param smsSendInfo
     * @return
     */
    private AjaxResult validateSmsSendInfo(MsgSmsSendInfo smsSendInfo) {
        String msg = null;
        // 业务流水号不能为空, 且长度不大于48
        String receivedSeq = smsSendInfo.getReceivedSeq();
        if (StringUtils.isBlank(receivedSeq) || receivedSeq.length() > 48) {
            msg  =MessageUtils.message("base.person.handler.receivedseq.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 场景标识不能为空, 且长度不大于48
        String sceneRemark = smsSendInfo.getSceneRemark();
        if (StringUtils.isBlank(sceneRemark) || sceneRemark.length() > 48) {
            msg = MessageUtils.message("msg.handler.scene.remark.max.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 短信云账户AppId不能为空
        String appId = smsSendInfo.getAppId();
        if (StringUtils.isBlank(appId)) {
            msg = MessageUtils.message("msg.handler.appid.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 收件人不能为空
        String toUserPhoneStr = smsSendInfo.getToUserPhoneStr();
        if (StringUtils.isBlank(toUserPhoneStr)) {
            msg = MessageUtils.message("msg.handler.tosuer.phonestr.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 消息主题不能为空
        String msgSubject = smsSendInfo.getMsgSubject();
        if (StringUtils.isBlank(msgSubject)) {
            msg = MessageUtils.message("msg.handler.msg.subject.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 消息发送模式不能为空
        String msgType = smsSendInfo.getMsgType();
        if (DictConstants.MsgType.NORMAL_MSG.equals(msgType)) {
            // 短信内容
            String msgContent = smsSendInfo.getMsgContent();
            if (StringUtils.isBlank(msgContent)) {
                msg = MessageUtils.message("msg.handler.msg.content.empty");
                return HttpAjaxResult.businessDataValidError(msg);
            }
        } else if (DictConstants.MsgType.TEMPLATE_MSG.equals(msgType)) {
            // 短信官方模板ID不能为空
            String templateId = smsSendInfo.getTemplateId();
            if (StringUtils.isBlank(templateId)) {
                msg = MessageUtils.message("msg.handler.templateid.empty");
                return HttpAjaxResult.businessDataValidError(msg);
            }
            // 模板数据不能为空
            String templateParamStr = smsSendInfo.getTemplateParamStr();
            if (StringUtils.isBlank(templateParamStr)) {
                msg = MessageUtils.message("msg.handler.template.paramstr.empty");
                return HttpAjaxResult.businessDataValidError(msg);
            }
            // 短信签名不能为空
            String sign = smsSendInfo.getSign();
            if (StringUtils.isBlank(sign)) {
                msg = MessageUtils.message("msg.handler.sign.empty");
                return HttpAjaxResult.businessDataValidError(msg);
            }
        } else {
            msg = MessageUtils.message("msg.handler.send.type.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        return HttpAjaxResult.httpSuccess();
    }

    /**
     * 发送邮件消息
     * 
     * @author mawenjun
     * @param bizContent
     * @param request
     * @return
     * @date 2020年3月22日
     *
     */
    public AjaxResult sendMailMessage(String bizContent, HttpServletRequest request) {
        // json转换
        MsgMailSendInfo mailSendInfo = null;
        try {
            mailSendInfo = JSONObject.parseObject(bizContent, MsgMailSendInfo.class);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        // 参数校验
        AjaxResult ajaxResult = validateMailSendInfo(mailSendInfo);
        if (!HttpAjaxResult.HTTP_SUCC_CODE.equals(ajaxResult.get(AjaxResult.CODE_TAG))) {
            return ajaxResult;
        }
        try {
            MultipartHttpServletRequest multipartHttpServletRequest =
                new StandardServletMultipartResolver().resolveMultipart(request);
            Map<String, MultipartFile> fileMap = multipartHttpServletRequest.getFileMap();
            if (null != fileMap && fileMap.size() > 0) {
                List<MultipartFile> files = fileMap.values().stream().collect(Collectors.toList());
                mailSendInfo.setMultipartFiles(files);
            }
        } catch (Exception e) {
            LOG.error("Request to MultipartHttpServletRequest error", e);
        }
        try {
            MsgSendResult sendResult = msgSendService.sendMailMessage(mailSendInfo);
            String statusCode = sendResult.getStatusCode();
            if (DictConstants.MsgResult.SUCCESS.equals(statusCode)) {
                return HttpAjaxResult.httpSuccess();
            }
            return HttpAjaxResult.businessError(sendResult.getErrmsg());
        } catch (CustomException e) {
            LOG.error("Email sending failed", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("Email sending failed", e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 邮件参数校验
     * 
     * @param mailSendInfo
     * @return
     */
    private AjaxResult validateMailSendInfo(MsgMailSendInfo mailSendInfo) {
        String msg = null;
        // 业务流水号不能为空, 且长度不大于48
        String receivedSeq = mailSendInfo.getReceivedSeq();
        if (StringUtils.isBlank(receivedSeq) || receivedSeq.length() > 48) {
            msg = MessageUtils.message("base.person.handler.receivedseq.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 场景标识不能为空, 且长度不大于48
        String sceneRemark = mailSendInfo.getSceneRemark();
        if (StringUtils.isBlank(sceneRemark) || sceneRemark.length() > 48) {
            msg = MessageUtils.message("msg.handler.scene.remark.max.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 发件邮箱不能为空
        String from = mailSendInfo.getFrom();
        if (StringUtils.isBlank(from)) {
            msg = MessageUtils.message("msg.handler.mail.form.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 收件人邮箱不能为空
        String toMailStr = mailSendInfo.getToMailStr();
        if (StringUtils.isBlank(toMailStr)) {
            msg = MessageUtils.message("msg.handler.mail.tomailstr.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 邮件类型不能为空,且参数值正确
        String mailType = mailSendInfo.getMailType();
        if (StringUtils.isBlank(mailType)) {
            msg = MessageUtils.message("msg.handler.mail.type.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        if (!DictConstants.MailType.TEXT_MAIL.equals(mailType) && !DictConstants.MailType.HTML_MAIL.equals(mailType)) {
            msg = MessageUtils.message("msg.handler.mail.type.format.error");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 邮件主题不能为空
        String subject = mailSendInfo.getSubject();
        if (StringUtils.isBlank(subject)) {
            msg = MessageUtils.message("msg.handler.mail.subject.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        return HttpAjaxResult.httpSuccess();
    }

    /**
     * 发送微信消息
     * 
     * @author mawenjun
     * @param bizContent
     * @return
     * @date 2020年3月22日
     *
     */
    public AjaxResult sendWechatMessage(String bizContent) {
        // json转换
        MsgWeixinSendInfo weixinSendInfo = null;
        try {
            weixinSendInfo = JSONObject.parseObject(bizContent, MsgWeixinSendInfo.class);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        // 参数校验
        AjaxResult ajaxResult = validateWeixinSendInfo(weixinSendInfo);
        if (!HttpAjaxResult.HTTP_SUCC_CODE.equals(ajaxResult.get(AjaxResult.CODE_TAG))) {
            return ajaxResult;
        }
        try {
            MsgSendResult sendResult = msgSendService.sendWeixinMessage(weixinSendInfo);
            String statusCode = sendResult.getStatusCode();
            if (DictConstants.MsgResult.SUCCESS.equals(statusCode)) {
                return HttpAjaxResult.httpSuccess();
            }
            return HttpAjaxResult.businessError(sendResult.getErrmsg());
        } catch (CustomException e) {
            LOG.error("Failed to send WeChat message", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("Failed to send WeChat message", e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 微信参数校验
     * 
     * @param weixinSendInfo
     * @return
     */
    private AjaxResult validateWeixinSendInfo(MsgWeixinSendInfo weixinSendInfo) {
        String msg = null;
        // 业务流水号不能为空, 且长度不大于48
        String receivedSeq = weixinSendInfo.getReceivedSeq();
        if (StringUtils.isBlank(receivedSeq) || receivedSeq.length() > 48) {
            msg = MessageUtils.message("base.person.handler.receivedseq.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 场景标识不能为空, 且长度不大于48
        String sceneRemark = weixinSendInfo.getSceneRemark();
        if (StringUtils.isBlank(sceneRemark) || sceneRemark.length() > 48) {
            msg = MessageUtils.message("msg.handler.scene.remark.max.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 微信公众号AppId不能为空
        String appId = weixinSendInfo.getAppId();
        if (StringUtils.isBlank(appId)) {
            msg = MessageUtils.message("msg.handler.wechat.appid.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 收信人微信手机号不能为空
        String phoneStr = weixinSendInfo.getPhoneStr();
        if (StringUtils.isBlank(phoneStr)) {
            msg = MessageUtils.message("msg.handler.wechat.phone.str.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 消息主题不能为空
        String msgSubject = weixinSendInfo.getMsgSubject();
        if (StringUtils.isBlank(msgSubject)) {
            msg = MessageUtils.message("msg.handler.wechat.msg.subject.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 消息类型不能为空
        String msgType = weixinSendInfo.getMsgType();
        if (DictConstants.MsgType.NORMAL_MSG.equals(msgType)) {
            // 消息内容
            String txtContent = weixinSendInfo.getTxtContent();
            if (StringUtils.isBlank(txtContent)) {
                msg = MessageUtils.message("msg.handler.wechat.msg.content.empty");
                return HttpAjaxResult.businessDataValidError(msg);
            }
        } else if (DictConstants.MsgType.TEMPLATE_MSG.equals(msgType)) {
            // 消息模板ID不能为空
            String templateId = weixinSendInfo.getTemplateId();
            if (StringUtils.isBlank(templateId)) {
                msg = MessageUtils.message("msg.handler.wechat..templateid.empty");
                return HttpAjaxResult.businessDataValidError(msg);
            }
            // 模板数据不能为空
            String templateDataStr = weixinSendInfo.getTemplateDataStr();
            if (StringUtils.isBlank(templateDataStr)) {
                msg = MessageUtils.message("msg.handler.wechat.template.datastr.empty");
                return HttpAjaxResult.businessDataValidError(msg);
            }
        } else {
            msg = MessageUtils.message("msg.handler.wechat.msg.type.format");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        return HttpAjaxResult.httpSuccess();
    }

    /**
     * 发送钉钉消息
     * 
     * @author mawenjun
     * @param bizContent
     * @return
     * @date 2020年3月22日
     *
     */
    public AjaxResult sendDingMessage(String bizContent) {
        // json转换
        MsgDingTalkSendInfo dingTalkSendInfo = null;
        try {
            dingTalkSendInfo = JSONObject.parseObject(bizContent, MsgDingTalkSendInfo.class);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        // 参数校验
        AjaxResult ajaxResult = validateDingtalkSendInfo(dingTalkSendInfo);
        if (!HttpAjaxResult.HTTP_SUCC_CODE.equals(ajaxResult.get(AjaxResult.CODE_TAG))) {
            return ajaxResult;
        }
        try {
            MsgSendResult sendResult = msgSendService.sendDingMessage(dingTalkSendInfo);
            String statusCode = sendResult.getStatusCode();
            if (DictConstants.MsgResult.SUCCESS.equals(statusCode)) {
                return HttpAjaxResult.httpSuccess(sendResult.getData());
            }
            return HttpAjaxResult.businessError(sendResult.getErrmsg());
        } catch (CustomException e) {
            LOG.error("Dingding message failed to send", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("Dingding message failed to send", e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 钉钉参数校验
     * 
     * @param dingTalkSendInfo
     * @return
     */
    private AjaxResult validateDingtalkSendInfo(MsgDingTalkSendInfo dingTalkSendInfo) {
        String msg = null;
        // 业务流水号不能为空, 且长度不大于48
        String receivedSeq = dingTalkSendInfo.getReceivedSeq();
        if (StringUtils.isBlank(receivedSeq) || receivedSeq.length() > 48) {
            msg  =MessageUtils.message("base.person.handler.receivedseq.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 场景标识不能为空, 且长度不大于48
        String sceneRemark = dingTalkSendInfo.getSceneRemark();
        if (StringUtils.isBlank(sceneRemark) || sceneRemark.length() > 48) {
            msg  =MessageUtils.message("msg.handler.scene.remark.max.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 钉钉企业CoprId不能为空
        String CoprId = dingTalkSendInfo.getCorpId();
        if (StringUtils.isBlank(CoprId)) {
            msg  =MessageUtils.message("msg.handler.ding.corpid.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 钉钉微应用agentId不能为空
        String agentId = dingTalkSendInfo.getAgentId();
        if (StringUtils.isBlank(agentId)) {
            msg  =MessageUtils.message("msg.handler.ding.agentid.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 钉钉微应用appKey不能为空
        String appKey = dingTalkSendInfo.getAppKey();
        if (StringUtils.isBlank(appKey)) {
            msg  =MessageUtils.message("msg.handler.ding.appkey.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 是否发送全部用户不能为空
        String toAllUser = dingTalkSendInfo.getToAllUser();
        if (!DictConstants.YesOrNoState.YES.equals(toAllUser) && !DictConstants.YesOrNoState.NO.equals(toAllUser)) {
            msg  =MessageUtils.message("msg.handler.ding.toalluser.flag");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 不发送全部用户时，手机号不能为空
        String phoneStr = dingTalkSendInfo.getPhoneStr();
        if (DictConstants.YesOrNoState.NO.equals(toAllUser) && StringUtils.isBlank(phoneStr)) {
            msg  =MessageUtils.message("msg.handler.ding.phonestr.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 消息主题不能为空
        String msgSubject = dingTalkSendInfo.getMsgSubject();
        if (StringUtils.isBlank(msgSubject)) {
            msg  =MessageUtils.message("msg.handler.ding.msg.subject.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 消息类型不能为空
        String msgType = dingTalkSendInfo.getMsgType();
        if (StringUtils.isBlank(msgType)) {
            msg  =MessageUtils.message("msg.handler.ding.msg.type.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        return HttpAjaxResult.httpSuccess();
    }

    /**
     * 获取钉钉发送结果
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult getSendDingMsgResult(String bizContent) {
        // json转换
        JSONObject parseObject = null;
        try {
            parseObject = JSON.parseObject(bizContent);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        // 参数校验
        String msg = null;
        // 业务流水号不能为空, 且长度不大于48
        String receivedSeq = parseObject.getString("receivedSeq");
        if (StringUtils.isBlank(receivedSeq) || receivedSeq.length() > 48) {
            msg = MessageUtils.message("base.person.handler.receivedseq.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 钉钉企业CoprId不能为空
        String corpId = parseObject.getString("corpId");
        if (StringUtils.isBlank(corpId)) {
            msg = MessageUtils.message("msg.handler.ding.corpid.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 钉钉微应用agentId不能为空
        String agentId = parseObject.getString("agentId");
        if (StringUtils.isBlank(agentId) || !LongValidator.getInstance().isValid(agentId)) {
            msg = MessageUtils.message("msg.handler.ding.agentid.format");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 钉钉微应用appKey不能为空
        String appKey = parseObject.getString("appKey");
        if (StringUtils.isBlank(appKey)) {
            msg = MessageUtils.message("msg.handler.ding.appkey.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 钉钉消息任务id不能为空
        String taskId = parseObject.getString("taskId");
        if (StringUtils.isBlank(taskId) || !LongValidator.getInstance().isValid(taskId)) {
            msg = MessageUtils.message("msg.handler.ding.taskid.format");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        MsgDingTalkSendInfo sendInfo = new MsgDingTalkSendInfo();
        sendInfo.setAppKey(appKey);
        sendInfo.setAgentId(agentId);
        sendInfo.setCorpId(corpId);
        sendInfo.setReceivedSeq(receivedSeq);
        try {
            AsyncSendResult sendResult = msgSendService.getDingTaskResult(sendInfo, Long.valueOf(taskId));
            return HttpAjaxResult.httpSuccess(sendResult);
        } catch (CustomException e) {
            LOG.error("Failed to get DingTalk message sending result", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("Failed to get DingTalk message sending result", e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 钉钉媒体文件上传
     * 
     * @param bizContent
     * @param request
     * @return
     */
    public AjaxResult uploadDingMedia(String bizContent, HttpServletRequest request) {
        // json转换
        JSONObject parseObject = null;
        try {
            parseObject = JSON.parseObject(bizContent);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        // 参数校验
        String msg = null;
        // 业务流水号不能为空, 且长度不大于48
        String receivedSeq = parseObject.getString("receivedSeq");
        if (StringUtils.isBlank(receivedSeq) || receivedSeq.length() > 48) {
            msg  = MessageUtils.message("base.person.handler.receivedseq.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 钉钉企业CoprId不能为空
        String corpId = parseObject.getString("corpId");
        if (StringUtils.isBlank(corpId)) {
            msg  = MessageUtils.message("msg.handler.ding.corpid.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 钉钉微应用agentId不能为空
        String agentId = parseObject.getString("agentId");
        if (StringUtils.isBlank(agentId) || !LongValidator.getInstance().isValid(agentId)) {
            msg  = MessageUtils.message("msg.handler.ding.agentid.format");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 钉钉微应用appKey不能为空
        String appKey = parseObject.getString("appKey");
        if (StringUtils.isBlank(appKey)) {
            msg  = MessageUtils.message("msg.handler.ding.appkey.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 媒体类型不能为空
        String mediaType = parseObject.getString("mediaType");
        if (StringUtils.isBlank(mediaType)) {
            msg  = MessageUtils.message("msg.handler.ding.mediatype.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 媒体文件不能为空
        MultipartHttpServletRequest multipartHttpServletRequest =
            new StandardServletMultipartResolver().resolveMultipart(request);
        MultipartFile file = multipartHttpServletRequest.getFile("file");
        if (null == file) {
            msg  = MessageUtils.message("msg.handler.ding.file.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        MsgDingTalkSendInfo sendInfo = new MsgDingTalkSendInfo();
        sendInfo.setAppKey(appKey);
        sendInfo.setAgentId(agentId);
        sendInfo.setCorpId(corpId);
        sendInfo.setMediaType(mediaType);
        sendInfo.setReceivedSeq(receivedSeq);
        try {
            Map<String, String> map = msgSendService.uploadDingtalkMedia(sendInfo, file);
            return HttpAjaxResult.httpSuccess(map);
        } catch (CustomException e) {
            LOG.error("DingTalk media file upload failed", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("DingTalk media file upload failed", e);
            return HttpAjaxResult.httpError();
        }
    }

}
