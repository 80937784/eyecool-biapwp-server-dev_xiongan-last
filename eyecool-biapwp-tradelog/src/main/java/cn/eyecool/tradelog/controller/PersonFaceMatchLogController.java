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
import cn.eyecool.tradelog.domain.PersonFaceMatchLog;
import cn.eyecool.tradelog.domain.PersonHealthCodeLog;
import cn.eyecool.tradelog.service.IPersonFaceMatchLogService;
import cn.eyecool.tradelog.service.IPersonHealthCodeLogService;

/**
 * 人脸比对日志Controller
 * 
 * @author admin
 * @date 2021-04-29
 */
@RestController
@RequestMapping("/tradelog/facematch")
public class PersonFaceMatchLogController extends BaseController {

    @Autowired
    private IPersonFaceMatchLogService personFaceMatchLogService;
    @Autowired
    private IPersonHealthCodeLogService healthCodeLogService;
    @Autowired
    private RedisCache redisCache;

    /**
     * 查询人脸比对日志列表
     */
    @PreAuthorize("@ss.hasPermi('tradelog:facematch:list')")
    @GetMapping("/list")
    public TableDataInfo list(PersonFaceMatchLog personFaceMatchLog) {
        startPage();
        List<PersonFaceMatchLog> list = personFaceMatchLogService.selectPersonFaceMatchLogList(personFaceMatchLog);
        list.stream().forEach(it -> {
            if (StringUtils.isNotBlank(it.getSceneImage())) {
                it.setSceneImageId(AESUtils.encryptAES(it.getSceneImage()));
            }
        });
        return getDataTable(list);
    }

    /**
     * 导出人脸比对日志列表
     */
    @PreAuthorize("@ss.hasPermi('tradelog:facematch:export')")
    @Log(title = "tradelog.face.match.title", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(PersonFaceMatchLog personFaceMatchLog, Boolean exportLatestOnly) {
        String taskId = "Export-" + IdWorker.getNextStringId();
        String tenantId = TenantContextHolder.getTenantId();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
            AjaxResult.success(StringUtils.EMPTY), 12, TimeUnit.HOURS);
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            LoginUserContextHolder.setLoginUser(loginUser);
            List<PersonFaceMatchLog> list = Collections.emptyList();
            if (Boolean.TRUE.equals(exportLatestOnly)) {
                list = personFaceMatchLogService.selectLastPersonFaceMatchLogList(personFaceMatchLog);
            } else {
                list = personFaceMatchLogService.selectPersonFaceMatchLogList(personFaceMatchLog);
            }
            ExcelUtil<PersonFaceMatchLog> util = new ExcelUtil<PersonFaceMatchLog>(PersonFaceMatchLog.class);
            AjaxResult result = util.exportExcel(list, "facematch");
            String fileName = (String)result.get(AjaxResult.MSG_TAG);
            redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
                AjaxResult.success(fileName), 12, TimeUnit.HOURS);
        });
        return AjaxResult.success(taskId);
    }

    /**
     * 获取人脸比对日志详细信息
     */
    @PreAuthorize("@ss.hasPermi('tradelog:facematch:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        PersonFaceMatchLog faceMatchLog = personFaceMatchLogService.selectPersonFaceMatchLogById(id);
        if (StringUtils.isNotBlank(faceMatchLog.getChipImage())) {
            String stringBase64 = PlatformFileUtils.getImageBase64(faceMatchLog.getChipImage());
            if (StringUtils.isNotBlank(stringBase64)) {
                stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                faceMatchLog.setChipImageBase64(stringBase64);
            }
        }
        if (StringUtils.isNotBlank(faceMatchLog.getOnlineImage())) {
            String stringBase64 = PlatformFileUtils.getImageBase64(faceMatchLog.getOnlineImage());
            if (StringUtils.isNotBlank(stringBase64)) {
                stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                faceMatchLog.setOnlineImageBase64(stringBase64);
            }
        }
        if (StringUtils.isNotBlank(faceMatchLog.getSceneImage())) {
            String stringBase64 = PlatformFileUtils.getImageBase64(faceMatchLog.getSceneImage());
            if (StringUtils.isNotBlank(stringBase64)) {
                stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                faceMatchLog.setSceneImageBase64(stringBase64);
            }
        }
        if (StringUtils.isNotBlank(faceMatchLog.getStockImage())) {
            String stringBase64 = PlatformFileUtils.getImageBase64(faceMatchLog.getStockImage());
            if (StringUtils.isNotBlank(stringBase64)) {
                stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                faceMatchLog.setStockImageBase64(stringBase64);
            }
        }
        if (StringUtils.isNotBlank(faceMatchLog.getHealthcodeLogId())) {
            PersonHealthCodeLog healthCodeLog =
                healthCodeLogService.selectPersonHealthCodeLogById(faceMatchLog.getHealthcodeLogId());
            faceMatchLog.setHealthCodeLog(healthCodeLog);
        }
        return AjaxResult.success(faceMatchLog);
    }

}
