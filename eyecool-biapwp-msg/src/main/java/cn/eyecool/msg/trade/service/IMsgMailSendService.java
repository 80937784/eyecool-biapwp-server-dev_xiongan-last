package cn.eyecool.msg.trade.service;

import cn.eyecool.msg.trade.entity.MsgMailSendInfo;
import cn.eyecool.msg.trade.entity.MsgSendResult;

/**
 * 邮件发送服务接口
 * 
 * @author mawenjun
 * @version 1.0
 * @date 2020年3月19日
 *
 */
public interface IMsgMailSendService {

    /** 收件人分隔符 */
    public static final String SPLIT = ",";

    /**
     * 发送邮件接口
     * 
     * @author mawenjun
     * @param sendInfo
     * @return
     * @throws Exception
     * @date 2020年3月22日
     *
     */
    public MsgSendResult sendMailInfo(MsgMailSendInfo sendInfo) throws Exception;

    /**
     * 发送简单文本邮件
     * 
     * @author mawenjun
     * @param sendInfo
     * @return
     * @throws Exception
     * @date 2020年3月22日
     *
     */
    public MsgSendResult sendSimpleMail(MsgMailSendInfo sendInfo) throws Exception;

    /**
     * 发送文本附件邮件
     * 
     * @author mawenjun
     * @param sendInfo
     * @return
     * @throws Exception
     * @date 2020年3月22日
     *
     */
    public MsgSendResult sendTextWithAnnexMail(MsgMailSendInfo sendInfo) throws Exception;

    /**
     * 发送HTML文本邮件
     * 
     * @author mawenjun
     * @param sendInfo
     * @return
     * @throws Exception
     * @date 2020年3月22日
     *
     */
    public MsgSendResult sendHtmlMail(MsgMailSendInfo sendInfo) throws Exception;

    /**
     * 发送HTML附件邮件
     * 
     * @author mawenjun
     * @param sendInfo
     * @return
     * @throws Exception
     * @date 2020年3月22日
     *
     */
    public MsgSendResult sendHtmlWithAnnexMail(MsgMailSendInfo sendInfo) throws Exception;

    /**
     * 发送图片邮件
     * 
     * @author mawenjun
     * @param sendInfo
     * @return
     * @throws Exception
     * @date 2020年3月22日
     *
     */
    public MsgSendResult sendInlinkResourceMail(MsgMailSendInfo sendInfo) throws Exception;

}
