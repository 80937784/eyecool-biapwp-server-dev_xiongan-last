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
import cn.eyecool.ocr.domain.OcrIdcardFrontLog;
import cn.eyecool.ocr.service.IOcrIdcardFrontLogService;

/**
 * 身份证正面OCR Controller
 *
 * @author admin
 * @date 2020-12-17
 */
@RestController
@RequestMapping("/ocr/idcardfront")
public class OcrIdcardFrontLogController extends BaseController {

    @Autowired
    private IOcrIdcardFrontLogService ocrIdcardFrontLogService;
    @Autowired
    private RedisCache redisCache;

    /**
     * 查询身份证正面OCR识别记录列表
     */
    @PreAuthorize("@ss.hasPermi('ocr:idcardfront:list')")
    @GetMapping("/list")
    public TableDataInfo list(OcrIdcardFrontLog ocrIdcardFrontLog) {
        startPage();
        List<OcrIdcardFrontLog> list = ocrIdcardFrontLogService.selectOcrIdcardFrontLogList(ocrIdcardFrontLog);
        list.parallelStream().filter(l -> StringUtils.isNotEmpty(l.getSceneImageUrl()))
            .forEach(l -> l.setSceneImageUrl(StringUtils
                .isEmpty(PlatformCryptUtils.decryptImageBase64(PlatformFileUtils.getImageBase64(l.getSceneImageUrl())))
                    ? null : OcrConstants.OCR_LOG_IMAGE_PREFIX + PlatformCryptUtils
                        .decryptImageBase64(PlatformFileUtils.getImageBase64(l.getSceneImageUrl()))));
        list.parallelStream().filter(l -> StringUtils.isNotEmpty(l.getIdcardHeadimage()))
            .forEach(l -> l.setIdcardHeadimage(StringUtils.isEmpty(
                PlatformCryptUtils.decryptImageBase64(PlatformFileUtils.getImageBase64(l.getIdcardHeadimage()))) ? null
                    : OcrConstants.OCR_LOG_IMAGE_PREFIX + PlatformCryptUtils
                        .decryptImageBase64(PlatformFileUtils.getImageBase64(l.getIdcardHeadimage()))));
        return getDataTable(list);
    }

    /**
     * 导出身份证正面OCR识别记录列表
     */
    @PreAuthorize("@ss.hasPermi('ocr:idcardfront:export')")
    @Log(title = "身份证正面OCR识别记录", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(OcrIdcardFrontLog ocrIdcardFrontLog) {
        String taskId = "Export-" + IdWorker.getNextStringId();
        String tenantId = TenantContextHolder.getTenantId();
        redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
            AjaxResult.success(StringUtils.EMPTY), 12, TimeUnit.HOURS);
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            List<OcrIdcardFrontLog> list = ocrIdcardFrontLogService.selectOcrIdcardFrontLogList(ocrIdcardFrontLog);
            ExcelUtil<OcrIdcardFrontLog> util = new ExcelUtil<OcrIdcardFrontLog>(OcrIdcardFrontLog.class);
            AjaxResult result = util.exportExcel(list, "log");
            String fileName = (String)result.get(AjaxResult.MSG_TAG);
            redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
                AjaxResult.success(fileName), 12, TimeUnit.HOURS);
        });
        return AjaxResult.success(taskId);
    }

    /**
     * 获取身份证正面OCR识别记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('ocr:idcardfront:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        OcrIdcardFrontLog ocrIdcardFrontLog = ocrIdcardFrontLogService.selectOcrIdcardFrontLogById(id);
        if (StringUtils.isNotEmpty(ocrIdcardFrontLog.getIdcardHeadimage())) {
            String s = PlatformCryptUtils
                .decryptImageBase64(PlatformFileUtils.getImageBase64(ocrIdcardFrontLog.getIdcardHeadimage()));
            ocrIdcardFrontLog.setIdcardHeadimage(StringUtils.isEmpty(s) ? null : OcrConstants.OCR_LOG_IMAGE_PREFIX + s);
        }
        if (StringUtils.isNotEmpty(ocrIdcardFrontLog.getSceneImageUrl())) {
            String s = PlatformCryptUtils
                .decryptImageBase64(PlatformFileUtils.getImageBase64(ocrIdcardFrontLog.getSceneImageUrl()));
            ocrIdcardFrontLog.setSceneImageUrl(StringUtils.isEmpty(s) ? null : OcrConstants.OCR_LOG_IMAGE_PREFIX + s);
        }
        return AjaxResult.success(ocrIdcardFrontLog);
    }

}
