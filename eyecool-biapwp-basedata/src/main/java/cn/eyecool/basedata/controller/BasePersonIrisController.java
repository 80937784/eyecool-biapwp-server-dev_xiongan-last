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

import cn.eyecool.basedata.domain.BasePersonIris;
import cn.eyecool.basedata.service.IBasePersonIrisService;
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
 * 虹膜图像信息Controller
 * 
 * @author mawj
 * @date 2021-01-27
 */
@RestController
@RequestMapping("/basedata/iris")
@Slf4j
public class BasePersonIrisController extends BaseController {
    @Autowired
    private IBasePersonIrisService basePersonIrisService;
    @Autowired
    private RedisCache redisCache;

    /**
     * 查询虹膜图像信息列表
     */
    @PreAuthorize("@ss.hasPermi('basedata:iris:list')")
    @GetMapping("/list")
    public TableDataInfo list(BasePersonIris basePersonIris) {
        startPage();
        List<BasePersonIris> list = basePersonIrisService.selectBasePersonIrisList(basePersonIris);
        return getDataTable(list);
    }

    /**
     * 导出虹膜图像信息列表
     */
    @PreAuthorize("@ss.hasPermi('basedata:iris:export')")
    @Log(title = "base.person.iris.image.information", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(BasePersonIris basePersonIris) {
        List<BasePersonIris> list = basePersonIrisService.selectBasePersonIrisList(basePersonIris);
        ExcelUtil<BasePersonIris> util = new ExcelUtil<BasePersonIris>(BasePersonIris.class);
        return util.exportExcel(list, "iris");
    }

    /**
     * 获取虹膜图像信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('basedata:iris:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        BasePersonIris basePersonIris = basePersonIrisService.selectBasePersonIrisById(id);
        if (StringUtils.isNotBlank(basePersonIris.getImageUrl())) {
            String stringBase64 = PlatformFileUtils.getImageBase64(basePersonIris.getImageUrl());
            if (StringUtils.isNotBlank(stringBase64)
                && DictConstants.Encrypted.ENABLE.equals(basePersonIris.getEncrypted())) {
                stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
            }
            basePersonIris.setImgBase64(stringBase64);
        }
        return AjaxResult.success(basePersonIris);
    }

    /**
     * 新增虹膜图像信息
     */
    @PreAuthorize("@ss.hasPermi('basedata:iris:add')")
    @Log(title = "base.person.iris.image.information", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BasePersonIris basePersonIris) {
        return toAjax(basePersonIrisService.insertBasePersonIris(basePersonIris));
    }

    /**
     * 修改虹膜图像信息
     */
    @PreAuthorize("@ss.hasPermi('basedata:iris:edit')")
    @Log(title = "base.person.iris.image.information", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BasePersonIris basePersonIris) {
        return toAjax(basePersonIrisService.updateBasePersonIris(basePersonIris));
    }

    /**
     * 删除虹膜图像信息
     */
    @PreAuthorize("@ss.hasPermi('basedata:iris:remove')")
    @Log(title = "base.person.iris.image.information", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(basePersonIrisService.deleteBasePersonIrisByIds(ids));
    }

    /**
     * 一键更新所有虹膜特征
     */
    @PreAuthorize("@ss.hasPermi('basedata:iris:updatefeature')")
    @Log(title = "base.person.iris.image.information", businessType = BusinessType.UPDATE)
    @PutMapping("/updatefeature")
    public AjaxResult updateAllIrisFeature(String algsVersion) {
        String taskId = "UpdateIrisFeature-" + IdWorker.getNextStringId();
        String tenantId = TenantContextHolder.getTenantId();
        redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
            AjaxResult.success(MessageUtils.message("base.person.task.in.progress")), 7, TimeUnit.DAYS);
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            try {
                AjaxResult ajaxResult = basePersonIrisService.batchUpdateIrisFeature(algsVersion);
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
     * 下载虹膜图片
     */
    @PreAuthorize("@ss.hasPermi('basedata:iris:download')")
    @Log(title = "base.person.iris.image.information", businessType = BusinessType.OTHER)
    @GetMapping("/download")
    public AjaxResult downloadImages(BasePersonIris basePersonIris, String ids) {
        if (StringUtils.isNotBlank(ids)) {
            basePersonIris.getParams().put("ids", Arrays.asList(Convert.toStrArray(ids)));
        }
        String zipFileName = basePersonIrisService.downloadImages(basePersonIris);
        return AjaxResult.success(zipFileName);
    }

    /**
     * 保存批量导入
     * 
     * @throws Exception
     * @throws IOException
     */
    @PreAuthorize("@ss.hasPermi('basedata:iris:import')")
    @Log(title = "base.person.iris.image.information", businessType = BusinessType.IMPORT)
    @PostMapping("/import")
    public AjaxResult saveImportData(@RequestParam(value = "zipFile") MultipartFile zipFile)
        throws IOException, Exception {
        String originalFilename = zipFile.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)) {
            log.error("the uploaded iris image zip file name is empty.");
            throw new CustomException(MessageUtils.message("base.person.iris.file.name.empty"));
        }
        if (!originalFilename.endsWith("zip")) {
            log.error("The format of upload file is not zip." + originalFilename);
            throw new CustomException(MessageUtils.message("base.person.iris.file.format.wrong", originalFilename));
        }
        String taskId = "UploadIrisImg-" + IdWorker.getNextStringId();
        String tenantId = TenantContextHolder.getTenantId();
        InputStream inputStream = zipFile.getInputStream();
        redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
            AjaxResult.success(MessageUtils.message("base.person.task.in.progress")), 7, TimeUnit.DAYS);
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            try {
                String message = basePersonIrisService.saveImportData(inputStream);
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
