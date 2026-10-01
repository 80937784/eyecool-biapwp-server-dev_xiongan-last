package cn.eyecool.msg.configure.ding;

import java.beans.Transient;

import org.apache.commons.lang.StringUtils;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.DingTalkConstants;
import com.dingtalk.api.request.OapiAttendanceRecordUploadRequest;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.request.OapiMediaUploadRequest;
import com.dingtalk.api.request.OapiMessageCorpconversationAsyncsendV2Request;
import com.dingtalk.api.request.OapiMessageCorpconversationGetsendprogressRequest;
import com.dingtalk.api.request.OapiMessageCorpconversationGetsendresultRequest;
import com.dingtalk.api.request.OapiMessageCorpconversationRecallRequest;
import com.dingtalk.api.request.OapiUserGetByMobileRequest;
import com.dingtalk.api.request.OapiV2DepartmentGetRequest;
import com.dingtalk.api.request.OapiV2DepartmentListsubRequest;
import com.dingtalk.api.request.OapiV2UserListRequest;
import com.dingtalk.api.response.OapiAttendanceRecordUploadResponse;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.dingtalk.api.response.OapiMediaUploadResponse;
import com.dingtalk.api.response.OapiMessageCorpconversationAsyncsendV2Response;
import com.dingtalk.api.response.OapiMessageCorpconversationGetsendprogressResponse;
import com.dingtalk.api.response.OapiMessageCorpconversationGetsendprogressResponse.AsyncSendProgress;
import com.dingtalk.api.response.OapiMessageCorpconversationGetsendresultResponse;
import com.dingtalk.api.response.OapiMessageCorpconversationGetsendresultResponse.AsyncSendResult;
import com.dingtalk.api.response.OapiMessageCorpconversationRecallResponse;
import com.dingtalk.api.response.OapiUserGetByMobileResponse;
import com.dingtalk.api.response.OapiV2DepartmentGetResponse;
import com.dingtalk.api.response.OapiV2DepartmentListsubResponse;
import com.dingtalk.api.response.OapiV2UserListResponse;
import com.taobao.api.ApiException;
import com.taobao.api.FileItem;

import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.MessageUtils;

/**
 * 钉钉基础支持对象
 * 
 * @author mawenjun
 * @date 2020年3月31日
 */
public class PlatformDingTalk {

    /** 同步锁 */
    private final static byte[] LOCK = new byte[0];

    /** 微应用agentId */
    private final Long agentId;
    /** 开发者appKey */
    private final String appKey;
    /** 开发者密钥 */
    private final String secret;
    /** 钉钉Token加载器 */
    private final DingTokenLoader tokenLoader = new DingTokenLoader();

    public PlatformDingTalk(Long agentId, String appKey, String secret) {
        this.agentId = agentId;
        this.appKey = appKey;
        this.secret = secret;
    }

    public Long getAgentId() {
        return agentId;
    }

    public String getAppKey() {
        return appKey;
    }

    public String getSecret() {
        return secret;
    }

    public DingTokenLoader getTokenLoader() {
        return tokenLoader;
    }

    /**
     * 获取Token
     * 
     * @return
     * @throws ApiException
     */
    @Transient
    public DingTalkToken token() throws ApiException {
        DingTalkToken token = tokenLoader.get(appKey);
        if (token == null) {
            synchronized (LOCK) {
                token = tokenLoader.get(appKey);
                if (token == null) {
                    try {
                        token = executeGetToken();
                        tokenLoader.refresh(token, appKey);
                    } catch (Exception e) {
                        throw new CustomException(e.getMessage(), e);
                    }
                }
            }
        }
        return token;
    }

    /**
     * 执行查询Token
     * 
     * @return
     * @throws ApiException
     */
    private DingTalkToken executeGetToken() throws ApiException {
        DefaultDingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/gettoken");
        OapiGettokenRequest request = new OapiGettokenRequest();
        request.setAppkey(appKey); // 替换刚才项目的appkey
        request.setAppsecret(secret);
        request.setHttpMethod(DingTalkConstants.HTTP_METHOD_GET);
        OapiGettokenResponse response = client.execute(request);
        return new DingTalkToken(response);
    }

