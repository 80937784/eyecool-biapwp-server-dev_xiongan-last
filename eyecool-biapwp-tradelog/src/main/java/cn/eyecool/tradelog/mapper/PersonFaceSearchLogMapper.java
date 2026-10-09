package cn.eyecool.tradelog.mapper;

import java.util.Date;
import java.util.List;

import cn.eyecool.tradelog.domain.police.DevicePolice;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.annotation.SqlParser;

import cn.eyecool.tradelog.domain.PersonFaceSearchLog;

/**
 * 人脸搜索日志Mapper接口
 * 
 * @author admin
 * @date 2021-04-29
 */
@SuppressWarnings("deprecation")
public interface PersonFaceSearchLogMapper {
    /**
     * 查询人脸搜索日志
     * 
     * @param id 人脸搜索日志ID
     * @return 人脸搜索日志
     */
    public PersonFaceSearchLog selectPersonFaceSearchLogById(String id);

    /**
     * 查询人脸搜索日志列表
     * 
     * @param personFaceSearchLog 人脸搜索日志
     * @return 人脸搜索日志集合
     */
    public List<PersonFaceSearchLog> selectPersonFaceSearchLogList(PersonFaceSearchLog personFaceSearchLog);

    /**
     * 查询人脸搜索日志最新比对列表
     * 
     * @param personFaceSearchLog 人脸搜索日志
     * @return 人脸搜索日志集合
     */
    public List<PersonFaceSearchLog> selectLastPersonFaceSearchLogList(PersonFaceSearchLog personFaceSearchLog);

    /**
     * 新增人脸搜索日志
     * 
     * @param personFaceSearchLog 人脸搜索日志
     * @return 结果
     */
    public int insertPersonFaceSearchLog(PersonFaceSearchLog personFaceSearchLog);

    /**
     * 修改人脸搜索日志
     * 
     * @param personFaceSearchLog 人脸搜索日志
     * @return 结果
     */
    public int updatePersonFaceSearchLog(PersonFaceSearchLog personFaceSearchLog);

    /**
     * 删除人脸搜索日志
     * 
     * @param id 人脸搜索日志ID
     * @return 结果
     */
    public int deletePersonFaceSearchLogById(String id);

    /**
     * 批量删除人脸搜索日志
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deletePersonFaceSearchLogByIds(String[] ids);

    /**
     * 根据时间范围查询日志
     * 
     * @param personFaceSearchLog
     * @return
     */
    public List<PersonFaceSearchLog> selectPersonFaceSearchLogByTimeRange(PersonFaceSearchLog personFaceSearchLog);

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

    /**
     * 查询人脸搜索日志数量
     * 
     * @param personFaceSearchLog
     * @return
     */
    public int selectPersonFaceSearchLogCount(PersonFaceSearchLog personFaceSearchLog);
    List<DevicePolice> selectDevicePoliceList(DevicePolice dev);
}
