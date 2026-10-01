package cn.eyecool.msg.mapper;

import java.util.List;
import cn.eyecool.msg.domain.MsgWeixinMenuReply;

/**
 * 微信公众号菜单回复Mapper接口
 * 
 * @author admin
 * @date 2021-04-15
 */
public interface MsgWeixinMenuReplyMapper 
{
    /**
     * 查询微信公众号菜单回复
     * 
     * @param id 微信公众号菜单回复ID
     * @return 微信公众号菜单回复
     */
    public MsgWeixinMenuReply selectMsgWeixinMenuReplyById(String id);

    /**
     * 查询微信公众号菜单回复列表
     * 
     * @param msgWeixinMenuReply 微信公众号菜单回复
     * @return 微信公众号菜单回复集合
     */
    public List<MsgWeixinMenuReply> selectMsgWeixinMenuReplyList(MsgWeixinMenuReply msgWeixinMenuReply);

    /**
     * 新增微信公众号菜单回复
     * 
     * @param msgWeixinMenuReply 微信公众号菜单回复
     * @return 结果
     */
    public int insertMsgWeixinMenuReply(MsgWeixinMenuReply msgWeixinMenuReply);

    /**
     * 修改微信公众号菜单回复
     * 
     * @param msgWeixinMenuReply 微信公众号菜单回复
     * @return 结果
     */
    public int updateMsgWeixinMenuReply(MsgWeixinMenuReply msgWeixinMenuReply);

    /**
     * 删除微信公众号菜单回复
     * 
     * @param id 微信公众号菜单回复ID
     * @return 结果
     */
    public int deleteMsgWeixinMenuReplyById(String id);

    /**
     * 批量删除微信公众号菜单回复
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMsgWeixinMenuReplyByIds(String[] ids);
}
