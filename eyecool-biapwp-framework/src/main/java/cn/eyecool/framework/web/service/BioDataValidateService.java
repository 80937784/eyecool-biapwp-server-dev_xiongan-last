package cn.eyecool.framework.web.service;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eyecool.abis.callmicroservice.IMultiFeatureService;
import com.eyecool.abis.callmicroservice.MicroConstants;
import com.eyecool.abis.callmicroservice.common.FaceExtractResult;
import com.eyecool.abis.callmicroservice.common.FeatureBean;
import com.eyecool.abis.callmicroservice.common.FingerExtractResult;
import com.eyecool.abis.callmicroservice.common.IrisExtractResult;

import cn.eyecool.common.constant.Constants;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.core.domain.entity.SysUser;
import cn.eyecool.common.exception.UserBioNotCollectException;
import cn.eyecool.common.exception.UserBioNotMatchException;
import cn.eyecool.common.match.MultiFusionFeatureService;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.framework.manager.AsyncManager;
import cn.eyecool.framework.manager.factory.AsyncFactory;
import cn.eyecool.match.service.commons.FeatureData;
import cn.eyecool.system.domain.SysUserFace;
import cn.eyecool.system.domain.SysUserFinger;
import cn.eyecool.system.domain.SysUserIris;
import cn.eyecool.system.domain.SysUserIrisFace;
import cn.eyecool.system.mapper.SysUserFaceMapper;
import cn.eyecool.system.mapper.SysUserFingerMapper;
import cn.eyecool.system.mapper.SysUserIrisFaceMapper;
import cn.eyecool.system.mapper.SysUserIrisMapper;
import cn.eyecool.system.service.ISysUserFaceRecogLogicService;
import cn.eyecool.system.service.ISysUserFingerRecogLogicService;
import cn.eyecool.system.service.ISysUserIrisRecogLogicService;

/**
 * @ClassName BioFeatureService
 * @Description 生物特征登录方法
 * @Author csm
 * @date:2021-1-6
 * @Version 1.0
 */
