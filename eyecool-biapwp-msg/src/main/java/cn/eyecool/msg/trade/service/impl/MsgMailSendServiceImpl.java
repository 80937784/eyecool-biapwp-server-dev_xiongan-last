package cn.eyecool.msg.trade.service.impl;

import java.io.File;
import java.util.List;

import javax.mail.internet.MimeMessage;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.msg.trade.entity.MsgMailSendInfo;
import cn.eyecool.msg.trade.entity.MsgSendResult;
import cn.eyecool.msg.trade.service.IMsgMailSendService;
import cn.eyecool.msg.util.MailUtil;

/**
 * 邮件发送服务实现类
 * 
 * @author mawenjun
 * @version 1.0
 * @date 2020年3月19日
 *
 */
@Service
public abstract class MsgMailSendServiceImpl implements IMsgMailSendService {

    // 使用方法注入的方法需要满足以下语法要求
    // <public|protected> [abstract] <return-type> theMethodName(no-arguments);
    @Lookup
    protected abstract JavaMailSenderImpl methodInject();

    /**
     * 发送邮件信息
     */
    @Override
    public MsgSendResult sendMailInfo(MsgMailSendInfo sendInfo) throws Exception {
        String mailType = sendInfo.getMailType();
        List<MultipartFile> multipartFiles = sendInfo.getMultipartFiles();
        if (DictConstants.MailType.TEXT_MAIL.equals(mailType) && CollectionUtils.isEmpty(multipartFiles)) {
            return sendSimpleMail(sendInfo);
        } else if (DictConstants.MailType.TEXT_MAIL.equals(mailType) && CollectionUtils.isNotEmpty(multipartFiles)) {
            return sendTextWithAnnexMail(sendInfo);
        } else if (DictConstants.MailType.HTML_MAIL.equals(mailType) && CollectionUtils.isEmpty(multipartFiles)) {
            return sendHtmlMail(sendInfo);
        } else if (DictConstants.MailType.HTML_MAIL.equals(mailType) && CollectionUtils.isNotEmpty(multipartFiles)) {
            return sendHtmlWithAnnexMail(sendInfo);
        }
        throw new CustomException(MessageUtils.message("msg.mai.send.service.mailtype.unsupported", mailType ));
    }

    /**
     * 发送简单文本邮件
     */
    @Override
    public MsgSendResult sendSimpleMail(MsgMailSendInfo sendInfo) throws Exception {
        this.checkMail(sendInfo);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(sendInfo.getTos());
        if (null != sendInfo.getCcs()) {
            message.setCc(sendInfo.getCcs());
        }
        message.setSubject(sendInfo.getSubject());
        message.setText(sendInfo.getContent());
        message.setFrom(sendInfo.getFrom());
        message.setSentDate(DateUtils.getNowDate());
        JavaMailSenderImpl mailSender = methodInject();
        MailUtil.applyProperties(mailSender, sendInfo.getMailProperties());
        mailSender.send(message);
        MsgSendResult result = new MsgSendResult(DictConstants.MsgResult.SUCCESS, null, null);
        return result;
    }

    /**
     * 发送文本附件邮件
     */
    @Override
    public MsgSendResult sendTextWithAnnexMail(MsgMailSendInfo sendInfo) throws Exception {
        return executeSendAnnexMail(sendInfo);
    }

