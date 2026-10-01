package cn.eyecool.msg.trade.service.impl;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.weixin4j.model.message.template.TemplateData;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.internal.Maps;
import com.dingtalk.api.response.OapiMessageCorpconversationGetsendresultResponse.AsyncSendResult;
import com.dingtalk.api.response.OapiMessageCorpconversationGetsendresultResponse.SendForbiddenModel;

import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.file.FileUploadUtils;
import cn.eyecool.common.utils.file.PlatformFileUploadUtils;
import cn.eyecool.common.utils.sign.Md5Utils;
import cn.eyecool.msg.domain.MsgDingApplication;
import cn.eyecool.msg.domain.MsgDingTeam;
import cn.eyecool.msg.domain.MsgLog;
import cn.eyecool.msg.domain.MsgLogAnnex;
import cn.eyecool.msg.domain.MsgMailProperty;
import cn.eyecool.msg.domain.MsgOfficalAccount;
import cn.eyecool.msg.domain.MsgOfficalAccountUser;
import cn.eyecool.msg.domain.MsgSmsCloudAccount;
import cn.eyecool.msg.mapper.MsgDingApplicationMapper;
import cn.eyecool.msg.mapper.MsgDingTeamMapper;
import cn.eyecool.msg.mapper.MsgLogAnnexMapper;
import cn.eyecool.msg.mapper.MsgLogMapper;
import cn.eyecool.msg.mapper.MsgMailPropertyMapper;
import cn.eyecool.msg.mapper.MsgOfficalAccountMapper;
import cn.eyecool.msg.mapper.MsgOfficalAccountUserMapper;
import cn.eyecool.msg.mapper.MsgSmsCloudAccountMapper;
import cn.eyecool.msg.trade.entity.MsgDingTalkSendInfo;
import cn.eyecool.msg.trade.entity.MsgMailSendInfo;
import cn.eyecool.msg.trade.entity.MsgSendResult;
import cn.eyecool.msg.trade.entity.MsgSmsSendInfo;
import cn.eyecool.msg.trade.entity.MsgWeixinSendInfo;
import cn.eyecool.msg.trade.service.IMsgDingTalkSendService;
import cn.eyecool.msg.trade.service.IMsgMailSendService;
import cn.eyecool.msg.trade.service.IMsgSendService;
import cn.eyecool.msg.trade.service.IMsgSmsSendService;
import cn.eyecool.msg.trade.service.IMsgWeixinSendService;
import cn.eyecool.msg.util.AmrDurationUtil;
import cn.eyecool.system.service.ISysConfigService;

/**
 * 消息发送服务实现
 * 
 * @author admin
 * @date 2020年3月19日
 */
@Service
public class MsgSendServiceImpl implements IMsgSendService {

    private static final Logger LOG = LoggerFactory.getLogger(MsgSendServiceImpl.class);
    /** 收件人分隔符 */
    private static final String SPLIT = ",";
    /** 默认消息附件存储文件夹 */
    private static final String DEFAULT_MSG_ATTACHMENT_DIR = "./eyecool/msg/attachment/";

    @Autowired
    private ISysConfigService configService;
    @Autowired
    private IMsgSmsSendService msgSmsSendService;
    @Autowired
    private IMsgMailSendService msgMailSendService;
    @Autowired
    private IMsgWeixinSendService msgWeixinSendService;
    @Autowired
    private IMsgDingTalkSendService msgDingTalkSendService;
    @Autowired
    private MsgLogMapper msgLogMapper;
    @Autowired
    private MsgLogAnnexMapper logAnnexMapper;
    @Autowired
    private MsgMailPropertyMapper mailPropertyMapper;
    @Autowired
    private MsgOfficalAccountMapper officalAccountMapper;
    @Autowired
    private MsgOfficalAccountUserMapper officalAccountUserMapper;
    @Autowired
    private MsgSmsCloudAccountMapper smsCloudAccountMapper;
    @Autowired
    private MsgDingApplicationMapper dingApplicationMapper;
    @Autowired
    private MsgDingTeamMapper dingTeamMapper;
    @Autowired
    private MailProperties mailProperties;

