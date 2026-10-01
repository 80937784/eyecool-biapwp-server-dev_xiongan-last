package cn.eyecool.scene.trade.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.basedata.manager.IPersonDataManagerLogicService;
import cn.eyecool.basedata.mapper.BasePersonInfoMapper;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.core.text.Convert;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.bean.BeanUtils;
import cn.eyecool.scene.constant.ChannelParamConstants;
import cn.eyecool.scene.domain.BasePersonLiveUpdateInfo;
import cn.eyecool.scene.domain.ChannelBusiness;
import cn.eyecool.scene.domain.ChannelInfo;
import cn.eyecool.scene.domain.ChannelSubtreasuryBusi;
import cn.eyecool.scene.domain.ChannelSubtreasuryInfo;
import cn.eyecool.scene.event.ISubTreasuryEventCallback;
import cn.eyecool.scene.event.SubtreasuryEventPublishlService;
import cn.eyecool.scene.mapper.ChannelBusinessMapper;
import cn.eyecool.scene.mapper.ChannelInfoMapper;
import cn.eyecool.scene.mapper.ChannelSubtreasuryBusiMapper;
import cn.eyecool.scene.mapper.ChannelSubtreasuryInfoMapper;
import cn.eyecool.scene.service.IBusiLiveUpdateSeqService;
import cn.eyecool.scene.trade.entity.PersonBusiOpen;
import cn.eyecool.scene.trade.entity.SubtreasuryBusiOperateParam;
import cn.eyecool.scene.trade.service.IChannelBusiCommonHttpService;
import lombok.extern.slf4j.Slf4j;

/**
 * 场景人员HTTP服务层实现
 * 
 * @author admin
 * @date 2019年11月13日
 */
@Service
@Slf4j
public class ChannelBusiCommonHttpServiceImpl implements IChannelBusiCommonHttpService {

    @Autowired
    private IPersonDataManagerLogicService dataManagerLogicService;
    @Autowired
    private ChannelBusinessMapper channelBusinessMapper;
    @Autowired
    private ChannelInfoMapper channelInfoMapper;
    @Autowired
    private BasePersonInfoMapper basePersonInfoMapper;
    @Autowired
    private IBusiLiveUpdateSeqService busiLiveUpdateSeqService;
    @Autowired
    private ChannelSubtreasuryBusiMapper channelSubtreasuryBusiMapper;
    @Autowired
    private ChannelSubtreasuryInfoMapper channelSubtreasuryInfoMapper;
    @Autowired
    private SubtreasuryEventPublishlService subtreasuryEventPublishlService;

