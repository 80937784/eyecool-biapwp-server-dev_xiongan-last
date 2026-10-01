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
import cn.eyecool.common.utils.AESUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.tradelog.domain.PersonFaceirisSearchLog;
import cn.eyecool.tradelog.domain.PersonHealthCodeLog;
import cn.eyecool.tradelog.service.IPersonFaceirisSearchLogService;
import cn.eyecool.tradelog.service.IPersonHealthCodeLogService;

/**
 * 人脸虹膜搜索日志Controller
 * 
 * @author admin
 * @date 2021-05-08
 */
@RestController
@RequestMapping("/tradelog/faceirisSearch")
public class PersonFaceirisSearchLogController extends BaseController {

    @Autowired
    private IPersonFaceirisSearchLogService personFaceirisSearchLogService;
    @Autowired
    private IPersonHealthCodeLogService healthCodeLogService;
    @Autowired
    private RedisCache redisCache;

    /**
     * 查询人脸虹膜搜索日志列表
     */
    @PreAuthorize("@ss.hasPermi('tradelog:faceirisSearch:list')")
    @GetMapping("/list")
    public TableDataInfo list(PersonFaceirisSearchLog personFaceirisSearchLog) {
        startPage();
        List<PersonFaceirisSearchLog> list =
            personFaceirisSearchLogService.selectPersonFaceirisSearchLogList(personFaceirisSearchLog);
        list.stream().forEach(it -> {
            if (StringUtils.isNotBlank(it.getSceneFaceImage())) {
                it.setSceneFaceImageId(AESUtils.encryptAES(it.getSceneFaceImage()));
            }
        });
        return getDataTable(list);
    }

    /**
     * 导出人脸虹膜搜索日志列表
     */
    @PreAuthorize("@ss.hasPermi('tradelog:faceirisSearch:export')")
    @Log(title = "tradelog.faceiris.search.title", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(PersonFaceirisSearchLog personFaceirisSearchLog, Boolean exportLatestOnly) {
        String taskId = "Export-" + IdWorker.getNextStringId();
        String tenantId = TenantContextHolder.getTenantId();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
            AjaxResult.success(StringUtils.EMPTY), 12, TimeUnit.HOURS);
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            LoginUserContextHolder.setLoginUser(loginUser);
            List<PersonFaceirisSearchLog> list = Collections.emptyList();
            if (Boolean.TRUE.equals(exportLatestOnly)) {
                list = personFaceirisSearchLogService.selectLastPersonFaceirisSearchLogList(personFaceirisSearchLog);
            } else {
                list = personFaceirisSearchLogService.selectPersonFaceirisSearchLogList(personFaceirisSearchLog);
            }
            ExcelUtil<PersonFaceirisSearchLog> util =
                new ExcelUtil<PersonFaceirisSearchLog>(PersonFaceirisSearchLog.class);
            AjaxResult result = util.exportExcel(list, "faceirisSearch");
            String fileName = (String)result.get(AjaxResult.MSG_TAG);
            redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
                AjaxResult.success(fileName), 12, TimeUnit.HOURS);
        });
        return AjaxResult.success(taskId);
    }

    /**
     * 获取人脸虹膜搜索日志详细信息
     */
    @PreAuthorize("@ss.hasPermi('tradelog:faceirisSearch:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        PersonFaceirisSearchLog faceirisSearchLog =
            personFaceirisSearchLogService.selectPersonFaceirisSearchLogById(id);
        if (StringUtils.isNotBlank(faceirisSearchLog.getSceneFaceImage())) {
            String stringBase64 = PlatformFileUtils.getImageBase64(faceirisSearchLog.getSceneFaceImage());
            if (StringUtils.isNotBlank(stringBase64)) {
                stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                faceirisSearchLog.setSceneFaceImageBase64(stringBase64);
            }
        }
        if (StringUtils.isNotBlank(faceirisSearchLog.getStockFaceImage())) {
            String stringBase64 = PlatformFileUtils.getImageBase64(faceirisSearchLog.getStockFaceImage());
            if (StringUtils.isNotBlank(stringBase64)) {
                stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                faceirisSearchLog.setStockFaceImageBase64(stringBase64);
            }
        }
        if (StringUtils.isNotBlank(faceirisSearchLog.getSceneIrisImage())) {
            String stringBase64 = PlatformFileUtils.getImageBase64(faceirisSearchLog.getSceneIrisImage());
            if (StringUtils.isNotBlank(stringBase64)) {
                stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                faceirisSearchLog.setSceneIrisImageBase64(stringBase64);
            }
        }
        if (StringUtils.isNotBlank(faceirisSearchLog.getStockIrisImage())) {
            String stringBase64 = PlatformFileUtils.getImageBase64(faceirisSearchLog.getStockIrisImage());
            if (StringUtils.isNotBlank(stringBase64)) {
                stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                faceirisSearchLog.setStockIrisImageBase64(stringBase64);
            }
        }
        if (StringUtils.isNotBlank(faceirisSearchLog.getHealthcodeLogId())) {
            PersonHealthCodeLog healthCodeLog =
                healthCodeLogService.selectPersonHealthCodeLogById(faceirisSearchLog.getHealthcodeLogId());
            faceirisSearchLog.setHealthCodeLog(healthCodeLog);
        }
        return AjaxResult.success(faceirisSearchLog);
    }

}
