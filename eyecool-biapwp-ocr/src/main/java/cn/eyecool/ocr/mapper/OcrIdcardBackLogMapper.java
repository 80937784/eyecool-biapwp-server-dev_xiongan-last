package cn.eyecool.ocr.mapper;

import java.util.List;
import cn.eyecool.ocr.domain.OcrIdcardBackLog;

/**
 * 身份证背面OCR  Mapper接口
 * 
 * @author admin
 * @date 2020-12-17
 */
public interface OcrIdcardBackLogMapper 
{
    /**
     * 查询身份证背面OCR  
     * 
     * @param id 身份证背面OCR  ID
     * @return 身份证背面OCR  
     */
    public OcrIdcardBackLog selectOcrIdcardBackLogById(String id);

    /**
     * 查询身份证背面OCR  列表
     * 
     * @param ocrIdcardBackLog 身份证背面OCR  
     * @return 身份证背面OCR  集合
     */
    public List<OcrIdcardBackLog> selectOcrIdcardBackLogList(OcrIdcardBackLog ocrIdcardBackLog);

    /**
     * 新增身份证背面OCR  
     * 
     * @param ocrIdcardBackLog 身份证背面OCR  
     * @return 结果
     */
    public int insertOcrIdcardBackLog(OcrIdcardBackLog ocrIdcardBackLog);

}
