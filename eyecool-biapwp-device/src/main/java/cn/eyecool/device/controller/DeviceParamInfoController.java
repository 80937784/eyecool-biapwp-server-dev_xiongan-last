package cn.eyecool.device.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
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
import cn.eyecool.device.domain.DeviceParamInfo;
import cn.eyecool.device.domain.DeviceParamModelRel;
import cn.eyecool.device.service.IDeviceParamInfoService;
import cn.eyecool.device.service.IDeviceParamModelRelService;

/**
 * 设备参数信息Controller
 * 
 * @author admin
 * @date 2021-03-30
 */
@RestController
@RequestMapping("/device/paraminfo")
public class DeviceParamInfoController extends BaseController {
    @Autowired
    private IDeviceParamInfoService deviceParamInfoService;
    @Autowired
    private IDeviceParamModelRelService paramModelRelService;

    /**
     * 查询设备参数信息列表
     */
    @PreAuthorize("@ss.hasPermi('device:paraminfo:list')")
    @GetMapping("/list")
    public TableDataInfo list(DeviceParamInfo deviceParamInfo) {
        startPage();
        List<DeviceParamInfo> list = deviceParamInfoService.selectDeviceParamInfoList(deviceParamInfo);
        return getDataTable(list);
    }

    /**
     * 导出设备参数信息列表
     */
    @PreAuthorize("@ss.hasPermi('device:paraminfo:export')")
    @Log(title = "device.param.info.name", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(DeviceParamInfo deviceParamInfo) {
        List<DeviceParamInfo> list = deviceParamInfoService.selectDeviceParamInfoList(deviceParamInfo);
        ExcelUtil<DeviceParamInfo> util = new ExcelUtil<DeviceParamInfo>(DeviceParamInfo.class);
        return util.exportExcel(list, "param");
    }

    /**
     * 获取设备参数信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('device:paraminfo:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return AjaxResult.success(deviceParamInfoService.selectDeviceParamInfoById(id));
    }

    /**
     * 新增设备参数信息
     */
    @PreAuthorize("@ss.hasPermi('device:paraminfo:add')")
    @Log(title = "device.param.info.name", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody DeviceParamInfo deviceParamInfo) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            return AjaxResult.error(MessageUtils.message("device.param.info.tenant.no.auth"));
        }
        return toAjax(deviceParamInfoService.insertDeviceParamInfo(deviceParamInfo));
    }

    /**
     * 修改设备参数信息
     */
    @PreAuthorize("@ss.hasPermi('device:paraminfo:edit')")
    @Log(title = "device.param.info.name", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody DeviceParamInfo deviceParamInfo) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            return AjaxResult.error(MessageUtils.message("device.param.info.tenant.no.auth"));
        }
        return toAjax(deviceParamInfoService.updateDeviceParamInfo(deviceParamInfo));
    }

    /**
     * 删除设备参数信息
     */
    @PreAuthorize("@ss.hasPermi('device:paraminfo:remove')")
    @Log(title = "device.param.info.name", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            return AjaxResult.error(MessageUtils.message("device.param.info.tenant.no.auth"));
        }
        return toAjax(deviceParamInfoService.deleteDeviceParamInfoByIds(ids));
    }

    /**
     * 查询参数列表
     */
    @GetMapping("/listAll/excludeModel/{modelCode}")
    public AjaxResult listAll(@PathVariable String modelCode) {
        List<DeviceParamInfo> list = deviceParamInfoService.selectDeviceParamInfoList(new DeviceParamInfo());
        DeviceParamModelRel rel = new DeviceParamModelRel();
        rel.setModelCode(modelCode);
        List<DeviceParamModelRel> relList = paramModelRelService.selectDeviceParamModelRelList(rel);
        if (CollectionUtils.isNotEmpty(relList)) {
            List<String> excludeParamCodeList =
                relList.stream().map(DeviceParamModelRel::getParamCode).collect(Collectors.toList());
            list = list.stream().filter(item -> !excludeParamCodeList.contains(item.getParamCode()))
                .collect(Collectors.toList());
        }
        return AjaxResult.success(list);
    }
}
