package cn.eyecool.msg.service;

import java.util.List;

import cn.eyecool.msg.domain.MsgSmsCloudAccount;

/**
 * 短信云账户Service接口
 * 
 * @author admin
 * @date 2021-04-15
 */
public interface IMsgSmsCloudAccountService {
    /**
     * 查询短信云账户
     * 
     * @param id 短信云账户ID
     * @return 短信云账户
     */
    public MsgSmsCloudAccount selectMsgSmsCloudAccountById(String id);

    /**
     * 查询短信云账户列表
     * 
     * @param msgSmsCloudAccount 短信云账户
     * @return 短信云账户集合
     */
    public List<MsgSmsCloudAccount> selectMsgSmsCloudAccountList(MsgSmsCloudAccount msgSmsCloudAccount);

    /**
     * 新增短信云账户
     * 
     * @param msgSmsCloudAccount 短信云账户
     * @return 结果
     */
    public int insertMsgSmsCloudAccount(MsgSmsCloudAccount msgSmsCloudAccount);

    /**
     * 修改短信云账户
     * 
     * @param msgSmsCloudAccount 短信云账户
     * @return 结果
     */
    public int updateMsgSmsCloudAccount(MsgSmsCloudAccount msgSmsCloudAccount);

    /**
     * 批量删除短信云账户
     * 
     * @param ids 需要删除的短信云账户ID
     * @return 结果
     */
    public int deleteMsgSmsCloudAccountByIds(String[] ids);

    /**
     * 删除短信云账户信息
     * 
     * @param id 短信云账户ID
     * @return 结果
     */
    public int deleteMsgSmsCloudAccountById(String id);

    /**
     * 校验云账户名称是否唯一
     * 
     * @param msgSmsCloudAccount
     * @return
     */
    public Boolean checkAppNameUnique(MsgSmsCloudAccount msgSmsCloudAccount);

    /**
     * 校验云账户AppId是否唯一
     * 
     * @param msgSmsCloudAccount
     * @return
     */
    public Boolean checkAppIdUnique(MsgSmsCloudAccount msgSmsCloudAccount);
}
