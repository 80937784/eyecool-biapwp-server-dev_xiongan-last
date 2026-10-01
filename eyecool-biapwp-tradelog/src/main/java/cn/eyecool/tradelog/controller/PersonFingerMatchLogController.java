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
import cn.eyecool.tradelog.domain.PersonFingerMatchLog;
import cn.eyecool.tradelog.service.IPersonFingerMatchLogService;

/**
 * 指纹比对日志Controller
 * 
 * @author admin
 * @date 2021-05-06
 */
@RestController
@RequestMapping("/tradelog/fingermatch")
public class PersonFingerMatchLogController extends BaseController {
    @Autowired
    private IPersonFingerMatchLogService personFingerMatchLogService;
    @Autowired
    private RedisCache redisCache;

    /**
     * 查询指纹比对日志列表
     */
    @PreAuthorize("@ss.hasPermi('tradelog:fingermatch:list')")
    @GetMapping("/list")
    public TableDataInfo list(PersonFingerMatchLog personFingerMatchLog) {
        startPage();
        List<PersonFingerMatchLog> list =
            personFingerMatchLogService.selectPersonFingerMatchLogList(personFingerMatchLog);
        return getDataTable(list);
    }

    /**
     * 导出指纹比对日志列表
     */
    @PreAuthorize("@ss.hasPermi('tradelog:fingermatch:export')")
    @Log(title = "tradelog.finger.match.title", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(PersonFingerMatchLog personFingerMatchLog, Boolean exportLatestOnly) {
        String taskId = "Export-" + IdWorker.getNextStringId();
        String tenantId = TenantContextHolder.getTenantId();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
            AjaxResult.success(StringUtils.EMPTY), 12, TimeUnit.HOURS);
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            LoginUserContextHolder.setLoginUser(loginUser);
            List<PersonFingerMatchLog> list = Collections.emptyList();
            if (Boolean.TRUE.equals(exportLatestOnly)) {
                list = personFingerMatchLogService.selectLastPersonFingerMatchLogList(personFingerMatchLog);
            } else {
                list = personFingerMatchLogService.selectPersonFingerMatchLogList(personFingerMatchLog);
            }
            ExcelUtil<PersonFingerMatchLog> util = new ExcelUtil<PersonFingerMatchLog>(PersonFingerMatchLog.class);
            AjaxResult result = util.exportExcel(list, "fingermatch");
            String fileName = (String)result.get(AjaxResult.MSG_TAG);
            redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
                AjaxResult.success(fileName), 12, TimeUnit.HOURS);
        });
        return AjaxResult.success(taskId);
    }

    /**
     * 获取指纹比对日志详细信息
     */
    @PreAuthorize("@ss.hasPermi('tradelog:fingermatch:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        PersonFingerMatchLog fingerMatchLog = personFingerMatchLogService.selectPersonFingerMatchLogById(id);
        if (StringUtils.isNotBlank(fingerMatchLog.getSceneImage())) {
            String stringBase64 = PlatformFileUtils.getImageBase64(fingerMatchLog.getSceneImage());
            if (StringUtils.isNotBlank(stringBase64)) {
                stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                fingerMatchLog.setSceneImageBase64(stringBase64);
            }
        }
        if (StringUtils.isNotBlank(fingerMatchLog.getStockImage())) {
            String stringBase64 = PlatformFileUtils.getImageBase64(fingerMatchLog.getStockImage());
            if (StringUtils.isNotBlank(stringBase64)) {
                stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                fingerMatchLog.setStockImageBase64(stringBase64);
            }
        }
        return AjaxResult.success(fingerMatchLog);
    }
}
