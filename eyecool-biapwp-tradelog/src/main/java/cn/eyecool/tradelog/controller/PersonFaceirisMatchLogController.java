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
import cn.eyecool.tradelog.domain.PersonFaceirisMatchLog;
import cn.eyecool.tradelog.domain.PersonHealthCodeLog;
import cn.eyecool.tradelog.service.IPersonFaceirisMatchLogService;
import cn.eyecool.tradelog.service.IPersonHealthCodeLogService;

/**
 * 人脸虹膜多模态比对日志Controller
 * 
 * @author admin
 * @date 2021-12-09
 */
@RestController
@RequestMapping("/tradelog/faceirisMatch")
public class PersonFaceirisMatchLogController extends BaseController {

    @Autowired
    private IPersonFaceirisMatchLogService personFaceirisMatchLogService;
    @Autowired
    private IPersonHealthCodeLogService healthCodeLogService;
    @Autowired
    private RedisCache redisCache;

    /**
     * 查询人脸虹膜多模态比对日志列表
     */
    @PreAuthorize("@ss.hasPermi('tradelog:faceirisMatch:list')")
    @GetMapping("/list")
    public TableDataInfo list(PersonFaceirisMatchLog personFaceirisMatchLog) {
        startPage();
        List<PersonFaceirisMatchLog> list =
            personFaceirisMatchLogService.selectPersonFaceirisMatchLogList(personFaceirisMatchLog);
        return getDataTable(list);
    }

    /**
     * 导出人脸虹膜多模态比对日志列表
     */
    @PreAuthorize("@ss.hasPermi('tradelog:faceirisMatch:export')")
    @Log(title = "tradelog.faceiris.match.title", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(PersonFaceirisMatchLog personFaceirisMatchLog, Boolean exportLatestOnly) {
        String taskId = "Export-" + IdWorker.getNextStringId();
        String tenantId = TenantContextHolder.getTenantId();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
            AjaxResult.success(StringUtils.EMPTY), 12, TimeUnit.HOURS);
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            LoginUserContextHolder.setLoginUser(loginUser);
            List<PersonFaceirisMatchLog> list = Collections.emptyList();
            if (Boolean.TRUE.equals(exportLatestOnly)) {
                list = personFaceirisMatchLogService.selectLastPersonFaceirisMatchLogList(personFaceirisMatchLog);
            } else {
                list = personFaceirisMatchLogService.selectPersonFaceirisMatchLogList(personFaceirisMatchLog);
            }
            ExcelUtil<PersonFaceirisMatchLog> util =
                new ExcelUtil<PersonFaceirisMatchLog>(PersonFaceirisMatchLog.class);
            AjaxResult result = util.exportExcel(list, "faceirisMatch");
            String fileName = (String)result.get(AjaxResult.MSG_TAG);
            redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
                AjaxResult.success(fileName), 12, TimeUnit.HOURS);
        });
        return AjaxResult.success(taskId);
    }

    /**
     * 获取人脸虹膜多模态比对日志详细信息
     */
    @PreAuthorize("@ss.hasPermi('tradelog:faceirisMatch:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        PersonFaceirisMatchLog faceirisMatchLog = personFaceirisMatchLogService.selectPersonFaceirisMatchLogById(id);
        if (StringUtils.isNotBlank(faceirisMatchLog.getSceneFaceImage())) {
            String stringBase64 = PlatformFileUtils.getImageBase64(faceirisMatchLog.getSceneFaceImage());
            if (StringUtils.isNotBlank(stringBase64)) {
                stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                faceirisMatchLog.setSceneFaceImageBase64(stringBase64);
            }
        }
        if (StringUtils.isNotBlank(faceirisMatchLog.getStockFaceImage())) {
            String stringBase64 = PlatformFileUtils.getImageBase64(faceirisMatchLog.getStockFaceImage());
            if (StringUtils.isNotBlank(stringBase64)) {
                stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                faceirisMatchLog.setStockFaceImageBase64(stringBase64);
            }
        }
        if (StringUtils.isNotBlank(faceirisMatchLog.getSceneIrisImage())) {
            String stringBase64 = PlatformFileUtils.getImageBase64(faceirisMatchLog.getSceneIrisImage());
            if (StringUtils.isNotBlank(stringBase64)) {
                stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                faceirisMatchLog.setSceneIrisImageBase64(stringBase64);
            }
        }
        if (StringUtils.isNotBlank(faceirisMatchLog.getStockIrisImage())) {
            String stringBase64 = PlatformFileUtils.getImageBase64(faceirisMatchLog.getStockIrisImage());
            if (StringUtils.isNotBlank(stringBase64)) {
                stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                faceirisMatchLog.setStockIrisImageBase64(stringBase64);
            }
        }
        if (StringUtils.isNotBlank(faceirisMatchLog.getHealthcodeLogId())) {
            PersonHealthCodeLog healthCodeLog =
                healthCodeLogService.selectPersonHealthCodeLogById(faceirisMatchLog.getHealthcodeLogId());
            faceirisMatchLog.setHealthCodeLog(healthCodeLog);
        }
        return AjaxResult.success(faceirisMatchLog);
    }
}
