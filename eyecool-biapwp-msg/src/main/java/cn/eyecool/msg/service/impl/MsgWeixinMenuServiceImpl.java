package cn.eyecool.msg.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.weixin4j.WeixinException;
import org.weixin4j.component.MenuComponent;
import org.weixin4j.model.menu.Menu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

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
import cn.eyecool.msg.mapper.MsgWeixinMenuMapper;
import cn.eyecool.msg.mapper.MsgWeixinMenuReplyMapper;
import cn.eyecool.msg.service.IMsgWeixinMenuService;

/**
 * 微信公众号菜单Service业务层处理
 * 
 * @author admin
 * @date 2021-04-15
 */
@Service
public class MsgWeixinMenuServiceImpl implements IMsgWeixinMenuService {

    private static final Logger LOG = LoggerFactory.getLogger(MsgWeixinMenuServiceImpl.class);

    @Autowired
    private MsgWeixinMenuMapper msgWeixinMenuMapper;
    @Autowired
    private MsgOfficalAccountMapper msgOfficalAccountMapper;
    @Autowired
    private MsgWeixinMenuReplyMapper msgWeixinMenuReplyMapper;

    /**
     * 查询微信公众号菜单
     * 
     * @param id 微信公众号菜单ID
     * @return 微信公众号菜单
     */
    @Override
    public MsgWeixinMenu selectMsgWeixinMenuById(String id) {
        return msgWeixinMenuMapper.selectMsgWeixinMenuById(id);
    }

    /**
     * 查询微信公众号菜单列表
     * 
     * @param msgWeixinMenu 微信公众号菜单
     * @return 微信公众号菜单
     */
    @Override
    public List<MsgWeixinMenu> selectMsgWeixinMenuList(MsgWeixinMenu msgWeixinMenu) {
        return msgWeixinMenuMapper.selectMsgWeixinMenuList(msgWeixinMenu);
    }