    /**
     * 发送文本消息
     * 
     * @param isToAllUser
     * @param userIds
     * @param deptIds
     * @param textContent
     * @return
     * @throws ApiException
     */
    public Long sendTextMessage(boolean isToAllUser, String userIds, String deptIds, String textContent)
        throws ApiException {
        OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
        msg.setMsgtype(DictConstants.DingtalkType.TEXT);
        msg.setText(new OapiMessageCorpconversationAsyncsendV2Request.Text());
        msg.getText().setContent(textContent);
        return sendMessage(isToAllUser, userIds, deptIds, msg);
    }

    /**
     * 发送图片消息
     * 
     * @param isToAllUser
     * @param userIds
     * @param deptIds
     * @param mediaId
     * @return
     * @throws ApiException
     */
    public Long sendImageMessage(boolean isToAllUser, String userIds, String deptIds, String mediaId)
        throws ApiException {
        OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
        msg.setMsgtype(DictConstants.DingtalkType.IMAGE);
        msg.setImage(new OapiMessageCorpconversationAsyncsendV2Request.Image());
        msg.getImage().setMediaId(mediaId);
        return sendMessage(isToAllUser, userIds, deptIds, msg);
    }

    /**
     * 发送语音消息
     * 
     * @param isToAllUser
     * @param userIds
     * @param deptIds
     * @param mediaId
     * @param duration
     * @return
     * @throws ApiException
     */
    public Long sendVoiceMessage(boolean isToAllUser, String userIds, String deptIds, String mediaId, String duration)
        throws ApiException {
        OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
        msg.setMsgtype(DictConstants.DingtalkType.VOICE);
        msg.setVoice(new OapiMessageCorpconversationAsyncsendV2Request.Voice());
        msg.getVoice().setMediaId(mediaId);
        msg.getVoice().setDuration(duration);
        return sendMessage(isToAllUser, userIds, deptIds, msg);
    }

    /**
     * 发送文件消息
     * 
     * @param isToAllUser
     * @param userIds
     * @param deptIds
     * @param mediaId
     * @return
     * @throws ApiException
     */
    public Long sendFileMessage(boolean isToAllUser, String userIds, String deptIds, String mediaId)
        throws ApiException {
        OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
        msg.setMsgtype(DictConstants.DingtalkType.FILE);
        msg.setFile(new OapiMessageCorpconversationAsyncsendV2Request.File());
        msg.getFile().setMediaId(mediaId);
        return sendMessage(isToAllUser, userIds, deptIds, msg);
    }

    /**
     * 发送链接消息
     * 
     * @param isToAllUser
     * @param userIds
     * @param deptIds
     * @param info
     * @return
     * @throws ApiException
     */
    public Long sendLinkMessage(boolean isToAllUser, String userIds, String deptIds,
        DingTalkMsgInfo.DingTalkLinkMsgInfo info) throws ApiException {
        OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
        msg.setMsgtype(DictConstants.DingtalkType.LINK);
        msg.setLink(new OapiMessageCorpconversationAsyncsendV2Request.Link());
        msg.getLink().setTitle(info.getTitle());
        msg.getLink().setText(info.getText());
        msg.getLink().setMessageUrl(info.getMessageUrl());
        msg.getLink().setPicUrl(info.getPicUrl());
        return sendMessage(isToAllUser, userIds, deptIds, msg);
    }

    /**
     * 发送markdown消息
     * 
     * @param isToAllUser
     * @param userIds
     * @param deptIds
     * @param info
     * @return
     * @throws ApiException
     */
    public Long sendMarkdownMessage(boolean isToAllUser, String userIds, String deptIds,
        DingTalkMsgInfo.DingTalkMarkdownMsgInfo info) throws ApiException {
        OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
        msg.setMsgtype(DictConstants.DingtalkType.MARKDOWN);
        msg.setMarkdown(new OapiMessageCorpconversationAsyncsendV2Request.Markdown());
        msg.getMarkdown().setText(info.getText());
        msg.getMarkdown().setTitle(info.getTitle());
        return sendMessage(isToAllUser, userIds, deptIds, msg);
    }

    /**
     * 发送OA消息
     * 
     * @param isToAllUser
     * @param userIds
     * @param deptIds
     * @return
     * @throws ApiException
     */
    public Long sendOaMessage(boolean isToAllUser, String userIds, String deptIds) throws ApiException {
        // TODO 暂时不支持发送OA消息，不做开发，以后可以拓展
        OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
        msg.setMsgtype(DictConstants.DingtalkType.OA);
        msg.getOa().setHead(new OapiMessageCorpconversationAsyncsendV2Request.Head());
        msg.getOa().getHead().setText("head");
        msg.getOa().getHead().setBgcolor("#ccc");
        msg.getOa().setBody(new OapiMessageCorpconversationAsyncsendV2Request.Body());
        msg.getOa().getBody().setContent("xxx");
        return sendMessage(isToAllUser, userIds, deptIds, msg);
    }

