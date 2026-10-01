package cn.eyecool.device.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.eyecool.common.annotation.Log;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.page.TableDataInfo;
import cn.eyecool.common.enums.BusinessType;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.device.domain.DeviceBatchLog;
import cn.eyecool.device.service.IDeviceBatchLogService;

/**
 * 设备批次Controller
 * 
 * @author admin
 * @date 2021-04-07
 */
@RestController
@RequestMapping("/device/batchlog")
public class DeviceBatchLogController extends BaseController {
    @Autowired
    private IDeviceBatchLogService deviceBatchLogService;

    /**
     * 查询设备批次列表
     */
    @PreAuthorize("@ss.hasPermi('device:batchlog:list')")
    @GetMapping("/list")
    public TableDataInfo list(DeviceBatchLog deviceBatchLog) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            return getDataTable(Collections.emptyList());
        }
        startPage();
        List<DeviceBatchLog> list = deviceBatchLogService.selectDeviceBatchLogList(deviceBatchLog);
        return getDataTable(list);
    }

    /**
     * 导出设备批次列表
     */
    @PreAuthorize("@ss.hasPermi('device:batchlog:export')")
    @Log(title = "device.batch.log.name", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(DeviceBatchLog deviceBatchLog) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            return AjaxResult.error(MessageUtils.message("device.batch.log.tenant.no.auth"));
        }
        List<DeviceBatchLog> list = deviceBatchLogService.selectDeviceBatchLogList(deviceBatchLog);
        ExcelUtil<DeviceBatchLog> util = new ExcelUtil<DeviceBatchLog>(DeviceBatchLog.class);
        return util.exportExcel(list, "batchlog");
    }

    /**
     * 获取设备批次详细信息
     */
    @PreAuthorize("@ss.hasPermi('device:batchlog:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            return AjaxResult.error(MessageUtils.message("device.batch.log.tenant.no.auth"));
        }
        return AjaxResult.success(deviceBatchLogService.selectDeviceBatchLogById(id));
    }

    /**
     * 设备批次导入回滚
     */
    @PreAuthorize("@ss.hasPermi('device:batchlog:rollback')")
    @Log(title = "device.batch.log.name", businessType = BusinessType.DELETE)
    @DeleteMapping("/rollback/{batchNums}")
    public AjaxResult rollback(@PathVariable String[] batchNums) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            return AjaxResult.error(MessageUtils.message("device.batch.log.tenant.no.auth"));
        }
        return toAjax(deviceBatchLogService.rollbackDeviceBatchByBatchNums(batchNums));
    }
}
