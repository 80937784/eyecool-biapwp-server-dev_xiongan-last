package cn.eyecool.ocr.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.eyecool.ocr.domain.OcrDriverLicLog;
import cn.eyecool.ocr.mapper.OcrDriverLicLogMapper;
import cn.eyecool.ocr.service.IOcrDriverLicLogService;
import cn.eyecool.common.utils.IdWorker;

/**
 * 驾驶证OCR Service业务层处理
 *
 * @author admin
 * @date 2020-12-17
 */
@Service
public class OcrDriverLicLogServiceImpl implements IOcrDriverLicLogService {

    @Autowired
    private OcrDriverLicLogMapper ocrDriverLicLogMapper;

    /**
     * 查询驾驶证OCR
     *
     * @param id 驾驶证OCR ID
     * @return 驾驶证OCR
     */
    @Override
    public OcrDriverLicLog selectOcrDriverLicLogById(String id) {
        return ocrDriverLicLogMapper.selectOcrDriverLicLogById(id);
    }

    /**
     * 查询驾驶证OCR 列表
     *
     * @param ocrDriverLicLog 驾驶证OCR
     * @return 驾驶证OCR
     */
    @Override
    public List<OcrDriverLicLog> selectOcrDriverLicLogList(OcrDriverLicLog ocrDriverLicLog) {
        return ocrDriverLicLogMapper.selectOcrDriverLicLogList(ocrDriverLicLog);
    }

    /**
     * 新增驾驶证OCR
     *
     * @param ocrDriverLicLog 驾驶证OCR
     * @return 结果
     */
    @Override
    public int insertOcrDriverLicLog(OcrDriverLicLog ocrDriverLicLog) {
        ocrDriverLicLog.setId(IdWorker.getNextStringId());
        return ocrDriverLicLogMapper.insertOcrDriverLicLog(ocrDriverLicLog);
    }
}
