package cn.eyecool.tradelog.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.annotation.SqlParser;

import cn.eyecool.tradelog.domain.PersonIrisSearchLog;

/**
 * 虹膜搜索日志Mapper接口
 * 
 * @author admin
 * @date 2021-05-07
 */
@SuppressWarnings("deprecation")
public interface PersonIrisSearchLogMapper {
    /**
     * 查询虹膜搜索日志
     * 
     * @param id 虹膜搜索日志ID
     * @return 虹膜搜索日志
     */
    public PersonIrisSearchLog selectPersonIrisSearchLogById(String id);

    /**
     * 查询虹膜搜索日志列表
     * 
     * @param personIrisSearchLog 虹膜搜索日志
     * @return 虹膜搜索日志集合
     */
    public List<PersonIrisSearchLog> selectPersonIrisSearchLogList(PersonIrisSearchLog personIrisSearchLog);

    /**
     * 查询虹膜搜索日志最新比对列表
     * 
     * @param personIrisSearchLog 虹膜搜索日志
     * @return 虹膜搜索日志集合
     */
    public List<PersonIrisSearchLog> selectLastPersonIrisSearchLogList(PersonIrisSearchLog personIrisSearchLog);

    /**
     * 新增虹膜搜索日志
     * 
     * @param personIrisSearchLog 虹膜搜索日志
     * @return 结果
     */
    public int insertPersonIrisSearchLog(PersonIrisSearchLog personIrisSearchLog);

    /**
     * 修改虹膜搜索日志
     * 
     * @param personIrisSearchLog 虹膜搜索日志
     * @return 结果
     */
    public int updatePersonIrisSearchLog(PersonIrisSearchLog personIrisSearchLog);

    /**
     * 删除虹膜搜索日志
     * 
     * @param id 虹膜搜索日志ID
     * @return 结果
     */
    public int deletePersonIrisSearchLogById(String id);

    /**
     * 批量删除虹膜搜索日志
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deletePersonIrisSearchLogByIds(String[] ids);

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
