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
import cn.eyecool.ocr.domain.OcrHkMacPassLog;
import cn.eyecool.ocr.service.IOcrHkMacPassLogService;

/**
 * 港澳通行证OCR Controller
 * 
 * @author admin
 * @date 2020-12-17
 */
@RestController
@RequestMapping("/ocr/hkmac")
public class OcrHkMacPassLogController extends BaseController {
    @Autowired
    private IOcrHkMacPassLogService ocrHkMacPassLogService;
    @Autowired
    private RedisCache redisCache;

    /**
     * 查询港澳通行证OCR识别记录列表
     */
    @PreAuthorize("@ss.hasPermi('ocr:hkmac:list')")
    @GetMapping("/list")
    public TableDataInfo list(OcrHkMacPassLog ocrHkMacPassLog) {
        startPage();
        List<OcrHkMacPassLog> list = ocrHkMacPassLogService.selectOcrHkMacPassLogList(ocrHkMacPassLog);
        list.parallelStream().filter(l -> StringUtils.isNotEmpty(l.getSceneImageUrl()))
            .forEach(l -> l.setSceneImageUrl(StringUtils
                .isEmpty(PlatformCryptUtils.decryptImageBase64(PlatformFileUtils.getImageBase64(l.getSceneImageUrl())))
                    ? null : OcrConstants.OCR_LOG_IMAGE_PREFIX + PlatformCryptUtils
                        .decryptImageBase64(PlatformFileUtils.getImageBase64(l.getSceneImageUrl()))));
        return getDataTable(list);
    }

    /**
     * 导出港澳通行证OCR识别记录列表
     */
    @PreAuthorize("@ss.hasPermi('ocr:hkmac:export')")
    @Log(title = "港澳通行证OCR识别记录", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(OcrHkMacPassLog ocrHkMacPassLog) {
        String taskId = "Export-" + IdWorker.getNextStringId();
        String tenantId = TenantContextHolder.getTenantId();
        redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
            AjaxResult.success(StringUtils.EMPTY), 12, TimeUnit.HOURS);
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            List<OcrHkMacPassLog> list = ocrHkMacPassLogService.selectOcrHkMacPassLogList(ocrHkMacPassLog);
            ExcelUtil<OcrHkMacPassLog> util = new ExcelUtil<OcrHkMacPassLog>(OcrHkMacPassLog.class);
            AjaxResult result = util.exportExcel(list, "hkmac");
            String fileName = (String)result.get(AjaxResult.MSG_TAG);
            redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
                AjaxResult.success(fileName), 12, TimeUnit.HOURS);
        });
        return AjaxResult.success(taskId);
    }

    /**
     * 获取港澳通行证OCR识别记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('ocr:hkmac:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        OcrHkMacPassLog ocrHkMacPassLog = ocrHkMacPassLogService.selectOcrHkMacPassLogById(id);
        if (StringUtils.isNotEmpty(ocrHkMacPassLog.getSceneImageUrl())) {
            String s = PlatformCryptUtils
                .decryptImageBase64(PlatformFileUtils.getImageBase64(ocrHkMacPassLog.getSceneImageUrl()));
            ocrHkMacPassLog.setSceneImageUrl(StringUtils.isEmpty(s) ? null : OcrConstants.OCR_LOG_IMAGE_PREFIX + s);
        }
        return AjaxResult.success(ocrHkMacPassLog);
    }
}
