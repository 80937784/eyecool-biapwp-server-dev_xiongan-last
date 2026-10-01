package cn.eyecool.device.service.impl;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.core.text.Convert;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.mqtt.PlatformMqttClientUtil;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.bean.BeanUtils;
import cn.eyecool.common.utils.file.FileUtils;
import cn.eyecool.common.utils.spring.SpringUtils;
import cn.eyecool.device.constant.MqttTopicConstants;
import cn.eyecool.device.domain.DeviceActionLog;
import cn.eyecool.device.domain.DeviceInfo;
import cn.eyecool.device.domain.DeviceUpgradeLog;
import cn.eyecool.device.domain.DeviceUpgradeTask;
import cn.eyecool.device.domain.DeviceUpgradeVersion;
import cn.eyecool.device.event.ECF203UpgradeTaskEventPublishService;
import cn.eyecool.device.mapper.DeviceActionLogMapper;
import cn.eyecool.device.mapper.DeviceInfoMapper;
import cn.eyecool.device.mapper.DeviceUpgradeLogMapper;
import cn.eyecool.device.mapper.DeviceUpgradeTaskMapper;
import cn.eyecool.device.mapper.DeviceUpgradeVersionMapper;
import cn.eyecool.device.proto.EdgeProto;
import cn.eyecool.device.proto.EdgeProto.UpdateMessage;
import cn.eyecool.device.proto.EdgeProto.UpdateMessage.UpdateType;
import cn.eyecool.device.service.IDeviceUpgradeTaskService;
import lombok.extern.slf4j.Slf4j;

/**
 * 升级任务Service业务层处理
 * 
 * @author admin
 * @date 2021-04-08
 */
@Service
@Slf4j
public class DeviceUpgradeTaskServiceImpl implements IDeviceUpgradeTaskService {

    /** 设备升级任务排序索引缓存KEY */
    private static final String DEVICE_UPGRADE_TASK_INDEX_CACHE_KEY = "device:upgrade_task_index";

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
    @Autowired
    private ECF203UpgradeTaskEventPublishService ecf203UpgradeTaskEventPublishService;

    @Value("${mqtt.enabled}")
    private boolean mqttEnabled;
    @Value("${eyecool.serverDomain}")
    private String serverDomain;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    private static final Object lockObj = new Object();

    /**
     * 查询升级任务
     * 
     * @param id 升级任务ID
     * @return 升级任务
     */
    @Override
    public DeviceUpgradeTask selectDeviceUpgradeTaskById(String id) {
        return deviceUpgradeTaskMapper.selectDeviceUpgradeTaskById(id);
    }

    /**
     * 查询升级任务列表
     * 
     * @param deviceUpgradeTask 升级任务
     * @return 升级任务
     */
    @Override
    public List<DeviceUpgradeTask> selectDeviceUpgradeTaskList(DeviceUpgradeTask deviceUpgradeTask) {
        return deviceUpgradeTaskMapper.selectDeviceUpgradeTaskList(deviceUpgradeTask);
    }

    /**
     * 新增升级任务
     * 
     * @param deviceUpgradeTask 升级任务
     * @return 结果
     */
    @Override
    @Transactional
    public int insertDeviceUpgradeTask(DeviceUpgradeTask deviceUpgradeTask) {
        int result = 0;
        String[] deviceIds = Convert.toStrArray(deviceUpgradeTask.getDeviceId());
        DeviceUpgradeTask task = null;
        for (String deviceId : deviceIds) {
            task = new DeviceUpgradeTask();
            BeanUtils.copyBeanProp(task, deviceUpgradeTask);
            task.setDeviceId(deviceId);
            task.setId(IdWorker.getNextStringId());
            try {
                task.setCreateBy(SecurityUtils.getUsername());
            } catch (Exception e) {
            }
            task.setCreateTime(DateUtils.getNowDate());
            // 此处放到redis实现原子处理
            task.setTaskIndex(incrementAndGetTaskIndex());
            task.setExecuteCount(0L);
            task.setExecuteResult(DictConstants.DeviceUpgradeTaskResult.TO_BE_EXECUTE);
            result += deviceUpgradeTaskMapper.insertDeviceUpgradeTask(task);
            // 自动生成一条升级日志记录
            addUpgradeLog(task);
        }
        return result;
    }

