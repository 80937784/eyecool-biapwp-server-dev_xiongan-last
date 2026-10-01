package cn.eyecool.msg.service;

import java.util.List;
import cn.eyecool.msg.domain.MsgLog;

/**
 * 消息日志Service接口
 * 
 * @author admin
 * @date 2021-04-15
 */
public interface IMsgLogService 
{
    /**
     * 查询消息日志
     * 
     * @param id 消息日志ID
     * @return 消息日志
     */
    public MsgLog selectMsgLogById(String id);

    /**
     * 查询消息日志列表
     * 
     * @param msgLog 消息日志
     * @return 消息日志集合
     */
    public List<MsgLog> selectMsgLogList(MsgLog msgLog);

    /**
     * 新增消息日志
     * 
     * @param msgLog 消息日志
     * @return 结果
     */
    public int insertMsgLog(MsgLog msgLog);

    /**
     * 修改消息日志
     * 
     * @param msgLog 消息日志
     * @return 结果
     */
    public int updateMsgLog(MsgLog msgLog);

    /**
     * 批量删除消息日志
     * 
     * @param ids 需要删除的消息日志ID
     * @return 结果
     */
    public int deleteMsgLogByIds(String[] ids);

    /**
     * 删除消息日志信息
     * 
     * @param id 消息日志ID
     * @return 结果
     */
    public int deleteMsgLogById(String id);
}
