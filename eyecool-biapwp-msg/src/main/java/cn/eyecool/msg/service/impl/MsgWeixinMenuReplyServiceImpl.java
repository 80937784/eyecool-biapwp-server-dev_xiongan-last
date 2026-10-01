package cn.eyecool.msg.service.impl;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.msg.domain.MsgWeixinMenuReply;
import cn.eyecool.msg.mapper.MsgWeixinMenuReplyMapper;
import cn.eyecool.msg.service.IMsgWeixinMenuReplyService;

/**
 * 微信公众号菜单回复Service业务层处理
 * 
 * @author admin
 * @date 2021-04-15
 */
@Service
public class MsgWeixinMenuReplyServiceImpl implements IMsgWeixinMenuReplyService {
    @Autowired
    private MsgWeixinMenuReplyMapper msgWeixinMenuReplyMapper;

    /**
     * 查询微信公众号菜单回复
     * 
     * @param id 微信公众号菜单回复ID
     * @return 微信公众号菜单回复
     */
    @Override
    public MsgWeixinMenuReply selectMsgWeixinMenuReplyById(String id) {
        return msgWeixinMenuReplyMapper.selectMsgWeixinMenuReplyById(id);
    }

    /**
     * 查询微信公众号菜单回复列表
     * 
     * @param msgWeixinMenuReply 微信公众号菜单回复
     * @return 微信公众号菜单回复
     */
    @Override
    public List<MsgWeixinMenuReply> selectMsgWeixinMenuReplyList(MsgWeixinMenuReply msgWeixinMenuReply) {
        return msgWeixinMenuReplyMapper.selectMsgWeixinMenuReplyList(msgWeixinMenuReply);
    }

    /**
     * 新增微信公众号菜单回复
     * 
     * @param msgWeixinMenuReply 微信公众号菜单回复
     * @return 结果
     */
    @Override
    public int insertMsgWeixinMenuReply(MsgWeixinMenuReply msgWeixinMenuReply) {
        MsgWeixinMenuReply condition = new MsgWeixinMenuReply();
        condition.setAppId(msgWeixinMenuReply.getAppId());
        condition.setMenuBtnKey(msgWeixinMenuReply.getMenuBtnKey());
        List<MsgWeixinMenuReply> list = msgWeixinMenuReplyMapper.selectMsgWeixinMenuReplyList(condition);
        if (CollectionUtils.isNotEmpty(list)) {
            throw new CustomException(MessageUtils.message("msg.weixin.menu.reply.service.menu.been.add.repeat"));
        }
        msgWeixinMenuReply.setId(IdWorker.getNextStringId());
        msgWeixinMenuReply.setCreateTime(DateUtils.getNowDate());
        return msgWeixinMenuReplyMapper.insertMsgWeixinMenuReply(msgWeixinMenuReply);
    }

    /**
     * 修改微信公众号菜单回复
     * 
     * @param msgWeixinMenuReply 微信公众号菜单回复
     * @return 结果
     */
    @Override
    public int updateMsgWeixinMenuReply(MsgWeixinMenuReply msgWeixinMenuReply) {
        msgWeixinMenuReply.setUpdateTime(DateUtils.getNowDate());
        return msgWeixinMenuReplyMapper.updateMsgWeixinMenuReply(msgWeixinMenuReply);
    }

    /**
     * 批量删除微信公众号菜单回复
     * 
     * @param ids 需要删除的微信公众号菜单回复ID
     * @return 结果
     */
    @Override
    public int deleteMsgWeixinMenuReplyByIds(String[] ids) {
        return msgWeixinMenuReplyMapper.deleteMsgWeixinMenuReplyByIds(ids);
    }

    /**
     * 删除微信公众号菜单回复信息
     * 
     * @param id 微信公众号菜单回复ID
     * @return 结果
     */
    @Override
    public int deleteMsgWeixinMenuReplyById(String id) {
        return msgWeixinMenuReplyMapper.deleteMsgWeixinMenuReplyById(id);
    }
}