    /**
     * 新增微信公众号菜单
     * 
     * @param msgWeixinMenu 微信公众号菜单
     * @return 结果
     */
    @Override
    @Transactional
    public int insertMsgWeixinMenu(MsgWeixinMenu msgWeixinMenu) {
        MsgWeixinMenu condition = new MsgWeixinMenu();
        condition.setAppId(msgWeixinMenu.getAppId());
        List<MsgWeixinMenu> list = msgWeixinMenuMapper.selectMsgWeixinMenuList(condition);
        if (CollectionUtils.isNotEmpty(list)) {
            throw new CustomException(MessageUtils.message("msg.weixin.menu.service.been.add.repeat"));
        }
        try {
            msgWeixinMenu.setCreateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        msgWeixinMenu.setCreateTime(DateUtils.getNowDate());
        msgWeixinMenu.setId(IdWorker.getNextStringId());
        int result = msgWeixinMenuMapper.insertMsgWeixinMenu(msgWeixinMenu);
        createMenu(msgWeixinMenu.getAppId(), msgWeixinMenu.getMenuJson());
        return result;
    }

    /**
     * 创建菜单
     * 
     * @param appId
     * @param menuJson
     */
    private void createMenu(String appId, String menuJson) {
        MsgOfficalAccount accountConditon = new MsgOfficalAccount();
        accountConditon.setAppId(appId);
        List<MsgOfficalAccount> accountList = msgOfficalAccountMapper.selectMsgOfficalAccountList(accountConditon);
        if (CollectionUtils.isEmpty(accountList)) {
            throw new CustomException(MessageUtils.message("msg.weixin.menu.service.officalaccount.not.exists"));
        }
        MsgOfficalAccount officalAccount = accountList.get(0);
        PlatformWeiXin weixin = WeixinInstanceCache.get(appId, officalAccount.getAppSecrect());
        MenuComponent menuComp = weixin.menu();
        Menu menu = null;
        try {
            Map<String, Object> menuContent = new HashMap<>();
            menuContent.put("menu", JSON.parseObject(menuJson));
            JSONObject menuObj = new JSONObject(menuContent);
            menu = new Menu(menuObj);
        } catch (Exception e) {
            LOG.error("The JSON format of the WeChat public account menu is incorrect", e);
            throw new CustomException(MessageUtils.message("msg.weixin.menu.service.json.format.error"));
        }
        try {
            menuComp.create(menu);
        } catch (WeixinException e) {
            LOG.error("WeChat official account menu is created abnormally", e);
            throw new CustomException(MessageUtils.message("msg.weixin.menu.service.create.menu.error", e.getMessage()));
        }
    }

    /**
     * 修改微信公众号菜单
     * 
     * @param msgWeixinMenu 微信公众号菜单
     * @return 结果
     */
    @Override
    @Transactional
    public int updateMsgWeixinMenu(MsgWeixinMenu msgWeixinMenu) {
        try {
            msgWeixinMenu.setUpdateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        msgWeixinMenu.setUpdateTime(DateUtils.getNowDate());
        int result = msgWeixinMenuMapper.updateMsgWeixinMenu(msgWeixinMenu);
        // 删除原菜单和菜单响应
        handleDeleteWeixinMenu(msgWeixinMenu.getAppId());
        // 重新创建微信菜单
        createMenu(msgWeixinMenu.getAppId(), msgWeixinMenu.getMenuJson());
        return result;
    }

    /**
     * 批量删除微信公众号菜单
     * 
     * @param ids 需要删除的微信公众号菜单ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteMsgWeixinMenuByIds(String[] ids) {
        int count = 0;
        for (String id : ids) {
            count += deleteMsgWeixinMenuById(id);
        }
        return count;
    }

    /**
     * 删除微信公众号菜单信息
     * 
     * @param id 微信公众号菜单ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteMsgWeixinMenuById(String id) {
        MsgWeixinMenu weixinMenu = msgWeixinMenuMapper.selectMsgWeixinMenuById(id);
        int result = msgWeixinMenuMapper.deleteMsgWeixinMenuById(id);
        handleDeleteWeixinMenu(weixinMenu.getAppId());
        return result;
    }

    /**
     * 处理删除微信公众号菜单和菜单响应
     * 
     * @param appId
     */
    private void handleDeleteWeixinMenu(String appId) {
        MsgOfficalAccount accountConditon = new MsgOfficalAccount();
        accountConditon.setAppId(appId);
        List<MsgOfficalAccount> accountList = msgOfficalAccountMapper.selectMsgOfficalAccountList(accountConditon);
        if (CollectionUtils.isEmpty(accountList)) {
            throw new CustomException(MessageUtils.message("msg.weixin.menu.service.officalaccount.not.exists"));
        }
        MsgOfficalAccount officalAccount = accountList.get(0);
        // 删除菜单响应
        MsgWeixinMenuReply reply = new MsgWeixinMenuReply();
        reply.setAppId(officalAccount.getAppId());
        List<MsgWeixinMenuReply> replyList = msgWeixinMenuReplyMapper.selectMsgWeixinMenuReplyList(reply);
        if (CollectionUtils.isNotEmpty(replyList)) {
            List<String> replyIdList = replyList.stream().map(MsgWeixinMenuReply::getId).collect(Collectors.toList());
            msgWeixinMenuReplyMapper.deleteMsgWeixinMenuReplyByIds(replyIdList.toArray(new String[] {}));
        }
        // 实际删除微信菜单
        PlatformWeiXin weixin = WeixinInstanceCache.get(officalAccount.getAppId(), officalAccount.getAppSecrect());
        MenuComponent menuComp = weixin.menu();
        try {
            menuComp.delete();
        } catch (WeixinException e) {
            LOG.error("Wechat public account menu is deleted abnormally", e);
            throw new CustomException(MessageUtils.message("msg.weixin.menu.service.delete.menu.error", e.getMessage()));
        }
    }

}
