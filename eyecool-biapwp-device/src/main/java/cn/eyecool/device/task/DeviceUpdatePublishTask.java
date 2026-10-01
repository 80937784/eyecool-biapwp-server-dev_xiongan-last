package cn.eyecool.device.task;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.mqtt.PlatformMqttClientUtil;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.spring.SpringUtils;
import cn.eyecool.device.constant.MqttTopicConstants;
import cn.eyecool.device.domain.DeviceInfo;
import cn.eyecool.device.domain.DeviceUpgradeTask;
import cn.eyecool.device.event.ECF203UpgradeTaskEventPublishService;
import cn.eyecool.device.mapper.DeviceInfoMapper;
import cn.eyecool.device.mapper.DeviceUpgradeTaskMapper;
import cn.eyecool.device.proto.EdgeProto;
import cn.eyecool.device.proto.EdgeProto.UpdateMessage;
import cn.eyecool.device.proto.EdgeProto.UpdateMessage.UpdateType;
import lombok.extern.slf4j.Slf4j;

/**
 * 设备升级任务发布定时任务
 * 
 * @author admin
 * @date 2020年7月21日
 */
@Component("deviceUpdatePublishTask")
@Slf4j
public class DeviceUpdatePublishTask {

    @Autowired
    private DeviceInfoMapper deviceInfoMapper;
    @Autowired
    private DeviceUpgradeTaskMapper deviceUpgradeTaskMapper;
    @Autowired
    private ECF203UpgradeTaskEventPublishService ecf203UpgradeTaskEventPublishService;
    @Value("${mqtt.enabled}")
    private boolean mqttEnabled;
    @Value("${eyecool.serverDomain}")
    private String serverDomain;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * 执行定时任务
     */
    public void execute() {
        // 设备升级任务不需要租户隔离，统管所有设备，此处为防止线程池拿到的线程携带租户信息，先进行一下租户清理
        TenantContextHolder.clear();
        PlatformMqttClientUtil clientUtil = null;
        if (mqttEnabled) {
            clientUtil = SpringUtils.getBean(PlatformMqttClientUtil.class);
            // 检查是否连接，没有则建立连接
            clientUtil.connect();
        }
        long total = 0; // 数据总量
        int pageNum = 1;// 分页
        int pageSize = 500;// 分页数量
        // 序列号原子对象
        do {
            PageHelper.startPage(pageNum, pageSize);
            List<DeviceUpgradeTask> taskList = deviceUpgradeTaskMapper.selectToBeAutoPublishedTaskList();
            for (DeviceUpgradeTask task : taskList) {
                DeviceInfo deviceInfo = deviceInfoMapper.selectDeviceInfoById(task.getDeviceId());
                if ("ECF203".equalsIgnoreCase(deviceInfo.getDeviceModelCode())) {
                    log.info("203 The device [{}] upgrade task is automatically released", deviceInfo.getDeviceNo());
                    publisECF203Task(task);
                } else if (mqttEnabled) {
                    log.info("Equipment [{}] upgrade task Mqtt automatically released", deviceInfo.getDeviceNo());
                    execMqttPublish(task, clientUtil);
                }
            }
            total = new PageInfo<DeviceUpgradeTask>(taskList).getTotal();
            pageNum++;
        } while ((pageNum - 1) * pageSize < total);
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
     * 执行Mqtt发布升级任务
     * 
     * @param task
     * @param clientUtil
     */
    private void execMqttPublish(DeviceUpgradeTask task, PlatformMqttClientUtil clientUtil) {
        // 设置升级下载URL
        String updateUrl = serverDomain + contextPath + "/api/standard";
        boolean rollback = Boolean.TRUE.equals(task.getRollbackInstall());
        UpdateMessage updateMessage = EdgeProto.UpdateMessage.newBuilder().setUpdateUrl(updateUrl)
            .setUpdateVersion(task.getAppVersion()).setUpdateVersionName(task.getAppName())
            .setUpdateType(rollback ? UpdateType.DEMOTE : UpdateType.UPGRADE).setFileMd5(task.getMd5()).build();
        clientUtil.publishMessage(MqttTopicConstants.PUB_DEVICE_UPGRADE_SIGNAL_TOPIC_PREFIX + task.getDeviceNo(),
            updateMessage.toByteArray(), 2, false, false);
        // 更新设备发布次数
        task.setUpdateTime(DateUtils.getNowDate());
        task.setPubCount(task.getPubCount() + 1);
        deviceUpgradeTaskMapper.updateDeviceUpgradeTask(task);
    }
}
