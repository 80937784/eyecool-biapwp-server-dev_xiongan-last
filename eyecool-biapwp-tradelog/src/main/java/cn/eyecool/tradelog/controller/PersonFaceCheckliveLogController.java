package cn.eyecool.tradelog.controller;

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
import cn.eyecool.tradelog.domain.PersonFaceCheckliveLog;
import cn.eyecool.tradelog.service.IPersonFaceCheckliveLogService;

/**
 * 人员人脸检活日志Controller
 * 
 * @author admin
 * @date 2021-09-02
 */
@RestController
@RequestMapping("/tradelog/checklive")
public class PersonFaceCheckliveLogController extends BaseController {
    @Autowired
    private IPersonFaceCheckliveLogService personFaceCheckliveLogService;
    @Autowired
    private RedisCache redisCache;

    /**
     * 查询人员人脸检活日志列表
     */
    @PreAuthorize("@ss.hasPermi('tradelog:checklive:list')")
    @GetMapping("/list")
    public TableDataInfo list(PersonFaceCheckliveLog personFaceCheckliveLog) {
        startPage();
        List<PersonFaceCheckliveLog> list =
            personFaceCheckliveLogService.selectPersonFaceCheckliveLogList(personFaceCheckliveLog);
        return getDataTable(list);
    }

    /**
     * 导出人员人脸检活日志列表
     */
    @PreAuthorize("@ss.hasPermi('tradelog:checklive:export')")
    @Log(title = "tradelog.face.checklive.title", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(PersonFaceCheckliveLog personFaceCheckliveLog) {
        String taskId = "Export-" + IdWorker.getNextStringId();
        String tenantId = TenantContextHolder.getTenantId();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
            AjaxResult.success(StringUtils.EMPTY), 12, TimeUnit.HOURS);
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            LoginUserContextHolder.setLoginUser(loginUser);
            List<PersonFaceCheckliveLog> list =
                personFaceCheckliveLogService.selectPersonFaceCheckliveLogList(personFaceCheckliveLog);
            ExcelUtil<PersonFaceCheckliveLog> util =
                new ExcelUtil<PersonFaceCheckliveLog>(PersonFaceCheckliveLog.class);
            AjaxResult result = util.exportExcel(list, "checklive");
            String fileName = (String)result.get(AjaxResult.MSG_TAG);
            redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
                AjaxResult.success(fileName), 12, TimeUnit.HOURS);
        });
        return AjaxResult.success(taskId);
    }

    /**
     * 获取人员人脸检活日志详细信息
     */
    @PreAuthorize("@ss.hasPermi('tradelog:checklive:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        PersonFaceCheckliveLog log = personFaceCheckliveLogService.selectPersonFaceCheckliveLogById(id);
        if (StringUtils.isNotBlank(log.getSceneImage())) {
            String stringBase64 = PlatformFileUtils.getImageBase64(log.getSceneImage());
            if (StringUtils.isNotBlank(stringBase64)) {
                stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                log.setSceneImageBase64(stringBase64);
            }
        }
        return AjaxResult.success(log);
    }
}