    /**
     * 发送ActionCard消息
     * 
     * @param isToAllUser
     * @param userIds
     * @param deptIds
     * @return
     * @throws ApiException
     */
    public Long sendActionCardMessage(boolean isToAllUser, String userIds, String deptIds) throws ApiException {
        // TODO 暂时不支持发送ActionCard消息，不做开发，以后可以开发拓展
        OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
        msg.setActionCard(new OapiMessageCorpconversationAsyncsendV2Request.ActionCard());
        msg.setMsgtype(DictConstants.DingtalkType.ACTION_CARD);
        msg.getActionCard().setTitle("xxx123411111");
        msg.getActionCard().setMarkdown(MessageUtils.message("platform.dingtalk.actioncard.markdown"));
        msg.getActionCard().setSingleTitle(MessageUtils.message("platform.dingtalk.actioncard.title"));
        msg.getActionCard().setSingleUrl("https://www.baidu.com");
        return sendMessage(isToAllUser, userIds, deptIds, msg);
    }

    /**
     * 发送消息
     * 
     * @param isToAllUser
     * @param userIds
     * @param deptIds
     * @param msg
     * @return
     * @throws ApiException
     */
    private Long sendMessage(boolean isToAllUser, String userIds, String deptIds,
        OapiMessageCorpconversationAsyncsendV2Request.Msg msg) throws ApiException {
        if (!isToAllUser && StringUtils.isBlank(userIds) && StringUtils.isBlank(deptIds)) {
            throw new IllegalArgumentException("userIds and deptIds can not be null or empty at the same time ");
        }

        DingTalkClient client =
            new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2");
        OapiMessageCorpconversationAsyncsendV2Request request = new OapiMessageCorpconversationAsyncsendV2Request();
        request.setToAllUser(isToAllUser);
        request.setAgentId(agentId);
        request.setMsg(msg);
        if (StringUtils.isNotBlank(userIds)) {
            request.setUseridList(userIds);
        } else {
            request.setDeptIdList(deptIds);
        }
        OapiMessageCorpconversationAsyncsendV2Response response = client.execute(request, token().getAccess_token());
        if (response.getErrcode() == 0) {
            return response.getTaskId();
        }
        throw new CustomException(response.getErrcode() + ":" + response.getErrmsg());
    }

    /**
     * 查询工作消息通知的发送进度
     * 
     * @param taskId
     * @return
     * @throws ApiException
     */
    public AsyncSendProgress getTaskProgress(Long taskId) throws ApiException {
        if (null == taskId) {
            throw new IllegalArgumentException("taskId can not be null");
        }

        DingTalkClient client =
            new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/message/corpconversation/getsendprogress");
        OapiMessageCorpconversationGetsendprogressRequest request =
            new OapiMessageCorpconversationGetsendprogressRequest();
        request.setAgentId(agentId);
        request.setTaskId(taskId);
        OapiMessageCorpconversationGetsendprogressResponse response =
            client.execute(request, token().getAccess_token());
        if (response.getErrcode() == 0) {
            AsyncSendProgress progress = response.getProgress();
            return progress;
        }
        throw new CustomException(response.getErrcode() + ":" + response.getErrmsg());
    }

    /**
     * 查询工作消息通知的发送结果
     * 
     * @param taskId
     * @return
     * @throws ApiException
     */
    public AsyncSendResult getTaskResult(Long taskId) throws ApiException {
        if (null == taskId) {
            throw new IllegalArgumentException("taskId can not be null");
        }

        DingTalkClient client =
            new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/message/corpconversation/getsendresult");
        OapiMessageCorpconversationGetsendresultRequest request = new OapiMessageCorpconversationGetsendresultRequest();
        request.setAgentId(agentId);
        request.setTaskId(taskId);
        OapiMessageCorpconversationGetsendresultResponse response = client.execute(request, token().getAccess_token());
        if (response.getErrcode() == 0) {
            AsyncSendResult sendResult = response.getSendResult();
            return sendResult;
        }
        throw new CustomException(response.getErrcode() + ":" + response.getErrmsg());
    }

