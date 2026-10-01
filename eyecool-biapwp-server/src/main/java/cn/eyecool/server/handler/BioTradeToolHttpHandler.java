package cn.eyecool.server.handler;

import java.util.List;
import java.util.Map;

import org.apache.commons.validator.routines.DoubleValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eyecool.abis.callmicroservice.common.FeatureBean;

import cn.eyecool.common.constant.Constants;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.http.HttpAjaxResult;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.scene.trade.service.IChannelBusiFaceHttpService;
import cn.eyecool.scene.trade.service.IChannelBusiFingerHttpService;
import cn.eyecool.scene.trade.service.IChannelBusiIrisHttpService;

/**
 * 生物特征操作工具类接口处理器
 * 
 * @author admin
 * @date 2020年7月30日
 */
@Component
public class BioTradeToolHttpHandler {

    private static final Logger LOG = LoggerFactory.getLogger(BioTradeToolHttpHandler.class);

    // 现场检活视频大小限制15M（单位：字节）
    private static Integer VIDEO_SIZE_LIMIT = 1024 * 1024 * 15;

    @Autowired
    private IChannelBusiFaceHttpService channelBusiFaceHttpService;
    @Autowired
    private IChannelBusiFingerHttpService channelBusiFingerHttpService;
    @Autowired
    private IChannelBusiIrisHttpService channelBusiIrisHttpService;

