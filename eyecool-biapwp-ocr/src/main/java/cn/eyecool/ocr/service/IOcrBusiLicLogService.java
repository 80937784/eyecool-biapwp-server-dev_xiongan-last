package cn.eyecool.ocr.service;

import java.util.List;
import cn.eyecool.ocr.domain.OcrBusiLicLog;

/**
 * 营业执照OCR  Service接口
 * 
 * @author admin
 * @date 2020-12-17
 */
public interface IOcrBusiLicLogService
{
    /**
     * 查询营业执照OCR  
     * 
     * @param id 营业执照OCR  ID
     * @return 营业执照OCR  
     */
    public OcrBusiLicLog selectOcrBusiLicLogById(String id);

    /**
     * 查询营业执照OCR  列表
     * 
     * @param ocrBusinessLicLog 营业执照OCR  
     * @return 营业执照OCR  集合
     */
    public List<OcrBusiLicLog> selectOcrBusiLicLogList(OcrBusiLicLog ocrBusinessLicLog);
    /**
     * 新增营业执照OCR  
     * 
     * @param ocrBusinessLicLog 营业执照OCR  
     * @return 结果
     */
    public int insertOcrBusiLicLog(OcrBusiLicLog ocrBusinessLicLog);

}
