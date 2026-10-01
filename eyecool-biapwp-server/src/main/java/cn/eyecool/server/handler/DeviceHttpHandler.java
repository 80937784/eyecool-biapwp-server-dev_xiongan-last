package cn.eyecool.server.handler;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.validator.routines.LongValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.internal.Maps;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.http.HttpAjaxResult;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.sql.SqlUtil;
import cn.eyecool.device.domain.DeviceInfo;
import cn.eyecool.device.domain.DeviceUpgradeTask;
import cn.eyecool.device.service.IDeviceInfoService;
import cn.eyecool.device.service.IDeviceUpgradeTaskService;

/**
 * 客户端设备升级和设备管理HTTP请求处理器
 * 
 * @author admin
 * @date 2019年12月31日
 */
@Component
public class DeviceHttpHandler {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceHttpHandler.class);

    @Autowired
    private IDeviceInfoService deviceInfoService;
    @Autowired
    private IDeviceUpgradeTaskService deviceUpgradeTaskService;
    @Autowired
    private TenantProperties tenantProperties;

    @Value("${eyecool.serverDomain}")
    private String serverDomain;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * 查询设备信息
     * 
     * @param deviceNo
     * @return
     */
    private DeviceInfo getClientByDeviceNo(String deviceNo) {
        // 清空在此之前设备之的TenantContextHolder上下文信息，不携带租户隔离查询设备信息
        String tenantId = TenantContextHolder.getTenantId();
        TenantContextHolder.clear();
        DeviceInfo condition = new DeviceInfo();
        condition.setDeviceNo(deviceNo);
        List<DeviceInfo> list = deviceInfoService.selectDeviceInfoList(condition);
        // 重新设置租户信息
        if (StringUtils.isNotBlank(tenantId)) {
            TenantContextHolder.setTenantId(tenantId);
        }
        if (CollectionUtils.isEmpty(list)) {
            LOG.error("Device [{}] does not exist", deviceNo);
            return null;
        }
        return list.get(0);
    }

    /**
     * 校验设备所属租户和接口授权租户是否一致
     *
     * @param deviceInfo
     * @return
     */
    private boolean validateDeviceTenant(DeviceInfo deviceInfo) {
        if (!tenantProperties.getEnabled()) {
            return true;
        }
        String deviceNo = deviceInfo.getDeviceNo();
        LOG.info("Device [{}] belongs to tenant [{}]", deviceNo, deviceInfo.getTenantId());
        if (StringUtils.isBlank(deviceInfo.getTenantId())) {
            LOG.error("Device [{}] does not maintain tenant information", deviceNo);
            return false;
        }
        // 验证appKey所属租户和设备所属租户是否一致
        String tenantId = TenantContextHolder.getTenantId();
        if (!deviceInfo.getTenantId().equals(tenantId)) {
            LOG.error("The tenant ID of the device [{}] is [{}], which is inconsistent with the authorized tenant ID [{}]", deviceNo, deviceInfo.getTenantId(), tenantId);
            return false;
        }
        return true;
    }

    /**
     * 检查是否有设备最新升级任务
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult checkDeviceLastUpgradeTask(String bizContent) {
        // json转换
        JSONObject parseObject = null;
        try {
            parseObject = JSON.parseObject(bizContent);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        String msg = null;
        // 设备编码不能为空
        String deviceNo = (String)parseObject.get("deviceNo");
        if (StringUtils.isBlank(deviceNo) || deviceNo.length() > 48) {
            msg = MessageUtils.message("device.handler.devicecode.max.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 验证设备是否存在
        DeviceInfo deviceInfo = getClientByDeviceNo(deviceNo);
        if (null == deviceInfo) {
            msg = MessageUtils.message("device.handler.deviceno.not.exists",deviceNo);
            return HttpAjaxResult.businessError(msg);
        }
        // 验证设备租户和授权appKey所属租户是否一致
        if (!validateDeviceTenant(deviceInfo)) {
            msg = MessageUtils.message("base.person.handler.device.tenant.error", deviceNo);
            return HttpAjaxResult.businessError(msg);
        }
        try {
            // 下载版本
            DeviceUpgradeTask task = deviceUpgradeTaskService.checkDeviceLastUpgradeTask(deviceInfo.getId());
            if (null == task) {
                return HttpAjaxResult.httpSuccess(MessageUtils.message("device.handler.no.upgrade.task"), null);
            }
            boolean rollback = Boolean.TRUE.equals(task.getRollbackInstall());
            Map<String, Object> result = Maps.newHashMap();
            result.put("updateVersionName", task.getAppName());
            result.put("updateVersion", task.getAppVersion());
            // 1是降级安装 2是升级安装
            result.put("rollback", rollback ? DictConstants.YesOrNoState.YES : DictConstants.YesOrNoState.NO);
            result.put("fileMd5", task.getMd5());
            result.put("fileSize", task.getFileSize());
            return HttpAjaxResult.httpSuccess(result);
        } catch (CustomException e) {
            LOG.error("Failed to detect device upgrade task", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("Failed to detect device upgrade task", e);
            return HttpAjaxResult.httpError();
        }

    }

    /**
     * 设备版本下载
     * 
     * @param bizContent
     * @param response
     * @return
     */
    public AjaxResult downloadVersion(String bizContent, HttpServletRequest request, HttpServletResponse response) {
        // json转换
        JSONObject parseObject = null;
        try {
            parseObject = JSON.parseObject(bizContent);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        String msg = null;
        // 设备编码不能为空
        String deviceNo = (String)parseObject.get("deviceNo");
        if (StringUtils.isBlank(deviceNo) || deviceNo.length() > 48) {
            msg = MessageUtils.message("device.handler.devicecode.max.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 验证设备是否存在
        DeviceInfo deviceInfo = getClientByDeviceNo(deviceNo);
        if (null == deviceInfo) {
            msg = MessageUtils.message("device.handler.deviceno.not.exists",deviceNo);
            return HttpAjaxResult.businessError(msg);
        }
        // 验证设备租户和授权appKey所属租户是否一致
        if (!validateDeviceTenant(deviceInfo)) {
            msg = MessageUtils.message("base.person.handler.device.tenant.error", deviceNo);
            return HttpAjaxResult.businessError(msg);
        }
        // 版本名称不能为空
        String versionName = (String)parseObject.get("versionName");
        if (StringUtils.isBlank(versionName) || versionName.length() > 48) {
            msg = MessageUtils.message("device.handler.versionname.max.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 当前程序版本号不能为空
        String version = (String)parseObject.get("version");
        if (StringUtils.isBlank(version) || version.length() > 48) {
            msg = MessageUtils.message("device.handler.version.max.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        try {
            // 下载版本
            deviceUpgradeTaskService.downloadVersion(versionName, version, deviceNo, request, response);
            return HttpAjaxResult.httpSuccess();
        } catch (CustomException e) {
            LOG.error("Device version download failed", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("Device version download failed", e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 升级结果回传
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult postbackUpgradeResult(String bizContent) {
        // json转换
        JSONObject parseObject = null;
        try {
            parseObject = JSON.parseObject(bizContent);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        String msg = null;
        // 设备编码不能为空
        String deviceNo = (String)parseObject.get("deviceNo");
        if (StringUtils.isBlank(deviceNo) || deviceNo.length() > 48) {
            msg = MessageUtils.message("device.handler.devicecode.max.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 验证设备是否存在
        DeviceInfo deviceInfo = getClientByDeviceNo(deviceNo);
        if (null == deviceInfo) {
            msg = MessageUtils.message("device.handler.deviceno.not.exists",deviceNo);
            return HttpAjaxResult.businessError(msg);
        }
        // 验证设备租户和授权appKey所属租户是否一致
        if (!validateDeviceTenant(deviceInfo)) {
            msg = MessageUtils.message("base.person.handler.device.tenant.error", deviceNo);
            return HttpAjaxResult.businessError(msg);
        }
        LOG.debug("Write back the device upgrade result information, deviceNo:{}", deviceNo);
        // 版本名称不能为空
        String versionName = (String)parseObject.get("versionName");
        if (StringUtils.isBlank(versionName) || versionName.length() > 48) {
            msg = MessageUtils.message("device.handler.versionname.max.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 升级版本号不能为空
        String updateVersion = (String)parseObject.get("updateVersion");
        if (StringUtils.isBlank(updateVersion) || updateVersion.length() > 48) {
            msg = MessageUtils.message("device.handler.updateversion.max.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 升级前版本号不能为空
        String beforeVersion = (String)parseObject.get("beforeVersion");
        if (StringUtils.isNotBlank(beforeVersion) && beforeVersion.length() > 48) {
            msg = MessageUtils.message("device.handler.beforeversion.max.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 更新结果不能为空
        String updateResultStr = (String)parseObject.get("updateResult");
        if (StringUtils.isBlank(updateResultStr)) {
            msg = MessageUtils.message("device.handler.update.result.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        boolean updateResult = Boolean.parseBoolean(updateResultStr);
        // 更新开始时间不能为空
        String startTime = (String)parseObject.get("startTime");
        if (StringUtils.isBlank(startTime)) {
            msg = MessageUtils.message("device.handler.starttime.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        try {
            DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, startTime);
        } catch (Exception e) {
            LOG.error("Update start time [startTime] malformed: {}", e.getMessage());
            msg = MessageUtils.message("device.handler.starttime.format.error");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 更新耗时
        String timeUsed = (String)parseObject.get("timeUsed");
        if (StringUtils.isBlank(timeUsed)) {
            msg = MessageUtils.message("device.handler.update.timeused.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        if (!LongValidator.getInstance().isValid(timeUsed)) {
            msg = MessageUtils.message("device.handler.update.timeused.format.error");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 失败原因
        String failReason = (String)parseObject.get("failReason");
        try {
            deviceUpgradeTaskService.saveUpgradeResult(versionName, beforeVersion, updateVersion, deviceNo,
                updateResult, startTime, Long.valueOf(timeUsed), failReason);
            return HttpAjaxResult.httpSuccess();
        } catch (CustomException e) {
            LOG.error("Failed to upload device upgrade result", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("Failed to upload device upgrade result", e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 查询设备列表
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult queryDevice(String bizContent) {
        // json转换
        JSONObject parseObject;
        try {
            parseObject = JSON.parseObject(bizContent);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        String msg = null;
        // 场景编码不能为空
        String channelCode = (String)parseObject.get("channelCode");
        if (StringUtils.isBlank(channelCode)) {
            msg = MessageUtils.message("bio.trade.handler.scenecode.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }

        String nextPage = parseObject.getString("nextPage");
        if (StringUtils.isBlank(nextPage)) {
            msg  =MessageUtils.message("device.handler.nextpage.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        try {
            Integer.parseInt(nextPage);
        } catch (Exception ex) {
            msg  =MessageUtils.message("device.handler.nextpage.format.error");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        try {
            String deviceNo = parseObject.getString("deviceNo");
            int pageNum = Integer.parseInt(nextPage);// 分页
            if (pageNum == 0) {
                pageNum = 1;
            }
            int pageSize = 10;// 分页数量， 一次更新10条
            String orderBy = SqlUtil.escapeOrderBySql("update_time desc");
            PageHelper.startPage(pageNum, pageSize, orderBy);
            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.setChannelCode(channelCode);
            deviceInfo.setDeviceNo(deviceNo);
            List<DeviceInfo> devInfos = deviceInfoService.selectDeviceInfoList(deviceInfo);
            long total = new PageInfo<DeviceInfo>(devInfos).getTotal();// 数据总量
            JSONObject rst = new JSONObject();
            rst.put("total", String.valueOf(total));
            rst.put("currentPage", String.valueOf(nextPage));
            boolean hasMore = false;

            JSONArray list = new JSONArray();
            if (!CollectionUtils.isEmpty(devInfos)) {
                if (total > pageNum * pageSize) {
                    hasMore = true;
                }
                for (DeviceInfo info : devInfos) {
                    JSONObject obj = new JSONObject();
                    obj.put("deviceNo", info.getDeviceNo());
                    obj.put("deviceName", info.getDeviceName());
                    obj.put("deviceModeCode", info.getDeviceModelCode());
                    // 此处型号名称也暂时返回型号编码即可
                    obj.put("deviceModeName", info.getDeviceModelCode());
                    obj.put("deviceState", info.getDeviceState());
                    obj.put("deviceIp", info.getDeviceIp());
                    obj.put("deviceAddr", info.getDeviceAddr());
                    obj.put("longtitude", info.getLatitude());
                    obj.put("latitude", info.getLatitude());
                    obj.put("deviceType", info.getDeviceType());
                    list.add(obj);
                }
            }
            rst.put("hasMore", String.valueOf(hasMore));
            rst.put("list", list);
            return HttpAjaxResult.httpSuccess(rst);
        } catch (Exception e) {
            LOG.error("Device query failed", e);
            return HttpAjaxResult.httpError(e.getMessage());
        }
    }
}
