package cn.eyecool.basedata.controller;

import java.util.List;

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

import cn.eyecool.basedata.domain.BasePersonIrisFace;
import cn.eyecool.basedata.service.IBasePersonIrisFaceService;
import cn.eyecool.common.annotation.Log;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.page.TableDataInfo;
import cn.eyecool.common.enums.BusinessType;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.common.utils.poi.ExcelUtil;

/**
 * 虹膜人脸多模态Controller
 * 
 * @author mawj
 * @date 2021-01-27
 */
@RestController
@RequestMapping("/basedata/faceIris")
public class BasePersonIrisFaceController extends BaseController {
    @Autowired
    private IBasePersonIrisFaceService basePersonIrisFaceService;

    /**
     * 查询虹膜人脸多模态列表
     */
    @PreAuthorize("@ss.hasPermi('basedata:faceIris:list')")
    @GetMapping("/list")
    public TableDataInfo list(BasePersonIrisFace basePersonIrisFace) {
        startPage();
        List<BasePersonIrisFace> list = basePersonIrisFaceService.selectBasePersonIrisFaceList(basePersonIrisFace);
        return getDataTable(list);
    }

    /**
     * 导出虹膜人脸多模态列表
     */
    @PreAuthorize("@ss.hasPermi('basedata:faceIris:export')")
    @Log(title = "base.person.iris.face.multimodal", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(BasePersonIrisFace basePersonIrisFace) {
        List<BasePersonIrisFace> list = basePersonIrisFaceService.selectBasePersonIrisFaceList(basePersonIrisFace);
        ExcelUtil<BasePersonIrisFace> util = new ExcelUtil<BasePersonIrisFace>(BasePersonIrisFace.class);
        return util.exportExcel(list, "faceIris");
    }

    /**
     * 获取虹膜人脸多模态详细信息
     */
    @PreAuthorize("@ss.hasPermi('basedata:faceIris:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        BasePersonIrisFace personIrisFace = basePersonIrisFaceService.selectBasePersonIrisFaceById(id);
        if (StringUtils.isNotBlank(personIrisFace.getFaceImageUrl())) {
            String stringBase64 = PlatformFileUtils.getImageBase64(personIrisFace.getFaceImageUrl());
            if (StringUtils.isNotBlank(stringBase64)
                && DictConstants.Encrypted.ENABLE.equals(personIrisFace.getEncrypted())) {
                stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
            }
            personIrisFace.setFaceImgBase64(stringBase64);
        }
        if (StringUtils.isNotBlank(personIrisFace.getIrisImageUrl())) {
            String stringBase64 = PlatformFileUtils.getImageBase64(personIrisFace.getIrisImageUrl());
            if (StringUtils.isNotBlank(stringBase64)
                && DictConstants.Encrypted.ENABLE.equals(personIrisFace.getEncrypted())) {
                stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
            }
            personIrisFace.setIrisImgBase64(stringBase64);
        }
        return AjaxResult.success(personIrisFace);
    }

    /**
     * 新增虹膜人脸多模态
     */
    @PreAuthorize("@ss.hasPermi('basedata:faceIris:add')")
    @Log(title = "base.perosn.iris.face.multimodal", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BasePersonIrisFace basePersonIrisFace) {
        return toAjax(basePersonIrisFaceService.insertBasePersonIrisFace(basePersonIrisFace));
    }

    /**
     * 修改虹膜人脸多模态
     */
    @PreAuthorize("@ss.hasPermi('basedata:faceIris:edit')")
    @Log(title = "base.perosn.iris.face.multimodal", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BasePersonIrisFace basePersonIrisFace) {
        return toAjax(basePersonIrisFaceService.updateBasePersonIrisFace(basePersonIrisFace));
    }

    /**
     * 删除虹膜人脸多模态
     */
    @PreAuthorize("@ss.hasPermi('basedata:faceIris:remove')")
    @Log(title = "base.perosn.iris.face.multimodal", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(basePersonIrisFaceService.deleteBasePersonIrisFaceByIds(ids));
    }
}
