package cn.eyecool.tradelog.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.annotation.SqlParser;

import cn.eyecool.tradelog.domain.PersonIrisMatchLog;

/**
 * 虹膜比对日志Mapper接口
 * 
 * @author admin
 * @date 2021-05-07
 */
@SuppressWarnings("deprecation")
public interface PersonIrisMatchLogMapper {
    /**
     * 查询虹膜比对日志
     * 
     * @param id 虹膜比对日志ID
     * @return 虹膜比对日志
     */
    public PersonIrisMatchLog selectPersonIrisMatchLogById(String id);

    /**
     * 查询虹膜比对日志列表
     * 
     * @param personIrisMatchLog 虹膜比对日志
     * @return 虹膜比对日志集合
     */
    public List<PersonIrisMatchLog> selectPersonIrisMatchLogList(PersonIrisMatchLog personIrisMatchLog);

    /**
     * 查询虹膜比对日志最新比对列表
     * 
     * @param personIrisMatchLog 虹膜比对日志
     * @return 虹膜比对日志集合
     */
    public List<PersonIrisMatchLog> selectLastPersonIrisMatchLogList(PersonIrisMatchLog personIrisMatchLog);

    /**
     * 新增虹膜比对日志
     * 
     * @param personIrisMatchLog 虹膜比对日志
     * @return 结果
     */
    public int insertPersonIrisMatchLog(PersonIrisMatchLog personIrisMatchLog);

    /**
     * 修改虹膜比对日志
     * 
     * @param personIrisMatchLog 虹膜比对日志
     * @return 结果
     */
    public int updatePersonIrisMatchLog(PersonIrisMatchLog personIrisMatchLog);

    /**
     * 删除虹膜比对日志
     * 
     * @param id 虹膜比对日志ID
     * @return 结果
     */
    public int deletePersonIrisMatchLogById(String id);

    /**
     * 批量删除虹膜比对日志
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deletePersonIrisMatchLogByIds(String[] ids);

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