    /**
     * 查询最大升级任务排序
     * 
     * @return
     */
    private Long incrementAndGetTaskIndex() {
        Long taskIndex = redisCache.incrementAndGet(DEVICE_UPGRADE_TASK_INDEX_CACHE_KEY);
        if (null != taskIndex) {
            if (log.isDebugEnabled()) {
                log.debug("Get the current largest device upgrade task sorting index from the cache:[{}]", taskIndex);
            }
            return taskIndex;
        }
        synchronized (lockObj) {
            taskIndex = redisCache.incrementAndGet(DEVICE_UPGRADE_TASK_INDEX_CACHE_KEY);
            if (null == taskIndex) {
                Long maxTaskIndex = deviceUpgradeTaskMapper.selectMaxTaskIndex();
                if (null == maxTaskIndex) {
                    maxTaskIndex = 1L;
                }
                taskIndex = redisCache.addAndGetLong(DEVICE_UPGRADE_TASK_INDEX_CACHE_KEY, maxTaskIndex);
            }
        }
        return taskIndex;
    }

    /**
     * 添加升级日志
     * 
     * @param task
     */
    private void addUpgradeLog(DeviceUpgradeTask task) {
        DeviceInfo deviceInfo = deviceInfoMapper.selectDeviceInfoById(task.getDeviceId());
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
     * 修改升级任务
     * 
     * @param deviceUpgradeTask 升级任务
     * @return 结果
     */
    @Override
    @Transactional
    public int updateDeviceUpgradeTask(DeviceUpgradeTask deviceUpgradeTask) {
        deviceUpgradeTask.setUpdateTime(DateUtils.getNowDate());
        String executeResult = deviceUpgradeTask.getExecuteResult();
        if (DictConstants.DeviceUpgradeTaskResult.SKIP_UPGRAD.equals(executeResult)) {
            // 跳过任务则将待下载和待升级日志状态设置为跳过
            deviceUpgradeLogMapper.skipDeviceUpgradeLogByTaskIds(new String[] {deviceUpgradeTask.getId()},
                DateUtils.getNowDate());
        }
        return deviceUpgradeTaskMapper.updateDeviceUpgradeTask(deviceUpgradeTask);
    }

    /**
     * 批量删除升级任务
     * 
     * @param ids 需要删除的升级任务ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteDeviceUpgradeTaskByIds(String[] ids) {
        int result = 0;
        for (String id : ids) {
            result += deleteDeviceUpgradeTaskById(id);
        }
        return result;
    }

    /**
     * 删除升级任务信息
     * 
     * @param id 升级任务ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteDeviceUpgradeTaskById(String id) {
        // 删除任务则将待下载和待升级日志状态设置为跳过
        deviceUpgradeLogMapper.skipDeviceUpgradeLogByTaskIds(new String[] {id}, DateUtils.getNowDate());
        return deviceUpgradeTaskMapper.deleteDeviceUpgradeTaskById(id);
    }

    /**
     * 保存设备升级结果
     * 
     * @param versionName
     * @param beforeVersion
     * @param updateVersion
     * @param deviceNo
     * @param updateResult
     * @param startTime
     * @param timeUsed
     * @param failReason
     */
    @Override
    @Transactional
    public void saveUpgradeResult(String versionName, String beforeVersion, String updateVersion, String deviceNo,
        boolean updateResult, String startTime, long timeUsed, String failReason) {
        // 查询设备是否存在
        DeviceInfo deviceInfo = getDeviceInfo(deviceNo);
        if (null == deviceInfo) {
            throw new CustomException(MessageUtils.message("device.upgrade.device.not.exists", deviceNo));
        }
        // 查询版本是否存在
        DeviceUpgradeVersion versionInfo = getDeviceVersionInfo(versionName, updateVersion);
        if (null == versionInfo) {
            throw new CustomException(MessageUtils.message("device.upgrade.version.not.exists"));
        }
        // 查询升级任务
        DeviceUpgradeTask condition = new DeviceUpgradeTask();
        condition.setDeviceId(deviceInfo.getId());
        condition.setExecuteResult(DictConstants.DeviceUpgradeTaskResult.TO_BE_EXECUTE);
        List<DeviceUpgradeTask> taskList = deviceUpgradeTaskMapper.selectDeviceUpgradeTaskList(condition);
        if (CollectionUtils.isEmpty(taskList)) {
            throw new CustomException(MessageUtils.message("device.upgrade.task.not.exists"));
        }
        // 当前需要回写结果的任务
        DeviceUpgradeTask task = taskList.stream()
            .filter(it -> it.getVersionId().equals(versionInfo.getId()) && it.getDeviceId().equals(deviceInfo.getId())
                && DictConstants.DeviceUpgradeTaskResult.TO_BE_EXECUTE.equals(it.getExecuteResult()))
            .findAny().orElse(null);
        if (null == task) {
            throw new CustomException(MessageUtils.message("device.upgrade.task.not.exists"));
        }
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
            log.setClientTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, startTime));
            log.setBeforeAppName(versionName);
            log.setBeforeVersion(beforeVersion);
            log.setTimeUsed(timeUsed);
            log.setServerTime(new Date(System.currentTimeMillis() - timeUsed));
            log.setFailReason(failReason);
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
            addUpgradeLog(task);
            // 再次发布一次任务
            if (mqttEnabled) {
                // 设置升级下载URL
                PlatformMqttClientUtil mqttClientUtil = SpringUtils.getBean(PlatformMqttClientUtil.class);
                String updateUrl = serverDomain + contextPath + "/api/standard";
                boolean rollback = Boolean.TRUE.equals(task.getRollbackInstall());
                UpdateMessage updateMessage = EdgeProto.UpdateMessage.newBuilder().setUpdateUrl(updateUrl)
                    .setUpdateVersion(task.getAppVersion()).setUpdateVersionName(task.getAppName())
                    .setUpdateType(rollback ? UpdateType.DEMOTE : UpdateType.UPGRADE).setFileMd5(task.getMd5()).build();
                mqttClientUtil.publishMessage(MqttTopicConstants.PUB_DEVICE_UPGRADE_SIGNAL_TOPIC_PREFIX + deviceNo,
                    updateMessage.toByteArray(), 2, false, false);
                task.setPubCount(task.getPubCount() + 1);
            }
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

