package cn.eyecool.msg.trade.handler;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.weixin4j.WeixinException;
import org.weixin4j.model.message.OutputMessage;
import org.weixin4j.model.message.event.ClickEventMessage;
import org.weixin4j.model.message.event.LocationEventMessage;
import org.weixin4j.model.message.event.LocationSelectEventMessage;
import org.weixin4j.model.message.event.PicPhotoOrAlbumEventMessage;
import org.weixin4j.model.message.event.PicSysPhotoEventMessage;
import org.weixin4j.model.message.event.PicWeixinEventMessage;
import org.weixin4j.model.message.event.QrsceneScanEventMessage;
import org.weixin4j.model.message.event.QrsceneSubscribeEventMessage;
import org.weixin4j.model.message.event.ScanCodePushEventMessage;
import org.weixin4j.model.message.event.ScanCodeWaitMsgEventMessage;
import org.weixin4j.model.message.event.SubscribeEventMessage;
import org.weixin4j.model.message.event.UnSubscribeEventMessage;
import org.weixin4j.model.message.event.ViewEventMessage;
import org.weixin4j.model.message.output.TextOutputMessage;
import org.weixin4j.model.user.User;
import org.weixin4j.spi.IEventMessageHandler;

import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.msg.configure.weixin.PlatformWeiXin;
import cn.eyecool.msg.configure.weixin.WeixinInstanceCache;
import cn.eyecool.msg.domain.MsgOfficalAccount;
import cn.eyecool.msg.domain.MsgOfficalAccountUser;
import cn.eyecool.msg.domain.MsgWeixinMenuReply;
import cn.eyecool.msg.mapper.MsgOfficalAccountMapper;
import cn.eyecool.msg.mapper.MsgOfficalAccountUserMapper;
import cn.eyecool.msg.mapper.MsgWeixinMenuReplyMapper;

/**
 * 接收事件推送
 * 
 * @author mawenjun
 * @version 1.0
 * @date 2020年3月25日
 *
 */
