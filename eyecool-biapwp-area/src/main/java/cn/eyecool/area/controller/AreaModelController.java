package cn.eyecool.area.controller;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
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

import cn.eyecool.area.domain.AreaModel;
import cn.eyecool.area.service.IAreaModelService;
import cn.eyecool.common.annotation.Log;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.enums.BusinessType;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.poi.ExcelUtil;

/**
 * 区域Controller
 * 
 * @author admin
 * @date 2021-03-26
 */
@RestController
@RequestMapping("/area/model")
public class AreaModelController extends BaseController {
    @Autowired
    private IAreaModelService areaModelService;

    /**
     * 查询区域列表
     */
    @PreAuthorize("@ss.hasPermi('area:model:list')")
    @GetMapping("/list")
    public AjaxResult list(AreaModel areaModel) {
        List<AreaModel> list = areaModelService.selectAreaModelList(areaModel);
        return AjaxResult.success(list);
    }

    /**
     * 查询区域列表（排除节点）
     */
    @PreAuthorize("@ss.hasPermi('area:model:list')")
    @GetMapping("/list/exclude/{id}")
    public AjaxResult excludeChild(@PathVariable(value = "id", required = false) Long id) {
        List<AreaModel> list = areaModelService.selectAreaModelList(new AreaModel());
        Iterator<AreaModel> it = list.iterator();
        while (it.hasNext()) {
            AreaModel d = it.next();
            if (d.getId().intValue() == id || ArrayUtils.contains(StringUtils.split(d.getAncestors(), ","), id + "")) {
                it.remove();
            }
        }
        return AjaxResult.success(list);
    }

    /**
     * 导出区域列表
     */
    @PreAuthorize("@ss.hasPermi('area:model:export')")
    @Log(title = "area.management", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(AreaModel areaModel) {
        List<AreaModel> list = areaModelService.selectAreaModelList(areaModel);
        ExcelUtil<AreaModel> util = new ExcelUtil<AreaModel>(AreaModel.class);
        return util.exportExcel(list, "model");
    }

    /**
     * 获取区域详细信息
     */
    @PreAuthorize("@ss.hasPermi('area:model:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(areaModelService.selectAreaModelById(id));
    }

    /**
     * 新增区域
     */
    @PreAuthorize("@ss.hasPermi('area:model:add')")
    @Log(title = "area.management", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AreaModel areaModel) {
        if (!areaModelService.checkAreaNameUnique(areaModel)) {
            String msg =  MessageUtils.message("area.add.fail.name.exist", areaModel.getAreaName());
            return AjaxResult.error(msg);
        }
        return toAjax(areaModelService.insertAreaModel(areaModel));
    }

    /**
     * 修改区域
     */
    @PreAuthorize("@ss.hasPermi('area:model:edit')")
    @Log(title = "area.management", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AreaModel areaModel) {
        if (DictConstants.Status.DISABLE.equals(areaModel.getStatus())
            && areaModelService.selectNormalChildrenCountById(areaModel.getId()) > 0) {
            return AjaxResult.error(MessageUtils.message("area.update.fail.contains.used.subarea"));
        }
        if (!areaModelService.checkAreaNameUnique(areaModel)) {
            String msg = MessageUtils.message("area.update.fail.name.exist", areaModel.getAreaName());
            return AjaxResult.error(msg);
        }
        return toAjax(areaModelService.updateAreaModel(areaModel));
    }

    /**
     * 删除区域
     */
    @PreAuthorize("@ss.hasPermi('area:model:remove')")
    @Log(title = "area.management", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        if (areaModelService.selectChildrenCount(ids[0]) > 0) {
            return AjaxResult.error(MessageUtils.message("area.delete.fail.has.children"));
        }
        return toAjax(areaModelService.deleteAreaModelByIds(ids));
    }

    /**
     * 获取部门下拉树列表
     */
    @GetMapping("/treeselect")
    public AjaxResult treeselect(AreaModel areaModel) {
        List<AreaModel> list = areaModelService.selectAreaModelList(areaModel);
        return AjaxResult.success(areaModelService.buildAreaTreeSelect(list));
    }
}
