package cn.eyecool.ocr.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.eyecool.ocr.domain.OcrIdcardBackLog;
import cn.eyecool.ocr.mapper.OcrIdcardBackLogMapper;
import cn.eyecool.ocr.service.IOcrIdcardBackLogService;
import cn.eyecool.common.utils.IdWorker;

/**
 * 身份证背面OCR Service业务层处理
 *
 * @author admin
 * @date 2020-12-17
 */
@Service
public class OcrIdcardBackLogServiceImpl implements IOcrIdcardBackLogService {

    @Autowired
    private OcrIdcardBackLogMapper ocrIdcardBackLogMapper;

    /**
     * 查询身份证背面OCR
     *
     * @param id 身份证背面OCR ID
     * @return 身份证背面OCR
     */
    @Override
    public OcrIdcardBackLog selectOcrIdcardBackLogById(String id) {
        return ocrIdcardBackLogMapper.selectOcrIdcardBackLogById(id);
    }

    /**
     * 查询身份证背面OCR 列表
     *
     * @param ocrIdcardBackLog 身份证背面OCR
     * @return 身份证背面OCR
     */
    @Override
    public List<OcrIdcardBackLog> selectOcrIdcardBackLogList(OcrIdcardBackLog ocrIdcardBackLog) {
        return ocrIdcardBackLogMapper.selectOcrIdcardBackLogList(ocrIdcardBackLog);
    }

    /**
     * 新增身份证背面OCR
     *
     * @param ocrIdcardBackLog 身份证背面OCR
     * @return 结果
     */
    @Override
    public int insertOcrIdcardBackLog(OcrIdcardBackLog ocrIdcardBackLog) {
        ocrIdcardBackLog.setId(IdWorker.getNextStringId());
        return ocrIdcardBackLogMapper.insertOcrIdcardBackLog(ocrIdcardBackLog);
    }

}
