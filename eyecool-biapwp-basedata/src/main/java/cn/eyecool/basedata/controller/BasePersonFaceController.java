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

import cn.eyecool.basedata.domain.BasePersonFace;
import cn.eyecool.basedata.service.IBasePersonFaceService;
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
import cn.eyecool.common.enums.PersonTypeEnum;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.common.utils.poi.ExcelUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 人脸图像信息Controller
 * 
 * @author mawj
 * @date 2021-01-27
 */
@RestController
@RequestMapping("/basedata/face")
@Slf4j
public class BasePersonFaceController extends BaseController {

    @Autowired
    private IBasePersonFaceService basePersonFaceService;
    @Autowired
    private RedisCache redisCache;

    /**
     * 查询人脸图像信息列表
     */
    @PreAuthorize("@ss.hasPermi('basedata:face:list')")
    @GetMapping("/list")
    public TableDataInfo list(BasePersonFace basePersonFace) {
        startPage();
        basePersonFace.setPersonType(PersonTypeEnum.USER.value());
        List<BasePersonFace> list = basePersonFaceService.selectBasePersonFaceList(basePersonFace);
        return getDataTable(list);
    }

    /**
     * 导出人脸图像信息列表
     */
    @PreAuthorize("@ss.hasPermi('basedata:face:export')")
    @Log(title = "base.person.face.image.information", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(BasePersonFace basePersonFace) {
        List<BasePersonFace> list = basePersonFaceService.selectBasePersonFaceList(basePersonFace);
        ExcelUtil<BasePersonFace> util = new ExcelUtil<BasePersonFace>(BasePersonFace.class);
        return util.exportExcel(list, "face");
    }

    /**
     * 获取人脸图像信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('basedata:face:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        BasePersonFace basePersonFace = basePersonFaceService.selectBasePersonFaceById(id);
        if (StringUtils.isNotBlank(basePersonFace.getImageUrl())) {
            String stringBase64 = PlatformFileUtils.getImageBase64(basePersonFace.getImageUrl());
            if (StringUtils.isNotBlank(stringBase64)
                && DictConstants.Encrypted.ENABLE.equals(basePersonFace.getEncrypted())) {
                stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
            }
            basePersonFace.setImgBase64(stringBase64);
        }
        return AjaxResult.success(basePersonFace);
    }

    /**
     * 新增人脸图像信息
     */
    @PreAuthorize("@ss.hasPermi('basedata:face:add')")
    @Log(title = "base.person.face.image.information", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BasePersonFace basePersonFace) {
        if (StringUtils.isBlank(basePersonFace.getImgBase64())) {
            return AjaxResult.error(MessageUtils.message("base.person.face.image.not.allow.null"));
        }
        return toAjax(basePersonFaceService.insertBasePersonFace(basePersonFace));
    }

    /**
     * 修改人脸图像信息
     */
    @PreAuthorize("@ss.hasPermi('basedata:face:edit')")
    @Log(title = "base.person.face.image.information", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BasePersonFace basePersonFace) {
        return toAjax(basePersonFaceService.updateBasePersonFace(basePersonFace));
    }

    /**
     * 删除人脸图像信息
     */
    @PreAuthorize("@ss.hasPermi('basedata:face:remove')")
    @Log(title = "base.person.face.image.information", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(basePersonFaceService.deleteBasePersonFaceByIds(ids));
    }

    /**
     * 一键更新所有人脸特征
     */
    @PreAuthorize("@ss.hasPermi('basedata:face:updatefeature')")
    @Log(title = "base.person.face.image.information", businessType = BusinessType.UPDATE)
    @PutMapping("/updatefeature")
    public AjaxResult updateAllFaceFeature(String algsVersion) {
        String taskId = "UpdateFaceFeature-" + IdWorker.getNextStringId();
        String tenantId = TenantContextHolder.getTenantId();
        redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
            AjaxResult.success(MessageUtils.message("base.person.task.in.progress")), 7, TimeUnit.DAYS);
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            try {
                AjaxResult ajaxResult = basePersonFaceService.batchUpdateFaceFeature(algsVersion);
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
     * 下载人脸图片
     */
    @PreAuthorize("@ss.hasPermi('basedata:face:download')")
    @Log(title = "base.person.face.image.information", businessType = BusinessType.OTHER)
    @GetMapping("/download")
    public AjaxResult downloadImages(BasePersonFace basePersonFace, String ids) {
        if (StringUtils.isNotBlank(ids)) {
            basePersonFace.getParams().put("ids", Arrays.asList(Convert.toStrArray(ids)));
        }
        String zipFileName = basePersonFaceService.downloadImages(basePersonFace);
        return AjaxResult.success(zipFileName);
    }

    /**
     * 保存批量导入
     * 
     * @throws Exception
     * @throws IOException
     */
    @PreAuthorize("@ss.hasPermi('basedata:face:import')")
    @Log(title = "base.person.face.image.information", businessType = BusinessType.IMPORT)
    @PostMapping("/import")
    public AjaxResult saveImportData(@RequestParam(value = "zipFile") MultipartFile zipFile)
        throws IOException, Exception {
        String originalFilename = zipFile.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)) {
            log.error("the upload face image zip file name is empty.");
            throw new CustomException(MessageUtils.message("base.person.face.file.name.empty"));
        }
        if (!originalFilename.endsWith("zip")) {
            log.error("The format of the input file is not zip." + originalFilename);
            throw new CustomException(MessageUtils.message("base.person.face.file.format.wrong", originalFilename));
        }
        String taskId = "UploadFaceImg-" + IdWorker.getNextStringId();
        String tenantId = TenantContextHolder.getTenantId();
        InputStream inputStream = zipFile.getInputStream();
        redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
            AjaxResult.success(MessageUtils.message("base.person.task.in.progress")), 7, TimeUnit.DAYS);
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            try {
                String message = basePersonFaceService.saveImportData(inputStream);
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
