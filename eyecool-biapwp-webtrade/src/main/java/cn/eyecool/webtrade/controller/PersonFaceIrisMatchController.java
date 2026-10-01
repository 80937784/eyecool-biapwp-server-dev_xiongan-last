package cn.eyecool.webtrade.controller;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eyecool.abis.callmicroservice.IMultiFeatureService;
import com.eyecool.abis.callmicroservice.MicroConstants;
import com.google.common.collect.Maps;

import cn.eyecool.basedata.domain.BasePersonIrisFace;
import cn.eyecool.basedata.service.IBasePersonIrisFaceService;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.scene.trade.entity.PersonFaceIrisVerify;
import cn.eyecool.scene.trade.entity.PersonIrisFaceVerify;
import cn.eyecool.scene.trade.service.IChannelBusiIrisFaceHttpService;
import cn.eyecool.scene.trade.vo.PersonIrisFaceVerifyVO;
import cn.eyecool.system.service.ISysConfigService;

/**
 * 人脸虹膜多模态比对操作Controller
 * 
 * @author admin
 * @date 2019年11月29日
 */
@RestController
@RequestMapping("/webtrade/faceiris")
public class PersonFaceIrisMatchController {
    private static final Logger LOG = LoggerFactory.getLogger(PersonFaceIrisMatchController.class);

    @Autowired
    private IBasePersonIrisFaceService basePersonIrisFaceService;
    @Autowired
    private IMultiFeatureService multiFeatureService;
    // @Autowired
    // private MultiFusionFeatureService fusionFeatureService;
    @Autowired
    private ISysConfigService syConfigService;
    @Autowired
    private IChannelBusiIrisFaceHttpService channelBusiIrisFaceHttpService;

    /**
     * 人脸虹膜1v1比对
     * 
     * @param faceIrisVerify
     * @return
     */
    @PreAuthorize("@ss.hasPermi('webtrade:faceiris:matchone')")
    @PostMapping("/matchone")
    public AjaxResult faceMatchOne(@RequestBody PersonFaceIrisVerify faceIrisVerify) {
        String uniqueId = faceIrisVerify.getUniqueId();
        if (StringUtils.isBlank(uniqueId)) {
            return AjaxResult.error(MessageUtils.message("person.faceiris.multimodal.uniqueid.empty"));
        }
        String compareThreshold = faceIrisVerify.getCompareThreshold();
        if (StringUtils.isBlank(compareThreshold)) {
            compareThreshold = syConfigService.selectConfigByKey("multi.irisface.fusion.compare.threshold");
        }
        if (StringUtils.isBlank(compareThreshold)) {
            compareThreshold = "80";
        }
        try {
            Double.valueOf(compareThreshold);
        } catch (Exception e) {
            LOG.error("传入的比对阈值格式错误", e);
            return AjaxResult.error(MessageUtils.message("person.faceiris.multimodal.match.threshold.format.error"));
        }
        BasePersonIrisFace condition = new BasePersonIrisFace();
        condition.setUniqueId(uniqueId);
        condition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonIrisFace> irisFaceList = basePersonIrisFaceService.selectBasePersonIrisFaceList(condition);
        if (CollectionUtils.isEmpty(irisFaceList)) {
            return AjaxResult.error(MessageUtils.message("person.faceiris.multimodal.multiinfo.empty", uniqueId));
        }
        BasePersonIrisFace irisFace = irisFaceList.get(0);
        if (StringUtils.isBlank(irisFace.getFaceFeature())) {
            return AjaxResult.error(MessageUtils.message("person.faceiris.multimodal.multiinfo.empty", uniqueId));
        }
        if (StringUtils.isBlank(irisFace.getIrisFeature())) {
            return AjaxResult.error(MessageUtils.message("person.faceiris.multimodal.multiinfo.empty", uniqueId));
        }

        String faceSceneImage = faceIrisVerify.getFaceSceneImage();
        String irisSceneFeature = faceIrisVerify.getIrisSceneFeature();
        String irisSceneImage = faceIrisVerify.getIrisSceneImage();
        if (StringUtils.isBlank(irisSceneFeature)) {
            try {
                irisSceneFeature =
                    multiFeatureService.extractFeatureByImage(irisSceneImage, MicroConstants.AlgType.IRIS);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                return AjaxResult.error(MessageUtils.message("person.faceiris.multimodal.match.error", e.getMessage()));
            }
        }
        String faceSceneFeature;
        try {
            faceSceneFeature = multiFeatureService.extractFeatureByImage(faceSceneImage, MicroConstants.AlgType.FACE);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return AjaxResult.error(MessageUtils.message("person.faceiris.multimodal.match.error", e.getMessage()));
        }

        PersonIrisFaceVerify personIrisFaceVerify = new PersonIrisFaceVerify();
        personIrisFaceVerify.setChannelCode(faceIrisVerify.getChannelCode());
        personIrisFaceVerify.setFaceBase64Img(faceSceneImage);
        personIrisFaceVerify.setFaceFeatureBase64(faceSceneFeature);
        personIrisFaceVerify.setFusionCompareThreshold(compareThreshold);
        personIrisFaceVerify.setIrisBase64Img(irisSceneImage);
        personIrisFaceVerify.setIrisFeatureBase64(irisSceneFeature);
        personIrisFaceVerify.setUniqueId(uniqueId);
        personIrisFaceVerify.setVerifyType(DictConstants.MuliIrisFaceVerifyType.VERIFY_IRIS_FACE_MULTI);
        personIrisFaceVerify.setReceivedSeq(IdWorker.getNextStringId());
        PersonIrisFaceVerifyVO verifyVO = channelBusiIrisFaceHttpService.verifyPersonIrisFace(personIrisFaceVerify);
        // String fusionFeature = fusionFeatureService.fusionFeature(faceSceneFeature, irisSceneFeature);
        // float score = fusionFeatureService.matchFusionFeatures(fusionFeature, irisFace.getFusionFeature());
        Map<String, Object> resultMap = Maps.newHashMap();
        resultMap.put("score", verifyVO.getFusionScore());
        resultMap.put("result", DictConstants.BioResult.PASS.equals(verifyVO.getResult()));
        String faceImg = PlatformFileUtils.getImageBase64(irisFace.getFaceImageUrl());
        if (DictConstants.Encrypted.ENABLE.equals(irisFace.getEncrypted())) {
            faceImg = PlatformCryptUtils.decryptImageBase64(faceImg);
        }
        resultMap.put("faceImg", faceImg);
        return AjaxResult.success(resultMap);
    }

}