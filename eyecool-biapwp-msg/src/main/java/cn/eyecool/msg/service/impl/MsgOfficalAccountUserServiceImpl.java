package cn.eyecool.msg.service.impl;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.weixin4j.WeixinException;
import org.weixin4j.component.UserComponent;
import org.weixin4j.model.user.Data;
import org.weixin4j.model.user.Followers;
import org.weixin4j.model.user.User;


import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.msg.configure.weixin.PlatformWeiXin;
import cn.eyecool.msg.configure.weixin.WeixinInstanceCache;
import cn.eyecool.msg.domain.MsgOfficalAccount;
import cn.eyecool.msg.domain.MsgOfficalAccountUser;
import cn.eyecool.msg.mapper.MsgOfficalAccountMapper;
import cn.eyecool.msg.mapper.MsgOfficalAccountUserMapper;
import cn.eyecool.msg.service.IMsgOfficalAccountUserService;

/**
 * 微信用户Service业务层处理
 * 
 * @author admin
 * @date 2021-04-15
 */
@Service
public class MsgOfficalAccountUserServiceImpl implements IMsgOfficalAccountUserService {

    private static final Logger LOG = LoggerFactory.getLogger(MsgOfficalAccountServiceImpl.class);

    @Autowired
    private MsgOfficalAccountMapper msgOfficalAccountMapper;
    @Autowired
    private MsgOfficalAccountUserMapper msgOfficalAccountUserMapper;

    /**
     * 查询微信用户
     * 
     * @param id 微信用户ID
     * @return 微信用户
     */
    @Override
    public MsgOfficalAccountUser selectMsgOfficalAccountUserById(String id) {
        return msgOfficalAccountUserMapper.selectMsgOfficalAccountUserById(id);
    }

    /**
     * 查询微信用户列表
     * 
     * @param msgOfficalAccountUser 微信用户
     * @return 微信用户
     */
    @Override
    public List<MsgOfficalAccountUser> selectMsgOfficalAccountUserList(MsgOfficalAccountUser msgOfficalAccountUser) {
        return msgOfficalAccountUserMapper.selectMsgOfficalAccountUserList(msgOfficalAccountUser);
    }

    /**
     * 新增微信用户
     * 
     * @param msgOfficalAccountUser 微信用户
     * @return 结果
     */
    @Override
    public int insertMsgOfficalAccountUser(MsgOfficalAccountUser msgOfficalAccountUser) {
        msgOfficalAccountUser.setId(IdWorker.getNextStringId());
        try {
            msgOfficalAccountUser.setCreateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        msgOfficalAccountUser.setCreateTime(DateUtils.getNowDate());
        return msgOfficalAccountUserMapper.insertMsgOfficalAccountUser(msgOfficalAccountUser);
    }

    /**
     * 修改微信用户
     * 
     * @param msgOfficalAccountUser 微信用户
     * @return 结果
     */
    @Override
    public int updateMsgOfficalAccountUser(MsgOfficalAccountUser msgOfficalAccountUser) {
        try {
            msgOfficalAccountUser.setUpdateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        msgOfficalAccountUser.setUpdateTime(DateUtils.getNowDate());
        return msgOfficalAccountUserMapper.updateMsgOfficalAccountUser(msgOfficalAccountUser);
    }

    /**
     * 批量删除微信用户
     * 
     * @param ids 需要删除的微信用户ID
     * @return 结果
     */
    @Override
    public int deleteMsgOfficalAccountUserByIds(String[] ids) {
        return msgOfficalAccountUserMapper.deleteMsgOfficalAccountUserByIds(ids);
    }

    /**
     * 删除微信用户信息
     * 
     * @param id 微信用户ID
     * @return 结果
     */
    @Override
    public int deleteMsgOfficalAccountUserById(String id) {
        return msgOfficalAccountUserMapper.deleteMsgOfficalAccountUserById(id);
    }

    /**
     * 拉取微信公众号用户
     * 
     * @param msgOfficalAccountUser
     */
    @Override
    public void pullWeixinUser(MsgOfficalAccountUser msgOfficalAccountUser) throws WeixinException {
        String appId = msgOfficalAccountUser.getAppId();
        MsgOfficalAccount accountConditon = new MsgOfficalAccount();
        accountConditon.setAppId(appId);
        List<MsgOfficalAccount> accountList = msgOfficalAccountMapper.selectMsgOfficalAccountList(accountConditon);
        if (CollectionUtils.isEmpty(accountList)) {
            return;
        }
        MsgOfficalAccount officalAccount = accountList.get(0);
        PlatformWeiXin weixin = WeixinInstanceCache.get(appId, officalAccount.getAppSecrect());
        UserComponent user = weixin.user();
        int count = 0;// 当前执行拉取的个数
        int total = 0;// 关注微信公众号的总个数
        String nextOpenId = null;// 一次最大拉取10000人
        AtomicLong failNum = new AtomicLong(0L);
        do {
            Followers all = user.get(nextOpenId);
            count = all.getCount();
            nextOpenId = all.getNext_openid();
            total = all.getTotal();
            Data data = all.getData();
            if (data == null || CollectionUtils.isEmpty(data.getOpenid())) {
                break;
            }
            data.getOpenid().stream().forEach(openId -> {
                try {
                    User userInfo = user.info(openId);
                    MsgOfficalAccountUser accountUser = new MsgOfficalAccountUser();
                    accountUser.setAppId(appId);
                    accountUser.setOpenId(openId);
                    accountUser.setOfficalAccountId(officalAccount.getId());
                    // 查询用户是否存在
                    boolean isExists = false;
                    List<MsgOfficalAccountUser> list =
                        msgOfficalAccountUserMapper.selectMsgOfficalAccountUserList(accountUser);
                    if (CollectionUtils.isNotEmpty(list)) {
                        isExists = true;
                        accountUser.setId(list.get(0).getId());
                        accountUser.setUpdateTime(DateUtils.getNowDate());
                    } else {
                        accountUser.setCreateTime(DateUtils.getNowDate());
                        accountUser.setId(String.valueOf(IdWorker.getNextStringId()));
                    }
                    accountUser.setWxName(userInfo.getNickname());
                    accountUser.setHeadImgUrl(userInfo.getHeadimgurl());
                    if (!isExists) {
                        msgOfficalAccountUserMapper.insertMsgOfficalAccountUser(accountUser);
                    } else {
                        msgOfficalAccountUserMapper.updateMsgOfficalAccountUser(accountUser);
                    }
                } catch (Exception e) {
                    failNum.incrementAndGet();
                    LOG.error("Failed to pull and save user information,openId：{}", openId, e);
                }
            });
        } while (total > 10000 && count >= 10000);
        if (failNum.get() > 0) {
            LOG.error("Failed to pull the user information part, the number of failures: {}", failNum.get());
            throw new CustomException(MessageUtils.message("msg.officalaccount.user.service.pull.user.error", failNum.get()));
        }

    }
}