    /**
     * 发送短信
     */
    @Override
    public MsgSendResult sendSmsMessage(MsgSmsSendInfo sendInfo) {
        MsgSendResult sendResult = null;
        Exception throwException = null;
        String appSecrect = sendInfo.getAppSecrect();
        String appId = sendInfo.getAppId();
        if (StringUtils.isBlank(appSecrect) && StringUtils.isNotBlank(appId)) {
            appSecrect = getSmsAppSecrect(appId);
            sendInfo.setAppSecrect(appSecrect);
            if (StringUtils.isBlank(appSecrect)) {
                throw new CustomException(MessageUtils.message("msg.send.service.first.add.sms.info"));
            }
        }
        try {
            sendResult = msgSmsSendService.sendSmsMessage(sendInfo);
            return sendResult;
        } catch (Exception e) {
            throwException = e;
            LOG.error("SMS sending exception", e);
            throw new CustomException(e.getMessage());
        } finally {// 异步保存消息日志
            saveSmsMsgLog(sendInfo, sendResult, throwException);
        }
    }

    /**
     * 发送邮件
     */
    @Override
    public MsgSendResult sendMailMessage(MsgMailSendInfo sendInfo) {
        sendInfo.setMailProperties(getMailProperties(sendInfo.getFrom()));
        MsgSendResult sendResult = null;
        Exception throwException = null;
        try {
            sendResult = msgMailSendService.sendMailInfo(sendInfo);
            return sendResult;
        } catch (Exception e) {
            throwException = e;
            LOG.error("Email sending exception", e);
            throw new CustomException(e.getMessage());
        } finally {// 异步保存消息日志
            saveMailMsgLog(sendInfo, sendResult, throwException);
        }
    }

    /**
     * 发送微信消息
     */
    @Override
    public MsgSendResult sendWeixinMessage(MsgWeixinSendInfo sendInfo) {
        MsgSendResult sendResult = null;
        Exception throwException = null;
        String appSecrect = sendInfo.getAppSecrect();
        String appId = sendInfo.getAppId();
        if (StringUtils.isBlank(appSecrect) && StringUtils.isNotBlank(appId)) {
            appSecrect = getWeixinAppSecrect(appId);
            sendInfo.setAppSecrect(appSecrect);
            if (StringUtils.isBlank(appSecrect)) {
                throw new CustomException(MessageUtils.message("msg.send.service.first.add.weixin.info"));
            }
        }
        // 根据收件人手机号，查询openId列表
        if (StringUtils.isNotBlank(sendInfo.getPhoneStr())) {
            String[] openIds = getOpenIdsByBindPhone(appId, sendInfo.getPhoneStr());
            sendInfo.setOpenIds(openIds);
        }
        try {
            sendResult = msgWeixinSendService.sendWeixinMessage(sendInfo);
            return sendResult;
        } catch (Exception e) {
            throwException = e;
            LOG.error("WeChat message sending exception", e);
            throw new CustomException(e.getMessage());
        } finally {// 异步保存消息日志
            saveWeixinMsgLog(sendInfo, sendResult, throwException);
        }
    }

    /**
     * 发送钉钉消息
     */
    @Override
    public MsgSendResult sendDingMessage(MsgDingTalkSendInfo sendInfo) {
        MsgSendResult sendResult = null;
        Exception throwException = null;
        String appSecrect = sendInfo.getAppSecrect();
        String appKey = sendInfo.getAppKey();
        String corpId = sendInfo.getCorpId();
        if (StringUtils.isBlank(appSecrect) && StringUtils.isNotBlank(appKey)) {
            appSecrect = getDingtalkAppSecrect(corpId, appKey);
            sendInfo.setAppSecrect(appSecrect);
            if (StringUtils.isBlank(appSecrect)) {
                throw new CustomException(MessageUtils.message("msg.send.service.first.add.ding.info"));
            }
        }
        try {
            sendResult = msgDingTalkSendService.sendDingTalkMessage(sendInfo);
            return sendResult;
        } catch (Exception e) {
            throwException = e;
            LOG.error("Dingding message sending exception", e);
            throw new CustomException(e.getMessage());
        } finally {// 异步保存消息日志
            saveDingtalkMsgLog(sendInfo, sendResult, throwException);
        }
    }

