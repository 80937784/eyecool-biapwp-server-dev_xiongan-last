package cn.eyecool.basedata.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import cn.eyecool.common.core.redis.RedisCache;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.validator.EmailValidator;
import org.apache.commons.validator.routines.LongValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.eyecool.abis.callmicroservice.IMultiFeatureService;
import com.eyecool.abis.callmicroservice.MicroConstants.AlgType;
import com.eyecool.abis.callmicroservice.common.CheckLiveResponse;
import com.eyecool.abis.callmicroservice.common.FaceSearchResult;
import com.eyecool.abis.callmicroservice.common.FingerSearchResult;
import com.eyecool.abis.callmicroservice.common.IrisSearchResult;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cn.eyecool.basedata.constant.OperateLock;
import cn.eyecool.basedata.domain.BasePersonFace;
import cn.eyecool.basedata.domain.BasePersonFacePutInfo;
import cn.eyecool.basedata.domain.BasePersonFinger;
import cn.eyecool.basedata.domain.BasePersonFingerPutInfo;
import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.basedata.domain.BasePersonIris;
import cn.eyecool.basedata.domain.BasePersonIrisFace;
import cn.eyecool.basedata.domain.BasePersonIrisFacePutInfo;
import cn.eyecool.basedata.domain.BasePersonIrisPutInfo;
import cn.eyecool.basedata.domain.BasePersonPutInfo;
import cn.eyecool.basedata.event.PersonChangeEventPublishService;
import cn.eyecool.basedata.manager.IPersonDataManagerLogicService;
import cn.eyecool.basedata.manager.IPersonFaceRecogLogicService;
import cn.eyecool.basedata.manager.IPersonFingerRecogLogicService;
import cn.eyecool.basedata.manager.IPersonIrisRecogLogicService;
import cn.eyecool.basedata.mapper.BasePersonFaceMapper;
import cn.eyecool.basedata.mapper.BasePersonFingerMapper;
import cn.eyecool.basedata.mapper.BasePersonInfoMapper;
import cn.eyecool.basedata.mapper.BasePersonIrisFaceMapper;
import cn.eyecool.basedata.mapper.BasePersonIrisMapper;
import cn.eyecool.basedata.service.IBasePersonInfoService;
import cn.eyecool.basedata.service.IBaseVisitorInfoService;
import cn.eyecool.common.annotation.DataScope;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.entity.SysDept;
import cn.eyecool.common.core.domain.entity.SysDictData;
import cn.eyecool.common.core.text.Convert;
import cn.eyecool.common.enums.PersonTypeEnum;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.match.MultiFusionFeatureService;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.common.utils.sign.Md5Utils;
import cn.eyecool.common.utils.sql.SqlUtil;
import cn.eyecool.system.service.ISysConfigService;
import cn.eyecool.system.service.ISysDeptService;
import cn.eyecool.system.service.ISysDictTypeService;
import lombok.extern.slf4j.Slf4j;

/**
 * 人员基础信息Service业务层处理
 *
 * @author mawj
 * @date 2021-01-27
 */
@SuppressWarnings("deprecation")
@Service
@Slf4j
public class BasePersonInfoServiceImpl implements IBasePersonInfoService {

    @Autowired
    private ISysDeptService deptService;
    @Autowired
    private ISysDictTypeService dictTypeService;
    @Autowired
    private BasePersonInfoMapper basePersonInfoMapper;
    @Autowired
    private BasePersonFaceMapper basePersonFaceMapper;
    @Autowired
    private BasePersonFingerMapper basePersonFingerMapper;
    @Autowired
    private BasePersonIrisMapper basePersonIrisMapper;
    @Autowired
    private BasePersonIrisFaceMapper basePersonIrisFaceMapper;
    @Autowired
    private IPersonFaceRecogLogicService personFaceRecogLogicService;
    @Autowired
    private IPersonFingerRecogLogicService personFingerRecogLogicService;
    @Autowired
    private IPersonIrisRecogLogicService personIrisRecogLogicService;
    @Autowired
    private IPersonDataManagerLogicService personDataManagerLogicService;
    @Autowired
    private PersonChangeEventPublishService personChangeEventPublishService;
    @Autowired
    private IMultiFeatureService multiFeatureService;
    @Autowired
    private MultiFusionFeatureService multiFusionFeatureService;
    @Autowired
    private ISysConfigService configService;
    @Autowired
    private IBaseVisitorInfoService iBaseVisitorInfoService;

    @Autowired
    private RedisCache redisCache;

    /** 人员信息更新场景人员自增标识缓存KEY */
    private static final String LIVE_UPDATE_CHANNEL_BUSI_SEQ_CACHE_KEY = "liveupdate:channelbusi_seq";
    /** 人员信息更新子场景人员自增标识缓存KEY */
    private static final String LIVE_UPDATE_SUB_BUSI_SEQ_CACHE_KEY = "liveupdate:subbusi_seq";

    /**
     * 查询人员基础信息
     *
     * @param id 人员基础信息ID
     * @return 人员基础信息
     */
    @Override
    public BasePersonInfo selectBasePersonInfoById(String id) {
        return basePersonInfoMapper.selectBasePersonInfoById(id);
    }

    /**
     * 查询人员基础信息列表
     *
     * @param basePersonInfo 人员基础信息
     * @return 人员基础信息
     */
    @Override
    @DataScope(deptAlias = "info")
    public List<BasePersonInfo> selectBasePersonInfoList(BasePersonInfo basePersonInfo) {
        return basePersonInfoMapper.selectBasePersonInfoList(basePersonInfo);
    }

