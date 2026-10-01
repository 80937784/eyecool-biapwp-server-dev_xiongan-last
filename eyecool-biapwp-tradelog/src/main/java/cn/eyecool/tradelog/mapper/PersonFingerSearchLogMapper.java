package cn.eyecool.tradelog.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.annotation.SqlParser;

import cn.eyecool.tradelog.domain.PersonFingerSearchLog;

/**
 * 指纹搜索日志Mapper接口
 * 
 * @author admin
 * @date 2021-05-06
 */
@SuppressWarnings("deprecation")
public interface PersonFingerSearchLogMapper {
    /**
     * 查询指纹搜索日志
     * 
     * @param id 指纹搜索日志ID
     * @return 指纹搜索日志
     */
    public PersonFingerSearchLog selectPersonFingerSearchLogById(String id);

    /**
     * 查询指纹搜索日志列表
     * 
     * @param personFingerSearchLog 指纹搜索日志
     * @return 指纹搜索日志集合
     */
    public List<PersonFingerSearchLog> selectPersonFingerSearchLogList(PersonFingerSearchLog personFingerSearchLog);

    /**
     * 查询指纹搜索日志最新比对列表
     * 
     * @param personFingerSearchLog 指纹搜索日志
     * @return 指纹搜索日志集合
     */
    public List<PersonFingerSearchLog> selectLastPersonFingerSearchLogList(PersonFingerSearchLog personFingerSearchLog);

    /**
     * 新增指纹搜索日志
     * 
     * @param personFingerSearchLog 指纹搜索日志
     * @return 结果
     */
    public int insertPersonFingerSearchLog(PersonFingerSearchLog personFingerSearchLog);

    /**
     * 修改指纹搜索日志
     * 
     * @param personFingerSearchLog 指纹搜索日志
     * @return 结果
     */
    public int updatePersonFingerSearchLog(PersonFingerSearchLog personFingerSearchLog);

    /**
     * 删除指纹搜索日志
     * 
     * @param id 指纹搜索日志ID
     * @return 结果
     */
    public int deletePersonFingerSearchLogById(String id);

    /**
     * 批量删除指纹搜索日志
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deletePersonFingerSearchLogByIds(String[] ids);

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
