package cn.eyecool.msg.controller;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.msg.trade.entity.MsgDingTalkSendInfo;
import cn.eyecool.msg.trade.entity.MsgMailSendInfo;
import cn.eyecool.msg.trade.entity.MsgSendResult;
import cn.eyecool.msg.trade.entity.MsgSmsSendInfo;
import cn.eyecool.msg.trade.entity.MsgWeixinSendInfo;
import cn.eyecool.msg.trade.service.IMsgSendService;

/**
 * 消息发送Controller
 * 
 * @author admin
 * @date 2020-03-17
 */
@RestController
@RequestMapping("/msg/send")
public class MsgSendController extends BaseController {

    @Autowired
    private IMsgSendService msgSendService;

    /**
     * 发送短信
     * 
     * @param sendInfo
     * @return
     */
    @PreAuthorize("@ss.hasPermi('msg:send:sms')")
    @PostMapping("/sms")
    public AjaxResult sendSms(@RequestBody MsgSmsSendInfo sendInfo) {
        MsgSendResult result = msgSendService.sendSmsMessage(sendInfo);
        return AjaxResult.success(result);
    }

    /**
     * 发送邮件
     * 
     * @param sendInfo
     * @param files
     * @return
     */
    @PreAuthorize("@ss.hasPermi('msg:send:mail')")
    @PostMapping("/mail")
    public AjaxResult sendMail(MsgMailSendInfo sendInfo, MultipartFile[] files) {
        if (null != files && files.length > 0) {
            sendInfo.setMultipartFiles(Arrays.asList(files));
        }
        MsgSendResult result = msgSendService.sendMailMessage(sendInfo);
        return AjaxResult.success(result);
    }

    /**
     * 发送微信消息
     * 
     * @param sendInfo
     * @return
     */
    @PreAuthorize("@ss.hasPermi('msg:send:weixin')")
    @PostMapping("/weixin")
    public AjaxResult sendWeixin(@RequestBody MsgWeixinSendInfo sendInfo) {
        MsgSendResult result = msgSendService.sendWeixinMessage(sendInfo);
        return AjaxResult.success(result);
    }

    /**
     * 发送钉钉消息
     * 
     * @param sendInfo
     * @return
     */
    @PreAuthorize("@ss.hasPermi('msg:send:dingtalk')")
    @PostMapping("/dingtalk")
    public AjaxResult sendDingtalk(@RequestBody MsgDingTalkSendInfo sendInfo) {
        MsgSendResult result = msgSendService.sendDingMessage(sendInfo);
        return AjaxResult.success(result);
    }

    /**
     * 钉钉媒体文件上传
     * 
     * @param sendInfo
     * @param multipartFile
     * @return
     */
    @PreAuthorize("@ss.hasPermi('msg:send:dingMediaUpload')")
    @PostMapping("/dingtalk/upload")
    public AjaxResult uploadDingtalkMedia(MsgDingTalkSendInfo sendInfo,
        @RequestParam("file") MultipartFile multipartFile) {
        Map<String, String> map = msgSendService.uploadDingtalkMedia(sendInfo, multipartFile);
        return AjaxResult.success(map);
    }

}
