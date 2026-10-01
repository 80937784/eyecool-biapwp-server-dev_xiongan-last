package cn.eyecool.basedata.manager.impl;

import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.eyecool.abis.callmicroservice.IBioFaceDataManagerService;
import com.eyecool.abis.callmicroservice.common.datamanager.DataStoreResult;
import com.eyecool.abis.callmicroservice.common.datamanager.PersonData;
import com.eyecool.abis.callmicroservice.common.datamanager.PersonFeature;
import com.google.common.collect.Lists;

import cn.eyecool.basedata.domain.BasePersonFace;
import cn.eyecool.basedata.domain.BasePersonFinger;
import cn.eyecool.basedata.domain.BasePersonIris;
import cn.eyecool.basedata.manager.IPersonDataManagerLogicService;
import cn.eyecool.common.constant.DatamanagerConstants;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.enums.DatamanageFeatureType;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.system.service.ISysConfigService;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;

/**
 * 人员信息DataManager逻辑服务实现
 * 
 * @author admin
 * @date 2019年11月20日
 */
@Service
@Slf4j
public class PersonDataManagerLogicServiceImpl implements IPersonDataManagerLogicService {

    @Autowired
    private ISysConfigService configService;
    @Autowired
    private IBioFaceDataManagerService bioFaceDataManagerService;

    /**
     * 新增人员信息
     * 
     * @param uniqueId
     * @param personName
     * @param destFace
     * @param destFingerList
     * @param destIris
     */
    @Override
    public void insertPersonData(String uniqueId, String personName, BasePersonFace destFace,
        List<BasePersonFinger> destFingerList, BasePersonIris destIris) {
        // 平台没有开启1-N功能支持，则datamanager功能也不需要启用
        if (!getPlatformIsOpenSearchN()) {
            return;
        }
        List<PersonFeature> personFeatures = getPersonFeatures(destFace, destFingerList, destIris);
        if (CollectionUtils.isEmpty(personFeatures)) {
            return;
        }
        // 获取租户ID
        String tenantId = TenantContextHolder.getTenantId();
        PersonData personData = new PersonData();
        // 开启多租户支持后，人员ID添加租户ID前缀，使用#连接符
        personData.setId(StringUtils.isBlank(tenantId) ? uniqueId : tenantId + "#" + uniqueId);
        personData.setName(StringUtils.isBlank(personName) ? uniqueId : personName);
        personData.setFeatures(personFeatures);
        DataStoreResult dataStoreResult = saveOrReplacePersonData(personData);
        if (!dataStoreResult.isResult()) {
            log.error("dataManager,save the person failed, tenantId: [{}],uniqueId:[{}], personName:[{}], dataStoreResult:[{}]",
                tenantId, personData.getId(), personData.getName(), dataStoreResult.getMessage());
            throw new CustomException(MessageUtils.message("person.data.manager.save.failed.msg", dataStoreResult.getMessage()));
        }
        // 开启多租户之后，人员入库需要自动绑定人库关系（租户ID作为库号）
        if (StringUtils.isNotBlank(tenantId)) {
            bindLibPersonRelation(true, tenantId, uniqueId);
        }
    }

