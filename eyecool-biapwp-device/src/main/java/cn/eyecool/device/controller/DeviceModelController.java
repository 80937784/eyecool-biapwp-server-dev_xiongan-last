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
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.device.domain.DeviceModel;
import cn.eyecool.device.service.IDeviceModelService;
import lombok.extern.slf4j.Slf4j;

/**
 * 设备型号信息Controller
 * 
 * @author admin
 * @date 2021-03-29
 */
@RestController
@RequestMapping("/device/model")
@Slf4j
public class DeviceModelController extends BaseController {
    @Autowired
    private IDeviceModelService deviceModelService;

    /**
     * 查询设备型号信息列表
     */
    @PreAuthorize("@ss.hasPermi('device:model:list')")
    @GetMapping("/list")
    public TableDataInfo list(DeviceModel deviceModel) {
        startPage();
        List<DeviceModel> list = deviceModelService.selectDeviceModelList(deviceModel);
        return getDataTable(list);
    }

    /**
     * 导出设备型号信息列表
     */
    @PreAuthorize("@ss.hasPermi('device:model:export')")
    @Log(title = "device.model.name", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(DeviceModel deviceModel) {
        List<DeviceModel> list = deviceModelService.selectDeviceModelList(deviceModel);
        ExcelUtil<DeviceModel> util = new ExcelUtil<DeviceModel>(DeviceModel.class);
        return util.exportExcel(list, "model");
    }

    /**
     * 获取设备型号信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('device:model:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        DeviceModel model = deviceModelService.selectDeviceModelById(id);
        if (StringUtils.isNotBlank(model.getExteriorImage())) {
            try {
                String imageBase64 = PlatformFileUtils.getImageBase64(model.getExteriorImage());
                model.setImageBase64(imageBase64);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return AjaxResult.success(model);
    }

    /**
     * 新增设备型号信息
     */
    @PreAuthorize("@ss.hasPermi('device:model:add')")
    @Log(title = "device.model.name", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody DeviceModel deviceModel) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            return AjaxResult.error(MessageUtils.message("device.model.tenant.no.auth"));
        }
        return toAjax(deviceModelService.insertDeviceModel(deviceModel));
    }

    /**
     * 修改设备型号信息
     */
    @PreAuthorize("@ss.hasPermi('device:model:edit')")
    @Log(title = "device.model.name", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody DeviceModel deviceModel) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            return AjaxResult.error(MessageUtils.message("device.model.tenant.no.auth"));
        }
        return toAjax(deviceModelService.updateDeviceModel(deviceModel));
    }

    /**
     * 删除设备型号信息
     */
    @PreAuthorize("@ss.hasPermi('device:model:remove')")
    @Log(title = "device.model.name", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            return AjaxResult.error(MessageUtils.message("device.model.tenant.no.auth"));
        }
        return toAjax(deviceModelService.deleteDeviceModelByIds(ids));
    }

    /**
     * 查询型号列表
     */
    @GetMapping("/listAll")
    public AjaxResult listAll() {
        List<DeviceModel> list = deviceModelService.selectDeviceModelList(new DeviceModel());
        return AjaxResult.success(list);
    }

}
