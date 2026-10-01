package cn.eyecool.basedata.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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

import cn.eyecool.basedata.domain.BasePersonCert;
import cn.eyecool.basedata.service.IBasePersonCertService;
import cn.eyecool.common.annotation.Log;
import cn.eyecool.common.constant.Constants;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.page.TableDataInfo;
import cn.eyecool.common.core.redis.RedisCache;
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
 * 人员证件信息Controller
 * 
 * @author mawj
 * @date 2021-01-27
 */
@RestController
@RequestMapping("/basedata/cert")
@Slf4j
public class BasePersonCertController extends BaseController {
    @Autowired
    private IBasePersonCertService basePersonCertService;
    @Autowired
    private RedisCache redisCache;

    /**
     * 查询人员证件信息列表
     */
    @PreAuthorize("@ss.hasPermi('basedata:cert:list')")
    @GetMapping("/list")
    public TableDataInfo list(BasePersonCert basePersonCert) {
        startPage();
        List<BasePersonCert> list = basePersonCertService.selectBasePersonCertList(basePersonCert);
        return getDataTable(list);
    }

    /**
     * 导出人员证件信息列表
     */
    @PreAuthorize("@ss.hasPermi('basedata:cert:export')")
    @Log(title = "base.person.identity.information", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(BasePersonCert basePersonCert) {
        List<BasePersonCert> list = basePersonCertService.selectBasePersonCertList(basePersonCert);
        ExcelUtil<BasePersonCert> util = new ExcelUtil<BasePersonCert>(BasePersonCert.class);
        return util.exportExcel(list, "cert");
    }

    /**
     * 获取人员证件信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('basedata:cert:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        BasePersonCert basePersonCert = basePersonCertService.selectBasePersonCertById(id);
        if (StringUtils.isNotBlank(basePersonCert.getCertImg())) {
            String stringBase64 = PlatformFileUtils.getImageBase64(basePersonCert.getCertImg());
            if (DictConstants.Encrypted.ENABLE.equals(basePersonCert.getEncrypted())) {
                stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
            }
            basePersonCert.setImgBase64(stringBase64);
        }
        return AjaxResult.success(basePersonCert);
    }

    /**
     * 新增人员证件信息
     */
    @PreAuthorize("@ss.hasPermi('basedata:cert:add')")
    @Log(title = "base.person.identity.information", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BasePersonCert basePersonCert) {
        return toAjax(basePersonCertService.insertBasePersonCert(basePersonCert));
    }

    /**
     * 修改人员证件信息
     */
    @PreAuthorize("@ss.hasPermi('basedata:cert:edit')")
    @Log(title = "base.person.identity.information", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BasePersonCert basePersonCert) {
        return toAjax(basePersonCertService.updateBasePersonCert(basePersonCert));
    }

    /**
     * 删除人员证件信息
     */
    @PreAuthorize("@ss.hasPermi('basedata:cert:remove')")
    @Log(title = "base.person.identity.information", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(basePersonCertService.deleteBasePersonCertByIds(ids));
    }

    /**
     * 下载用户证件照
     */
    @PreAuthorize("@ss.hasPermi('basedata:cert:download')")
    @Log(title = "base.person.identity.information", businessType = BusinessType.OTHER)
    @GetMapping("/download")
    public AjaxResult download(BasePersonCert basePersonCert, String photoType) {
        String zipFileName = basePersonCertService.downloadImages(basePersonCert, photoType);
        return AjaxResult.success(zipFileName);
    }

    /**
     * 导入模板下载
     * 
     * @return
     */
    @GetMapping("/importTemplate")
    public AjaxResult importTemplate() {
        ExcelUtil<BasePersonCert> util = new ExcelUtil<BasePersonCert>(BasePersonCert.class);
        return util.importTemplateExcel(MessageUtils.message("base.person.identity.information.data"));
    }

    /**
     * 保存批量导入
     * 
     * @throws Exception
     * @throws IOException
     */
    @PreAuthorize("@ss.hasPermi('basedata:cert:import')")
    @Log(title = "base.person.identity.information", businessType = BusinessType.IMPORT)
    @PostMapping("/import")
    public AjaxResult saveImportData(@RequestParam(value = "excelFile", required = false) MultipartFile excelFile,
        @RequestParam(value = "zipFile", required = false) MultipartFile zipFile, String photoType, String certType,
        Boolean updateSupport) throws IOException, Exception {
        if (null == excelFile && zipFile == null) {
            throw new CustomException(MessageUtils.message("base.person.choose.excel.zip"));
        }
        if (null != excelFile) {
            String excelFilename = excelFile.getOriginalFilename();
            String excelSuffix = null;
            if (StringUtils.isNotBlank(excelFilename)) {
                excelSuffix = excelFilename.substring(excelFilename.lastIndexOf(".") + 1);
            }
            if (!"xls".equalsIgnoreCase(excelSuffix) && !"xlsx".equalsIgnoreCase(excelSuffix)) {
                throw new CustomException(MessageUtils.message("base.person.choose.excel"));
            }
        }
        if (null != zipFile) {
            String zipFilename = zipFile.getOriginalFilename();
            String zipSuffix = null;
            if (StringUtils.isNotBlank(zipFilename)) {
                zipSuffix = zipFilename.substring(zipFilename.lastIndexOf(".") + 1);
            }
            if (!"zip".equalsIgnoreCase(zipSuffix)) {
                throw new CustomException(MessageUtils.message("base.person.choose.zip"));
            }
        }
        String taskId = "ImportCertInfo-" + IdWorker.getNextStringId();
        String tenantId = TenantContextHolder.getTenantId();
        InputStream excelIStream = null == excelFile ? null : excelFile.getInputStream();
        InputStream zipIStream = null == zipFile ? null : zipFile.getInputStream();
        redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
            AjaxResult.success(MessageUtils.message("base.person.task.in.progress")), 7, TimeUnit.DAYS);
        final SecurityContext context = SecurityContextHolder.getContext();
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            SecurityContextHolder.setContext(context);
            try {
                String message =
                    basePersonCertService.saveImportData(excelIStream, zipIStream, photoType, certType, updateSupport);
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