    /**
     * 查询设备信息
     * 
     * @param deviceNo
     * @return
     */
    private DeviceInfo getDeviceInfo(String deviceNo) {
        DeviceInfo device = new DeviceInfo();
        device.setDeviceNo(deviceNo);
        List<DeviceInfo> deviceInfos = deviceInfoMapper.selectDeviceInfoList(device);
        return CollectionUtils.isEmpty(deviceInfos) ? null : deviceInfos.get(0);
    }

    /**
     * 查询版本信息
     * 
     * @param versionName
     * @param version
     * @return
     */
    private DeviceUpgradeVersion getDeviceVersionInfo(String versionName, String version) {
        DeviceUpgradeVersion deviceVerion = new DeviceUpgradeVersion();
        deviceVerion.setVersion(version);
        deviceVerion.setAppName(versionName);
        List<DeviceUpgradeVersion> versionList =
            deviceUpgradeVersionMapper.selectDeviceUpgradeVersionList(deviceVerion);
        return CollectionUtils.isEmpty(versionList) ? null : versionList.get(0);
    }

    /**
     * 下载程序版本
     */
    @Override
    public void downloadVersion(String versionName, String version, String deviceNo, HttpServletRequest request,
        HttpServletResponse response) {
        // 查询设备是否存在
        DeviceInfo deviceInfo = getDeviceInfo(deviceNo);
        if (null == deviceInfo) {
            throw new CustomException(MessageUtils.message("device.upgrade.device.not.exists", deviceNo));
        }
        // 查询版本是否存在
        DeviceUpgradeVersion versionInfo = getDeviceVersionInfo(versionName, version);
        if (null == versionInfo) {
            throw new CustomException(MessageUtils.message("device.upgrade.version.not.exists"));
        }
        if (Boolean.FALSE.equals(versionInfo.getEnabled())) {
            throw new CustomException(MessageUtils.message("device.upgrade.version.disabled"));
        }
        // 查询升级任务是否存在
        DeviceUpgradeTask condition = new DeviceUpgradeTask();
        condition.setVersionId(versionInfo.getId());
        condition.setDeviceId(deviceInfo.getId());
        condition.setExecuteResult(DictConstants.DeviceUpgradeTaskResult.TO_BE_EXECUTE);
        List<DeviceUpgradeTask> taskList = deviceUpgradeTaskMapper.selectDeviceUpgradeTaskList(condition);
        if (CollectionUtils.isEmpty(taskList)) {
            throw new CustomException(MessageUtils.message("device.upgrade.task.not.exists.not.allow.download"));
        }
        DeviceUpgradeTask taskInfo = taskList.get(0);
        if (taskInfo.getExecuteCount() >= taskInfo.getUpgradeCountLimit()) {
            // TODO 预留监控报警
            throw new CustomException(MessageUtils.message("device.upgrade.multi.failed.not.allow.download", taskInfo.getExecuteCount()));
        }
        String path = versionInfo.getPath();
        response.setContentType("multipart/form-data");
        try {
            response.setHeader("Content-Disposition",
                "attachment;filename=" + FileUtils.setFileDownloadHeader(request, versionInfo.getFilename()));
            FileUtils.writeBytes(path, response.getOutputStream());
        } catch (Exception e) {
            log.error("APP version download error!", e);
            throw new CustomException(e.getMessage());
        }
        // 查询更新日志
        DeviceUpgradeLog upgradeLog = new DeviceUpgradeLog();
        upgradeLog.setTaskId(taskInfo.getId());
        upgradeLog.setUpgradeStatus(DictConstants.DeviceUpgradeStatus.TO_BE_DOWNLOAD);
        List<DeviceUpgradeLog> logList = deviceUpgradeLogMapper.selectDeviceUpgradeLogList(upgradeLog);
        // 修改日志的更新状态
        if (CollectionUtils.isEmpty(logList)) {
            return;
        }
        DeviceUpgradeLog log = logList.get(0);
        log.setUpgradeStatus(DictConstants.DeviceUpgradeStatus.TO_BE_UPGRADE);
        deviceUpgradeLogMapper.updateDeviceUpgradeLog(log);

    }

