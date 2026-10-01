package cn.eyecool.msg.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.weixin4j.WeixinException;
import org.weixin4j.component.MenuComponent;

import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.msg.configure.weixin.PlatformWeiXin;
import cn.eyecool.msg.configure.weixin.WeixinInstanceCache;
import cn.eyecool.msg.domain.MsgOfficalAccount;
import cn.eyecool.msg.domain.MsgWeixinMenu;
import cn.eyecool.msg.domain.MsgWeixinMenuReply;
import cn.eyecool.msg.mapper.MsgOfficalAccountMapper;
import cn.eyecool.msg.mapper.MsgOfficalAccountUserMapper;
import cn.eyecool.msg.mapper.MsgTemplateMapper;
import cn.eyecool.msg.mapper.MsgWeixinMenuMapper;
import cn.eyecool.msg.mapper.MsgWeixinMenuReplyMapper;
import cn.eyecool.msg.service.IMsgOfficalAccountService;

/**
 * 微信公众号Service业务层处理
 * 
 * @author admin
 * @date 2021-04-15
 */
@Service
public class MsgOfficalAccountServiceImpl implements IMsgOfficalAccountService {

    private static final Logger LOG = LoggerFactory.getLogger(MsgOfficalAccountServiceImpl.class);

    @Autowired
    private MsgOfficalAccountMapper msgOfficalAccountMapper;
    @Autowired
    private MsgOfficalAccountUserMapper msgOfficalAccountUserMapper;
    @Autowired
    private MsgTemplateMapper msgTemplateMapper;
    @Autowired
    private MsgWeixinMenuMapper msgWeixinMenuMapper;
    @Autowired
    private MsgWeixinMenuReplyMapper msgWeixinMenuReplyMapper;

    /**
     * 查询微信公众号
     * 
     * @param id 微信公众号ID
     * @return 微信公众号
     */
    @Override
    public MsgOfficalAccount selectMsgOfficalAccountById(String id) {
        return msgOfficalAccountMapper.selectMsgOfficalAccountById(id);
    }

    /**
     * 查询微信公众号列表
     * 
     * @param msgOfficalAccount 微信公众号
     * @return 微信公众号
     */
    @Override
    public List<MsgOfficalAccount> selectMsgOfficalAccountList(MsgOfficalAccount msgOfficalAccount) {
        return msgOfficalAccountMapper.selectMsgOfficalAccountList(msgOfficalAccount);
    }

