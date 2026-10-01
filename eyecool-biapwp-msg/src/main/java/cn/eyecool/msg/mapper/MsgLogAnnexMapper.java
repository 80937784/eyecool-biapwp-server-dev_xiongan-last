package cn.eyecool.msg.mapper;

import java.util.List;
import cn.eyecool.msg.domain.MsgLogAnnex;

/**
 * 消息日志附件Mapper接口
 * 
 * @author admin
 * @date 2021-04-15
 */
public interface MsgLogAnnexMapper 
{
    /**
     * 查询消息日志附件
     * 
     * @param id 消息日志附件ID
     * @return 消息日志附件
     */
    public MsgLogAnnex selectMsgLogAnnexById(String id);

    /**
     * 查询消息日志附件列表
     * 
     * @param msgLogAnnex 消息日志附件
     * @return 消息日志附件集合
     */
    public List<MsgLogAnnex> selectMsgLogAnnexList(MsgLogAnnex msgLogAnnex);

    /**
     * 新增消息日志附件
     * 
     * @param msgLogAnnex 消息日志附件
     * @return 结果
     */
    public int insertMsgLogAnnex(MsgLogAnnex msgLogAnnex);

    /**
     * 修改消息日志附件
     * 
     * @param msgLogAnnex 消息日志附件
     * @return 结果
     */
    public int updateMsgLogAnnex(MsgLogAnnex msgLogAnnex);

    /**
     * 删除消息日志附件
     * 
     * @param id 消息日志附件ID
     * @return 结果
     */
    public int deleteMsgLogAnnexById(String id);

    /**
     * 批量删除消息日志附件
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMsgLogAnnexByIds(String[] ids);
}
