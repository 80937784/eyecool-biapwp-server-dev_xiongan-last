package cn.eyecool.scene.trade.service.impl;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.beust.jcommander.internal.Maps;
import com.eyecool.abis.callmicroservice.common.FeatureBean;
import com.eyecool.abis.callmicroservice.common.IrisSearchResult;
import com.eyecool.abis.callmicroservice.common.MatchBean;
import com.google.common.collect.Lists;

import cn.eyecool.abis.detect.commons.FeatureData;
import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.basedata.domain.BasePersonIris;
import cn.eyecool.basedata.manager.IPersonIrisRecogLogicService;
import cn.eyecool.basedata.mapper.BasePersonInfoMapper;
import cn.eyecool.basedata.mapper.BasePersonIrisMapper;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.DictConstants.BioResult;
import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.domain.entity.SysDept;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.bean.BeanUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.scene.constant.ChannelParamConstants;
import cn.eyecool.scene.domain.ChannelBusiness;
import cn.eyecool.scene.domain.ChannelInfo;
import cn.eyecool.scene.domain.ChannelParam;
import cn.eyecool.scene.domain.ChannelSubtreasuryBusi;
import cn.eyecool.scene.domain.ChannelSubtreasuryInfo;
import cn.eyecool.scene.mapper.ChannelBusinessMapper;
import cn.eyecool.scene.mapper.ChannelInfoMapper;
import cn.eyecool.scene.mapper.ChannelParamMapper;
import cn.eyecool.scene.mapper.ChannelSubtreasuryBusiMapper;
import cn.eyecool.scene.mapper.ChannelSubtreasuryInfoMapper;
import cn.eyecool.scene.trade.entity.PersonIrisRecog;
import cn.eyecool.scene.trade.entity.PersonIrisVerify;
import cn.eyecool.scene.trade.service.IChannelBusiIrisHttpService;
import cn.eyecool.scene.trade.vo.PersonBioRecogBaseVO;
import cn.eyecool.scene.trade.vo.PersonBioRecogSceneVO;
import cn.eyecool.scene.trade.vo.PersonIrisRecogVO;
import cn.eyecool.scene.trade.vo.PersonIrisVerifyVO;
import cn.eyecool.system.mapper.SysDeptMapper;
import cn.eyecool.system.service.ISysConfigService;
import cn.eyecool.tradelog.domain.PersonIrisMatchLog;
import cn.eyecool.tradelog.domain.PersonIrisSearchLog;
import cn.eyecool.tradelog.mapper.PersonIrisMatchLogMapper;
import cn.eyecool.tradelog.mapper.PersonIrisSearchLogMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * 场景虹膜业务HTTP服务层实现
 * 
 * @author admin
 * @date 2019年11月28日
 */
@Service
@Slf4j
public class ChannelBusiIrisHttpServiceImpl implements IChannelBusiIrisHttpService {

    @Autowired
    private ChannelInfoMapper channelInfoMapper;
    @Autowired
    private IPersonIrisRecogLogicService irisRecogLogicService;
    @Autowired
    private BasePersonInfoMapper basePersonInfoMapper;
    @Autowired
    private BasePersonIrisMapper basePersonIrisMapper;
    @Autowired
    private ChannelParamMapper channelParamMapper;
    @Autowired
    private ChannelBusinessMapper channelBusinessMapper;
    @Autowired
    private ISysConfigService configService;
    @Autowired
    private ChannelSubtreasuryBusiMapper channelSubtreasuryBusiMapper;
    @Autowired
    private ChannelSubtreasuryInfoMapper channelSubtreasuryInfoMapper;
    @Autowired
    private PersonIrisMatchLogMapper irisMatchLogMapper;
    @Autowired
    private PersonIrisSearchLogMapper irisSearchLogMapper;
    @Autowired
    private SysDeptMapper sysDeptMapper;

