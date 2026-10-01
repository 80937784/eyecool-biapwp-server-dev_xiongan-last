package cn.eyecool.msg.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.msg.domain.MsgLog;
import cn.eyecool.msg.mapper.MsgLogMapper;
import cn.eyecool.msg.service.IMsgLogService;

/**
 * 消息日志Service业务层处理
 * 
 * @author admin
 * @date 2021-04-15
 */
@Service
public class MsgLogServiceImpl implements IMsgLogService {
    @Autowired
    private MsgLogMapper msgLogMapper;

    /**
     * 查询消息日志
     * 
     * @param id 消息日志ID
     * @return 消息日志
     */
    @Override
    public MsgLog selectMsgLogById(String id) {
        return msgLogMapper.selectMsgLogById(id);
    }

    /**
     * 查询消息日志列表
     * 
     * @param msgLog 消息日志
     * @return 消息日志
     */
    @Override
    public List<MsgLog> selectMsgLogList(MsgLog msgLog) {
        return msgLogMapper.selectMsgLogList(msgLog);
    }

    /**
     * 新增消息日志
     * 
     * @param msgLog 消息日志
     * @return 结果
     */
    @Override
    public int insertMsgLog(MsgLog msgLog) {
        msgLog.setId(IdWorker.getNextStringId());
        msgLog.setCreateTime(DateUtils.getNowDate());
        return msgLogMapper.insertMsgLog(msgLog);
    }

    /**
     * 修改消息日志
     * 
     * @param msgLog 消息日志
     * @return 结果
     */
    @Override
    public int updateMsgLog(MsgLog msgLog) {
        return msgLogMapper.updateMsgLog(msgLog);
    }

    /**
     * 批量删除消息日志
     * 
     * @param ids 需要删除的消息日志ID
     * @return 结果
     */
    @Override
    public int deleteMsgLogByIds(String[] ids) {
        return msgLogMapper.deleteMsgLogByIds(ids);
    }

    /**
     * 删除消息日志信息
     * 
     * @param id 消息日志ID
     * @return 结果
     */
    @Override
    public int deleteMsgLogById(String id) {
        return msgLogMapper.deleteMsgLogById(id);
    }
}
