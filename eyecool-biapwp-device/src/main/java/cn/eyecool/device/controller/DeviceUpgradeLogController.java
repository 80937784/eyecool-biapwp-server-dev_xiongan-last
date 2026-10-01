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
import cn.eyecool.device.domain.DeviceUpgradeLog;
import cn.eyecool.device.service.IDeviceUpgradeLogService;

/**
 * 升级日志Controller
 * 
 * @author admin
 * @date 2021-04-09
 */
@RestController
@RequestMapping("/device/upgradelog")
public class DeviceUpgradeLogController extends BaseController {
    @Autowired
    private IDeviceUpgradeLogService deviceUpgradeLogService;

    /**
     * 查询升级日志列表
     */
    @PreAuthorize("@ss.hasPermi('device:upgradelog:list')")
    @GetMapping("/list")
    public TableDataInfo list(DeviceUpgradeLog deviceUpgradeLog) {
        startPage();
        List<DeviceUpgradeLog> list = deviceUpgradeLogService.selectDeviceUpgradeLogList(deviceUpgradeLog);
        return getDataTable(list);
    }

    /**
     * 导出升级日志列表
     */
    @PreAuthorize("@ss.hasPermi('device:upgradelog:export')")
    @Log(title = "device.upgrade.log.name", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(DeviceUpgradeLog deviceUpgradeLog) {
        List<DeviceUpgradeLog> list = deviceUpgradeLogService.selectDeviceUpgradeLogList(deviceUpgradeLog);
        ExcelUtil<DeviceUpgradeLog> util = new ExcelUtil<DeviceUpgradeLog>(DeviceUpgradeLog.class);
        return util.exportExcel(list, "upgradelog");
    }

    /**
     * 获取升级日志详细信息
     */
    @PreAuthorize("@ss.hasPermi('device:upgradelog:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return AjaxResult.success(deviceUpgradeLogService.selectDeviceUpgradeLogById(id));
    }
}