    /**
     * 虹膜1:1认证
     */
    @Override
    public PersonIrisVerifyVO verifyPersonIris(PersonIrisVerify irisVerify) {
        long receivedTime = System.currentTimeMillis();
        // 校验场景是否存在
        String channelCode = irisVerify.getChannelCode();
        ChannelInfo channelInfo = null;
        if (StringUtils.isNotBlank(channelCode)) {
            channelInfo = checkChannelExists(channelCode);
        }

        // 判断场景是否开通虹膜
        if (null != channelInfo) {
            String irisMode = channelInfo.getIrisMode();
            if (DictConstants.BioModeStatus.DISABLE.equals(irisMode)) {
                throw new CustomException(
                    MessageUtils.message("channel.busi.iris.service.scene.not.open", channelCode));
            }
        }

        // 校验人员信息是否存在
        String uniqueId = irisVerify.getUniqueId();
        BasePersonInfo basePersonInfo = checkBasePersonExists(uniqueId);
        Long deptId = basePersonInfo.getDeptId();
        String personName = basePersonInfo.getName();
        String deptName = null;
        if (null != deptId) {
            SysDept sysDept = sysDeptMapper.selectDeptById(deptId);
            deptName = null == sysDept ? null : sysDept.getDeptName();
        }

        // 判断当前人员是否开通虹膜
        if (null != channelInfo) {
            ChannelBusiness channelBusiness = getChannelBusiness(channelInfo.getId(), basePersonInfo.getId());
            if (null == channelBusiness) {
                throw new CustomException(
                    MessageUtils.message("channel.face.service.scene.person.not.exists", channelCode, uniqueId));
            }
            if (DictConstants.BioModeStatus.DISABLE.equals(channelBusiness.getIrisMode())) {
                throw new CustomException(MessageUtils.message("channel.busi.iris.service.scene.person.not.open.iris",
                    channelCode, uniqueId));
            }
            if (DictConstants.YesOrNoState.YES.equalsIgnoreCase(channelBusiness.getLocked())) {
                throw new CustomException(
                    MessageUtils.message("channel.common.service.scene.person.locked", channelCode, uniqueId));
            }
        }

        // 查询底库虹膜照片
        List<BasePersonIris> irisList = getBasePersonIris(basePersonInfo.getId());
        if (CollectionUtils.isEmpty(irisList)) {
            throw new CustomException(MessageUtils.message("channel.busi.iris.service.base.image.not.exists"));
        }
        BasePersonIris basePersonIris = irisList.get(0);
        // 提取现场照虹膜特征
        String sceneImage = irisVerify.getSceneImage();
        String sceneFeature = irisVerify.getSceneFeature();
        FeatureBean sceneImageFeatureBean = new FeatureBean();
        sceneImageFeatureBean.setFeature(sceneFeature);
        sceneImageFeatureBean.setType(String.valueOf(FeatureData.FeatureType.IrisFeatureUnknown));
        if (StringUtils.isBlank(sceneFeature)) {
            sceneImageFeatureBean = irisRecogLogicService.getFeatureBean(sceneImage,
                MessageUtils.message("channel.busi.iris.service.liveimage.no.iris"),
                MessageUtils.message("channel.busi.iris.service.liveimage.multi.iris"));
        }

        // 定义比对List(放入现场照、底库照)
        List<FeatureBean> compareFeatureBeanList = Lists.newArrayList();
        compareFeatureBeanList.add(sceneImageFeatureBean);
        // 底库虹膜特征放入比对列表
        FeatureBean stockImageFeatureBean = new FeatureBean();
        stockImageFeatureBean.setFeature(basePersonIris.getFeature());
        stockImageFeatureBean.setType(String.valueOf(FeatureData.FeatureType.IrisFeatureUnknown));
        compareFeatureBeanList.add(stockImageFeatureBean);
        // 获取虹膜对比对阈值
        String compareThresholdStr = irisVerify.getCompareThreshold();
        double irisCompareThreshold = StringUtils.isBlank(compareThresholdStr)
            ? getIrisMatchThreshold(null == channelInfo ? null : channelInfo.getId())
            : Double.valueOf(compareThresholdStr);
        PersonIrisVerifyVO irisVerifyVO = new PersonIrisVerifyVO();
        DecimalFormat df = new DecimalFormat("0.00");
        // 进行1:1比对
        List<MatchBean> matchBeanList = irisRecogLogicService.irisOne2OneCompare(compareFeatureBeanList);
        if (CollectionUtils.isEmpty(matchBeanList)) {
            throw new CustomException(MessageUtils.message("channel.busi.iris.service.iris.image.match.error"));
        }
        double score = matchBeanList.get(0).getResults().get(0).getScore();
        Boolean result = score > irisCompareThreshold;
        irisVerifyVO.setSceneStockScore(df.format(score));
        irisVerifyVO.setResult(result ? DictConstants.BioResult.PASS : DictConstants.BioResult.NOTPASS);

        // 存储比对日志
        asyncSaveIrisMatchLog(irisVerify, irisVerifyVO, basePersonIris, receivedTime, deptId, deptName, personName,
            matchBeanList);
        return irisVerifyVO;
    }