@Component
public class BioDataValidateService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BioDataValidateService.class);

    @Autowired
    private SysUserFaceMapper faceMapper;
    @Autowired
    private SysUserFingerMapper fingerMapper;
    @Autowired
    private SysUserIrisMapper irisMapper;
    @Autowired
    private SysUserIrisFaceMapper irisFaceMapper;
    @Autowired
    private ISysUserFaceRecogLogicService sysUserFaceRecogLogicService;
    @Autowired
    private ISysUserFingerRecogLogicService sysUserFingerRecogLogicService;
    @Autowired
    private ISysUserIrisRecogLogicService sysUserIrisRecogLogicService;
    @Autowired
    private IMultiFeatureService multiFeatureService;
    @Autowired
    private MultiFusionFeatureService fusionFeatureService;

    /**
     * 生物特征校验
     *
     * @param loginType
     * @param user
     * @param bioData
     */
    public void validate(String loginType, SysUser user, String bioData) {
        String loginName = user.getUserName();

        boolean flag = false;
        LOGGER.info("loginType:{}", loginType);
        switch (loginType) {
            case DictConstants.BioFeatureType.FACE_FEATURE:
                flag = matchFace(user, bioData);
                break;
            case DictConstants.BioFeatureType.FINGER_FEATURE_UNKNOWN:
                flag = matchFinger(user, bioData);
                break;
            case DictConstants.BioFeatureType.IRIS_FEATURE_UNKNOWN:
                flag = matchIris(user, bioData);
                break;
            case DictConstants.BioFeatureType.FACE_IRIS_FEATURE:
                flag = matchFaceIris(user, bioData);
                break;
            default:
                break;

        }
        if (!flag) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(user.getTenantId(), loginName, Constants.LOGIN_FAIL,
                MessageUtils.message("user.bio.not.match")));
            throw new UserBioNotMatchException();
        }
    }

    private boolean matchFace(SysUser user, String bioData) {
        SysUserFace condition = new SysUserFace();
        condition.setUserId(user.getUserId());
        condition.setStatus(DictConstants.Status.ENABLE);
        List<SysUserFace> sysUserFaces = faceMapper.selectSysUserFaceList(condition);
        if (CollectionUtils.isEmpty(sysUserFaces)) {
            throw new UserBioNotCollectException();
        }
        String feature = sysUserFaces.get(0).getFeature();
        FaceExtractResult faceExtractResult = sysUserFaceRecogLogicService.getFaceExtractResult(bioData);
        FeatureBean featureScene =
            sysUserFaceRecogLogicService.getFeatureBean(faceExtractResult, MessageUtils.message("bio.data.validate.upload.face.image"), MessageUtils.message("bio.data.validate.upload.clear.face.image"));
        FeatureBean stockFeature = new FeatureBean();
        stockFeature.setFeature(feature);
        stockFeature.setType(String.valueOf(FeatureData.FeatureType.FaceFeature));
        double score = sysUserFaceRecogLogicService.faceOne2OneCompare(featureScene, stockFeature, 0d, MessageUtils.message("bio.data.validate.upload.clear.face.image"));
        LOGGER.info("face login comparison,username:[{}],score:[{}],threshold：[{}]", user.getUserName(), score, 55);
        // FIXME 阈值不确定
        return score > 85;
    }

    private boolean matchFinger(SysUser user, String bioData) {
        SysUserFinger condition = new SysUserFinger();
        condition.setUserId(user.getUserId());
        condition.setStatus(DictConstants.Status.ENABLE);
        List<SysUserFinger> sysUserFingers = fingerMapper.selectSysUserFingerList(condition);
        if (CollectionUtils.isEmpty(sysUserFingers)) {
            throw new UserBioNotCollectException();
        }
        boolean flag = false;
        FingerExtractResult fingerExtractResult = sysUserFingerRecogLogicService.getFingerExtractResult(bioData);
        FeatureBean featureBean =
            sysUserFingerRecogLogicService.getFeatureBean(fingerExtractResult, MessageUtils.message("bio.data.validate.upload.finger.image"), MessageUtils.message("bio.data.validate.upload.onehand.finger.image"));
        for (SysUserFinger sysUserFinger : sysUserFingers) {
            String feature = sysUserFinger.getFeature();
            FeatureBean stockFeature = new FeatureBean();
            stockFeature.setFeature(feature);
            stockFeature.setType(String.valueOf(FeatureData.FeatureType.FingerFeatureUnknown));
            double score = sysUserFingerRecogLogicService.fingerOne2OneCompare(Arrays.asList(featureBean, stockFeature))
                .get(0).getResults().get(0).getScore();
            // FIXME 阈值不确定
            flag = flag || (score > 55);
            LOGGER.info("Fingerprint login comparison,username:[{}],score:[{}],threshold：[{}]", user.getUserName(), score, 55);
        }
        return flag;
    }

    private boolean matchIris(SysUser user, String bioData) {
        SysUserIris condition = new SysUserIris();
        condition.setUserId(user.getUserId());
        condition.setStatus(DictConstants.Status.ENABLE);
        List<SysUserIris> sysUserIrises = irisMapper.selectSysUserIrisList(condition);
        if (CollectionUtils.isEmpty(sysUserIrises)) {
            throw new UserBioNotCollectException();
        }
        JSONObject bioObject = JSON.parseObject(bioData);
        String sceneImageBase64 = bioObject.getString("imageBase64");
        String sceneFeature = bioObject.getString("feature");
        FeatureBean featureBean = new FeatureBean();
        featureBean.setFeature(sceneFeature);
        featureBean.setType(String.valueOf(FeatureData.FeatureType.IrisFeatureUnknown));
        if (StringUtils.isBlank(sceneFeature)) {
            IrisExtractResult irisExtractResult = sysUserIrisRecogLogicService.getIrisExtractResult(sceneImageBase64);
            featureBean = sysUserIrisRecogLogicService.getFeatureBean(irisExtractResult, MessageUtils.message("bio.data.validate.upload.iris.image"), MessageUtils.message("bio.data.validate.upload.iris.image.one"));
        }
        SysUserIris sysUserIris = sysUserIrises.get(0);
        String feature = sysUserIris.getFeature();
        FeatureBean stockFeature = new FeatureBean();
        stockFeature.setFeature(feature);
        stockFeature.setType(String.valueOf(FeatureData.FeatureType.IrisFeatureUnknown));
        double score = sysUserIrisRecogLogicService.irisOne2OneCompare(Arrays.asList(featureBean, stockFeature)).get(0)
            .getResults().get(0).getScore();
        LOGGER.info("Iris login comparison,username:[{}],score:[{}],threshold：[{}]", user.getUserName(), score, 55);
        // FIXME 阈值不确定
        return score > 55;
    }

    private boolean matchFaceIris(SysUser user, String bioData) {
        SysUserIrisFace condition = new SysUserIrisFace();
        condition.setUserId(user.getUserId());
        condition.setStatus(DictConstants.Status.ENABLE);
        List<SysUserIrisFace> sysUserIrisFaces = irisFaceMapper.selectSysUserIrisFaceList(condition);
        if (CollectionUtils.isEmpty(sysUserIrisFaces)) {
            throw new UserBioNotCollectException();
        }
        SysUserIrisFace userIrisFace = sysUserIrisFaces.get(0);
        JSONObject bioObject = JSON.parseObject(bioData);
        String faceImgBase64 = bioObject.getString("faceImgBase64");
        String irisImgBase64 = bioObject.getString("irisImgBase64");
        String irisFeature = bioObject.getString("irisFeature");
        if (StringUtils.isBlank(irisFeature)) {
            try {
                irisFeature = multiFeatureService.extractFeatureByImage(irisImgBase64, MicroConstants.AlgType.IRIS);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                return false;
            }
        }
        String faceSceneFeature;
        try {
            faceSceneFeature = multiFeatureService.extractFeatureByImage(faceImgBase64, MicroConstants.AlgType.FACE);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
        String fusionFeature = fusionFeatureService.fusionFeature(faceSceneFeature, irisFeature);
        float score = fusionFeatureService.matchFusionFeatures(fusionFeature, userIrisFace.getFusionFeature());
        LOGGER.info("Multimodal login comparison,username:[{}],score:[{}],threshold：[{}]", user.getUserName(), score, 80);
        // FIXME 阈值不确定
        return score > 85;
    }
}