    /**
     * 发送HTML邮件
     */
    @Override
    public MsgSendResult sendHtmlMail(MsgMailSendInfo sendInfo) throws Exception {
        this.checkMail(sendInfo);
        JavaMailSenderImpl mailSender = methodInject();
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "GBK");
        helper.setTo(sendInfo.getTos());
        if (null != sendInfo.getCcs()) {
            helper.setCc(sendInfo.getCcs());
        }
        helper.setSubject(sendInfo.getSubject());
        helper.setText(sendInfo.getContent(), true);
        helper.setFrom(sendInfo.getFrom());
        helper.setSentDate(DateUtils.getNowDate());
        MailUtil.applyProperties(mailSender, sendInfo.getMailProperties());
        mailSender.send(message);
        MsgSendResult result = new MsgSendResult(DictConstants.MsgResult.SUCCESS, null, null);
        return result;
    }

    /**
     * 发送HTML附件邮件
     */
    @Override
    public MsgSendResult sendHtmlWithAnnexMail(MsgMailSendInfo sendInfo) throws Exception {
        return executeSendAnnexMail(sendInfo);
    }

    /**
     * 发送图片邮件
     */
    @Override
    public MsgSendResult sendInlinkResourceMail(MsgMailSendInfo sendInfo) throws Exception {
        this.checkMail(sendInfo);
        JavaMailSenderImpl mailSender = methodInject();
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(sendInfo.getTos());
        if (null != sendInfo.getCcs()) {
            helper.setCc(sendInfo.getCcs());
        }
        helper.setSubject(sendInfo.getSubject());
        helper.setText(sendInfo.getContent(), true);
        helper.setFrom(sendInfo.getFrom());
        helper.setSentDate(DateUtils.getNowDate());
        FileSystemResource res = new FileSystemResource(new File(sendInfo.getRscPath()));
        helper.addInline(sendInfo.getRscId(), res);
        MailUtil.applyProperties(mailSender, sendInfo.getMailProperties());
        mailSender.send(message);
        MsgSendResult result = new MsgSendResult(DictConstants.MsgResult.SUCCESS, null, null);
        return result;
    }

    /**
     * 发送附件邮件
     * 
     * @author mawenjun
     * @param sendInfo
     * @return
     * @throws Exception
     * @date 2020年3月22日
     *
     */
    private MsgSendResult executeSendAnnexMail(MsgMailSendInfo sendInfo) throws Exception {
        this.checkMail(sendInfo);
        JavaMailSenderImpl mailSender = methodInject();
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "GBK");
        boolean isHtml = DictConstants.MailType.HTML_MAIL.equals(sendInfo.getMailType());
        helper.setTo(sendInfo.getTos());
        if (null != sendInfo.getCcs()) {
            helper.setCc(sendInfo.getCcs());
        }
        helper.setSubject(sendInfo.getSubject());
        helper.setText(sendInfo.getContent(), isHtml);
        helper.setFrom(sendInfo.getFrom());
        helper.setSentDate(DateUtils.getNowDate());

        if (CollectionUtils.isNotEmpty(sendInfo.getMultipartFiles())) {
            for (MultipartFile multipartFile : sendInfo.getMultipartFiles()) {
                String originalFilename = multipartFile.getOriginalFilename();
                helper.addAttachment(originalFilename, multipartFile);
            }
        }
        MailUtil.applyProperties(mailSender, sendInfo.getMailProperties());
        mailSender.send(message);
        MsgSendResult result = new MsgSendResult(DictConstants.MsgResult.SUCCESS, null, null);
        return result;
    }

    /**
     * 邮件信息检测
     * 
     * @author mawenjun
     * @param sendInfo
     * @date 2020年3月22日
     *
     */
    private void checkMail(MsgMailSendInfo sendInfo) {
        String[] tos = sendInfo.getTos();
        String toMailStr = sendInfo.getToMailStr();
        String[] ccs = sendInfo.getCcs();
        String ccMailStr = sendInfo.getCcMailStr();
        MailProperties mailProperties = sendInfo.getMailProperties();
        tos = null != tos ? tos : StringUtils.isBlank(toMailStr) ? null : toMailStr.split(SPLIT);
        ccs = null != ccs ? ccs : StringUtils.isBlank(ccMailStr) ? null : ccMailStr.split(SPLIT);
        sendInfo.setTos(tos);
        sendInfo.setCcs(ccs);
        if (null == mailProperties) {
            throw new CustomException(MessageUtils.message("msg.mai.send.service.property.empty"));
        } else if (StringUtils.isBlank(mailProperties.getHost())) {
            throw new CustomException(MessageUtils.message("msg.mai.send.service.host.empty"));
        } else if (StringUtils.isBlank(mailProperties.getUsername())) {
            throw new CustomException(MessageUtils.message("msg.mai.send.service.username.empty"));
        } else if (StringUtils.isBlank(mailProperties.getPassword())) {
            throw new CustomException(MessageUtils.message("msg.mai.send.service.password.empty"));
        }
        if (null == sendInfo.getTos()) {
            throw new CustomException(MessageUtils.message("msg.mai.send.service.touser.empty"));
        }
        if (StringUtils.isBlank(sendInfo.getFrom())) {
            throw new CustomException(MessageUtils.message("msg.mai.send.service.fromuser.empty"));
        } else if (StringUtils.isBlank(mailProperties.getProperties().get("from"))) {
            mailProperties.getProperties().put("from", sendInfo.getFrom());
        }
        if (StringUtils.isBlank(sendInfo.getSubject())) {
            throw new CustomException(MessageUtils.message("msg.mai.send.service.subject.empty"));
        }

    }
}
