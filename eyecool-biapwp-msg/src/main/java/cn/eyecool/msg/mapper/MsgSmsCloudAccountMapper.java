package cn.eyecool.msg.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.eyecool.msg.domain.MsgSmsCloudAccount;

/**
 * 短信云账户Mapper接口
 * 
 * @author admin
 * @date 2021-04-15
 */
public interface MsgSmsCloudAccountMapper {
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
     * 删除短信云账户
     * 
     * @param id 短信云账户ID
     * @return 结果
     */
    public int deleteMsgSmsCloudAccountById(String id);

    /**
     * 批量删除短信云账户
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMsgSmsCloudAccountByIds(String[] ids);

    /**
     * 校验云账户名称是否唯一
     * 
     * @param appName
     * @return
     */
    public MsgSmsCloudAccount checkAppNameUnique(@Param("appName") String appName);

    /**
     * 校验云账户AppId是否唯一
     * 
     * @param appId
     * @return
     */
    public MsgSmsCloudAccount checkAppIdUnique(@Param("appId") String appId);
}
