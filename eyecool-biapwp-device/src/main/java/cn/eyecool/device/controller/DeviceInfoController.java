package cn.eyecool.device.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cn.eyecool.common.annotation.Log;
import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.constant.Constants;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.entity.SysTenant;
import cn.eyecool.common.core.page.TableDataInfo;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.core.text.Convert;
import cn.eyecool.common.enums.BusinessType;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.device.constant.RedisKeyConstants;
import cn.eyecool.device.domain.DeviceInfo;
import cn.eyecool.device.domain.DeviceModel;
import cn.eyecool.device.service.IDeviceInfoService;
import cn.eyecool.device.service.IDeviceModelService;
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
 * @date 2021-04-01
 */
@RestController
@RequestMapping("/device/info")
@Slf4j
public class DeviceInfoController extends BaseController {
    @Autowired
    private IDeviceInfoService deviceInfoService;
    @Autowired
    private IDeviceModelService deviceModelService;
    @Autowired
    private ISysTenantService tenantService;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private IChannelInfoService channelInfoService;
    @Autowired
    private IChannelSubtreasuryInfoService subtreasuryInfoService;
    @Autowired
    private TenantProperties tenantProperties;

    /**
     * 查询设备信息列表
     */
    @PreAuthorize("@ss.hasPermi('device:info:list')")
    @GetMapping("/list")
    public TableDataInfo list(DeviceInfo deviceInfo) {
        startPage();
        List<DeviceInfo> list = deviceInfoService.selectDeviceInfoList(deviceInfo);
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
    @PreAuthorize("@ss.hasPermi('device:info:export')")
    @Log(title = "device.info.name", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(DeviceInfo deviceInfo) {
        List<DeviceInfo> list = deviceInfoService.selectDeviceInfoList(deviceInfo);
        ExcelUtil<DeviceInfo> util = new ExcelUtil<DeviceInfo>(DeviceInfo.class);
        return util.exportExcel(list, "info");
    }

    /**
     * 获取设备信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('device:info:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        DeviceInfo deviceInfo = deviceInfoService.selectDeviceInfoById(id);
        Object object =
            redisCache.getCacheObject(RedisKeyConstants.ONLINE_DEVICE_KEY_PREFIX + deviceInfo.getDeviceNo());
        deviceInfo.setDeviceState(
            null == object ? DictConstants.DeviceOnlineState.OFFLINE : DictConstants.DeviceOnlineState.ONLINE);
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
        if (StringUtils.isNotBlank(deviceInfo.getChannelCode())) {
            ChannelInfo channelCondition = new ChannelInfo();
            channelCondition.setChannelCode(deviceInfo.getChannelCode());
            List<ChannelInfo> channelInfoList = channelInfoService.selectChannelInfoList(channelCondition);
            if (CollectionUtils.isNotEmpty(channelInfoList)) {
                deviceInfo.setChannelName(channelInfoList.get(0).getChannelName());
                deviceInfo.setChannelId(channelInfoList.get(0).getId());
            } else {
                log.error("The scene code [{}] bound to the device [{}] failed to match the scene information", deviceInfo.getDeviceNo(), deviceInfo.getChannelCode());
            }
        }
        if (StringUtils.isNotBlank(deviceInfo.getSubtreasuryCode())) {
            String[] subCodeArray = Convert.toStrArray(deviceInfo.getSubtreasuryCode());
            String subNames = StringUtils.EMPTY;
            for (String subCode : subCodeArray) {
                ChannelSubtreasuryInfo subCondition = new ChannelSubtreasuryInfo();
                subCondition.setSubTreasuryCode(subCode);
                List<ChannelSubtreasuryInfo> subInfoList =
                    subtreasuryInfoService.selectChannelSubtreasuryInfoList(subCondition);
                if (CollectionUtils.isEmpty(subInfoList)) {
                    log.error("The sub-scene code [{}] bound to the device [{}] failed to match the sub-scene information", deviceInfo.getDeviceNo(), subCode);
                    continue;
                }
                String subName = subInfoList.get(0).getSubTreasuryName();
                subNames += subNames.length() == 0 ? subName : "," + subName;
                if (subCode.equals(deviceInfo.getPrimarySubCode())) {
                    deviceInfo.setPrimarySubName(subName);
                }
            }
            deviceInfo.setSubtreasuryName(subNames);
        }
        return AjaxResult.success(deviceInfo);
    }

    /**
     * 新增设备信息
     */
    @PreAuthorize("@ss.hasPermi('device:info:add')")
    @Log(title = "device.info.name", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody DeviceInfo deviceInfo) {
        return toAjax(deviceInfoService.insertDeviceInfo(deviceInfo));
    }

    /**
     * 修改设备信息
     */
    @PreAuthorize("@ss.hasPermi('device:info:edit')")
    @Log(title = "device.info.name", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody DeviceInfo deviceInfo) {
        return toAjax(deviceInfoService.updateDeviceInfo(deviceInfo));
    }

    /**
     * 删除设备信息
     */
    @PreAuthorize("@ss.hasPermi('device:info:remove')")
    @Log(title = "device.info.name", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            return AjaxResult.error(MessageUtils.message("device.info.tenant.no.auth"));
        }
        return toAjax(deviceInfoService.deleteDeviceInfoByIds(ids));
    }

    /**
     * 导入模板下载
     * 
     * @return
     */
    @GetMapping("/importTemplate")
    public AjaxResult importTemplate() {
        ExcelUtil<DeviceInfo> util = new ExcelUtil<DeviceInfo>(DeviceInfo.class);
        return util.importTemplateExcel(MessageUtils.message("device.info.data"));
    }

    /**
     * 生成设备导入批次号
     * 
     * @return
     */
    @GetMapping("/importBatchNum")
    public String generateImportBatchNum() {
        return "BATCH_" + DateUtils.dateTimeNow("yyyyMMdd_HHmmss");
    }

    /**
     * 保存批量导入
     * 
     * @throws Exception
     * @throws IOException
     */
    @PreAuthorize("@ss.hasPermi('device:info:import')")
    @Log(title = "device.info.name", businessType = BusinessType.IMPORT)
    @PostMapping("/import")
    public AjaxResult saveImportData(@RequestParam(value = "excelFile") MultipartFile excelFile, Boolean updateSupport,
        String deviceType, String tenantId, String importBatchNum, String batchDesc) throws IOException, Exception {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            return AjaxResult.error(MessageUtils.message("device.info.tenant.no.auth"));
        }
        if (StringUtils.isBlank(importBatchNum)) {
            return AjaxResult.error(MessageUtils.message("device.info.import.batch.empty"));
        }
        String originalFilename = excelFile.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)) {
            return AjaxResult.error(MessageUtils.message("device.info.import.filename.empty"));
        }
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        if (!"xls".equalsIgnoreCase(suffix) && !"xlsx".equalsIgnoreCase(suffix)) {
            return AjaxResult.error(MessageUtils.message("device.info.import.choose.excel"));
        }
        // 校验租户是否存在
        if (tenantProperties.getEnabled() && StringUtils.isNotBlank(tenantId)) {
            SysTenant tenant = tenantService.selectSysTenantByTenantId(tenantId);
            if (StringUtils.isNull(tenant)) {
                return AjaxResult.error(MessageUtils.message("device.info.tenant.no.exists", tenantId));
            }
        }
        String taskId = "ImportDeviceInfo-" + IdWorker.getNextStringId();
        InputStream inputStream = excelFile.getInputStream();
        redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
            AjaxResult.success(MessageUtils.message("device.info.task.processing")), 7, TimeUnit.DAYS);
        String username = SecurityUtils.getUsername();
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            try {
                String message = deviceInfoService.saveImportData(username, inputStream, deviceType, importBatchNum,
                    updateSupport, DeviceInfo.class, batchDesc);
                redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
                    AjaxResult.success(message), 7, TimeUnit.DAYS);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
                    AjaxResult.error(e.getMessage()), 7, TimeUnit.DAYS);
            }
        });
        return AjaxResult.success(taskId);
    }

    /**
     * 选择待加入升级任务的设备列表
     * 
     * @param versionId
     * @param deviceInfo
     * @return
     */
    @GetMapping("/listUpgradeDevice/{versionId}")
    public TableDataInfo listUpgradeDevice(@PathVariable("versionId") String versionId, DeviceInfo deviceInfo) {
        startPage();
        List<DeviceInfo> list = deviceInfoService.listUpgradeDevice(versionId, deviceInfo);
        list.stream().forEach(it -> {
            Object object = redisCache.getCacheObject(RedisKeyConstants.ONLINE_DEVICE_KEY_PREFIX + it.getDeviceNo());
            it.setDeviceState(
                null == object ? DictConstants.DeviceOnlineState.OFFLINE : DictConstants.DeviceOnlineState.ONLINE);
        });
        return getDataTable(list);
    }

}