@Service
public class WeixinEventMessageHandler implements IEventMessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeixinEventMessageHandler.class);

    @Autowired
    private MsgOfficalAccountMapper msgOfficalAccountMapper;
    @Autowired
    private MsgOfficalAccountUserMapper msgOfficalAccountUserMapper;
    @Autowired
    private MsgWeixinMenuReplyMapper msgWeixinMenuReplyMapper;
    @Value("${weixin.subscribe.reply}")
    private String subscribeReply;

    /**
     * 订阅事件
     * 
     * @author mawenjun
     * @param msg
     * @param appId
     * @return
     * @date 2020年3月25日
     *
     */
    public OutputMessage subscribe(SubscribeEventMessage msg, String appId) {
        LOGGER.info("Pay attention to WeChat public account,toUserName:{},fromUserName:{},appId:{}", msg.getToUserName(), msg.getFromUserName(),
            appId);
        TextOutputMessage out = new TextOutputMessage();
        out.setContent(subscribeReply);
        if (StringUtils.isBlank(appId)) {
            return out;
        }
        // 查询微信公众号信息
        MsgOfficalAccount accountInfo = queryOfficalAccount(appId);
        if (null == accountInfo) {
            return out;
        }
        String appSecrect = accountInfo.getAppSecrect();
        String officalAccountId = accountInfo.getId();
        MsgOfficalAccountUser condition = new MsgOfficalAccountUser();
        condition.setAppId(appId);
        condition.setOpenId(msg.getFromUserName());
        List<MsgOfficalAccountUser> officalAccountUserList =
            msgOfficalAccountUserMapper.selectMsgOfficalAccountUserList(condition);
        PlatformWeiXin platformWeiXin = WeixinInstanceCache.get(appId, appSecrect);
        if (CollectionUtils.isEmpty(officalAccountUserList)) {
            MsgOfficalAccountUser accountUser = new MsgOfficalAccountUser();
            accountUser.setId(IdWorker.getNextStringId());
            accountUser.setOpenId(msg.getFromUserName());
            accountUser.setCreateTime(DateUtils.getNowDate());
            accountUser.setAppId(appId);
            // 将公众号的主键填入
            accountUser.setOfficalAccountId(officalAccountId);
            try {
                User userInfo = platformWeiXin.user().info(msg.getFromUserName());
                accountUser.setWxName(userInfo.getNickname());
                accountUser.setHeadImgUrl(userInfo.getHeadimgurl());
                accountUser.setPhone(null);
            } catch (WeixinException e) {
                e.printStackTrace();
            }
            msgOfficalAccountUserMapper.insertMsgOfficalAccountUser(accountUser);
        }
        return out;
    }

    /**
     * 取消订阅公众号
     * 
     * @author mawenjun
     * @param msg
     * @param appId
     * @return
     * @date 2020年3月25日
     *
     */
    public OutputMessage unSubscribe(UnSubscribeEventMessage msg, String appId) {
        LOGGER.info("Unfollow WeChat Official Account,toUserName:{},fromUserName:{},appId:{}", msg.getToUserName(), msg.getFromUserName(),
            appId);
        TextOutputMessage out = new TextOutputMessage();
        out.setContent(MessageUtils.message("weixin.event.handler.msg.received"));
        if (StringUtils.isBlank(appId)) {
            return out;
        }
        MsgOfficalAccountUser condition = new MsgOfficalAccountUser();
        condition.setAppId(appId);
        condition.setOpenId(msg.getFromUserName());
        List<MsgOfficalAccountUser> officalAccountUserList =
            msgOfficalAccountUserMapper.selectMsgOfficalAccountUserList(condition);
        if (CollectionUtils.isNotEmpty(officalAccountUserList)) {
            msgOfficalAccountUserMapper.deleteMsgOfficalAccountUserById(officalAccountUserList.get(0).getId());
            LOGGER.info("weixin user {} deleted", msg.getFromUserName());
        }
        return out;
    }

    /**
     * 查询公众号信息
     * 
     * @param appId
     * @return
     */
    private MsgOfficalAccount queryOfficalAccount(String appId) {
        MsgOfficalAccount accountCondition = new MsgOfficalAccount();
        accountCondition.setAppId(appId);
        List<MsgOfficalAccount> accountList = msgOfficalAccountMapper.selectMsgOfficalAccountList(accountCondition);
        if (CollectionUtils.isEmpty(accountList)) {
            LOGGER.error("Failed to query WeChat public account,appId:{}", appId);
            return null;
        }
        return accountList.get(0);
    }

    /**
     * 菜单点击事件
     * 
     * @param msg
     * @param appId
     * @return
     */
    public OutputMessage click(ClickEventMessage msg, String appId) {
        LOGGER.debug("User clicks on the official account menu,eventKey:{},fromUserName:{},appId:{}", msg.getEventKey(), msg.getFromUserName(), appId);
        TextOutputMessage out = new TextOutputMessage();
        out.setContent(MessageUtils.message("weixin.event.handler.msg.received"));
        if (StringUtils.isBlank(appId)) {
            return out;
        }
        // 查询微信公众号信息
        MsgOfficalAccount accountInfo = queryOfficalAccount(appId);
        if (null == accountInfo) {
            return out;
        }
        String eventKey = msg.getEventKey();
        MsgWeixinMenuReply reply = new MsgWeixinMenuReply();
        reply.setAppId(appId);
        reply.setMenuBtnKey(eventKey);
        List<MsgWeixinMenuReply> replyList = msgWeixinMenuReplyMapper.selectMsgWeixinMenuReplyList(reply);
        if (CollectionUtils.isEmpty(replyList)) {
            return out;
        }
        MsgWeixinMenuReply menuReply = replyList.get(0);
        String replyContent = menuReply.getReplyContent();
        out.setContent(replyContent);
        return out;
    }

    @Override
    public OutputMessage subscribe(SubscribeEventMessage msg) {
        return null;
    }

    @Override
    public OutputMessage unSubscribe(UnSubscribeEventMessage msg) {
        return null;
    }

    @Override
    public OutputMessage qrsceneSubscribe(QrsceneSubscribeEventMessage msg) {
        return null;
    }

    @Override
    public OutputMessage qrsceneScan(QrsceneScanEventMessage msg) {
        return null;
    }

    @Override
    public OutputMessage location(LocationEventMessage msg) {
        return null;
    }

    @Override
    public OutputMessage click(ClickEventMessage msg) {
        return null;
    }

    @Override
    public OutputMessage view(ViewEventMessage msg) {
        return null;
    }

    @Override
    public OutputMessage scanCodePush(ScanCodePushEventMessage msg) {
        return null;
    }

    @Override
    public OutputMessage scanCodeWaitMsg(ScanCodeWaitMsgEventMessage msg) {
        return null;
    }

    @Override
    public OutputMessage picSysPhoto(PicSysPhotoEventMessage msg) {
        return null;
    }

    @Override
    public OutputMessage picPhotoOrAlbum(PicPhotoOrAlbumEventMessage msg) {
        return null;
    }

    @Override
    public OutputMessage picWeixin(PicWeixinEventMessage msg) {
        return null;
    }

    @Override
    public OutputMessage locationSelect(LocationSelectEventMessage msg) {
        return null;
    }
}