    /**
     * 校验人员信息是否存在
     * 
     * @param uniqueId
     * @return
     */
    private BasePersonInfo checkBasePersonExists(String uniqueId) {
        BasePersonInfo personCondition = new BasePersonInfo();
        personCondition.setUniqueId(uniqueId);
        personCondition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonInfo> personList = basePersonInfoMapper.selectBasePersonInfoList(personCondition);
        if (CollectionUtils.isEmpty(personList)) {
            throw new CustomException(MessageUtils.message("channel.common.service.person.not.exists", uniqueId));
        }
        return personList.get(0);
    }

    /**
     * 查询虹膜信息
     * 
     * @param personId
     * @param eyeCode
     * @return
     */
    private List<BasePersonIris> getBasePersonIris(String personId) {
        BasePersonIris irisCondition = new BasePersonIris();
        irisCondition.setPersonId(personId);
        irisCondition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonIris> irisList = basePersonIrisMapper.selectBasePersonIrisList(irisCondition);
        if (CollectionUtils.isEmpty(irisList)) {
            return Collections.emptyList();
        }
        return irisList;
    }

    /**
     * 查询业务信息
     * 
     * @param channelId
     * @param personId
     */
    private ChannelBusiness getChannelBusiness(String channelId, String personId) {
        ChannelBusiness busiCondition = new ChannelBusiness();
        busiCondition.setChannelId(channelId);
        busiCondition.setPersonId(personId);
        busiCondition.setStatus(DictConstants.Status.ENABLE);
        List<ChannelBusiness> businessList = channelBusinessMapper.selectChannelBusinessList(busiCondition);
        return CollectionUtils.isEmpty(businessList) ? null : businessList.get(0);
    }

    /**
     * 获取虹膜1：1比对阈值
     * 
     * @param channelId
     * @return
     */
    private double getIrisMatchThreshold(String channelId) {
        if (StringUtils.isNotBlank(channelId)) {
            ChannelParam channelParam = getChannelParam(channelId, DictConstants.BioAttestType.IRIS,
                ChannelParamConstants.IRIS_COMPARE_THRESHOLD_CODE);
            if (null != channelParam) {
                return Double.valueOf(channelParam.getParamValue());
            }
        }
        // 场景参数不存在，则查询公共参数配置
        return irisRecogLogicService.getOne2OneCompareThreshold();
    }

    /**
     * 获取场景参数
     * 
     * @param channelId
     * @param bioAssestType
     * @param paramCode
     * @return
     */
    private ChannelParam getChannelParam(String channelId, String bioAssestType, String paramCode) {
        ChannelParam paramCondition = new ChannelParam();
        paramCondition.setChannelId(channelId);
        paramCondition.setBioAttestType(bioAssestType);
        paramCondition.setParamCode(paramCode);
        List<ChannelParam> paramList = channelParamMapper.selectChannelParamList(paramCondition);
        return CollectionUtils.isEmpty(paramList) ? null : paramList.get(0);
    }