    /**
     * 新增人员基础信息
     *
     * @param basePersonInfo 人员基础信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertBasePersonInfo(BasePersonPutInfo basePersonInfo) {
        if (null == basePersonInfo.getDeptId()) {
            basePersonInfo.setDeptId(getDeptId(null, true));
        }
        return insertBasePersonInfo(basePersonInfo, DictConstants.DataSource.INTERFACE);
    }

    /**
     * 修改人员基础信息
     *
     * @param basePersonInfo 人员基础信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateBasePersonInfo(BasePersonPutInfo basePersonInfo) {
        String uniqueId = basePersonInfo.getUniqueId();
        if (StringUtils.isBlank(uniqueId)) {
            BasePersonInfo info = basePersonInfoMapper.selectBasePersonInfoById(basePersonInfo.getId());
            uniqueId = info.getUniqueId();
        }
        // 校验账号和卡号唯一性
        checkAccountAndCardNoUnique(uniqueId, basePersonInfo.getAccount(), basePersonInfo.getCardNo());
        basePersonInfo.setUpdateTime(DateUtils.getNowDate());
        try {
            basePersonInfo.setUpdateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        int result = basePersonInfoMapper.updateBasePersonInfo(basePersonInfo);
        List<String> deletedPersonFeatureIds = Lists.newArrayList();
        // 4、保存人脸图片
        BasePersonFace destFace = insertOrUpdatePersonFacePutInfo(basePersonInfo, null, true, deletedPersonFeatureIds);
        // 5、保存人员入库指纹信息,校验 传入的多个指纹是否是同一个手指头
        List<BasePersonFinger> destFingerList =
            insertOrUpdatePersonFingerPutInfo(basePersonInfo, null, true, deletedPersonFeatureIds);
        // 6、保存虹膜图片
        BasePersonIris destIris = insertOrUpdatePersonIrisPutInfo(basePersonInfo, null, true, deletedPersonFeatureIds);
        // 7、保存人脸虹膜多模态信息
        BasePersonIrisFace destIrisFace = insertOrUpdatePersonIrisFacePutInfo(basePersonInfo, null, true);
        // 保存人员信息到datamanager
        personDataManagerLogicService.updatePersonData(uniqueId, basePersonInfo.getName(), destFace, destFingerList,
            destIris, deletedPersonFeatureIds);

        // 发布人员信息改变事件
        boolean faceChanged = StringUtils.isNotNull(destFace);
        boolean fingerChanged = CollectionUtils.isNotEmpty(destFingerList);
        boolean irisChanged = StringUtils.isNotNull(destIris);
        boolean irisFaceChanged = StringUtils.isNotNull(destIrisFace);

        // 讲绑定渠道拆分出场景和子场景
        String channelCodes = Arrays.stream(basePersonInfo.getChannels().split(",")).filter(it -> !it.contains("_"))
            .collect(Collectors.joining(","));
        String subCodes = Arrays.stream(basePersonInfo.getChannels().split(",")).filter(it -> it.contains("_"))
            .collect(Collectors.joining(","));
        // 发布人员信息改变事件
        personChangeEventPublishService.personUpdatePublish(basePersonInfo.getId(), uniqueId, faceChanged,
            fingerChanged, irisChanged, false, irisFaceChanged, null, channelCodes, subCodes,
            basePersonInfo.getDatasource());
        return result;
    }

    /**
     * 批量删除人员基础信息
     *
     * @param ids 需要删除的人员基础信息ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteBasePersonInfoByIds(String[] ids) {
        int result = 0;
        for (String id : ids) {
            result += deleteBasePersonInfoById(id);
        }
        return result;
    }

    /**
     * 删除人员基础信息信息
     *
     * @param id 人员基础信息ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteBasePersonInfoById(String id) {
        // 删除人脸信息
        deleteBasePersonFaceByPersonId(id);
        // 删除指纹信息
        deleteBasePersonFingerByPersonId(id);
        // 删除虹膜信息
        deleteBasePersonIrisByPersonId(id);
        // 删除人脸虹膜多模态
        deleteBasePersonIrisFaceByPersonId(id);
        // TODO 删除指静脉或者其他特征类型
        // 删除场景库人员信息和子场景人员信息（暂时不删除，进行场景人员操作都会先验证人员是否存在，需要删除可以通过发布事件通知）
        // 删除人员信息
        BasePersonInfo basePersonInfo = new BasePersonInfo();
        basePersonInfo.setId(id);
        basePersonInfo.setStatus(DictConstants.Status.DISABLE);
        basePersonInfo.setUpdateTime(DateUtils.getNowDate());
        String loginName = null;
        try {
            loginName = SecurityUtils.getUsername();
        } catch (Exception e) {
        }
        basePersonInfo.setUpdateBy(loginName);
        int result = basePersonInfoMapper.updateBasePersonInfo(basePersonInfo);
        // 删除DataManager生物特征信息
        BasePersonInfo info = basePersonInfoMapper.selectBasePersonInfoById(id);
        personDataManagerLogicService.deletePersonInfo(info.getUniqueId());
        // 发布人员信息改变事件
        personChangeEventPublishService.personDelPublish(id, info.getUniqueId(), null);

        // 人员信息删除，人员对应的邀请访客相关信息也要删除
        BasePersonInfo condition = new BasePersonInfo();
        condition.setPersonType(PersonTypeEnum.VISITOR.value());
        condition.setInviterId(id);
        List<BasePersonInfo> visitorList = basePersonInfoMapper.selectBasePersonInfoList(condition);
        if (!CollectionUtils.isEmpty(visitorList)) {
            String[] visitorIds = visitorList.stream().map(BasePersonInfo::getId).toArray(String[]::new);
            iBaseVisitorInfoService.deleteBasePersonInfoByIds(visitorIds);
        }
        return result;
    }

    /**
     * 保存批量导入人员
     *
     * @param excelFile
     * @return
     * @throws IOException
     * @throws Exception
     */
    @Override
    public String saveImportData(MultipartFile excelFile, Boolean isUpdateSupport) throws IOException, Exception {
        InputStream inputStream = excelFile.getInputStream();
        return saveImportData(inputStream, isUpdateSupport);
    }

    /**
     * 保存批量导入人员
     *
     * @param inputStream
     * @return
     * @throws IOException
     * @throws Exception
     */
    @Override
    public String saveImportData(InputStream inputStream, Boolean isUpdateSupport) throws IOException, Exception {
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        Map<String, Object> importResMap = Collections.emptyMap();
        // 导入数据
        if (inputStream != null) {
            ExcelUtil<BasePersonInfo> util = new ExcelUtil<BasePersonInfo>(BasePersonInfo.class);
            List<BasePersonInfo> infoList = util.importExcel(inputStream);
            // 执行导入数据
            importResMap = importPersonInfoData(infoList, isUpdateSupport);
            Integer importFailNum = (Integer)importResMap.get("failureNum");
            if (null != importFailNum && importFailNum > 0) {
                failureMsg.append(importResMap.get("failureMsg")).append("<br/>");
            }
        }
        if (failureMsg.length() > 0) {
            throw new CustomException(failureMsg.toString());
        }
        String importSuccMsg = (String)importResMap.get("successMsg");
        if (StringUtils.isNotBlank(importSuccMsg)) {
            successMsg = successMsg.append(StringUtils.nvl(importSuccMsg, "")).append("<br/>");
        }
        return successMsg.toString();

    }

    /**
     * 同步人员数据到Datamanager
     *
     * @param basePersonInfo
     * @return
     */
    @Override
    public AjaxResult syncdata(BasePersonInfo basePersonInfo) {
        boolean tryLock = OperateLock.syncBaseDataLock.tryLock();
        if (!tryLock) {
            throw new CustomException(MessageUtils.message("base.person.info.updating"));
        }
        try {
            long total = 0; // 数据总量
            int pageNum = 1;// 分页
            int pageSize = 1000;// 分页数量
            String orderBy = SqlUtil.escapeOrderBySql("create_time asc, id asc");
            // 序列号原子对象
            final AtomicInteger successNum = new AtomicInteger(0);// 更新成功数量原子对象
            final AtomicInteger failNum = new AtomicInteger(0);// 更新失败数量原子对象
            do {
                PageHelper.startPage(pageNum, pageSize, orderBy);
                BasePersonInfo condition = new BasePersonInfo();
                // 不同步访客
                condition.setPersonType(PersonTypeEnum.USER.value());
                List<BasePersonInfo> list = basePersonInfoMapper.selectBasePersonInfoList(condition);
                list.stream().forEach(info -> {
                    try {
                        handleSyncSingleData(info);
                        successNum.getAndIncrement();
                    } catch (Exception e) {
                        log.error("Sync people uniqueId:{} to datamanager error", info.getUniqueId(), e);
                        failNum.getAndIncrement();
                    }
                });
                total = new PageInfo<BasePersonInfo>(list).getTotal();
                pageNum++;
            } while ((pageNum - 1) * pageSize < total);
            Map<String, Integer> map = Maps.newHashMap();
            map.put("failNum", failNum.get());
            map.put("successNum", successNum.get());
            String msg = "";
            if (failNum.get() > 0) {
                msg = MessageUtils.message("base.person.info.sync.sumary", successNum.get(), failNum.get());
                return AjaxResult.error(msg, map);
            } else {
                msg = MessageUtils.message("base.person.info.sync.success.sumary");
                return AjaxResult.success(msg, map);
            }
        } finally {
            OperateLock.syncBaseDataLock.unlock();
        }

    }

