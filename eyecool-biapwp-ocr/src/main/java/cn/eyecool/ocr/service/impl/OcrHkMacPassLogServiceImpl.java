package cn.eyecool.ocr.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.eyecool.ocr.domain.OcrHkMacPassLog;
import cn.eyecool.ocr.mapper.OcrHkMacPassLogMapper;
import cn.eyecool.ocr.service.IOcrHkMacPassLogService;
import cn.eyecool.common.utils.IdWorker;

/**
 * 港澳通行证OCR Service业务层处理
 *
 * @author admin
 * @date 2020-12-17
 */
@Service
public class OcrHkMacPassLogServiceImpl implements IOcrHkMacPassLogService {

    @Autowired
    private OcrHkMacPassLogMapper ocrHkMacPassLogMapper;

    /**
     * 查询港澳通行证OCR
     *
     * @param id 港澳通行证OCR ID
     * @return 港澳通行证OCR
     */
    @Override
    public OcrHkMacPassLog selectOcrHkMacPassLogById(String id) {
        return ocrHkMacPassLogMapper.selectOcrHkMacPassLogById(id);
    }

    /**
     * 查询港澳通行证OCR 列表
     *
     * @param ocrHkMacPassLog 港澳通行证OCR
     * @return 港澳通行证OCR
     */
    @Override
    public List<OcrHkMacPassLog> selectOcrHkMacPassLogList(OcrHkMacPassLog ocrHkMacPassLog) {
        return ocrHkMacPassLogMapper.selectOcrHkMacPassLogList(ocrHkMacPassLog);
    }

    /**
     * 新增港澳通行证OCR
     *
     * @param ocrHkMacPassLog 港澳通行证OCR
     * @return 结果
     */
    @Override
    public int insertOcrHkMacPassLog(OcrHkMacPassLog ocrHkMacPassLog) {
        ocrHkMacPassLog.setId(IdWorker.getNextStringId());
        return ocrHkMacPassLogMapper.insertOcrHkMacPassLog(ocrHkMacPassLog);
    }

}
