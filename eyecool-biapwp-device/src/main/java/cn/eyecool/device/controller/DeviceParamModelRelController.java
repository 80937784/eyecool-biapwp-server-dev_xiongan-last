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
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.device.domain.DeviceParamModelRel;
import cn.eyecool.device.service.IDeviceParamModelRelService;

/**
 * 参数型号关系Controller
 * 
 * @author admin
 * @date 2021-03-31
 */
@RestController
@RequestMapping("/device/paramModelRel")
public class DeviceParamModelRelController extends BaseController {
    @Autowired
    private IDeviceParamModelRelService deviceParamModelRelService;

    /**
     * 查询参数型号关系列表
     */
    @PreAuthorize("@ss.hasPermi('device:paramModelRel:list')")
    @GetMapping("/list")
    public TableDataInfo list(DeviceParamModelRel deviceParamModelRel) {
        startPage();
        List<DeviceParamModelRel> list = deviceParamModelRelService.selectDeviceParamModelRelList(deviceParamModelRel);
        return getDataTable(list);
    }

    /**
     * 导出参数型号关系列表
     */
    @PreAuthorize("@ss.hasPermi('device:paramModelRel:export')")
    @Log(title = "device.param.model.rel.name", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(DeviceParamModelRel deviceParamModelRel) {
        List<DeviceParamModelRel> list = deviceParamModelRelService.selectDeviceParamModelRelList(deviceParamModelRel);
        ExcelUtil<DeviceParamModelRel> util = new ExcelUtil<DeviceParamModelRel>(DeviceParamModelRel.class);
        return util.exportExcel(list, "paramModelRel");
    }

    /**
     * 获取参数型号关系详细信息
     */
    @PreAuthorize("@ss.hasPermi('device:paramModelRel:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return AjaxResult.success(deviceParamModelRelService.selectDeviceParamModelRelById(id));
    }

    /**
     * 新增参数型号关系
     */
    @PreAuthorize("@ss.hasPermi('device:paramModelRel:add')")
    @Log(title = "device.param.model.rel.name", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody DeviceParamModelRel deviceParamModelRel) { // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            return AjaxResult.error(MessageUtils.message("device.param.model.rel.tenant.no.auth"));
        }
        return toAjax(deviceParamModelRelService.insertDeviceParamModelRel(deviceParamModelRel));
    }

    /**
     * 修改参数型号关系
     */
    @PreAuthorize("@ss.hasPermi('device:paramModelRel:edit')")
    @Log(title = "device.param.model.rel.name", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody DeviceParamModelRel deviceParamModelRel) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            return AjaxResult.error(MessageUtils.message("device.param.model.rel.tenant.no.auth"));
        }
        return toAjax(deviceParamModelRelService.updateDeviceParamModelRel(deviceParamModelRel));
    }

    /**
     * 删除参数型号关系
     */
    @PreAuthorize("@ss.hasPermi('device:paramModelRel:remove')")
    @Log(title = "device.param.model.rel.name", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            return AjaxResult.error(MessageUtils.message("device.param.model.rel.tenant.no.auth"));
        }
        return toAjax(deviceParamModelRelService.deleteDeviceParamModelRelByIds(ids));
    }

    /**
     * 查询参数型号关系列表
     */
    @GetMapping("/listAll/{deviceModelCode}")
    public AjaxResult listAll(@PathVariable("deviceModelCode") String deviceModelCode) {
        DeviceParamModelRel rel = new DeviceParamModelRel();
        rel.setModelCode(deviceModelCode);
        List<DeviceParamModelRel> list = deviceParamModelRelService.selectDeviceParamModelRelList(rel);
        return AjaxResult.success(list);
    }
}
