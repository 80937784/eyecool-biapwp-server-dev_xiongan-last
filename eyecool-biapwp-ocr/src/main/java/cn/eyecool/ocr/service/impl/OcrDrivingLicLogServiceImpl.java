package cn.eyecool.ocr.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.eyecool.ocr.domain.OcrDrivingLicLog;
import cn.eyecool.ocr.mapper.OcrDrivingLicLogMapper;
import cn.eyecool.ocr.service.IOcrDrivingLicLogService;
import cn.eyecool.common.utils.IdWorker;

/**
 * 行驶证OCR Service业务层处理
 *
 * @author admin
 * @date 2020-12-17
 */
@Service
public class OcrDrivingLicLogServiceImpl implements IOcrDrivingLicLogService {

    @Autowired
    private OcrDrivingLicLogMapper ocrDrivingLicLogMapper;

    /**
     * 查询行驶证OCR
     *
     * @param id 行驶证OCR ID
     * @return 行驶证OCR
     */
    @Override
    public OcrDrivingLicLog selectOcrDrivingLicLogById(String id) {
        return ocrDrivingLicLogMapper.selectOcrDrivingLicLogById(id);
    }

    /**
     * 查询行驶证OCR 列表
     *
     * @param ocrDrivingLicLog 行驶证OCR
     * @return 行驶证OCR
     */
    @Override
    public List<OcrDrivingLicLog> selectOcrDrivingLicLogList(OcrDrivingLicLog ocrDrivingLicLog) {
        return ocrDrivingLicLogMapper.selectOcrDrivingLicLogList(ocrDrivingLicLog);
    }
    /**
     * 新增行驶证OCR
     *
     * @param ocrDrivingLicLog 行驶证OCR
     * @return 结果
     */
    @Override
    public int insertOcrDrivingLicLog(OcrDrivingLicLog ocrDrivingLicLog) {
        ocrDrivingLicLog.setId(IdWorker.getNextStringId());
        return ocrDrivingLicLogMapper.insertOcrDrivingLicLog(ocrDrivingLicLog);
    }

}
