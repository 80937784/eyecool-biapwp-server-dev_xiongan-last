package cn.eyecool.ocr.mapper;

import java.util.List;
import cn.eyecool.ocr.domain.OcrPassportLog;

/**
 * 护照OCR  Mapper接口
 * 
 * @author admin
 * @date 2020-12-17
 */
public interface OcrPassportLogMapper 
{
    /**
     * 查询护照OCR  
     * 
     * @param id 护照OCR  ID
     * @return 护照OCR  
     */
    public OcrPassportLog selectOcrPassportLogById(String id);

    /**
     * 查询护照OCR  列表
     * 
     * @param ocrPassportLog 护照OCR  
     * @return 护照OCR  集合
     */
    public List<OcrPassportLog> selectOcrPassportLogList(OcrPassportLog ocrPassportLog);

    /**
     * 新增护照OCR  
     * 
     * @param ocrPassportLog 护照OCR  
     * @return 结果
     */
    public int insertOcrPassportLog(OcrPassportLog ocrPassportLog);

}