    /**
     * 新增人员（WEB和HTTP接口同时调用）
     *
     * @param basePersonInfo
     * @param dataSource
     * @return
     */
    private int insertBasePersonInfo(BasePersonPutInfo basePersonInfo, String dataSource) {
        // 1、查询人员是否存在(有效或者无效)
        BasePersonInfo personCondition = new BasePersonInfo();
        personCondition.setUniqueId(basePersonInfo.getUniqueId());
        List<BasePersonInfo> infoList = basePersonInfoMapper.selectBasePersonInfoList(personCondition);
        boolean isExists = CollectionUtils.isNotEmpty(infoList);
        if (isExists) {
            // 是否有效人员
            boolean anyMatch = infoList.stream().anyMatch(it -> DictConstants.Status.ENABLE.equals(it.getStatus()));
            if (anyMatch) {
                log.debug("Personnel valid information already exists, unique identifier:{}",
                    basePersonInfo.getUniqueId());
                throw new CustomException(
                    MessageUtils.message("base.person.info.valid.info.exists", basePersonInfo.getUniqueId()));
            } else {
                BasePersonInfo existsPersonInfo = infoList.get(0);
                basePersonInfo.setId(existsPersonInfo.getId());
                basePersonInfo.setStatus(DictConstants.Status.ENABLE);
            }
        } else {
            String personId = IdWorker.getNextStringId();
            basePersonInfo.setId(personId);
        }
        // 校验账号和卡号唯一性
        checkAccountAndCardNoUnique(basePersonInfo.getUniqueId(), basePersonInfo.getAccount(),
            basePersonInfo.getCardNo());
        // 2、新增人员信息
        basePersonInfo.setCreateTime(DateUtils.getNowDate());
        basePersonInfo.setDatasource(dataSource);
        String loginName = null;
        try {
            loginName = SecurityUtils.getUsername();
        } catch (Exception e) {
        }
        basePersonInfo.setCreateBy(loginName);
        int result = isExists ? basePersonInfoMapper.updateBasePersonInfo(basePersonInfo)
            : basePersonInfoMapper.insertBasePersonInfo(basePersonInfo);
        // 3、校验指纹编码和眼睛编码是否重复
        checkFingerNo(basePersonInfo);
        // 4、保存人脸图片
        BasePersonFace destFace = insertOrUpdatePersonFacePutInfo(basePersonInfo, dataSource, false, null);
        // 5、保存人员入库指纹信息,校验 传入的多个指纹是否是同一个手指头
        List<BasePersonFinger> destFingerList =
            insertOrUpdatePersonFingerPutInfo(basePersonInfo, dataSource, false, null);
        // 6、保存虹膜图片
        BasePersonIris destIris = insertOrUpdatePersonIrisPutInfo(basePersonInfo, dataSource, false, null);
        // 7、保存人脸虹膜多模态信息
        BasePersonIrisFace destIrisFace = insertOrUpdatePersonIrisFacePutInfo(basePersonInfo, dataSource, false);
        // 8、保存人员信息到datamanager
        personDataManagerLogicService.insertPersonData(basePersonInfo.getUniqueId(), basePersonInfo.getName(), destFace,
            destFingerList, destIris);
        // 发布人员信息改变事件
        boolean faceChanged = StringUtils.isNotNull(destFace);
        boolean fingerChanged = CollectionUtils.isNotEmpty(destFingerList);
        boolean irisChanged = StringUtils.isNotNull(destIris);
        Boolean irisFaceChanged = StringUtils.isNotNull(destIrisFace);

        // 绑定渠道拆分出场景和子场景
        String channelCodes = Arrays.stream(basePersonInfo.getChannels().split(",")).filter(it -> !it.contains("_"))
            .collect(Collectors.joining(","));
        String subCodes = Arrays.stream(basePersonInfo.getChannels().split(",")).filter(it -> it.contains("_"))
            .collect(Collectors.joining(","));
        personChangeEventPublishService.personAddPublish(basePersonInfo.getId(), basePersonInfo.getUniqueId(),
            faceChanged, fingerChanged, irisChanged, false, irisFaceChanged, null, channelCodes, subCodes, dataSource);
        return result;
    }

    /**
     * 校验账号和卡号唯一性
     *
     * @param uniqueId
     * @param account
     * @param cardNo
     */
    private void checkAccountAndCardNoUnique(String uniqueId, String account, String cardNo) {
        BasePersonInfo personCondition = new BasePersonInfo();
        // 查询账号是否重复
        if (StringUtils.isNotBlank(account)) {
            personCondition.setAccount(account);
            personCondition.setPersonType(PersonTypeEnum.USER.value());
            List<BasePersonInfo> list = basePersonInfoMapper.selectBasePersonInfoList(personCondition);
            if (CollectionUtils.isNotEmpty(list) && list.stream().anyMatch(it -> !it.getUniqueId().equals(uniqueId))) {
                log.debug("Duplicate staff account:{}", account);
                throw new CustomException(MessageUtils.message("base.person.info.duplicate.account", account));
            }
        }
        // 查询卡号是否重复
        if (StringUtils.isNotBlank(cardNo)) {
            personCondition.setAccount(null);
            personCondition.setCardNo(cardNo);
            personCondition.setPersonType(PersonTypeEnum.USER.value());
            List<BasePersonInfo> list = basePersonInfoMapper.selectBasePersonInfoList(personCondition);
            if (CollectionUtils.isNotEmpty(list) && list.stream().anyMatch(it -> !it.getUniqueId().equals(uniqueId))) {
                log.debug("Person card number is repeated:{}", cardNo);
                throw new CustomException(MessageUtils.message("base.person.info.duplicate.card.number", cardNo));
            }
        }
    }

    /**
     * 校验指纹编号是否重复
     *
     * @param basePersonInfo
     */
    private void checkFingerNo(BasePersonPutInfo basePersonInfo) {
        // 校验指纹编编号是否重复
        List<BasePersonFingerPutInfo> fingerPutInfoList = basePersonInfo.getFingerPutInfoList();
        if (CollectionUtils.isNotEmpty(fingerPutInfoList)) {
            List<BasePersonFingerPutInfo> exactFingerNoList = fingerPutInfoList.stream()
                .filter(it -> !DictConstants.UncertainFingerNo.uncertainFingerNoList.contains(it.getFingerNo()))
                .collect(Collectors.toList());
            long distinctFingerNoCount =
                exactFingerNoList.stream().map(BasePersonFingerPutInfo::getFingerNo).distinct().count();
            if (distinctFingerNoCount < exactFingerNoList.size()) {
                throw new CustomException(MessageUtils.message("base.person.info.duplicate.finger.serial"));
            }
        }
    }

    /**
     * 保存人员入库人脸信息
     *
     * @param basePersonInfo
     * @param dataSource
     * @param isUpdate true:更新 false:新增
     * @param deletedPersonFeatureIds 需要删除的datamanager中的特征Id
     * @return
     */
    private BasePersonFace insertOrUpdatePersonFacePutInfo(BasePersonPutInfo basePersonInfo, String dataSource,
        boolean isUpdate, List<String> deletedPersonFeatureIds) {
        BasePersonFacePutInfo facePutInfo = basePersonInfo.getFacePutInfo();
        if (null == facePutInfo || StringUtils.isBlank(facePutInfo.getImageBase64())) {
            return null;
        }
        // 活体检测
        if (personFaceRecogLogicService.getFaceAddIsCheckLive()) {
            CheckLiveResponse checkLiveResponse =
                personFaceRecogLogicService.checkLive(facePutInfo.getImageBase64(), null);
            if (!checkLiveResponse.getResult()) {
                log.error("Face warehousing live detection failed，uniqueId:{}!", basePersonInfo.getUniqueId());
                throw new CustomException(MessageUtils.message("base.person.face.checklive.failed"));
            }
        }
        String stockImgFeature = null;
        if (isUpdate) {
            String personId = basePersonInfo.getId();
            BasePersonFace face = getBasePersonFaceByPersonId(personId);
            if (null != face) {
                if (null != deletedPersonFeatureIds) {
                    deletedPersonFeatureIds
                        .addAll(personDataManagerLogicService.getPersonFeatureIds(face.getId(), null, null));
                }
                stockImgFeature = face.getFeature();
                // 先逻辑删除存在的人脸信息
                deleteBasePersonFaceByPersonId(personId);
            }
        }
        BasePersonFace srcFace = new BasePersonFace();
        srcFace.setUniqueId(basePersonInfo.getUniqueId());
        srcFace.setPersonId(basePersonInfo.getId());
        srcFace.setEncrypted(StringUtils.isBlank(facePutInfo.getEncrypted()) ? DictConstants.Encrypted.ENABLE
            : facePutInfo.getEncrypted());
        srcFace.setDatasource(dataSource);
        // 考虑到可能出现abis搜索结果有误（关系库不存在的人被搜索出来），因为校验不通过会抛出异常，此处调用不进行1：N校验，后边单独进行1：N校验并查询关系库排除误搜索
        BasePersonFace destFace = personFaceRecogLogicService.execCheckAndUploadFace(facePutInfo.getImageBase64(), null,
            srcFace, stockImgFeature, false, isUpdate);
        boolean faceAddIsValidateN = personFaceRecogLogicService.getFaceAddIsValidateN();
        if (faceAddIsValidateN) {
            List<FaceSearchResult> searchNResult =
                personFaceRecogLogicService.faceSearchN(destFace.getFeature(), "", 1, null);
            if (CollectionUtils.isNotEmpty(searchNResult)
                && !searchNResult.get(0).getUserId().equals(destFace.getUniqueId())) {
                // 查询人脸是否存在
                String featureId = searchNResult.get(0).getFeatureId();
                String faceId = featureId.substring(featureId.indexOf("_") + 1);
                BasePersonFace face = basePersonFaceMapper.selectBasePersonFaceById(faceId);
                if (null != face && DictConstants.Status.ENABLE.equals(face.getStatus())) {
                    log.error(
                        "The face storage 1-N verification failed, the search results:[uniqueId:{},faceId:{}],The face already exists and is not the person [uniqueId:{}]",
                        searchNResult.get(0).getUserId(), faceId, destFace.getUniqueId());
                    throw new CustomException(MessageUtils.message("base.person.face.1n.check.failed.not.me"));
                } else {
                    log.error(
                        "[FOX_MINISEARCH]Face 1-N search results are wrong, faces that do not exist (or have invalid status) in the relation library [uniqueId:{}, faceId:{}]",
                        searchNResult.get(0).getUserId(), faceId);
                }
            }
        }
        basePersonFaceMapper.insertBasePersonFace(destFace);
        return destFace;
    }

