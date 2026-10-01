package cn.eyecool.msg.mapper;

import java.util.List;
import cn.eyecool.msg.domain.MsgWeixinMenu;

/**
 * 微信公众号菜单Mapper接口
 * 
 * @author admin
 * @date 2021-04-15
 */
public interface MsgWeixinMenuMapper 
{
    /**
     * 查询微信公众号菜单
     * 
     * @param id 微信公众号菜单ID
     * @return 微信公众号菜单
     */
    public MsgWeixinMenu selectMsgWeixinMenuById(String id);

    /**
     * 查询微信公众号菜单列表
     * 
     * @param msgWeixinMenu 微信公众号菜单
     * @return 微信公众号菜单集合
     */
    public List<MsgWeixinMenu> selectMsgWeixinMenuList(MsgWeixinMenu msgWeixinMenu);

    /**
     * 新增微信公众号菜单
     * 
     * @param msgWeixinMenu 微信公众号菜单
     * @return 结果
     */
    public int insertMsgWeixinMenu(MsgWeixinMenu msgWeixinMenu);

    /**
     * 修改微信公众号菜单
     * 
     * @param msgWeixinMenu 微信公众号菜单
     * @return 结果
     */
    public int updateMsgWeixinMenu(MsgWeixinMenu msgWeixinMenu);

    /**
     * 删除微信公众号菜单
     * 
     * @param id 微信公众号菜单ID
     * @return 结果
     */
    public int deleteMsgWeixinMenuById(String id);

    /**
     * 批量删除微信公众号菜单
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMsgWeixinMenuByIds(String[] ids);
}
