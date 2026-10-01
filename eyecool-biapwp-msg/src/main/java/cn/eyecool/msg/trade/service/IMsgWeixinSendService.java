package cn.eyecool.msg.trade.service;

import cn.eyecool.msg.trade.entity.MsgSendResult;
import cn.eyecool.msg.trade.entity.MsgWeixinSendInfo;

/**
 * 微信消息推送服务接口
 * 
 * @author admin
 * @date 2020年3月24日
 */
public interface IMsgWeixinSendService {

    /** 收件人分隔符 */
    public static final String SPLIT = ",";

    /**
     * 发送微信消息
     * 
     * @param sendInfo
     * @return
     * @throws Exception
     */
    public MsgSendResult sendWeixinMessage(MsgWeixinSendInfo sendInfo) throws Exception;

    /**
     * 根据openid单发文本消息
     * 
     * @param sendInfo
     * @return
     * @throws Exception
     */
    public MsgSendResult customSendContent(MsgWeixinSendInfo sendInfo) throws Exception;

    /**
     * 根据openid列表群发文本消息
     * 
     * @param sendInfo
     * @return
     * @throws Exception
     */
    public MsgSendResult massSendContent(MsgWeixinSendInfo sendInfo) throws Exception;

    /**
     * 根据openid单发模板消息
     * 
     * @param sendInfo
     * @return
     * @throws Exception
     */
    public MsgSendResult customSendTemplateMsg(MsgWeixinSendInfo sendInfo) throws Exception;

    /**
     * 根据openId列表群发模板消息
     * 
     * @param sendInfo
     * @return
     * @throws Exception
     */
    public MsgSendResult massSendTemplateMsg(MsgWeixinSendInfo sendInfo) throws Exception;

}
