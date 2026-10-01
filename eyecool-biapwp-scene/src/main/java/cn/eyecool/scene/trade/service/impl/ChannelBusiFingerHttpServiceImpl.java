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
import com.eyecool.abis.callmicroservice.common.FingerSearchResult;
import com.eyecool.abis.callmicroservice.common.MatchBean;
import com.google.common.collect.Lists;

import cn.eyecool.abis.detect.commons.FeatureData;
import cn.eyecool.basedata.domain.BasePersonFinger;
import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.basedata.manager.IPersonFingerRecogLogicService;
import cn.eyecool.basedata.mapper.BasePersonFingerMapper;
import cn.eyecool.basedata.mapper.BasePersonInfoMapper;
import cn.eyecool.common.constant.DictConstants;
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
import cn.eyecool.scene.trade.entity.PersonFingerRecog;
import cn.eyecool.scene.trade.entity.PersonFingerVerify;
import cn.eyecool.scene.trade.service.IChannelBusiFingerHttpService;
import cn.eyecool.scene.trade.vo.PersonBioRecogBaseVO;
import cn.eyecool.scene.trade.vo.PersonBioRecogSceneVO;
import cn.eyecool.scene.trade.vo.PersonFingerRecogVO;
import cn.eyecool.scene.trade.vo.PersonFingerVerifyVO;
import cn.eyecool.system.mapper.SysDeptMapper;
import cn.eyecool.system.service.ISysConfigService;
import cn.eyecool.tradelog.domain.PersonFingerMatchLog;
import cn.eyecool.tradelog.domain.PersonFingerSearchLog;
import cn.eyecool.tradelog.mapper.PersonFingerMatchLogMapper;
import cn.eyecool.tradelog.mapper.PersonFingerSearchLogMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * 场景指纹业务HTTP服务层实现
 * 
 * @author admin
 * @date 2019年11月28日
 */
@Service
@Slf4j
public class ChannelBusiFingerHttpServiceImpl implements IChannelBusiFingerHttpService {

    @Autowired
    private ChannelInfoMapper channelInfoMapper;
    @Autowired
    private IPersonFingerRecogLogicService fingerRecogLogicService;
    @Autowired
    private BasePersonInfoMapper basePersonInfoMapper;
    @Autowired
    private BasePersonFingerMapper basePersonFingerMapper;
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
    private PersonFingerMatchLogMapper fingerMatchLogMapper;
    @Autowired
    private PersonFingerSearchLogMapper fingerSearchLogMapper;
    @Autowired
    private SysDeptMapper sysDeptMapper;

