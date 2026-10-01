package cn.eyecool.msg.mapper;

import java.util.List;

import cn.eyecool.msg.domain.MsgOfficalAccountUser;

/**
 * 微信用户Mapper接口
 * 
 * @author admin
 * @date 2021-04-15
 */
public interface MsgOfficalAccountUserMapper {
    /**
     * 查询微信用户
     * 
     * @param id 微信用户ID
     * @return 微信用户
     */
    public MsgOfficalAccountUser selectMsgOfficalAccountUserById(String id);

    /**
     * 查询微信用户列表
     * 
     * @param msgOfficalAccountUser 微信用户
     * @return 微信用户集合
     */
    public List<MsgOfficalAccountUser> selectMsgOfficalAccountUserList(MsgOfficalAccountUser msgOfficalAccountUser);

    /**
     * 新增微信用户
     * 
     * @param msgOfficalAccountUser 微信用户
     * @return 结果
     */
    public int insertMsgOfficalAccountUser(MsgOfficalAccountUser msgOfficalAccountUser);

    /**
     * 修改微信用户
     * 
     * @param msgOfficalAccountUser 微信用户
     * @return 结果
     */
    public int updateMsgOfficalAccountUser(MsgOfficalAccountUser msgOfficalAccountUser);

    /**
     * 删除微信用户
     * 
     * @param id 微信用户ID
     * @return 结果
     */
    public int deleteMsgOfficalAccountUserById(String id);

    /**
     * 批量删除微信用户
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMsgOfficalAccountUserByIds(String[] ids);

    /**
     * 根据公众号主键删除公众号用户
     * 
     * @param officalAccountId
     */
    public void deleteAccountUserByAccountId(String officalAccountId);
}