    /**
     * 检测设备升级任务
     * 
     * @param deviceId
     * @param filterExecuted
     * @return
     */
    @Override
    public DeviceUpgradeTask checkDeviceLastUpgradeTask(String deviceId) {
        DeviceUpgradeTask taskCondition = new DeviceUpgradeTask();
        taskCondition.setDeviceId(deviceId);
        taskCondition.setExecuteResult(DictConstants.DeviceUpgradeTaskResult.TO_BE_EXECUTE);
        List<DeviceUpgradeTask> taskList = deviceUpgradeTaskMapper.selectDeviceUpgradeTaskList(taskCondition);
        if (CollectionUtils.isEmpty(taskList)) {
            return null;
        }
        DeviceUpgradeTask task = taskList.stream()
            .sorted(Comparator.comparing(DeviceUpgradeTask::getTaskIndex).reversed()).findFirst().orElse(null);
        return task;
    }

    /**
     * 升级任务发布
     * 
     * @param id
     * @return
     */
    @Override
    public int publish(String id) {
        DeviceUpgradeTask task = deviceUpgradeTaskMapper.selectDeviceUpgradeTaskById(id);
        if (null != task.getUpgradeTime() && task.getUpgradeTime().getTime() > System.currentTimeMillis()) {
            throw new CustomException(MessageUtils.message("device.upgrade.time.not.yet.now.allow.publish"));
        }
        DeviceInfo deviceInfo = deviceInfoMapper.selectDeviceInfoById(task.getDeviceId());
        if ("ECF203".equalsIgnoreCase(deviceInfo.getDeviceModelCode())) {
            return publisECF203Task(task);
        }
        return publishMqttTask(task);
    }

    /**
     * 203升级任务发布
     * 
     * @param task
     * @return
     */
    private int publisECF203Task(DeviceUpgradeTask task) {
        // 发布203升级事件
        ecf203UpgradeTaskEventPublishService.publish(task.getId(), task.getDeviceNo(), task.getVersionId(), null);
        // 修改发布次数
        task.setPubCount(task.getPubCount() + 1);
        task.setUpdateTime(DateUtils.getNowDate());
        return deviceUpgradeTaskMapper.updateDeviceUpgradeTask(task);
    }

    /**
     * Mqtt升级任务发布
     * 
     * @param task
     * @return
     */
    private int publishMqttTask(DeviceUpgradeTask task) {
        if (!mqttEnabled) {
            throw new CustomException(MessageUtils.message("device.upgrade.not.find.mqtt"));
        }
        PlatformMqttClientUtil mqttClientUtil = SpringUtils.getBean(PlatformMqttClientUtil.class);
        // 设置升级下载URL
        String updateUrl = serverDomain + contextPath + "/api/standard";
        boolean rollback = Boolean.TRUE.equals(task.getRollbackInstall());
        UpdateMessage updateMessage = EdgeProto.UpdateMessage.newBuilder().setUpdateUrl(updateUrl)
            .setUpdateVersion(task.getAppVersion()).setUpdateVersionName(task.getAppName())
            .setUpdateType(rollback ? UpdateType.DEMOTE : UpdateType.UPGRADE).setFileMd5(task.getMd5()).build();
        boolean res = mqttClientUtil.publishMessage(
            MqttTopicConstants.PUB_DEVICE_UPGRADE_SIGNAL_TOPIC_PREFIX + task.getDeviceNo(), updateMessage.toByteArray(),
            2, false, false);
        if (!res) {
            throw new CustomException(MessageUtils.message("device.upgrade.publish.failed"));
        }
        // 修改发布次数
        task.setPubCount(task.getPubCount() + 1);
        task.setUpdateTime(DateUtils.getNowDate());
        return deviceUpgradeTaskMapper.updateDeviceUpgradeTask(task);
    }
}
