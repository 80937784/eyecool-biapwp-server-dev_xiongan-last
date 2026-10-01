package cn.eyecool.basedata.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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

import cn.eyecool.basedata.domain.BasePersonFinger;
import cn.eyecool.basedata.service.IBasePersonFingerService;
import cn.eyecool.common.annotation.Log;
import cn.eyecool.common.constant.Constants;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.page.TableDataInfo;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.core.text.Convert;
import cn.eyecool.common.enums.BusinessType;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.common.utils.poi.ExcelUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 指纹图像信息Controller
 * 
 * @author mawj
 * @date 2021-01-27
 */
@RestController
@RequestMapping("/basedata/finger")
@Slf4j
public class BasePersonFingerController extends BaseController {

    @Autowired
    private IBasePersonFingerService basePersonFingerService;
    @Autowired
    private RedisCache redisCache;

    /**
     * 查询指纹图像信息列表
     */
    @PreAuthorize("@ss.hasPermi('basedata:finger:list')")
    @GetMapping("/list")
    public TableDataInfo list(BasePersonFinger basePersonFinger) {
        startPage();
        List<BasePersonFinger> list = basePersonFingerService.selectBasePersonFingerList(basePersonFinger);
        return getDataTable(list);
    }

    /**
     * 导出指纹图像信息列表
     */
    @PreAuthorize("@ss.hasPermi('basedata:finger:export')")
    @Log(title = "base.person.finger.image.information", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(BasePersonFinger basePersonFinger) {
        List<BasePersonFinger> list = basePersonFingerService.selectBasePersonFingerList(basePersonFinger);
        ExcelUtil<BasePersonFinger> util = new ExcelUtil<BasePersonFinger>(BasePersonFinger.class);
        return util.exportExcel(list, "finger");
    }

    /**
     * 获取指纹图像信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('basedata:finger:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        BasePersonFinger basePersonFinger = basePersonFingerService.selectBasePersonFingerById(id);
        if (StringUtils.isNotBlank(basePersonFinger.getImageUrl())) {
            String stringBase64 = PlatformFileUtils.getImageBase64(basePersonFinger.getImageUrl());
            if (StringUtils.isNotBlank(stringBase64)
                && DictConstants.Encrypted.ENABLE.equals(basePersonFinger.getEncrypted())) {
                stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
            }
            basePersonFinger.setImgBase64(stringBase64);
        }
        return AjaxResult.success(basePersonFinger);
    }

    /**
     * 新增指纹图像信息
     */
    @PreAuthorize("@ss.hasPermi('basedata:finger:add')")
    @Log(title = "base.person.finger.image.information", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BasePersonFinger basePersonFinger) {
        if (StringUtils.isBlank(basePersonFinger.getImgBase64())) {
            return AjaxResult.error(MessageUtils.message("base.person.finger.image.not.allow.null"));
        }
        return toAjax(basePersonFingerService.insertBasePersonFinger(basePersonFinger));
    }

    /**
     * 修改指纹图像信息
     */
    @PreAuthorize("@ss.hasPermi('basedata:finger:edit')")
    @Log(title = "base.person.finger.image.information", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BasePersonFinger basePersonFinger) {
        return toAjax(basePersonFingerService.updateBasePersonFinger(basePersonFinger));
    }

    /**
     * 删除指纹图像信息
     */
    @PreAuthorize("@ss.hasPermi('basedata:finger:remove')")
    @Log(title = "base.person.finger.image.information", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(basePersonFingerService.deleteBasePersonFingerByIds(ids));
    }

    /**
     * 一键更新所有指纹特征
     */
    @PreAuthorize("@ss.hasPermi('basedata:finger:updatefeature')")
    @Log(title = "base.person.finger.image.information", businessType = BusinessType.UPDATE)
    @PutMapping("/updatefeature")
    public AjaxResult updateAllFingerFeature(String algsVersion) {
        String taskId = "UpdateFingerFeature-" + IdWorker.getNextStringId();
        String tenantId = TenantContextHolder.getTenantId();
        redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
            AjaxResult.success(MessageUtils.message("base.person.task.in.progress")), 7, TimeUnit.DAYS);
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            try {
                AjaxResult ajaxResult = basePersonFingerService.batchUpdateFingerFeature(algsVersion);
                redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId, ajaxResult, 7,
                    TimeUnit.DAYS);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
                    AjaxResult.error(e.getMessage()), 7, TimeUnit.DAYS);
            }
        });
        return AjaxResult.success(taskId);
    }

    /**
     * 下载指纹图片
     */
    @PreAuthorize("@ss.hasPermi('basedata:finger:download')")
    @Log(title = "base.person.finger.image.information", businessType = BusinessType.OTHER)
    @GetMapping("/download")
    public AjaxResult downloadImages(BasePersonFinger basePersonFinger, String ids) {
        if (StringUtils.isNotBlank(ids)) {
            basePersonFinger.getParams().put("ids", Arrays.asList(Convert.toStrArray(ids)));
        }
        String zipFileName = basePersonFingerService.downloadImages(basePersonFinger);
        return AjaxResult.success(zipFileName);
    }

    /**
     * 保存批量导入
     * 
     * @throws Exception
     * @throws IOException
     */
    @PreAuthorize("@ss.hasPermi('basedata:finger:import')")
    @Log(title = "base.person.finger.image.information", businessType = BusinessType.IMPORT)
    @PostMapping("/import")
    public AjaxResult saveImportData(@RequestParam(value = "zipFile") MultipartFile zipFile)
        throws IOException, Exception {
        String originalFilename = zipFile.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)) {
            log.error("the upload face image zip file name is empty.");
            throw new CustomException(MessageUtils.message("base.person.finger.file.name.empty"));
        }
        if (!originalFilename.endsWith("zip")) {
            log.error("The format of the input file is not zip." + originalFilename);
            throw new CustomException(MessageUtils.message("base.person.finger.file.format.wrong", originalFilename));
        }
        String taskId = "UploadFingerImg-" + IdWorker.getNextStringId();
        String tenantId = TenantContextHolder.getTenantId();
        InputStream inputStream = zipFile.getInputStream();
        redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
            AjaxResult.success(MessageUtils.message("base.person.task.in.progress")), 7, TimeUnit.DAYS);
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            try {
                String message = basePersonFingerService.saveImportData(inputStream);
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
}
