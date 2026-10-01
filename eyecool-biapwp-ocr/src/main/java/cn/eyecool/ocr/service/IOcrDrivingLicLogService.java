package cn.eyecool.ocr.service;

import java.util.List;
import cn.eyecool.ocr.domain.OcrDrivingLicLog;

/**
 * 行驶证OCR  Service接口
 * 
 * @author admin
 * @date 2020-12-17
 */
public interface IOcrDrivingLicLogService 
{
    /**
     * 查询行驶证OCR  
     * 
     * @param id 行驶证OCR  ID
     * @return 行驶证OCR  
     */
    public OcrDrivingLicLog selectOcrDrivingLicLogById(String id);

    /**
     * 查询行驶证OCR  列表
     * 
     * @param ocrDrivingLicLog 行驶证OCR  
     * @return 行驶证OCR  集合
     */
    public List<OcrDrivingLicLog> selectOcrDrivingLicLogList(OcrDrivingLicLog ocrDrivingLicLog);

    /**
     * 新增行驶证OCR  
     * 
     * @param ocrDrivingLicLog 行驶证OCR  
     * @return 结果
     */
    public int insertOcrDrivingLicLog(OcrDrivingLicLog ocrDrivingLicLog);

}
