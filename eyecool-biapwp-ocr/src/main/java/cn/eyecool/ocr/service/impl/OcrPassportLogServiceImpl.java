package cn.eyecool.ocr.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.eyecool.ocr.domain.OcrPassportLog;
import cn.eyecool.ocr.mapper.OcrPassportLogMapper;
import cn.eyecool.ocr.service.IOcrPassportLogService;
import cn.eyecool.common.utils.IdWorker;

/**
 * 护照OCR Service业务层处理
 *
 * @author admin
 * @date 2020-12-17
 */
@Service
public class OcrPassportLogServiceImpl implements IOcrPassportLogService {

    @Autowired
    private OcrPassportLogMapper ocrPassportLogMapper;

    /**
     * 查询护照OCR
     *
     * @param id 护照OCR ID
     * @return 护照OCR
     */
    @Override
    public OcrPassportLog selectOcrPassportLogById(String id) {
        return ocrPassportLogMapper.selectOcrPassportLogById(id);
    }

    /**
     * 查询护照OCR 列表
     *
     * @param ocrPassportLog 护照OCR
     * @return 护照OCR
     */
    @Override
    public List<OcrPassportLog> selectOcrPassportLogList(OcrPassportLog ocrPassportLog) {
        return ocrPassportLogMapper.selectOcrPassportLogList(ocrPassportLog);
    }
    /**
     * 新增护照OCR
     *
     * @param ocrPassportLog 护照OCR
     * @return 结果
     */
    @Override
    public int insertOcrPassportLog(OcrPassportLog ocrPassportLog) {
        ocrPassportLog.setId(IdWorker.getNextStringId());
        return ocrPassportLogMapper.insertOcrPassportLog(ocrPassportLog);
    }

}
