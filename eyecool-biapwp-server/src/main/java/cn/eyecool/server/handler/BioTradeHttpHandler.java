package cn.eyecool.server.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.validator.routines.DoubleValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import com.alibaba.fastjson.JSONObject;

import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.constant.Constants;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.entity.SysDictData;
import cn.eyecool.common.core.domain.http.HttpAjaxResult;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.device.domain.DeviceInfo;
import cn.eyecool.device.service.IDeviceInfoService;
import cn.eyecool.scene.domain.ChannelSubtreasuryInfo;
import cn.eyecool.scene.service.IChannelSubtreasuryInfoService;
import cn.eyecool.scene.trade.entity.PersonFaceRecog;
import cn.eyecool.scene.trade.entity.PersonFaceVerify;
import cn.eyecool.scene.trade.entity.PersonFingerRecog;
import cn.eyecool.scene.trade.entity.PersonFingerVerify;
import cn.eyecool.scene.trade.entity.PersonIdentityVerification;
import cn.eyecool.scene.trade.entity.PersonIrisFaceVerify;
import cn.eyecool.scene.trade.entity.PersonIrisRecog;
import cn.eyecool.scene.trade.entity.PersonIrisVerify;
import cn.eyecool.scene.trade.service.IChannelBusiFaceHttpService;
import cn.eyecool.scene.trade.service.IChannelBusiFingerHttpService;
import cn.eyecool.scene.trade.service.IChannelBusiIrisFaceHttpService;
import cn.eyecool.scene.trade.service.IChannelBusiIrisHttpService;
import cn.eyecool.scene.trade.vo.PersonFaceRecogVO;
import cn.eyecool.scene.trade.vo.PersonFaceVerifyVO;
import cn.eyecool.scene.trade.vo.PersonFingerRecogVO;
import cn.eyecool.scene.trade.vo.PersonFingerVerifyVO;
import cn.eyecool.scene.trade.vo.PersonIrisFaceVerifyVO;
import cn.eyecool.scene.trade.vo.PersonIrisRecogVO;
import cn.eyecool.scene.trade.vo.PersonIrisVerifyVO;
import cn.eyecool.system.service.ISysDictTypeService;

/**
 * 场景人员信息HTTP请求处理器
 * 
 * @author admin
 * @date 2019年11月7日
 */
@Component
public class BioTradeHttpHandler {

    private static final Logger LOG = LoggerFactory.getLogger(BioTradeHttpHandler.class);
    // 现场检活视频大小限制15M（单位：字节）
    private static Long VIDEO_SIZE_LIMIT = 1024L * 1024 * 15;

    @Autowired
    private IChannelBusiFaceHttpService channelBusiFaceHttpService;
    @Autowired
    private IChannelBusiFingerHttpService channelBusiFingerHttpService;
    @Autowired
    private IChannelBusiIrisHttpService channelBusiIrisHttpService;
    @Autowired
    private IChannelBusiIrisFaceHttpService channelBusiIrisFaceHttpService;
    @Autowired
    private ISysDictTypeService dictTypeService;
    @Autowired
    private TenantProperties tenantProperties;
    @Autowired
    private IDeviceInfoService deviceInfoService;
    @Autowired
    private IChannelSubtreasuryInfoService channelSubtreasuryInfoService;

