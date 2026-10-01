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

import cn.eyecool.basedata.domain.BasePersonFinger;
import cn.eyecool.basedata.service.IBasePersonFingerService;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.scene.trade.entity.PersonFingerRecog;
import cn.eyecool.scene.trade.entity.PersonFingerVerify;
import cn.eyecool.scene.trade.service.IChannelBusiFingerHttpService;
import cn.eyecool.scene.trade.vo.PersonFingerRecogVO;
import cn.eyecool.scene.trade.vo.PersonFingerVerifyVO;
import cn.eyecool.webtrade.controller.param.CompareTwoImgParam;

/**
 * 指纹比对操作Controller
 * 
 * @author admin
 * @date 2019年11月29日
 */
@RestController
@RequestMapping("/webtrade/finger")
public class PersonFingerMatchController extends BaseController {

    private static final Logger LOG = LoggerFactory.getLogger(PersonFingerMatchController.class);

    @Autowired
    private IChannelBusiFingerHttpService channelBusiFingerHttpService;
    @Autowired
    private IBasePersonFingerService basePersonFingerService;

    /**
     * 比对操作(1比1)
     * 
     * @param fingerVerify
     * @return
     */
    @PreAuthorize("@ss.hasPermi('webtrade:finger:matchone')")
    @PostMapping("/matchone")
    public AjaxResult faceMatchOne(@RequestBody PersonFingerVerify fingerVerify) {
        /*  比对Demo暂时不再验证场景编码，没有则全库比对
            String channelCode = fingerVerify.getChannelCode();
            if (StringUtils.isBlank(channelCode)) {
                return AjaxResult.error(MessageUtils.message("person.face.match.scene.empty"));
            }
        */
        String compareThresholdStr = fingerVerify.getCompareThreshold();
        if (StringUtils.isNotBlank(compareThresholdStr)) {
            try {
                Double.valueOf(compareThresholdStr);
            } catch (Exception e) {
                LOG.error("Incoming alignment threshold is malformed", e);
                return AjaxResult
                    .error(MessageUtils.message("person.faceiris.multimodal.match.threshold.format.error"));
            }
        }
        fingerVerify.setReceivedSeq(IdWorker.getNextStringId());
        PersonFingerVerifyVO verifyVO = channelBusiFingerHttpService.verifyPersonFinger(fingerVerify);
        Map<String, Object> result = Maps.newHashMap();
        result.put("verifyVO", verifyVO);
        // 查询用户底库照
        if (StringUtils.isNotBlank(verifyVO.getFingerId())) {
            BasePersonFinger basePersonFinger =
                basePersonFingerService.selectBasePersonFingerById(verifyVO.getFingerId());
            String fingerImg = PlatformFileUtils.getImageBase64(basePersonFinger.getImageUrl());
            if (DictConstants.Encrypted.ENABLE.equals(basePersonFinger.getEncrypted())) {
                fingerImg = PlatformCryptUtils.decryptImageBase64(fingerImg);
            }
            result.put("fingerImg", fingerImg);
        }
        return AjaxResult.success(MessageUtils.message("ajax.result.operator.success"), result);
    }

    /**
     * 比对操作(1:N)
     * 
     * @param fingerRecog
     * @return
     */
    @PreAuthorize("@ss.hasPermi('webtrade:finger:matchn')")
    @PostMapping("/matchn")
    public AjaxResult faceMatchN(@RequestBody PersonFingerRecog fingerRecog) {
        /*  比对Demo暂时不再验证场景编码，没有则全库比对
            String channelCode = fingerRecog.getChannelCode();
            if (StringUtils.isBlank(channelCode)) {
                return AjaxResult.error(MessageUtils.message("person.face.match.scene.empty"));
            }
        */
        String searchNThreshold = fingerRecog.getSearchNThreshold();
        if (StringUtils.isNotBlank(searchNThreshold)) {
            try {
                Double.valueOf(searchNThreshold);
            } catch (Exception e) {
                LOG.error("Incoming alignment threshold is malformed", e);
                return AjaxResult
                    .error(MessageUtils.message("person.faceiris.multimodal.match.threshold.format.error"));
            }
        }
        fingerRecog.setReceivedSeq(IdWorker.getNextStringId());
        List<PersonFingerRecogVO> recogVOList = channelBusiFingerHttpService.recogPersonFinger(fingerRecog);
        List<Map<String, Object>> list = recogVOList.stream().map(it -> {
            Map<String, Object> map = Maps.newHashMap();
            BasePersonFinger finger = new BasePersonFinger();
            finger.setUniqueId(it.getUniqueId());
            finger.setStatus(DictConstants.Status.ENABLE);
            finger.setFingerNo(it.getFingerNo());
            List<BasePersonFinger> fingerList = basePersonFingerService.selectBasePersonFingerList(finger);
            if (CollectionUtils.isNotEmpty(fingerList)) {
                BasePersonFinger basePersonFinger = fingerList.get(0);
                String fingerImg = PlatformFileUtils.getImageBase64(basePersonFinger.getImageUrl());
                if (DictConstants.Encrypted.ENABLE.equals(basePersonFinger.getEncrypted())) {
                    fingerImg = PlatformCryptUtils.decryptImageBase64(fingerImg);
                }
                map.put("fingerImg", fingerImg);
            }
            map.put("uniqueId", it.getUniqueId());
            map.put("score", it.getScore());
            map.put("fingerNo", it.getFingerNo());
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
    @PreAuthorize("@ss.hasPermi('webtrade:finger:comparetwo')")
    @PostMapping("/comparetwo")
    public AjaxResult compareTwoImage(@RequestBody CompareTwoImgParam param) {
        // 获取图片的base64
        String image1Base64 = param.getImage1Base64();
        String image2Base64 = param.getImage2Base64();
        if (StringUtils.isBlank(image1Base64) || StringUtils.isBlank(image2Base64)) {
            return AjaxResult.error(MessageUtils.message("person.face.match.need.two.images"));
        }
        Map<String, Object> result =
            channelBusiFingerHttpService.compareTwoImage(image1Base64, image2Base64, param.getThreshold(), null);
        return AjaxResult.success(result);
    }
}