    /**
     * 查询有效人脸信息
     *
     * @param personId
     * @return
     */
    private BasePersonFace getBasePersonFaceByPersonId(String personId) {
        // 查询人脸信息是否存在
        BasePersonFace faceCondition = new BasePersonFace();
        faceCondition.setPersonId(personId);
        faceCondition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonFace> faceList = basePersonFaceMapper.selectBasePersonFaceList(faceCondition);
        return CollectionUtils.isNotEmpty(faceList) ? faceList.get(0) : null;
    }

    /**
     * 保存人员入库指纹信息,校验 传入的多个指纹是否是同一个手指头
     *
     * @param basePersonInfo
     * @param dataSource
     * @param isUpdate true:更新 false:新增
     * @param deletedPersonFeatureIds 需要删除的datamanager中的特征Id
     * @return
     */
    private List<BasePersonFinger> insertOrUpdatePersonFingerPutInfo(BasePersonPutInfo basePersonInfo,
        String dataSource, boolean isUpdate, List<String> deletedPersonFeatureIds) {
        List<BasePersonFingerPutInfo> fingerPutInfoList = basePersonInfo.getFingerPutInfoList();
        if (CollectionUtils.isEmpty(fingerPutInfoList)) {
            return Collections.emptyList();
        }
        // 返回的手指列表
        List<BasePersonFinger> destFingerList = Lists.newArrayList();
        // 进行重复校验特征值列表
        List<String> fingerFeatureList = Lists.newArrayList();
        List<SysDictData> type = dictTypeService.selectDictDataByType(DictConstants.BIO_FINGER_NO_DICT_TYPE);
        List<String> fingerNoList = type.stream().map(SysDictData::getDictValue).collect(Collectors.toList());
        String personId = basePersonInfo.getId();
        List<BasePersonFinger> fingerList = getBasePersonFingerByPersonId(personId);
        if (isUpdate && CollectionUtils.isNotEmpty(fingerList)) {
            if (null != deletedPersonFeatureIds) {
                deletedPersonFeatureIds
                    .addAll(personDataManagerLogicService.getPersonFeatureIds(null, fingerList, null));
            }
            // 先逻辑删除存在的指纹信息, 注意不要删除身份证指纹信息
            deleteBasePersonFingerByPersonId(personId);
        }
        boolean fingerAddIsValidateN = personFingerRecogLogicService.getFingerAddIsValidateN();
        fingerPutInfoList.forEach(fingerPutInfo -> {
            String stockImgFeature = null;
            String fingerNo = fingerPutInfo.getFingerNo();
            String encrypted = StringUtils.isBlank(fingerPutInfo.getEncrypted()) ? DictConstants.Encrypted.ENABLE
                : fingerPutInfo.getEncrypted();
            if (null != fingerPutInfo && StringUtils.isNotBlank(fingerPutInfo.getImageBase64())) {
                if (StringUtils.isBlank(fingerNo) || !fingerNoList.contains(fingerNo)) {
                    throw new CustomException(MessageUtils.message("base.person.info.finger.serial.need"));
                }
                if (isUpdate && CollectionUtils.isNotEmpty(fingerList)
                    && !DictConstants.UncertainFingerNo.uncertainFingerNoList.contains(fingerNo)) {
                    stockImgFeature = fingerList.stream().filter(it -> fingerNo.equals(it.getFingerNo()))
                        .map(it -> it.getFeature()).findFirst().orElse(null);
                }
                BasePersonFinger srcFinger = new BasePersonFinger();
                srcFinger.setUniqueId(basePersonInfo.getUniqueId());
                srcFinger.setPersonId(personId);
                srcFinger.setFingerNo(fingerNo);
                srcFinger.setEncrypted(encrypted);
                srcFinger.setDatasource(dataSource);
                // 考虑到可能出现abis搜索结果有误（关系库不存在的人被搜索出来），因为校验不通过会抛出异常，此处调用不进行1：N校验，后边单独进行1：N校验并查询关系库排除误搜索
                BasePersonFinger destFinger = personFingerRecogLogicService.execCheckAndUploadFinger(
                    fingerPutInfo.getImageBase64(), null, srcFinger, stockImgFeature, false, isUpdate);
                if (fingerAddIsValidateN) {
                    List<FingerSearchResult> searchNResult =
                        personFingerRecogLogicService.fingerSearchN(destFinger.getFeature(), "", 1, null);
                    if (CollectionUtils.isNotEmpty(searchNResult)
                        && !searchNResult.get(0).getUserId().equals(destFinger.getUniqueId())) {
                        // 查询指纹是否存在
                        String featureId = searchNResult.get(0).getFeatureId();
                        String fingerId = featureId.substring(featureId.indexOf("_") + 4);
                        BasePersonFinger finger = basePersonFingerMapper.selectBasePersonFingerById(fingerId);
                        if (null != finger && DictConstants.Status.ENABLE.equals(finger.getStatus())) {
                            log.error(
                                "Fingerprint storage 1-N verification failed, search results:[uniqueId:{},fingerId:{}],Fingerprint already exists and not me [uniqueId:{}]",
                                searchNResult.get(0).getUserId(), fingerId, destFinger.getUniqueId());
                            throw new CustomException(
                                MessageUtils.message("base.person.finger.1n.check.failed.not.me"));
                        } else {
                            log.error(
                                "[FOX_MINISEARCH]Fingerprints 1-N search results are wrong, fingerprints that do not exist (or have an invalid state) in the relation library [uniqueId:{}, fingerId:{}]",
                                searchNResult.get(0).getUserId(), fingerId);
                        }
                    }
                }
                basePersonFingerMapper.insertBasePersonFinger(destFinger);
                fingerFeatureList.add(destFinger.getFeature());
                destFingerList.add(destFinger);
            }
        });
        // 校验指纹是否是同一个手指头
        boolean hasRepeatFinger = personFingerRecogLogicService.checkHasRepeatFinger(fingerFeatureList, null);
        if (hasRepeatFinger) {
            throw new CustomException(MessageUtils.message("base.person.info.finger.duplicate"));
        }
        return destFingerList;
    }

    /**
     * 查询有效指纹信息
     *
     * @param personId
     * @return
     */
    private List<BasePersonFinger> getBasePersonFingerByPersonId(String personId) {
        // 查询指纹信息是否存在
        BasePersonFinger fingerCondition = new BasePersonFinger();
        fingerCondition.setPersonId(personId);
        fingerCondition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonFinger> fingerList = basePersonFingerMapper.selectBasePersonFingerList(fingerCondition);
        return CollectionUtils.isEmpty(fingerList) ? Collections.emptyList() : fingerList;
    }

