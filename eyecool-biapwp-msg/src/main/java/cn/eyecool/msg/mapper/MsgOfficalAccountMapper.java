package cn.eyecool.msg.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.eyecool.msg.domain.MsgOfficalAccount;

/**
 * 微信公众号Mapper接口
 * 
 * @author admin
 * @date 2021-04-15
 */
public interface MsgOfficalAccountMapper {
    /**
     * 查询微信公众号
     * 
     * @param id 微信公众号ID
     * @return 微信公众号
     */
    public MsgOfficalAccount selectMsgOfficalAccountById(String id);

    /**
     * 查询微信公众号列表
     * 
     * @param msgOfficalAccount 微信公众号
     * @return 微信公众号集合
     */
    public List<MsgOfficalAccount> selectMsgOfficalAccountList(MsgOfficalAccount msgOfficalAccount);

    /**
     * 新增微信公众号
     * 
     * @param msgOfficalAccount 微信公众号
     * @return 结果
     */
    public int insertMsgOfficalAccount(MsgOfficalAccount msgOfficalAccount);

    /**
     * 修改微信公众号
     * 
     * @param msgOfficalAccount 微信公众号
     * @return 结果
     */
    public int updateMsgOfficalAccount(MsgOfficalAccount msgOfficalAccount);

    /**
     * 删除微信公众号
     * 
     * @param id 微信公众号ID
     * @return 结果
     */
    public int deleteMsgOfficalAccountById(String id);

    /**
     * 批量删除微信公众号
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMsgOfficalAccountByIds(String[] ids);

    /**
     * 校验公众号名称是否唯一
     * 
     * @param appName
     * @return
     */
    public MsgOfficalAccount checkAppNameUnique(@Param("appName") String appName);

    /**
     * 校验公众号AppId是否唯一
     * 
     * @param appId
     * @return
     */
    public MsgOfficalAccount checkAppIdUnique(@Param("appId") String appId);
}
