package cn.eyecool.basedata.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;

import cn.eyecool.basedata.domain.BasePersonFace;
import cn.eyecool.basedata.domain.BasePersonFacePutInfo;
import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.basedata.domain.BasePersonPutInfo;
import cn.eyecool.basedata.service.IBasePersonFaceService;
import cn.eyecool.basedata.service.IBasePersonInfoService;
import cn.eyecool.basedata.service.IBaseVisitorInfoService;
import cn.eyecool.basedata.vo.BaseVisitorInfoVO;
import cn.eyecool.common.annotation.Log;
import cn.eyecool.common.config.EyecoolConfig;
import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.HttpStatus;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.page.TableDataInfo;
import cn.eyecool.common.enums.BusinessType;
import cn.eyecool.common.enums.PersonTypeEnum;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.AESUtils;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.common.utils.qrcode.QRCodeUtil;
import cn.eyecool.common.utils.uuid.UUID;

/**
 * 访客系统-访客-后台页面-相关接口Controller
 * 
 * @Author Administrator
 * @create 2021/10/21 14:45
 */
@RestController
@RequestMapping("/basedata/visitor")
public class BaseVisitorInfoController extends BaseController {

    @Autowired
    private IBasePersonInfoService basePersonInfoService;

    @Autowired
    private IBasePersonFaceService basePersonFaceService;

    @Autowired
    private IBaseVisitorInfoService iBaseVisitorInfoService;

    @Autowired
    private TenantProperties tenantProperties;

    @Value("${visit.front.address}")
    private String frontAddress;

