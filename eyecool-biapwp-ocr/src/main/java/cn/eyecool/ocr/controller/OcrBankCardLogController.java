package cn.eyecool.ocr.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.eyecool.common.annotation.Log;
import cn.eyecool.common.constant.Constants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.page.TableDataInfo;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.enums.BusinessType;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.ocr.OcrConstants;
import cn.eyecool.ocr.domain.OcrBankCardLog;
import cn.eyecool.ocr.service.IOcrBankCardLogService;

/**
 * 银行卡ocr Controller
 *
 * @author admin
 * @date 2020-12-17
 */
@RestController
@RequestMapping("/ocr/bankcard")
public class OcrBankCardLogController extends BaseController {

    @Autowired
    private IOcrBankCardLogService ocrBankCardLogService;
    @Autowired
    private RedisCache redisCache;

    /**
     * 查询银行卡OCR识别记录列表
     */
    @PreAuthorize("@ss.hasPermi('ocr:bankcard:list')")
    @GetMapping("/list")
    public TableDataInfo list(OcrBankCardLog ocrBankCardLog) {
        startPage();
        List<OcrBankCardLog> list = ocrBankCardLogService.selectOcrBankCardLogList(ocrBankCardLog);
        list.stream().filter(l -> StringUtils.isNotBlank(l.getSceneImageUrl()))
            .forEach(l -> l.setSceneImageUrl(OcrConstants.OCR_LOG_IMAGE_PREFIX
                + PlatformCryptUtils.decryptImageBase64(PlatformFileUtils.getImageBase64(l.getSceneImageUrl()))));
        return getDataTable(list);
    }

    /**
     * 导出银行卡OCR识别记录列表
     */
    @PreAuthorize("@ss.hasPermi('ocr:bankcard:export')")
    @Log(title = "银行卡OCR识别记录", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(OcrBankCardLog ocrBankCardLog) {
        String taskId = "Export-" + IdWorker.getNextStringId();
        String tenantId = TenantContextHolder.getTenantId();
        redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
            AjaxResult.success(StringUtils.EMPTY), 12, TimeUnit.HOURS);
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            List<OcrBankCardLog> list = ocrBankCardLogService.selectOcrBankCardLogList(ocrBankCardLog);
            ExcelUtil<OcrBankCardLog> util = new ExcelUtil<OcrBankCardLog>(OcrBankCardLog.class);
            AjaxResult result = util.exportExcel(list, "log");
            String fileName = (String)result.get(AjaxResult.MSG_TAG);
            redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
                AjaxResult.success(fileName), 12, TimeUnit.HOURS);
        });
        return AjaxResult.success(taskId);
    }

    /**
     * 获取银行卡OCR识别记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('ocr:bankcard:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        OcrBankCardLog ocrBankCardLog = ocrBankCardLogService.selectOcrBankCardLogById(id);
        if (StringUtils.isNotBlank(ocrBankCardLog.getSceneImageUrl())) {
            ocrBankCardLog.setSceneImageUrl(OcrConstants.OCR_LOG_IMAGE_PREFIX + PlatformCryptUtils
                .decryptImageBase64(PlatformFileUtils.getImageBase64(ocrBankCardLog.getSceneImageUrl())));
        }
        return AjaxResult.success(ocrBankCardLog);
    }
}
