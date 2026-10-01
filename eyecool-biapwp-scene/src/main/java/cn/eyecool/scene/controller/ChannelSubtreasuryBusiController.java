package cn.eyecool.scene.controller;

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
import cn.eyecool.scene.domain.ChannelSubBusiParam;
import cn.eyecool.scene.domain.ChannelSubtreasuryBusi;
import cn.eyecool.scene.domain.ChannelSubtreasuryInfo;
import cn.eyecool.scene.service.IChannelSubtreasuryBusiService;
import cn.eyecool.scene.service.IChannelSubtreasuryInfoService;
import lombok.extern.slf4j.Slf4j;

/**
 * 子场景人员Controller
 * 
 * @author admin
 * @date 2021-03-22
 */
@RestController
@RequestMapping("/scene/subtreasuryBusi")
@Slf4j
public class ChannelSubtreasuryBusiController extends BaseController {

    @Autowired
    private IChannelSubtreasuryInfoService channelSubtreasuryInfoService;
    @Autowired
    private IChannelSubtreasuryBusiService channelSubtreasuryBusiService;
    @Autowired
    private IBasePersonInfoService basePersonInfoService;
    @Autowired
    private RedisCache redisCache;

    /**
     * 查询子场景人员列表
     */
    @PreAuthorize("@ss.hasPermi('scene:channelBusi:list')")
    @GetMapping("/list")
    public TableDataInfo list(ChannelSubtreasuryBusi channelSubtreasuryBusi) {
        startPage();
        List<ChannelSubtreasuryBusi> list =
            channelSubtreasuryBusiService.selectChannelSubtreasuryBusiList(channelSubtreasuryBusi);
        list.stream().forEach(item -> {
            BasePersonInfo personInfo = basePersonInfoService.selectBasePersonInfoById(item.getPersonId());
            if (null != personInfo) {
                item.setPersonName(personInfo.getName());
            }
        });
        return getDataTable(list);
    }

    /**
     * 导出子场景人员列表
     */
    @PreAuthorize("@ss.hasPermi('scene:channelBusi:export')")
    @Log(title = "channel.sub.scene.person.title", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(ChannelSubtreasuryBusi channelSubtreasuryBusi) {
        String taskId = "Export-" + IdWorker.getNextStringId();
        String tenantId = TenantContextHolder.getTenantId();
        redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
            AjaxResult.success(StringUtils.EMPTY), 12, TimeUnit.HOURS);
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            List<ChannelSubtreasuryBusi> list =
                channelSubtreasuryBusiService.selectChannelSubtreasuryBusiList(channelSubtreasuryBusi);
            ExcelUtil<ChannelSubtreasuryBusi> util =
                new ExcelUtil<ChannelSubtreasuryBusi>(ChannelSubtreasuryBusi.class);
            AjaxResult result = util.exportExcel(list, "subtreasuryBusi");
            String fileName = (String)result.get(AjaxResult.MSG_TAG);
            redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
                AjaxResult.success(fileName), 12, TimeUnit.HOURS);
        });
        return AjaxResult.success(taskId);

    }

    /**
     * 获取子场景人员详细信息
     */
    @PreAuthorize("@ss.hasPermi('scene:channelBusi:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        ChannelSubtreasuryBusi subtreasuryBusi = channelSubtreasuryBusiService.selectChannelSubtreasuryBusiById(id);
        BasePersonInfo personInfo = basePersonInfoService.selectBasePersonInfoById(subtreasuryBusi.getPersonId());
        if (null != personInfo) {
            subtreasuryBusi.setPersonName(personInfo.getName());
        }
        return AjaxResult.success(subtreasuryBusi);
    }

    /**
     * 新增子场景人员
     */
    @PreAuthorize("@ss.hasPermi('scene:channelBusi:add')")
    @Log(title = "channel.sub.scene.person.title", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ChannelSubBusiParam param) {
        return toAjax(channelSubtreasuryBusiService.insertChannelSubtreasuryBusi(param));
    }

    /**
     * 修改子场景人员
     */
    @PreAuthorize("@ss.hasPermi('scene:channelBusi:edit')")
    @Log(title = "channel.sub.scene.person.title", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ChannelSubtreasuryBusi channelSubtreasuryBusi) {
        return toAjax(channelSubtreasuryBusiService.updateChannelSubtreasuryBusi(channelSubtreasuryBusi));
    }

    /**
     * 删除子场景人员
     */
    @PreAuthorize("@ss.hasPermi('scene:channelBusi:remove')")
    @Log(title = "channel.sub.scene.person.title", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(channelSubtreasuryBusiService.deleteChannelSubtreasuryBusiByIds(ids));
    }

    /**
     * 查询待加入场景库的人员列表
     * 
     * @param channelId
     * @param basePersonInfo
     * @return
     */
    @PreAuthorize("@ss.hasPermi('scene:channelBusi:list')")
    @GetMapping("/listUnBindPerson/{subTreasuryId}")
    public TableDataInfo selectToBeJoinedPersonInfoList(@PathVariable("subTreasuryId") String subTreasuryId,
        BasePersonInfo basePersonInfo) {
        ChannelSubtreasuryInfo subtreasuryInfo =
            channelSubtreasuryInfoService.selectChannelSubtreasuryInfoById(subTreasuryId);
        startPage();
        List<BasePersonInfo> list = channelSubtreasuryBusiService.selectUnbindPersonInfoList(basePersonInfo,
            subTreasuryId, subtreasuryInfo.getChannelId());
        return getDataTable(list);
    }

    /**
     * 同步人库关系信息到Datamanager
     * 
     * @param sbusi
     * @return
     */
    @PreAuthorize("@ss.hasPermi('scene:channelBusi:syncdata')")
    @Log(title = "channel.sub.scene.person.title", businessType = BusinessType.OTHER)
    @PostMapping("/syncdata")
    public AjaxResult syncdata(@RequestBody(required = false) ChannelSubtreasuryBusi sbusi) {
        String taskId = "SyncSubBusiData-" + IdWorker.getNextStringId();
        String tenantId = TenantContextHolder.getTenantId();
        redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
            AjaxResult.success(MessageUtils.message("channel.sub.scene.task.processing")), 7, TimeUnit.DAYS);
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            try {
                AjaxResult ajaxResult = channelSubtreasuryBusiService.syncdata(sbusi);
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

    /**
     * 清空子场景人员
     */
    @PreAuthorize("@ss.hasPermi('scene:channelBusi:remove')")
    @Log(title = "channel.sub.scene.person.title", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clearSubData/{subtreasuryIds}")
    public AjaxResult clearSubData(@PathVariable String[] subtreasuryIds) {
        channelSubtreasuryBusiService.clearSubData(subtreasuryIds);
        return AjaxResult.success();
    }
}