    /**
     * 保存人员入库虹膜信息
     *
     * @param basePersonInfo
     * @param dataSource
     * @param isUpdate true:更新 false:新增
     * @param deletedPersonFeatureIds 需要删除的datamanager中的特征Id
     * @return
     */
    private BasePersonIris insertOrUpdatePersonIrisPutInfo(BasePersonPutInfo basePersonInfo, String dataSource,
        boolean isUpdate, List<String> deletedPersonFeatureIds) {
        BasePersonIrisPutInfo irisPutInfo = basePersonInfo.getIrisPutInfo();
        if (null == irisPutInfo
            || StringUtils.isBlank(irisPutInfo.getImageBase64()) && StringUtils.isBlank(irisPutInfo.getFeature())) {
            return null;
        }
        String stockImgFeature = null;
        if (isUpdate) {
            String personId = basePersonInfo.getId();
            BasePersonIris existsIris = getBasePersonIrisByPersonId(personId);
            if (null != existsIris) {
                if (null != deletedPersonFeatureIds) {
                    deletedPersonFeatureIds
                        .addAll(personDataManagerLogicService.getPersonFeatureIds(null, null, existsIris.getId()));
                }
                stockImgFeature = existsIris.getFeature();
                // 先逻辑删除存在的虹膜信息
                deleteBasePersonIrisByPersonId(personId);
            }
        }
        String encrypted = StringUtils.isBlank(irisPutInfo.getEncrypted()) ? DictConstants.Encrypted.ENABLE
            : irisPutInfo.getEncrypted();
        BasePersonIris srcIris = new BasePersonIris();
        srcIris.setFeature(irisPutInfo.getFeature());
        srcIris.setUniqueId(basePersonInfo.getUniqueId());
        srcIris.setPersonId(basePersonInfo.getId());
        srcIris.setEncrypted(encrypted);
        srcIris.setDatasource(dataSource);
        // 考虑到可能出现abis搜索结果有误（关系库不存在的人被搜索出来），因为校验不通过会抛出异常，此处调用不进行1：N校验，后边单独进行1：N校验并查询关系库排除误搜索
        BasePersonIris destIris = personIrisRecogLogicService.execCheckAndUploadIris(irisPutInfo.getImageBase64(), null,
            srcIris, stockImgFeature, false, isUpdate);

        boolean irisAddIsValidateN = personIrisRecogLogicService.getIrisAddIsValidateN();
        if (irisAddIsValidateN) {
            List<IrisSearchResult> searchNResult =
                personIrisRecogLogicService.irisSearchN(destIris.getFeature(), StringUtils.EMPTY, 1, null);
            if (CollectionUtils.isNotEmpty(searchNResult)
                && !searchNResult.get(0).getUserId().equals(destIris.getUniqueId())) {
                // 查询虹膜是否存在
                String featureId = searchNResult.get(0).getFeatureId();
                String irisId = featureId.substring(featureId.indexOf("_") + 1);
                BasePersonIris iris = basePersonIrisMapper.selectBasePersonIrisById(irisId);
                if (null != iris && DictConstants.Status.ENABLE.equals(iris.getStatus())) {
                    log.error(
                        "The iris storage 1-N check failed, the search result:[uniqueId:{},irisId:{}],The iris already exists and is not the person [uniqueId:{}]",
                        searchNResult.get(0).getUserId(), irisId, destIris.getUniqueId());
                    throw new CustomException(MessageUtils.message("person.iris.recog.1n.check.failed"));
                } else {
                    log.error(
                        "[FOX_MINISEARCH] Iris 1-N search results are wrong, the iris does not exist (or the status is invalid) in the relation library [uniqueId:{}, irisId:{}]",
                        searchNResult.get(0).getUserId(), irisId);
                }
            }
        }
        basePersonIrisMapper.insertBasePersonIris(destIris);
        return destIris;
    }

    /**
     * 保存人员入库虹膜人脸多模态信息
     *
     * @param basePersonInfo
     * @param dataSource
     * @param isUpdate true:更新 false:新增
     * @return
     */
    private BasePersonIrisFace insertOrUpdatePersonIrisFacePutInfo(BasePersonPutInfo basePersonInfo, String dataSource,
        boolean isUpdate) {
        BasePersonIrisFacePutInfo irisFacePutInfo = basePersonInfo.getIrisFacePutInfo();
        if (null == irisFacePutInfo || StringUtils.isBlank(irisFacePutInfo.getIrisImgBase64())
            && StringUtils.isBlank(irisFacePutInfo.getFaceImgBase64())) {
            return null;
        }
        // 活体检测
        if (personFaceRecogLogicService.getFaceAddIsCheckLive()) {
            CheckLiveResponse checkLiveResponse =
                personFaceRecogLogicService.checkLive(irisFacePutInfo.getFaceImgBase64(), null);
            if (!checkLiveResponse.getResult()) {
                log.error("The multimodal face storage in live detection failed,uniqueId:{}!",
                    basePersonInfo.getUniqueId());
                throw new CustomException(MessageUtils.message("base.person.info.multimodal.face.checklive.failed"));
            }
        }
        BasePersonIrisFace irisFace;
        try {
            irisFace = handleFeature(irisFacePutInfo, basePersonInfo.getUniqueId(), basePersonInfo.getId());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new CustomException("Multimodal information saving exception:" + e.getMessage() + "!");
        }
        String stockFusionFeature = null;
        BasePersonIrisFace personIrisFace = getBasePersonIrisFaceByPersonId(basePersonInfo.getId());
        if (personIrisFace != null) {
            personIrisFace.setDatasource(dataSource);
            stockFusionFeature = personIrisFace.getFusionFeature();
        }
        if (isUpdate) {
            deleteBasePersonIrisFaceByPersonId(basePersonInfo.getId());
        }
        if (StringUtils.isNotBlank(stockFusionFeature)) {
            float score =
                multiFusionFeatureService.matchFusionFeatures(irisFace.getFusionFeature(), stockFusionFeature);
            double threshold = getFusionFeatureMatchOneThreshold();
            if (score < threshold) {
                throw new CustomException(MessageUtils.message("base.person.info.multimodal.match.failed"));
            }
        }
        basePersonIrisFaceMapper.insertBasePersonIrisFace(irisFace);
        return irisFace;
    }

    /**
     * 获取1:1比对阈值参数
     *
     * @return
     */
    private double getFusionFeatureMatchOneThreshold() {
        String thresholdStr =
            configService.selectConfigByKey(SysConfigConstants.BASEDATA_FACE_IRIS_FUSION_COMPARE_THRESHOLD_KEY);
        if (StringUtils.isBlank(thresholdStr)) {
            String msg = MessageUtils.message("base.person.info.multimodal.face.iris.threshold.configure",
                SysConfigConstants.BASEDATA_FACE_IRIS_FUSION_COMPARE_THRESHOLD_KEY);
            throw new CustomException(msg);
        }
        try {
            return Double.valueOf(thresholdStr);
        } catch (Exception e) {
            String msg = MessageUtils.message("base.person.info.multimodal.face.iris.threshold.number",
                SysConfigConstants.BASEDATA_FACE_IRIS_FUSION_COMPARE_THRESHOLD_KEY);
            throw new CustomException(msg);
        }
    }

