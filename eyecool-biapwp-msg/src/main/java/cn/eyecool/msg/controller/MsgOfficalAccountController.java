package cn.eyecool.msg.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import cn.eyecool.common.annotation.Log;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.page.TableDataInfo;
import cn.eyecool.common.enums.BusinessType;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.msg.configure.ding.DingTokenLoader;
import cn.eyecool.msg.domain.MsgOfficalAccount;
import cn.eyecool.msg.service.IMsgOfficalAccountService;

/**
 * 微信公众号Controller
 * 
 * @author admin
 * @date 2021-04-15
 */
@RestController
@RequestMapping("/msg/officalAccount")
public class MsgOfficalAccountController extends BaseController {
    private static final Logger LOG = LoggerFactory.getLogger(DingTokenLoader.class);

    @Value("${eyecool.mpauth-dir:default}")
    private String mpauthDir;
    @Autowired
    private IMsgOfficalAccountService msgOfficalAccountService;

    /**
     * 查询微信公众号列表
     */
    @PreAuthorize("@ss.hasPermi('msg:officalAccount:list')")
    @GetMapping("/list")
    public TableDataInfo list(MsgOfficalAccount msgOfficalAccount) {
        startPage();
        List<MsgOfficalAccount> list = msgOfficalAccountService.selectMsgOfficalAccountList(msgOfficalAccount);
        return getDataTable(list);
    }

    /**
     * 导出微信公众号列表
     */
    @PreAuthorize("@ss.hasPermi('msg:officalAccount:export')")
    @Log(title = "msg.offical.account.name", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(MsgOfficalAccount msgOfficalAccount) {
        List<MsgOfficalAccount> list = msgOfficalAccountService.selectMsgOfficalAccountList(msgOfficalAccount);
        ExcelUtil<MsgOfficalAccount> util = new ExcelUtil<MsgOfficalAccount>(MsgOfficalAccount.class);
        return util.exportExcel(list, "officalAccount");
    }

    /**
     * 获取微信公众号详细信息
     */
    @PreAuthorize("@ss.hasPermi('msg:officalAccount:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return AjaxResult.success(msgOfficalAccountService.selectMsgOfficalAccountById(id));
    }

    /**
     * 新增微信公众号
     */
    @PreAuthorize("@ss.hasPermi('msg:officalAccount:add')")
    @Log(title = "msg.offical.account.name", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MsgOfficalAccount msgOfficalAccount) {
        return toAjax(msgOfficalAccountService.insertMsgOfficalAccount(msgOfficalAccount));
    }

    /**
     * 修改微信公众号
     */
    @PreAuthorize("@ss.hasPermi('msg:officalAccount:edit')")
    @Log(title = "msg.offical.account.name", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MsgOfficalAccount msgOfficalAccount) {
        return toAjax(msgOfficalAccountService.updateMsgOfficalAccount(msgOfficalAccount));
    }

    /**
     * 删除微信公众号
     */
    @PreAuthorize("@ss.hasPermi('msg:officalAccount:remove')")
    @Log(title = "msg.offical.account.name", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(msgOfficalAccountService.deleteMsgOfficalAccountByIds(ids));
    }

    /**
     * 查询所所有微信公众号列表
     */
    @GetMapping("/listAll")
    public AjaxResult listAll(MsgOfficalAccount msgOfficalAccount) {
        List<MsgOfficalAccount> list = msgOfficalAccountService.selectMsgOfficalAccountList(msgOfficalAccount);
        return AjaxResult.success(list);
    }

    /**
     * 上传微信公众号授权访问文件
     * 
     * @param file
     * @return
     */
    @PostMapping("/uploadMpAuthFile")
    @PreAuthorize("@ss.hasPermi('msg:officalAccount:uploadMpAuthFile')")
    public AjaxResult uploadMpAuthFile(@RequestParam(value = "file") MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)) {
            return AjaxResult.error(MessageUtils.message("msg.offical.account.upload.filename.empty"));
        }
        long size = file.getSize();
        if (!originalFilename.startsWith("MP_") || !originalFilename.endsWith(".txt")) {
            return AjaxResult.error(MessageUtils.message("msg.offical.account.upload.file.format.wrong"));
        }
        if (size > 100 * 1024) {
            return AjaxResult.error(MessageUtils.message("msg.offical.account.upload.file.max.size"));
        }
        String dir = mpauthDir;
        if ("default".equals(dir)) {
            File dirFile = new File(System.getProperty("user.dir"));
            dir = dirFile.getParent() + File.separator + "mpauth";
            File mpauthDirFile = new File(dir);
            if (!mpauthDirFile.exists()) {
                mpauthDirFile.mkdirs();
            }
        }
        LOG.info("mpauthFileName is [{}], dir is [{}], tenantId is [{}]", originalFilename, dir,
            TenantContextHolder.getTenantId());
        try {
            File desc = new File(dir, originalFilename);
            file.transferTo(desc);
            return AjaxResult.success();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return AjaxResult.error(MessageUtils.message("msg.offical.account.upload.file.error"));
        }
    }

}
