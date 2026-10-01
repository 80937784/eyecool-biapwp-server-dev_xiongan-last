package cn.eyecool.tradelog.service;

import java.util.List;
import cn.eyecool.tradelog.domain.TradeReqRecord;

/**
 * 接口交易请求记录Service接口
 * 
 * @author admin
 * @date 2021-05-13
 */
public interface ITradeReqRecordService 
{
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
     * 批量删除接口交易请求记录
     * 
     * @param ids 需要删除的接口交易请求记录ID
     * @return 结果
     */
    public int deleteTradeReqRecordByIds(String[] ids);

    /**
     * 删除接口交易请求记录信息
     * 
     * @param id 接口交易请求记录ID
     * @return 结果
     */
    public int deleteTradeReqRecordById(String id);
}