    /**
     * 指纹1:1认证
     */
    @Override
    public PersonFingerVerifyVO verifyPersonFinger(PersonFingerVerify fingerVerify) {
        long receivedTime = System.currentTimeMillis();
        // 校验场景是否存在
        String channelCode = fingerVerify.getChannelCode();
        ChannelInfo channelInfo = null;
        if (StringUtils.isNotBlank(channelCode)) {
            channelInfo = checkChannelExists(channelCode);
        }

        // 判断场景是否开通指纹
        if (null != channelInfo) {
            String fingerMode = channelInfo.getFingerMode();
            if (DictConstants.BioModeStatus.DISABLE.equals(fingerMode)) {
                throw new CustomException(
                    MessageUtils.message("channel.busi.finger.service.scene.not.open.finger", channelCode));
            }

        }
        // 校验人员信息是否存在
        String uniqueId = fingerVerify.getUniqueId();
        BasePersonInfo basePersonInfo = checkBasePersonExists(uniqueId);
        String personName = basePersonInfo.getName();
        Long deptId = basePersonInfo.getDeptId();
        String deptName = null;
        if (null != deptId) {
            SysDept sysDept = sysDeptMapper.selectDeptById(deptId);
            if (null != sysDept) {
                deptName = sysDept.getDeptName();
            }
        }

        // 判断当前人员是否开通指纹
        if (null != channelInfo) {
            ChannelBusiness channelBusiness = getChannelBusiness(channelInfo.getId(), basePersonInfo.getId());
            if (null == channelBusiness) {
                throw new CustomException(
                    MessageUtils.message("channel.face.service.scene.person.not.exists", channelCode, uniqueId));
            }
            if (DictConstants.BioModeStatus.DISABLE.equals(channelBusiness.getFingerMode())) {
                throw new CustomException(MessageUtils
                    .message("channel.busi.finger.service.scene.person.not.open.finger", channelCode, uniqueId));
            }
            if (DictConstants.YesOrNoState.YES.equalsIgnoreCase(channelBusiness.getLocked())) {
                throw new CustomException(
                    MessageUtils.message("channel.common.service.scene.person.locked", channelCode, uniqueId));
            }
        }

        // 定义比对List(有序放入现场照、底库照,可以没有，顺序不能乱)
        List<FeatureBean> compareFeatureBeanList = Lists.newArrayList();

        // 提取现场照指纹特征
        String sceneImage = fingerVerify.getSceneImage();
        FeatureBean sceneFImageFeatureBean = fingerRecogLogicService.getFeatureBean(sceneImage,
            MessageUtils.message("channel.busi.finger.service.liveimage.no.fingerprint"),
            MessageUtils.message("channel.busi.finger.service.liveimage.multi.fingerprint"));
        compareFeatureBeanList.add(sceneFImageFeatureBean);

        // 查询底库指纹照片, 人员的所有指纹照片
        String fingerNo = fingerVerify.getFingerNo();
        List<String> uncertainFingerNoList = Arrays.asList(DictConstants.UncertainFingerNo.LEFT_UNCERTAIN_FINGER,
            DictConstants.UncertainFingerNo.RIGHT_UNCERTAIN_FINGER,
            DictConstants.UncertainFingerNo.OTHER_UNCERTAIN_FINGER);
        String tmpFingerNo =
            StringUtils.isNotBlank(fingerNo) && !uncertainFingerNoList.contains(fingerNo) ? fingerNo : null;
        List<BasePersonFinger> basePersonFingerList = getBasePersonFinger(basePersonInfo.getId(), tmpFingerNo);
        boolean hasStockImage = CollectionUtils.isNotEmpty(basePersonFingerList);
        if (!hasStockImage) {
            throw new CustomException(MessageUtils.message("channel.busi.finger.service.matchimage.not.exists"));
        }
        // 获取指纹对比对阈值
        String compareThresholdStr = fingerVerify.getCompareThreshold();
        double fingerCompareThreshold = StringUtils.isBlank(compareThresholdStr)
            ? getFingerMatchThreshold(null == channelInfo ? null : channelInfo.getId())
            : Double.valueOf(compareThresholdStr);
        PersonFingerVerifyVO verifyVO = null;
        double maxScore = 0D;// 比对最大得分
        String algsVersion = null;
        String serverId = null;
        BasePersonFinger maxScoreFinger = null;
        DecimalFormat df = new DecimalFormat("0.00");
        for (BasePersonFinger finger : basePersonFingerList) {
            FeatureBean stockImageFeatureBean = new FeatureBean();
            stockImageFeatureBean.setFeature(finger.getFeature());
            stockImageFeatureBean.setType(String.valueOf(FeatureData.FeatureType.FingerFeatureUnknown));
            compareFeatureBeanList.add(1, stockImageFeatureBean);
            // 进行1:1比对
            List<MatchBean> matchBeanList = fingerRecogLogicService.fingerOne2OneCompare(compareFeatureBeanList);
            if (CollectionUtils.isEmpty(matchBeanList)) {
                continue;
            }
            // 现场照与底库的比对结果
            double sceneStockScore = matchBeanList.get(0).getResults().get(0).getScore();
            boolean result = sceneStockScore > fingerCompareThreshold;
            maxScoreFinger = maxScore < sceneStockScore ? finger : maxScoreFinger;
            maxScore = Math.max(maxScore, sceneStockScore);
            algsVersion = matchBeanList.get(0).getAlgVersion();
            serverId = matchBeanList.get(0).getServerId();
            if (result) {
                verifyVO = new PersonFingerVerifyVO();
                verifyVO.setSceneStockScore(df.format(sceneStockScore));
                verifyVO.setSceneStockResult(sceneStockScore > fingerCompareThreshold ? DictConstants.BioResult.PASS
                    : DictConstants.BioResult.NOTPASS);
                verifyVO.setResult(sceneStockScore > fingerCompareThreshold ? DictConstants.BioResult.PASS
                    : DictConstants.BioResult.NOTPASS);
                verifyVO.setFingerNo(finger.getFingerNo());
                verifyVO.setFingerId(finger.getId());
                // 异步保存指纹比对日志
                asyncSaveFingerMatchLog(fingerVerify, verifyVO, finger.getImageUrl(), finger.getEncrypted(),
                    receivedTime, deptId, deptName, personName, serverId, algsVersion);
                return verifyVO;
            }
        }
        verifyVO = new PersonFingerVerifyVO();
        verifyVO.setSceneStockScore(df.format(maxScore));
        verifyVO.setSceneStockResult(DictConstants.BioResult.NOTPASS);
        verifyVO.setResult(DictConstants.BioResult.NOTPASS);
        String stockImageUrl = null;
        String stockImageEncrypted = null;
        if (null != maxScoreFinger) {
            verifyVO.setFingerNo(maxScoreFinger.getFingerNo());
            verifyVO.setFingerId(maxScoreFinger.getId());
            stockImageUrl = maxScoreFinger.getImageUrl();
            stockImageEncrypted = maxScoreFinger.getEncrypted();
        }
        // 异步保存指纹比对日志
        asyncSaveFingerMatchLog(fingerVerify, verifyVO, stockImageUrl, stockImageEncrypted, receivedTime, deptId,
            deptName, personName, serverId, algsVersion);
        return verifyVO;
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
     * 查询指纹信息
     * 
     * @param personId
     * @param fingerNo
     * @return
     */
    private List<BasePersonFinger> getBasePersonFinger(String personId, String fingerNo) {
        BasePersonFinger fingerCondition = new BasePersonFinger();
        fingerCondition.setPersonId(personId);
        fingerCondition.setStatus(DictConstants.Status.ENABLE);
        fingerCondition.setFingerNo(fingerNo);
        List<BasePersonFinger> fingerList = basePersonFingerMapper.selectBasePersonFingerList(fingerCondition);
        if (CollectionUtils.isEmpty(fingerList)) {
            return Collections.emptyList();
        }
        return fingerList;
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
     * 获取指纹1：1比对阈值
     * 
     * @param channelId
     * @return
     */
    private double getFingerMatchThreshold(String channelId) {
        if (StringUtils.isNotBlank(channelId)) {
            ChannelParam channelParam = getChannelParam(channelId, DictConstants.BioAttestType.FINGER,
                ChannelParamConstants.FINGER_COMPARE_THRESHOLD_CODE);
            if (null != channelParam) {
                return Double.valueOf(channelParam.getParamValue());
            }
        }
        // 场景参数不存在，则查询公共参数配置
        return fingerRecogLogicService.getOne2OneCompareThreshold();
    }

    /**
     * 异步保存指纹1:1比对日志
     * 
     * @param fingerVerify
     * @param verifyVO
     * @param stockImageUrl
     * @param stockImageEncrypted
     * @param receivedTime
     * @param deptId
     * @param serverId
     * @param algsVersion
     */
    private void asyncSaveFingerMatchLog(PersonFingerVerify fingerVerify, PersonFingerVerifyVO verifyVO,
        String stockImageUrl, String stockImageEncrypted, long receivedTime, Long deptId, String deptName,
        String personName, String serverId, String algsVersion) {
        long responseTime = System.currentTimeMillis();
        String tenantId = TenantContextHolder.getTenantId();
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            String stockImageBase64 = null;
            if (StringUtils.isNotBlank(stockImageUrl)) {
                stockImageBase64 = PlatformFileUtils.getImageBase64(stockImageUrl);
                stockImageBase64 = DictConstants.Encrypted.ENABLE.equals(stockImageEncrypted)
                    ? PlatformCryptUtils.decryptImageBase64(stockImageBase64) : stockImageBase64;
            }
            PersonFingerMatchLog fingerMatchLog = new PersonFingerMatchLog();
            // 进行比对图片的上传
            String baseDir = getSysFingerOne2OnePicBaseDir();
            // 加密上传现场照
            fingerMatchLog.setSceneImage(
                fingerRecogLogicService.uploadFingerImg(true, null, fingerVerify.getSceneImage(), baseDir));
            // 底库照，重新存储，不能使用底库图片数据，底库图片可能会发生改变
            if (StringUtils.isNotBlank(stockImageBase64)) {
                fingerMatchLog
                    .setStockImage(fingerRecogLogicService.uploadFingerImg(true, null, stockImageBase64, baseDir));
            }
            fingerMatchLog.setId(IdWorker.getNextStringId());
            fingerMatchLog.setChannelCode(fingerVerify.getChannelCode());
            fingerMatchLog.setReceivedSeq(fingerVerify.getReceivedSeq());
            fingerMatchLog.setCreateTime(DateUtils.getNowDate());
            fingerMatchLog.setUniqueId(fingerVerify.getUniqueId());
            fingerMatchLog.setDeptId(deptId);
            fingerMatchLog.setDeptName(deptName);
            fingerMatchLog.setPersonName(personName);
            fingerMatchLog.setReceivedTime(new Date(receivedTime));
            fingerMatchLog.setTimeUsed(responseTime - receivedTime);
            fingerMatchLog.setServerId(serverId);
            fingerMatchLog.setVendorCode("eyecool");
            fingerMatchLog.setAlgsVersion(algsVersion);
            fingerMatchLog.setSceneStockResult(verifyVO.getSceneStockResult());
            fingerMatchLog.setSceneStockScore(StringUtils.isBlank(verifyVO.getSceneStockScore()) ? null
                : Double.valueOf(verifyVO.getSceneStockScore()));
            fingerMatchLog.setResult(verifyVO.getResult());
            fingerMatchLog.setFingerNo(verifyVO.getFingerNo());
            fingerMatchLogMapper.insertPersonFingerMatchLog(fingerMatchLog);
        });
    }

