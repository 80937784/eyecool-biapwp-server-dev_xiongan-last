package cn.eyecool.ocr.service;

import java.util.List;
import cn.eyecool.ocr.domain.OcrDriverLicLog;

/**
 * 驾驶证OCR  Service接口
 * 
 * @author admin
 * @date 2020-12-17
 */
public interface IOcrDriverLicLogService 
{
    /**
     * 查询驾驶证OCR  
     * 
     * @param id 驾驶证OCR  ID
     * @return 驾驶证OCR  
     */
    public OcrDriverLicLog selectOcrDriverLicLogById(String id);

    /**
     * 查询驾驶证OCR  列表
     * 
     * @param ocrDriverLicLog 驾驶证OCR  
     * @return 驾驶证OCR  集合
     */
    public List<OcrDriverLicLog> selectOcrDriverLicLogList(OcrDriverLicLog ocrDriverLicLog);
    /**
     * 新增驾驶证OCR  
     * 
     * @param ocrDriverLicLog 驾驶证OCR  
     * @return 结果
     */
    public int insertOcrDriverLicLog(OcrDriverLicLog ocrDriverLicLog);

}
