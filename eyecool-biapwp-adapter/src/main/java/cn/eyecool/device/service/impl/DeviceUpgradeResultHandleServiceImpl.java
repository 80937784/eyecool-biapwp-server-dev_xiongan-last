package cn.eyecool.device.service.impl;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.websocket.Session;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.device.constant.AdapterConstants;
import cn.eyecool.device.domain.DeviceActionLog;
import cn.eyecool.device.domain.DeviceInfo;
import cn.eyecool.device.domain.DeviceUpgradeLog;
import cn.eyecool.device.domain.DeviceUpgradeTask;
import cn.eyecool.device.domain.DeviceUpgradeVersion;
import cn.eyecool.device.mapper.DeviceActionLogMapper;
import cn.eyecool.device.mapper.DeviceInfoMapper;
import cn.eyecool.device.mapper.DeviceUpgradeLogMapper;
import cn.eyecool.device.mapper.DeviceUpgradeTaskMapper;
import cn.eyecool.device.mapper.DeviceUpgradeVersionMapper;
import cn.eyecool.device.service.IDeviceUpgradeResultHandleService;
import lombok.extern.slf4j.Slf4j;

/**
 * 设备升级结果处理服务
 * 
 * @author mawj
 * @date 2021/10/29
 */
@Service
@Slf4j
public class DeviceUpgradeResultHandleServiceImpl implements IDeviceUpgradeResultHandleService {

    @Autowired
    private DeviceUpgradeTaskMapper deviceUpgradeTaskMapper;
    @Autowired
    private DeviceUpgradeLogMapper deviceUpgradeLogMapper;
    @Autowired
    private DeviceInfoMapper deviceInfoMapper;
    @Autowired
    private DeviceUpgradeVersionMapper deviceUpgradeVersionMapper;
    @Autowired
    private DeviceActionLogMapper deviceActionLogMapper;
    @Autowired
    private RedisCache redisCache;