    /**
     * 修改人员信息
     * 
     * @param uniqueId
     * @param personName
     * @param destFace
     * @param destFingerList
     * @param destIris
     * @param deletedFeatureIds
     */
    @Override
    public void updatePersonData(String uniqueId, String personName, BasePersonFace destFace,
        List<BasePersonFinger> destFingerList, BasePersonIris destIris, List<String> deletedFeatureIds) {
        // 平台没有开启1-N功能支持，则datamanager功能也不需要启用
        if (!getPlatformIsOpenSearchN()) {
            return;
        }
        // 查询人员信息
        PersonData personData = getPersonData(uniqueId);
        // 人员信息不存在，直接新增
        if (personData == null) {
            insertPersonData(uniqueId, personName, destFace, destFingerList, destIris);
            return;
        }
        // 人员信息存在，新增或修改特征信息
        List<PersonFeature> personFeatures = getPersonFeatures(destFace, destFingerList, destIris);
        List<PersonFeature> features = personData.getFeatures();
        if (CollectionUtils.isEmpty(features)) {
            personData.setFeatures(personFeatures);
        } else if (CollectionUtils.isNotEmpty(personFeatures)) {
            Iterator<PersonFeature> iterator = features.iterator();
            while (iterator.hasNext()) {
                PersonFeature next = iterator.next();
                // 查询当前的特征信息是否需要更新, 需要更新，则把原来的删除
                boolean anyMatch = personFeatures.stream()
                    .anyMatch(it -> next.getFeatureType() == it.getFeatureType()
                        && it.getFeatureType() != DatamanageFeatureType.FINGER_UNKNOWN.getCode()
                        || (CollectionUtils.isNotEmpty(deletedFeatureIds) && deletedFeatureIds.contains(it.getId())));
                if (anyMatch) {
                    iterator.remove();
                }
            }
            features.addAll(personFeatures);
            personData.setFeatures(features);
        }
        personData.setName(StringUtils.isBlank(personName) ? personData.getName() : personName);
        DataStoreResult dataStoreResult = saveOrReplacePersonData(personData);
        if (!dataStoreResult.isResult()) {
            log.error("dataManager，update the person failed, uniqueId:[{}], personName:[{}], dataStoreResult:[{}]", personData.getId(),
                personData.getName(), dataStoreResult.getMessage());
            throw new CustomException(MessageUtils.message("person.data.manager.update.failed.msg", dataStoreResult.getMessage()));
        }
    }

