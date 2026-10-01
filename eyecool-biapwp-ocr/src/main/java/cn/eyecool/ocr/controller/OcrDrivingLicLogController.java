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
import cn.eyecool.ocr.domain.OcrDrivingLicLog;
import cn.eyecool.ocr.service.IOcrDrivingLicLogService;

/**
 * 行驶证OCR Controller
 * 
 * @author admin
 * @date 2020-12-17
 */
@RestController
@RequestMapping("/ocr/drivinglic")
public class OcrDrivingLicLogController extends BaseController {

    @Autowired
    private IOcrDrivingLicLogService ocrDrivingLicLogService;
    @Autowired
    private RedisCache redisCache;

    /**
     * 查询行驶证OCR识别记录列表
     */
    @PreAuthorize("@ss.hasPermi('ocr:drivinglic:list')")
    @GetMapping("/list")
    public TableDataInfo list(OcrDrivingLicLog ocrDrivingLicLog) {
        startPage();
        List<OcrDrivingLicLog> list = ocrDrivingLicLogService.selectOcrDrivingLicLogList(ocrDrivingLicLog);
        list.parallelStream().filter(l -> StringUtils.isNotEmpty(l.getSceneImageUrl()))
            .forEach(l -> l.setSceneImageUrl(StringUtils
                .isEmpty(PlatformCryptUtils.decryptImageBase64(PlatformFileUtils.getImageBase64(l.getSceneImageUrl())))
                    ? null : OcrConstants.OCR_LOG_IMAGE_PREFIX + PlatformCryptUtils
                        .decryptImageBase64(PlatformFileUtils.getImageBase64(l.getSceneImageUrl()))));
        return getDataTable(list);
    }

    /**
     * 导出行驶证OCR识别记录列表
     */
    @PreAuthorize("@ss.hasPermi('ocr:drivinglic:export')")
    @Log(title = "行驶证OCR识别记录", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(OcrDrivingLicLog ocrDrivingLicLog) {
        String taskId = "Export-" + IdWorker.getNextStringId();
        String tenantId = TenantContextHolder.getTenantId();
        redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
            AjaxResult.success(StringUtils.EMPTY), 12, TimeUnit.HOURS);
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            List<OcrDrivingLicLog> list = ocrDrivingLicLogService.selectOcrDrivingLicLogList(ocrDrivingLicLog);
            ExcelUtil<OcrDrivingLicLog> util = new ExcelUtil<OcrDrivingLicLog>(OcrDrivingLicLog.class);
            AjaxResult result = util.exportExcel(list, "drivinglic");
            String fileName = (String)result.get(AjaxResult.MSG_TAG);
            redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
                AjaxResult.success(fileName), 12, TimeUnit.HOURS);
        });
        return AjaxResult.success(taskId);
    }

    /**
     * 获取行驶证OCR识别记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('ocr:drivinglic:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        OcrDrivingLicLog ocrDrivingLicLog = ocrDrivingLicLogService.selectOcrDrivingLicLogById(id);
        String s = PlatformCryptUtils
            .decryptImageBase64(PlatformFileUtils.getImageBase64(ocrDrivingLicLog.getSceneImageUrl()));
        ocrDrivingLicLog.setSceneImageUrl(StringUtils.isEmpty(s) ? null : OcrConstants.OCR_LOG_IMAGE_PREFIX + s);
        return AjaxResult.success(ocrDrivingLicLog);
    }
}