    /**
     * 开通场景人员
     */
    @Override
    @Transactional
    public void openPersonChannelBusi(PersonBusiOpen personBusiOpen, String bioAttestType) {
        // 校验场景是否存在
        String channelCode = personBusiOpen.getChannelCode();
        ChannelInfo channelInfo = checkChannelExists(channelCode);

        // 校验人员信息是否存在
        String uniqueId = personBusiOpen.getUniqueId();
        BasePersonInfo basePersonInfo = checkBasePersonExists(uniqueId);

        // 查询业务信息是否存在
        ChannelBusiness channelBusiness = getChannelBusiness(channelInfo.getId(), basePersonInfo.getId());
        // 待保存的业务信息
        ChannelBusiness saveBusiness = new ChannelBusiness();
        saveBusiness.setDatasource(DictConstants.DataSource.HTTP_INTERFACE);
        if (null == channelBusiness) {
            BeanUtils.copyBeanProp(saveBusiness, personBusiOpen);
            saveBusiness.setId(IdWorker.getNextStringId());
            saveBusiness.setChannelId(channelInfo.getId());
            saveBusiness.setChannelName(channelInfo.getChannelName());
            saveBusiness.setPersonId(basePersonInfo.getId());
            saveBusiness.setPersonName(basePersonInfo.getName());
            saveBusiness.setCreateTime(DateUtils.getNowDate());
        } else {
            BeanUtils.copyBeanProp(saveBusiness, channelBusiness);
            saveBusiness.setStatus(DictConstants.Status.ENABLE);
            saveBusiness.setUpdateTime(DateUtils.getNowDate());
        }
        String bioAttestTypeDesc = null;
        String bioMode = null;
        switch (bioAttestType) {
            case DictConstants.BioAttestType.FACE:
                // 判断场景是否开通人脸
                String faceMode = channelInfo.getFaceMode();
                if (DictConstants.BioModeStatus.DISABLE.equals(faceMode)) {
                    throw new CustomException(MessageUtils.message("channel.common.service.scene.face.not.open", channelCode));
                }
                bioAttestTypeDesc =MessageUtils.message("channel.common.service.scene.verify.face");
                if (null == channelBusiness || DictConstants.Status.DISABLE.equals(channelBusiness.getStatus())) {
                    saveBusiness.setFaceMode(DictConstants.BioModeStatus.ENABLE);
                    saveBusiness.setFingerMode(DictConstants.BioModeStatus.DISABLE);
                    saveBusiness.setIrisMode(DictConstants.BioModeStatus.DISABLE);
                    saveBusiness.setFveinMode(DictConstants.BioModeStatus.DISABLE);
                    saveBusiness.setFaceIrisMode(DictConstants.BioModeStatus.DISABLE);
                } else {
                    bioMode = channelBusiness.getFaceMode();
                    saveBusiness.setFaceMode(DictConstants.BioModeStatus.ENABLE);
                }
                break;
            case DictConstants.BioAttestType.FINGER:
                // 判断场景是否开通指纹
                String fingerMode = channelInfo.getFingerMode();
                if (DictConstants.BioModeStatus.DISABLE.equals(fingerMode)) {
                    throw new CustomException(MessageUtils.message("channel.common.service.scene.finger.not.open", channelCode));
                }
                bioAttestTypeDesc = MessageUtils.message("channel.common.service.scene.verify.finger");
                if (null == channelBusiness || DictConstants.Status.DISABLE.equals(channelBusiness.getStatus())) {
                    saveBusiness.setFaceMode(DictConstants.BioModeStatus.DISABLE);
                    saveBusiness.setFingerMode(DictConstants.BioModeStatus.ENABLE);
                    saveBusiness.setIrisMode(DictConstants.BioModeStatus.DISABLE);
                    saveBusiness.setFveinMode(DictConstants.BioModeStatus.DISABLE);
                    saveBusiness.setFaceIrisMode(DictConstants.BioModeStatus.DISABLE);
                } else {
                    bioMode = channelBusiness.getFingerMode();
                    saveBusiness.setFingerMode(DictConstants.BioModeStatus.ENABLE);
                }
                break;
            case DictConstants.BioAttestType.IRIS:
                // 判断场景是否开通虹膜
                String irisMode = channelInfo.getIrisMode();
                if (DictConstants.BioModeStatus.DISABLE.equals(irisMode)) {
                    throw new CustomException(MessageUtils.message("channel.common.service.scene.iris.not.open", channelCode));
                }
                bioAttestTypeDesc = MessageUtils.message("channel.common.service.scene.verify.iris");
                if (null == channelBusiness || DictConstants.Status.DISABLE.equals(channelBusiness.getStatus())) {
                    saveBusiness.setFaceMode(DictConstants.BioModeStatus.DISABLE);
                    saveBusiness.setFingerMode(DictConstants.BioModeStatus.DISABLE);
                    saveBusiness.setIrisMode(DictConstants.BioModeStatus.ENABLE);
                    saveBusiness.setFveinMode(DictConstants.BioModeStatus.DISABLE);
                    saveBusiness.setFaceIrisMode(DictConstants.BioModeStatus.DISABLE);
                } else {
                    bioMode = channelBusiness.getIrisMode();
                    saveBusiness.setIrisMode(DictConstants.BioModeStatus.ENABLE);
                }
                break;
            case DictConstants.BioAttestType.FVEIN:
                bioAttestTypeDesc = MessageUtils.message("channel.common.service.scene.verify.fvein");
                if (null == channelBusiness || DictConstants.Status.DISABLE.equals(channelBusiness.getStatus())) {
                    saveBusiness.setFaceMode(DictConstants.BioModeStatus.DISABLE);
                    saveBusiness.setFingerMode(DictConstants.BioModeStatus.DISABLE);
                    saveBusiness.setIrisMode(DictConstants.BioModeStatus.DISABLE);
                    saveBusiness.setFveinMode(DictConstants.BioModeStatus.ENABLE);
                    saveBusiness.setFaceIrisMode(DictConstants.BioModeStatus.DISABLE);
                } else {
                    bioMode = channelBusiness.getFveinMode();
                    saveBusiness.setFveinMode(DictConstants.BioModeStatus.ENABLE);
                }
                break;
            case DictConstants.BioAttestType.FACE_IRIS:
                bioAttestTypeDesc = MessageUtils.message("channel.common.service.scene.verify.faceiris");
                if (null == channelBusiness || DictConstants.Status.DISABLE.equals(channelBusiness.getStatus())) {
                    saveBusiness.setFaceMode(DictConstants.BioModeStatus.DISABLE);
                    saveBusiness.setFingerMode(DictConstants.BioModeStatus.DISABLE);
                    saveBusiness.setIrisMode(DictConstants.BioModeStatus.DISABLE);
                    saveBusiness.setFveinMode(DictConstants.BioModeStatus.DISABLE);
                    saveBusiness.setFaceIrisMode(DictConstants.BioModeStatus.ENABLE);
                } else {
                    bioMode = channelBusiness.getFaceIrisMode();
                    saveBusiness.setFaceIrisMode(DictConstants.BioModeStatus.ENABLE);
                }
                break;
        }
        // 更新标识序列号
        Long updateSeriaNum = busiLiveUpdateSeqService.incrementAndGetChannelBusiSeqNum();
        saveBusiness.setUpdateSeriaNum(updateSeriaNum);
        if (null == channelBusiness) {// 如果业务信息不存在，直接插入
            channelBusinessMapper.insertChannelBusiness(saveBusiness);
            // 添加人员到库
            dataManagerLogicService.addLibraryPerson(channelCode, uniqueId);
        } else if (DictConstants.Status.DISABLE.equals(channelBusiness.getStatus())) {// 业务信息存在但状态无效
            saveBusiness.setLocked(DictConstants.YesOrNoState.NO);
            channelBusinessMapper.updateChannelBusiness(saveBusiness);
            // 添加人员到库
            dataManagerLogicService.addLibraryPerson(channelCode, uniqueId);
        } else if (DictConstants.BioModeStatus.ENABLE.equals(bioMode)) {
            // 如果业务信息存在，判断是否已开通
            log.info("channel code [" + channelCode + "],person [" + uniqueId + "] has been opened" + bioAttestTypeDesc );
            return;
        } else if (DictConstants.YesOrNoState.YES.equalsIgnoreCase(channelBusiness.getLocked())) {
            // 如果业务信息存在，判断是否被锁定
            throw new CustomException(MessageUtils.message("channel.common.service.scene.person.locked", channelCode,uniqueId));
        } else {
            // 业务信息存在，没有开通，直接开通
            channelBusinessMapper.updateChannelBusiness(saveBusiness);
        }
    }

