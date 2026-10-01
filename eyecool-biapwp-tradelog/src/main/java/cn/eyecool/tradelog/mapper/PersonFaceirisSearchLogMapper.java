package cn.eyecool.tradelog.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.annotation.SqlParser;

import cn.eyecool.tradelog.domain.PersonFaceirisSearchLog;

/**
 * 人脸虹膜搜索日志Mapper接口
 * 
 * @author admin
 * @date 2021-05-08
 */
@SuppressWarnings("deprecation")
public interface PersonFaceirisSearchLogMapper {
    /**
     * 查询人脸虹膜搜索日志
     * 
     * @param id 人脸虹膜搜索日志ID
     * @return 人脸虹膜搜索日志
     */
    public PersonFaceirisSearchLog selectPersonFaceirisSearchLogById(String id);

    /**
     * 查询人脸虹膜搜索日志列表
     * 
     * @param personFaceirisSearchLog 人脸虹膜搜索日志
     * @return 人脸虹膜搜索日志集合
     */
    public List<PersonFaceirisSearchLog>
        selectPersonFaceirisSearchLogList(PersonFaceirisSearchLog personFaceirisSearchLog);

    /**
     * 查询人脸虹膜搜索日志最新比对列表
     * 
     * @param personFaceirisSearchLog 人脸虹膜搜索日志
     * @return 人脸虹膜搜索日志集合
     */
    public List<PersonFaceirisSearchLog>
        selectLastPersonFaceirisSearchLogList(PersonFaceirisSearchLog personFaceirisSearchLog);

    /**
     * 新增人脸虹膜搜索日志
     * 
     * @param personFaceirisSearchLog 人脸虹膜搜索日志
     * @return 结果
     */
    public int insertPersonFaceirisSearchLog(PersonFaceirisSearchLog personFaceirisSearchLog);

    /**
     * 修改人脸虹膜搜索日志
     * 
     * @param personFaceirisSearchLog 人脸虹膜搜索日志
     * @return 结果
     */
    public int updatePersonFaceirisSearchLog(PersonFaceirisSearchLog personFaceirisSearchLog);

    /**
     * 删除人脸虹膜搜索日志
     * 
     * @param id 人脸虹膜搜索日志ID
     * @return 结果
     */
    public int deletePersonFaceirisSearchLogById(String id);

    /**
     * 批量删除人脸虹膜搜索日志
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deletePersonFaceirisSearchLogByIds(String[] ids);

    /**
     * 根据时间范围查询人脸虹膜识别日志
     * 
     * @param personFaceirisSearchLog
     * @return
     */
    public List<PersonFaceirisSearchLog>
        selectPersonFaceIrisSearchLogByTimeRange(PersonFaceirisSearchLog personFaceirisSearchLog);

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
