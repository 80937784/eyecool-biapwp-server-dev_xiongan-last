package cn.eyecool.tradelog.controller;

import java.util.Collections;
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
import cn.eyecool.common.context.LoginUserContextHolder;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.model.LoginUser;
import cn.eyecool.common.core.page.TableDataInfo;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.enums.BusinessType;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.tradelog.domain.PersonIrisSearchLog;
import cn.eyecool.tradelog.service.IPersonIrisSearchLogService;

/**
 * 虹膜搜索日志Controller
 * 
 * @author admin
 * @date 2021-05-07
 */
@RestController
@RequestMapping("/tradelog/irissearch")
public class PersonIrisSearchLogController extends BaseController {
    @Autowired
    private IPersonIrisSearchLogService personIrisSearchLogService;
    @Autowired
    private RedisCache redisCache;

    /**
     * 查询虹膜搜索日志列表
     */
    @PreAuthorize("@ss.hasPermi('tradelog:irissearch:list')")
    @GetMapping("/list")
    public TableDataInfo list(PersonIrisSearchLog personIrisSearchLog) {
        startPage();
        List<PersonIrisSearchLog> list = personIrisSearchLogService.selectPersonIrisSearchLogList(personIrisSearchLog);
        return getDataTable(list);
    }

    /**
     * 导出虹膜搜索日志列表
     */
    @PreAuthorize("@ss.hasPermi('tradelog:irissearch:export')")
    @Log(title = "tradelog.iris.search.title", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(PersonIrisSearchLog personIrisSearchLog, Boolean exportLatestOnly) {
        String taskId = "Export-" + IdWorker.getNextStringId();
        String tenantId = TenantContextHolder.getTenantId();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
            AjaxResult.success(StringUtils.EMPTY), 12, TimeUnit.HOURS);
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            LoginUserContextHolder.setLoginUser(loginUser);
            List<PersonIrisSearchLog> list = Collections.emptyList();
            if (Boolean.TRUE.equals(exportLatestOnly)) {
                list = personIrisSearchLogService.selectLastPersonIrisSearchLogList(personIrisSearchLog);
            } else {
                list = personIrisSearchLogService.selectPersonIrisSearchLogList(personIrisSearchLog);
            }
            ExcelUtil<PersonIrisSearchLog> util = new ExcelUtil<PersonIrisSearchLog>(PersonIrisSearchLog.class);
            AjaxResult result = util.exportExcel(list, "irissearch");
            String fileName = (String)result.get(AjaxResult.MSG_TAG);
            redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
                AjaxResult.success(fileName), 12, TimeUnit.HOURS);
        });
        return AjaxResult.success(taskId);
    }

    /**
     * 获取虹膜搜索日志详细信息
     */
    @PreAuthorize("@ss.hasPermi('tradelog:irissearch:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        PersonIrisSearchLog irisSearchLog = personIrisSearchLogService.selectPersonIrisSearchLogById(id);
        if (StringUtils.isNotBlank(irisSearchLog.getSceneImage())) {
            String stringBase64 = PlatformFileUtils.getImageBase64(irisSearchLog.getSceneImage());
            if (StringUtils.isNotBlank(stringBase64)) {
                stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                irisSearchLog.setSceneImageBase64(stringBase64);
            }
        }
        if (StringUtils.isNotBlank(irisSearchLog.getStockImage())) {
            String stringBase64 = PlatformFileUtils.getImageBase64(irisSearchLog.getStockImage());
            if (StringUtils.isNotBlank(stringBase64)) {
                stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                irisSearchLog.setStockImageBase64(stringBase64);
            }
        }
        return AjaxResult.success(irisSearchLog);
    }

}
