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
import cn.eyecool.ocr.domain.OcrDriverLicLog;
import cn.eyecool.ocr.service.IOcrDriverLicLogService;

/**
 * 驾驶证ocr Controller
 *
 * @author admin
 * @date 2020-12-17
 */
@RestController
@RequestMapping("/ocr/driverlic")
public class OcrDriverLicLogController extends BaseController {

    @Autowired
    private IOcrDriverLicLogService ocrDriverLicLogService;
    @Autowired
    private RedisCache redisCache;

    /**
     * 查询驾驶证OCR识别记录列表
     */
    @PreAuthorize("@ss.hasPermi('ocr:driverlic:list')")
    @GetMapping("/list")
    public TableDataInfo list(OcrDriverLicLog ocrDriverLicLog) {
        startPage();
        List<OcrDriverLicLog> list = ocrDriverLicLogService.selectOcrDriverLicLogList(ocrDriverLicLog);
        list.stream().filter(l -> StringUtils.isNotEmpty(l.getSceneImageUrl()))
            .forEach(l -> l.setSceneImageUrl(StringUtils
                .isEmpty(PlatformCryptUtils.decryptImageBase64(PlatformFileUtils.getImageBase64(l.getSceneImageUrl())))
                    ? null : OcrConstants.OCR_LOG_IMAGE_PREFIX + PlatformCryptUtils
                        .decryptImageBase64(PlatformFileUtils.getImageBase64(l.getSceneImageUrl()))));
        list.stream().filter(l -> StringUtils.isNotEmpty(l.getDriverLicHeadImage()))
            .forEach(l -> l.setDriverLicHeadImage(StringUtils.isEmpty(
                PlatformCryptUtils.decryptImageBase64(PlatformFileUtils.getImageBase64(l.getDriverLicHeadImage())))
                    ? null : OcrConstants.OCR_LOG_IMAGE_PREFIX + PlatformCryptUtils
                        .decryptImageBase64(PlatformFileUtils.getImageBase64(l.getDriverLicHeadImage()))));
        return getDataTable(list);
    }

    /**
     * 导出驾驶证OCR识别记录列表
     */
    @PreAuthorize("@ss.hasPermi('ocr:driverlic:export')")
    @Log(title = "驾驶证OCR识别记录", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(OcrDriverLicLog ocrDriverLicLog) {
        String taskId = "Export-" + IdWorker.getNextStringId();
        String tenantId = TenantContextHolder.getTenantId();
        redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
            AjaxResult.success(StringUtils.EMPTY), 12, TimeUnit.HOURS);
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            List<OcrDriverLicLog> list = ocrDriverLicLogService.selectOcrDriverLicLogList(ocrDriverLicLog);
            ExcelUtil<OcrDriverLicLog> util = new ExcelUtil<OcrDriverLicLog>(OcrDriverLicLog.class);
            AjaxResult result = util.exportExcel(list, "log");
            String fileName = (String)result.get(AjaxResult.MSG_TAG);
            redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
                AjaxResult.success(fileName), 12, TimeUnit.HOURS);
        });
        return AjaxResult.success(taskId);
    }

    /**
     * 获取驾驶证OCR识别记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('ocr:driverlic:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        OcrDriverLicLog ocrDriverLicLog = ocrDriverLicLogService.selectOcrDriverLicLogById(id);
        if (StringUtils.isNotEmpty(ocrDriverLicLog.getSceneImageUrl())) {
            String s = PlatformCryptUtils
                .decryptImageBase64(PlatformFileUtils.getImageBase64(ocrDriverLicLog.getSceneImageUrl()));
            ocrDriverLicLog.setSceneImageUrl(StringUtils.isEmpty(s) ? null : OcrConstants.OCR_LOG_IMAGE_PREFIX + s);
        }
        if (StringUtils.isNotEmpty(ocrDriverLicLog.getDriverLicHeadImage())) {
            String s = PlatformCryptUtils
                .decryptImageBase64(PlatformFileUtils.getImageBase64(ocrDriverLicLog.getDriverLicHeadImage()));
            ocrDriverLicLog
                .setDriverLicHeadImage(StringUtils.isEmpty(s) ? null : OcrConstants.OCR_LOG_IMAGE_PREFIX + s);
        }
        return AjaxResult.success(ocrDriverLicLog);
    }

}
