package cn.eyecool.tradelog.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.tradelog.domain.PersonHealthCodeLog;
import cn.eyecool.tradelog.service.IPersonHealthCodeLogService;

/**
 * 健康码请求Controller
 * 
 * @author admin
 * @date 2021-05-13
 */
@RestController
@RequestMapping("/tradelog/healthcode")
public class PersonHealthCodeLogController extends BaseController {
    @Autowired
    private IPersonHealthCodeLogService personHealthCodeLogService;
    @Autowired
    private RedisCache redisCache;

    /**
     * 查询健康码请求列表
     */
    @PreAuthorize("@ss.hasPermi('tradelog:healthcode:list')")
    @GetMapping("/list")
    public TableDataInfo list(PersonHealthCodeLog personHealthCodeLog) {
        startPage();
        List<PersonHealthCodeLog> list = personHealthCodeLogService.selectPersonHealthCodeLogList(personHealthCodeLog);
        return getDataTable(list);
    }

    /**
     * 导出健康码请求列表
     */
    @PreAuthorize("@ss.hasPermi('tradelog:healthcode:export')")
    @Log(title = "tradelog.healthcode.title", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(PersonHealthCodeLog personHealthCodeLog) {
        String taskId = "Export-" + IdWorker.getNextStringId();
        String tenantId = TenantContextHolder.getTenantId();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
            AjaxResult.success(StringUtils.EMPTY), 12, TimeUnit.HOURS);
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            LoginUserContextHolder.setLoginUser(loginUser);
            List<PersonHealthCodeLog> list =
                personHealthCodeLogService.selectPersonHealthCodeLogList(personHealthCodeLog);
            ExcelUtil<PersonHealthCodeLog> util = new ExcelUtil<PersonHealthCodeLog>(PersonHealthCodeLog.class);
            AjaxResult result = util.exportExcel(list, "healthcode");
            String fileName = (String)result.get(AjaxResult.MSG_TAG);
            redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
                AjaxResult.success(fileName), 12, TimeUnit.HOURS);
        });
        return AjaxResult.success(taskId);
    }

    /**
     * 获取健康码请求详细信息
     */
    @PreAuthorize("@ss.hasPermi('tradelog:healthcode:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return AjaxResult.success(personHealthCodeLogService.selectPersonHealthCodeLogById(id));
    }

    /**
     * 新增健康码请求
     */
    @PreAuthorize("@ss.hasPermi('tradelog:healthcode:add')")
    @Log(title = "tradelog.healthcode.title", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PersonHealthCodeLog personHealthCodeLog) {
        return toAjax(personHealthCodeLogService.insertPersonHealthCodeLog(personHealthCodeLog));
    }

    /**
     * 修改健康码请求
     */
    @PreAuthorize("@ss.hasPermi('tradelog:healthcode:edit')")
    @Log(title = "tradelog.healthcode.title", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PersonHealthCodeLog personHealthCodeLog) {
        return toAjax(personHealthCodeLogService.updatePersonHealthCodeLog(personHealthCodeLog));
    }

    /**
     * 删除健康码请求
     */
    @PreAuthorize("@ss.hasPermi('tradelog:healthcode:remove')")
    @Log(title = "tradelog.healthcode.title", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(personHealthCodeLogService.deletePersonHealthCodeLogByIds(ids));
    }
}
