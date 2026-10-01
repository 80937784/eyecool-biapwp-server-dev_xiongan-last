package cn.eyecool.device.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.device.controller.WebsocketController;
import lombok.extern.slf4j.Slf4j;

/**
 * 定时任务调度
 * 
 * @author 段存明
 */
@Slf4j
@Component("syncTask")
public class SyncTask {

    @Autowired
    private WebsocketController websocket;
    private volatile boolean running = false;

    /**
     * 执行同步任务
     *
     * syncAllDevice
     */
    public void syncAllDevice() {
        log.info("task executed");
        if (running) {
            log.info("the 203 device synchronization task is executing...... ");
            return;
        }
        running = true;
        log.info("the 203 device synchronization task ,  start time:[{}]", DateUtils.dateTimeNow());
        try {
            websocket.syncAllDev();
        } catch (Exception e) {
            log.error("the device synchronization task is abnormal：[{}] ", e.toString());
        } finally {
            running = false;
        }
        log.info("the 203 device synchronization task ,  end time:[{}]", DateUtils.dateTimeNow());
    }
}
