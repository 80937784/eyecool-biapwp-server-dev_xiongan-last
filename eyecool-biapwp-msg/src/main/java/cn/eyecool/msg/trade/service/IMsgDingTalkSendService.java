package cn.eyecool.msg.trade.service;

import org.springframework.web.multipart.MultipartFile;

import com.dingtalk.api.response.OapiMessageCorpconversationGetsendprogressResponse.AsyncSendProgress;
import com.dingtalk.api.response.OapiMessageCorpconversationGetsendresultResponse.AsyncSendResult;

import cn.eyecool.msg.trade.entity.MsgDingTalkSendInfo;
import cn.eyecool.msg.trade.entity.MsgSendResult;

/**
 * 钉钉消息发送服务
 * 
 * @author admin
 * @date 2020年3月31日
 */
public interface IMsgDingTalkSendService {

    /**
     * 发送钉钉消息
     * 
     * @param sendInfo
     * @return
     * @throws Exception
     */
    public MsgSendResult sendDingTalkMessage(MsgDingTalkSendInfo sendInfo) throws Exception;

    /**
     * 发送文本消息
     * 
     * @param sendInfo
     * @return
     * @throws Exception
     */
    public MsgSendResult sendTextContent(MsgDingTalkSendInfo sendInfo) throws Exception;

    /**
     * 发送链接消息
     * 
     * @param sendInfo
     * @return
     * @throws Exception
     */
    public MsgSendResult sendLinkMessage(MsgDingTalkSendInfo sendInfo) throws Exception;

    /**
     * 发送Markdown消息
     * 
     * @param sendInfo
     * @return
     * @throws Exception
     */
    public MsgSendResult sendMarkdownMessage(MsgDingTalkSendInfo sendInfo) throws Exception;

    /**
     * 发送图片
     * 
     * @param sendInfo
     * @return
     * @throws Exception
     */
    public MsgSendResult sendImageMessage(MsgDingTalkSendInfo sendInfo) throws Exception;

    /**
     * 发送语音
     * 
     * @param sendInfo
     * @return
     * @throws Exception
     */
    public MsgSendResult sendVoiceMessage(MsgDingTalkSendInfo sendInfo) throws Exception;

    /**
     * 发送普通文件
     * 
     * @param sendInfo
     * @return
     * @throws Exception
     */
    public MsgSendResult sendFileMessage(MsgDingTalkSendInfo sendInfo) throws Exception;

    /**
     * 消息发送进查询
     * 
     * @param sendInfo
     * @param taskId
     * @return
     * @throws Exception
     */
    public AsyncSendProgress getTaskProgress(MsgDingTalkSendInfo sendInfo, Long taskId) throws Exception;

    /**
     * 查询消息发送结果
     * 
     * @param sendInfo
     * @param taskId
     * @return
     * @throws Exception
     */
    public AsyncSendResult getTaskResult(MsgDingTalkSendInfo sendInfo, Long taskId) throws Exception;

    /**
     * 撤回消息任务
     * 
     * @param sendInfo
     * @param taskId
     * @throws Exception
     */
    public void revokeTask(MsgDingTalkSendInfo sendInfo, Long taskId) throws Exception;

    /**
     * 上传钉钉媒体文件
     * 
     * @param sendInfo
     * @param multipartFile
     * @return
     * @throws Exception
     */
    public String uploadMedia(MsgDingTalkSendInfo sendInfo, MultipartFile multipartFile) throws Exception;

}
