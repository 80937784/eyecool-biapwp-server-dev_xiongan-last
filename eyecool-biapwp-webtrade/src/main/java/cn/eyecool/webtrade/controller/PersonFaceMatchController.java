package cn.eyecool.webtrade.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Maps;

import cn.eyecool.basedata.domain.BasePersonFace;
import cn.eyecool.basedata.service.IBasePersonFaceService;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.scene.trade.entity.PersonFaceRecog;
import cn.eyecool.scene.trade.entity.PersonFaceVerify;
import cn.eyecool.scene.trade.service.IChannelBusiFaceHttpService;
import cn.eyecool.scene.trade.vo.PersonFaceRecogVO;
import cn.eyecool.scene.trade.vo.PersonFaceVerifyVO;
import cn.eyecool.webtrade.controller.param.CompareTwoImgParam;

/**
 * 人脸比对操作Controller
 * 
 * @author admin
 * @date 2019年11月29日
 */
@RestController
@RequestMapping("/webtrade/face")
public class PersonFaceMatchController extends BaseController {

    private static final Logger LOG = LoggerFactory.getLogger(PersonFaceMatchController.class);

    @Autowired
    private IChannelBusiFaceHttpService channelBusiFaceHttpService;
    @Autowired
    private IBasePersonFaceService basePersonFaceService;

    /**
     * 比对操作(1比1)webtrade:face:matchn
     * 
     * @param faceVerify
     * @return
     */
    @PreAuthorize("@ss.hasPermi('webtrade:face:matchone')")
    @PostMapping("/matchone")
    public AjaxResult faceMatchOne(@RequestBody PersonFaceVerify faceVerify) {
        return doFaceMatchOne(faceVerify);
    }

    /**
     * 比对操作(1比1)
     * 
     * @param file
     * @param faceVerify
     * @return
     */
    @PreAuthorize("@ss.hasPermi('webtrade:face:matchone')")
    @PostMapping("/matchone/file")
    public AjaxResult faceMatchOne(@RequestParam(value = "file") MultipartFile file, PersonFaceVerify faceVerify) {
        if ("2".equals(faceVerify.getSceneMediaType())) {
            faceVerify.setVideo(file);
        } else {
            faceVerify.setSceneImage(PlatformFileUtils.getImageBase64(file));
        }
        return doFaceMatchOne(faceVerify);
    }

    /**
     * 人脸1v1比对
     * 
     * @param faceVerify
     * @return
     */
    private AjaxResult doFaceMatchOne(PersonFaceVerify faceVerify) {
        /*  比对Demo暂时不再验证场景编码，没有则全库比对
            String channelCode = faceVerify.getChannelCode();
            if (StringUtils.isBlank(channelCode)) {
                return AjaxResult.error(MessageUtils.message("person.face.match.scene.empty"));
            }
        */
        String uniqueId = faceVerify.getUniqueId();
        if (StringUtils.isBlank(uniqueId)) {
            return AjaxResult.error(MessageUtils.message("person.face.match.uniqueid.empty"));
        }
        String compareThresholdStr = faceVerify.getCompareThreshold();
        if (StringUtils.isNotBlank(compareThresholdStr)) {
            try {
                Double.valueOf(compareThresholdStr);
            } catch (Exception e) {
                LOG.error("Incoming alignment threshold is malformed", e);
                return AjaxResult
                    .error(MessageUtils.message("person.faceiris.multimodal.match.threshold.format.error"));
            }
        }
        String liveDetectionThreshold = faceVerify.getLiveDetectionThreshold();
        if (StringUtils.isNotBlank(liveDetectionThreshold)) {
            try {
                Double.valueOf(liveDetectionThreshold);
            } catch (Exception e) {
                LOG.error("Incoming live detection threshold is malformed", e);
                return AjaxResult.error(MessageUtils.message("person.face.match.checklive.threshold.error"));
            }
        }

        faceVerify.setReceivedSeq(IdWorker.getNextStringId());
        PersonFaceVerifyVO verifyVO = channelBusiFaceHttpService.verifyPersonFace(faceVerify);
        Map<String, Object> result = Maps.newHashMap();
        result.put("verifyVO", verifyVO);
        // 查询用户底库照
        BasePersonFace face = new BasePersonFace();
        face.setUniqueId(uniqueId);
        face.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonFace> faceList = basePersonFaceService.selectBasePersonFaceList(face);
        BasePersonFace basePersonFace = faceList.get(0);
        String faceImg = PlatformFileUtils.getImageBase64(basePersonFace.getImageUrl());
        if (DictConstants.Encrypted.ENABLE.equals(basePersonFace.getEncrypted())) {
            faceImg = PlatformCryptUtils.decryptImageBase64(faceImg);
        }
        result.put("faceImg", faceImg);
        return AjaxResult.success(MessageUtils.message("ajax.result.operator.success"), result);
    }

