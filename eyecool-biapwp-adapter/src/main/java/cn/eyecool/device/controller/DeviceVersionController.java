package cn.eyecool.device.controller;

import java.net.URLEncoder;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.AESUtils;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.file.FileUtils;
import cn.eyecool.device.constant.AdapterConstants;
import cn.eyecool.device.domain.DeviceUpgradeLog;
import cn.eyecool.device.event.ECF203UpgradeTaskEventAttrs;
import cn.eyecool.device.service.IDeviceUpgradeLogService;
import lombok.extern.slf4j.Slf4j;

/**
 * 设备版本下载请求控制
 * 
 * @author mawj
 * @date 2021/10/29
 */
@RestController
@RequestMapping("/api/device/version")
@Slf4j
public class DeviceVersionController {

    @Autowired
    private RedisCache redisCache;
    @Autowired
    private IDeviceUpgradeLogService deviceUpgradeLogService;

    /**
     * 版本下载
     * 
     * @param encryptFlag
     * @param request
     * @param response
     */
    @GetMapping(value = "/download/{encryptFlag}")
    public void downloadVersion(@PathVariable("encryptFlag") String encryptFlag, HttpServletRequest request,
        HttpServletResponse response) {
        try {
            String flag = AESUtils.decryptAES(new String(Base64.getDecoder().decode(encryptFlag)));
            String[] array = flag.split(":");
            String taskId = array[0];
            String deviceNo = array[1];
            ECF203UpgradeTaskEventAttrs eventAttrs =
                redisCache.getCacheObject(AdapterConstants.DEVICE_UPGRADE_TASK_CACHE_PREFIX + encryptFlag);
            if (null == eventAttrs) {
                log.error("The upgrade task [{}],the link has been expired for device [{}],you need republish it again!", taskId,deviceNo);
                
                throw new CustomException(MessageUtils.message("device.upgrade.download.link.expired"));
            }
            String filePath = eventAttrs.getFilePath();
            String fileName = eventAttrs.getOriginalFileName();
            Long fileSize = eventAttrs.getFileSize();
            // 配置文件下载
            response.setHeader("Content-Length", String.valueOf(fileSize));
            response.setHeader("content-type", "application/octet-stream");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));

            FileUtils.writeBytes(filePath, response.getOutputStream());
            // 更新升级任务日志状态
            CompletableFuture.runAsync(() -> {
                updateUpgradeLogStatus(taskId);
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new CustomException(MessageUtils.message("device.upgrade.version.download.failed.msg") + e.getMessage());
        }
    }

    /**
     * 更新升级日志
     * 
     * @param taskId
     */
    private void updateUpgradeLogStatus(String taskId) {
        // 查询更新日志
        DeviceUpgradeLog upgradeLog = new DeviceUpgradeLog();
        upgradeLog.setTaskId(taskId);
        upgradeLog.setUpgradeStatus(DictConstants.DeviceUpgradeStatus.TO_BE_DOWNLOAD);
        List<DeviceUpgradeLog> logList = deviceUpgradeLogService.selectDeviceUpgradeLogList(upgradeLog);
        // 修改日志的更新状态
        if (CollectionUtils.isEmpty(logList)) {
            return;
        }
        DeviceUpgradeLog log = logList.get(0);
        log.setUpgradeStatus(DictConstants.DeviceUpgradeStatus.TO_BE_UPGRADE);
        log.setServerTime(DateUtils.getNowDate());
        deviceUpgradeLogService.updateDeviceUpgradeLog(log);
    }

}