    /**
     * 保存升级结果
     * 
     * @param session
     * @param message
     */
    @Override
    public void updateDeviceUpgradeResult(Session session, String message) {
        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(message);
        } catch (Exception e) {
            log.error("解析设备端消息json格式出错, message:[{}], error:[{}]", message, e.getMessage(), e);
            return;
        }
        Boolean result = jsonObject.getBoolean("result");
        if (!Boolean.TRUE.equals(result)) {
            return;
        }
        Integer id = jsonObject.getInteger("id");
        if (null == id)
            return;
        // 值按照回复编号ID、升级任务ID、设备编码顺序通过:拼接字符串存入redis，等设备端有webssocket回复后，根据会话ID取出信息，进行任务升级结果判断和保存
        String cacheValue = redisCache.getCacheObject(AdapterConstants.DEVICE_VERSION_QUERY_CACHE_PREFIX + id);
        if (StringUtils.isBlank(cacheValue)) {
            return;
        }
        // 删除获取设备版本信息redis会话缓存
        redisCache.deleteObject(AdapterConstants.DEVICE_VERSION_QUERY_CACHE_PREFIX + id);
        String[] array = cacheValue.split(":");
        @SuppressWarnings("unused")
        String cacheId = array[0];
        String cacheTaskId = array[1];
        String cacheDeviceNo = array[2];
        JSONObject params = jsonObject.getJSONObject("params");
        JSONObject softwareVersion = params.getJSONObject("softwareVersion");
        String version = softwareVersion.getString("version");
        DeviceUpgradeTask task = deviceUpgradeTaskMapper.selectDeviceUpgradeTaskById(cacheTaskId);
        boolean updateResult = task.getAppVersion().equals(version);
        updateUpgradeTaskStatus(cacheTaskId, cacheDeviceNo, updateResult, DateUtils.getNowDate());
    }

    /**
     * 更新升级任务结果
     * 
     * @param taskId
     * @param deviceNo
     * @param updateResult
     * @param upgradeTime
     */
    private void updateUpgradeTaskStatus(String taskId, String deviceNo, boolean updateResult, Date upgradeTime) {
        DeviceUpgradeTask taskCondition = new DeviceUpgradeTask();
        taskCondition.setDeviceNo(deviceNo);
        taskCondition.setExecuteResult(DictConstants.DeviceUpgradeTaskResult.TO_BE_EXECUTE);
        List<DeviceUpgradeTask> taskList = deviceUpgradeTaskMapper.selectDeviceUpgradeTaskList(taskCondition);
        if (CollectionUtils.isEmpty(taskList)) {
            log.warn("the device [{}] has no pending upgrade tasks, do not need to update task results", deviceNo);
            return;
        }
        DeviceUpgradeTask task = taskList.stream().filter(it -> it.getId().equals(taskId)).findFirst().orElse(null);
        if (null == task) {
            log.warn("the device [{}] has no pending upgrade task with the task ID of [{}], and the task result does not need to be updated", deviceNo, taskId);
            return;
        }
        DeviceInfo deviceInfo = deviceInfoMapper.selectDeviceInfoById(task.getDeviceId());
        // 更新需要跳过的任务Id列表
        List<String> skipTaskIdList = taskList.stream().filter(it -> it.getTaskIndex() < task.getTaskIndex())
            .map(DeviceUpgradeTask::getId).collect(Collectors.toList());
        // 跳过之前的未执行的升级任务日志和升级任务
        if (CollectionUtils.isNotEmpty(skipTaskIdList)) {
            deviceUpgradeLogMapper.skipDeviceUpgradeLogByTaskIds(skipTaskIdList.toArray(new String[] {}),
                DateUtils.getNowDate());
            deviceUpgradeTaskMapper.skipTaskByTaskIds(skipTaskIdList.toArray(new String[] {}), DateUtils.getNowDate());
        }
        // 更新本次升级任务对应日志信息
        DeviceUpgradeLog logCondition = new DeviceUpgradeLog();
        logCondition.setDeviceNo(deviceNo);
        logCondition.setTaskId(task.getId());
        List<DeviceUpgradeLog> logList = deviceUpgradeLogMapper.selectDeviceUpgradeLogList(logCondition);
        if (CollectionUtils.isNotEmpty(logList)) {
            DeviceUpgradeLog log = logList.get(0);
            log.setUpgradeStatus(updateResult ? DictConstants.DeviceUpgradeStatus.UPGRADE_SUCCESS
                : DictConstants.DeviceUpgradeStatus.UPGRADE_FAIL);
            log.setClientTime(upgradeTime);
            // log.setBeforeAppName(versionName);
            // log.setBeforeVersion(beforeVersion);
            log.setTimeUsed(0L);
            log.setServerTime(DateUtils.getNowDate());
            // log.setFailReason(failReason);
            deviceUpgradeLogMapper.updateDeviceUpgradeLog(log);
        }
        // 更新本次任务结果和执行次数
        task.setUpdateTime(DateUtils.getNowDate());
        task.setExecuteCount(task.getExecuteCount() + 1);
        if (updateResult) {
            task.setExecuteResult(DictConstants.DeviceUpgradeTaskResult.UPGRADE_SUCCESS);
        } else if (task.getUpgradeCountLimit() <= task.getExecuteCount()) {
            task.setExecuteResult(DictConstants.DeviceUpgradeTaskResult.UPGRADE_FAIL);
        } else {
            // 新建任务下一次执行的日志
            addUpgradeLog(task, deviceInfo);
        }
        deviceUpgradeTaskMapper.updateDeviceUpgradeTask(task);
        String tenantId = TenantContextHolder.getTenantId();
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            if (DictConstants.DeviceUpgradeTaskResult.UPGRADE_SUCCESS.equals(task.getExecuteResult())) {
                saveDeviceActionLog(deviceNo, deviceInfo.getDeviceName(), deviceInfo.getDeviceModelCode(),
                    deviceInfo.getChannelCode(), DictConstants.DeviceActionLogType.UPDATE);
            }
        });
    }

    /**
     * 添加升级日志
     * 
     * @param task
     */
    private void addUpgradeLog(DeviceUpgradeTask task, DeviceInfo deviceInfo) {
        DeviceUpgradeVersion versionInfo =
            deviceUpgradeVersionMapper.selectDeviceUpgradeVersionById(task.getVersionId());
        DeviceUpgradeLog upgradeLog = new DeviceUpgradeLog();
        upgradeLog.setTaskId(task.getId());
        upgradeLog.setDeviceName(deviceInfo.getDeviceName());
        upgradeLog.setDeviceNo(deviceInfo.getDeviceNo());
        upgradeLog.setBeforeAppName(null);
        upgradeLog.setBeforeVersion(null);
        upgradeLog.setAfterAppName(versionInfo.getAppName());
        upgradeLog.setAfterVersion(versionInfo.getVersion());
        upgradeLog.setCreateTime(DateUtils.getNowDate());
        upgradeLog.setClientTime(null);
        upgradeLog.setId(IdWorker.getNextStringId());
        upgradeLog.setUpgradeStatus(DictConstants.DeviceUpgradeStatus.TO_BE_DOWNLOAD);
        deviceUpgradeLogMapper.insertDeviceUpgradeLog(upgradeLog);
    }

    /**
     * 保存设备动作日志
     * 
     * @param deviceNo
     * @param deviceName
     * @param modelCode
     * @param channelCode
     * @param actionType
     */
    private void saveDeviceActionLog(String deviceNo, String deviceName, String modelCode, String channelCode,
        String actionType) {
        // 保存设备动作日志
        DeviceActionLog log = new DeviceActionLog();
        log.setDeviceNo(deviceNo);
        log.setDeviceName(deviceName);
        log.setActionType(actionType);
        log.setCreateTime(DateUtils.getNowDate());
        log.setModelCode(modelCode);
        log.setChannelCode(channelCode);
        log.setId(IdWorker.getNextStringId());
        deviceActionLogMapper.insertDeviceActionLog(log);
    }

}
