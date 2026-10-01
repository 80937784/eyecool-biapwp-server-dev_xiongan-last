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
import cn.eyecool.tradelog.domain.PersonIrisMatchLog;
import cn.eyecool.tradelog.service.IPersonIrisMatchLogService;

/**
 * 虹膜比对日志Controller
 * 
 * @author admin
 * @date 2021-05-07
 */
@RestController
@RequestMapping("/tradelog/irismatch")
public class PersonIrisMatchLogController extends BaseController {
    @Autowired
    private IPersonIrisMatchLogService personIrisMatchLogService;
    @Autowired
    private RedisCache redisCache;

    /**
     * 查询虹膜比对日志列表
     */
    @PreAuthorize("@ss.hasPermi('tradelog:irismatch:list')")
    @GetMapping("/list")
    public TableDataInfo list(PersonIrisMatchLog personIrisMatchLog) {
        startPage();
        List<PersonIrisMatchLog> list = personIrisMatchLogService.selectPersonIrisMatchLogList(personIrisMatchLog);
        return getDataTable(list);
    }

    /**
     * 导出虹膜比对日志列表
     */
    @PreAuthorize("@ss.hasPermi('tradelog:irismatch:export')")
    @Log(title = "tradelog.iris.match.title", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(PersonIrisMatchLog personIrisMatchLog, Boolean exportLatestOnly) {
        String taskId = "Export-" + IdWorker.getNextStringId();
        String tenantId = TenantContextHolder.getTenantId();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
            AjaxResult.success(StringUtils.EMPTY), 12, TimeUnit.HOURS);
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            LoginUserContextHolder.setLoginUser(loginUser);
            List<PersonIrisMatchLog> list = Collections.emptyList();
            if (Boolean.TRUE.equals(exportLatestOnly)) {
                list = personIrisMatchLogService.selectLastPersonIrisMatchLogList(personIrisMatchLog);
            } else {
                list = personIrisMatchLogService.selectPersonIrisMatchLogList(personIrisMatchLog);
            }
            ExcelUtil<PersonIrisMatchLog> util = new ExcelUtil<PersonIrisMatchLog>(PersonIrisMatchLog.class);
            AjaxResult result = util.exportExcel(list, "irismatch");
            String fileName = (String)result.get(AjaxResult.MSG_TAG);
            redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
                AjaxResult.success(fileName), 12, TimeUnit.HOURS);
        });
        return AjaxResult.success(taskId);
    }

    /**
     * 获取虹膜比对日志详细信息
     */
    @PreAuthorize("@ss.hasPermi('tradelog:irismatch:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        PersonIrisMatchLog irisMatchLog = personIrisMatchLogService.selectPersonIrisMatchLogById(id);
        if (StringUtils.isNotBlank(irisMatchLog.getSceneImage())) {
            String stringBase64 = PlatformFileUtils.getImageBase64(irisMatchLog.getSceneImage());
            if (StringUtils.isNotBlank(stringBase64)) {
                stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                irisMatchLog.setSceneImageBase64(stringBase64);
            }
        }
        if (StringUtils.isNotBlank(irisMatchLog.getStockImage())) {
            String stringBase64 = PlatformFileUtils.getImageBase64(irisMatchLog.getStockImage());
            if (StringUtils.isNotBlank(stringBase64)) {
                stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                irisMatchLog.setStockImageBase64(stringBase64);
            }
        }
        return AjaxResult.success(irisMatchLog);
    }
}
