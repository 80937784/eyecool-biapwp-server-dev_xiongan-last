package cn.eyecool.msg.trade.service;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.dingtalk.api.response.OapiMessageCorpconversationGetsendresultResponse.AsyncSendResult;

import cn.eyecool.msg.trade.entity.MsgDingTalkSendInfo;
import cn.eyecool.msg.trade.entity.MsgMailSendInfo;
import cn.eyecool.msg.trade.entity.MsgSendResult;
import cn.eyecool.msg.trade.entity.MsgSmsSendInfo;
import cn.eyecool.msg.trade.entity.MsgWeixinSendInfo;

/**
 * 平台消息推动统一服务接口
 * 
 * @author admin
 * @date 2020年3月19日
 */
public interface IMsgSendService {

    /**
     * 短信消息发送接口
     * 
     * @author mawenjun
     * @param sendInfo
     * @return
     * @date 2020年3月22日
     *
     */
    public MsgSendResult sendSmsMessage(MsgSmsSendInfo sendInfo);

    /**
     * 邮件消息发送接口
     * 
     * @author mawenjun
     * @param sendInfo
     * @return
     * @date 2020年3月22日
     *
     */
    public MsgSendResult sendMailMessage(MsgMailSendInfo sendInfo);

    /**
     * 微信消息发送接口
     * 
     * @author mawenjun
     * @param sendInfo
     * @return
     * @date 2020年3月22日
     *
     */
    public MsgSendResult sendWeixinMessage(MsgWeixinSendInfo sendInfo);

    /**
     * 钉钉消息发送接口
     * 
     * @author mawenjun
     * @param sendInfo
     * @return
     * @date 2020年3月22日
     *
     */
    public MsgSendResult sendDingMessage(MsgDingTalkSendInfo sendInfo);

    /**
     * 上传钉钉媒体文件
     * 
     * @param sendInfo
     * @param multipartFile
     * @return
     */
    public Map<String, String> uploadDingtalkMedia(MsgDingTalkSendInfo sendInfo, MultipartFile multipartFile);

    /**
     * 钉钉消息发送任务执行结果查询
     * 
     * @param sendInfo
     * @param valueOf
     * @return
     */
    public AsyncSendResult getDingTaskResult(MsgDingTalkSendInfo sendInfo, Long valueOf);
}
