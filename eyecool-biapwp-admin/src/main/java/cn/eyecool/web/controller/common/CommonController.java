package cn.eyecool.web.controller.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cn.eyecool.common.config.EyecoolConfig;
import cn.eyecool.common.constant.Constants;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.FileUploadUtils;
import cn.eyecool.common.utils.file.FileUtils;
import cn.eyecool.framework.config.ServerConfig;

/**
 * 通用请求处理
 * 
 * @author admin
 */
@RestController
public class CommonController {
    private static final Logger log = LoggerFactory.getLogger(CommonController.class);

    @Autowired
    private ServerConfig serverConfig;
    @Autowired
    private RedisCache redisCache;

    /**
     * 通用下载请求
     * 
     * @param fileName 文件名称
     * @param delete 是否删除
     */
    @GetMapping("common/download")
    public void fileDownload(String fileName, Boolean delete, HttpServletResponse response,
        HttpServletRequest request) {
        try {
            if (!FileUtils.checkAllowDownload(fileName)) {
                String msg = MessageUtils.message("file.name.invalid", fileName);
                throw new Exception(msg);
            }
            String realFileName = System.currentTimeMillis() + fileName.substring(fileName.indexOf("_") + 1);
            String filePath = EyecoolConfig.getDownloadPath() + fileName;

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, realFileName);
            FileUtils.writeBytes(filePath, response.getOutputStream());
            if (delete) {
                FileUtils.deleteFile(filePath);
            }
        } catch (Exception e) {
            log.error("下载文件失败", e);
        }
    }

    /**
     * 通用上传请求
     */
    @PostMapping("/common/upload")
    public AjaxResult uploadFile(MultipartFile file) throws Exception {
        try {
            // 上传文件路径
            String filePath = EyecoolConfig.getUploadPath();
            // 上传并返回新文件名称
            String fileName = FileUploadUtils.upload(filePath, file);
            String url = serverConfig.getUrl() + fileName;
            AjaxResult ajax = AjaxResult.success();
            ajax.put("fileName", fileName);
            ajax.put("url", url);
            return ajax;
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 本地资源通用下载
     */
    @GetMapping("/common/download/resource")
    public void resourceDownload(String resource, HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        try {
            if (!FileUtils.checkAllowDownload(resource)) {
                String msg = MessageUtils.message("file.name.invalid", resource);
                throw new Exception(msg);
            }
            // 本地资源路径
            String localPath = EyecoolConfig.getProfile();
            // 数据库资源地址
            String downloadPath = localPath + StringUtils.substringAfter(resource, Constants.RESOURCE_PREFIX);
            // 下载名称
            String downloadName = StringUtils.substringAfterLast(downloadPath, "/");
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, downloadName);
            FileUtils.writeBytes(downloadPath, response.getOutputStream());
        } catch (Exception e) {
            log.error("下载文件失败", e);
        }
    }

    /**
     * 本地资源通用下载
     */
    @GetMapping("/common/asynctask/result/{taskId}")
    public AjaxResult asynctaskResult(@PathVariable("taskId") String taskId) {
        AjaxResult taskResult =
            (AjaxResult)redisCache.getCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId);
        if (null == taskResult) {
            String msg = MessageUtils.message("task.not.exist", taskId);
            return AjaxResult.error(msg);
        }
        return AjaxResult.success(taskResult);
    }
}