    /**
     * 获取指纹1:1比对图片存放文件夹
     * 
     * @return
     */
    private String getSysFingerOne2OnePicBaseDir() {
        String baseDir = configService.selectConfigByKey(SysConfigConstants.BUSI_FINGER_COMPARE_DIR_KEY);
        if (StringUtils.isBlank(baseDir)) {
            throw new CustomException(MessageUtils.message("channel.busi.finger.service.match.folder.need",
                SysConfigConstants.BUSI_FINGER_COMPARE_DIR_KEY));
        }
        return baseDir;
    }

    /**
     * 指纹识别(1:N识别)
     */
    @Override
    public List<PersonFingerRecogVO> recogPersonFinger(PersonFingerRecog fingerRecog) {
        // 查询平台是否开启了1-N功能
        String config = configService.selectConfigByKey(SysConfigConstants.PLATFORM_SEARCH_N_FUNCTION_OPEN_KEY);
        if (DictConstants.YesOrNoState.NO.equals(config)) {
            throw new CustomException(MessageUtils.message("channel.busi.finger.service.search.not.opened"));
        }
        long receivedTime = System.currentTimeMillis();
        // 校验场景是否存在
        String channelCode = fingerRecog.getChannelCode();
        ChannelInfo channelInfo = null;
        if (StringUtils.isNotBlank(channelCode)) {
            channelInfo = checkChannelExists(channelCode);
        }
        if (null != channelInfo) {
            // 判断场景是否开通指纹
            String fingerMode = channelInfo.getFingerMode();
            if (DictConstants.BioModeStatus.DISABLE.equals(fingerMode)) {
                throw new CustomException(
                    MessageUtils.message("channel.busi.finger.service.scene.subscene.not.opened", channelCode));
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
            tmpChannelCode = fingerRecog.getSubTreasury();
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
            tmpChannelCode = fingerRecog.getChannelCode();
            log.info("Scene library search, scene number: {}", tmpChannelCode);
        } else if (DictConstants.SearchNType.SEARCH_N_TYPE_SEQ.equals(searchN)) { // 依次查询
            isSeqQuery = true;
            if (StringUtils.isNotBlank(fingerRecog.getSubTreasury())) {
                tmpChannelCodeList.add(fingerRecog.getSubTreasury());
            }
            if (StringUtils.isNotBlank(fingerRecog.getChannelCode())) {
                tmpChannelCodeList.add(fingerRecog.getChannelCode());
            }
            tmpChannelCodeList.add(StringUtils.EMPTY);
            log.info("Query search in turn, query parameters: {}", tmpChannelCodeList.toString());
        } else {
            log.info("Full library search, query parameters: {}", tmpChannelCode);
        }
        log.info("Full library search, query parameters: {}", tmpChannelCode);
        tmpChannelCode = tmpChannelCode == null ? StringUtils.EMPTY : tmpChannelCode;
        // 不传默认返回一条
        Integer topN = StringUtils.isBlank(fingerRecog.getTopN()) ? ChannelParamConstants.FINGER_SEARCH_TOP_N_VALUE
            : Integer.valueOf(fingerRecog.getTopN());// 返回数据条数
        // 获取场景1-N搜索阈值参数
        String searchNThresholdStr = fingerRecog.getSearchNThreshold();
        Double searchNThreahold = StringUtils.isBlank(searchNThresholdStr) ? null : Double.valueOf(searchNThresholdStr);
        if (null == searchNThreahold && null != channelInfo) { // 从场景参数获取1-N搜索阈值
            ChannelParam channelParam = getChannelParam(channelInfo.getId(), DictConstants.BioAttestType.FINGER,
                ChannelParamConstants.FINGER_SEARCH_N_THRESHOLD_CODE);
            searchNThreahold = null == channelParam ? null : Double.valueOf(channelParam.getParamValue());
        }
        // 获取现场照特征
        FeatureBean featureBean = fingerRecogLogicService.getFeatureBean(fingerRecog.getSceneImage());
        // 执行1-N比对搜索
        List<FingerSearchResult> fingerSearchNResults = null;
        if (isSeqQuery) {
            for (String tmpArg : tmpChannelCodeList) {
                fingerSearchNResults =
                    fingerRecogLogicService.fingerSearchN(featureBean.getFeature(), tmpArg, topN, searchNThreahold);
                if (CollectionUtils.isNotEmpty(fingerSearchNResults)) {
                    break;
                }
            }
        } else {
            fingerSearchNResults =
                fingerRecogLogicService.fingerSearchN(featureBean.getFeature(), tmpChannelCode, topN, searchNThreahold);
        }
        List<PersonFingerRecogVO> searchNResultList = handleFingerSearchNResult(fingerSearchNResults, searchN,
            null == channelInfo ? null : channelInfo.getId(), fingerRecog.getSubTreasury());
        // 异步保存搜索日志
        asyncSaveFingerSearchLog(fingerRecog, fingerSearchNResults, searchNResultList, receivedTime);
        return searchNResultList;
    }

    /**
     * 处理指纹1:N比对搜索结果
     * 
     * @param fingerSearchNResults
     * @param searchN
     * @param channelId
     * @param subTreasuryCode
     * @return
     */
    private List<PersonFingerRecogVO> handleFingerSearchNResult(List<FingerSearchResult> fingerSearchNResults,
        String searchN, String channelId, String subTreasuryCode) {
        if (CollectionUtils.isEmpty(fingerSearchNResults)) {
            return Collections.emptyList();
        }
        List<PersonFingerRecogVO> personFingerRecogVOList =
            fingerSearchNResults.stream().filter(it -> null != it).map(it -> {
                String featureId = it.getFeatureId();
                String fingerNo = featureId.substring(featureId.indexOf("_") + 1, featureId.indexOf("_") + 3);
                String fingerId = featureId.substring(featureId.indexOf("_") + 4);
                PersonBioRecogBaseVO recogBaseVO = handleSearchNSingleResult(it.getUserId(), searchN, channelId,
                    it.getScore(), fingerId, subTreasuryCode);
                if (null == recogBaseVO) {
                    return null;
                }
                PersonFingerRecogVO vo = new PersonFingerRecogVO();
                BeanUtils.copyBeanProp(vo, recogBaseVO);
                vo.setFingerNo(fingerNo);
                return vo;
            }).filter(it -> null != it).collect(Collectors.toList());
        return personFingerRecogVOList;
    }

    /**
     * 处理1：N结果,添加场景信息
     * 
     * @param uniqueId
     * @param searchN
     * @param channelId
     * @param score
     * @param fingerId
     * @param subTreasuryCode
     * @return
     */
    private PersonBioRecogBaseVO handleSearchNSingleResult(String uniqueId, String searchN, String channelId,
        double score, String fingerId, String subTreasuryCode) {
        // 查询basePersonInfo是否存在有效信息, 只有有效才返回
        BasePersonInfo personInfoCondition = new BasePersonInfo();
        personInfoCondition.setUniqueId(uniqueId);
        personInfoCondition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonInfo> personInfoList = basePersonInfoMapper.selectBasePersonInfoList(personInfoCondition);
        if (CollectionUtils.isEmpty(personInfoList)) {
            log.error(
                "[FOX_MINISEARCH]Fingerprint 1-N search results are wrong, the person [uniqueId:{}] that does not exist (or has an invalid status) in the relationship database is identified!!!",
                uniqueId);
            return null;
        }
        BasePersonInfo personInfo = personInfoList.get(0);
        // 查询指纹是否存在有效信息
        BasePersonFinger finger = basePersonFingerMapper.selectBasePersonFingerById(fingerId);
        if (null == finger || DictConstants.Status.DISABLE.equals(finger.getStatus())) {
            log.error(
                "[FOX_MINISEARCH]The search result of fingerprint 1-N is wrong, and the fingerprint [uniqueId:{}, fingerId:{}] that does not exist (or the status is invalid) in the relation database is recognized!!!",
                uniqueId, fingerId);
            return null;
        }
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
                    "[FOX_MINISEARCH] The search result of fingerprint 1-N is wrong, and the person [subTreasuryCode:{},uniqueId:{}, fingerId:{}] who does not exist (or the status is invalid) in the relationship database (sub-scenario) is identified!!!",
                    subTreasuryCode, uniqueId, fingerId);
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
                "[FOX_MINISEARCH] The search result of fingerprint 1-N is wrong, and the person [channelCode:{},uniqueId:{}, fingerId:{}] who does not exist (or the status is invalid) in the relation library (scene library) is identified!!!",
                channelBusinessList.get(0).getChannelCode(), uniqueId, fingerId);
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
     * 异步保存指纹1:N搜索日志
     * 
     * @param fingerRecog
     * @param fingerSearchNResults
     * @param searchNResultList
     * @param receivedTime
     */
    private void asyncSaveFingerSearchLog(PersonFingerRecog fingerRecog, List<FingerSearchResult> fingerSearchNResults,
        List<PersonFingerRecogVO> searchNResultList, long receivedTime) {
        long responseTime = System.currentTimeMillis();
        String tenantId = TenantContextHolder.getTenantId();
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            // 按照得分倒序排序，获取比对最高分元素
            PersonFingerRecogVO recogVO =
                CollectionUtils.isNotEmpty(searchNResultList) ? searchNResultList.get(0) : null;
            String serverId =
                CollectionUtils.isNotEmpty(fingerSearchNResults) ? fingerSearchNResults.get(0).getServerId() : null;
            String algsVersion =
                CollectionUtils.isNotEmpty(fingerSearchNResults) ? fingerSearchNResults.get(0).getAlgVersion() : null;
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
                List<BasePersonFinger> basePersonFingerList =
                    getBasePersonFinger(personInfo.getId(), recogVO.getFingerNo());
                if (CollectionUtils.isNotEmpty(basePersonFingerList)
                    && StringUtils.isNotBlank(basePersonFingerList.get(0).getImageUrl())) {
                    stockImageBase64 = PlatformFileUtils.getImageBase64(basePersonFingerList.get(0).getImageUrl());
                    if (DictConstants.Encrypted.ENABLE.equals(basePersonFingerList.get(0).getEncrypted())) {
                        stockImageBase64 = PlatformCryptUtils.decryptImageBase64(stockImageBase64);
                    }
                }
            }
            PersonFingerSearchLog fingerSearchLog = new PersonFingerSearchLog();
            // 进行比对图片的上传
            String baseDir = getSysFingerSearchPicBaseDir();
            // 加密上传现场照
            fingerSearchLog.setSceneImage(
                fingerRecogLogicService.uploadFingerImg(true, null, fingerRecog.getSceneImage(), baseDir));
            // 底库照，重新存储，不能使用底库图片数据，底库图片可能会发生改变
            if (StringUtils.isNotBlank(stockImageBase64)) {
                fingerSearchLog
                    .setStockImage(fingerRecogLogicService.uploadFingerImg(true, null, stockImageBase64, baseDir));
            }
            String result = null != recogVO ? DictConstants.BioResult.PASS : DictConstants.BioResult.NOTPASS;
            fingerSearchLog.setId(IdWorker.getNextStringId());
            fingerSearchLog.setChannelCode(fingerRecog.getChannelCode());
            fingerSearchLog.setReceivedSeq(fingerRecog.getReceivedSeq());
            fingerSearchLog.setCreateTime(DateUtils.getNowDate());
            fingerSearchLog.setUniqueId(null == recogVO ? null : recogVO.getUniqueId());
            fingerSearchLog.setDeptId(deptId);
            fingerSearchLog.setDeptName(deptName);
            fingerSearchLog.setPersonName(personName);
            fingerSearchLog.setFingerNo(null == recogVO ? null : recogVO.getFingerNo());
            fingerSearchLog.setReceivedTime(new Date(receivedTime));
            fingerSearchLog.setTimeUsed(responseTime - receivedTime);
            fingerSearchLog.setServerId(serverId);
            fingerSearchLog.setVendorCode("eyecool");
            fingerSearchLog.setAlgsVersion(algsVersion);
            fingerSearchLog.setSceneStockScore(null == recogVO ? null : Double.valueOf(recogVO.getScore()));
            fingerSearchLog.setSubTreasuryCode(fingerRecog.getSubTreasury());
            // 查询子场景名
            if (StringUtils.isNotBlank(fingerRecog.getSubTreasury())) {
                ChannelInfo channelInfo = new ChannelInfo();
                channelInfo.setChannelCode(fingerRecog.getChannelCode());
                List<ChannelInfo> channelInfoList = channelInfoMapper.selectChannelInfoList(channelInfo);
                ChannelSubtreasuryInfo condition = new ChannelSubtreasuryInfo();
                condition.setChannelId(channelInfoList.get(0).getId());
                condition.setSubTreasuryCode(fingerRecog.getSubTreasury());
                List<ChannelSubtreasuryInfo> subtreasuryInfoList =
                    channelSubtreasuryInfoMapper.selectChannelSubtreasuryInfoList(condition);
                if (CollectionUtils.isNotEmpty(subtreasuryInfoList)) {
                    fingerSearchLog.setSubTreasuryName(subtreasuryInfoList.get(0).getSubTreasuryName());
                }
            }
            fingerSearchLog.setResult(result);
            fingerSearchLog.setSceneType(DictConstants.SearchNLogSceneType.SEARCH_N_HTTP);
            fingerSearchLogMapper.insertPersonFingerSearchLog(fingerSearchLog);
        });
    }

    /**
     * 获取指纹1:N搜索图片存放文件夹
     * 
     * @return
     */
    private String getSysFingerSearchPicBaseDir() {
        String baseDir = configService.selectConfigByKey(SysConfigConstants.BUSI_FINGER_SEARCH_DIR_KEY);
        if (StringUtils.isBlank(baseDir)) {
            throw new CustomException(MessageUtils.message("channel.busi.finger.service.search.folder.need",
                SysConfigConstants.BUSI_FINGER_SEARCH_DIR_KEY));
        }
        return baseDir;
    }

    /**
     * 比对两张图片
     */
    @Override
    public Map<String, Object> compareTwoImage(String imageBase64_1, String imageBase64_2, Double threshold,
        String channelCode) {
        // 校验场景是否存在
        if (StringUtils.isNotBlank(channelCode)) {
            checkChannelExists(channelCode);
        }
        FeatureBean featureBean1 = fingerRecogLogicService.getFeatureBean(imageBase64_1);
        FeatureBean featureBean2 = fingerRecogLogicService.getFeatureBean(imageBase64_2);
        List<MatchBean> matchBeanList =
            fingerRecogLogicService.fingerOne2OneCompare(Arrays.asList(featureBean1, featureBean2));
        double score = matchBeanList.get(0).getResults().get(0).getScore();
        if (null == threshold) {
            threshold = fingerRecogLogicService.getOne2OneCompareThreshold();
        }
        Map<String, Object> map = Maps.newHashMap();
        map.put("score", score);
        map.put("threshold", threshold);
        map.put("result", score > threshold);
        return map;
    }

    /**
     * 提取指纹特征信息
     */
    @Override
    public List<FeatureBean> getPersonFingerFeature(String sceneImage, String channelCode) {
        // 校验场景是否存在
        if (StringUtils.isNotBlank(channelCode)) {
            checkChannelExists(channelCode);
        }
        // 提取现场照人脸特征
        return fingerRecogLogicService.getMultiPersonFingerFeature(sceneImage);
    }

    /**
     * 指纹图片质量检测
     */
    @Override
    public Map<String, Object> personFingerQualityDetect(String sceneImage, Double threshold, String channelCode) {
        // 校验场景是否存在
        if (StringUtils.isNotBlank(channelCode)) {
            checkChannelExists(channelCode);
        }
        double score = fingerRecogLogicService.qualityDetect(sceneImage, -1D, null);
        if (null == threshold) {
            threshold = fingerRecogLogicService.getDetectThreshold();
        }
        Map<String, Object> map = Maps.newHashMap();
        map.put("score", score);
        map.put("threshold", threshold);
        map.put("result", score > threshold);
        return map;
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
}
