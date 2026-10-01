package cn.eyecool.basedata.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.eyecool.basedata.domain.BasePersonFace;
import cn.eyecool.basedata.service.IBasePersonFaceService;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.page.TableDataInfo;
import cn.eyecool.common.enums.PersonTypeEnum;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;

/**
 * 访客人脸图像信息Controller
 * 
 * @author mawj
 * @date 2021-01-27
 */
@RestController
@RequestMapping("/basedata/visitor/face")
public class BaseVisitorFaceController extends BaseController {

    @Autowired
    private IBasePersonFaceService basePersonFaceService;

    /**
     * 查询人脸图像信息列表
     */
    @PreAuthorize("@ss.hasPermi('basedata:visitorFace:list')")
    @GetMapping("/list")
    public TableDataInfo list(BasePersonFace basePersonFace) {
        startPage();
        basePersonFace.setPersonType(PersonTypeEnum.VISITOR.value());
        List<BasePersonFace> list = basePersonFaceService.selectBasePersonFaceList(basePersonFace);
        return getDataTable(list);
    }

    /**
     * 获取人脸图像信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('basedata:visitorFace:query')")
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

}