    /**
     * 人脸1:1认证
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult verifyPersonFace(String bizContent, HttpServletRequest request) {
        // json转换
        PersonFaceVerify personFaceVerify = null;
        try {
            personFaceVerify = JSONObject.parseObject(bizContent, PersonFaceVerify.class);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult
                .businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        // 参数校验
        AjaxResult ajaxResult = validateVerifyFaceParam(personFaceVerify, request);
        if (!HttpAjaxResult.HTTP_SUCC_CODE.equals(ajaxResult.get(AjaxResult.CODE_TAG))) {
            return ajaxResult;
        }
        try {
            // 进入1:1认证
            PersonFaceVerifyVO verifyVO = channelBusiFaceHttpService.verifyPersonFace(personFaceVerify);
            return HttpAjaxResult.httpSuccess(verifyVO);
        } catch (CustomException e) {
            LOG.error("Face 1:1 authentication failed", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("Face 1:1 authentication failed", e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 人脸认证参数校验
     * 
     * @param personFaceVerify
     * @return
     */
    private AjaxResult validateVerifyFaceParam(PersonFaceVerify personFaceVerify, HttpServletRequest request) {
        String msg = null;
        // 业务流水号不能为空, 且长度不大于48
        String receivedSeq = personFaceVerify.getReceivedSeq();
        if (StringUtils.isBlank(receivedSeq) || receivedSeq.length() > 48) {
            msg = MessageUtils.message("base.person.handler.receivedseq.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        String deviceCode = personFaceVerify.getDeviceCode();
        // 设备编码不为空，说明是受平台管控的设备直接调用后端服务接口，需要进行设备合法性校验
        if (StringUtils.isNotBlank(deviceCode)) {
            DeviceInfo deviceInfo = getDeviceInfoByDeviceNo(deviceCode);
            if (null == deviceInfo) {
                msg = MessageUtils.message("bio.trade.handler.device.not.exists", deviceCode);
                LOG.error(msg);
                return HttpAjaxResult.businessError(msg);
            }
            // 验证设备租户和授权appKey所属租户是否一致
            if (!validateDeviceTenant(deviceInfo)) {
                msg = MessageUtils.message("base.person.handler.device.tenant.error", deviceCode);
                return HttpAjaxResult.businessError(msg);
            }
            // 根据设备确定场景和子场景，传输的参数不起作用
            if (StringUtils.isBlank(deviceInfo.getChannelCode())) {
                msg = MessageUtils.message("base.person.handler.device.not.bind.scene", deviceCode);
                LOG.error(msg);
                return HttpAjaxResult.businessError(msg);
            }
            personFaceVerify.setChannelCode(deviceInfo.getChannelCode());
            personFaceVerify.setDeviceModel(deviceInfo.getDeviceModelCode());
            if (StringUtils.isNotBlank(deviceInfo.getDeviceName())) {
                personFaceVerify.setDeviceName(deviceInfo.getDeviceName());
            }
            if (StringUtils.isNotBlank(deviceInfo.getDeviceIp())) {
                personFaceVerify.setDeviceIp(deviceInfo.getDeviceIp());
            }
            if (StringUtils.isNotNull(deviceInfo.getLongitude())) {
                personFaceVerify.setDeviceLongitude(String.valueOf(deviceInfo.getLongitude()));
            }
            if (StringUtils.isNotNull(deviceInfo.getLatitude())) {
                personFaceVerify.setDeviceDimension(String.valueOf(deviceInfo.getLatitude()));
            }
            personFaceVerify.setDeviceDirection(deviceInfo.getDeviceDirection());
        }
        // 人员标识不能为空, 且长度不大于48
        String uniqueId = personFaceVerify.getUniqueId();
        if (StringUtils.isBlank(uniqueId) || uniqueId.length() > 48) {
            msg = MessageUtils.message("base.person.handler.uniqueid.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 现场照不能为空
        String sceneImage = personFaceVerify.getSceneImage();
        String sceneMediaType = personFaceVerify.getSceneMediaType();
        boolean isVideoMedia = Constants.STATUS_TWO.equals(sceneMediaType);
        if (isVideoMedia) {
            // 视频不能为空
            MultipartHttpServletRequest multipartHttpServletRequest = null;
            try {
                multipartHttpServletRequest = new StandardServletMultipartResolver().resolveMultipart(request);
            } catch (Exception e) {
                msg = MessageUtils.message("bio.trade.handler.multipart.format.wrong");
                LOG.error(msg, e);
                return HttpAjaxResult.businessDataValidError(msg);
            }
            MultipartFile video = multipartHttpServletRequest.getFile("video");
            if (null == video) {
                msg = MessageUtils.message("bio.trade.handler.video.empty");
                return HttpAjaxResult.businessDataValidError(msg);
            }
            if (VIDEO_SIZE_LIMIT < video.getSize()) {
                msg = MessageUtils.message("bio.trade.handler.video.max.limit", VIDEO_SIZE_LIMIT);
                return HttpAjaxResult.businessDataValidError(msg);
            }
            personFaceVerify.setVideo(video);
        } else if (StringUtils.isBlank(sceneImage)) {
            msg = MessageUtils.message("bio.trade.handler.sceneimage.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 场景编码不能为空
        String channelCode = personFaceVerify.getChannelCode();
        if (StringUtils.isBlank(channelCode)) {
            msg = MessageUtils.message("bio.trade.handler.scenecode.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 是否检活不为空时，只能是[Y/N]之一
        String liveDetection = personFaceVerify.getLiveDetection();
        List<String> list = Arrays.asList(DictConstants.YesOrNoState.YES, DictConstants.YesOrNoState.NO);
        if (StringUtils.isNotBlank(liveDetection) && !list.contains(liveDetection)) {
            msg = MessageUtils.message("bio.trade.handler.livedetect.yn");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 检活阈值不为空时，必须是数字
        String liveDetectionThreshold = personFaceVerify.getLiveDetectionThreshold();
        if (StringUtils.isNotBlank(liveDetectionThreshold)) {
            try {
                Double.valueOf(liveDetectionThreshold);
            } catch (Exception e) {
                msg = MessageUtils.message("bio.trade.handler.livedetect.threshold.empty.number");
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }
        // 比对阈值不为空时，必须是数字
        String compareThreshold = personFaceVerify.getCompareThreshold();
        if (StringUtils.isNotBlank(compareThreshold)) {
            try {
                Double.valueOf(compareThreshold);
            } catch (Exception e) {
                msg = MessageUtils.message("bio.trade.handler.compare.threshold.empty.number");
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }
        // 前端测温+后端比对，校验温度检测结果和传入温度格式
        String temperatureResult = personFaceVerify.getTemperatureResult();
        String temperatureFloor = personFaceVerify.getTemperatureFloor();
        String temperatureTop = personFaceVerify.getTemperatureTop();
        String temperature = personFaceVerify.getTemperature();
        if (StringUtils.isNotBlank(temperatureResult)) {
            if (!DictConstants.BioResult.PASS.equals(temperatureResult)
                && !DictConstants.BioResult.NOTPASS.equals(temperatureResult)) {
                msg = MessageUtils.message("bio.trade.handler.temperature.result.error", temperatureResult);
                LOG.error(msg);
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }
        if (StringUtils.isNotBlank(temperatureFloor) && !DoubleValidator.getInstance().isValid(temperatureFloor)) {
            msg = MessageUtils.message("bio.trade.handler.temperature.min.error");
            LOG.error(msg);
            return HttpAjaxResult.businessDataValidError(msg);
        }
        if (StringUtils.isNotBlank(temperatureTop) && !DoubleValidator.getInstance().isValid(temperatureTop)) {
            msg = MessageUtils.message("bio.trade.handler.temperature.max.error");
            LOG.error(msg);
            return HttpAjaxResult.businessDataValidError(msg);
        }
        if (StringUtils.isNotBlank(temperature) && !DoubleValidator.getInstance().isValid(temperature)) {
            msg = MessageUtils.message("bio.trade.handler.temperature.error");
            LOG.error(msg);
            return HttpAjaxResult.businessDataValidError(msg);
        }
        return HttpAjaxResult.httpSuccess();
    }

    /**
     * 人脸1:N识别
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult recogPersonFace(String bizContent, HttpServletRequest request) {
        // json转换
        PersonFaceRecog personFaceRecog = null;
        try {
            personFaceRecog = JSONObject.parseObject(bizContent, PersonFaceRecog.class);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult
                .businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        // 参数校验
        AjaxResult ajaxResult = validateRecogFaceParam(personFaceRecog, request);
        if (!HttpAjaxResult.HTTP_SUCC_CODE.equals(ajaxResult.get(AjaxResult.CODE_TAG))) {
            return ajaxResult;
        }
        try {
            // 进入1:N识别
            List<PersonFaceRecogVO> personFaceRecogVOList = channelBusiFaceHttpService.recogPersonFace(personFaceRecog);
            return HttpAjaxResult.httpSuccess(personFaceRecogVOList);
        } catch (CustomException e) {
            LOG.error("Face 1:N recognition failed", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("Face 1:N recognition failed", e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 人脸识别参数校验
     * 
     * @param personFaceRecog
     * @return
     */
    private AjaxResult validateRecogFaceParam(PersonFaceRecog personFaceRecog, HttpServletRequest request) {
        String msg = null;
        // 业务流水号不能为空, 且长度不大于48
        String receivedSeq = personFaceRecog.getReceivedSeq();
        if (StringUtils.isBlank(receivedSeq) || receivedSeq.length() > 48) {
            msg = MessageUtils.message("base.person.handler.receivedseq.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        String deviceCode = personFaceRecog.getDeviceCode();
        // 设备编码不为空，说明是受平台管控的设备直接调用后端服务接口，需要进行设备合法性校验、租户重新设置和场景、子场景信息查询
        if (StringUtils.isNotBlank(deviceCode)) {
            DeviceInfo deviceInfo = getDeviceInfoByDeviceNo(deviceCode);
            if (null == deviceInfo) {
                msg = MessageUtils.message("bio.trade.handler.device.not.exists", deviceCode);
                LOG.error(msg);
                return HttpAjaxResult.businessError(msg);
            }
            // 验证设备租户和授权appKey所属租户是否一致
            if (!validateDeviceTenant(deviceInfo)) {
                msg = MessageUtils.message("base.person.handler.device.tenant.error", deviceCode);
                return HttpAjaxResult.businessError(msg);
            }
            // 根据设备确定场景和子场景，传输的参数不起作用
            if (StringUtils.isBlank(deviceInfo.getChannelCode())) {
                msg = MessageUtils.message("base.person.handler.device.not.bind.scene", deviceCode);
                LOG.error(msg);
                return HttpAjaxResult.businessError(msg);
            }
            personFaceRecog.setChannelCode(deviceInfo.getChannelCode());
            personFaceRecog.setDeviceModel(deviceInfo.getDeviceModelCode());
            if (StringUtils.isNotBlank(deviceInfo.getPrimarySubCode())) {
                ChannelSubtreasuryInfo subtreasuryCondition = new ChannelSubtreasuryInfo();
                subtreasuryCondition.setSubTreasuryCode(deviceInfo.getPrimarySubCode());
                List<ChannelSubtreasuryInfo> subtreasuryInfoList =
                    channelSubtreasuryInfoService.selectChannelSubtreasuryInfoList(subtreasuryCondition);
                personFaceRecog.setSubTreasury(
                    CollectionUtils.isEmpty(subtreasuryInfoList) ? null : deviceInfo.getPrimarySubCode());
            }
            if (StringUtils.isNotBlank(deviceInfo.getDeviceName())) {
                personFaceRecog.setDevicName(deviceInfo.getDeviceName());
            }
            if (StringUtils.isNotBlank(deviceInfo.getDeviceIp())) {
                personFaceRecog.setDeviceIp(deviceInfo.getDeviceIp());
            }
            if (StringUtils.isNotNull(deviceInfo.getLongitude())) {
                personFaceRecog.setDeviceLongitude(String.valueOf(deviceInfo.getLongitude()));
            }
            if (StringUtils.isNotNull(deviceInfo.getLatitude())) {
                personFaceRecog.setDeviceDimension(String.valueOf(deviceInfo.getLatitude()));
            }
            if (null != deviceInfo.getDuplicateTime()) {
                personFaceRecog.setDuplicateTime(String.valueOf(deviceInfo.getDuplicateTime()));
            }
            personFaceRecog.setDeviceDirection(deviceInfo.getDeviceDirection());
        }
        // 现场照片不能为空
        String sceneImage = personFaceRecog.getSceneImage();
        String sceneMediaType = personFaceRecog.getSceneMediaType();
        boolean isVideoMedia = Constants.STATUS_TWO.equals(sceneMediaType);
        if (isVideoMedia) {
            // 视频不能为空
            MultipartHttpServletRequest multipartHttpServletRequest = null;
            try {
                multipartHttpServletRequest = new StandardServletMultipartResolver().resolveMultipart(request);
            } catch (Exception e) {
                msg = MessageUtils.message("bio.trade.handler.multipart.format.wrong");
                LOG.error(msg, e);
                return HttpAjaxResult.businessDataValidError(msg);
            }
            MultipartFile video = multipartHttpServletRequest.getFile("video");
            if (null == video) {
                msg = MessageUtils.message("bio.trade.handler.video.empty");
                return HttpAjaxResult.businessDataValidError(msg);
            }
            if (VIDEO_SIZE_LIMIT < video.getSize()) {
                msg = MessageUtils.message("bio.trade.handler.video.max.limit", VIDEO_SIZE_LIMIT);
                return HttpAjaxResult.businessDataValidError(msg);
            }
            personFaceRecog.setVideo(video);
        } else if (StringUtils.isBlank(sceneImage)) {
            msg = MessageUtils.message("bio.trade.handler.sceneimage.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 场景编码不能为空
        String channelCode = personFaceRecog.getChannelCode();
        if (StringUtils.isBlank(channelCode)) {
            msg = MessageUtils.message("bio.trade.handler.scenecode.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 是否检活不为空时，只能是[Y/N]之一
        String liveDetection = personFaceRecog.getLiveDetection();
        List<String> list = Arrays.asList(DictConstants.YesOrNoState.YES, DictConstants.YesOrNoState.NO);
        if (StringUtils.isNotBlank(liveDetection) && !list.contains(liveDetection)) {
            msg = MessageUtils.message("bio.trade.handler.livedetect.yn");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 检活阈值不为空时，必须是数字
        String liveDetectionThreshold = personFaceRecog.getLiveDetectionThreshold();
        if (StringUtils.isNotBlank(liveDetectionThreshold)) {
            try {
                Double.valueOf(liveDetectionThreshold);
            } catch (Exception e) {
                msg = MessageUtils.message("bio.trade.handler.livedetect.threshold.empty.number");
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }
        // 1-N搜索比对阈值不为空时，必须是数字
        String searchNThreshold = personFaceRecog.getSearchNThreshold();
        if (StringUtils.isNotBlank(searchNThreshold)) {
            try {
                Double.valueOf(searchNThreshold);
            } catch (Exception e) {
                msg = MessageUtils.message("bio.trade.handler.1n.threshold.empty.number");
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }
        // 返回TopN不为空时，必须是数字[1~10]
        String topN = personFaceRecog.getTopN();
        if (StringUtils.isNotBlank(topN)) {
            try {
                int val = Integer.valueOf(topN);
                if (val > 10 || val < 1) {
                    msg = MessageUtils.message("bio.trade.handler.topn.return.num.format");
                    return HttpAjaxResult.businessDataValidError(msg);
                }
            } catch (Exception e) {
                msg = MessageUtils.message("bio.trade.handler.topn.return.num.format");
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }
        // 子场景编码不为空时，格式必须为[场景号_子场景编码]
        String subTreasury = personFaceRecog.getSubTreasury();
        if (StringUtils.isNotBlank(subTreasury) && !subTreasury.startsWith(channelCode + "_")) {
            msg = MessageUtils.message("bio.trade.handler.subscene.format");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 设备经度不为空时，必须是数字
        String deviceLongitude = personFaceRecog.getDeviceLongitude();
        if (StringUtils.isNotBlank(deviceLongitude)) {
            try {
                Double val = Double.valueOf(deviceLongitude);
                if (val > 180 || val < 0) {
                    msg = MessageUtils.message("bio.trade.handler.device.longitude.format");
                    return HttpAjaxResult.businessDataValidError(msg);
                }
            } catch (Exception e) {
                msg = MessageUtils.message("bio.trade.handler.device.longitude.format");
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }
        // 设备维度不为空时，必须是数字
        String deviceDimension = personFaceRecog.getDeviceDimension();
        if (StringUtils.isNotBlank(deviceDimension)) {
            try {
                Double val = Double.valueOf(deviceDimension);
                if (val > 90 || val < 0) {
                    msg = MessageUtils.message("bio.trade.handler.device.latitue.format");
                    return HttpAjaxResult.businessDataValidError(msg);
                }
            } catch (Exception e) {
                msg = MessageUtils.message("bio.trade.handler.device.latitue.format");
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }
        // 前端测温+后端比对，校验温度检测结果和传入温度格式
        String temperatureResult = personFaceRecog.getTemperatureResult();
        String temperatureFloor = personFaceRecog.getTemperatureFloor();
        String temperatureTop = personFaceRecog.getTemperatureTop();
        String temperature = personFaceRecog.getTemperature();
        if (StringUtils.isNotBlank(temperatureResult)) {
            if (!DictConstants.BioResult.PASS.equals(temperatureResult)
                && !DictConstants.BioResult.NOTPASS.equals(temperatureResult)) {
                msg = MessageUtils.message("bio.trade.handler.temperature.result.error", temperatureResult);
                LOG.error(msg);
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }
        if (StringUtils.isNotBlank(temperatureFloor) && !DoubleValidator.getInstance().isValid(temperatureFloor)) {
            msg = MessageUtils.message("bio.trade.handler.temperature.min.error");
            LOG.error(msg);
            return HttpAjaxResult.businessDataValidError(msg);
        }
        if (StringUtils.isNotBlank(temperatureTop) && !DoubleValidator.getInstance().isValid(temperatureTop)) {
            msg = MessageUtils.message("bio.trade.handler.temperature.max.error");
            LOG.error(msg);
            return HttpAjaxResult.businessDataValidError(msg);
        }
        if (StringUtils.isNotBlank(temperature) && !DoubleValidator.getInstance().isValid(temperature)) {
            msg = MessageUtils.message("bio.trade.handler.temperature.error");
            LOG.error(msg);
            return HttpAjaxResult.businessDataValidError(msg);
        }
        return HttpAjaxResult.httpSuccess();
    }

    /**
     * 指纹1:1认证
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult verifyPersonFinger(String bizContent) {
        // json转换
        PersonFingerVerify personFingerVerify = null;
        try {
            personFingerVerify = JSONObject.parseObject(bizContent, PersonFingerVerify.class);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult
                .businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        // 参数校验
        AjaxResult ajaxResult = validateVerifyFingerParam(personFingerVerify);
        if (!HttpAjaxResult.HTTP_SUCC_CODE.equals(ajaxResult.get(AjaxResult.CODE_TAG))) {
            return ajaxResult;
        }
        try {
            // 进入1:1认证
            PersonFingerVerifyVO verifyVO = channelBusiFingerHttpService.verifyPersonFinger(personFingerVerify);
            return HttpAjaxResult.httpSuccess(verifyVO);
        } catch (CustomException e) {
            LOG.error("Fingerprint 1:1 authentication failed", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("Fingerprint 1:1 authentication failed", e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 指纹1:1比对参数校验
     * 
     * @param personFingerVerify
     * @return
     */
    private AjaxResult validateVerifyFingerParam(PersonFingerVerify personFingerVerify) {
        String msg = null;
        // 人员标识不能为空, 且长度不大于48
        String uniqueId = personFingerVerify.getUniqueId();
        if (StringUtils.isBlank(uniqueId) || uniqueId.length() > 48) {
            msg = MessageUtils.message("base.person.handler.uniqueid.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 业务流水号不能为空, 且长度不大于48
        String receivedSeq = personFingerVerify.getReceivedSeq();
        if (StringUtils.isBlank(receivedSeq) || receivedSeq.length() > 48) {
            msg = MessageUtils.message("base.person.handler.receivedseq.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 现场指纹图片不能为空
        String sceneImage = personFingerVerify.getSceneImage();
        if (StringUtils.isBlank(sceneImage)) {
            msg = MessageUtils.message("bio.trade.handler.finger.image.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 场景编码不能为空
        String channelCode = personFingerVerify.getChannelCode();
        if (StringUtils.isBlank(channelCode)) {
            msg = MessageUtils.message("bio.trade.handler.scenecode.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 指纹编号不为空时，必须是合法数字
        String fingerNo = personFingerVerify.getFingerNo();
        List<SysDictData> type = dictTypeService.selectDictDataByType(DictConstants.BIO_FINGER_NO_DICT_TYPE);
        List<String> fingerNoList = type.stream().map(SysDictData::getDictValue).collect(Collectors.toList());
        if (StringUtils.isNotBlank(fingerNo) && !fingerNoList.contains(fingerNo)) {
            msg = MessageUtils.message("bio.trade.handler.finger.fingerno.format");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 比对阈值不为空时，必须是数字
        String compareThreshold = personFingerVerify.getCompareThreshold();
        if (StringUtils.isNotBlank(compareThreshold)) {
            try {
                Double.valueOf(compareThreshold);
            } catch (Exception e) {
                msg = MessageUtils.message("bio.trade.handler.compare.threshold.empty.number");
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }
        return HttpAjaxResult.httpSuccess();
    }

    /**
     * 指纹1:N识别
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult recogPersonFinger(String bizContent) {
        // json转换
        PersonFingerRecog personFingerRecog = null;
        try {
            personFingerRecog = JSONObject.parseObject(bizContent, PersonFingerRecog.class);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult
                .businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        // 参数校验
        AjaxResult ajaxResult = validateRecogFingerParam(personFingerRecog);
        if (!HttpAjaxResult.HTTP_SUCC_CODE.equals(ajaxResult.get(AjaxResult.CODE_TAG))) {
            return ajaxResult;
        }
        try {
            // 进入1:N识别
            List<PersonFingerRecogVO> personFingerRecogVOList =
                channelBusiFingerHttpService.recogPersonFinger(personFingerRecog);
            return HttpAjaxResult.httpSuccess(personFingerRecogVOList);
        } catch (CustomException e) {
            LOG.error("Fingerprint 1:N recognition failed", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("Fingerprint 1:N recognition failed", e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 指纹识别参数校验
     * 
     * @param personFaceRecog
     * @return
     */
    private AjaxResult validateRecogFingerParam(PersonFingerRecog personFingerRecog) {
        String msg = null;
        // 业务流水号不能为空, 且长度不大于48
        String receivedSeq = personFingerRecog.getReceivedSeq();
        if (StringUtils.isBlank(receivedSeq) || receivedSeq.length() > 48) {
            msg = MessageUtils.message("base.person.handler.receivedseq.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 现场照片不能为空
        String sceneImage = personFingerRecog.getSceneImage();
        if (StringUtils.isBlank(sceneImage)) {
            msg = MessageUtils.message("bio.trade.handler.sceneimage.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 场景编码不能为空
        String channelCode = personFingerRecog.getChannelCode();
        if (StringUtils.isBlank(channelCode)) {
            msg = MessageUtils.message("bio.trade.handler.scenecode.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 1-N搜索比对阈值不为空时，必须是数字
        String searchNThreshold = personFingerRecog.getSearchNThreshold();
        if (StringUtils.isNotBlank(searchNThreshold)) {
            try {
                Double.valueOf(searchNThreshold);
            } catch (Exception e) {
                msg = MessageUtils.message("bio.trade.handler.1n.threshold.empty.number");
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }
        // 返回TopN不为空时，必须是数字[1~10]
        String topN = personFingerRecog.getTopN();
        if (StringUtils.isNotBlank(topN)) {
            try {
                int val = Integer.valueOf(topN);
                if (val > 10 || val < 1) {
                    msg = MessageUtils.message("bio.trade.handler.topn.return.num.format");
                    return HttpAjaxResult.businessDataValidError(msg);
                }
            } catch (Exception e) {
                msg = MessageUtils.message("bio.trade.handler.topn.return.num.format");
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }
        // 子场景编码不为空时，格式必须为[场景号_子场景编码]
        String subTreasury = personFingerRecog.getSubTreasury();
        if (StringUtils.isNotBlank(subTreasury) && !subTreasury.startsWith(channelCode + "_")) {
            msg = MessageUtils.message("bio.trade.handler.subscene.format");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        return HttpAjaxResult.httpSuccess();
    }

    /**
     * 虹膜1:1认证
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult verifyPersonIris(String bizContent) {
        // json转换
        PersonIrisVerify personIrisVerify = null;
        try {
            personIrisVerify = JSONObject.parseObject(bizContent, PersonIrisVerify.class);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult
                .businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        // 参数校验
        AjaxResult ajaxResult = validateVerifyIrisParam(personIrisVerify);
        if (!HttpAjaxResult.HTTP_SUCC_CODE.equals(ajaxResult.get(AjaxResult.CODE_TAG))) {
            return ajaxResult;
        }
        try {
            // 进入1:1认证
            PersonIrisVerifyVO verifyVO = channelBusiIrisHttpService.verifyPersonIris(personIrisVerify);
            return HttpAjaxResult.httpSuccess(verifyVO);
        } catch (CustomException e) {
            LOG.error("Fingerprint 1:1 authentication failed", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("Fingerprint 1:1 authentication failed", e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 虹膜1:1比对参数校验
     * 
     * @param personIrisVerify
     * @return
     */
    private AjaxResult validateVerifyIrisParam(PersonIrisVerify personIrisVerify) {
        String msg = null;
        // 人员标识不能为空, 且长度不大于48
        String uniqueId = personIrisVerify.getUniqueId();
        if (StringUtils.isBlank(uniqueId) || uniqueId.length() > 48) {
            msg = MessageUtils.message("base.person.handler.uniqueid.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 业务流水号不能为空, 且长度不大于48
        String receivedSeq = personIrisVerify.getReceivedSeq();
        if (StringUtils.isBlank(receivedSeq) || receivedSeq.length() > 48) {
            msg = MessageUtils.message("base.person.handler.receivedseq.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 现场虹膜图片不能为空
        String sceneImage = personIrisVerify.getSceneImage();
        String sceneFeature = personIrisVerify.getSceneFeature();
        if (StringUtils.isBlank(sceneImage) && StringUtils.isBlank(sceneFeature)) {
            msg = MessageUtils.message("bio.trade.handler.iris.feature.image.all.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 场景编码不能为空
        String channelCode = personIrisVerify.getChannelCode();
        if (StringUtils.isBlank(channelCode)) {
            msg = MessageUtils.message("bio.trade.handler.scenecode.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 比对阈值不为空时，必须是数字
        String compareThreshold = personIrisVerify.getCompareThreshold();
        if (StringUtils.isNotBlank(compareThreshold)) {
            try {
                Double.valueOf(compareThreshold);
            } catch (Exception e) {
                msg = MessageUtils.message("bio.trade.handler.compare.threshold.empty.number");
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }
        return HttpAjaxResult.httpSuccess();
    }

    /**
     * 虹膜1:N识别
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult recogPersonIris(String bizContent) {
        // json转换
        PersonIrisRecog personIrisRecog = null;
        try {
            personIrisRecog = JSONObject.parseObject(bizContent, PersonIrisRecog.class);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult
                .businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        // 参数校验
        AjaxResult ajaxResult = validateRecogIrisParam(personIrisRecog);
        if (!HttpAjaxResult.HTTP_SUCC_CODE.equals(ajaxResult.get(AjaxResult.CODE_TAG))) {
            return ajaxResult;
        }
        try {
            // 进入1:N识别
            List<PersonIrisRecogVO> personIrisRecogVOList = channelBusiIrisHttpService.recogPersonIris(personIrisRecog);
            return HttpAjaxResult.httpSuccess(personIrisRecogVOList);
        } catch (CustomException e) {
            LOG.error("Fingerprint 1:N recognition failed", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("Fingerprint 1:N recognition failed", e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 虹膜识别参数校验
     * 
     * @param personIrisRecog
     * @return
     */
    private AjaxResult validateRecogIrisParam(PersonIrisRecog personIrisRecog) {
        String msg = null;
        // 业务流水号不能为空, 且长度不大于48
        String receivedSeq = personIrisRecog.getReceivedSeq();
        if (StringUtils.isBlank(receivedSeq) || receivedSeq.length() > 48) {
            msg = MessageUtils.message("base.person.handler.receivedseq.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 现场照校验
        String sceneImage = personIrisRecog.getSceneImage();
        String sceneFeature = personIrisRecog.getSceneFeature();
        if (StringUtils.isBlank(sceneImage) && StringUtils.isBlank(sceneFeature)) {
            msg = MessageUtils.message("bio.trade.handler.iris.feature.image.all.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 场景编码不能为空
        String channelCode = personIrisRecog.getChannelCode();
        if (StringUtils.isBlank(channelCode)) {
            msg = MessageUtils.message("bio.trade.handler.scenecode.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 1-N搜索比对阈值不为空时，必须是数字
        String searchNThreshold = personIrisRecog.getSearchNThreshold();
        if (StringUtils.isNotBlank(searchNThreshold)) {
            try {
                Double.valueOf(searchNThreshold);
            } catch (Exception e) {
                msg = MessageUtils.message("bio.trade.handler.1n.threshold.empty.number");
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }
        // 返回TopN不为空时，必须是数字[1~10]
        String topN = personIrisRecog.getTopN();
        if (StringUtils.isNotBlank(topN)) {
            try {
                int val = Integer.valueOf(topN);
                if (val > 10 || val < 1) {
                    msg = MessageUtils.message("bio.trade.handler.topn.return.num.format");
                    return HttpAjaxResult.businessDataValidError(msg);
                }
            } catch (Exception e) {
                msg = MessageUtils.message("bio.trade.handler.topn.return.num.format");
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }
        // 子场景编码不为空时，格式必须为[场景编码_子场景编码]
        String subTreasury = personIrisRecog.getSubTreasury();
        if (StringUtils.isNotBlank(subTreasury) && !subTreasury.startsWith(channelCode + "_")) {
            msg = MessageUtils.message("bio.trade.handler.subscene.format");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        return HttpAjaxResult.httpSuccess();
    }

    /**
     * 查询设备信息
     * 
     * @param deviceNo
     * @return
     */
    private DeviceInfo getDeviceInfoByDeviceNo(String deviceNo) {
        // 清空在此之前设备之的TenantContextHolder上下文信息，不携带租户隔离查询设备信息
        String tenantId = TenantContextHolder.getTenantId();
        TenantContextHolder.clear();
        DeviceInfo condition = new DeviceInfo();
        condition.setDeviceNo(deviceNo);
        List<DeviceInfo> list = deviceInfoService.selectDeviceInfoList(condition);
        // 重新设置租户信息
        if (StringUtils.isNotBlank(tenantId)) {
            TenantContextHolder.setTenantId(tenantId);
        }
        if (CollectionUtils.isEmpty(list)) {
            LOG.error("Device [{}] does not exist", deviceNo);
            return null;
        }
        return list.get(0);
    }

    /**
     * 校验设备所属租户和接口授权租户是否一致
     *
     * @param deviceInfo
     * @return
     */
    private boolean validateDeviceTenant(DeviceInfo deviceInfo) {
        if (!tenantProperties.getEnabled()) {
            return true;
        }
        String deviceNo = deviceInfo.getDeviceNo();
        LOG.info("Device [{}] belongs to tenant [{}]", deviceNo, deviceInfo.getTenantId());
        if (StringUtils.isBlank(deviceInfo.getTenantId())) {
            LOG.error("Device [{}] does not maintain tenant information", deviceNo);
            return false;
        }
        // 验证appKey所属租户和设备所属租户是否一致
        String tenantId = TenantContextHolder.getTenantId();
        if (!deviceInfo.getTenantId().equals(tenantId)) {
            LOG.error(
                "The tenant ID of the device [{}] is [{}], which is inconsistent with the authorized tenant ID [{}]",
                deviceNo, deviceInfo.getTenantId(), tenantId);
            return false;
        }
        return true;
    }

    /**
     * 联网身份核查接口
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult personIdentityVerification(String bizContent) {
        // json转换
        PersonIdentityVerification personIdVerification = null;
        try {
            personIdVerification = JSONObject.parseObject(bizContent, PersonIdentityVerification.class);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult
                .businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        String msg = null;
        // 业务流水号不能为空, 且长度不大于48
        String receivedSeq = personIdVerification.getReceivedSeq();
        if (StringUtils.isBlank(receivedSeq) || receivedSeq.length() > 48) {
            msg = MessageUtils.message("base.person.handler.receivedseq.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }

        String deviceCode = personIdVerification.getDeviceCode();
        if (StringUtils.isBlank(deviceCode)) {
            msg = MessageUtils.message("base.person.handler.devicecdoe.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        DeviceInfo deviceInfo = getDeviceInfoByDeviceNo(deviceCode);
        if (null == deviceInfo) {
            msg = MessageUtils.message("bio.trade.handler.device.not.exists", deviceCode);
            LOG.error(msg);
            return HttpAjaxResult.businessError(msg);
        }
        // 验证设备租户和授权appKey所属租户是否一致
        if (!validateDeviceTenant(deviceInfo)) {
            msg = MessageUtils.message("base.person.handler.device.tenant.error", deviceCode);
            return HttpAjaxResult.businessError(msg);
        }
        personIdVerification.setChannelCode(deviceInfo.getChannelCode());
        personIdVerification.setDeviceModel(deviceInfo.getDeviceModelCode());
        if (StringUtils.isNotBlank(deviceInfo.getDeviceName())) {
            personIdVerification.setDevicName(deviceInfo.getDeviceName());
        }
        if (StringUtils.isNotBlank(deviceInfo.getDeviceIp())) {
            personIdVerification.setDeviceIp(deviceInfo.getDeviceIp());
        }
        if (StringUtils.isNotNull(deviceInfo.getLongitude())) {
            personIdVerification.setDeviceLongitude(String.valueOf(deviceInfo.getLongitude()));
        }
        if (StringUtils.isNotNull(deviceInfo.getLatitude())) {
            personIdVerification.setDeviceDimension(String.valueOf(deviceInfo.getLatitude()));
        }
        personIdVerification.setDeviceDirection(deviceInfo.getDeviceDirection());
        String name = personIdVerification.getName();
        if (StringUtils.isBlank(name)) {
            msg = MessageUtils.message("bio.trade.handler.person.name.empty");
            LOG.error(msg);
            return HttpAjaxResult.businessDataValidError(msg);
        }
        String uniqueId = personIdVerification.getUniqueId();
        if (StringUtils.isBlank(uniqueId)) {
            msg = MessageUtils.message("base.person.handler.uniqueid.empty");
            LOG.error(msg);
            return HttpAjaxResult.businessDataValidError(msg);
        }
        String sceneImageBase64 = personIdVerification.getSceneImageBase64();
        if (StringUtils.isBlank(sceneImageBase64)) {
            msg = MessageUtils.message("bio.trade.handler.sceneimage.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 前端测温+后端比对，校验温度检测结果和传入温度格式
        String temperatureResult = personIdVerification.getTemperatureResult();
        String temperatureFloor = personIdVerification.getTemperatureFloor();
        String temperatureTop = personIdVerification.getTemperatureTop();
        String temperature = personIdVerification.getTemperature();
        if (StringUtils.isNotBlank(temperatureResult)) {
            if (!DictConstants.BioResult.PASS.equals(temperatureResult)
                && !DictConstants.BioResult.NOTPASS.equals(temperatureResult)) {
                msg = MessageUtils.message("bio.trade.handler.temperature.result.error");
                LOG.error(msg);
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }
        if (StringUtils.isNotBlank(temperatureFloor) && !DoubleValidator.getInstance().isValid(temperatureFloor)) {
            msg = MessageUtils.message("bio.trade.handler.temperature.min.error");
            LOG.error(msg);
            return HttpAjaxResult.businessDataValidError(msg);
        }
        if (StringUtils.isNotBlank(temperatureTop) && !DoubleValidator.getInstance().isValid(temperatureTop)) {
            msg = MessageUtils.message("bio.trade.handler.temperature.max.error");
            LOG.error(msg);
            return HttpAjaxResult.businessDataValidError(msg);
        }
        if (StringUtils.isNotBlank(temperature) && !DoubleValidator.getInstance().isValid(temperature)) {
            msg = MessageUtils.message("bio.trade.handler.temperature.error");
            LOG.error(msg);
            return HttpAjaxResult.businessDataValidError(msg);
        }
        try {
            Map<String, Object> resultMap = channelBusiFaceHttpService.personIdentityVerification(personIdVerification);
            return HttpAjaxResult.httpSuccess(resultMap);
        } catch (CustomException e) {
            LOG.error("Personnel network verification failed", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("Personnel network verification failed", e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 人脸虹膜多模态1vN搜索
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult irisFaceSearch(String bizContent) {
        // TODO 暂时返回一个假数据
        Map<String, Object> resultMap = new HashedMap<String, Object>();
        resultMap.put("uniqueId", "zhangsan");
        resultMap.put("name", "张三");
        resultMap.put("score", 90.0D);
        resultMap.put("usedTime", 300);
        resultMap.put("receivedSeq", "2021012121212");
        resultMap.put("handleSeq", "2021012121223");
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(resultMap);
        return HttpAjaxResult.httpSuccess(list);
    }

    /**
     * 人脸虹膜多模态1v1比对
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult irisFaceVerify(String bizContent) {
        // json转换
        PersonIrisFaceVerify personIrisFaceVerify = null;
        try {
            personIrisFaceVerify = JSONObject.parseObject(bizContent, PersonIrisFaceVerify.class);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult
                .businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        // 参数校验
        AjaxResult ajaxResult = validateVerifyIrisFaceParam(personIrisFaceVerify);
        if (!HttpAjaxResult.HTTP_SUCC_CODE.equals(ajaxResult.get(AjaxResult.CODE_TAG))) {
            return ajaxResult;
        }
        try {
            // 进入1:1认证
            PersonIrisFaceVerifyVO verifyVO = channelBusiIrisFaceHttpService.verifyPersonIrisFace(personIrisFaceVerify);
            return HttpAjaxResult.httpSuccess(verifyVO);
        } catch (CustomException e) {
            LOG.error("Face-iris multi-modal 1:1 authentication failed", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("Face-iris multi-modal 1:1 authentication failed", e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 人脸虹膜多模态认证参数校验
     * 
     * @param personIrisFaceVerify
     * @return
     */
    private AjaxResult validateVerifyIrisFaceParam(PersonIrisFaceVerify personIrisFaceVerify) {
        String msg = null;
        // 业务流水号不能为空, 且长度不大于48
        String receivedSeq = personIrisFaceVerify.getReceivedSeq();
        if (StringUtils.isBlank(receivedSeq) || receivedSeq.length() > 48) {
            msg = MessageUtils.message("base.person.handler.receivedseq.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        String deviceCode = personIrisFaceVerify.getDeviceCode();
        // 设备编码不为空，说明是受平台管控的设备直接调用后端服务接口，需要进行设备合法性校验
        if (StringUtils.isNotBlank(deviceCode)) {
            DeviceInfo deviceInfo = getDeviceInfoByDeviceNo(deviceCode);
            if (null == deviceInfo) {
                msg = MessageUtils.message("bio.trade.handler.device.not.exists", deviceCode);
                LOG.error(msg);
                return HttpAjaxResult.businessError(msg);
            }
            // 验证设备租户和授权appKey所属租户是否一致
            if (!validateDeviceTenant(deviceInfo)) {
                msg = MessageUtils.message("base.person.handler.device.tenant.error", deviceCode);
                return HttpAjaxResult.businessError(msg);
            }
            // 根据设备确定场景和子场景，传输的参数不起作用
            if (StringUtils.isBlank(deviceInfo.getChannelCode())) {
                msg = MessageUtils.message("base.person.handler.device.not.bind.scene", deviceCode);
                LOG.error(msg);
                return HttpAjaxResult.businessError(msg);
            }
            personIrisFaceVerify.setChannelCode(deviceInfo.getChannelCode());
            personIrisFaceVerify.setDeviceModel(deviceInfo.getDeviceModelCode());
            if (StringUtils.isNotBlank(deviceInfo.getDeviceName())) {
                personIrisFaceVerify.setDeviceName(deviceInfo.getDeviceName());
            }
            if (StringUtils.isNotBlank(deviceInfo.getDeviceIp())) {
                personIrisFaceVerify.setDeviceIp(deviceInfo.getDeviceIp());
            }
            if (StringUtils.isNotNull(deviceInfo.getLongitude())) {
                personIrisFaceVerify.setDeviceLongitude(String.valueOf(deviceInfo.getLongitude()));
            }
            if (StringUtils.isNotNull(deviceInfo.getLatitude())) {
                personIrisFaceVerify.setDeviceDimension(String.valueOf(deviceInfo.getLatitude()));
            }
            personIrisFaceVerify.setDeviceDirection(deviceInfo.getDeviceDirection());
            personIrisFaceVerify.setDeviceAddr(deviceInfo.getDeviceAddr());
        }
        // 人员标识不能为空, 且长度不大于48
        String uniqueId = personIrisFaceVerify.getUniqueId();
        if (StringUtils.isBlank(uniqueId) || uniqueId.length() > 48) {
            msg = MessageUtils.message("base.person.handler.uniqueid.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        String verifyType = personIrisFaceVerify.getVerifyType();
        if (!DictConstants.MuliIrisFaceVerifyType.muliIrisFaceVerifyTypeList.contains(verifyType)) {
            msg = MessageUtils.message("bio.trade.handler.verify.type.invalid");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        String faceBase64Img = personIrisFaceVerify.getFaceBase64Img();
        String faceFeatureBase64 = personIrisFaceVerify.getFaceFeatureBase64();
        String irisBase64Img = personIrisFaceVerify.getIrisBase64Img();
        String irisFeatureBase64 = personIrisFaceVerify.getIrisFeatureBase64();
        // 虹膜比对
        if (DictConstants.MuliIrisFaceVerifyType.VERIFY_IRIS.equals(verifyType) && StringUtils.isBlank(irisBase64Img)
            && StringUtils.isBlank(irisFeatureBase64)) {
            msg = MessageUtils.message("bio.trade.handler.iris.feature.image.all.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 人脸比对
        if (DictConstants.MuliIrisFaceVerifyType.VERIFY_FACE.equals(verifyType) && StringUtils.isBlank(faceBase64Img)
            && StringUtils.isBlank(faceFeatureBase64)) {
            msg = MessageUtils.message("base.person.handler.face.feature.image.all.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 虹膜或者人脸比对
        if (DictConstants.MuliIrisFaceVerifyType.VERIFY_IRIS_OR_FACE.equals(verifyType)) {
            if (StringUtils.isBlank(irisBase64Img) && StringUtils.isBlank(irisFeatureBase64)) {
                msg = MessageUtils.message("base.person.handler.iris.feature.image.all.empty");
                return HttpAjaxResult.businessDataValidError(msg);
            }
            if (StringUtils.isBlank(faceBase64Img) && StringUtils.isBlank(faceFeatureBase64)) {
                msg = MessageUtils.message("base.person.handler.iris.feature.image.all.empty");
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }
        // 虹膜和人脸比对
        if (DictConstants.MuliIrisFaceVerifyType.VERIFY_IRIS_AND_FACE.equals(verifyType)) {
            if (StringUtils.isBlank(irisBase64Img) && StringUtils.isBlank(irisFeatureBase64)) {
                msg = MessageUtils.message("base.person.handler.iris.feature.image.all.empty");
                return HttpAjaxResult.businessDataValidError(msg);
            }
            if (StringUtils.isBlank(faceBase64Img) && StringUtils.isBlank(faceFeatureBase64)) {
                msg = MessageUtils.message("base.person.handler.face.feature.image.all.empty");
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }
        // 虹膜人脸多模态融合比对
        if (DictConstants.MuliIrisFaceVerifyType.VERIFY_IRIS_FACE_MULTI.equals(verifyType)) {
            if (StringUtils.isBlank(irisBase64Img) && StringUtils.isBlank(irisFeatureBase64)) {
                msg = MessageUtils.message("base.person.handler.iris.feature.image.all.empty");
                return HttpAjaxResult.businessDataValidError(msg);
            }
            if (StringUtils.isBlank(faceBase64Img) && StringUtils.isBlank(faceFeatureBase64)) {
                msg = MessageUtils.message("base.person.handler.face.feature.image.all.empty");
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }
        // 场景编码不能为空
        String channelCode = personIrisFaceVerify.getChannelCode();
        if (StringUtils.isBlank(channelCode)) {
            msg = MessageUtils.message("bio.trade.handler.scenecode.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 是否检活不为空时，只能是[Y/N]之一
        String liveDetection = personIrisFaceVerify.getLiveDetection();
        List<String> list = Arrays.asList(DictConstants.YesOrNoState.YES, DictConstants.YesOrNoState.NO);
        if (StringUtils.isNotBlank(liveDetection) && !list.contains(liveDetection)) {
            msg = MessageUtils.message("bio.trade.handler.livedetect.yn");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 检活阈值不为空时，必须是数字
        String liveDetectionThreshold = personIrisFaceVerify.getLiveDetectionThreshold();
        if (StringUtils.isNotBlank(liveDetectionThreshold)) {
            try {
                Double.valueOf(liveDetectionThreshold);
            } catch (Exception e) {
                msg = MessageUtils.message("bio.trade.handler.livedetect.threshold.empty.number");
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }
        // 比对阈值不为空时，必须是数字
        String irisCompareThreshold = personIrisFaceVerify.getIrisCompareThreshold();
        if (StringUtils.isNotBlank(irisCompareThreshold)
            && !DoubleValidator.getInstance().isValid(irisCompareThreshold)) {
            msg = MessageUtils.message("bio.trade.handler.compare.threshold.empty.number");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        String faceCompareThreshold = personIrisFaceVerify.getFaceCompareThreshold();
        if (StringUtils.isNotBlank(faceCompareThreshold)
            && !DoubleValidator.getInstance().isValid(faceCompareThreshold)) {
            msg = MessageUtils.message("bio.trade.handler.compare.threshold.empty.number");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        String fusionCompareThreshold = personIrisFaceVerify.getFusionCompareThreshold();
        if (StringUtils.isNotBlank(fusionCompareThreshold)
            && !DoubleValidator.getInstance().isValid(fusionCompareThreshold)) {
            msg = MessageUtils.message("bio.trade.handler.compare.threshold.empty.number");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 前端测温+后端比对，校验温度检测结果和传入温度格式
        String temperatureResult = personIrisFaceVerify.getTemperatureResult();
        String temperatureFloor = personIrisFaceVerify.getTemperatureFloor();
        String temperatureTop = personIrisFaceVerify.getTemperatureTop();
        String temperature = personIrisFaceVerify.getTemperature();
        if (StringUtils.isNotBlank(temperatureResult)) {
            if (!DictConstants.BioResult.PASS.equals(temperatureResult)
                && !DictConstants.BioResult.NOTPASS.equals(temperatureResult)) {
                msg = MessageUtils.message("bio.trade.handler.temperature.result.error", temperatureResult);
                LOG.error(msg);
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }
        if (StringUtils.isNotBlank(temperatureFloor) && !DoubleValidator.getInstance().isValid(temperatureFloor)) {
            msg = MessageUtils.message("bio.trade.handler.temperature.min.error");
            LOG.error(msg);
            return HttpAjaxResult.businessDataValidError(msg);
        }
        if (StringUtils.isNotBlank(temperatureTop) && !DoubleValidator.getInstance().isValid(temperatureTop)) {
            msg = MessageUtils.message("bio.trade.handler.temperature.max.error");
            LOG.error(msg);
            return HttpAjaxResult.businessDataValidError(msg);
        }
        if (StringUtils.isNotBlank(temperature) && !DoubleValidator.getInstance().isValid(temperature)) {
            msg = MessageUtils.message("bio.trade.handler.temperature.error");
            LOG.error(msg);
            return HttpAjaxResult.businessDataValidError(msg);
        }
        return HttpAjaxResult.httpSuccess();
    }

}