    /**
     * 多模态处理图片特征信息
     *
     * @param irisFacePutInfo
     * @param uniqueId
     * @param personId
     * @return
     * @throws Exception
     */
    private BasePersonIrisFace handleFeature(BasePersonIrisFacePutInfo irisFacePutInfo, String uniqueId,
        String personId) throws Exception {
        BasePersonIrisFace irisFace = new BasePersonIrisFace();
        String faceImage = irisFacePutInfo.getFaceImgBase64();
        String irisImage = irisFacePutInfo.getIrisImgBase64();
        if (StringUtils.isNotEmpty(faceImage)) {
            // 质量检测
            double qualityScore = personFaceRecogLogicService.qualityDetect(faceImage, null,
                MessageUtils.message("person.face.image.not.clear"));
            irisFace.setFaceQuality(qualityScore);
        }
        CompletableFuture<String> faceFeatureFuture = CompletableFuture.supplyAsync(() -> {
            try {
                if (StringUtils.isEmpty(faceImage)) {
                    return null;
                }
                return multiFeatureService.extractFeatureByImage(faceImage, AlgType.FACE);
            } catch (Exception var3) {
                log.error("handleFeature error", var3);
                throw new CustomException(var3.getMessage());
            }
        });
        CompletableFuture<String> irisFeatureFuture = CompletableFuture.supplyAsync(() -> {
            try {
                if (StringUtils.isNotEmpty(irisFacePutInfo.getIrisFeature())
                    || StringUtils.isEmpty(irisFacePutInfo.getIrisImgBase64())) {
                    return irisFacePutInfo.getIrisFeature();
                }
                return multiFeatureService.extractFeatureByImage(irisImage, AlgType.IRIS);
            } catch (Exception var3) {
                log.error("handleFeature error", var3);
                throw new CustomException(var3.getMessage());
            }
        });
        CompletableFuture<Void> allResult = CompletableFuture.allOf(faceFeatureFuture, irisFeatureFuture);
        allResult.join();
        String faceFeature = faceFeatureFuture.get();
        String irisFeature = irisFeatureFuture.get();
        String feature = multiFusionFeatureService.fusionFeature(faceFeature, irisFeature);
        // String feature = multiFeatureService.faceIrisMixFeatureByFeature(faceFeature, irisFeature);
        irisFace.setFaceFeature(faceFeature);
        irisFace.setIrisFeature(irisFeature);
        irisFace.setFusionFeature(feature);
        irisFace.setFaceFeatureMd5(Md5Utils.hash(irisFace.getFaceFeature()));
        irisFace.setIrisFeatureMd5(Md5Utils.hash(irisFace.getIrisFeature()));
        irisFace.setFusionFeatureMd5(Md5Utils.hash(irisFace.getFusionFeature()));
        irisFace.setId(IdWorker.getNextStringId());
        irisFace.setCreateTime(DateUtils.getNowDate());
        irisFace.setUniqueId(uniqueId);
        irisFace.setPersonId(personId);
        // 文件落盘
        irisFace.setEncrypted(DictConstants.Encrypted.ENABLE);
        String loginName = null;
        try {
            loginName = SecurityUtils.getUsername();
            irisFace.setCreateBy(loginName);
        } catch (Exception e) {
        }
        String baseDir = getMulitImgBaseDir();
        String facePath =
            personFaceRecogLogicService.uploadFaceImg(true, null, faceImage, baseDir + "face" + File.separator);
        String irisPath =
            personIrisRecogLogicService.uploadIrisImg(true, null, irisImage, baseDir + "iris" + File.separator);
        irisFace.setFaceImageUrl(facePath);
        irisFace.setIrisImageUrl(irisPath);
        return irisFace;
    }

    /**
     * 获取多模态图片存储基础路径
     *
     * @return
     */
    private String getMulitImgBaseDir() {
        String baseDir = configService.selectConfigByKey(SysConfigConstants.BASE_MULIT_DIR_KEY);
        if (StringUtils.isBlank(baseDir)) {
            throw new CustomException(
                MessageUtils.message("base.person.info.multimodal.folder.need", SysConfigConstants.BASE_MULIT_DIR_KEY));
        }
        if (!baseDir.endsWith(File.separator)) {
            baseDir = baseDir + File.separator;
        }
        return baseDir;
    }

    /**
     * 查询有效人员虹膜信息
     *
     * @param personId
     * @return
     */
    private BasePersonIris getBasePersonIrisByPersonId(String personId) {
        // 查询虹膜是否存在
        BasePersonIris irisCondition = new BasePersonIris();
        irisCondition.setPersonId(personId);
        irisCondition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonIris> irisList = basePersonIrisMapper.selectBasePersonIrisList(irisCondition);
        return CollectionUtils.isEmpty(irisList) ? null : irisList.get(0);
    }

    /**
     * 查询有效人员虹膜人臉多模态信息
     *
     * @param personId
     * @return
     */
    private BasePersonIrisFace getBasePersonIrisFaceByPersonId(String personId) {
        // 查询虹膜是否存在
        BasePersonIrisFace irisFaceCondition = new BasePersonIrisFace();
        irisFaceCondition.setPersonId(personId);
        irisFaceCondition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonIrisFace> irisFaceList =
            basePersonIrisFaceMapper.selectBasePersonIrisFaceList(irisFaceCondition);
        return CollectionUtils.isEmpty(irisFaceList) ? null : irisFaceList.get(0);
    }

    /**
     * 根据人员ID删除人脸信息(逻辑删除)
     *
     * @param personId
     * @return
     */
    private int deleteBasePersonFaceByPersonId(String personId) {
        BasePersonFace basePersonFace = new BasePersonFace();
        basePersonFace.setPersonId(personId);
        basePersonFace.setStatus(DictConstants.Status.DISABLE);
        basePersonFace.setUpdateTime(DateUtils.getNowDate());
        String loginName = null;
        try {
            loginName = SecurityUtils.getUsername();
        } catch (Exception e) {
        }
        basePersonFace.setUpdateBy(loginName);
        return basePersonFaceMapper.updateBasePersonFaceByPersonId(basePersonFace);
    }

    /**
     * 根据人员ID删除指纹信息(逻辑删除)
     *
     * @param personId
     * @return
     */
    private int deleteBasePersonFingerByPersonId(String personId) {
        BasePersonFinger basePersonFinger = new BasePersonFinger();
        basePersonFinger.setPersonId(personId);
        basePersonFinger.setStatus(DictConstants.Status.DISABLE);
        basePersonFinger.setUpdateTime(DateUtils.getNowDate());
        String loginName = null;
        try {
            loginName = SecurityUtils.getUsername();
        } catch (Exception e) {
        }
        basePersonFinger.setUpdateBy(loginName);
        return basePersonFingerMapper.updateBasePersonFingerByPersonId(basePersonFinger);
    }

    /**
     * 根据人员ID删除虹膜信息(逻辑删除)
     *
     * @param personId 虹膜ID
     * @return 结果
     */
    public int deleteBasePersonIrisByPersonId(String personId) {
        BasePersonIris basePersonIris = new BasePersonIris();
        basePersonIris.setPersonId(personId);
        basePersonIris.setStatus(DictConstants.Status.DISABLE);
        basePersonIris.setUpdateTime(DateUtils.getNowDate());
        String loginName = null;
        try {
            loginName = SecurityUtils.getUsername();
        } catch (Exception e) {
        }
        basePersonIris.setUpdateBy(loginName);
        return basePersonIrisMapper.updateBasePersonIrisByPersonId(basePersonIris);
    }

    /**
     * 根据人员ID删除多模态信息(逻辑删除)
     *
     * @param personId
     * @return
     */
    private int deleteBasePersonIrisFaceByPersonId(String personId) {
        BasePersonIrisFace basePersonIrisFace = new BasePersonIrisFace();
        basePersonIrisFace.setPersonId(personId);
        basePersonIrisFace.setStatus(DictConstants.Status.DISABLE);
        basePersonIrisFace.setUpdateTime(DateUtils.getNowDate());
        String loginName = null;
        try {
            loginName = SecurityUtils.getUsername();
        } catch (Exception e) {
        }
        basePersonIrisFace.setUpdateBy(loginName);
        return basePersonIrisFaceMapper.updateBasePersonIrisFaceByPersonId(basePersonIrisFace);
    }

    /**
     * 查询部门信息
     *
     * @author mawenjun
     * @param deptCode
     * @param isSetDefault
     * @return
     * @date 2020年2月13日
     *
     */
    private Long getDeptId(String deptCode, boolean isSetDefault) {
        SysDept deptCondition = new SysDept();
        if (StringUtils.isNotBlank(deptCode)) {
            deptCondition.setDeptCode(deptCode);
        } else if (isSetDefault) {
            // 如果没有传输部门编码，则设置为默认部门
            deptCondition.setDeptCode(SysConfigConstants.DEFAULT_SYS_DEPT_CODE);
        } else {
            return null;
        }
        // 查询部门信息
        List<SysDept> deptList = deptService.selectDeptList(deptCondition);
        if (CollectionUtils.isEmpty(deptList)) {
            String msg = MessageUtils.message("base.person.info.dept.not.exists", deptCondition.getDeptCode());
            throw new CustomException(msg);
        }
        return deptList.get(0).getDeptId();
    }

