package cn.eyecool.device.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import cn.eyecool.common.annotation.Log;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.page.TableDataInfo;
import cn.eyecool.common.enums.BusinessType;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.device.domain.DeviceUpgradeVersion;
import cn.eyecool.device.service.IDeviceUpgradeVersionService;

/**
 * 版本信息Controller
 * 
 * @author admin
 * @date 2021-04-07
 */
@RestController
@RequestMapping("/device/upgrade/version")
public class DeviceUpgradeVersionController extends BaseController {
    @Autowired
    private IDeviceUpgradeVersionService deviceUpgradeVersionService;

    /**
     * 查询版本信息列表
     */
    @PreAuthorize("@ss.hasPermi('device:upgradeVersion:list')")
    @GetMapping("/list")
    public TableDataInfo list(DeviceUpgradeVersion deviceUpgradeVersion) {
        startPage();
        if (SecurityUtils.isTenantUser()) {
            deviceUpgradeVersion.setEnabled(true);
        }
        List<DeviceUpgradeVersion> list =
            deviceUpgradeVersionService.selectDeviceUpgradeVersionList(deviceUpgradeVersion);
        return getDataTable(list);
    }

    /**
     * 导出版本信息列表
     */
    @PreAuthorize("@ss.hasPermi('device:upgradeVersion:export')")
    @Log(title = "device.upgrade.version.name", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(DeviceUpgradeVersion deviceUpgradeVersion) {
        List<DeviceUpgradeVersion> list =
            deviceUpgradeVersionService.selectDeviceUpgradeVersionList(deviceUpgradeVersion);
        ExcelUtil<DeviceUpgradeVersion> util = new ExcelUtil<DeviceUpgradeVersion>(DeviceUpgradeVersion.class);
        return util.exportExcel(list, "version");
    }

    /**
     * 获取版本信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('device:upgradeVersion:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return AjaxResult.success(deviceUpgradeVersionService.selectDeviceUpgradeVersionById(id));
    }

    /**
     * 新增版本信息
     */
    @PreAuthorize("@ss.hasPermi('device:upgradeVersion:add')")
    @Log(title = "device.upgrade.version.name", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestParam(value = "file") MultipartFile file, DeviceUpgradeVersion deviceUpgradeVersion) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            return AjaxResult.error(MessageUtils.message("device.upgrade.version.tenant.no.auth"));
        }
        return toAjax(deviceUpgradeVersionService.insertDeviceUpgradeVersion(file, deviceUpgradeVersion));
    }

    /**
     * 修改版本信息
     */
    @PreAuthorize("@ss.hasPermi('device:upgradeVersion:edit')")
    @Log(title = "device.upgrade.version.name", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody DeviceUpgradeVersion deviceUpgradeVersion) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            return AjaxResult.error(MessageUtils.message("device.upgrade.version.tenant.no.auth"));
        }
        return toAjax(deviceUpgradeVersionService.updateDeviceUpgradeVersion(deviceUpgradeVersion));
    }

    /**
     * 删除版本信息
     */
    @PreAuthorize("@ss.hasPermi('device:upgradeVersion:remove')")
    @Log(title = "device.upgrade.version.name", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            return AjaxResult.error(MessageUtils.message("device.upgrade.version.tenant.no.auth"));
        }
        return toAjax(deviceUpgradeVersionService.deleteDeviceUpgradeVersionByIds(ids));
    }

    /**
     * 下载版本文件
     */
    @Log(title = "device.upgrade.version.download", businessType = BusinessType.OTHER)
    @GetMapping("/download/{id}")
    public void download(@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response) {
        deviceUpgradeVersionService.downloadVersionFile(id, request, response);
    }

    /**
     * 查询所有版本信息列表
     */
    @GetMapping("/listAll")
    public AjaxResult listAll(DeviceUpgradeVersion deviceUpgradeVersion) {
        List<DeviceUpgradeVersion> list =
            deviceUpgradeVersionService.selectDeviceUpgradeVersionList(deviceUpgradeVersion);
        return AjaxResult.success(list);
    }
}
