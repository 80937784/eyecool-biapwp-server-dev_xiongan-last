package cn.eyecool.basedata.service.impl;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eyecool.abis.callmicroservice.IMultiFeatureService;
import com.eyecool.abis.callmicroservice.MicroConstants;
import com.eyecool.abis.callmicroservice.MicroConstants.AlgType;

import cn.eyecool.basedata.domain.BasePersonCert;
import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.basedata.domain.BasePersonIrisFace;
import cn.eyecool.basedata.domain.BasePersonPutInfo;
import cn.eyecool.basedata.domain.IrisFaceRegister;
import cn.eyecool.basedata.enums.MultiRegistOptionTypeEnum;
import cn.eyecool.basedata.event.PersonChangeEventPublishService;
import cn.eyecool.basedata.manager.IPersonFaceRecogLogicService;
import cn.eyecool.basedata.manager.IPersonIrisRecogLogicService;
import cn.eyecool.basedata.mapper.BasePersonCertMapper;
import cn.eyecool.basedata.mapper.BasePersonInfoMapper;
import cn.eyecool.basedata.mapper.BasePersonIrisFaceMapper;
import cn.eyecool.basedata.service.IBasePersonIrisFaceService;
import cn.eyecool.common.annotation.DataScope;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.match.MultiFusionFeatureService;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.sign.Md5Utils;
import cn.eyecool.system.service.ISysConfigService;
import lombok.extern.slf4j.Slf4j;

/**
 * 虹膜人脸多模态Service业务层处理
 *
 * @author mawj
 * @date 2021-01-27
 */
@Service
@Slf4j
public class BasePersonIrisFaceServiceImpl implements IBasePersonIrisFaceService {

    @Autowired
    private BasePersonIrisFaceMapper basePersonIrisFaceMapper;
    @Autowired
    private BasePersonInfoMapper basePersonInfoMapper;
    @Autowired
    private IPersonFaceRecogLogicService personFaceRecogLogicService;
    @Autowired
    private IPersonIrisRecogLogicService personIrisRecogLogicService;
    @Autowired
    private PersonChangeEventPublishService personChangeEventPublishService;
    @Autowired
    private ISysConfigService configService;
    @Autowired
    private IMultiFeatureService multiFeatureService;
    @Autowired
    private MultiFusionFeatureService multiFusionFeatureService;
    @Autowired
    private BasePersonCertMapper basePersonCertMapper;

    /**
     * 查询虹膜人脸多模态
     *
     * @param id 虹膜人脸多模态ID
     * @return 虹膜人脸多模态
     */
    @Override
    public BasePersonIrisFace selectBasePersonIrisFaceById(String id) {
        return basePersonIrisFaceMapper.selectBasePersonIrisFaceById(id);
    }

    /**
     * 查询虹膜人脸多模态列表
     *
     * @param basePersonIrisFace 虹膜人脸多模态
     * @return 虹膜人脸多模态
     */
    @Override
    @DataScope(deptAlias = "info")
    public List<BasePersonIrisFace> selectBasePersonIrisFaceList(BasePersonIrisFace basePersonIrisFace) {
        return basePersonIrisFaceMapper.selectBasePersonIrisFaceList(basePersonIrisFace);
    }

