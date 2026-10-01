package cn.eyecool.tradelog.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.annotation.SqlParser;

import cn.eyecool.tradelog.domain.PersonFingerMatchLog;

/**
 * 指纹比对日志Mapper接口
 * 
 * @author admin
 * @date 2021-05-06
 */
@SuppressWarnings("deprecation")
public interface PersonFingerMatchLogMapper {
    /**
     * 查询指纹比对日志
     * 
     * @param id 指纹比对日志ID
     * @return 指纹比对日志
     */
    public PersonFingerMatchLog selectPersonFingerMatchLogById(String id);

    /**
     * 查询指纹比对日志列表
     * 
     * @param personFingerMatchLog 指纹比对日志
     * @return 指纹比对日志集合
     */
    public List<PersonFingerMatchLog> selectPersonFingerMatchLogList(PersonFingerMatchLog personFingerMatchLog);

    /**
     * 查询指纹比对日志最新比对列表
     * 
     * @param personFingerMatchLog 指纹比对日志
     * @return 指纹比对日志集合
     */
    public List<PersonFingerMatchLog> selectLastPersonFingerMatchLogList(PersonFingerMatchLog personFingerMatchLog);

    /**
     * 新增指纹比对日志
     * 
     * @param personFingerMatchLog 指纹比对日志
     * @return 结果
     */
    public int insertPersonFingerMatchLog(PersonFingerMatchLog personFingerMatchLog);

    /**
     * 修改指纹比对日志
     * 
     * @param personFingerMatchLog 指纹比对日志
     * @return 结果
     */
    public int updatePersonFingerMatchLog(PersonFingerMatchLog personFingerMatchLog);

    /**
     * 删除指纹比对日志
     * 
     * @param id 指纹比对日志ID
     * @return 结果
     */
    public int deletePersonFingerMatchLogById(String id);

    /**
     * 批量删除指纹比对日志
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deletePersonFingerMatchLogByIds(String[] ids);

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