    /**
     * 保存或替换人员信息
     * 
     * @param personData
     */
    private DataStoreResult saveOrReplacePersonData(PersonData personData) {
        try {
            return bioFaceDataManagerService.replacePersonData(personData, false);
        } catch (TimeoutException e) {
            log.error("invoke the datamanager service timeout", e);
            throw new CustomException(MessageUtils.message("person.data.invoke.service.timeout"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            log.error("invoke the datamanager,save or update person failed", e);
            throw new CustomException(MessageUtils.message("person.data.invoke.service.saveupdate.error", msg));
        }
    }

    /**
     * 删除人员信息
     * 
     * @param uniqueId
     */
    @Override
    public void deletePersonInfo(String uniqueId) {
        // 平台没有开启1-N功能支持，则datamanager功能也不需要启用
        if (!getPlatformIsOpenSearchN()) {
            return;
        }
        // 查询人员信息
        PersonData personData = getPersonData(uniqueId);
        // 人员信息不存在，直接返回
        if (personData == null) {
            return;
        }
        DataStoreResult dataStoreResult = null;
        try {
            String tenantId = TenantContextHolder.getTenantId();
            String realUniqueId = StringUtils.isBlank(tenantId) ? uniqueId : tenantId + "#" + uniqueId;
            dataStoreResult = bioFaceDataManagerService.deletePersonLibaryData(realUniqueId, null, null, "PERSON");
        } catch (TimeoutException e) {
            log.error("invoke the datamanager service timeout", e);
            throw new CustomException(MessageUtils.message("person.data.invoke.service.timeout"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            log.error("datamanager,delete the person [{}] error", uniqueId, e);
            throw new CustomException(MessageUtils.message("person.data.datamanager.delete.error", uniqueId,msg));
        }
        if (dataStoreResult.isResult()) {
            if (log.isDebugEnabled()) {
                log.debug("datamanager,deleter the person successfully, uniqueId:[{}]", uniqueId);
            }
            return;
        }
        log.error("datamanager,delete the person failed, uniqueId:[{}],  dataStoreResult:[{}]", uniqueId, dataStoreResult.getMessage());
        throw new CustomException(MessageUtils.message("person.data.datamanager.delete.error", uniqueId,dataStoreResult.getMessage()));
    }

    /**
     * 查询人员信息
     * 
     * @param uniqueId
     * @return
     */
    @Override
    public PersonData getPersonData(String uniqueId) {
        try {
            // 开启多租户之后，datamanager中人员ID以租户ID作为前缀
            String tenantId = TenantContextHolder.getTenantId();
            PersonData personData = bioFaceDataManagerService
                .getPersonData(StringUtils.isBlank(tenantId) ? uniqueId : tenantId + "#" + uniqueId);
            if (log.isDebugEnabled()) {
                log.debug("Datamanager,query the person,uniqueId:{}, result:{}", uniqueId,
                    null == personData ? null : JSON.toJSONString(personData));
            }
            return null == personData || StringUtils.isBlank(personData.getId()) ? null : personData;
        } catch (TimeoutException e) {
            log.error("invoke the datamanager service timeout", e);
            throw new CustomException(MessageUtils.message("person.data.invoke.service.timeout"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            log.error("dataManager,query the perosn [{}] failed", uniqueId, e);
            throw new CustomException(MessageUtils.message("person.data.datamanager.query.error", uniqueId,msg));
        }
    }

    /**
     * 获取datamanager人员生物特征列表
     * 
     * @param destFace
     * @param destFingerList
     * @param destIris
     * @return
     */
    private List<PersonFeature> getPersonFeatures(BasePersonFace destFace, List<BasePersonFinger> destFingerList,
        BasePersonIris destIris) {
        List<PersonFeature> features = Lists.newArrayList();
        // 添加人脸特征
        if (null != destFace && StringUtils.isNotBlank(destFace.getFeature())) {
            PersonFeature feature = new PersonFeature();
            feature.setId(DatamanagerConstants.PERSON_FEATURE_FACE_ID_PREFIX + destFace.getId());
            feature.setFeatureType(DatamanageFeatureType.FACE.getCode());
            feature.setData(Base64.getMimeDecoder().decode(destFace.getFeature()));
            features.add(feature);
        }
        // 添加指纹特征
        if (CollectionUtils.isNotEmpty(destFingerList)) {
            destFingerList.stream().filter(it -> it != null && StringUtils.isNotBlank(it.getFeature())).forEach(it -> {
                PersonFeature feature = new PersonFeature();
                // 指纹ID的存储以手指号为前缀finger_16_155947700862272
                feature
                    .setId(DatamanagerConstants.PERSON_FEATURE_FINGER_ID_PREFIX + it.getFingerNo() + "_" + it.getId());
                feature.setFeatureType(DatamanageFeatureType.parse(it.getFingerNo()).getCode());
                feature.setData(Base64.getMimeDecoder().decode(it.getFeature()));
                features.add(feature);
            });
        }
        // 添加虹膜特征
        if (null != destIris && StringUtils.isNotBlank(destIris.getFeature())) {
            PersonFeature feature = new PersonFeature();
            feature.setId(DatamanagerConstants.PERSON_FEATURE_IRIS_ID_PREFIX + destIris.getId());
            feature.setFeatureType(DatamanageFeatureType.IRIS_UNKNOWN.getCode());
            feature.setData(Base64.getMimeDecoder().decode(destIris.getFeature()));
            features.add(feature);
        }
        return features;
    }

    /**
     * 删除人脸信息
     * 
     * @param uniqueId
     * @param faceId
     */
    @Override
    public void deletePersonFace(String uniqueId, String faceId) {
        // 平台没有开启1-N功能支持，则datamanager功能也不需要启用
        if (!getPlatformIsOpenSearchN()) {
            return;
        }
        // 查询人员信息
        PersonData personData = getPersonData(uniqueId);
        // 人员信息不存在，直接返回
        if (personData == null) {
            return;
        }
        String featureId = DatamanagerConstants.PERSON_FEATURE_FACE_ID_PREFIX + faceId;
        DataStoreResult dataStoreResult = null;
        String tenantId = TenantContextHolder.getTenantId();
        try {
            String realUniqueId = StringUtils.isBlank(tenantId) ? uniqueId : tenantId + "#" + uniqueId;
            dataStoreResult =
                bioFaceDataManagerService.deletePersonLibaryData(realUniqueId, featureId, null, "FEATURE");
        } catch (TimeoutException e) {
            log.error("invoke the datamanager service timeout", e);
            throw new CustomException(MessageUtils.message("person.data.invoke.service.timeout"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            log.error("datamanager,delete the face feature error,uniqueId:[{}], faceId:[{}].", uniqueId, faceId, e);
            throw new CustomException(MessageUtils.message("person.data.datamanager.delete.face.feature.error",uniqueId,msg));
        }
        if (dataStoreResult.isResult()) {
            if (log.isDebugEnabled()) {
                log.debug("datamanager,delete the face successfully, uniqueId:[{}],faceId:[{}]", uniqueId, faceId);
            }

        } else {
            log.info("datamanager,delete the face failed, tenantId:[{}], uniqueId:[{}], faceId:[{}], dataStoreResult:[{}]", tenantId,
                uniqueId, faceId, dataStoreResult.getMessage());
            throw new CustomException(MessageUtils.message("person.data.datamanager.delete.face.feature.error",uniqueId, dataStoreResult.getMessage()));
        }
    }

    /**
     * 删除人员指纹信息
     * 
     * @param uniqueId
     * @param fingerNo
     * @param fingerId
     */
    @Override
    public void deletePersonFinger(String uniqueId, String fingerNo, String fingerId) {
        // 平台没有开启1-N功能支持，则datamanager功能也不需要启用
        if (!getPlatformIsOpenSearchN()) {
            return;
        }
        // 查询人员信息
        PersonData personData = getPersonData(uniqueId);
        // 人员信息不存在，直接返回
        if (personData == null) {
            return;
        }
        String featureId = DatamanagerConstants.PERSON_FEATURE_FINGER_ID_PREFIX + fingerNo + "_" + fingerId;
        DataStoreResult dataStoreResult = null;
        String tenantId = TenantContextHolder.getTenantId();
        try {
            String realUniqueId = StringUtils.isBlank(tenantId) ? uniqueId : tenantId + "#" + uniqueId;
            dataStoreResult =
                bioFaceDataManagerService.deletePersonLibaryData(realUniqueId, featureId, null, "FEATURE");
        } catch (TimeoutException e) {
            log.error("invoke the datamanager service timeout", e);
            throw new CustomException(MessageUtils.message("person.data.invoke.service.timeout"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            log.error("datamanager,delete the finger feature error,uniqueId:[{}],fingerNo:[{}], fingerId:[{}].", uniqueId, fingerNo, fingerId,
                e);
            throw new CustomException(MessageUtils.message("person.data.datamanager.deleter.finger.feature.error", uniqueId,msg));
        }
        if (dataStoreResult.isResult()) {
            if (log.isDebugEnabled()) {
                log.debug("datamanager,delete the finger successfully, uniqueId:[{}],fingerNo:[{}],fingerId:[{}]", uniqueId, fingerNo, fingerId);
            }
            return;
        }
        log.error("datamanager,delete the finger failed, uniqueId:[{}], fingerNo:[{}],fingerId:[{}], dataStoreResult:[{}]", uniqueId,
            fingerNo, fingerId, dataStoreResult.getMessage());
        throw new CustomException(MessageUtils.message("person.data.datamanager.deleter.finger.feature.error", uniqueId, dataStoreResult.getMessage()));
    }

    /**
     * 删除人员所有指纹信息
     */
    @Override
    public void clearPersonFinger(String uniqueId) {
        // 平台没有开启1-N功能支持，则datamanager功能也不需要启用
        if (!getPlatformIsOpenSearchN()) {
            return;
        }
        this.clearPersonAnyBioFeature(uniqueId, DatamanagerConstants.PERSON_FEATURE_FINGER_ID_PREFIX, "指纹");
    }

    /**
     * 删除人员虹膜信息
     * 
     * @param uniqueId
     * @param irisId
     */
    @Override
    public void deletePersonIris(String uniqueId, String irisId) {
        // 平台没有开启1-N功能支持，则datamanager功能也不需要启用
        if (!getPlatformIsOpenSearchN()) {
            return;
        }
        // 查询人员信息
        PersonData personData = getPersonData(uniqueId);
        // 人员信息不存在，直接返回
        if (personData == null) {
            return;
        }
        String featureId = DatamanagerConstants.PERSON_FEATURE_IRIS_ID_PREFIX + irisId;
        DataStoreResult dataStoreResult = null;
        try {
            String tenantId = TenantContextHolder.getTenantId();
            String realUniqueId = StringUtils.isBlank(tenantId) ? uniqueId : tenantId + "#" + uniqueId;
            dataStoreResult =
                bioFaceDataManagerService.deletePersonLibaryData(realUniqueId, featureId, null, "FEATURE");
        } catch (TimeoutException e) {
            log.error("invoke the datamanager service timeout", e);
            throw new CustomException(MessageUtils.message("person.data.invoke.service.timeout"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            log.error("datamanager,delete the iris error,uniqueId:[{}],irisId:[{}].", uniqueId, irisId, e);
            throw new CustomException(MessageUtils.message("person.data.datamanager.deleter.iris.feature.error", uniqueId, msg));
        }
        if (dataStoreResult.isResult()) {
            if (log.isDebugEnabled()) {
                log.debug("datamanager,delete the iris successfully, uniqueId:[{}],irisId:[{}]", uniqueId, irisId);
            }
            return;
        }
        log.error("datamanager,delete the iris failed, uniqueId:[{}],irisId:[{}], dataStoreResult:[{}]", uniqueId, irisId,
            dataStoreResult.getMessage());
        throw new CustomException(MessageUtils.message("person.data.datamanager.deleter.iris.feature.error", uniqueId,  dataStoreResult.getMessage()));

    }

    /**
     * 删除人员所有虹膜信息
     * 
     * @param uniqueId
     */
    @Override
    public void clearPersonIris(String uniqueId) {
        // 平台没有开启1-N功能支持，则datamanager功能也不需要启用
        if (!getPlatformIsOpenSearchN()) {
            return;
        }
        this.clearPersonAnyBioFeature(uniqueId, DatamanagerConstants.PERSON_FEATURE_IRIS_ID_PREFIX, "虹膜");
    }

    /**
     * 添加库人员(绑定人库关系)
     * 
     * @param libraryId
     * @param uniqueId
     */
    @Override
    public void addLibraryPerson(String libraryId, String uniqueId) {
        // 平台没有开启1-N功能支持，则datamanager功能也不需要启用
        if (!getPlatformIsOpenSearchN()) {
            return;
        }
        if (queryLibraryPersonExists(libraryId, uniqueId)) {
            return;
        }
        this.bindLibPersonRelation(false, libraryId, uniqueId);
    }

    /**
     * 绑定人库关系
     * 
     * @param isTenantLib 是否是租户大库
     * @param libraryId 库ID
     * @param uniqueId 人员唯一标识
     */
    private void bindLibPersonRelation(boolean isTenantLib, String libraryId, String uniqueId) {
        String tenantId = TenantContextHolder.getTenantId();
        if (isTenantLib && StringUtils.isBlank(tenantId)) {
            throw new CustomException(MessageUtils.message("person.data.datamanager.save.error.tenant.id.null"));
        }
        DataStoreResult dataStoreResult = null;
        try {
            String realLibId =
                isTenantLib ? tenantId : StringUtils.isBlank(tenantId) ? libraryId : tenantId + "#" + libraryId;;
            String realUniqueId = StringUtils.isBlank(tenantId) ? uniqueId : tenantId + "#" + uniqueId;
            dataStoreResult = bioFaceDataManagerService.addLibraryPerson(realLibId, realUniqueId);
        } catch (TimeoutException e) {
            log.error("invoke the datamanager service timeout", e);
            throw new CustomException(MessageUtils.message("person.data.invoke.service.timeout"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            log.error("datamanager,save the person error,uniqueId:[{}], libraryId：[{}].", uniqueId, libraryId, e);
            String s = MessageUtils.message("person.data.datamanager.save.failed.msg", uniqueId,libraryId,msg);
            throw new CustomException(s);
        }
        if (dataStoreResult.isResult()) {
            if (log.isDebugEnabled()) {
                log.debug("save the person successfully, uniqueId:[{}], libraryId：[{}], dataStoreResult:[{}]", uniqueId, libraryId,
                    dataStoreResult.getMessage());
            }
            return;
        }
        log.error("save the person failed, uniqueId:[{}], libraryId：[{}], dataStoreResult:[{}]", uniqueId, libraryId,
            dataStoreResult.getMessage());
        String msg = MessageUtils.message("person.data.datamanager.save.failed.msg", uniqueId,libraryId,dataStoreResult.getMessage());
        throw new CustomException(msg);
    }

    /**
     * 删除库人员(解绑人库关系)
     * 
     * @param libraryId
     * @param uniqueId
     */
    @Override
    public void deleteLibraryPerson(String libraryId, String uniqueId) {
        // 平台没有开启1-N功能支持，则datamanager功能也不需要启用
        if (!getPlatformIsOpenSearchN()) {
            return;
        }
        if (!queryLibraryPersonExists(libraryId, uniqueId)) {
            return;
        }
        DataStoreResult dataStoreResult = null;
        try {
            String tenantId = TenantContextHolder.getTenantId();
            String realLibId = StringUtils.isBlank(tenantId) ? libraryId : tenantId + "#" + libraryId;
            String realUniqueId = StringUtils.isBlank(tenantId) ? uniqueId : tenantId + "#" + uniqueId;
            dataStoreResult =
                bioFaceDataManagerService.deletePersonLibaryData(realUniqueId, null, realLibId, "LIBRARYPERSON");
        } catch (TimeoutException e) {
            log.error("invoke the datamanager service timeout", e);
            throw new CustomException(MessageUtils.message("person.data.invoke.service.timeout"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            log.error("datamanager,delete the perosn failed, libraryId:[{}], uniqueId:[{}].", libraryId, uniqueId, e);
            throw new CustomException(MessageUtils.message("person.data.datamanager.delete.error", uniqueId,msg));
        }
        if (dataStoreResult.isResult()) {
            if (log.isDebugEnabled()) {
                log.debug("datamanager,delete the person successfully, uniqueId:[{}], libraryId：[{]]", uniqueId, libraryId);
            }
            return;
        }
        log.error("datamanager,delete the person failed, uniqueId:[{}], libraryId：[{}], dataStoreResult:[{}]", uniqueId, libraryId,
            dataStoreResult.getMessage());
        throw new CustomException(MessageUtils.message("person.data.datamanager.delete.error", uniqueId,dataStoreResult.getMessage()));
    }

    /**
     * 查询库中人员是否存在
     * 
     * @param libraryId
     * @param uniqueId
     * @return
     */
    @Override
    public boolean queryLibraryPersonExists(String libraryId, String uniqueId) {
        try {
            // 开启租户之后，datamanager中人库关系的库号以租户ID开头（租户库除外，租户库以租户ID为库号）
            String tenantId = TenantContextHolder.getTenantId();
            String realLibId = StringUtils.isBlank(tenantId) ? libraryId : tenantId + "#" + libraryId;
            String realUniqueId = StringUtils.isBlank(tenantId) ? uniqueId : tenantId + "#" + uniqueId;
            List<String> result = bioFaceDataManagerService.getLibrary(realLibId, realUniqueId, "LIBRARY");
            if (CollectionUtils.isEmpty(result)) {
                return false;
            }
            return result.stream().anyMatch(it -> it.equals(realUniqueId));
        } catch (TimeoutException e) {
            log.error("invoke the datamanager service timeout.", e);
            throw new CustomException(MessageUtils.message("person.data.invoke.service.timeout"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            log.error("datamanager,query the perosn failed, libraryId:[{}], uniqueId:[{}].", libraryId, uniqueId, e);
            throw new CustomException(MessageUtils.message("person.data.datamanager.query.error", uniqueId, msg));
        }
    }

    /**
     * 删除库
     * 
     * @param libraryId
     */
    @Override
    public void deleteLibrary(String libraryId) {
        // 平台没有开启1-N功能支持，则datamanager功能也不需要启用
        if (!getPlatformIsOpenSearchN()) {
            return;
        }
        DataStoreResult dataStoreResult = null;
        try {
            // 开启租户之后，datamanager中人库关系的库号以租户ID开头（租户库除外，租户库以租户ID为库号）
            String tenantId = TenantContextHolder.getTenantId();
            String realLibId = StringUtils.isBlank(tenantId) ? libraryId : tenantId + "#" + libraryId;
            dataStoreResult = bioFaceDataManagerService.deletePersonLibaryData(null, null, realLibId, "LIBRARY");
        } catch (TimeoutException e) {
            log.error("invoke the datamanager service timeout", e);
            throw new CustomException(MessageUtils.message("person.data.invoke.service.timeout"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            log.error("datamanager,delete the library failed, libraryId:[{}].", libraryId, e);
            throw new CustomException(MessageUtils.message("person.data.datamanager.delete.libray.error", libraryId , msg));
        }
        if (dataStoreResult.isResult()) {
            if (log.isDebugEnabled()) {
                log.debug("datamanager,delete the library successfully, libraryId：[{}]", libraryId);
            }
            return;
        }
        log.error("datamanager,delete the library failed, libraryId：[{}], dataStoreResult:[{}]", libraryId, dataStoreResult.getMessage());
        throw new CustomException(MessageUtils.message("person.data.datamanager.delete.libray.error", libraryId , dataStoreResult.getMessage()));
    }

    /**
     * 生成人员特征ID列表
     * 
     * @param faceId
     * @param fingerList
     * @param irisId
     * @return
     */
    @Override
    public List<String> getPersonFeatureIds(String faceId, List<BasePersonFinger> fingerList, String irisId) {
        List<String> personFeatureIds = Lists.newArrayList();
        if (StringUtils.isNotBlank(faceId)) {
            personFeatureIds.add(DatamanagerConstants.PERSON_FEATURE_FACE_ID_PREFIX + faceId);
        }
        if (CollectionUtils.isNotEmpty(fingerList)) {
            List<String> fingerFeatureIds = fingerList.stream().filter(it -> it != null)
                .map(it -> DatamanagerConstants.PERSON_FEATURE_FINGER_ID_PREFIX + it.getFingerNo() + "_" + it.getId())
                .collect(Collectors.toList());
            personFeatureIds.addAll(fingerFeatureIds);
        }
        if (StringUtils.isNotBlank(irisId)) {
            personFeatureIds.add(DatamanagerConstants.PERSON_FEATURE_IRIS_ID_PREFIX + irisId);
        }
        return personFeatureIds;
    }

    /**
     * 获取平台是否开启了1-N功能
     * 
     * @return
     */
    private boolean getPlatformIsOpenSearchN() {
        String validConfig = configService.selectConfigByKey(SysConfigConstants.PLATFORM_SEARCH_N_FUNCTION_OPEN_KEY);
        // 不进行参数配置则默认开启了1-N功能
        return null == validConfig ? true : DictConstants.YesOrNoState.YES.equalsIgnoreCase(validConfig);
    }

    /**
     * 清空人员某一生物特征
     * 
     * @param uniqueId 人员唯一标识
     * @param idPerfix 生物特征前缀
     * @param bioDesc 生物类型描述
     */
    private void clearPersonAnyBioFeature(String uniqueId, String idPerfix, String bioDesc) {
        // 查询人员信息
        PersonData personData = getPersonData(uniqueId);
        if (null != personData && CollectionUtils.isNotEmpty(personData.getFeatures())) {
            List<PersonFeature> features = personData.getFeatures();
            List<PersonFeature> newFeatures =
                features.stream().filter(it -> !it.getId().startsWith(idPerfix)).collect(Collectors.toList());
            personData.setFeatures(newFeatures);
            DataStoreResult dataStoreResult = saveOrReplacePersonData(personData);
            if (dataStoreResult.isResult()) {
                if (log.isDebugEnabled()) {
                    log.debug("dataManager,delete the person [{}] successfully,uniqueId: [{}], dataStoreResult：[{}]", bioDesc, uniqueId,
                        dataStoreResult.toString());
                }
                return;
            }
            log.error("dataManager delete [{}] failed,uniqueId:[{}],dataStoreResult：[{}]", bioDesc, uniqueId,
                dataStoreResult.toString());
            String msg = MessageUtils.message("person.data.datamanager.delete.error=datamanager", uniqueId,bioDesc);
            throw new CustomException(msg);
        }
    }

}
