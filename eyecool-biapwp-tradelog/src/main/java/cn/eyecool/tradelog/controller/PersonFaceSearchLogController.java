package cn.eyecool.tradelog.controller;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.eyecool.common.annotation.Log;
import cn.eyecool.common.config.EyecoolConfig;
import cn.eyecool.common.constant.Constants;
import cn.eyecool.common.context.LoginUserContextHolder;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.model.LoginUser;
import cn.eyecool.common.core.page.TableDataInfo;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.enums.BusinessType;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.AESUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.ListUtil;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.FileUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.common.utils.file.ZipEntryFileModel;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.tradelog.domain.PersonFaceSearchLog;
import cn.eyecool.tradelog.domain.PersonHealthCodeLog;
import cn.eyecool.tradelog.service.IPersonFaceSearchLogService;
import cn.eyecool.tradelog.service.IPersonHealthCodeLogService;
import lombok.extern.slf4j.Slf4j;

/**
 * 人脸搜索日志Controller
 * 
 * @author admin
 * @date 2021-04-29
 */
@Slf4j
@RestController
@RequestMapping("/tradelog/facesearch")
public class PersonFaceSearchLogController extends BaseController {

    @Autowired
    private IPersonFaceSearchLogService personFaceSearchLogService;
    @Autowired
    private IPersonHealthCodeLogService healthCodeLogService;
    @Autowired
    private RedisCache redisCache;

    /**
     * 查询人脸搜索日志列表
     */
    @PreAuthorize("@ss.hasPermi('tradelog:facesearch:list')")
    @GetMapping("/list")
    public TableDataInfo list(PersonFaceSearchLog personFaceSearchLog) {
        startPage();
        List<PersonFaceSearchLog> list = personFaceSearchLogService.selectPersonFaceSearchLogList(personFaceSearchLog);
        list.stream().forEach(it -> {
            if (StringUtils.isNotBlank(it.getSceneImage())) {
                it.setSceneImageId(AESUtils.encryptAES(it.getSceneImage()));
            }
        });
        return getDataTable(list);
    }

