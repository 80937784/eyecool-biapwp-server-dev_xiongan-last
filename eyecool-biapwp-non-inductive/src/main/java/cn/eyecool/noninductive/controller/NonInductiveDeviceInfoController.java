package cn.eyecool.noninductive.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cn.eyecool.common.annotation.Log;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.entity.SysTenant;
import cn.eyecool.common.core.page.TableDataInfo;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.core.text.Convert;
import cn.eyecool.common.enums.BusinessType;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.device.constant.RedisKeyConstants;
import cn.eyecool.device.domain.DeviceInfo;
import cn.eyecool.device.domain.DeviceModel;
import cn.eyecool.device.service.IDeviceInfoService;
import cn.eyecool.device.service.IDeviceModelService;
import cn.eyecool.noninductive.domain.NonInductiveDeviceInfo;
import cn.eyecool.scene.domain.ChannelInfo;
import cn.eyecool.scene.domain.ChannelSubtreasuryInfo;
import cn.eyecool.scene.service.IChannelInfoService;
import cn.eyecool.scene.service.IChannelSubtreasuryInfoService;
import cn.eyecool.system.service.ISysTenantService;
import lombok.extern.slf4j.Slf4j;

/**
 * 设备信息Controller
 *
 * @author admin
 * @date 2019-12-27
 */
@RestController
@RequestMapping("/device/noninductive")
@Slf4j
public class NonInductiveDeviceInfoController extends BaseController {
    @Autowired
    private IDeviceInfoService deviceInfoService;
    @Autowired
    private IDeviceModelService deviceModelService;
    @Autowired
    private IChannelInfoService channelInfoService;

    @Autowired
    private IChannelSubtreasuryInfoService subtreasuryInfoService;
    @Autowired
    private ISysTenantService sysTenantService;
    @Autowired
    private RedisCache redisCache;

    /**
     * 查询设备信息列表
     */
    @PreAuthorize("@ss.hasPermi('noninductive:device:list')")
    @GetMapping("/list")
    public TableDataInfo list(DeviceInfo clientDeviceInfo) {
        startPage();
        // 只查询无感设备
        clientDeviceInfo.setDeviceType(DictConstants.DeviceType.NONINDUCTIVE_DEVICE);
        List<DeviceInfo> list = deviceInfoService.selectDeviceInfoList(clientDeviceInfo);
        list.stream().forEach(it -> {
            Object object = redisCache.getCacheObject(RedisKeyConstants.ONLINE_DEVICE_KEY_PREFIX + it.getDeviceNo());
            it.setDeviceState(
                null == object ? DictConstants.DeviceOnlineState.OFFLINE : DictConstants.DeviceOnlineState.ONLINE);
        });
        return getDataTable(list);
    }

    /**
     * 导出设备信息列表
     */
    @PreAuthorize("@ss.hasPermi('noninductive:device:export')")
    @Log(title = "设备信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(DeviceInfo clientDeviceInfo) {
        clientDeviceInfo.setDeviceType(DictConstants.DeviceType.NONINDUCTIVE_DEVICE);
        List<DeviceInfo> list = deviceInfoService.selectDeviceInfoList(clientDeviceInfo);
        ExcelUtil<DeviceInfo> util = new ExcelUtil<DeviceInfo>(DeviceInfo.class);
        return util.exportExcel(list, "device");
    }

