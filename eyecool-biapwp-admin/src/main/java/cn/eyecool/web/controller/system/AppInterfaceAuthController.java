package cn.eyecool.web.controller.system;

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
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.entity.SysDictData;
import cn.eyecool.common.core.page.TableDataInfo;
import cn.eyecool.common.enums.BusinessType;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.system.domain.AppInterfaceAuth;
import cn.eyecool.system.domain.SysTenantInterface;
import cn.eyecool.system.service.IAppInterfaceAuthService;
import cn.eyecool.system.service.ISysDictTypeService;
import cn.eyecool.system.service.ISysTenantInterfaceService;

/**
 * 应用接口授权Controller
 * 
 * @author admin
 * @date 2021-04-14
 */
@RestController
@RequestMapping("/app/auth")
public class AppInterfaceAuthController extends BaseController {
    @Autowired
    private IAppInterfaceAuthService appInterfaceAuthService;
    @Autowired
    private ISysTenantInterfaceService tenantInterfaceService;
    @Autowired
    private ISysDictTypeService dictTypeService;

    /**
     * 查询应用接口授权列表
     */
    @PreAuthorize("@ss.hasPermi('app:auth:list')")
    @GetMapping("/list")
    public TableDataInfo list(AppInterfaceAuth appInterfaceAuth) {
        startPage();
        List<AppInterfaceAuth> list = appInterfaceAuthService.selectAppInterfaceAuthList(appInterfaceAuth);
        return getDataTable(list);
    }

    /**
     * 导出应用接口授权列表
     */
    @PreAuthorize("@ss.hasPermi('app:auth:export')")
    @Log(title = "app.interface.auth", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(AppInterfaceAuth appInterfaceAuth) {
        List<AppInterfaceAuth> list = appInterfaceAuthService.selectAppInterfaceAuthList(appInterfaceAuth);
        ExcelUtil<AppInterfaceAuth> util = new ExcelUtil<AppInterfaceAuth>(AppInterfaceAuth.class);
        return util.exportExcel(list, "auth");
    }

    /**
     * 获取应用接口授权详细信息
     */
    @PreAuthorize("@ss.hasPermi('app:auth:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return AjaxResult.success(appInterfaceAuthService.selectAppInterfaceAuthById(id));
    }

    /**
     * 新增应用接口授权
     */
    @PreAuthorize("@ss.hasPermi('app:auth:add')")
    @Log(title = "app.interface.auth", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AppInterfaceAuth appInterfaceAuth) {
        return toAjax(appInterfaceAuthService.insertAppInterfaceAuth(appInterfaceAuth));
    }

    /**
     * 修改应用接口授权
     */
    @PreAuthorize("@ss.hasPermi('app:auth:edit')")
    @Log(title = "app.interface.auth", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AppInterfaceAuth appInterfaceAuth) {
        return toAjax(appInterfaceAuthService.updateAppInterfaceAuth(appInterfaceAuth));
    }

    /**
     * 删除应用接口授权
     */
    @PreAuthorize("@ss.hasPermi('app:auth:remove')")
    @Log(title = "app.interface.auth", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(appInterfaceAuthService.deleteAppInterfaceAuthByIds(ids));
    }

    /**
     * 查询租户所有可用接口列表
     */
    @GetMapping("/listTenantInteraface")
    public AjaxResult listTenantInteraface() {
        List<SysDictData> interfaceList = dictTypeService.selectDictDataByType(DictConstants.HTTP_INTREFACE_DICT_TYPE);
        // 普通租户
        if (SecurityUtils.isTenantUser()) {
            SysTenantInterface tenantInterface = new SysTenantInterface();
            tenantInterface.setTenantId(TenantContextHolder.getTenantId());
            List<SysTenantInterface> list = tenantInterfaceService.selectSysTenantInterfaceList(tenantInterface);
            List<String> transcodeList =
                list.stream().map(SysTenantInterface::getTranscode).collect(Collectors.toList());
            interfaceList = interfaceList.stream().filter(it -> {
                return transcodeList.contains(it.getDictValue());
            }).collect(Collectors.toList());
        }
        return AjaxResult.success(interfaceList);
    }

}