    /**
     * 比对操作(1:N)
     * 
     * @param file
     * @param faceRecog
     * @return
     */
    @PreAuthorize("@ss.hasPermi('webtrade:face:matchn')")
    @PostMapping("/matchn/file")
    public AjaxResult faceMatchN(@RequestParam(value = "file", required = false) MultipartFile file,
        PersonFaceRecog faceRecog) {
        if ("2".equals(faceRecog.getSceneMediaType())) {
            faceRecog.setVideo(file);
        } else if (null != file) {
            faceRecog.setSceneImage(PlatformFileUtils.getImageBase64(file));
        }
        return doFaceMatchN(faceRecog);
    }

    /**
     * 人脸1vN
     * 
     * @param faceRecog
     * @return
     */
    @PreAuthorize("@ss.hasPermi('webtrade:face:matchn')")
    @PostMapping("/matchn")
    public AjaxResult faceMatchN(@RequestBody PersonFaceRecog faceRecog) {
        return doFaceMatchN(faceRecog);
    }

    /**
     * 人脸1vN
     * 
     * @param faceRecog
     * @return
     */
    private AjaxResult doFaceMatchN(PersonFaceRecog faceRecog) {
        /*  比对Demo暂时不再验证场景编码，没有则全库比对
            String channelCode = faceRecog.getChannelCode();
            if (StringUtils.isBlank(channelCode)) {
                return AjaxResult.error(MessageUtils.message("person.face.match.scene.empty"));
            }
         */
        String searchNThreshold = faceRecog.getSearchNThreshold();
        if (StringUtils.isNotBlank(searchNThreshold)) {
            try {
                Double.valueOf(searchNThreshold);
            } catch (Exception e) {
                LOG.error("Incoming alignment threshold is malformed", e);
                return AjaxResult
                    .error(MessageUtils.message("person.faceiris.multimodal.match.threshold.format.error"));
            }
        }
        String liveDetectionThreshold = faceRecog.getLiveDetectionThreshold();
        if (StringUtils.isNotBlank(liveDetectionThreshold)) {
            try {
                Double.valueOf(liveDetectionThreshold);
            } catch (Exception e) {
                LOG.error("Incoming live detection threshold is malformed", e);
                return AjaxResult.error(MessageUtils.message("person.face.match.checklive.threshold.error"));
            }
        }

        faceRecog.setReceivedSeq(IdWorker.getNextStringId());
        List<PersonFaceRecogVO> recogVOList = channelBusiFaceHttpService.recogPersonFace(faceRecog);
        List<Map<String, Object>> list = recogVOList.stream().map(it -> {
            Map<String, Object> map = Maps.newHashMap();
            BasePersonFace face = new BasePersonFace();
            face.setUniqueId(it.getUniqueId());
            face.setStatus(DictConstants.Status.ENABLE);
            List<BasePersonFace> faceList = basePersonFaceService.selectBasePersonFaceList(face);
            if (CollectionUtils.isNotEmpty(faceList)) {
                BasePersonFace basePersonFace = faceList.get(0);
                String faceImg = PlatformFileUtils.getImageBase64(basePersonFace.getImageUrl());
                if (DictConstants.Encrypted.ENABLE.equals(basePersonFace.getEncrypted())) {
                    faceImg = PlatformCryptUtils.decryptImageBase64(faceImg);
                }
                map.put("faceImg", faceImg);
            }
            map.put("uniqueId", it.getUniqueId());
            map.put("score", it.getScore());
            return map;
        }).collect(Collectors.toList());
        return AjaxResult.success(MessageUtils.message("ajax.result.operator.success"), list);
    }

    /**
     * 两张图片比对操作
     * 
     * @param file
     * @param request
     * @return
     */
    @PreAuthorize("@ss.hasPermi('webtrade:face:comparetwo')")
    @PostMapping("/comparetwo")
    public AjaxResult compareTwoImage(@RequestBody CompareTwoImgParam param) {
        // 获取图片的base64
        String image1Base64 = param.getImage1Base64();
        String image2Base64 = param.getImage2Base64();
        if (StringUtils.isBlank(image1Base64) || StringUtils.isBlank(image2Base64)) {
            return AjaxResult.error(MessageUtils.message("person.face.match.need.two.images"));
        }
        Map<String, Object> result =
            channelBusiFaceHttpService.compareTwoImage(image1Base64, image2Base64, param.getThreshold(), null);
        return AjaxResult.success(result);
    }

}
