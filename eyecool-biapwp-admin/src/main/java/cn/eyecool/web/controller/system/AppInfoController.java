package cn.eyecool.web.controller.system;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.eyecool.common.annotation.Log;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.page.TableDataInfo;
import cn.eyecool.common.enums.BusinessType;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.system.domain.AppInfo;
import cn.eyecool.system.service.IAppInfoService;

/**
 * 应用系统信息Controller
 * 
 * @author admin
 * @date 2021-04-14
 */
@RestController
@RequestMapping("/app/info")
public class AppInfoController extends BaseController {
    @Autowired
    private IAppInfoService appInfoService;

    /**
     * 查询应用系统信息列表
     */
    @PreAuthorize("@ss.hasPermi('app:info:list')")
    @GetMapping("/list")
    public TableDataInfo list(AppInfo appInfo) {
        startPage();
        List<AppInfo> list = appInfoService.selectAppInfoList(appInfo);
        return getDataTable(list);
    }

    /**
     * 导出应用系统信息列表
     */
    @PreAuthorize("@ss.hasPermi('app:info:export')")
    @Log(title = "app.system.info", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(AppInfo appInfo) {
        List<AppInfo> list = appInfoService.selectAppInfoList(appInfo);
        ExcelUtil<AppInfo> util = new ExcelUtil<AppInfo>(AppInfo.class);
        return util.exportExcel(list, "info");
    }

    /**
     * 获取应用系统信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('app:info:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return AjaxResult.success(appInfoService.selectAppInfoById(id));
    }

    /**
     * 新增应用系统信息
     */
    @PreAuthorize("@ss.hasPermi('app:info:add')")
    @Log(title = "app.system.info", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AppInfo appInfo) {
        return toAjax(appInfoService.insertAppInfo(appInfo));
    }

    /**
     * 删除应用系统信息
     */
    @PreAuthorize("@ss.hasPermi('app:info:remove')")
    @Log(title = "app.system.info", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(appInfoService.deleteAppInfoByIds(ids));
    }

    /**
     * 查询应用系统信息列表
     */
    @GetMapping("/listAll")
    public AjaxResult listAll(AppInfo appInfo) {
        List<AppInfo> list = appInfoService.selectAppInfoList(appInfo);
        return AjaxResult.success(list);
    }

}