    /**
     * 新增微信公众号
     * 
     * @param msgOfficalAccount 微信公众号
     * @return 结果
     */
    @Override
    @Transactional
    public int insertMsgOfficalAccount(MsgOfficalAccount msgOfficalAccount) {
        if (!this.checkAppNameUnique(msgOfficalAccount)) {
            throw new CustomException(MessageUtils.message("msg.officalaccount.service.name.exists"));
        }
        if (!this.checkAppIdUnique(msgOfficalAccount)) {
            throw new CustomException(MessageUtils.message("msg.officalaccount.service.appid.exists"));
        }
        try {
            msgOfficalAccount.setCreateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        msgOfficalAccount.setId(IdWorker.getNextStringId());
        msgOfficalAccount.setCreateTime(DateUtils.getNowDate());
        int result = msgOfficalAccountMapper.insertMsgOfficalAccount(msgOfficalAccount);
        WeixinInstanceCache.refresh(msgOfficalAccount.getAppId(), msgOfficalAccount.getAppSecrect());
        return result;
    }

    /**
     * 修改微信公众号
     * 
     * @param msgOfficalAccount 微信公众号
     * @return 结果
     */
    @Override
    @Transactional
    public int updateMsgOfficalAccount(MsgOfficalAccount msgOfficalAccount) {
        if (!this.checkAppNameUnique(msgOfficalAccount)) {
            throw new CustomException(MessageUtils.message("msg.officalaccount.service.name.exists.check"));
        }
        if (!this.checkAppIdUnique(msgOfficalAccount)) {
            throw new CustomException(MessageUtils.message("msg.officalaccount.service.appid.exists.check"));
        }
        MsgOfficalAccount account = msgOfficalAccountMapper.selectMsgOfficalAccountById(msgOfficalAccount.getId());
        try {
            msgOfficalAccount.setUpdateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        msgOfficalAccount.setUpdateTime(DateUtils.getNowDate());
        int result = msgOfficalAccountMapper.updateMsgOfficalAccount(msgOfficalAccount);
        if (account.getAppId().equals(msgOfficalAccount.getAppId())) {
            WeixinInstanceCache.refresh(msgOfficalAccount.getAppId(), msgOfficalAccount.getAppSecrect());
        } else {
            WeixinInstanceCache.remove(account.getAppId());
        }
        return result;
    }

    /**
     * 批量删除微信公众号
     * 
     * @param ids 需要删除的微信公众号ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteMsgOfficalAccountByIds(String[] ids) {
        int count = 0;
        for (String id : ids) {
            count += deleteMsgOfficalAccountById(id);
        }
        return count;
    }

    /**
     * 删除微信公众号信息
     * 
     * @param id 微信公众号ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteMsgOfficalAccountById(String id) {
        MsgOfficalAccount account = msgOfficalAccountMapper.selectMsgOfficalAccountById(id);
        // 删除公众号用户
        msgOfficalAccountUserMapper.deleteAccountUserByAccountId(id);
        // 删除和公众关联的模板
        msgTemplateMapper.deleteTemplateByAccountId(id);
        // 删除公众号菜单和响应
        deleteMsgWeixinMenuAndReply(id);
        int result = msgOfficalAccountMapper.deleteMsgOfficalAccountById(id);
        WeixinInstanceCache.remove(account.getAppId());
        return result;
    }

    /**
     * 删除公众号菜单和响应
     * 
     * @param officalAccountId
     * @return
     */
    private void deleteMsgWeixinMenuAndReply(String officalAccountId) {
        MsgOfficalAccount officalAccount = msgOfficalAccountMapper.selectMsgOfficalAccountById(officalAccountId);
        // 删除菜单响应
        MsgWeixinMenuReply reply = new MsgWeixinMenuReply();
        reply.setAppId(officalAccount.getAppId());
        List<MsgWeixinMenuReply> replyList = msgWeixinMenuReplyMapper.selectMsgWeixinMenuReplyList(reply);
        if (CollectionUtils.isNotEmpty(replyList)) {
            List<String> replyIdList = replyList.stream().map(MsgWeixinMenuReply::getId).collect(Collectors.toList());
            msgWeixinMenuReplyMapper.deleteMsgWeixinMenuReplyByIds(replyIdList.toArray(new String[] {}));
        }
        MsgWeixinMenu condition = new MsgWeixinMenu();
        condition.setAppId(officalAccount.getAppId());
        List<MsgWeixinMenu> list = msgWeixinMenuMapper.selectMsgWeixinMenuList(condition);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        msgWeixinMenuMapper.deleteMsgWeixinMenuById(list.get(0).getId());
        // 实际删除微信菜单
        PlatformWeiXin weixin = WeixinInstanceCache.get(officalAccount.getAppId(), officalAccount.getAppSecrect());
        MenuComponent menuComp = weixin.menu();
        try {
            menuComp.delete();
        } catch (WeixinException e) {
            LOG.error("Wechat public account menu is deleted abnormally", e);
            throw new CustomException(MessageUtils.message("msg.officalaccount.service.menu.delete.error", e.getMessage()));
        }
    }

    /**
     * 校验公众号名称是否唯一
     * 
     * @param msgOfficalAccount
     * @return
     */
    @Override
    public Boolean checkAppNameUnique(MsgOfficalAccount msgOfficalAccount) {
        String id = msgOfficalAccount.getId();
        MsgOfficalAccount info = msgOfficalAccountMapper.checkAppNameUnique(msgOfficalAccount.getAppName());
        return null == info || info.getId().equals(id);
    }

    /**
     * 校验公众号AppId是否唯一
     * 
     * @param msgOfficalAccount
     * @return
     */
    @Override
    public Boolean checkAppIdUnique(MsgOfficalAccount msgOfficalAccount) {
        String id = msgOfficalAccount.getId();
        MsgOfficalAccount info = msgOfficalAccountMapper.checkAppIdUnique(msgOfficalAccount.getAppId());
        return null == info || info.getId().equals(id);
    }
}
