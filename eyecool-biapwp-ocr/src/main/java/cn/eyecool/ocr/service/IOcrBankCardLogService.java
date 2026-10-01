package cn.eyecool.ocr.service;

import java.util.List;
import cn.eyecool.ocr.domain.OcrBankCardLog;

/**
 * 银行卡OCR  Service接口
 * 
 * @author admin
 * @date 2020-12-17
 */
public interface IOcrBankCardLogService 
{
    /**
     * 查询银行卡OCR  
     * 
     * @param id 银行卡OCR  ID
     * @return 银行卡OCR  
     */
    public OcrBankCardLog selectOcrBankCardLogById(String id);

    /**
     * 查询银行卡OCR  列表
     * 
     * @param ocrBankCardLog 银行卡OCR  
     * @return 银行卡OCR  集合
     */
    public List<OcrBankCardLog> selectOcrBankCardLogList(OcrBankCardLog ocrBankCardLog);
    /**
     * 新增银行卡OCR  
     * 
     * @param ocrBankCardLog 银行卡OCR  
     * @return 结果
     */
    public int insertOcrBankCardLog(OcrBankCardLog ocrBankCardLog);

}
