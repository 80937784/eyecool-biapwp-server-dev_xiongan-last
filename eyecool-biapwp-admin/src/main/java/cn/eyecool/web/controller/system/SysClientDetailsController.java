package cn.eyecool.web.controller.system;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.eyecool.common.annotation.Log;
import cn.eyecool.common.constant.UserConstants;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.page.TableDataInfo;
import cn.eyecool.common.enums.BusinessType;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.system.domain.SysClientDetails;
import cn.eyecool.system.service.ISysClientDetailsService;

/**
 * 终端配置Controller
 * 
 * @author admin
 * @date 2020-11-03
 */
@RestController
@RequestMapping("/system/client")
public class SysClientDetailsController extends BaseController {
    @Autowired
    private ISysClientDetailsService sysClientDetailsService;

    /**
     * 查询终端配置列表
     */
    @PreAuthorize("@ss.hasPermi('system:client:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysClientDetails sysClientDetails) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            return getDataTable(Collections.emptyList());
        }
        startPage();
        List<SysClientDetails> list = sysClientDetailsService.selectSysClientDetailsList(sysClientDetails);
        if (SecurityUtils.isTenantUser()) {
            list = list.stream().map(it -> {
                it.setOriginSecret(null);
                it.setClientSecret(null);
                return it;
            }).collect(Collectors.toList());
        }
        return getDataTable(list);
    }

    /**
     * 导出终端配置列表
     */
    @PreAuthorize("@ss.hasPermi('system:client:export')")
    @Log(title = "terminal.device.configuration", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(SysClientDetails sysClientDetails) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            String msg = MessageUtils.message("tenant_no_operation_auth");
            return AjaxResult.error(msg);
        }
        List<SysClientDetails> list = sysClientDetailsService.selectSysClientDetailsList(sysClientDetails);
        if (SecurityUtils.isTenantUser()) {
            list = list.stream().map(it -> {
                it.setOriginSecret(null);
                it.setClientSecret(null);
                return it;
            }).collect(Collectors.toList());
        }
        ExcelUtil<SysClientDetails> util = new ExcelUtil<SysClientDetails>(SysClientDetails.class);
        return util.exportExcel(list, "client");
    }

    /**
     * 获取终端配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:client:query')")
    @GetMapping(value = "/{clientId}")
    public AjaxResult getInfo(@PathVariable("clientId") String clientId) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            String msg = MessageUtils.message("tenant_no_operation_auth");
            return AjaxResult.error(msg);
        }
        SysClientDetails clientDetails = sysClientDetailsService.selectSysClientDetailsById(clientId);
        if (null != clientDetails) {
            clientDetails.setOriginSecret(null);
            clientDetails.setClientSecret(null);
        }
        return AjaxResult.success(clientDetails);
    }

    /**
     * 新增终端配置
     */
    @PreAuthorize("@ss.hasPermi('system:client:add')")
    @Log(title = "terminal.device.configuration", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysClientDetails sysClientDetails) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            String msg = MessageUtils.message("tenant_no_operation_auth");
            return AjaxResult.error(msg);
        }
        if (UserConstants.NOT_UNIQUE.equals(sysClientDetailsService.checkClientIdUnique(sysClientDetails))) {
            String msg = MessageUtils.message("terminal.exist.msg", sysClientDetails.getClientId());
            return AjaxResult.error(msg);
        }
        return toAjax(sysClientDetailsService.insertSysClientDetails(sysClientDetails));
    }

    /**
     * 修改终端配置
     */
    @PreAuthorize("@ss.hasPermi('system:client:edit')")
    @Log(title = "terminal.device.configuration", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysClientDetails sysClientDetails) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            String msg = MessageUtils.message("tenant_no_operation_auth");
            return AjaxResult.error(msg);
        }
        return toAjax(sysClientDetailsService.updateSysClientDetails(sysClientDetails));
    }

    /**
     * 删除终端配置
     */
    @PreAuthorize("@ss.hasPermi('system:client:remove')")
    @Log(title = "terminal.device.configuration", businessType = BusinessType.DELETE)
    @DeleteMapping("/{clientIds}")
    public AjaxResult remove(@PathVariable String[] clientIds) {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            String msg = MessageUtils.message("tenant_no_operation_auth");
            return AjaxResult.error(msg);
        }
        return toAjax(sysClientDetailsService.deleteSysClientDetailsByIds(clientIds));
    }

    /**
     * 清空缓存
     */
    @PreAuthorize("@ss.hasPermi('system:client:remove')")
    @Log(title = "terminal.device.configuration", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clearCache")
    public AjaxResult clearCache() {
        // 校验是否是租户操作
        if (SecurityUtils.isTenantUser()) {
            String msg = MessageUtils.message("tenant_no_operation_auth");
            return AjaxResult.error(msg);
        }
        sysClientDetailsService.clearCache();
        return AjaxResult.success();
    }
}