    /**
     * 工作通知消息撤回
     * 
     * @param taskId
     * @throws ApiException
     */
    public void revokeTask(Long taskId) throws ApiException {
        DingTalkClient client =
            new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/message/corpconversation/recall");
        OapiMessageCorpconversationRecallRequest request = new OapiMessageCorpconversationRecallRequest();
        request.setAgentId(agentId);
        request.setMsgTaskId(taskId);
        OapiMessageCorpconversationRecallResponse response = client.execute(request, token().getAccess_token());
        if (response.getErrcode() == 0) {
            return;
        }
        throw new CustomException(response.getErrcode() + ":" + response.getErrmsg());
    }

    /**
     * 根据手机号获取用户userId
     * 
     * @param phone
     * @return
     * @throws ApiException
     */
    public String getUserIdByMobile(String mobile) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/user/get_by_mobile");
        OapiUserGetByMobileRequest request = new OapiUserGetByMobileRequest();
        request.setMobile(mobile);
        OapiUserGetByMobileResponse response = client.execute(request, token().getAccess_token());
        if (response.getErrcode() == 0) {
            return response.getUserid();
        }
        throw new CustomException(response.getErrcode() + ":" + response.getErrmsg());
    }

    /**
     * 媒体文件上传接口
     * 
     * @param fileName
     * @param mediaType
     * @param content
     * @return
     * @throws ApiException
     */
    public OapiMediaUploadResponse uploadMedia(String fileName, String mediaType, byte[] content) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/media/upload");
        OapiMediaUploadRequest request = new OapiMediaUploadRequest();
        request.setType(mediaType);
        request.setMedia(new FileItem(fileName, content));
        OapiMediaUploadResponse response = client.execute(request, token().getAccess_token());
        return response;
    }

    /**
     * 查询子部门列表
     * 
     * @param deptId 部门ID
     * @return
     * @throws ApiException
     */
    public OapiV2DepartmentGetResponse deptDetail(Long deptId) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/department/get");
        OapiV2DepartmentGetRequest req = new OapiV2DepartmentGetRequest();
        req.setDeptId(deptId);
        req.setLanguage("zh_CN");
        OapiV2DepartmentGetResponse rsp = client.execute(req, token().getAccess_token());
        return rsp;
    }

    /**
     * 查询子部门列表
     * 
     * @param deptId 部门ID
     * @return
     * @throws ApiException
     */
    public OapiV2DepartmentListsubResponse listSubDept(Long deptId) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/department/listsub");
        OapiV2DepartmentListsubRequest req = new OapiV2DepartmentListsubRequest();
        if (null == deptId) {
            deptId = 1L;
        }
        req.setDeptId(deptId);
        req.setLanguage("zh_CN");
        OapiV2DepartmentListsubResponse rsp = client.execute(req, token().getAccess_token());
        return rsp;
    }

    /**
     * 分页查询部门下人员给详情列表
     * 
     * @param deptId 部门ID
     * @param cursor 分页游标 最开始传0
     * @param size 分页大小
     * @return
     * @throws ApiException
     */
    public OapiV2UserListResponse listPersonDetail(Long deptId, Long cursor, Long size) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/user/list");
        OapiV2UserListRequest req = new OapiV2UserListRequest();
        req.setDeptId(deptId);
        req.setCursor(cursor);
        req.setSize(size);
        req.setOrderField("modify_desc");// 按照部门信息修改时间降序
        req.setContainAccessLimit(false);
        req.setLanguage("zh_CN");
        OapiV2UserListResponse rsp = client.execute(req, token().getAccess_token());
        return rsp;
    }

    /**
     * 考勤打卡记录上传
     * 
     * @param userId 员工的userId
     * @param deviceSn 设备SN
     * @param deviceName 设备名称
     * @param photoUrl 打卡图片地址
     * @param userCheckTime 打卡时间
     * @return
     * @throws ApiException
     */
    public OapiAttendanceRecordUploadResponse attendanceRecordUpload(String userId, String deviceSn, String deviceName,
        String photoUrl, Long userCheckTime) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/attendance/record/upload");
        OapiAttendanceRecordUploadRequest req = new OapiAttendanceRecordUploadRequest();
        req.setUserid(userId);
        req.setDeviceName(deviceName);
        req.setDeviceId(deviceSn);
        req.setPhotoUrl(photoUrl);
        req.setUserCheckTime(userCheckTime);
        OapiAttendanceRecordUploadResponse rsp = client.execute(req, token().getAccess_token());
        return rsp;
    }

}
