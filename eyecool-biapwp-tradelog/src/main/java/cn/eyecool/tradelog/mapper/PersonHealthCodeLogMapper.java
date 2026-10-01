package cn.eyecool.tradelog.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.annotation.SqlParser;

import cn.eyecool.tradelog.domain.PersonHealthCodeLog;

/**
 * 健康码请求Mapper接口
 * 
 * @author admin
 * @date 2021-05-13
 */
@SuppressWarnings("deprecation")
public interface PersonHealthCodeLogMapper {
    /**
     * 查询健康码请求
     * 
     * @param id 健康码请求ID
     * @return 健康码请求
     */
    public PersonHealthCodeLog selectPersonHealthCodeLogById(String id);

    /**
     * 查询健康码请求列表
     * 
     * @param personHealthCodeLog 健康码请求
     * @return 健康码请求集合
     */
    public List<PersonHealthCodeLog> selectPersonHealthCodeLogList(PersonHealthCodeLog personHealthCodeLog);

    /**
     * 新增健康码请求
     * 
     * @param personHealthCodeLog 健康码请求
     * @return 结果
     */
    public int insertPersonHealthCodeLog(PersonHealthCodeLog personHealthCodeLog);

    /**
     * 修改健康码请求
     * 
     * @param personHealthCodeLog 健康码请求
     * @return 结果
     */
    public int updatePersonHealthCodeLog(PersonHealthCodeLog personHealthCodeLog);

    /**
     * 删除健康码请求
     * 
     * @param id 健康码请求ID
     * @return 结果
     */
    public int deletePersonHealthCodeLogById(String id);

    /**
     * 批量删除健康码请求
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deletePersonHealthCodeLogByIds(String[] ids);

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