    /**
     * 新增虹膜人脸多模态
     *
     * @param basePersonIrisFace 虹膜人脸多模态
     * @return 结果
     */
    @Override
    @Transactional
    public int insertBasePersonIrisFace(BasePersonIrisFace basePersonIrisFace) {
        // 校验人员和图片是存在
        BasePersonInfo basePersonInfo = getEnabledPersonInfo(basePersonIrisFace.getUniqueId());
        if (null == basePersonInfo) {
            throw new CustomException(MessageUtils.message("base.person.iris.face.valid.user.not.exists"));
        }

        String personId = basePersonInfo.getId();
        basePersonIrisFace.setPersonId(personId);

        // 查询人脸图片信息是否存在
        if (null != getEnabledPersonIrisFaceByPersonId(personId)) {
            throw new CustomException(MessageUtils.message("base.person.iris.face.multimodal.exists"));
        }
        // 处理特征
        try {
            basePersonIrisFace = handleFeature(basePersonIrisFace);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new CustomException(
                MessageUtils.message("base.person.iris.face.multimodal.feature.error", e.getMessage()));
        }
        // 其他属性设置
        try {
            basePersonIrisFace.setCreateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        basePersonIrisFace.setId(IdWorker.getNextStringId());
        basePersonIrisFace.setCreateTime(DateUtils.getNowDate());
        basePersonIrisFace.setPersonId(basePersonInfo.getId());
        String loginName = null;
        try {
            loginName = SecurityUtils.getUsername();
            basePersonIrisFace.setCreateBy(loginName);
        } catch (Exception e) {
        }
        if (StringUtils.isBlank(basePersonIrisFace.getDatasource())) {
            basePersonIrisFace.setDatasource(DictConstants.DataSource.INTERFACE);
        }
        // 文件落盘
        basePersonIrisFace.setEncrypted(DictConstants.Encrypted.ENABLE);
        String baseDir = getMulitImgBaseDir();
        String facePath = personFaceRecogLogicService.uploadFaceImg(Boolean.TRUE, null,
            basePersonIrisFace.getFaceImgBase64(), baseDir + "face" + File.separator);
        String irisPath = personIrisRecogLogicService.uploadIrisImg(Boolean.TRUE, null,
            basePersonIrisFace.getIrisImgBase64(), baseDir + "iris" + File.separator);
        basePersonIrisFace.setFaceImageUrl(facePath);
        basePersonIrisFace.setIrisImageUrl(irisPath);

        int result = basePersonIrisFaceMapper.insertBasePersonIrisFace(basePersonIrisFace);
        // 发布人员信息改变事件
        personChangeEventPublishService.personIrisFaceChangePublish(basePersonIrisFace.getPersonId(),
            basePersonIrisFace.getUniqueId(), null);
        return result;
    }

    /**
     * 修改虹膜人脸多模态
     *
     * @param basePersonIrisFace 虹膜人脸多模态
     * @return 结果
     */
    @Override
    @Transactional
    public int updateBasePersonIrisFace(BasePersonIrisFace basePersonIrisFace) {
        // 特征处理
        try {
            basePersonIrisFace = handleFeature(basePersonIrisFace);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new CustomException(
                MessageUtils.message("base.person.iris.face.multimodal.feature.error", e.getMessage()));
        }
        try {
            basePersonIrisFace.setUpdateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        basePersonIrisFace.setUpdateTime(DateUtils.getNowDate());
        boolean encrypted = DictConstants.Encrypted.ENABLE.equals(basePersonIrisFace.getEncrypted());
        String baseDir = getMulitImgBaseDir();
        if (StringUtils.isNotEmpty(basePersonIrisFace.getFaceImgBase64())) {
            String facePath = personFaceRecogLogicService.uploadFaceImg(encrypted, null,
                basePersonIrisFace.getFaceImgBase64(), baseDir + "face" + File.separator);
            basePersonIrisFace.setFaceImageUrl(facePath);
        }
        if (StringUtils.isNotEmpty(basePersonIrisFace.getIrisImgBase64())) {
            String irisPath = personIrisRecogLogicService.uploadIrisImg(encrypted, null,
                basePersonIrisFace.getIrisImgBase64(), baseDir + "iris" + File.separator);
            basePersonIrisFace.setIrisImageUrl(irisPath);
        }
        int result = basePersonIrisFaceMapper.updateBasePersonIrisFace(basePersonIrisFace);
        // 发布人员信息改变事件
        personChangeEventPublishService.personIrisFaceChangePublish(basePersonIrisFace.getPersonId(),
            basePersonIrisFace.getUniqueId(), null);
        return result;
    }

    /**
     * 获取多模态图片存储基础路径
     *
     * @return
     */
    private String getMulitImgBaseDir() {
        String baseDir = configService.selectConfigByKey(SysConfigConstants.BASE_MULIT_DIR_KEY);
        if (StringUtils.isBlank(baseDir)) {
            throw new CustomException(MessageUtils.message("base.person.iris.face.multimodal.folder.need",
                SysConfigConstants.BASE_MULIT_DIR_KEY));
        }
        if (!baseDir.endsWith(File.separator)) {
            baseDir = baseDir + File.separator;
        }
        return baseDir;
    }

    /**
     * 批量删除虹膜人脸多模态
     *
     * @param ids 需要删除的虹膜人脸多模态ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteBasePersonIrisFaceByIds(String[] ids) {
        int result = 0;
        for (String id : ids) {
            result += deleteBasePersonIrisFaceById(id);
        }
        return result;
    }

    /**
     * 删除虹膜人脸多模态信息
     *
     * @param id 虹膜人脸多模态ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteBasePersonIrisFaceById(String id) {
        BasePersonIrisFace basePersonIrisFace = new BasePersonIrisFace();
        basePersonIrisFace.setId(id);
        basePersonIrisFace.setStatus(DictConstants.Status.DISABLE);
        basePersonIrisFace.setUpdateTime(DateUtils.getNowDate());
        try {
            basePersonIrisFace.setUpdateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        int result = basePersonIrisFaceMapper.updateBasePersonIrisFace(basePersonIrisFace);
        BasePersonIrisFace irisFace = basePersonIrisFaceMapper.selectBasePersonIrisFaceById(id);
        // TODO 删除DataManager中人员多模态信息
        // 发布人员信息改变事件
        personChangeEventPublishService.personIrisFaceChangePublish(irisFace.getPersonId(), irisFace.getUniqueId(),
            null);
        return result;
    }

    /**
     * 查询有效人员信息
     *
     * @param uniqueId 唯一标识
     * @return 人员信息
     */
    private BasePersonInfo getEnabledPersonInfo(String uniqueId) {
        BasePersonInfo personCondition = new BasePersonInfo();
        personCondition.setUniqueId(uniqueId);
        personCondition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonInfo> personInfoList = basePersonInfoMapper.selectBasePersonInfoList(personCondition);
        if (CollectionUtils.isEmpty(personInfoList)) {
            return null;
        }
        return personInfoList.get(0);
    }

    /**
     * 查询有效多模态信息
     *
     * @param personId 人员主键
     * @return
     */
    private BasePersonIrisFace getEnabledPersonIrisFaceByPersonId(String personId) {
        BasePersonIrisFace condition = new BasePersonIrisFace();
        condition.setPersonId(personId);
        condition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonIrisFace> irisFaceList = basePersonIrisFaceMapper.selectBasePersonIrisFaceList(condition);
        if (CollectionUtils.isEmpty(irisFaceList)) {
            return null;
        }
        return irisFaceList.get(0);
    }

    /**
     * 新增多模态处理图片特征信息
     *
     * @param basePersonIrisFace
     * @return
     * @throws Exception
     */
    private BasePersonIrisFace handleFeature(BasePersonIrisFace basePersonIrisFace) throws Exception {
        String faceImage = basePersonIrisFace.getFaceImgBase64();
        if (StringUtils.isNotEmpty(faceImage)) {
            // 质量检测
            double qualityScore = personFaceRecogLogicService.qualityDetect(faceImage, null,
                MessageUtils.message("person.face.image.not.clear"));
            basePersonIrisFace.setFaceQuality(qualityScore);
        }
        CompletableFuture<String> faceFeatureFuture = CompletableFuture.supplyAsync(() -> {
            try {
                if (StringUtils.isEmpty(basePersonIrisFace.getFaceImgBase64())) {
                    return basePersonIrisFace.getFaceFeature();
                }
                return multiFeatureService.extractFeatureByImage(basePersonIrisFace.getFaceImgBase64(), AlgType.FACE);
            } catch (Exception var3) {
                log.error("handleFeature error", var3);
                throw new CustomException(var3.getMessage());
            }
        });
        CompletableFuture<String> irisFeatureFuture = CompletableFuture.supplyAsync(() -> {
            try {
                if (StringUtils.isNotEmpty(basePersonIrisFace.getIrisFeature())
                    || StringUtils.isEmpty(basePersonIrisFace.getIrisImgBase64())) {
                    return basePersonIrisFace.getIrisFeature();
                }
                return multiFeatureService.extractFeatureByImage(basePersonIrisFace.getIrisImgBase64(), AlgType.IRIS);
            } catch (Exception var3) {
                log.error("handleFeature error", var3);
                throw new CustomException(var3.getMessage());
            }
        });
        CompletableFuture<Void> allResult = CompletableFuture.allOf(faceFeatureFuture, irisFeatureFuture);
        allResult.join();
        String faceFeature = faceFeatureFuture.get();
        String irisFeature = irisFeatureFuture.get();
        // 特征融合
        String feature = multiFusionFeatureService.fusionFeature(faceFeature, irisFeature);
        // String feature = multiFeatureService.faceIrisMixFeatureByFeature(faceFeature, irisFeature);
        basePersonIrisFace.setFaceFeature(faceFeature);
        basePersonIrisFace.setIrisFeature(irisFeature);
        basePersonIrisFace.setFusionFeature(feature);
        basePersonIrisFace.setFaceFeatureMd5(Md5Utils.hash(basePersonIrisFace.getFaceFeature()));
        basePersonIrisFace.setIrisFeatureMd5(Md5Utils.hash(basePersonIrisFace.getIrisFeature()));
        basePersonIrisFace.setFusionFeatureMd5(Md5Utils.hash(basePersonIrisFace.getFusionFeature()));
        return basePersonIrisFace;
    }

    /**
     * 多模态信息注册
     *
     * @param irisFaceRegister
     * @param channelCode
     * @param primarySubCode
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void irisFaceRegister(IrisFaceRegister irisFaceRegister, String channelCode, String primarySubCode) {
        String uniqueId = irisFaceRegister.getUniqueId();
        String optionType = irisFaceRegister.getOptionType();
        BasePersonIrisFace condition = new BasePersonIrisFace();
        condition.setUniqueId(uniqueId);
        condition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonIrisFace> irisFaceList = basePersonIrisFaceMapper.selectBasePersonIrisFaceList(condition);
        BasePersonIrisFace personIrisFace = CollectionUtils.isNotEmpty(irisFaceList) ? irisFaceList.get(0) : null;
        String personId = null;
        switch (MultiRegistOptionTypeEnum.parse(optionType)) {
            case ADD:
            case UPDATE:
                if (null != personIrisFace) {
                    deleteIrisFace(personIrisFace.getId());
                }
                try {
                    personId = addIrisFace(irisFaceRegister);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new CustomException(e.getMessage());
                }
                // 发布人员信息注册事件
                personChangeEventPublishService.personIrisFaceRegisterPublish(personId, uniqueId, channelCode,
                    primarySubCode, null, DictConstants.DataSource.HTTP_INTERFACE);
                break;
            case DELETE:
                if (null != personIrisFace) {
                    personChangeEventPublishService.devicePersonDeletePublish(personIrisFace.getPersonId(), uniqueId,
                        null);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 逻辑是删除人脸虹膜特征
     *
     * @param id 多模态特征主键
     */
    private void deleteIrisFace(String id) {
        BasePersonIrisFace basePersonIrisFace = new BasePersonIrisFace();
        basePersonIrisFace.setId(id);
        basePersonIrisFace.setUpdateTime(DateUtils.getNowDate());
        basePersonIrisFace.setStatus(DictConstants.Status.DISABLE);
        basePersonIrisFaceMapper.updateBasePersonIrisFace(basePersonIrisFace);
        // TODO datamanger集成后也许要删除特征信息
    }

    /**
     * 多模态数据注册
     *
     * @param irisFaceRegister
     * @throws Exception
     */
    private String addIrisFace(IrisFaceRegister irisFaceRegister) throws Exception {
        String uniqueId = irisFaceRegister.getUniqueId();
        String irisFeature = irisFaceRegister.getIrisFeatureBase64();
        String irisImg = irisFaceRegister.getIrisBase64Img();
        String faceFeature = irisFaceRegister.getFaceFeatureBase64();
        String faceImg = irisFaceRegister.getFaceBase64Img();
        if (StringUtils.isEmpty(faceFeature) && StringUtils.isNotEmpty(faceImg)) {
            faceFeature = multiFeatureService.extractFeatureByImage(faceImg, MicroConstants.AlgType.FACE);
        }
        if (StringUtils.isEmpty(faceFeature)) {
            throw new CustomException(MessageUtils.message("base.person.iris.face.feature.failed.face"));
        }
        if (StringUtils.isEmpty(irisFeature) && StringUtils.isNotEmpty(irisImg)) {
            irisFeature = multiFeatureService.extractFeatureByImage(irisImg, MicroConstants.AlgType.IRIS);
        }
        if (StringUtils.isEmpty(irisFeature)) {
            throw new CustomException(MessageUtils.message("base.person.iris.face.feature.failed.iris"));
        }
        // 特征融合
        String feature = multiFusionFeatureService.fusionFeature(faceFeature, irisFeature);
        // 处理人员基础信息,返回人员主键
        String personId = operatePersonInfo(irisFaceRegister);
        // 处理人员身份证信息
        operateIdCardInfo(irisFaceRegister);
        String baseDir = getMulitImgBaseDir();
        BasePersonIrisFace newMulti = new BasePersonIrisFace();
        if (StringUtils.isNotEmpty(irisImg)) {
            String irisPath = personIrisRecogLogicService.uploadIrisImg(Boolean.TRUE, null, irisImg,
                baseDir + "iris" + File.separator);
            newMulti.setIrisImageUrl(irisPath);
        }
        if (StringUtils.isNotEmpty(faceImg)) {
            String facePath = personFaceRecogLogicService.uploadFaceImg(Boolean.TRUE, null, faceImg,
                baseDir + "face" + File.separator);
            newMulti.setFaceImageUrl(facePath);
        }
        newMulti.setIrisFeature(irisFeature);
        newMulti.setFaceFeature(faceFeature);
        newMulti.setEncrypted(DictConstants.Encrypted.ENABLE);
        newMulti.setIrisFeatureMd5(Md5Utils.hash(irisFeature));
        newMulti.setFaceFeatureMd5(Md5Utils.hash(faceFeature));
        // 处理特征
        newMulti.setDataDescribe(irisFaceRegister.getDataDescribe());
        newMulti.setUniqueId(uniqueId);
        newMulti.setStatus(DictConstants.Status.ENABLE);
        newMulti.setId(IdWorker.getNextStringId());
        newMulti.setFusionFeature(feature);
        newMulti.setFusionFeatureMd5(Md5Utils.hash(faceFeature));
        newMulti.setPersonId(personId);
        newMulti.setDatasource(DictConstants.DataSource.HTTP_INTERFACE);
        newMulti.setCreateTime(DateUtils.getNowDate());
        basePersonIrisFaceMapper.insertBasePersonIrisFace(newMulti);
        return personId;
    }

    /**
     * 多模态注册处理人员基础信息
     *
     * @param irisFaceRegister
     * @return
     */
    private String operatePersonInfo(IrisFaceRegister irisFaceRegister) {
        String uniqueId = irisFaceRegister.getUniqueId();
        BasePersonInfo basePersonInfo = new BasePersonInfo();
        basePersonInfo.setUniqueId(uniqueId);
        List<BasePersonInfo> basePersonInfos = basePersonInfoMapper.selectBasePersonInfoList(basePersonInfo);
        if (CollectionUtils.isEmpty(basePersonInfos)) {
            BasePersonPutInfo personInfo = new BasePersonPutInfo();
            personInfo.setId(IdWorker.getNextStringId());
            personInfo.setUniqueId(uniqueId);
            personInfo.setName(irisFaceRegister.getName());
            personInfo.setCardNo(irisFaceRegister.getCardNo());
            personInfo.setPhone(irisFaceRegister.getPhone());
            personInfo.setStatus(DictConstants.Status.ENABLE);
            personInfo.setCreateTime(new Date());
            personInfo.setDatasource(DictConstants.DataSource.HTTP_INTERFACE);
            personInfo.setRemark(irisFaceRegister.getDataDescribe());
            personInfo.setExtAttrs(irisFaceRegister.getExtAttrs());
            basePersonInfoMapper.insertBasePersonInfo(personInfo);
            return personInfo.getId();
        }
        BasePersonInfo person = basePersonInfos.get(0);
        person.setStatus(DictConstants.Status.ENABLE);
        person.setUpdateTime(new Date());
        person.setCardNo(irisFaceRegister.getCardNo());
        person.setName(irisFaceRegister.getName());
        person.setPhone(irisFaceRegister.getPhone());
        person.setExtAttrs(irisFaceRegister.getExtAttrs());
        basePersonInfoMapper.updateBasePersonInfo(person);
        return person.getId();
    }

    /**
     * 多模态注册处理人员身份证信息
     *
     * @param irisFaceRegister
     * @return
     */
    private void operateIdCardInfo(IrisFaceRegister irisFaceRegister) {
        String idCardNo = irisFaceRegister.getIdCardNo();
        String uniqueId = irisFaceRegister.getUniqueId();
        if (StringUtils.isBlank(uniqueId) || StringUtils.isBlank(idCardNo)) {
            return;
        }
        BasePersonCert certCondition = new BasePersonCert();
        certCondition.setCertType(DictConstants.CertType.ID_CARD);
        certCondition.setUniqueId(uniqueId);
        List<BasePersonCert> certList = basePersonCertMapper.selectBasePersonCertList(certCondition);
        if (CollectionUtils.isEmpty(certList)) {
            BasePersonCert certInfo = new BasePersonCert();
            certInfo.setId(IdWorker.getNextStringId());
            certInfo.setUniqueId(uniqueId);
            certInfo.setCertName(irisFaceRegister.getName());
            certInfo.setCertType(DictConstants.CertType.ID_CARD);
            certInfo.setCertNum(idCardNo);
            basePersonCertMapper.insertBasePersonCert(certInfo);
            return;
        }
        BasePersonCert cert = certList.get(0);
        cert.setCertNum(idCardNo);
        cert.setCertName(irisFaceRegister.getName());
        cert.setUpdateTime(DateUtils.getNowDate());
        basePersonCertMapper.updateBasePersonCert(cert);
    }

    /**
     * 校验人员是否有人脸虹膜多模态数据
     *
     * @param personId
     * @return
     */
    @Override
    public boolean checkPersonHasFaceIris(String personId) {
        int count = basePersonIrisFaceMapper.countEnabledFaceIrisByPersonId(personId);
        return count > 0;
    }
}
