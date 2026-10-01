package cn.eyecool.tool.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cn.eyecool.common.annotation.Log;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.page.TableDataInfo;
import cn.eyecool.common.enums.BusinessType;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.tool.domain.SysSdkFile;
import cn.eyecool.tool.service.ISysSdkFileService;

/**
 * SDK文件上传Controller
 *
 * @author admin
 * @date 2021-03-31
 */
@RestController
@RequestMapping("/tool/sdkFile")
public class SysSdkFileController extends BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SysSdkFileController.class);
    @Autowired
    private ISysSdkFileService sysSdkFileService;

    /**
     * 查询SDK文件上传列表
     */
    @PreAuthorize("@ss.hasPermi('tool:sdkFile:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysSdkFile sysSdkFile) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            return getDataTable(Collections.emptyList());
        }
        startPage();
        List<SysSdkFile> list = sysSdkFileService.selectSysSdkFileList(sysSdkFile);
        return getDataTable(list);
    }

    /**
     * 导出SDK文件上传列表
     */
    @PreAuthorize("@ss.hasPermi('tool:sdkFile:export')")
    @Log(title = "util.sdk.file.upload", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(SysSdkFile sysSdkFile) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            return AjaxResult.error(MessageUtils.message("sys.job.tenant.no.auth"));
        }
        List<SysSdkFile> list = sysSdkFileService.selectSysSdkFileList(sysSdkFile);
        ExcelUtil<SysSdkFile> util = new ExcelUtil<SysSdkFile>(SysSdkFile.class);
        return util.exportExcel(list, "file");
    }

    /**
     * 获取SDK文件上传详细信息
     */
    @PreAuthorize("@ss.hasPermi('tool:sdkFile:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            return AjaxResult.error(MessageUtils.message("sys.job.tenant.no.auth"));
        }
        return AjaxResult.success(sysSdkFileService.selectSysSdkFileById(id));
    }

    /**
     * 新增SDK文件上传
     */
    @PreAuthorize("@ss.hasPermi('tool:sdkFile:add')")
    @Log(title = "util.sdk.file.upload", businessType = BusinessType.INSERT)
    @PostMapping("/addFileForm")
    public AjaxResult addFileForm(@RequestParam("sdkFile") MultipartFile sdkFile, SysSdkFile sysSdkFile) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            return AjaxResult.error(MessageUtils.message("sys.job.tenant.no.auth"));
        }
        if (sysSdkFile != null) {
            sysSdkFile.setCreateBy(SecurityUtils.getUsername());
        }
        return toAjax(sysSdkFileService.addFileForm(sdkFile, sysSdkFile));
    }

    /**
     * 删除SDK文件上传
     */
    @PreAuthorize("@ss.hasPermi('tool:sdkFile:remove')")
    @Log(title = "util.sdk.file.upload", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            return AjaxResult.error(MessageUtils.message("sys.job.tenant.no.auth"));
        }
        return toAjax(sysSdkFileService.deleteSysSdkFileByIds(ids));
    }

    @GetMapping(value = "/download/{id}")
    public AjaxResult sdkDownload(@PathVariable("id") String id, HttpServletRequest request,
        HttpServletResponse response) {
        try {
            sysSdkFileService.sdkDownload(id, request, response);
            return AjaxResult.success();
        } catch (IOException e) {
            LOGGER.error("Download file failed", e);
            throw new CustomException("file [" + id + "] Download file failed！" + e.getMessage());
        }
    }

    @GetMapping(value = "/downloadUnsafe/{id}")
    public AjaxResult sdkDownloadUnsafe(@PathVariable("id") String id, HttpServletRequest request,
        HttpServletResponse response) {
        try {
            sysSdkFileService.sdkDownload(id, request, response);
            return AjaxResult.success();
        } catch (IOException e) {
            LOGGER.error("Download file failed", e);
            return AjaxResult.error("file [" + id + "] Download file failed！" + e.getMessage());
        }
    }

    @GetMapping("/lastSDK")
    @ResponseBody
    public TableDataInfo listLastSDK(SysSdkFile SysSdkFile) {
        startPage();
        List<SysSdkFile> list = sysSdkFileService.selectLastSdkUploads();
        return getDataTable(list);
    }
}