    /**
     * 导出人脸搜索日志列表
     */
    @PreAuthorize("@ss.hasPermi('tradelog:facesearch:export')")
    @Log(title = "tradelog.face.search.title", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(PersonFaceSearchLog personFaceSearchLog, Boolean exportLatestOnly) {
        String taskId = "Export-" + IdWorker.getNextStringId();
        String tenantId = TenantContextHolder.getTenantId();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
            AjaxResult.success(StringUtils.EMPTY), 12, TimeUnit.HOURS);
        int logCount = personFaceSearchLogService.selectPersonFaceSearchLogCount(personFaceSearchLog);
        // TODO 临时屏蔽大数据量导出，后期优化导出功能
        if (logCount > Constants.EXPORT_SIZE_LIMIT) {
            throw new CustomException(MessageUtils.message("tradelog.face.search.export.large"));
        }
        CompletableFuture.runAsync(() -> {
            long startTime = System.currentTimeMillis();
            log.info("Start exporting face search logs,startTime:[{}], tenantId:[{}], params:[{}]", startTime, tenantId,
                personFaceSearchLog.toString());
            TenantContextHolder.setTenantId(tenantId);
            LoginUserContextHolder.setLoginUser(loginUser);
            List<PersonFaceSearchLog> list = Collections.emptyList();
            if (Boolean.TRUE.equals(exportLatestOnly)) {
                list = personFaceSearchLogService.selectLastPersonFaceSearchLogList(personFaceSearchLog);
            } else {
                list = personFaceSearchLogService.selectPersonFaceSearchLogList(personFaceSearchLog);
            }
            int size = list.size();
            log.info("After exporting the face search log query,tenantId:[{}], usedTime:[{}], count:[{}]", tenantId,
                System.currentTimeMillis() - startTime, size);
            String fileName = null;
            ExcelUtil<PersonFaceSearchLog> util = new ExcelUtil<PersonFaceSearchLog>(PersonFaceSearchLog.class);
            if (size > Constants.EXPORT_SIZE_LIMIT) {
                List<List<PersonFaceSearchLog>> groupList =
                    ListUtil.groupListByUnitSize(Constants.EXPORT_SIZE_LIMIT, list);
                List<ZipEntryFileModel> entryList = new CopyOnWriteArrayList<>();
                List<String> filePaths = new CopyOnWriteArrayList<>();
                groupList.parallelStream().forEach(subList -> {
                    AjaxResult result = util.exportExcel(subList, "facesearch");
                    String singleExcelFileName = (String)result.get(AjaxResult.MSG_TAG);
                    String filePath = EyecoolConfig.getDownloadPath() + singleExcelFileName;
                    ZipEntryFileModel fileModel = new ZipEntryFileModel(new File(filePath), singleExcelFileName, false);
                    entryList.add(fileModel);
                    filePaths.add(filePath);
                });
                fileName = IdWorker.getNextStringId() + "_facesearch.zip";
                File zipFile = new File(EyecoolConfig.getDownloadPath() + fileName);
                PlatformFileUtils.zipFiles(entryList, zipFile);
                CompletableFuture.runAsync(() -> {
                    filePaths.stream().forEach(it -> {
                        FileUtils.deleteFile(it);
                    });
                });
            } else {
                AjaxResult result = util.exportExcel(list, "facesearch");
                fileName = (String)result.get(AjaxResult.MSG_TAG);
            }
            redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
                AjaxResult.success(fileName), 12, TimeUnit.HOURS);
            long endTime = System.currentTimeMillis();
            log.info("End of exporting face search log,endTime:[{}], tenantId:[{}], usedTime:[{}ms]", endTime, tenantId,
                endTime - startTime);
        });
        return AjaxResult.success(taskId);
    }

    /**
     * 获取人脸搜索日志详细信息
     */
    @PreAuthorize("@ss.hasPermi('tradelog:facesearch:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        PersonFaceSearchLog faceSearchLog = personFaceSearchLogService.selectPersonFaceSearchLogById(id);
        if (StringUtils.isNotBlank(faceSearchLog.getSceneImage())) {
            String stringBase64 = PlatformFileUtils.getImageBase64(faceSearchLog.getSceneImage());
            if (StringUtils.isNotBlank(stringBase64)) {
                stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                faceSearchLog.setSceneImageBase64(stringBase64);
            }
        }
        if (StringUtils.isNotBlank(faceSearchLog.getStockImage())) {
            String stringBase64 = PlatformFileUtils.getImageBase64(faceSearchLog.getStockImage());
            if (StringUtils.isNotBlank(stringBase64)) {
                stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                faceSearchLog.setStockImageBase64(stringBase64);
            }
        }
        if (StringUtils.isNotBlank(faceSearchLog.getTakePhoto1())) {
            String stringBase64 = PlatformFileUtils.getImageBase64(faceSearchLog.getTakePhoto1());
            if (StringUtils.isNotBlank(stringBase64)) {
                stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                faceSearchLog.setTakePhoto1Base64(stringBase64);
            }
        }
        if (StringUtils.isNotBlank(faceSearchLog.getTakePhoto2())) {
            String stringBase64 = PlatformFileUtils.getImageBase64(faceSearchLog.getTakePhoto2());
            if (StringUtils.isNotBlank(stringBase64)) {
                stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                faceSearchLog.setTakePhoto2Base64(stringBase64);
            }
        }
        if (StringUtils.isNotBlank(faceSearchLog.getHealthcodeLogId())) {
            PersonHealthCodeLog healthCodeLog =
                healthCodeLogService.selectPersonHealthCodeLogById(faceSearchLog.getHealthcodeLogId());
            faceSearchLog.setHealthCodeLog(healthCodeLog);
        }
        return AjaxResult.success(faceSearchLog);
    }

}
