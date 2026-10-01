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
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Maps;

import cn.eyecool.basedata.domain.BasePersonIris;
import cn.eyecool.basedata.service.IBasePersonIrisService;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.scene.trade.entity.PersonIrisRecog;
import cn.eyecool.scene.trade.entity.PersonIrisVerify;
import cn.eyecool.scene.trade.service.IChannelBusiIrisHttpService;
import cn.eyecool.scene.trade.vo.PersonIrisRecogVO;
import cn.eyecool.scene.trade.vo.PersonIrisVerifyVO;
import cn.eyecool.webtrade.controller.param.CompareTwoImgParam;

/**
 * 虹膜比对操作Controller
 * 
 * @author admin
 * @date 2019年11月29日
 */
@RestController
@RequestMapping("/webtrade/iris")
public class PersonIrisMatchController extends BaseController {

    private static final Logger LOG = LoggerFactory.getLogger(PersonIrisMatchController.class);

    @Autowired
    private IChannelBusiIrisHttpService channelBusiIrisHttpService;
    @Autowired
    private IBasePersonIrisService basePersonIrisService;

    /**
     * 比对操作(1比1)
     * 
     * @param irisVerify
     * @return
     */
    @PreAuthorize("@ss.hasPermi('webtrade:iris:matchone')")
    @PostMapping("/matchone")
    public AjaxResult faceMatchOne(@RequestBody PersonIrisVerify irisVerify) {
        /*  比对Demo暂时不再验证场景编码，没有则全库比对
            String channelCode = irisVerify.getChannelCode();
            if (StringUtils.isBlank(channelCode)) {
                return AjaxResult.error(MessageUtils.message("person.face.match.scene.empty"));
            }
        */
        String uniqueId = irisVerify.getUniqueId();
        if (StringUtils.isBlank(uniqueId)) {
            return AjaxResult.error(MessageUtils.message("person.face.match.uniqueid.empty"));
        }
        irisVerify.setUniqueId(uniqueId);
        String compareThresholdStr = irisVerify.getCompareThreshold();
        if (StringUtils.isNotBlank(compareThresholdStr)) {
            try {
                Double.valueOf(compareThresholdStr);
            } catch (Exception e) {
                LOG.error("Incoming alignment threshold is malformed", e);
                return AjaxResult
                    .error(MessageUtils.message("person.faceiris.multimodal.match.threshold.format.error"));
            }
        }
        irisVerify.setReceivedSeq(IdWorker.getNextStringId());
        PersonIrisVerifyVO verifyVO = channelBusiIrisHttpService.verifyPersonIris(irisVerify);
        Map<String, Object> result = Maps.newHashMap();
        result.put("verifyVO", verifyVO);
        // 查询用户底库照
        BasePersonIris iris = new BasePersonIris();
        iris.setUniqueId(uniqueId);
        iris.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonIris> irisList = basePersonIrisService.selectBasePersonIrisList(iris);
        BasePersonIris basePersonIris = irisList.get(0);
        String irisImg = PlatformFileUtils.getImageBase64(basePersonIris.getImageUrl());
        if (DictConstants.Encrypted.ENABLE.equals(basePersonIris.getEncrypted())) {
            irisImg = PlatformCryptUtils.decryptImageBase64(irisImg);
        }
        result.put("irisImg", irisImg);
        return AjaxResult.success(MessageUtils.message("ajax.result.operator.success"), result);
    }

    /**
     * 比对操作(1:N)
     * 
     * @param irisRecog
     * @return
     */
    @PreAuthorize("@ss.hasPermi('webtrade:iris:matchn')")
    @PostMapping("/matchn")
    public AjaxResult faceMatchN(@RequestBody PersonIrisRecog irisRecog) {
        /*  比对Demo暂时不再验证场景编码，没有则全库比对
            String channelCode = irisRecog.getChannelCode();
            if (StringUtils.isBlank(channelCode)) {
                return AjaxResult.error(MessageUtils.message("person.face.match.scene.empty"));
            }
        */
        String searchNThreshold = irisRecog.getSearchNThreshold();
        if (StringUtils.isNotBlank(searchNThreshold)) {
            try {
                Double.valueOf(searchNThreshold);
            } catch (Exception e) {
                LOG.error("Incoming alignment threshold is malformed", e);
                return AjaxResult.error(MessageUtils.message("person.face.match.checklive.threshold.error"));
            }
        }
        irisRecog.setReceivedSeq(IdWorker.getNextStringId());
        List<PersonIrisRecogVO> recogVOList = channelBusiIrisHttpService.recogPersonIris(irisRecog);
        List<Map<String, Object>> list = recogVOList.stream().map(it -> {
            Map<String, Object> map = Maps.newHashMap();
            BasePersonIris iris = new BasePersonIris();
            iris.setUniqueId(it.getUniqueId());
            iris.setStatus(DictConstants.Status.ENABLE);
            List<BasePersonIris> irisList = basePersonIrisService.selectBasePersonIrisList(iris);
            if (CollectionUtils.isNotEmpty(irisList)) {
                irisList.forEach(imgItem -> {
                    String irisImg = PlatformFileUtils.getImageBase64(imgItem.getImageUrl());
                    if (DictConstants.Encrypted.ENABLE.equals(imgItem.getEncrypted())) {
                        irisImg = PlatformCryptUtils.decryptImageBase64(irisImg);
                    }
                    map.put("irisImg", irisImg);
                });
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
     * @param param
     * @return
     */
    @PreAuthorize("@ss.hasPermi('webtrade:iris:comparetwo')")
    @PostMapping("/comparetwo")
    public AjaxResult compareTwoImage(@RequestBody CompareTwoImgParam param) {
        // 获取图片的base64
        String image1Base64 = param.getImage1Base64();
        String image2Base64 = param.getImage2Base64();
        if (StringUtils.isBlank(image1Base64) || StringUtils.isBlank(image2Base64)) {
            return AjaxResult.error(MessageUtils.message("person.face.match.need.two.images"));
        }
        Map<String, Object> result =
            channelBusiIrisHttpService.compareTwoImage(image1Base64, image2Base64, param.getThreshold(), null);
        return AjaxResult.success(result);
    }
}
