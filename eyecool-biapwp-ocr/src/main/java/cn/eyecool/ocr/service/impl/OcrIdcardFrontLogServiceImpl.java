package cn.eyecool.ocr.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.eyecool.ocr.domain.OcrIdcardFrontLog;
import cn.eyecool.ocr.mapper.OcrIdcardFrontLogMapper;
import cn.eyecool.ocr.service.IOcrIdcardFrontLogService;
import cn.eyecool.common.utils.IdWorker;

/**
 * 身份证正面OCR Service业务层处理
 *
 * @author admin
 * @date 2020-12-17
 */
@Service
public class OcrIdcardFrontLogServiceImpl implements IOcrIdcardFrontLogService {

    @Autowired
    private OcrIdcardFrontLogMapper ocrIdcardFrontLogMapper;

    /**
     * 查询身份证正面OCR
     *
     * @param id 身份证正面OCR ID
     * @return 身份证正面OCR
     */
    @Override
    public OcrIdcardFrontLog selectOcrIdcardFrontLogById(String id) {
        return ocrIdcardFrontLogMapper.selectOcrIdcardFrontLogById(id);
    }

    /**
     * 查询身份证正面OCR 列表
     *
     * @param ocrIdcardFrontLog 身份证正面OCR
     * @return 身份证正面OCR
     */
    @Override
    public List<OcrIdcardFrontLog> selectOcrIdcardFrontLogList(OcrIdcardFrontLog ocrIdcardFrontLog) {
        return ocrIdcardFrontLogMapper.selectOcrIdcardFrontLogList(ocrIdcardFrontLog);
    }
    /**
     * 新增身份证正面OCR
     *
     * @param ocrIdcardFrontLog 身份证正面OCR
     * @return 结果
     */
    @Override
    public int insertOcrIdcardFrontLog(OcrIdcardFrontLog ocrIdcardFrontLog) {
        ocrIdcardFrontLog.setId(IdWorker.getNextStringId());
        return ocrIdcardFrontLogMapper.insertOcrIdcardFrontLog(ocrIdcardFrontLog);
    }

}
