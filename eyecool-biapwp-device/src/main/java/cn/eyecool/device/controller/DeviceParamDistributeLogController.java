package cn.eyecool.device.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.eyecool.common.annotation.Log;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.page.TableDataInfo;
import cn.eyecool.common.core.text.Convert;
import cn.eyecool.common.enums.BusinessType;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.device.domain.DeviceParamDistributeLog;
import cn.eyecool.device.service.IDeviceParamDistributeLogService;

/**
 * 参数下发日志Controller
 * 
 * @author admin
 * @date 2021-04-13
 */
@RestController
@RequestMapping("/device/paramlog")
public class DeviceParamDistributeLogController extends BaseController {
    @Autowired
    private IDeviceParamDistributeLogService deviceParamDistributeLogService;

    /**
     * 查询参数下发日志列表
     */
    @PreAuthorize("@ss.hasPermi('device:paramlog:list')")
    @GetMapping("/list")
    public TableDataInfo list(DeviceParamDistributeLog deviceParamDistributeLog) {
        startPage();
        List<DeviceParamDistributeLog> list =
            deviceParamDistributeLogService.selectDeviceParamDistributeLogList(deviceParamDistributeLog);
        return getDataTable(list);
    }

    /**
     * 导出参数下发日志列表
     */
    @PreAuthorize("@ss.hasPermi('device:paramlog:export')")
    @Log(title = "device.param.distribute.name", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(DeviceParamDistributeLog deviceParamDistributeLog) {
        List<DeviceParamDistributeLog> list =
            deviceParamDistributeLogService.selectDeviceParamDistributeLogList(deviceParamDistributeLog);
        ExcelUtil<DeviceParamDistributeLog> util =
            new ExcelUtil<DeviceParamDistributeLog>(DeviceParamDistributeLog.class);
        return util.exportExcel(list, "paramlog");
    }

    /**
     * 获取参数下发日志详细信息
     */
    @PreAuthorize("@ss.hasPermi('device:paramlog:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return AjaxResult.success(deviceParamDistributeLogService.selectDeviceParamDistributeLogById(id));
    }

    /**
     * 新增参数下发日志
     */
    @PreAuthorize("@ss.hasPermi('device:paramlog:add')")
    @Log(title = "device.param.distribute.name", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody DeviceParamDistributeLog deviceParamDistributeLog) {
        int succNum = deviceParamDistributeLogService.insertDeviceParamDistributeLog(deviceParamDistributeLog);
        int deviceCount = Convert.toStrArray(deviceParamDistributeLog.getDeviceNo()).length;
        return succNum < deviceCount ? AjaxResult.error((deviceCount - succNum) + MessageUtils.message("device.param.distribute.failed")) : AjaxResult.success();
    }
}
