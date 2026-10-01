package cn.eyecool.device.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;

import cn.eyecool.device.domain.DeviceUpgradeVersion;
import cn.eyecool.device.service.IDeviceUpgradeVersionService;
import lombok.extern.slf4j.Slf4j;

/**
 * 203设备升级任务事件发布服务
 * 
 * @author mawj
 * @date 2021/01/06
 */
@Service
@Slf4j
public class ECF203UpgradeTaskEventPublishService implements ApplicationEventPublisherAware {

    private ApplicationEventPublisher publisher;
    @Autowired
    private IDeviceUpgradeVersionService deviceUpgradeVersionService;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publish(String taskId, String deviceNo, String versionId, IECF203UpgradeEventCallback callback) {
        DeviceUpgradeVersion upgradeVersion = deviceUpgradeVersionService.selectDeviceUpgradeVersionById(versionId);
        if (null == upgradeVersion) {
            log.warn("The device version with the version ID [{}] corresponding to the upgrade task of the device [{}] does not exist");
            return;
        }
        ECF203UpgradeTaskEventAttrs attrs = new ECF203UpgradeTaskEventAttrs(taskId, deviceNo, upgradeVersion.getPath(),
            upgradeVersion.getFileSize(), upgradeVersion.getMd5(), upgradeVersion.getFilename(),
            upgradeVersion.getAppName(), upgradeVersion.getVersion());
        this.publish(attrs, callback);
    }

    /**
     * 发布事件
     * 
     * @param attrs
     * @param callback
     */
    public void publish(ECF203UpgradeTaskEventAttrs attrs, IECF203UpgradeEventCallback callback) {
        if (null == callback) {
            callback = new IECF203UpgradeEventCallback() {

                @Override
                public void onError(String errmsg) {
                    log.error("203 Device upgrade event release processing error:[{}]", errmsg);
                }

                @Override
                public void onSuccess() {
                    log.info("203 The device upgrade event is released and processed successfully");
                }
            };
        }
        publisher.publishEvent(new EFC203UpgradeTaskEvent(this, attrs, callback));
    }
}