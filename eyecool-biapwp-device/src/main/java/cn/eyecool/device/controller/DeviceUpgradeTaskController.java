package cn.eyecool.device.controller;

import java.util.List;

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
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.page.TableDataInfo;
import cn.eyecool.common.enums.BusinessType;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.device.domain.DeviceUpgradeTask;
import cn.eyecool.device.service.IDeviceUpgradeTaskService;

/**
 * 升级任务Controller
 * 
 * @author admin
 * @date 2021-04-08
 */
@RestController
@RequestMapping("/device/upgradeTask")
public class DeviceUpgradeTaskController extends BaseController {
    @Autowired
    private IDeviceUpgradeTaskService deviceUpgradeTaskService;

    /**
     * 查询升级任务列表
     */
    @PreAuthorize("@ss.hasPermi('device:upgradeTask:list')")
    @GetMapping("/list")
    public TableDataInfo list(DeviceUpgradeTask deviceUpgradeTask) {
        startPage();
        List<DeviceUpgradeTask> list = deviceUpgradeTaskService.selectDeviceUpgradeTaskList(deviceUpgradeTask);
        return getDataTable(list);
    }

    /**
     * 导出升级任务列表
     */
    @PreAuthorize("@ss.hasPermi('device:upgradeTask:export')")
    @Log(title = "device.upgrade.task.name", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(DeviceUpgradeTask deviceUpgradeTask) {
        List<DeviceUpgradeTask> list = deviceUpgradeTaskService.selectDeviceUpgradeTaskList(deviceUpgradeTask);
        ExcelUtil<DeviceUpgradeTask> util = new ExcelUtil<DeviceUpgradeTask>(DeviceUpgradeTask.class);
        return util.exportExcel(list, "upgradeTask");
    }

    /**
     * 获取升级任务详细信息
     */
    @PreAuthorize("@ss.hasPermi('device:upgradeTask:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return AjaxResult.success(deviceUpgradeTaskService.selectDeviceUpgradeTaskById(id));
    }

    /**
     * 新增升级任务
     */
    @PreAuthorize("@ss.hasPermi('device:upgradeTask:add')")
    @Log(title = "device.upgrade.task.name", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody DeviceUpgradeTask deviceUpgradeTask) {
        return toAjax(deviceUpgradeTaskService.insertDeviceUpgradeTask(deviceUpgradeTask));
    }

    /**
     * 修改升级任务
     */
    @PreAuthorize("@ss.hasPermi('device:upgradeTask:edit')")
    @Log(title = "device.upgrade.task.name", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody DeviceUpgradeTask deviceUpgradeTask) {
        return toAjax(deviceUpgradeTaskService.updateDeviceUpgradeTask(deviceUpgradeTask));
    }

    /**
     * 删除升级任务
     */
    @PreAuthorize("@ss.hasPermi('device:upgradeTask:remove')")
    @Log(title = "device.upgrade.task.name", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(deviceUpgradeTaskService.deleteDeviceUpgradeTaskByIds(ids));
    }

    /**
     * 升级任务发布
     */
    @Log(title = "device.upgrade.task.publish", businessType = BusinessType.UPDATE)
    @PostMapping("/publish/{id}")
    public AjaxResult publish(@PathVariable("id") String id) {
        return toAjax(deviceUpgradeTaskService.publish(id));
    }
}