    /**
     * 异步保存虹膜1:1比对日志
     * 
     * @param irisVerify
     * @param verifyVO
     * @param stockPersonIris
     * @param receivedTime
     * @param deptId
     * @param matchBeanList
     */
    private void asyncSaveIrisMatchLog(PersonIrisVerify irisVerify, PersonIrisVerifyVO verifyVO,
        BasePersonIris stockPersonIris, long receivedTime, Long deptId, String deptName, String personName,
        List<MatchBean> matchBeanList) {
        long responseTime = System.currentTimeMillis();
        String tenantId = TenantContextHolder.getTenantId();
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            String stockImageBase64 = null;
            if (StringUtils.isNotBlank(stockPersonIris.getImageUrl())) {
                stockImageBase64 = PlatformFileUtils.getImageBase64(stockPersonIris.getImageUrl());
                stockImageBase64 = DictConstants.Encrypted.ENABLE.equals(stockPersonIris.getEncrypted())
                    ? PlatformCryptUtils.decryptImageBase64(stockImageBase64) : stockImageBase64;
            }
            PersonIrisMatchLog irisMatchLog = new PersonIrisMatchLog();
            // 进行比对图片的上传
            String baseDir = getSysIrisOne2OnePicBaseDir();
            // 加密上传现场照
            if (StringUtils.isNotBlank(irisVerify.getSceneImage())) {
                irisMatchLog.setSceneImage(
                    irisRecogLogicService.uploadIrisImg(true, null, irisVerify.getSceneImage(), baseDir));
            }
            // 底库照，重新存储，不能使用底库图片数据，底库图片可能会发生改变
            if (StringUtils.isNotBlank(stockImageBase64)) {
                irisMatchLog.setStockImage(irisRecogLogicService.uploadIrisImg(true, null, stockImageBase64, baseDir));
            }
            irisMatchLog.setSceneStockResult(verifyVO.getResult());
            irisMatchLog.setResult(verifyVO.getResult());
            irisMatchLog.setSceneStockScore(StringUtils.isBlank(verifyVO.getSceneStockScore()) ? null
                : Double.valueOf(verifyVO.getSceneStockScore()));
            irisMatchLog.setId(IdWorker.getNextStringId());
            irisMatchLog.setChannelCode(irisVerify.getChannelCode());
            irisMatchLog.setReceivedSeq(irisVerify.getReceivedSeq());
            irisMatchLog.setCreateTime(DateUtils.getNowDate());
            irisMatchLog.setUniqueId(irisVerify.getUniqueId());
            irisMatchLog.setDeptId(deptId);
            irisMatchLog.setDeptName(deptName);
            irisMatchLog.setPersonName(personName);
            irisMatchLog.setReceivedTime(new Date(receivedTime));
            irisMatchLog.setTimeUsed(responseTime - receivedTime);
            irisMatchLog.setVendorCode("eyecool");
            if (CollectionUtils.isNotEmpty(matchBeanList)) {
                irisMatchLog.setServerId(matchBeanList.get(0).getServerId());
                irisMatchLog.setAlgsVersion(matchBeanList.get(0).getAlgVersion());
            }
            irisMatchLog.setResult(verifyVO.getResult());
            irisMatchLogMapper.insertPersonIrisMatchLog(irisMatchLog);
        });
    }

    /**
     * 获取虹膜1:1比对图片存放文件夹
     * 
     * @return
     */
    private String getSysIrisOne2OnePicBaseDir() {
        String baseDir = configService.selectConfigByKey(SysConfigConstants.BUSI_IRIS_COMPARE_DIR_KEY);
        if (StringUtils.isBlank(baseDir)) {
            throw new CustomException(MessageUtils.message("channel.busi.iris.service.match.folder.need",
                SysConfigConstants.BUSI_IRIS_COMPARE_DIR_KEY));
        }
        return baseDir;
    }

    /**
     * 虹膜识别(1:N)
     */
    @Override
    public List<PersonIrisRecogVO> recogPersonIris(PersonIrisRecog irisRecog) {
        // 查询平台是否开启了1-N功能
        String config = configService.selectConfigByKey(SysConfigConstants.PLATFORM_SEARCH_N_FUNCTION_OPEN_KEY);
        if (DictConstants.YesOrNoState.NO.equals(config)) {
            throw new CustomException(MessageUtils.message("channel.face.service.1n.search.not.opened"));
        }
        long receivedTime = System.currentTimeMillis();
        // 校验场景是否存在
        String channelCode = irisRecog.getChannelCode();
        ChannelInfo channelInfo = null;
        if (StringUtils.isNotBlank(channelCode)) {
            channelInfo = checkChannelExists(channelCode);
        }
        // 判断场景是否开通虹膜
        if (null != channelInfo) {
            String irisMode = channelInfo.getIrisMode();
            if (DictConstants.BioModeStatus.DISABLE.equals(irisMode)) {
                throw new CustomException(MessageUtils.message("channel.busi.iris.service.iris.not.open", channelCode));
            }
        }

        String tmpChannelCode = StringUtils.EMPTY;// 1-N识别方式, 初始化为空串
        String searchN = DictConstants.SearchNType.SEARCH_N_TYPE_ALL;
        if (null != channelInfo) {
            searchN = channelInfo.getSearchN();
        }
        boolean isSeqQuery = false;// 1-N校验方式是否为依次校验
        List<String> tmpChannelCodeList = Lists.newArrayList();// 1-N校验方式为依次校验时，有序放入非空子场景编码、非空场景号和全库查询参数
        if (DictConstants.SearchNType.SEARCH_N_TYPE_SUB.equals(searchN)) {
            tmpChannelCode = irisRecog.getSubTreasury();
            log.info("Sub-scene search, sub-scene code: {}", tmpChannelCode);
            // 场景设置的校验方式为查询子场景，但是没有传入子场景编码，不搜索直接报错返回
            if (StringUtils.isBlank(tmpChannelCode)) {
                throw new CustomException(
                    MessageUtils.message("channel.face.service.scenesearch.subscene.scenecode.empty"));
            }
            ChannelSubtreasuryInfo condition = new ChannelSubtreasuryInfo();
            condition.setChannelId(channelInfo.getId());
            condition.setSubTreasuryCode(tmpChannelCode);
            List<ChannelSubtreasuryInfo> subtreasuryInfoList =
                channelSubtreasuryInfoMapper.selectChannelSubtreasuryInfoList(condition);
            if (CollectionUtils.isEmpty(subtreasuryInfoList)) {
                throw new CustomException(
                    MessageUtils.message("channel.face.service.scene.subscene.not.exists", tmpChannelCode));
            }
        } else if (DictConstants.SearchNType.SEARCH_N_TYPE_CHANNEL.equals(searchN)) {
            tmpChannelCode = irisRecog.getChannelCode();
            log.info("Scene library search, scene number: {}", tmpChannelCode);
        } else if (DictConstants.SearchNType.SEARCH_N_TYPE_SEQ.equals(searchN)) { // 依次查询
            isSeqQuery = true;
            if (StringUtils.isNotBlank(irisRecog.getSubTreasury())) {
                tmpChannelCodeList.add(irisRecog.getSubTreasury());
            }
            if (StringUtils.isNotBlank(irisRecog.getChannelCode())) {
                tmpChannelCodeList.add(irisRecog.getChannelCode());
            }
            tmpChannelCodeList.add(StringUtils.EMPTY);
            log.info("Query search in turn, query parameters: {}", tmpChannelCodeList.toString());
        } else {
            log.info("Full library search, query parameters: {}", tmpChannelCode);
        }
        log.info("Full library search, query parameters: {}", tmpChannelCode);
        tmpChannelCode = tmpChannelCode == null ? StringUtils.EMPTY : tmpChannelCode;
        // 不传默认返回一条
        Integer topN = StringUtils.isBlank(irisRecog.getTopN()) ? ChannelParamConstants.IRIS_SEARCH_TOP_N_VALUE
            : Integer.valueOf(irisRecog.getTopN());// 返回数据条数
        // 获取场景1-N搜索阈值参数
        String searchNThresholdStr = irisRecog.getSearchNThreshold();
        Double searchNThreahold = StringUtils.isBlank(searchNThresholdStr) ? null : Double.valueOf(searchNThresholdStr);
        if (null == searchNThreahold && null != channelInfo) { // 从场景参数获取1-N搜索阈值
            ChannelParam channelParam = getChannelParam(channelInfo.getId(), DictConstants.BioAttestType.IRIS,
                ChannelParamConstants.IRIS_SEARCH_N_THRESHOLD_CODE);
            searchNThreahold = null == channelParam ? null : Double.valueOf(channelParam.getParamValue());
        }
        // 执行1:N微服务调用搜索
        List<IrisSearchResult> irisSearchNResults = searchIris(irisRecog.getSceneImage(), tmpChannelCode, topN,
            searchNThreahold, isSeqQuery, tmpChannelCodeList, irisRecog.getSceneFeature());
        List<PersonIrisRecogVO> searchNResultList = handleIrisSearchNResult(irisSearchNResults, searchN,
            null == channelInfo ? null : channelInfo.getId(), irisRecog.getSubTreasury());
        // 保存虹膜识别日志
        asyncSaveIrisSearchLog(irisRecog, irisSearchNResults, searchNResultList, receivedTime);
        return searchNResultList;
    }

    /**
     * 异步执行1:N微服务调用搜索
     * 
     * @param sceneImg
     * @param tmpChannelCode
     * @param topN
     * @param searchNThreahold
     * @param isSeqQuery 是否是依次查询
     * @param tmpChannelCodeList 依次查询的参数列表
     * @return
     */
    private List<IrisSearchResult> searchIris(String sceneImg, String tmpChannelCode, Integer topN,
        Double searchNThreahold, boolean isSeqQuery, List<String> tmpChannelCodeList, String sceneFeature) {
        if (StringUtils.isBlank(sceneImg) && StringUtils.isBlank(sceneFeature)) {
            return null;
        }
        if (StringUtils.isBlank(sceneFeature)) {
            FeatureBean featureBean = irisRecogLogicService.getFeatureBean(sceneImg);
            sceneFeature = featureBean.getFeature();
        }
        // 执行1-N比对搜索
        if (isSeqQuery) {
            List<IrisSearchResult> irisSearchNResults = null;
            for (String tmpArg : tmpChannelCodeList) {
                irisSearchNResults = irisRecogLogicService.irisSearchN(sceneFeature, tmpArg, topN, searchNThreahold);
                if (CollectionUtils.isNotEmpty(irisSearchNResults)) {
                    return irisSearchNResults;
                }
            }
            return null;
        } else {
            return irisRecogLogicService.irisSearchN(sceneFeature, tmpChannelCode, topN, searchNThreahold);
        }
    }

    /**
     * 处理虹膜1:N比对搜索结果
     * 
     * @param irisSearchNResults1
     * @param irisSearchNResults2
     * @param searchN
     * @param channelId
     * @param strategy
     * @return
     */
    private List<PersonIrisRecogVO> handleIrisSearchNResult(List<IrisSearchResult> irisSearchNResults, String searchN,
        String channelId, String subTreasuryCode) {
        if (CollectionUtils.isEmpty(irisSearchNResults)) {
            return Collections.emptyList();
        }
        List<PersonIrisRecogVO> personIrisRecogVOList = irisSearchNResults.stream().filter(it -> null != it).map(it -> {
            String featureId = it.getFeatureId();
            PersonBioRecogBaseVO recogBaseVO = handleSearchNSingleResult(it.getUserId(), searchN, channelId,
                it.getScore(), featureId.substring(featureId.indexOf("_") + 1), subTreasuryCode);
            if (null == recogBaseVO) {
                return null;
            }
            PersonIrisRecogVO vo = new PersonIrisRecogVO();
            BeanUtils.copyBeanProp(vo, recogBaseVO);
            return vo;
        }).filter(it -> null != it).collect(Collectors.toList());
        return personIrisRecogVOList;
    }

    /**
     * 处理1：N结果,添加场景信息
     * 
     * @param uniqueId
     * @param searchN
     * @param channelId
     * @param score
     * @param irisId
     * @param subTreasuryCode
     * @return
     */
    private PersonBioRecogBaseVO handleSearchNSingleResult(String uniqueId, String searchN, String channelId,
        double score, String irisId, String subTreasuryCode) {
        // 查询basePersonInfo是否存在有效信息, 只有有效才返回
        BasePersonInfo personInfoCondition = new BasePersonInfo();
        personInfoCondition.setUniqueId(uniqueId);
        personInfoCondition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonInfo> personInfoList = basePersonInfoMapper.selectBasePersonInfoList(personInfoCondition);
        if (CollectionUtils.isEmpty(personInfoList)) {
            log.error(
                "[FOX_MINISEARCH] The iris 1-N search result is wrong, and the person [uniqueId:{}] that does not exist (or has an invalid status) in the relation library is identified!!!",
                uniqueId);
            return null;
        }
        // 查询虹膜是否存在有效信息(防止abis搜索结果有误)
        BasePersonIris iris = basePersonIrisMapper.selectBasePersonIrisById(irisId);
        if (null == iris || DictConstants.Status.DISABLE.equals(iris.getStatus())) {
            log.error(
                "[FOX_MINISEARCH] The search result of face 1-N is wrong, and the iris [uniqueId:{}, irisId:{}] that does not exist (or the status is invalid) in the relation library is recognized!!!",
                uniqueId, irisId);
            return null;
        }
        BasePersonInfo personInfo = personInfoList.get(0);
        if (DictConstants.SearchNType.SEARCH_N_TYPE_SUB.equals(searchN)) {// 查询人在子场景下是否存在(防止abis搜索结果有误)
            ChannelSubtreasuryBusi subCondition = new ChannelSubtreasuryBusi();
            subCondition.setChannelId(channelId);
            subCondition.setSubTreasuryCode(subTreasuryCode);
            subCondition.setPersonId(personInfo.getId());
            subCondition.setStatus(DictConstants.Status.ENABLE);
            List<ChannelSubtreasuryBusi> subtreasuryBusiList =
                channelSubtreasuryBusiMapper.selectChannelSubtreasuryBusiList(subCondition);
            if (CollectionUtils.isEmpty(subtreasuryBusiList)) {
                log.error(
                    "[FOX_MINISEARCH] The iris 1-N search result is wrong, and the person [subTreasuryCode:{},uniqueId:{}] that does not exist (or the status is invalid) in the relationship library (sub-scene) is identified!!!",
                    subTreasuryCode, uniqueId);
                return null;
            }
        }
        PersonBioRecogBaseVO recogVO = new PersonBioRecogBaseVO();
        recogVO.setUniqueId(personInfo.getUniqueId());
        recogVO.setScore(String.valueOf(score));
        recogVO.setSearchNType(searchN);
        recogVO.setScenesInfo(Lists.newArrayList());
        // 查询场景(场景)业务信息
        ChannelBusiness busiCondition = new ChannelBusiness();
        if (!DictConstants.SearchNType.SEARCH_N_TYPE_ALL.equals(searchN)) {
            // 查询全库的话，就不传入场景ID
            busiCondition.setChannelId(channelId);
        }
        busiCondition.setPersonId(personInfo.getId());
        busiCondition.setStatus(DictConstants.Status.ENABLE);
        List<ChannelBusiness> channelBusinessList = channelBusinessMapper.selectChannelBusinessList(busiCondition);
        // 查询人在场景下是否存在(防止abis搜索结果有误)
        if (DictConstants.SearchNType.SEARCH_N_TYPE_CHANNEL.equals(searchN)
            && CollectionUtils.isEmpty(channelBusinessList)) {
            log.error(
                "[FOX_MINISEARCH] The iris 1-N search result is wrong, and the person [channelCode:{},uniqueId:{}] who does not exist (or the status is invalid) in the relation library (scene library) is identified!!!",
                channelBusinessList.get(0).getChannelCode(), uniqueId);
            return null;
        }
        if (CollectionUtils.isNotEmpty(channelBusinessList)) {
            List<PersonBioRecogSceneVO> scenesInfo = channelBusinessList.stream().map(busi -> {
                // 查询场景信息
                ChannelInfo sceneChannel = channelInfoMapper.selectChannelInfoById(busi.getChannelId());
                PersonBioRecogSceneVO recogSceneVO = new PersonBioRecogSceneVO();
                recogSceneVO.setChannelCode(sceneChannel.getChannelCode());
                recogSceneVO.setBusiCodeFirst(StringUtils.trimToEmpty(busi.getBusiCodeFirst()));
                recogSceneVO.setBusiCodeSecond(StringUtils.trimToEmpty(busi.getBusiCodeSecond()));
                recogSceneVO.setBusiCodeThird(StringUtils.trimToEmpty(busi.getBusiCodeThird()));
                return recogSceneVO;
            }).filter(it -> null != it).collect(Collectors.toList());
            recogVO.setScenesInfo(scenesInfo);
        }
        return recogVO;
    }

    /**
     * 校验场景是否存在
     * 
     * @param channelCode
     * @return
     */
    private ChannelInfo checkChannelExists(String channelCode) {
        ChannelInfo infoCondition = new ChannelInfo();
        infoCondition.setChannelCode(channelCode);
        List<ChannelInfo> infoList = channelInfoMapper.selectChannelInfoList(infoCondition);
        if (CollectionUtils.isEmpty(infoList)) {
            throw new CustomException(
                MessageUtils.message("channel.common.service.channelcode.not.exists", channelCode));
        }
        ChannelInfo channelInfo = infoList.get(0);
        return channelInfo;
    }

    /**
     * 异步保存虹膜1:N搜索日志
     * 
     * @param irisRecog
     * @param irisSearchNResults
     * @param searchNResultList
     * @param receivedTime
     */
    private void asyncSaveIrisSearchLog(PersonIrisRecog irisRecog, List<IrisSearchResult> irisSearchNResults,
        List<PersonIrisRecogVO> searchNResultList, long receivedTime) {
        long responseTime = System.currentTimeMillis();
        String tenantId = TenantContextHolder.getTenantId();
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            // 按照得分倒序排序，获取比对最高分元素
            PersonIrisRecogVO recogVO = CollectionUtils.isNotEmpty(searchNResultList) ? searchNResultList.get(0) : null;
            String serverId =
                CollectionUtils.isNotEmpty(irisSearchNResults) ? irisSearchNResults.get(0).getServerId() : null;
            String algsVersion =
                CollectionUtils.isNotEmpty(irisSearchNResults) ? irisSearchNResults.get(0).getAlgVersion() : null;
            Long deptId = null;
            String deptName = null;
            String personName = null;
            String stockImageBase64 = null;
            if (null != recogVO) {
                // 查询人员部门信息
                BasePersonInfo personInfo = checkBasePersonExists(recogVO.getUniqueId());
                deptId = personInfo.getDeptId();
                personName = personInfo.getName();
                if (null != deptId) {
                    SysDept sysDept = sysDeptMapper.selectDeptById(deptId);
                    deptName = null == sysDept ? null : sysDept.getDeptName();
                }
                // 查询底库图片
                List<BasePersonIris> basePersonIrisList = getBasePersonIris(personInfo.getId());
                if (CollectionUtils.isNotEmpty(basePersonIrisList)) {
                    stockImageBase64 = PlatformFileUtils.getImageBase64(basePersonIrisList.get(0).getImageUrl());
                    if (DictConstants.Encrypted.ENABLE.equals(basePersonIrisList.get(0).getEncrypted())) {
                        stockImageBase64 = PlatformCryptUtils.decryptImageBase64(stockImageBase64);
                    }
                }
            }
            PersonIrisSearchLog irisSearchLog = new PersonIrisSearchLog();
            // 进行比对图片的上传
            String baseDir = getSysIrisSearchPicBaseDir();
            // 加密上传现场照
            if (StringUtils.isNotBlank(irisRecog.getSceneImage())) {
                irisSearchLog
                    .setSceneImage(irisRecogLogicService.uploadIrisImg(true, null, irisRecog.getSceneImage(), baseDir));
            }
            // 底库照，重新存储，不能使用底库图片数据，底库图片可能会发生改变
            if (StringUtils.isNotBlank(stockImageBase64)) {
                irisSearchLog.setStockImage(irisRecogLogicService.uploadIrisImg(true, null, stockImageBase64, baseDir));
            }
            String result = null != recogVO ? BioResult.PASS : BioResult.NOTPASS;
            irisSearchLog.setResult(result);
            irisSearchLog.setSceneStockScore(null == recogVO ? null : Double.valueOf(recogVO.getScore()));
            irisSearchLog.setId(IdWorker.getNextStringId());
            irisSearchLog.setChannelCode(irisRecog.getChannelCode());
            irisSearchLog.setReceivedSeq(irisRecog.getReceivedSeq());
            irisSearchLog.setCreateTime(DateUtils.getNowDate());
            irisSearchLog.setUniqueId(null == recogVO ? null : recogVO.getUniqueId());
            irisSearchLog.setDeptId(deptId);
            irisSearchLog.setDeptName(deptName);
            irisSearchLog.setPersonName(personName);
            irisSearchLog.setReceivedTime(new Date(receivedTime));
            irisSearchLog.setTimeUsed(responseTime - receivedTime);
            irisSearchLog.setServerId(serverId);
            irisSearchLog.setVendorCode("eyecool");
            irisSearchLog.setAlgsVersion(algsVersion);
            irisSearchLog.setSubTreasuryCode(irisRecog.getSubTreasury());
            // 查询子场景名
            if (StringUtils.isNotBlank(irisRecog.getSubTreasury())) {
                ChannelInfo channelInfo = new ChannelInfo();
                channelInfo.setChannelCode(irisRecog.getChannelCode());
                List<ChannelInfo> channelInfoList = channelInfoMapper.selectChannelInfoList(channelInfo);
                ChannelSubtreasuryInfo condition = new ChannelSubtreasuryInfo();
                condition.setChannelId(channelInfoList.get(0).getId());
                condition.setSubTreasuryCode(irisRecog.getSubTreasury());
                List<ChannelSubtreasuryInfo> subtreasuryInfoList =
                    channelSubtreasuryInfoMapper.selectChannelSubtreasuryInfoList(condition);
                if (CollectionUtils.isNotEmpty(subtreasuryInfoList)) {
                    irisSearchLog.setSubTreasuryName(subtreasuryInfoList.get(0).getSubTreasuryName());
                }
            }
            irisSearchLog.setSceneType(DictConstants.SearchNLogSceneType.SEARCH_N_HTTP);
            irisSearchLogMapper.insertPersonIrisSearchLog(irisSearchLog);
        });
    }

    /**
     * 获取虹膜1:N搜索图片存放文件夹
     * 
     * @return
     */
    private String getSysIrisSearchPicBaseDir() {
        String baseDir = configService.selectConfigByKey(SysConfigConstants.BUSI_IRIS_SEARCH_DIR_KEY);
        if (StringUtils.isBlank(baseDir)) {
            throw new CustomException(MessageUtils.message("channel.busi.iris.service.search.folder.need",
                SysConfigConstants.BUSI_IRIS_SEARCH_DIR_KEY));
        }
        return baseDir;
    }

    /**
     * 比对两张虹膜图片
     */
    @Override
    public Map<String, Object> compareTwoImage(String imageBase64_1, String imageBase64_2, Double threshold,
        String channelCode) {
        // 校验场景是否存在
        if (StringUtils.isNotBlank(channelCode)) {
            checkChannelExists(channelCode);
        }
        FeatureBean featureBean1 = irisRecogLogicService.getFeatureBean(imageBase64_1);
        FeatureBean featureBean2 = irisRecogLogicService.getFeatureBean(imageBase64_2);
        List<MatchBean> matchBeanList =
            irisRecogLogicService.irisOne2OneCompare(Arrays.asList(featureBean1, featureBean2));
        double score = matchBeanList.get(0).getResults().get(0).getScore();
        if (null == threshold) {
            threshold = irisRecogLogicService.getOne2OneCompareThreshold();
        }
        Map<String, Object> map = Maps.newHashMap();
        map.put("score", score);
        map.put("threshold", threshold);
        map.put("result", score > threshold);
        return map;
    }

    /**
     * 提取虹膜特征
     */
    @Override
    public List<FeatureBean> getPersonIrisFeature(String sceneImage, String channelCode) {
        // 校验场景是否存在
        if (StringUtils.isNotBlank(channelCode)) {
            checkChannelExists(channelCode);
        }
        // 提取现场照人脸特征
        return irisRecogLogicService.getMultiPersonIrisFeature(sceneImage);
    }

}