    /**
     * 导入人员信息数据
     *
     * @param basePersonInfoList 人员信息数据列表
     * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
     * @return 结果
     */
    private Map<String, Object> importPersonInfoData(List<BasePersonInfo> basePersonInfoList, Boolean isUpdateSupport) {
        isUpdateSupport = null == isUpdateSupport ? true : isUpdateSupport;
        if (CollectionUtils.isEmpty(basePersonInfoList)) {
            log.error("Import personnel data is empty!");
            throw new CustomException(MessageUtils.message("base.person.info.import.data.empty"));
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        // 查询默认部门ID
        Long defaultDeptId = getDeptId(null, true);
        for (BasePersonInfo basePersonInfo : basePersonInfoList) {
            try {
                // 进行字段合法性校验
                validateImportField(basePersonInfo);
                // 验证是否已存在人员信息(有效或者无效)
                boolean existsFlag = false;
                BasePersonInfo infoCondition = new BasePersonInfo();
                infoCondition.setUniqueId(basePersonInfo.getUniqueId());
                List<BasePersonInfo> infoList = basePersonInfoMapper.selectBasePersonInfoList(infoCondition);
                existsFlag = CollectionUtils.isNotEmpty(infoList);
                String loginName = null;
                try {
                    loginName = SecurityUtils.getUsername();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                Date now = new Date();
                basePersonInfo.setDatasource(DictConstants.DataSource.IMP);
                if (!existsFlag) {// 不存在, 直接入库保存
                    basePersonInfo.setId(IdWorker.getNextStringId());
                    basePersonInfo.setCreateBy(loginName);
                    basePersonInfo.setCreateTime(now);
                    basePersonInfo.setUpdateTime(now);
                    basePersonInfo
                        .setDeptId(null == basePersonInfo.getDeptId() ? defaultDeptId : basePersonInfo.getDeptId());
                    basePersonInfoMapper.insertBasePersonInfo(basePersonInfo);
                    // 发布人员信息改变事件
                    personChangeEventPublishService.personAddPublish(basePersonInfo.getId(),
                        basePersonInfo.getUniqueId(), false, false, false, false, false, null);
                    successNum++;
                    continue;
                }
                // 有效人员是否存在
                boolean anyMatch = infoList.stream().anyMatch(it -> DictConstants.Status.ENABLE.equals(it.getStatus()));
                if (anyMatch && isUpdateSupport || !anyMatch) {// 存在有效且支持更新,或者只是存在无效人员信息
                    BasePersonInfo existsPersonInfo = infoList.get(0);
                    basePersonInfo.setCreateTime(anyMatch ? null : now);
                    basePersonInfo.setCreateBy(anyMatch ? null : loginName);
                    basePersonInfo.setUpdateBy(loginName);
                    basePersonInfo.setUpdateTime(now);
                    basePersonInfo.setStatus(DictConstants.Status.ENABLE);
                    basePersonInfo.setDeptId(null != basePersonInfo.getDeptId() ? basePersonInfo.getDeptId()
                        : (existsPersonInfo.getDeptId() == null ? defaultDeptId : existsPersonInfo.getDeptId()));
                    basePersonInfo.setId(existsPersonInfo.getId());
                    basePersonInfoMapper.updateBasePersonInfo(basePersonInfo);
                    if (!anyMatch) {
                        personChangeEventPublishService.personAddPublish(basePersonInfo.getId(),
                            basePersonInfo.getUniqueId(), false, false, false, false, false, null);
                    } else {
                        personChangeEventPublishService.personChangePublish(basePersonInfo.getId(),
                            basePersonInfo.getUniqueId(), null);
                    }
                    successNum++;
                } else {// 存在有效但是不支持更新
                    failureNum++;
                    String msg = MessageUtils.message("base.person.info.import.failed.user.exists",
                        basePersonInfo.getUniqueId());
                    failureMsg.append("<br/>" + failureNum + msg);
                }
            } catch (Exception e) {
                failureNum++;
                String s = MessageUtils.message("base.person.info.import.failed", basePersonInfo.getUniqueId());
                String msg = "<br/>" + failureNum + s + e.getMessage();
                failureMsg.append(msg);
                log.error(msg, e);
            }
        }
        if (failureNum > 0) {
            failureMsg.insert(0, MessageUtils.message("base.person.info.import.failed.sumary", failureNum));
            log.info(failureMsg.toString());
        } else {
            successMsg.insert(0, MessageUtils.message("base.person.info.import.success.sumary", successNum));
            log.info(successMsg.toString());
        }
        Map<String, Object> resMap = new HashMap<>();
        resMap.put("failureNum", failureNum);
        resMap.put("failureMsg", failureMsg.toString());
        resMap.put("successNum", successNum);
        resMap.put("successMsg", successMsg.toString());
        return resMap;
    }

    /**
     * 校验字段合法性
     *
     * @param basePersonInfo
     * @return
     */
    private void validateImportField(BasePersonInfo basePersonInfo) {
        String uniqueId = basePersonInfo.getUniqueId();// 唯一标识
        String name = basePersonInfo.getName();
        String account = basePersonInfo.getAccount();// 账号
        String email = basePersonInfo.getEmail();// 邮箱
        String cardNo = basePersonInfo.getCardNo();// 卡号
        String phone = basePersonInfo.getPhone();// 手机号
        String deptCode = basePersonInfo.getDeptCode(); // 部门编码
        basePersonInfo.setDeptId(getDeptId(deptCode, true));
        if (StringUtils.isBlank(uniqueId) || uniqueId.length() > 48
            || !Pattern.compile("[\u4e00-\u9fa5\\w]+").matcher(uniqueId).matches()) {
            throw new CustomException(MessageUtils.message("base.person.info.invalid.uniqueid"));
        }
        if (StringUtils.isBlank(name) || name.length() > 60) {
            throw new CustomException(MessageUtils.message("base.person.info.invalid.uniqueid"));
        }
        if (StringUtils.isNotBlank(phone) && (phone.length() != 11 || !LongValidator.getInstance().isValid(phone))) {
            throw new CustomException(MessageUtils.message("base.person.info.invalid.name"));
        }
        if (StringUtils.isNotBlank(email) && !EmailValidator.getInstance().isValid(email)) {
            throw new CustomException(MessageUtils.message("base.person.info.invalid.email"));
        }
        if (StringUtils.isNotBlank(account) && account.length() > 48) {
            throw new CustomException(MessageUtils.message("base.person.info.invalid.account"));
        }
        if (StringUtils.isNotBlank(cardNo) && cardNo.length() > 48) {
            throw new CustomException(MessageUtils.message("base.person.info.invalid.cardno"));
        }
        // 校验账号和卡号唯一性
        this.checkAccountAndCardNoUnique(uniqueId, basePersonInfo.getAccount(), basePersonInfo.getCardNo());
    }

    /**
     * 同步单条人员数据到Datamanager
     *
     * @param info
     */
    private void handleSyncSingleData(BasePersonInfo info) {
        // 先删除，再写入
        personDataManagerLogicService.deletePersonInfo(info.getUniqueId());
        if (DictConstants.Status.DISABLE.equals(info.getStatus())) {
            return;
        }
        // 查询人脸数据
        BasePersonFace faceCondition = new BasePersonFace();
        faceCondition.setStatus(DictConstants.Status.ENABLE);
        faceCondition.setPersonId(info.getId());
        List<BasePersonFace> faceList = basePersonFaceMapper.selectBasePersonFaceList(faceCondition);
        BasePersonFace destFace = CollectionUtils.isEmpty(faceList) ? null : faceList.get(0);
        // 查询指纹数据
        BasePersonFinger fingerCondition = new BasePersonFinger();
        fingerCondition.setStatus(DictConstants.Status.ENABLE);
        fingerCondition.setPersonId(info.getId());
        List<BasePersonFinger> fingerList = basePersonFingerMapper.selectBasePersonFingerList(fingerCondition);
        // 查询虹膜数据
        BasePersonIris irisCondition = new BasePersonIris();
        irisCondition.setStatus(DictConstants.Status.ENABLE);
        irisCondition.setPersonId(info.getId());
        List<BasePersonIris> irisList = basePersonIrisMapper.selectBasePersonIrisList(irisCondition);
        BasePersonIris destIris = CollectionUtils.isEmpty(irisList) ? null : irisList.get(0);
        // TODO 查询多模态数据
        // 写入数据
        if (CollectionUtils.isEmpty(fingerList) && null == destIris && null == destFace) {
            return;
        }
        personDataManagerLogicService.insertPersonData(info.getUniqueId(), info.getName(), destFace, fingerList,
            destIris);
    }

    /**
     * 新增Http接口数据源人员信息
     */
    @Override
    @Transactional
    public int insertHttpBasePersonInfo(BasePersonPutInfo basePersonInfo) {
        basePersonInfo.setDeptId(getDeptId(basePersonInfo.getDeptCode(), true));
        return insertBasePersonInfo(basePersonInfo, DictConstants.DataSource.HTTP_INTERFACE);
    }

    /**
     * 修改Http接口数据源人员信息
     */
    @Override
    @Transactional
    public int updateHttpBasePersonInfo(BasePersonPutInfo basePersonInfo) {
        // 校验人员信息是否存在
        String uniqueId = basePersonInfo.getUniqueId();
        BasePersonInfo infoCondition = new BasePersonInfo();
        infoCondition.setUniqueId(uniqueId);
        infoCondition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonInfo> infoList = basePersonInfoMapper.selectBasePersonInfoList(infoCondition);
        if (CollectionUtils.isEmpty(infoList)) {
            throw new CustomException(MessageUtils.message("base.person.info.valid.info.exists", uniqueId));
        }
        // 校验账号和卡号是否唯一
        checkAccountAndCardNoUnique(uniqueId, basePersonInfo.getAccount(), basePersonInfo.getCardNo());
        // 设置部门信息
        basePersonInfo.setDeptId(getDeptId(basePersonInfo.getDeptCode(), false));
        String dataSource = DictConstants.DataSource.HTTP_INTERFACE;
        // 1、保存人员基本信息
        String personId = infoList.get(0).getId();
        basePersonInfo.setId(personId);
        basePersonInfo.setDatasource(dataSource);
        basePersonInfo.setUpdateTime(DateUtils.getNowDate());
        int result = basePersonInfoMapper.updateBasePersonInfo(basePersonInfo);
        // 2、校验指纹编编号是否重复
        checkFingerNo(basePersonInfo);
        // 定义需要删除的人员特征Ids（datamanager）, 防止存在垃圾数据，导致搜索结果不正确
        List<String> deletedPersonFeatureIds = Lists.newArrayList();
        // 3、保存人脸图片
        BasePersonFace destFace =
            insertOrUpdatePersonFacePutInfo(basePersonInfo, dataSource, true, deletedPersonFeatureIds);
        // 4、保存指纹图片
        List<BasePersonFinger> destFingerList =
            insertOrUpdatePersonFingerPutInfo(basePersonInfo, dataSource, true, deletedPersonFeatureIds);
        // 5、保存虹膜图片
        BasePersonIris destIris =
            insertOrUpdatePersonIrisPutInfo(basePersonInfo, dataSource, true, deletedPersonFeatureIds);
        // 6、保存人员信息到datamanager
        personDataManagerLogicService.updatePersonData(basePersonInfo.getUniqueId(), basePersonInfo.getName(), destFace,
            destFingerList, destIris, deletedPersonFeatureIds);
        // 发布人员信息改变事件
        boolean faceChanged = StringUtils.isNotNull(destFace);
        boolean fingerChanged = CollectionUtils.isNotEmpty(destFingerList);
        boolean irisChanged = StringUtils.isNotNull(destIris);
        personChangeEventPublishService.personUpdatePublish(basePersonInfo.getId(), basePersonInfo.getUniqueId(),
            faceChanged, fingerChanged, irisChanged, false, false, null);
        return result;
    }

    /**
     * 查询人员数量
     *
     * @param basePersonInfo
     * @return
     */
    @Override
    public int selectBasePersonCount(BasePersonInfo basePersonInfo) {
        return basePersonInfoMapper.selectBasePersonCount(basePersonInfo);
    }

    /**
     * 根据人员唯一标识或者姓名查询人员列表
     *
     * @param basePersonInfo
     * @return
     */
    @Override
    public List<BasePersonInfo> selectByUidOrName(BasePersonInfo basePersonInfo) {
        return basePersonInfoMapper.selectByUidOrName(basePersonInfo);
    }

    /**
     * 根据人员标识修改标志（黑白名单）
     *
     * @param uniqueIds
     * @param flag
     */
    @Override
    public int updatePersonFlagByUniqueIds(String uniqueIds, String flag) {
        String[] arr = Convert.toStrArray(uniqueIds);
        return basePersonInfoMapper.updatePersonFlagByUniqueIds(arr, flag);
    }

    @Override
    public int insertOnlyPersonInfo(BasePersonInfo personInfo) {
        if (null == personInfo.getDeptId()) {
            personInfo.setDeptId(getDeptId(null, true));
        }
        return basePersonInfoMapper.insertBasePersonInfo(personInfo);
    }

    @Override
    public int updateOnlyPersonInfo(BasePersonInfo personInfo) {
        String uniqueId = personInfo.getUniqueId();
        if (StringUtils.isBlank(uniqueId)) {
            BasePersonInfo info = basePersonInfoMapper.selectBasePersonInfoById(personInfo.getId());
            uniqueId = info.getUniqueId();
        }
        // 校验账号和卡号唯一性
        checkAccountAndCardNoUnique(uniqueId, personInfo.getAccount(), personInfo.getCardNo());
        personInfo.setUpdateTime(DateUtils.getNowDate());
        try {
            personInfo.setUpdateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        return basePersonInfoMapper.updateBasePersonInfo(personInfo);
    }

    /**
     *
     * @param ids 人员id，用英文逗号分割
     * @param type 0 启用 ，1 停用
     * @return
     */
    @Override
    @Transactional
    public int isStopAndEnable(String ids, String type) {
        try {
            if (StringUtils.isNotEmpty(ids)) {
                String status = "1".equals(type) ? DictConstants.Status.DISABLE : DictConstants.Status.ENABLE;
                String[] split = ids.split(",");
                for (String id : split) {
                    BasePersonInfo basePersonInfo = new BasePersonInfo();
                    basePersonInfo.setId(id);
                    basePersonInfo.setStatus(status);
                    basePersonInfo.setUpdateTime(DateUtils.getNowDate());
                    String loginName = null;
                    try {
                        loginName = SecurityUtils.getUsername();
                    } catch (Exception e) {
                    }
                    basePersonInfo.setUpdateBy(loginName);
                    int result = basePersonInfoMapper.updateBasePersonInfo(basePersonInfo);
                    //场景、子场景 序列号
                    Long channelBusiSeqNum = incrementAndGetChannelBusiSeqNum(LIVE_UPDATE_CHANNEL_BUSI_SEQ_CACHE_KEY,"0");
                    Long subBusiSeqNum = incrementAndGetChannelBusiSeqNum(LIVE_UPDATE_SUB_BUSI_SEQ_CACHE_KEY,"1");
                    // 更新序列号、使设备能更新人员信息
                    basePersonInfoMapper.updateChannelBusiSeq(id,channelBusiSeqNum);
                    basePersonInfoMapper.updateChannelSubBusiSeq(id,subBusiSeqNum);
                }
            }
            return 1;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     *  获取序列号并自增
     */
    private Long incrementAndGetChannelBusiSeqNum(String key, String type) {
        Long seq = redisCache.incrementAndGet(key);
        if (null != seq) {
            return seq;
        }
        synchronized (this) {
            seq = redisCache.incrementAndGet(key);
            if (null == seq) {
                Long maxSeriaNum = null;
                if ("0".equals(type)) {
                    maxSeriaNum = basePersonInfoMapper.selectMaxSeriaNumForChannelBusiness();
                } else {
                    maxSeriaNum = basePersonInfoMapper.selectMaxSeriaNumForchannelSubBusi();
                }
                if (null == maxSeriaNum) {
                    maxSeriaNum = 1L;
                }
                seq = redisCache.addAndGetLong(key, maxSeriaNum);
            }
        }
        return seq;
    }
}
