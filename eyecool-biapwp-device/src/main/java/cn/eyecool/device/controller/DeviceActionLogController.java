package cn.eyecool.device.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.eyecool.common.annotation.Log;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.page.TableDataInfo;
import cn.eyecool.common.enums.BusinessType;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.device.domain.DeviceActionLog;
import cn.eyecool.device.service.IDeviceActionLogService;

/**
 * 设备动作日志Controller
 * 
 * @author admin
 * @date 2021-04-14
 */
@RestController
@RequestMapping("/device/actionlog")
public class DeviceActionLogController extends BaseController {
    @Autowired
    private IDeviceActionLogService deviceActionLogService;

    /**
     * 查询设备动作日志列表
     */
    @PreAuthorize("@ss.hasPermi('device:actionlog:list')")
    @GetMapping("/list")
    public TableDataInfo list(DeviceActionLog deviceActionLog) {
        startPage();
        List<DeviceActionLog> list = deviceActionLogService.selectDeviceActionLogList(deviceActionLog);
        return getDataTable(list);
    }

    /**
     * 导出设备动作日志列表
     */
    @PreAuthorize("@ss.hasPermi('device:actionlog:export')")
    @Log(title = "device.action.log.name", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(DeviceActionLog deviceActionLog) {
        List<DeviceActionLog> list = deviceActionLogService.selectDeviceActionLogList(deviceActionLog);
        ExcelUtil<DeviceActionLog> util = new ExcelUtil<DeviceActionLog>(DeviceActionLog.class);
        return util.exportExcel(list, "actionlog");
    }

    /**
     * 获取设备动作日志详细信息
     */
    @PreAuthorize("@ss.hasPermi('device:actionlog:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return AjaxResult.success(deviceActionLogService.selectDeviceActionLogById(id));
    }

}
