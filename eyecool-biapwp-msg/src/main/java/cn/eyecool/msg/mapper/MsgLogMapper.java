package cn.eyecool.msg.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.annotation.SqlParser;

import cn.eyecool.msg.domain.MsgLog;

/**
 * 消息日志Mapper接口
 * 
 * @author admin
 * @date 2021-04-15
 */
@SuppressWarnings("deprecation")
public interface MsgLogMapper {
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
     * 删除消息日志
     * 
     * @param id 消息日志ID
     * @return 结果
     */
    public int deleteMsgLogById(String id);

    /**
     * 批量删除消息日志
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMsgLogByIds(String[] ids);

    /**
     * 新增分区
     * 
     * @param schemaName
     * @param tableName
     * @param offsetDay
     */
    @SqlParser(filter = true)
    public void addPartition(@Param("schemaName") String schemaName, @Param("tableName") String tableName,
        @Param("offsetDay") int offsetDay);

    /**
     * 删除过期分区
     * 
     * @param schemaName 数据库名称
     * @param tableName 表名
     * @param holdDate 最早保留时间（小于此时间所在分区范围的其他分区都会被删除）
     */
    @SqlParser(filter = true)
    public void dropPartition(@Param("schemaName") String schemaName, @Param("tableName") String tableName,
        @Param("holdDate") Date holdDate);
}
