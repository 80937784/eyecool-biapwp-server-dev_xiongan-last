package cn.eyecool.scene.controller;

import java.io.IOException;
import java.io.InputStream;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.basedata.service.IBasePersonInfoService;
import cn.eyecool.common.annotation.Log;
import cn.eyecool.common.constant.Constants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.page.TableDataInfo;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.enums.BusinessType;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.scene.domain.ChannelBusiParam;
import cn.eyecool.scene.domain.ChannelBusiness;
import cn.eyecool.scene.service.IChannelBusinessService;
import lombok.extern.slf4j.Slf4j;

/**
 * 场景人员Controller
 * 
 * @author admin
 * @date 2021-03-22
 */
@RestController
@RequestMapping("/scene/channelBusi")
@Slf4j
public class ChannelBusinessController extends BaseController {

    @Autowired
    private IChannelBusinessService channelBusinessService;
    @Autowired
    private IBasePersonInfoService basePersonInfoService;
    @Autowired
    private RedisCache redisCache;

    /**
     * 查询场景人员列表
     */
    @PreAuthorize("@ss.hasPermi('scene:channelBusi:list')")
    @GetMapping("/list")
    public TableDataInfo list(ChannelBusiness channelBusiness) {
        startPage();
        List<ChannelBusiness> list = channelBusinessService.selectChannelBusinessList(channelBusiness);
        list.stream().forEach(item -> {
            BasePersonInfo personInfo = basePersonInfoService.selectBasePersonInfoById(item.getPersonId());
            if (null != personInfo) {
                item.setPersonName(personInfo.getName());
            }
        });
        return getDataTable(list);
    }

    /**
     * 导出场景人员列表
     */
    @PreAuthorize("@ss.hasPermi('scene:channelBusi:export')")
    @Log(title = "channel.business.person.title", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(ChannelBusiness channelBusiness) {
        String taskId = "Export-" + IdWorker.getNextStringId();
        String tenantId = TenantContextHolder.getTenantId();
        redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
            AjaxResult.success(StringUtils.EMPTY), 12, TimeUnit.HOURS);
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            List<ChannelBusiness> list = channelBusinessService.selectChannelBusinessList(channelBusiness);
            ExcelUtil<ChannelBusiness> util = new ExcelUtil<ChannelBusiness>(ChannelBusiness.class);
            AjaxResult result = util.exportExcel(list, "channelBusi");
            String fileName = (String)result.get(AjaxResult.MSG_TAG);
            redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
                AjaxResult.success(fileName), 12, TimeUnit.HOURS);
        });
        return AjaxResult.success(taskId);

    }

    /**
     * 获取场景人员详细信息
     */
    @PreAuthorize("@ss.hasPermi('scene:channelBusi:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        ChannelBusiness business = channelBusinessService.selectChannelBusinessById(id);
        BasePersonInfo personInfo = basePersonInfoService.selectBasePersonInfoById(business.getPersonId());
        if (null != personInfo) {
            business.setPersonName(personInfo.getName());
        }
        return AjaxResult.success(business);
    }

    /**
     * 新增场景人员
     */
    @PreAuthorize("@ss.hasPermi('scene:channelBusi:add')")
    @Log(title = "channel.business.person.title", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ChannelBusiParam channelBusiness) {
        return toAjax(channelBusinessService.insertChannelBusiness(channelBusiness));
    }

    /**
     * 修改场景人员
     */
    @PreAuthorize("@ss.hasPermi('scene:channelBusi:edit')")
    @Log(title = "channel.business.person.title", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ChannelBusiness channelBusiness) {
        return toAjax(channelBusinessService.updateChannelBusiness(channelBusiness));
    }

    /**
     * 删除场景人员
     */
    @PreAuthorize("@ss.hasPermi('scene:channelBusi:remove')")
    @Log(title = "channel.business.person.title", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(channelBusinessService.deleteChannelBusinessByIds(ids));
    }

    /**
     * 查询待加入场景库的人员列表
     * 
     * @param channelId
     * @param basePersonInfo
     * @return
     */
    @PreAuthorize("@ss.hasPermi('scene:channelBusi:list')")
    @GetMapping("/listUnBindPerson/{channelId}")
    public TableDataInfo selectToBeJoinedPersonInfoList(@PathVariable("channelId") String channelId,
        BasePersonInfo basePersonInfo) {
        startPage();
        List<BasePersonInfo> list = channelBusinessService.selectUnbindPersonInfoList(basePersonInfo, channelId);
        return getDataTable(list);
    }

    /**
     * 导入模板下载
     * 
     * @return
     */
    @GetMapping("/importTemplate")
    public AjaxResult importTemplate() {
        ExcelUtil<ChannelBusiness> util = new ExcelUtil<ChannelBusiness>(ChannelBusiness.class);
        return util.importTemplateExcel(MessageUtils.message("channel.business.person.title"));
    }

    /**
     * 保存批量导入
     * 
     * @throws Exception
     * @throws IOException
     */
    @PreAuthorize("@ss.hasPermi('scene:channelBusi:import')")
    @Log(title = "channel.business.person.title", businessType = BusinessType.IMPORT)
    @PostMapping("/import")
    public AjaxResult saveImportData(@RequestParam(value = "excelFile") MultipartFile excelFile, String channelId,
        Boolean updateSupport) throws IOException, Exception {
        String originalFilename = excelFile.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)) {
            return AjaxResult.error(MessageUtils.message("channel.business.person.import.filename.empty"));
        }
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        if (!"xls".equalsIgnoreCase(suffix) && !"xlsx".equalsIgnoreCase(suffix)) {
            return AjaxResult.error(MessageUtils.message("channel.business.person.import.file.format.wrong"));
        }
        String taskId = "ImportChannelBusi-" + IdWorker.getNextStringId();
        String tenantId = TenantContextHolder.getTenantId();
        InputStream inputStream = excelFile.getInputStream();
        redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
            AjaxResult.success(MessageUtils.message("channel.business.task.processing")), 7, TimeUnit.DAYS);
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            try {
                String message = channelBusinessService.saveImportData(inputStream, channelId, updateSupport);
                redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
                    AjaxResult.success(message), 7, TimeUnit.DAYS);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
                    AjaxResult.error(e.getMessage()), 7, TimeUnit.DAYS);
            }
        });
        return AjaxResult.success(taskId);
    }

    /**
     * 同步人库关系信息到Datamanager
     * 
     * @param basePersonInfo
     * @return
     */
    @PreAuthorize("@ss.hasPermi('scene:channelBusi:syncdata')")
    @Log(title = "channel.business.person.title", businessType = BusinessType.OTHER)
    @PostMapping("/syncdata")
    public AjaxResult syncdata(@RequestBody(required = false) ChannelBusiness channelBusiness) {
        String taskId = "SyncChannelBusiData-" + IdWorker.getNextStringId();
        String tenantId = TenantContextHolder.getTenantId();
        redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
            AjaxResult.success(MessageUtils.message("channel.business.task.processing")), 7, TimeUnit.DAYS);
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            try {
                AjaxResult ajaxResult = channelBusinessService.syncdata(channelBusiness);
                redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId, ajaxResult, 7,
                    TimeUnit.DAYS);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
                    AjaxResult.error(e.getMessage()), 7, TimeUnit.DAYS);
            }
        });
        return AjaxResult.success(taskId);
    }
}