    /**
     * 关闭场景人员
     */
    @Override
    @Transactional
    public void closePersonChannelBusi(PersonBusiOpen personBusiOpen, String bioAttestType) {
        // 校验场景是否存在
        String channelCode = personBusiOpen.getChannelCode();
        ChannelInfo channelInfo = checkChannelExists(channelCode);

        // 校验人员信息是否存在
        String uniqueId = personBusiOpen.getUniqueId();
        BasePersonInfo basePersonInfo = checkBasePersonExists(uniqueId);

        // 查询业务信息是否存在
        ChannelBusiness channelBusiness = getChannelBusiness(channelInfo.getId(), basePersonInfo.getId());
        // 待保存的业务信息
        ChannelBusiness saveBusiness = new ChannelBusiness();
        if (null != channelBusiness) {
            saveBusiness.setId(channelBusiness.getId());
            saveBusiness.setDatasource(DictConstants.DataSource.HTTP_INTERFACE);
            saveBusiness.setUpdateTime(DateUtils.getNowDate());
        }
        String bioAttestTypeDesc = null;
        String bioMode = null;
        switch (bioAttestType) {
            case DictConstants.BioAttestType.FACE:
                bioAttestTypeDesc = MessageUtils.message("channel.common.service.scene.verify.face");
                if (null != channelBusiness) {
                    bioMode = channelBusiness.getFaceMode();
                    saveBusiness.setFaceMode(DictConstants.BioModeStatus.DISABLE);
                }
                break;
            case DictConstants.BioAttestType.FINGER:
                bioAttestTypeDesc = MessageUtils.message("channel.common.service.scene.verify.finger");
                if (null != channelBusiness) {
                    bioMode = channelBusiness.getFingerMode();
                    saveBusiness.setFingerMode(DictConstants.BioModeStatus.DISABLE);
                }
                break;
            case DictConstants.BioAttestType.IRIS:
                bioAttestTypeDesc = MessageUtils.message("channel.common.service.scene.verify.iris");
                if (null != channelBusiness) {
                    bioMode = channelBusiness.getIrisMode();
                    saveBusiness.setIrisMode(DictConstants.BioModeStatus.DISABLE);
                }
                break;
            case DictConstants.BioAttestType.FVEIN:
                bioAttestTypeDesc = MessageUtils.message("channel.common.service.scene.verify.fvein");
                if (null != channelBusiness) {
                    bioMode = channelBusiness.getFveinMode();
                    saveBusiness.setFveinMode(DictConstants.BioModeStatus.DISABLE);
                }
                break;
            case DictConstants.BioAttestType.FACE_IRIS:
                bioAttestTypeDesc =  MessageUtils.message("channel.common.service.scene.verify.faceiris");
                if (null != channelBusiness) {
                    bioMode = channelBusiness.getFaceIrisMode();
                    saveBusiness.setFaceIrisMode(DictConstants.BioModeStatus.DISABLE);
                }
                break;
        }

        if (null == channelBusiness || DictConstants.BioModeStatus.DISABLE.equals(bioMode)) {
            // 如果业务信息不存在或者未开通
            log.info("channel code [" + channelCode + "],person [" + uniqueId + "] has not been opened " + bioAttestTypeDesc );
            return;
        } else if (DictConstants.YesOrNoState.YES.equalsIgnoreCase(channelBusiness.getLocked())) {
            // 如果业务信息存在，判断是否被锁定
            throw new CustomException(MessageUtils.message("channel.common.service.scene.person.locked", channelCode,uniqueId));
        } else {
            // 更新标识序列号
            Long updateSeriaNum = busiLiveUpdateSeqService.incrementAndGetChannelBusiSeqNum();
            saveBusiness.setUpdateSeriaNum(updateSeriaNum);
            // 业务信息存在，已开通，直接关闭
            channelBusinessMapper.updateChannelBusiness(saveBusiness);
        }
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
            throw new CustomException(MessageUtils.message("channel.common.service.channelcode.not.exists",  channelCode ));
        }
        ChannelInfo channelInfo = infoList.get(0);
        return channelInfo;
    }

    /**
     * 校验人员信息是否存在
     * 
     * @param uniqueId
     * @return
     */
    private BasePersonInfo checkBasePersonExists(String uniqueId) {
        BasePersonInfo info = getBasePersonInfo(uniqueId);
        if (null == info) {
            throw new CustomException(MessageUtils.message("channel.common.service.person.not.exists", uniqueId));
        }
        return info;
    }

    /**
     * 查询人员基础信息
     * 
     * @param uniqueId
     * @return
     */
    private BasePersonInfo getBasePersonInfo(String uniqueId) {
        BasePersonInfo personCondition = new BasePersonInfo();
        personCondition.setUniqueId(uniqueId);
        personCondition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonInfo> personList = basePersonInfoMapper.selectBasePersonInfoList(personCondition);
        return CollectionUtils.isEmpty(personList) ? null : personList.get(0);
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
        List<ChannelBusiness> businessList = channelBusinessMapper.selectChannelBusinessList(busiCondition);
        return CollectionUtils.isEmpty(businessList) ? null : businessList.get(0);
    }

    /**
     * 操作子场景
     */
    @Override
    public Map<String, Object> operatePersonSubtreasury(SubtreasuryBusiOperateParam channelSubtreasuryBusiSaveParam) {
        Map<String, Object> result = Maps.newHashMap();
        int succNum = 0;
        int failNum = 0;
        List<String> failList = Lists.newArrayList();
        // 校验场景是否存在
        String channelCode = channelSubtreasuryBusiSaveParam.getChannelCode();
        ChannelInfo channelInfo = checkChannelExists(channelCode);
        String channelId = channelInfo.getId();
        // 子场景编码
        String subTreasuryCode = channelSubtreasuryBusiSaveParam.getSubTreasuryCode();
        // 查询子场景是否存在
        ChannelSubtreasuryInfo subCondition = new ChannelSubtreasuryInfo();
        subCondition.setChannelId(channelId);
        subCondition.setSubTreasuryCode(subTreasuryCode);
        List<ChannelSubtreasuryInfo> subtreasuryInfoList =
            channelSubtreasuryInfoMapper.selectChannelSubtreasuryInfoList(subCondition);
        if (CollectionUtils.isEmpty(subtreasuryInfoList)) {
            throw new CustomException(MessageUtils.message("channel.common.service.subscene.not.exists", subTreasuryCode));
        }
        String subTreasuryId = subtreasuryInfoList.get(0).getId();
        // 操作类型
        String operateType = channelSubtreasuryBusiSaveParam.getOperateType();
        // 人员唯一标识列表，多个使用逗号分隔
        String uniqueIds = channelSubtreasuryBusiSaveParam.getUniqueIds();
        String[] uniqueIdArray = null;
        if (StringUtils.isNotBlank(uniqueIds)) {
            uniqueIdArray = Convert.toStrArray(uniqueIds);
        }
        ChannelBusiCommonHttpServiceImpl proxyObj = (ChannelBusiCommonHttpServiceImpl)AopContext.currentProxy();
        // 1、清空子场景人员
        if (ChannelParamConstants.ChannelSubtreasuryOperateType.OPERATE_TYPE_CLEAR.equals(operateType)) {
            proxyObj.clearSubTreasury(subTreasuryId, subTreasuryCode);
            return null;
        }
        // 2、删除子场景人员
        if (ChannelParamConstants.ChannelSubtreasuryOperateType.OPERATE_TYPE_DELETE.equals(operateType)) {
            // 人员标识不能为空
            if (null == uniqueIdArray || uniqueIdArray.length < 1) {
                throw new CustomException(MessageUtils.message("channel.common.service.delete.uniqueIds.empty"));
            }
            // 循环删除人员
            for (String uniqueId : uniqueIdArray) {
                // 查询人员是否存在
                BasePersonInfo basePersonInfo = getBasePersonInfo(uniqueId);
                if (null == basePersonInfo) {// 人员基础信息不存在
                    failNum++;
                    failList.add("uniqueId:" + uniqueId + ",reason:Personnel basic information does not exist!");
                    continue;
                }
                // 查询人员在子场景中是否存在
                ChannelSubtreasuryBusi subtreasuryPersonInfo = getSubtreasuryPersonInfo(channelId, subTreasuryId,
                    basePersonInfo.getId(), DictConstants.Status.ENABLE);
                if (null == subtreasuryPersonInfo) {// 人员在子场景中不存在, 直接跳过，认为删除成功
                    succNum++;
                    continue;
                }
                try {
                    // 删除人员
                    proxyObj.logicDelSubTreasuryBusi(subtreasuryPersonInfo);
                    succNum++;
                } catch (Exception e) {
                    failNum++;
                    failList.add("uniqueId:" + uniqueId + ",reason:" + e.getMessage());
                }
            }
            result.put("succNum", succNum);
            result.put("failNum", failNum);
            result.put("failList", failList);
            return result;
        }
        // 3、添加子场景人员
        String isAppend = channelSubtreasuryBusiSaveParam.getIsAppend(); // 判断是否追加
        if (DictConstants.YesOrNoState.NO.equals(isAppend)) {// 不追加, 直接清空库中人员，重新添加
            proxyObj.clearSubTreasury(subTreasuryId, subTreasuryCode);
        }
        // 循环添加人员
        for (String uniqueId : uniqueIdArray) {
            // 查询人员是否存在
            BasePersonInfo basePersonInfo = getBasePersonInfo(uniqueId);
            if (null == basePersonInfo) {// 人员基础信息不存在
                failNum++;
                failList.add("uniqueId:" + uniqueId + ",reason:Personnel basic information does not exist!");
                continue;
            }
            // 查询人员在场景库是否存在
            ChannelBusiness busiCondition = new ChannelBusiness();
            busiCondition.setChannelId(channelId);
            busiCondition.setPersonId(basePersonInfo.getId());
            busiCondition.setStatus(DictConstants.Status.ENABLE);
            List<ChannelBusiness> businessList = channelBusinessMapper.selectChannelBusinessList(busiCondition);
            if (CollectionUtils.isEmpty(businessList)) {
                failNum++;
                failList.add("uniqueId:" + uniqueId + ",reason:Person does not exist in scene library!");
                continue;
            }
            // 查询人员在子场景中是否存在
            ChannelSubtreasuryBusi subtreasuryPersonInfo =
                getSubtreasuryPersonInfo(channelId, subTreasuryId, basePersonInfo.getId(), null);
            try {
                if (null == subtreasuryPersonInfo) {// 人员在子场景中不存在, 直接添加
                    ChannelSubtreasuryBusi subtreasuryBusi = new ChannelSubtreasuryBusi();
                    subtreasuryBusi.setChannelId(channelId);
                    subtreasuryBusi.setSubTreasuryId(subTreasuryId);
                    subtreasuryBusi.setSubTreasuryCode(subTreasuryCode);
                    subtreasuryBusi.setDatasource(DictConstants.DataSource.HTTP_INTERFACE);
                    subtreasuryBusi.setCreateTime(DateUtils.getNowDate());
                    subtreasuryBusi.setPersonId(basePersonInfo.getId());
                    subtreasuryBusi.setStatus(DictConstants.Status.ENABLE);
                    subtreasuryBusi.setUniqueId(uniqueId);
                    subtreasuryBusi.setId(IdWorker.getNextStringId());
                    proxyObj.insertSubTreasuryBusi(subtreasuryBusi);
                    succNum++;
                } else if (DictConstants.Status.DISABLE.equals(subtreasuryPersonInfo.getStatus())) {
                    subtreasuryPersonInfo.setStatus(DictConstants.Status.ENABLE);
                    subtreasuryPersonInfo.setUpdateTime(DateUtils.getNowDate());
                    proxyObj.enableSubTreasuryBusi(subtreasuryPersonInfo);
                    succNum++;
                } else {// 存在，直接计数成功
                    succNum++;
                }
            } catch (Exception e) {
                failNum++;
                failList.add("uniqueId:" + uniqueId + ",reason:" + e.getMessage());
            }
        }
        result.put("succNum", succNum);
        result.put("failNum", failNum);
        result.put("failList", failList);
        return result;
    }

    /**
     * 清空子场景人员
     * 
     * @param subTreasuryId
     * @param subTreasuryCode
     */
    @Transactional
    public void clearSubTreasury(String subTreasuryId, String subTreasuryCode) {
        channelSubtreasuryBusiMapper.deleteChannelSubtreasuryBusiBySubIds(new String[] {subTreasuryId});
        dataManagerLogicService.deleteLibrary(subTreasuryCode);
        // 通过Spring的事件收发机制发布时间，通知设备模块更新全量拉取标志
        subtreasuryEventPublishlService.clearSubDataPublish(Lists.newArrayList(subTreasuryCode),
            new ISubTreasuryEventCallback() {
                @Override
                public void onError(String errmsg) {
                    log.error("Delete sub-scene event publishing processing failed:[]", errmsg);
                    throw new CustomException(MessageUtils.message("channel.common.service.delete.subscene.failed", errmsg));
                }
            });
    }

    /**
     * 新增子场景人员，加事务处理，保证datamanager和关系库数据一致性
     * 
     * @param subtreasuryBusi
     */
    @Transactional
    public void insertSubTreasuryBusi(ChannelSubtreasuryBusi subtreasuryBusi) {
        // 子场景人员更新标识序列号
        Long subSeqNum = busiLiveUpdateSeqService.incrementAndGetSubtreasuryBusiSeqNum();
        subtreasuryBusi.setUpdateSeriaNum(subSeqNum);
        channelSubtreasuryBusiMapper.insertChannelSubtreasuryBusi(subtreasuryBusi);
        dataManagerLogicService.addLibraryPerson(subtreasuryBusi.getSubTreasuryCode(), subtreasuryBusi.getUniqueId());
    }

    /**
     * 修改子场景人员(DISABLE->ENABLE)并添加到datamanager，加事务处理，保证datamanager和关系库数据一致性
     * 
     * @param subtreasuryBusi
     */
    @Transactional
    public void enableSubTreasuryBusi(ChannelSubtreasuryBusi subtreasuryBusi) {
        // 子场景人员更新标识序列号
        Long subSeqNum = busiLiveUpdateSeqService.incrementAndGetSubtreasuryBusiSeqNum();
        subtreasuryBusi.setUpdateSeriaNum(subSeqNum);
        channelSubtreasuryBusiMapper.updateChannelSubtreasuryBusi(subtreasuryBusi);
        dataManagerLogicService.addLibraryPerson(subtreasuryBusi.getSubTreasuryCode(), subtreasuryBusi.getUniqueId());
    }

    /**
     * 删除子场景人员,加事务处理，保证datamanager和关系库数据一致性
     * 
     * @param subtreasuryBusi
     */
    @Transactional
    public void logicDelSubTreasuryBusi(ChannelSubtreasuryBusi subtreasuryBusi) {
        subtreasuryBusi.setStatus(DictConstants.Status.DISABLE);
        subtreasuryBusi.setUpdateTime(DateUtils.getNowDate());
        // 子场景人员更新标识序列号
        Long subSeqNum = busiLiveUpdateSeqService.incrementAndGetSubtreasuryBusiSeqNum();
        subtreasuryBusi.setUpdateSeriaNum(subSeqNum);
        channelSubtreasuryBusiMapper.updateChannelSubtreasuryBusi(subtreasuryBusi);
        dataManagerLogicService.deleteLibraryPerson(subtreasuryBusi.getSubTreasuryCode(),
            subtreasuryBusi.getUniqueId());
    }

    /**
     * 查询子场景人员信息
     * 
     * @param channelId
     * @param subTreasuryId
     * @param personId
     * @param status
     * @return
     */
    private ChannelSubtreasuryBusi getSubtreasuryPersonInfo(String channelId, String subTreasuryId, String personId,
        String status) {
        ChannelSubtreasuryBusi subtreasuryBusiCondition = new ChannelSubtreasuryBusi();
        subtreasuryBusiCondition.setStatus(status);
        subtreasuryBusiCondition.setChannelId(channelId);
        subtreasuryBusiCondition.setSubTreasuryId(subTreasuryId);
        subtreasuryBusiCondition.setPersonId(personId);
        List<ChannelSubtreasuryBusi> subtreasuryBusiList =
            channelSubtreasuryBusiMapper.selectChannelSubtreasuryBusiList(subtreasuryBusiCondition);
        return CollectionUtils.isEmpty(subtreasuryBusiList) ? null : subtreasuryBusiList.get(0);
    }

    /**
     * 查询场景库人员是否存在
     */
    @Override
    public boolean queryChannelPersonExists(String channelCode, String uniqueId) {
        try {
            // 校验场景是否存在
            ChannelInfo channelInfo = checkChannelExists(channelCode);
            if (null == channelInfo) {
                return false;
            }
            String channelId = channelInfo.getId();
            ChannelBusiness busi = new ChannelBusiness();
            busi.setChannelId(channelId);
            busi.setUniqueId(uniqueId);
            busi.setStatus(DictConstants.Status.ENABLE);
            List<ChannelBusiness> list = channelBusinessMapper.selectChannelBusinessList(busi);
            return CollectionUtils.isNotEmpty(list);
        } catch (CustomException e) {
            return false;
        }
    }

    /**
     * 删除场景库人员
     */
    @Override
    public void deleteChannelPerson(String channelCode, String uniqueId) {
        // 校验场景是否存在
        ChannelInfo channelInfo = checkChannelExists(channelCode);
        // 校验人员信息是否存在
        BasePersonInfo basePersonInfo = checkBasePersonExists(uniqueId);
        // 查询业务信息是否存在
        ChannelBusiness channelBusiness = getChannelBusiness(channelInfo.getId(), basePersonInfo.getId());
        if (null == channelBusiness || DictConstants.Status.DISABLE.equals(channelBusiness.getStatus())) {
            log.info("The scene library staff does not exist, no need to delete!");
            return;
        }
        channelBusiness.setStatus(DictConstants.Status.DISABLE);
        // 更新标识序列号
        Long updateSeriaNum = busiLiveUpdateSeqService.incrementAndGetChannelBusiSeqNum();
        channelBusiness.setUpdateSeriaNum(updateSeriaNum);
        channelBusinessMapper.updateChannelBusiness(channelBusiness);
        dataManagerLogicService.deleteLibraryPerson(channelBusiness.getChannelCode(), channelBusiness.getUniqueId());
        // 逻辑删除子场景信息
        ChannelSubtreasuryBusi subtreasuryBusi = new ChannelSubtreasuryBusi();
        subtreasuryBusi.setChannelId(channelBusiness.getChannelId());
        subtreasuryBusi.setPersonId(channelBusiness.getPersonId());
        List<ChannelSubtreasuryBusi> subtreasuryBusiList =
            channelSubtreasuryBusiMapper.selectChannelSubtreasuryBusiList(subtreasuryBusi);
        if (CollectionUtils.isNotEmpty(subtreasuryBusiList)) {
            subtreasuryBusiList.stream().forEach(it -> {
                it.setStatus(DictConstants.Status.DISABLE);
                it.setUpdateTime(DateUtils.getNowDate());
                // 子场景人员更新标识序列号
                Long subSeqNum = busiLiveUpdateSeqService.incrementAndGetSubtreasuryBusiSeqNum();
                it.setUpdateSeriaNum(subSeqNum);
                channelSubtreasuryBusiMapper.updateChannelSubtreasuryBusi(it);
                dataManagerLogicService.deleteLibraryPerson(it.getSubTreasuryCode(), it.getUniqueId());
            });
        }
    }

    /**
     * 获取实时更新人员信息列表
     */
    @Override
    public List<BasePersonLiveUpdateInfo> selectLiveUpdateBasePersonInfoList(Map<String, Object> map) {
        return channelBusinessMapper.selectLiveUpdateBasePersonInfoList(map);
    }

}
