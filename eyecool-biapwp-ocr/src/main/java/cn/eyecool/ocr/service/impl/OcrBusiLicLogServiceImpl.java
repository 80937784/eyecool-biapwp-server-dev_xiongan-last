package cn.eyecool.ocr.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.eyecool.ocr.domain.OcrBusiLicLog;
import cn.eyecool.ocr.mapper.OcrBusiLicLogMapper;
import cn.eyecool.ocr.service.IOcrBusiLicLogService;
import cn.eyecool.common.utils.IdWorker;

/**
 * 营业执照OCR Service业务层处理
 *
 * @author admin
 * @date 2020-12-17
 */
@Service
public class OcrBusiLicLogServiceImpl implements IOcrBusiLicLogService {

    @Autowired
    private OcrBusiLicLogMapper ocrBusinessLicLogMapper;

    /**
     * 查询营业执照OCR
     *
     * @param id 营业执照OCR ID
     * @return 营业执照OCR
     */
    @Override
    public OcrBusiLicLog selectOcrBusiLicLogById(String id) {
        return ocrBusinessLicLogMapper.selectOcrBusiLicLogById(id);
    }

    /**
     * 查询营业执照OCR 列表
     *
     * @param ocrBusinessLicLog 营业执照OCR
     * @return 营业执照OCR
     */
    @Override
    public List<OcrBusiLicLog> selectOcrBusiLicLogList(OcrBusiLicLog ocrBusinessLicLog) {
        return ocrBusinessLicLogMapper.selectOcrBusiLicLogList(ocrBusinessLicLog);
    }
    /**
     * 新增营业执照OCR
     *
     * @param ocrBusinessLicLog 营业执照OCR
     * @return 结果
     */
    @Override
    public int insertOcrBusiLicLog(OcrBusiLicLog ocrBusinessLicLog) {
        ocrBusinessLicLog.setId(IdWorker.getNextStringId());
        return ocrBusinessLicLogMapper.insertOcrBusiLicLog(ocrBusinessLicLog);
    }

}
