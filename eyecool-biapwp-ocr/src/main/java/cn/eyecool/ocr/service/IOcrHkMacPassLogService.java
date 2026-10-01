package cn.eyecool.ocr.service;

import java.util.List;
import cn.eyecool.ocr.domain.OcrHkMacPassLog;

/**
 * 港澳通行证OCR  Service接口
 * 
 * @author admin
 * @date 2020-12-17
 */
public interface IOcrHkMacPassLogService 
{
    /**
     * 查询港澳通行证OCR  
     * 
     * @param id 港澳通行证OCR  ID
     * @return 港澳通行证OCR  
     */
    public OcrHkMacPassLog selectOcrHkMacPassLogById(String id);

    /**
     * 查询港澳通行证OCR  列表
     * 
     * @param ocrHkMacPassLog 港澳通行证OCR  
     * @return 港澳通行证OCR  集合
     */
    public List<OcrHkMacPassLog> selectOcrHkMacPassLogList(OcrHkMacPassLog ocrHkMacPassLog);
    /**
     * 新增港澳通行证OCR  
     * 
     * @param ocrHkMacPassLog 港澳通行证OCR  
     * @return 结果
     */
    public int insertOcrHkMacPassLog(OcrHkMacPassLog ocrHkMacPassLog);

}
