package cn.eyecool.ocr.service;

import java.util.List;
import cn.eyecool.ocr.domain.OcrIdcardFrontLog;

/**
 * 身份证正面OCR  Service接口
 * 
 * @author admin
 * @date 2020-12-17
 */
public interface IOcrIdcardFrontLogService 
{
    /**
     * 查询身份证正面OCR  
     * 
     * @param id 身份证正面OCR  ID
     * @return 身份证正面OCR  
     */
    public OcrIdcardFrontLog selectOcrIdcardFrontLogById(String id);

    /**
     * 查询身份证正面OCR  列表
     * 
     * @param ocrIdcardFrontLog 身份证正面OCR  
     * @return 身份证正面OCR  集合
     */
    public List<OcrIdcardFrontLog> selectOcrIdcardFrontLogList(OcrIdcardFrontLog ocrIdcardFrontLog);

    /**
     * 新增身份证正面OCR  
     * 
     * @param ocrIdcardFrontLog 身份证正面OCR  
     * @return 结果
     */
    public int insertOcrIdcardFrontLog(OcrIdcardFrontLog ocrIdcardFrontLog);

}