    /**
     * 查询访客信息列表
     */
    @PreAuthorize("@ss.hasPermi('basedata:visitor:list')")
    @GetMapping("/list")
    public TableDataInfo list(BaseVisitorInfoVO visitorCondition) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
        startPage();
        // 添加一下人员类型为访客
        visitorCondition.setPersonType(PersonTypeEnum.VISITOR.value());
        // 状态为有效
        visitorCondition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonInfo> list = iBaseVisitorInfoService.selectBaseVisitorInfoList(visitorCondition);
        List<BaseVisitorInfoVO> visitorList = Lists.newArrayList();
        list.forEach(it -> {
            BaseVisitorInfoVO baseVisitorInfoVO = new BaseVisitorInfoVO();
            // 设置邀请人姓名
            BasePersonInfo inviterInfo = basePersonInfoService.selectBasePersonInfoById(it.getInviterId());
            baseVisitorInfoVO.setInviterName(inviterInfo.getName());
            // 设置生物特征采集情况
            boolean hasFace = basePersonFaceService.checkPersonHasFace(it.getId());
            it.setHasFace(hasFace);
            it.setHasFinger(false);
            it.setHasIris(false);
            it.setHasFvein(false);
            it.setHasFaceIris(false);
            // 设置到访时段
            Date effectiveBeginTime = it.getEffectiveBeginTime();
            Date effectiveEndTime = it.getEffectiveEndTime();
            String beginTime = sdf.format(effectiveBeginTime);
            String endTime = sdf.format(effectiveEndTime);
            BeanUtils.copyProperties(it, baseVisitorInfoVO);
            baseVisitorInfoVO.setVisitTimePeriod(beginTime + " 至 " + endTime);
            // 设置生效状态 0：未生效 1：生效中 2：已失效
            Long currentTime = System.currentTimeMillis();
            Long visitBeginTime = null == effectiveBeginTime ? 0L : effectiveBeginTime.getTime();
            Long visitEndTime = null == effectiveEndTime ? 0L : effectiveEndTime.getTime();
            if (currentTime < visitBeginTime) {
                // 当前时间小于到访开始时间，为未生效
                baseVisitorInfoVO.setEffectiveStatus(DictConstants.visitorEffectiveStatus.NOT_EFFECTIVE);
            } else if (currentTime > visitBeginTime && currentTime < visitEndTime) {
                // 当前时间介于到访开始跟结束时间中，为生效中
                baseVisitorInfoVO.setEffectiveStatus(DictConstants.visitorEffectiveStatus.IN_EFFECT);
            } else if (currentTime > visitEndTime) {
                // 当前时间大于到访结束时间，为已失效
                baseVisitorInfoVO.setEffectiveStatus(DictConstants.visitorEffectiveStatus.EXPIRED);
            }
            visitorList.add(baseVisitorInfoVO);
        });

        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg(MessageUtils.message("base.visitor.search.successful"));
        rspData.setRows(visitorList);
        rspData.setTotal(new PageInfo<BasePersonInfo>(list).getTotal());
        return rspData;
    }

    /**
     * 访客信息中搜索栏页面搜索邀请人接口
     */
    @PreAuthorize("@ss.hasPermi('basedata:visitor:list')")
    @GetMapping("/listByUidOrName")
    public TableDataInfo listByUidOrName(String name) {
        startPage();
        BasePersonInfo basePersonInfo = new BasePersonInfo();
        basePersonInfo.setName(name);
        basePersonInfo.setPersonType(PersonTypeEnum.USER.value());
        basePersonInfo.setFlag(DictConstants.PersonFlag.NORMAL);
        basePersonInfo.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonInfo> list = basePersonInfoService.selectBasePersonInfoList(basePersonInfo);
        return getDataTable(list);
    }

    /**
     * 获取访客基础信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('basedata:visitor:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        BasePersonInfo personInfo = basePersonInfoService.selectBasePersonInfoById(id);
        BasePersonPutInfo putInfo = new BasePersonPutInfo();
        cn.eyecool.common.utils.bean.BeanUtils.copyBeanProp(putInfo, personInfo);
        // 查询人脸图片
        BasePersonFace faceConditon = new BasePersonFace();
        faceConditon.setPersonId(id);
        faceConditon.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonFace> faceList = basePersonFaceService.selectBasePersonFaceList(faceConditon);

        if (CollectionUtils.isNotEmpty(faceList)) {
            BasePersonFacePutInfo face = faceList.stream().map(it -> {
                boolean encrypted = DictConstants.Encrypted.ENABLE.equals(it.getEncrypted());
                BasePersonFacePutInfo facePutInfo = new BasePersonFacePutInfo();
                facePutInfo.setEncrypted(encrypted ? DictConstants.Encrypted.ENABLE : DictConstants.Encrypted.DISABLE);
                String stringBase64 = PlatformFileUtils.getImageBase64(it.getImageUrl());
                if (StringUtils.isNotBlank(stringBase64) && encrypted) {
                    stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                }
                facePutInfo.setImageBase64(stringBase64);
                return facePutInfo;
            }).findFirst().orElse(null);
            putInfo.setFacePutInfo(face);
        }
        return AjaxResult.success(putInfo);
    }

    /**
     * 删除访客信息（同时解绑对应的子场景）
     */
    @PreAuthorize("@ss.hasPermi('basedata:visitor:remove')")
    @Log(title = "base.visitor.person.basic.information", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(iBaseVisitorInfoService.deleteBasePersonInfoByIds(ids));
    }

    /**
     * 生成二维码 使用访客模块的地址生成二维码
     * 
     * @param
     * @return
     */
    @PreAuthorize("@ss.hasPermi('basedata:visitor:list')")
    @GetMapping("/qrCodeGen")
    public AjaxResult getUserIris() {
        // 查询是否开启多租户，开启了的话就要在二维码中加上该参数,不能直接加上，要加密一下
        String completeFrontAddress = frontAddress;
        if (tenantProperties.getEnabled()) {
            String tenantId = TenantContextHolder.getTenantId();
            String encryptTenantId = new String(Base64.getEncoder().encode(AESUtils.encryptAES(tenantId).getBytes()));
            completeFrontAddress += "?key=" + encryptTenantId;
        }
        String destPath = EyecoolConfig.getDownloadPath() + UUID.randomUUID() + ".jpg";
        try {
            QRCodeUtil.encode(completeFrontAddress, null, destPath, true);
        } catch (Exception e) {
            throw new CustomException(MessageUtils.message("base.visitor.generate.qrcode.error") + e.getMessage());
        }
        Map<String, Object> result = new HashMap<>();
        String imageBase64 = PlatformFileUtils.getImageBase64(destPath);
        result.put("qrcodeImgBase64", imageBase64);
        CompletableFuture.runAsync(() -> PlatformFileUtils.deleteFile(destPath));
        return AjaxResult.success(result);
    }

    @GetMapping(value = "/downloadQrCode")
    public AjaxResult downloadQrCode(HttpServletRequest request, HttpServletResponse response) {
        try {
            iBaseVisitorInfoService.downloadQrCode(request, response);
        } catch (IOException e) {
            return AjaxResult.error(MessageUtils.message("base.visitor.download.file.error"));
        }
        return AjaxResult.success();
    }

}