    /**
     * 新增设备信息
     */
    @PreAuthorize("@ss.hasPermi('noninductive:device:add')")
    @Log(title = "设备信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody NonInductiveDeviceInfo nonInductiveClientDeviceInfo) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            return AjaxResult.error("租户没有操作权限，如有疑问，请联系系统管理员!");
        }
        if (SecurityUtils.isTenantUser()) {
            return AjaxResult.error("租户没有操作权限，如有疑问，请联系系统管理员!");
        }
        // 无感考勤设备
        nonInductiveClientDeviceInfo.setDeviceType(DictConstants.DeviceType.NONINDUCTIVE_DEVICE);
        nonInductiveClientDeviceInfo.setExtInfo();
        return toAjax(deviceInfoService.insertDeviceInfo(nonInductiveClientDeviceInfo));
    }

    /**
     * 修改保存设备信息
     */
    @PreAuthorize("@ss.hasPermi('noninductive:device:edit')")
    @Log(title = "设备信息", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(DeviceInfo clientDeviceInfo) {
        // 无感考勤设备
        clientDeviceInfo.setDeviceType(DictConstants.DeviceType.NONINDUCTIVE_DEVICE);
        return toAjax(deviceInfoService.updateDeviceInfo(clientDeviceInfo));
    }

    /**
     * 删除设备信息
     */
    @PreAuthorize("@ss.hasPermi('noninductive:device:remove')")
    @Log(title = "设备信息", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids, String tenantId) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            return AjaxResult.error("租户没有操作权限，如有疑问，请联系系统管理员!");
        }
        // 校验租户是否存在
        if (StringUtils.isNotBlank(tenantId)) {
            SysTenant tenant = sysTenantService.selectSysTenantByTenantId(tenantId);
            if (StringUtils.isNull(tenant)) {
                return AjaxResult.error("租户[" + tenantId + "]信息不存在!");
            }
        }
        return toAjax(deviceInfoService.deleteDeviceInfoByIds(Convert.toStrArray(ids)));
    }

    /**
     * 获取设备信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('noninductive:device:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        DeviceInfo deviceInfo = deviceInfoService.selectDeviceInfoById(id);
        String deviceModelCode = deviceInfo.getDeviceModelCode();
        if (StringUtils.isBlank(deviceModelCode)) {
            DeviceModel model = new DeviceModel();
            model.setModelCode(deviceModelCode);
            List<DeviceModel> list = deviceModelService.selectDeviceModelList(model);
            if (CollectionUtils.isNotEmpty(list) && StringUtils.isNotBlank(list.get(0).getExteriorImage())) {
                try {
                    String imageBase64 = PlatformFileUtils.getImageBase64(list.get(0).getExteriorImage());
                    deviceInfo.setImageBase64(imageBase64);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return AjaxResult.success(deviceInfo);
    }

    /**
     * 下载模板
     */
    @GetMapping("/importTemplate")
    @ResponseBody
    public AjaxResult importTemplate() {
        ExcelUtil<NonInductiveDeviceInfo> util = new ExcelUtil<>(NonInductiveDeviceInfo.class);
        return util.importTemplateExcel("无感设备信息数据");
    }

    /**
     * 保存批量导入
     *
     * @throws Exception
     * @throws IOException
     */
    @PreAuthorize("@ss.hasPermi('noninductive:device:import')")
    @PostMapping("/import")
    @Log(title = "设备信息", businessType = BusinessType.IMPORT)
    @ResponseBody
    public AjaxResult saveImportData(@RequestParam(value = "excelFile", required = false) MultipartFile excelFile,
        String tenantId, String importBatchNum, Boolean supportUpdate) throws IOException, Exception {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            return AjaxResult.error("租户没有操作权限，如有疑问，请联系系统管理员!");
        }
        if (StringUtils.isBlank(importBatchNum)) {
            return AjaxResult.error("设备导入批次号不能为空，请检查!");
        }
        // 校验租户是否存在
        if (StringUtils.isNotBlank(tenantId)) {
            SysTenant tenant = sysTenantService.selectSysTenantByTenantId(tenantId);
            if (StringUtils.isNull(tenant)) {
                return AjaxResult.error("租户[" + tenantId + "]信息不存在!");
            }
        }
        if (null == excelFile) {
            throw new CustomException("请选择Excel文件！");
        }
        String message = deviceInfoService.saveImportData(excelFile, DictConstants.DeviceType.NONINDUCTIVE_DEVICE,
            importBatchNum, supportUpdate, NonInductiveDeviceInfo.class, "");
        return AjaxResult.success(message);
    }

    /**
     * 查询子场景列表
     *
     * @param channelCode
     * @param subInfo
     * @return
     */
    @PostMapping("/listSubtreasury/{channelCode}")
    @ResponseBody
    public TableDataInfo selectSubtreasuryList(@PathVariable("channelCode") String channelCode,
        ChannelSubtreasuryInfo subInfo) {
        ChannelInfo channelInfo = new ChannelInfo();
        channelInfo.setChannelCode(channelCode);
        List<ChannelInfo> channelInfoList = channelInfoService.selectChannelInfoList(channelInfo);
        if (CollectionUtils.isEmpty(channelInfoList)) {
            return new TableDataInfo(Collections.emptyList(), 0);
        }
        subInfo.setChannelId(channelInfoList.get(0).getId());
        startPage();
        List<ChannelSubtreasuryInfo> list = subtreasuryInfoService.selectChannelSubtreasuryInfoList(subInfo);
        return getDataTable(list);
    }

}
