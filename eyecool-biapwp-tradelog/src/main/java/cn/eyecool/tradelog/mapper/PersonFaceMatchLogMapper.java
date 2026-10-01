package cn.eyecool.tradelog.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.annotation.SqlParser;

import cn.eyecool.tradelog.domain.PersonFaceMatchLog;

/**
 * 人脸比对日志Mapper接口
 * 
 * @author admin
 * @date 2021-04-29
 */
@SuppressWarnings("deprecation")
public interface PersonFaceMatchLogMapper {
    /**
     * 查询人脸比对日志
     * 
     * @param id 人脸比对日志ID
     * @return 人脸比对日志
     */
    public PersonFaceMatchLog selectPersonFaceMatchLogById(String id);

    /**
     * 查询人脸比对日志列表
     * 
     * @param personFaceMatchLog 人脸比对日志
     * @return 人脸比对日志集合
     */
    public List<PersonFaceMatchLog> selectPersonFaceMatchLogList(PersonFaceMatchLog personFaceMatchLog);

    /**
     * 查询人脸比对日志最新比对列表
     * 
     * @param personFaceMatchLog 人脸比对日志
     * @return 人脸比对日志集合
     */
    public List<PersonFaceMatchLog> selectLastPersonFaceMatchLogList(PersonFaceMatchLog personFaceMatchLog);

    /**
     * 新增人脸比对日志
     * 
     * @param personFaceMatchLog 人脸比对日志
     * @return 结果
     */
    public int insertPersonFaceMatchLog(PersonFaceMatchLog personFaceMatchLog);

    /**
     * 修改人脸比对日志
     * 
     * @param personFaceMatchLog 人脸比对日志
     * @return 结果
     */
    public int updatePersonFaceMatchLog(PersonFaceMatchLog personFaceMatchLog);

    /**
     * 删除人脸比对日志
     * 
     * @param id 人脸比对日志ID
     * @return 结果
     */
    public int deletePersonFaceMatchLogById(String id);

    /**
     * 批量删除人脸比对日志
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deletePersonFaceMatchLogByIds(String[] ids);

    /**
     * 根据时间范围查询日志
     * 
     * @param personFaceMatchLog
     * @return
     */
    public List<PersonFaceMatchLog> selectPersonFaceMatchLogByTimeRange(PersonFaceMatchLog personFaceMatchLog);

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
