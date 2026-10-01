package cn.eyecool.tradelog.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.annotation.SqlParser;

import cn.eyecool.tradelog.domain.PersonFaceCheckliveLog;

/**
 * 人员人脸检活日志Mapper接口
 * 
 * @author admin
 * @date 2021-09-02
 */
@SuppressWarnings("deprecation")
public interface PersonFaceCheckliveLogMapper {
    /**
     * 查询人员人脸检活日志
     * 
     * @param id 人员人脸检活日志ID
     * @return 人员人脸检活日志
     */
    public PersonFaceCheckliveLog selectPersonFaceCheckliveLogById(String id);

    /**
     * 查询人员人脸检活日志列表
     * 
     * @param personFaceCheckliveLog 人员人脸检活日志
     * @return 人员人脸检活日志集合
     */
    public List<PersonFaceCheckliveLog> selectPersonFaceCheckliveLogList(PersonFaceCheckliveLog personFaceCheckliveLog);

    /**
     * 新增人员人脸检活日志
     * 
     * @param personFaceCheckliveLog 人员人脸检活日志
     * @return 结果
     */
    public int insertPersonFaceCheckliveLog(PersonFaceCheckliveLog personFaceCheckliveLog);

    /**
     * 修改人员人脸检活日志
     * 
     * @param personFaceCheckliveLog 人员人脸检活日志
     * @return 结果
     */
    public int updatePersonFaceCheckliveLog(PersonFaceCheckliveLog personFaceCheckliveLog);

    /**
     * 删除人员人脸检活日志
     * 
     * @param id 人员人脸检活日志ID
     * @return 结果
     */
    public int deletePersonFaceCheckliveLogById(String id);

    /**
     * 批量删除人员人脸检活日志
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deletePersonFaceCheckliveLogByIds(String[] ids);

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