    /**
     * 查询短信AppSecrect
     * 
     * @param appId
     * @return
     */
    private String getSmsAppSecrect(String appId) {
        MsgSmsCloudAccount account = new MsgSmsCloudAccount();
        account.setAppId(appId);
        List<MsgSmsCloudAccount> list = smsCloudAccountMapper.selectMsgSmsCloudAccountList(account);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0).getAppSecrect();
        }
        return null;
    }

    /**
     * 查询微信AppSecrect
     * 
     * @param appId
     * @return
     */
    private String getWeixinAppSecrect(String appId) {
        MsgOfficalAccount officalAccount = new MsgOfficalAccount();
        officalAccount.setAppId(appId);
        List<MsgOfficalAccount> list = officalAccountMapper.selectMsgOfficalAccountList(officalAccount);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0).getAppSecrect();
        }
        return null;
    }

    /**
     * 查询钉钉AppSecrect
     * 
     * @param appId
     * @return
     */
    private String getDingtalkAppSecrect(String corpId, String appKey) {
        MsgDingApplication application = new MsgDingApplication();
        application.setCorpId(corpId);
        application.setAppKey(appKey);
        List<MsgDingApplication> list = dingApplicationMapper.selectMsgDingApplicationList(application);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0).getAppSecrect();
        }
        return null;
    }

    /**
     * 根据用户名获取邮箱属性信息
     * 
     * @author mawenjun
     * @param from
     * @return
     * @date 2020年3月22日
     */
    private MailProperties getMailProperties(String from) {
        MsgMailProperty mailProperty = new MsgMailProperty();
        mailProperty.setEmailAddr(from);
        List<MsgMailProperty> propertyList = mailPropertyMapper.selectMsgMailPropertyList(mailProperty);
        if (CollectionUtils.isEmpty(propertyList)) {
            throw new CustomException(MessageUtils.message("msg.send.service.first.add.mail.info"));
        }
        MsgMailProperty msgMailProperty = propertyList.get(0);
        MailProperties properties = new MailProperties();
        properties.setHost(msgMailProperty.getHost());
        properties.setPort(msgMailProperty.getPort());
        properties.setUsername(msgMailProperty.getUsername());
        properties.setPassword(msgMailProperty.getPassword());
        properties.getProperties().put("from", msgMailProperty.getEmailAddr());
        properties.getProperties().putAll(mailProperties.getProperties());
        return properties;
    }

    /**
     * 异步保存短信日志
     * 
     * @param sendInfo
     * @param sendResult
     * @param throwException
     */
    private void saveSmsMsgLog(MsgSmsSendInfo sendInfo, MsgSendResult sendResult, Exception throwException) {
        String tenantId = TenantContextHolder.getTenantId();
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            String noticeMethod = DictConstants.MsgNoticeMethod.SMS_METHOD;
            String msgSubject = sendInfo.getMsgSubject();
            String msgContent = sendInfo.getMsgContent();
            String hasAnnex = DictConstants.YesOrNoState.NO;
            String toUser = sendInfo.getToUserPhoneStr();
            String[] toUserPhones = sendInfo.getToUserPhones();
            toUser = null != toUserPhones ? StringUtils.join(toUserPhones, SPLIT) : toUser;
            String sceneRemark = sendInfo.getSceneRemark();
            String resultStatus = null == sendResult ? DictConstants.MsgResult.FAIL : sendResult.getStatusCode();
            String errMsg = null != sendResult ? sendResult.getErrmsg()
                : null == throwException ? null : throwException.getMessage();
            String jsonResponse = null == sendResult ? null : sendResult.getJsonResponse();
            // 如果消息类型是模板发送，需要根据模板ID查询消息模板，拼接完整消息,暂时直接存储模板数据
            if (DictConstants.MsgType.TEMPLATE_MSG.equals(sendInfo.getMsgType())) {
                String templateParamStr = sendInfo.getTemplateParamStr();
                msgContent = StringUtils.isBlank(templateParamStr)
                    ? StringUtils.join(sendInfo.getTemplateParams().toArray(new String[] {})) : templateParamStr;
            }
            MsgLog log = new MsgLog(sendInfo.getReceivedSeq(), noticeMethod, msgSubject, msgContent, hasAnnex, null,
                null, null, toUser, sceneRemark, resultStatus, errMsg, jsonResponse);
            saveMsgLog(log, null);
        });
    }

    /**
     * 保存邮件日志
     * 
     * @param sendInfo
     * @param sendResult
     * @param throwException
     */
    private void saveMailMsgLog(MsgMailSendInfo sendInfo, MsgSendResult sendResult, Exception throwException) {
        // 涉及到附件，异步保存读取文件相对路径报错，暂时不走异步
        // CompletableFuture.runAsync(() -> {
        String noticeMethod = DictConstants.MsgNoticeMethod.MAIL_METHOD;
        String msgSubject = sendInfo.getSubject();
        String msgContent = sendInfo.getContent();
        List<MultipartFile> multipartFiles = sendInfo.getMultipartFiles();
        String hasAnnex =
            CollectionUtils.isNotEmpty(multipartFiles) ? DictConstants.YesOrNoState.YES : DictConstants.YesOrNoState.NO;
        String fromUser = sendInfo.getFrom();
        String toUser = sendInfo.getToMailStr();
        String[] tos = sendInfo.getTos();
        toUser = null != tos ? StringUtils.join(tos, SPLIT) : toUser;
        String ccUser = sendInfo.getCcMailStr();
        String[] ccs = sendInfo.getCcs();
        ccUser = null != ccs ? StringUtils.join(ccs, SPLIT) : ccUser;
        toUser = StringUtils.isBlank(ccUser) ? toUser : toUser + "," + ccUser;
        String sceneRemark = sendInfo.getSceneRemark();
        String resultStatus = null == sendResult ? DictConstants.MsgResult.FAIL : sendResult.getStatusCode();
        String errMsg =
            null != sendResult ? sendResult.getErrmsg() : null == throwException ? null : throwException.getMessage();
        errMsg = StringUtils.isBlank(errMsg) || errMsg.length() < 255 ? errMsg : errMsg.substring(0, 255);
        String jsonResponse = null == sendResult ? null : sendResult.getJsonResponse();
        MsgLog log = new MsgLog(sendInfo.getReceivedSeq(), noticeMethod, msgSubject, msgContent, hasAnnex, fromUser,
            null, null, toUser, sceneRemark, resultStatus, errMsg, jsonResponse);
        saveMsgLog(log, multipartFiles);
        // });
    }

    /**
     * 异步保存微信日志
     * 
     * @param sendInfo
     * @param sendResult
     * @param throwException
     */
    private void saveWeixinMsgLog(MsgWeixinSendInfo sendInfo, MsgSendResult sendResult, Exception throwException) {
        String tenantId = TenantContextHolder.getTenantId();
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            String noticeMethod = DictConstants.MsgNoticeMethod.WECHAT_METHOD;
            String msgSubject = sendInfo.getMsgSubject();
            String msgContent = sendInfo.getTxtContent();
            String hasAnnex = DictConstants.YesOrNoState.NO;
            String openIdStr = sendInfo.getOpenIdStr();
            String[] openIds = sendInfo.getOpenIds();
            openIdStr = null != openIds ? StringUtils.join(openIds, SPLIT) : openIdStr;
            String sceneRemark = sendInfo.getSceneRemark();
            String resultStatus = null == sendResult ? DictConstants.MsgResult.FAIL : sendResult.getStatusCode();
            String errMsg = null != sendResult ? sendResult.getErrmsg()
                : null == throwException ? null : throwException.getMessage();
            String jsonResponse = null == sendResult ? null : sendResult.getJsonResponse();
            // 如果消息类型是模板发送，需要根据模板ID查询消息模板，拼接完整消息,暂时直接存储模板数据
            if (DictConstants.MsgType.TEMPLATE_MSG.equals(sendInfo.getMsgType())) {
                List<TemplateData> templateData = sendInfo.getTemplateData();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("templateData", templateData);
                if (StringUtils.isNotBlank(sendInfo.getRedirectUrl())) {
                    jsonObject.put("redirectUrl", sendInfo.getRedirectUrl());
                }
                msgContent = jsonObject.toJSONString();
            }
            // 根据appId查询平台维护的公众号信息
            String appId = sendInfo.getAppId();
            MsgOfficalAccount accountCondition = new MsgOfficalAccount();
            accountCondition.setAppId(appId);
            List<MsgOfficalAccount> accountList = officalAccountMapper.selectMsgOfficalAccountList(accountCondition);
            String officalAccountId = null;
            String officalAccountName = null;
            if (CollectionUtils.isNotEmpty(accountList)) {
                officalAccountId = accountList.get(0).getId();
                officalAccountName = accountList.get(0).getAppName();
            }
            String toUser = openIdStr;
            // 查询公众号的用户信息
            if (null != openIds && openIds.length > 0) {
                MsgOfficalAccountUser user = new MsgOfficalAccountUser();
                user.setAppId(appId);
                Map<String, Object> map = Maps.newHashMap();
                map.put("openIds", Arrays.asList(openIds));
                user.setParams(map);
                List<MsgOfficalAccountUser> userList = officalAccountUserMapper.selectMsgOfficalAccountUserList(user);
                if (CollectionUtils.isNotEmpty(userList)) {
                    String wxnames = userList.stream().map(MsgOfficalAccountUser::getWxName)
                        .reduce((e1, e2) -> e1 + "," + e2).orElseGet(null);
                    toUser = StringUtils.isBlank(wxnames) ? toUser : wxnames;
                }
            }
            MsgLog log = new MsgLog(sendInfo.getReceivedSeq(), noticeMethod, msgSubject, msgContent, hasAnnex, null,
                officalAccountId, officalAccountName, toUser, sceneRemark, resultStatus, errMsg, jsonResponse);
            saveMsgLog(log, null);
        });
    }

    /**
     * 异步保存钉钉日志
     * 
     * @param sendInfo
     * @param sendResult
     * @param throwException
     */
    private void saveDingtalkMsgLog(MsgDingTalkSendInfo sendInfo, MsgSendResult sendResult, Exception throwException) {
        String tenantId = TenantContextHolder.getTenantId();
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            String resultStatus = null == sendResult ? DictConstants.MsgResult.FAIL : sendResult.getStatusCode();
            String jsonResponse = null == sendResult ? null : sendResult.getJsonResponse();
            String errMsg = null != sendResult ? sendResult.getErrmsg()
                : null == throwException ? null : throwException.getMessage();
            if (null != sendResult && DictConstants.MsgResult.SUCCESS.equals(sendResult.getStatusCode())) {
                // 查询消息发送任务执行结果
                AsyncSendResult taskResult = null;
                Long taskId = (Long)sendResult.getData().get("taskId");
                try {
                    taskResult = msgDingTalkSendService.getTaskResult(sendInfo, taskId);
                    List<String> failedUserIdList = taskResult.getFailedUserIdList();
                    List<String> forbiddenUserIdList = taskResult.getForbiddenUserIdList();
                    List<Long> invalidDeptIdList = taskResult.getInvalidDeptIdList();
                    List<String> invalidUserIdList = taskResult.getInvalidUserIdList();
                    List<SendForbiddenModel> forbiddenList = taskResult.getForbiddenList();
                    if (CollectionUtils.isNotEmpty(failedUserIdList) || CollectionUtils.isNotEmpty(forbiddenUserIdList)
                        || CollectionUtils.isNotEmpty(invalidDeptIdList)
                        || CollectionUtils.isNotEmpty(invalidUserIdList) || CollectionUtils.isNotEmpty(forbiddenList)) {
                        resultStatus = DictConstants.MsgResult.FAIL;
                        jsonResponse = JSON.toJSONString(taskResult);
                    }
                } catch (Exception e) {
                    resultStatus = DictConstants.MsgResult.FAIL;
                    errMsg = e.getMessage();
                    LOG.error("The execution result of getting the DingTalk message sending task is abnormal :CorpId：{},appKey:{},agentId:{},taskId:{}", sendInfo.getCorpId(),
                        sendInfo.getAppKey(), sendInfo.getAgentId(), taskId, e);
                }
            }
            String noticeMethod = DictConstants.MsgNoticeMethod.DING_METHOD;
            String msgSubject = sendInfo.getMsgSubject();
            String text = sendInfo.getText();
            String title = sendInfo.getTitle();
            String messageUrl = sendInfo.getMessageUrl();
            String picUrl = sendInfo.getPicUrl();
            String mediaId = sendInfo.getMediaId();
            JSONObject jsonObject = new JSONObject();
            if (StringUtils.isNotBlank(title)) {
                jsonObject.put("title", title);
            }
            if (StringUtils.isNotBlank(text)) {
                jsonObject.put("text", text);
            }
            if (StringUtils.isNotBlank(title)) {
                jsonObject.put("messageUrl", messageUrl);
            }
            if (StringUtils.isNotBlank(picUrl)) {
                jsonObject.put("picUrl", picUrl);
            }
            if (StringUtils.isNotBlank(mediaId)) {
                jsonObject.put("mediaId", mediaId);
            }
            String msgContent = jsonObject.toJSONString();
            String hasAnnex = DictConstants.YesOrNoState.NO;
            String sceneRemark = sendInfo.getSceneRemark();
            // 根据corpId查询平台维护的企业信息
            MsgDingTeam team = new MsgDingTeam();
            team.setCorpId(sendInfo.getCorpId());
            List<MsgDingTeam> teamList = dingTeamMapper.selectMsgDingTeamList(team);
            String officalAccountId = null;
            String officalAccountName = null;
            if (CollectionUtils.isNotEmpty(teamList)) {
                officalAccountId = teamList.get(0).getId();
                officalAccountName = teamList.get(0).getTeamName();
            }
            String toUser =
                DictConstants.YesOrNoState.YES.equals(sendInfo.getToAllUser()) ? MessageUtils.message("msg.send.service.all.user") : sendInfo.getPhoneStr();
            MsgLog log = new MsgLog(sendInfo.getReceivedSeq(), noticeMethod, msgSubject, msgContent, hasAnnex, null,
                officalAccountId, officalAccountName, toUser, sceneRemark, resultStatus, errMsg, jsonResponse);
            saveMsgLog(log, null);
        });
    }

    /**
     * 保存消息发送日志
     * 
     * @param log
     * @param multipartFiles
     */
    private void saveMsgLog(MsgLog log, List<MultipartFile> multipartFiles) {
        String logId = String.valueOf(IdWorker.getNextStringId());
        log.setId(logId);
        log.setCreateTime(DateUtils.getNowDate());
        log.setReceivedSeq(StringUtils.isBlank(log.getReceivedSeq()) ? logId : log.getReceivedSeq());
        msgLogMapper.insertMsgLog(log);
        // 上传附件
        if (CollectionUtils.isEmpty(multipartFiles)) {
            return;
        }
        // 查询消息附件存放位置
        String attachmentDir = getAttachmentDir();
        for (MultipartFile file : multipartFiles) {
            try {
                String path = PlatformFileUploadUtils.upload(attachmentDir, file, null);
                MsgLogAnnex annex = new MsgLogAnnex();
                annex.setCreateTime(DateUtils.getNowDate());
                annex.setId(IdWorker.getNextStringId());
                annex.setFileName(file.getOriginalFilename());
                annex.setFilePath(path);
                annex.setLogId(logId);
                annex.setFileMd5(Md5Utils.hash(new File(path)));
                logAnnexMapper.insertMsgLogAnnex(annex);
            } catch (Exception e) {
                LOG.error("File attachment upload failed");
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取消息附件存储位置
     * 
     * @return
     */
    private String getAttachmentDir() {
        String baseDir = configService.selectConfigByKey(SysConfigConstants.MSG_ATTACHMENT_DIR_KEY);
        if (StringUtils.isBlank(baseDir)) {
            baseDir = DEFAULT_MSG_ATTACHMENT_DIR;
        }
        return baseDir;
    }

    /**
     * 根据微信公众号粉丝绑定手机号查询openId
     * 
     * @param appId
     * @param phoneStr
     * @return
     */
    private String[] getOpenIdsByBindPhone(String appId, String phoneStr) {
        MsgOfficalAccountUser condition = new MsgOfficalAccountUser();
        condition.setAppId(appId);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("phoneList", Arrays.asList(phoneStr.split(SPLIT)));
        condition.setParams(params);
        List<MsgOfficalAccountUser> list = officalAccountUserMapper.selectMsgOfficalAccountUserList(condition);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.stream().map(MsgOfficalAccountUser::getOpenId).collect(Collectors.toList())
            .toArray(new String[] {});
    }

    /**
     * 钉钉上传媒体文件
     */
    @Override
    public Map<String, String> uploadDingtalkMedia(MsgDingTalkSendInfo sendInfo, MultipartFile multipartFile) {
        String appSecrect = sendInfo.getAppSecrect();
        String appKey = sendInfo.getAppKey();
        String corpId = sendInfo.getCorpId();
        if (StringUtils.isBlank(appSecrect) && StringUtils.isNotBlank(appKey)) {
            appSecrect = getDingtalkAppSecrect(corpId, appKey);
            sendInfo.setAppSecrect(appSecrect);
            if (StringUtils.isBlank(appSecrect)) {
                throw new CustomException(MessageUtils.message("msg.send.service.first.add.ding.info"));
            }
        }
        try {
            Map<String, String> map = Maps.newHashMap();
            String mediaId = msgDingTalkSendService.uploadMedia(sendInfo, multipartFile);
            String extension = FileUploadUtils.getExtension(multipartFile);
            if ("amr".equals(FileUploadUtils.getExtension(multipartFile)) || "AMR".equals(extension)) {
                int duration = AmrDurationUtil.getAmrDuration(multipartFile);
                map.put("duration", String.valueOf(duration));
            }
            map.put("mediaId", mediaId);
            return map;
        } catch (Exception e) {
            LOG.error("DingTalk media file upload exception", e);
            throw new CustomException(e.getMessage());
        }
    }

    /**
     * 钉钉消息发送任务执行结果查询
     */
    @Override
    public AsyncSendResult getDingTaskResult(MsgDingTalkSendInfo sendInfo, Long taskId) {
        String appSecrect = sendInfo.getAppSecrect();
        String appKey = sendInfo.getAppKey();
        String corpId = sendInfo.getCorpId();
        if (StringUtils.isBlank(appSecrect) && StringUtils.isNotBlank(appKey)) {
            appSecrect = getDingtalkAppSecrect(corpId, appKey);
            sendInfo.setAppSecrect(appSecrect);
            if (StringUtils.isBlank(appSecrect)) {
                throw new CustomException(MessageUtils.message("msg.send.service.first.add.ding.info"));
            }
        }
        try {
            AsyncSendResult taskResult = msgDingTalkSendService.getTaskResult(sendInfo, taskId);
            return taskResult;
        } catch (Exception e) {
            LOG.error("Dingding message sending result is abnormal", e);
            throw new CustomException(e.getMessage());
        }
    }
}
