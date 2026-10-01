package cn.eyecool.tradelog.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.annotation.SqlParser;

import cn.eyecool.tradelog.domain.TradeReqRecord;

/**
 * 接口交易请求记录Mapper接口
 * 
 * @author admin
 * @date 2021-05-13
 */
@SuppressWarnings("deprecation")
public interface TradeReqRecordMapper {
    /**
     * 查询接口交易请求记录
     * 
     * @param id 接口交易请求记录ID
     * @return 接口交易请求记录
     */
    public TradeReqRecord selectTradeReqRecordById(String id);

    /**
     * 查询接口交易请求记录列表
     * 
     * @param tradeReqRecord 接口交易请求记录
     * @return 接口交易请求记录集合
     */
    public List<TradeReqRecord> selectTradeReqRecordList(TradeReqRecord tradeReqRecord);

    /**
     * 新增接口交易请求记录
     * 
     * @param tradeReqRecord 接口交易请求记录
     * @return 结果
     */
    public int insertTradeReqRecord(TradeReqRecord tradeReqRecord);

    /**
     * 修改接口交易请求记录
     * 
     * @param tradeReqRecord 接口交易请求记录
     * @return 结果
     */
    public int updateTradeReqRecord(TradeReqRecord tradeReqRecord);

    /**
     * 删除接口交易请求记录
     * 
     * @param id 接口交易请求记录ID
     * @return 结果
     */
    public int deleteTradeReqRecordById(String id);

    /**
     * 批量删除接口交易请求记录
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteTradeReqRecordByIds(String[] ids);

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