    /**
     * 提取人脸特征
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult getPersonFaceFeature(String bizContent) {
        return getPersonBioFeature(DictConstants.BioAttestType.FACE, MessageUtils.message("channel.common.service.scene.verify.face"), bizContent);
    }

    /**
     * 提取指纹特征
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult getPersonFingerFeature(String bizContent) {
        return getPersonBioFeature(DictConstants.BioAttestType.FINGER, MessageUtils.message("channel.common.service.scene.verify.finger"), bizContent);
    }

    /**
     * 提取虹膜特征
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult getPersonIrisFeature(String bizContent) {
        return getPersonBioFeature(DictConstants.BioAttestType.IRIS, MessageUtils.message("channel.common.service.scene.verify.iris"), bizContent);
    }

    /**
     * 提取生物特征
     * 
     * @param bioType
     * @param bioDesc
     * @param bizContent
     * @return
     */
    private AjaxResult getPersonBioFeature(String bioType, String bioDesc, String bizContent) {
        // json转换
        JSONObject parseObject = null;
        try {
            parseObject = JSON.parseObject(bizContent);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        String msg = null;
        // 场景编码
        String channelCode = (String)parseObject.get("channelCode");
        // 现场照不能为空
        String sceneImage = (String)parseObject.get("sceneImage");
        if (StringUtils.isBlank(sceneImage)) {
            msg = MessageUtils.message("bio.trade.handler.sceneimage.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        try {
            List<FeatureBean> featureBeans = null;
            // 获取特征
            switch (bioType) {
                case DictConstants.BioAttestType.FACE:
                    featureBeans = channelBusiFaceHttpService.getPersonFaceFeature(sceneImage, channelCode);
                    break;
                case DictConstants.BioAttestType.FINGER:
                    featureBeans = channelBusiFingerHttpService.getPersonFingerFeature(sceneImage, channelCode);
                    break;
                case DictConstants.BioAttestType.IRIS:
                    featureBeans = channelBusiIrisHttpService.getPersonIrisFeature(sceneImage, channelCode);
                    break;
            }
            return HttpAjaxResult.httpSuccess(featureBeans);
        } catch (CustomException e) {
            LOG.error("Failed to get {} feature", bioDesc, e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("Failed to get {} feature", bioDesc, e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 比对两张人脸照片
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult compareTwoFaceImg(String bizContent) {
        return compareTwoBioImage(DictConstants.BioAttestType.FACE, MessageUtils.message("channel.common.service.scene.verify.face"), bizContent);
    }

    /**
     * 比对两张指纹照片
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult compareTwoFingerImg(String bizContent) {
        return compareTwoBioImage(DictConstants.BioAttestType.FINGER, MessageUtils.message("channel.common.service.scene.verify.finger"), bizContent);
    }

    /**
     * 比对两张虹膜照片
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult compareTwoIrisImg(String bizContent) {
        return compareTwoBioImage(DictConstants.BioAttestType.IRIS, MessageUtils.message("channel.common.service.scene.verify.iris"), bizContent);
    }

    /**
     * 比对两张图片
     * 
     * @param bioType
     * @param bioDesc
     * @param bizContent
     * @return
     */
    private AjaxResult compareTwoBioImage(String bioType, String bioDesc, String bizContent) {
        // json转换
        JSONObject parseObject = null;
        try {
            parseObject = JSON.parseObject(bizContent);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        String msg = null;
        // 场景编码
        String channelCode = (String)parseObject.get("channelCode");
        // 现场照不能为空
        String sceneImage1 = (String)parseObject.get("sceneImage1");
        String sceneImage2 = (String)parseObject.get("sceneImage2");
        if (StringUtils.isBlank(sceneImage1) || StringUtils.isBlank(sceneImage2)) {
            msg = MessageUtils.message("bio.tradetool.handler.sceneimage1.sceneimage2.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        String thresholdStr = (String)parseObject.get("threshold");
        if (StringUtils.isNotBlank(thresholdStr) && !DoubleValidator.getInstance().isValid(thresholdStr)) {
            msg = MessageUtils.message("bio.tradetool.handler.threshold.format.number");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        Double threshold = StringUtils.isBlank(thresholdStr) ? null : Double.valueOf(thresholdStr);
        try {
            // 获取特征
            Map<String, Object> map = null;
            // 获取特征
            switch (bioType) {
                case DictConstants.BioAttestType.FACE:
                    map = channelBusiFaceHttpService.compareTwoImage(sceneImage1, sceneImage2, threshold, channelCode);
                    break;
                case DictConstants.BioAttestType.FINGER:
                    map =
                        channelBusiFingerHttpService.compareTwoImage(sceneImage1, sceneImage2, threshold, channelCode);
                    break;
                case DictConstants.BioAttestType.IRIS:
                    map = channelBusiIrisHttpService.compareTwoImage(sceneImage1, sceneImage2, threshold, channelCode);
                    break;
            }
            return HttpAjaxResult.httpSuccess(map);
        } catch (CustomException e) {
            LOG.error("{}Image comparison failed", bioDesc, e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("{}Image comparison failed", bioDesc, e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 人脸图片或者视频检活
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult checklivePersonFaceOrVideo(String bizContent) {
        // json转换
        JSONObject parseObject = null;
        try {
            parseObject = JSON.parseObject(bizContent);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        String msg = null;
        // 场景编码
        String channelCode = (String)parseObject.get("channelCode");
        // 检活媒体类型不能为空
        String mediaType = (String)parseObject.get("mediaType");
        if (!Constants.STATUS_ONE.equals(mediaType) && !Constants.STATUS_TWO.equals(mediaType)) {
            msg = MessageUtils.message("bio.tradetool.handler.mediatype.empty",Constants.STATUS_ONE + "|" + Constants.STATUS_TWO);
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 现场照或者现场视频不能为空
        String sceneImage = (String)parseObject.get("sceneImage");
        String sceneVideo = (String)parseObject.get("sceneVideo");
        if (Constants.STATUS_ONE.equals(mediaType) && StringUtils.isBlank(sceneImage)) {
            msg = MessageUtils.message("bio.trade.handler.sceneimage.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        } else if (Constants.STATUS_TWO.equals(mediaType) && StringUtils.isBlank(sceneVideo)) {
            msg = MessageUtils.message("bio.trade.handler.video.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        if (StringUtils.isNotBlank(sceneVideo)) {
            Integer videoSize = PlatformFileUtils.fileSize(sceneVideo);
            if (VIDEO_SIZE_LIMIT < videoSize) {
                msg = MessageUtils.message("bio.trade.handler.video.max.limit", VIDEO_SIZE_LIMIT);
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }
        String thresholdStr = (String)parseObject.get("threshold");
        if (StringUtils.isNotBlank(thresholdStr) && !DoubleValidator.getInstance().isValid(thresholdStr)) {
            msg = MessageUtils.message("bio.tradetool.handler.checklive.threshold.format.number");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        Double threshold = StringUtils.isBlank(thresholdStr) ? null : Double.valueOf(thresholdStr);
        try {
            // 获取特征
            Map<String, Object> map = null;
            if (Constants.STATUS_ONE.equals(mediaType)) {
                map = channelBusiFaceHttpService.checklivePersonFaceImage(sceneImage, threshold, channelCode);
            } else {
                map = channelBusiFaceHttpService.checklivePersonFaceVideo(sceneVideo, threshold, channelCode);
            }
            return HttpAjaxResult.httpSuccess(map);
        } catch (CustomException e) {
            LOG.error("face detection abnormality", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("face detection abnormality", e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 人脸图片质量检测
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult personFaceQualityDetect(String bizContent) {
        return bioQualityDetect(DictConstants.BioAttestType.FACE, MessageUtils.message("channel.common.service.scene.verify.face"), bizContent);
    }

    /**
     * 指纹图片质量检测
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult personFingerQualityDetect(String bizContent) {
        return bioQualityDetect(DictConstants.BioAttestType.FINGER,  MessageUtils.message("channel.common.service.scene.verify.finger"), bizContent);
    }

    /**
     * 图片质量检测
     * 
     * @param bizContent
     * @return
     */
    private AjaxResult bioQualityDetect(String bioType, String bioDesc, String bizContent) {
        // json转换
        JSONObject parseObject = null;
        try {
            parseObject = JSON.parseObject(bizContent);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        String msg = null;
        // 场景编码
        String channelCode = (String)parseObject.get("channelCode");
        // 现场照不能为空
        String sceneImage = (String)parseObject.get("sceneImage");
        if (StringUtils.isBlank(sceneImage)) {
            msg = MessageUtils.message("bio.trade.handler.sceneimage.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        String thresholdStr = (String)parseObject.get("threshold");
        if (StringUtils.isNotBlank(thresholdStr) && !DoubleValidator.getInstance().isValid(thresholdStr)) {
            msg = MessageUtils.message("bio.tradetool.handler.quality.check.threshold.format");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        Double threshold = StringUtils.isBlank(thresholdStr) ? null : Double.valueOf(thresholdStr);
        try {
            // 获取特征
            Map<String, Object> map = null;
            // 获取特征
            switch (bioType) {
                case DictConstants.BioAttestType.FACE:
                    map = channelBusiFaceHttpService.personFaceQualityDetect(sceneImage, threshold, channelCode);
                    break;
                case DictConstants.BioAttestType.FINGER:
                    map = channelBusiFingerHttpService.personFingerQualityDetect(sceneImage, threshold, channelCode);
                    break;
            }
            return HttpAjaxResult.httpSuccess(map);
        } catch (CustomException e) {
            LOG.error("{}Quality check exception", bioDesc, e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("{}Quality check exception", bioDesc, e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 人脸视频检活和比对
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult personFaceVideoCheckliveAndCompare(String bizContent) {
        // json转换
        JSONObject parseObject = null;
        try {
            parseObject = JSON.parseObject(bizContent);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        String msg = null;
        // 场景编码
        String channelCode = (String)parseObject.get("channelCode");
        // 现场照不能为空
        String sceneImage = (String)parseObject.get("sceneImage");
        String sceneVideo = (String)parseObject.get("sceneVideo");
        if (StringUtils.isBlank(sceneImage) || StringUtils.isBlank(sceneVideo)) {
            msg = MessageUtils.message("bio.tradetool.handler.sceneimage.scenevideo.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        String checkliveThresholdStr = (String)parseObject.get("checkliveThreshold");
        if (StringUtils.isNotBlank(checkliveThresholdStr)
            && !DoubleValidator.getInstance().isValid(checkliveThresholdStr)) {
            msg = MessageUtils.message("bio.tradetool.handler.video.checklive.threshold.format");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        Integer videoSize = PlatformFileUtils.fileSize(sceneVideo);
        if (VIDEO_SIZE_LIMIT < videoSize) {
            msg = MessageUtils.message("bio.trade.handler.video.max.limit",VIDEO_SIZE_LIMIT);
            return HttpAjaxResult.businessDataValidError(msg);
        }
        Double checkliveThreshold =
            StringUtils.isBlank(checkliveThresholdStr) ? null : Double.valueOf(checkliveThresholdStr);
        try {
            // 获取特征
            Map<String, Object> map = channelBusiFaceHttpService.checkliveFaceVideoAndCompare(sceneVideo, sceneImage,
                checkliveThreshold, channelCode);
            return HttpAjaxResult.httpSuccess(map);
        } catch (CustomException e) {
            LOG.error("Abnormal face check biopsy", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("Abnormal face check biopsy", e);
            return HttpAjaxResult.httpError();
        }
    }

}
