package cn.eyecool.tradelog.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.eyecool.tradelog.mapper.TradeReqRecordMapper;
import cn.eyecool.tradelog.domain.TradeReqRecord;
import cn.eyecool.tradelog.service.ITradeReqRecordService;

/**
 * 接口交易请求记录Service业务层处理
 * 
 * @author admin
 * @date 2021-05-13
 */
@Service
public class TradeReqRecordServiceImpl implements ITradeReqRecordService 
{
    @Autowired
    private TradeReqRecordMapper tradeReqRecordMapper;

    /**
     * 查询接口交易请求记录
     * 
     * @param id 接口交易请求记录ID
     * @return 接口交易请求记录
     */
    @Override
    public TradeReqRecord selectTradeReqRecordById(String id)
    {
        return tradeReqRecordMapper.selectTradeReqRecordById(id);
    }

    /**
     * 查询接口交易请求记录列表
     * 
     * @param tradeReqRecord 接口交易请求记录
     * @return 接口交易请求记录
     */
    @Override
    public List<TradeReqRecord> selectTradeReqRecordList(TradeReqRecord tradeReqRecord)
    {
        return tradeReqRecordMapper.selectTradeReqRecordList(tradeReqRecord);
    }

    /**
     * 新增接口交易请求记录
     * 
     * @param tradeReqRecord 接口交易请求记录
     * @return 结果
     */
    @Override
    public int insertTradeReqRecord(TradeReqRecord tradeReqRecord)
    {
        return tradeReqRecordMapper.insertTradeReqRecord(tradeReqRecord);
    }

    /**
     * 修改接口交易请求记录
     * 
     * @param tradeReqRecord 接口交易请求记录
     * @return 结果
     */
    @Override
    public int updateTradeReqRecord(TradeReqRecord tradeReqRecord)
    {
        return tradeReqRecordMapper.updateTradeReqRecord(tradeReqRecord);
    }

    /**
     * 批量删除接口交易请求记录
     * 
     * @param ids 需要删除的接口交易请求记录ID
     * @return 结果
     */
    @Override
    public int deleteTradeReqRecordByIds(String[] ids)
    {
        return tradeReqRecordMapper.deleteTradeReqRecordByIds(ids);
    }

    /**
     * 删除接口交易请求记录信息
     * 
     * @param id 接口交易请求记录ID
     * @return 结果
     */
    @Override
    public int deleteTradeReqRecordById(String id)
    {
        return tradeReqRecordMapper.deleteTradeReqRecordById(id);
    }
}
