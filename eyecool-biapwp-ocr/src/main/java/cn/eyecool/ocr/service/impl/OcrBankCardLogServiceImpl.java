package cn.eyecool.ocr.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.ocr.domain.OcrBankCardLog;
import cn.eyecool.ocr.mapper.OcrBankCardLogMapper;
import cn.eyecool.ocr.service.IOcrBankCardLogService;

/**
 * 银行卡OCR Service业务层处理
 *
 * @author admin
 * @date 2020-12-17
 */
@Service
public class OcrBankCardLogServiceImpl implements IOcrBankCardLogService {
    @Autowired
    private OcrBankCardLogMapper ocrBankCardLogMapper;

    /**
     * 查询银行卡OCR
     *
     * @param id 银行卡OCR ID
     * @return 银行卡OCR
     */
    @Override
    public OcrBankCardLog selectOcrBankCardLogById(String id) {
        return ocrBankCardLogMapper.selectOcrBankCardLogById(id);
    }

    /**
     * 查询银行卡OCR 列表
     *
     * @param ocrBankCardLog 银行卡OCR
     * @return 银行卡OCR
     */
    @Override
    public List<OcrBankCardLog> selectOcrBankCardLogList(OcrBankCardLog ocrBankCardLog) {
        return ocrBankCardLogMapper.selectOcrBankCardLogList(ocrBankCardLog);
    }

    /**
     * 新增银行卡OCR
     *
     * @param ocrBankCardLog 银行卡OCR
     * @return 结果
     */
    @Override
    public int insertOcrBankCardLog(OcrBankCardLog ocrBankCardLog) {
        ocrBankCardLog.setId(IdWorker.getNextStringId());
        return ocrBankCardLogMapper.insertOcrBankCardLog(ocrBankCardLog);
    }

}
