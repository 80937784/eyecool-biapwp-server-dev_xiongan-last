package cn.eyecool.msg.trade.service;

import cn.eyecool.msg.trade.entity.MsgSendResult;
import cn.eyecool.msg.trade.entity.MsgSmsSendInfo;

/**
 * 短信推送服务接口
 * 
 * @author admin
 * @date 2020年3月19日
 */
public interface IMsgSmsSendService {

    /** 收件人分隔符 */
    public static final String SPLIT = ",";

    /**
     * 短信发送接口
     * 
     * @author mawenjun
     * @param sendInfo
     * @return
     * @throws Exception
     * @date 2020年3月22日
     *
     */
    public MsgSendResult sendSmsMessage(MsgSmsSendInfo sendInfo) throws Exception;

    /**
     * 模板短信单发接口
     * 
     * @author mawenjun
     * @param sendInfo
     * @return
     * @throws Exception
     * @date 2020年3月22日
     *
     */
    public MsgSendResult sendSingleTemplateSms(MsgSmsSendInfo sendInfo) throws Exception;

    /**
     * 普通短信单发接口
     * 
     * @author mawenjun
     * @param sendInfo
     * @return
     * @throws Exception
     * @date 2020年3月22日
     *
     */
    public MsgSendResult sendSingleNormalSms(MsgSmsSendInfo sendInfo) throws Exception;

    /**
     * 模板短信群发接口
     * 
     * @author mawenjun
     * @param sendInfo
     * @return
     * @throws Exception
     * @date 2020年3月22日
     *
     */
    public MsgSendResult sendMultiTemplateSms(MsgSmsSendInfo sendInfo) throws Exception;

    /**
     * 普通短信群发接口
     * 
     * @author mawenjun
     * @param sendInfo
     * @return
     * @throws Exception
     * @date 2020年3月22日
     *
     */
    public MsgSendResult sendMultiNormalSms(MsgSmsSendInfo sendInfo) throws Exception;
}
