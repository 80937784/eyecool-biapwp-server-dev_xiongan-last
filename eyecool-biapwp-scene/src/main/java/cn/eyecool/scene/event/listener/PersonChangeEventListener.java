package cn.eyecool.scene.event.listener;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.beust.jcommander.internal.Lists;

import cn.eyecool.basedata.event.PersonChangeEventType;
import cn.eyecool.basedata.event.PersonInfoChangeEvent;
import cn.eyecool.basedata.manager.IPersonDataManagerLogicService;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.text.Convert;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.scene.domain.ChannelBusiness;
import cn.eyecool.scene.domain.ChannelInfo;
import cn.eyecool.scene.domain.ChannelSubtreasuryBusi;
import cn.eyecool.scene.domain.ChannelSubtreasuryInfo;
import cn.eyecool.scene.mapper.ChannelBusinessMapper;
import cn.eyecool.scene.mapper.ChannelInfoMapper;
import cn.eyecool.scene.mapper.ChannelSubtreasuryBusiMapper;
import cn.eyecool.scene.mapper.ChannelSubtreasuryInfoMapper;
import cn.eyecool.scene.service.IBusiLiveUpdateSeqService;
import cn.eyecool.system.service.ISysConfigService;
import lombok.extern.slf4j.Slf4j;

/**
 * 人员基础信息改变事件监听类
 * 
 * @author mawj
 * @date 2021/01/06
 */
@Component
@Slf4j
public class PersonChangeEventListener {

    @Autowired
    private ISysConfigService configService;
    @Autowired
    private ChannelInfoMapper channelInfoMapper;
    @Autowired
    private ChannelBusinessMapper channelBusinessMapper;
    @Autowired
    private ChannelSubtreasuryBusiMapper channelSubtreasuryBusiMapper;
    @Autowired
    private ChannelSubtreasuryInfoMapper channelSubtreasuryInfoMapper;
    @Autowired
    private IPersonDataManagerLogicService dataManagerLogicService;
    @Autowired
    private IBusiLiveUpdateSeqService busiLiveUpdateSeqService;

    /**
     * 人员基础信息改变事件处理
     * 
     * @param event
     */
    @Async
    @TransactionalEventListener(fallbackExecution = true)
    public void processPersonChangeEvent(PersonInfoChangeEvent event) {
        log.info(
            "Personnel basic information change event monitoring and processing,tenantId:[{}],personId:[{}],uniqueId:[{}], eventType:[{}]",
            TenantContextHolder.getTenantId(), event.getPersonId(), event.getUniqueId(),
            event.getPersonChangeEventType());
        try {
            PersonChangeEventType eventType = event.getPersonChangeEventType();
            if (null == eventType) {
                return;
            }
            switch (eventType) {
                case PERSON_ADD:
                    // 新增人员自动绑定人库关系(人和场景)
                    bindPersonChannelRel(event);
                    // 自动绑定人库关系(人和子场景)
                    bindPersonSubtreasuryRel(event);
                    break;
                case PERSON_DELETE:
                    // 删除人员自动解绑人库关系（人和场景）
                    unbindPersonChannelRel(event);
                    // 删除人员自动解绑人库关系（人和子场景）
                    unbindPersonSubtreasuryRel(event);
                    break;
                case PERSON_BIO_UPDATE:// 修改人员生物信息
                    break;
                case PERSON_CERT_UPDATE:// 修改人证件信息
                    break;
                case PERSON_UPDATE:// 修改人员属性和生物信息（会修改人员场景绑定关系）
                case PERSON_ATTR_UPDATE:// 修改人员基础属性（会修改人员场景绑定关系）
                    // 删除人员自动解绑人库关系（人和场景）
                    unbindPersonChannelRel(event);
                    // 删除人员自动解绑人库关系（人和子场景）
                    unbindPersonSubtreasuryRel(event);
                    // 新增人员自动绑定人库关系(人和场景)
                    bindPersonChannelRel(event);
                    // 自动绑定人库关系(人和子场景)
                    bindPersonSubtreasuryRel(event);
                    break;
                case PERSON_REGISTER:// 设备进行人员注册
                    // 自动绑定人库关系(人和场景)
                    bindPersonChannelRel(event);
                    // 自动绑定人库关系(人和子场景)
                    bindPersonSubtreasuryRel(event);
                    break;
                case DEVICE_PERSON_DELETE:// 设备进行人员删除
                    // 设备删除人员自动解绑人库关系（人和子场景）
                    unbindPersonSubtreasuryRel(event);
                    break;
                default:
                    break;
            }
            // 修改场景人员更新标识序列和子场景人员更新标识序列，删除操作的时候已经单独更新，此处不再需要再次更新
            if (!PersonChangeEventType.PERSON_DELETE.equals(eventType)
                && !PersonChangeEventType.DEVICE_PERSON_DELETE.equals(eventType)) {
                updateBusiUpdateSeq(event);
                updateSbusiUpdateSeq(event);
            }
            event.getCallback().onSuccess();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            event.getCallback().onError(e.getMessage());
        }
    }

    /**
     * 自动绑定人和场景关系
     * 
     * @param event
     */
    private void bindPersonChannelRel(PersonInfoChangeEvent event) {
        String personId = event.getPersonId();
        String uniqueId = event.getUniqueId();
        if (StringUtils.isBlank(uniqueId) || StringUtils.isBlank(personId)) {
            return;
        }
        String autoBindChannelCodes = event.getAutoBindChannelCodes();
        List<String> channelCodeList = StringUtils.isNotEmpty(autoBindChannelCodes)
            ? Lists.newArrayList(Convert.toStrArray(autoBindChannelCodes)) : Lists.newArrayList();
        if (getIsAutoBindRelation()) {// 是否自动绑定配置场景的人库关系
            List<String> configAutoBindChannelList = getAutoBindRelChannel();
            if (CollectionUtils.isNotEmpty(configAutoBindChannelList)) {
                channelCodeList.addAll(configAutoBindChannelList);
            }
        }
        if (CollectionUtils.isEmpty(channelCodeList)) {
            if (log.isDebugEnabled()) {
                log.debug(
                    "There is no scene that needs to automatically bind the relationship between people and databases");
            }
            return;
        }
        channelCodeList.forEach(channelCode -> {
            ChannelInfo condition = new ChannelInfo();
            condition.setChannelCode(channelCode);
            List<ChannelInfo> channelInfoList = channelInfoMapper.selectChannelInfoList(condition);
            if (CollectionUtils.isEmpty(channelInfoList)) {
                log.warn(
                    "There is no scene that needs to automatically bind the relationship between people and databases,channleCode:[{}]",
                    channelCode);
                return;
            }
            ChannelInfo channel = channelInfoList.get(0);
            // 查询人库关系是否存在
            ChannelBusiness busiCondition = new ChannelBusiness();
            busiCondition.setChannelId(channel.getId());
            busiCondition.setPersonId(personId);
            List<ChannelBusiness> businessList = channelBusinessMapper.selectChannelBusinessList(busiCondition);
            boolean exists = CollectionUtils.isNotEmpty(businessList);
            boolean isEnabled = exists ? DictConstants.Status.ENABLE.equals(businessList.get(0).getStatus()) : false;
            if (isEnabled) {
                if (log.isDebugEnabled()) {
                    log.debug("Person is bound to person database relationship,channelCode:[{}], uniqueId:[{]]",
                        channelCode, uniqueId);
                }
                return;
            }
            // 人库关系自动绑定
            ChannelBusiness rel = exists ? businessList.get(0) : new ChannelBusiness();
            rel.setUniqueId(uniqueId);
            rel.setStatus(DictConstants.Status.ENABLE);
            rel.setPersonId(personId);
            rel.setIrisMode(channel.getIrisMode());
            rel.setFveinMode(channel.getFveinMode());
            rel.setFingerMode(channel.getFingerMode());
            rel.setFaceMode(channel.getFaceMode());
            rel.setFaceIrisMode(channel.getFaceIrisMode());
            rel.setCreateTime(DateUtils.getNowDate());
            rel.setChannelId(channel.getId());
            rel.setDatasource(event.getDataSource());
            if (!exists) {
                rel.setId(IdWorker.getNextStringId());
                channelBusinessMapper.insertChannelBusiness(rel);
            } else {
                rel.setUpdateTime(DateUtils.getNowDate());
                channelBusinessMapper.updateChannelBusiness(rel);
            }
            dataManagerLogicService.addLibraryPerson(channelCode, uniqueId);
        });
    }

    /**
     * 自动绑定人和子场景关系
     * 
     * @param event
     */
    private void bindPersonSubtreasuryRel(PersonInfoChangeEvent event) {
        String personId = event.getPersonId();
        String uniqueId = event.getUniqueId();
        if (StringUtils.isBlank(uniqueId) || StringUtils.isBlank(personId)) {
            return;
        }
        String autoBindSubCodes = event.getAutoBindSubCodes();
        List<String> subCodeList = StringUtils.isNotEmpty(autoBindSubCodes)
            ? Lists.newArrayList(Convert.toStrArray(autoBindSubCodes)) : Lists.newArrayList();
        if (CollectionUtils.isEmpty(subCodeList)) {
            if (log.isDebugEnabled()) {
                log.debug(
                    "There are sub-scenarios that need to automatically bind the relationship between people and databases");
            }
            return;
        }
        subCodeList.forEach(subCode -> {
            ChannelSubtreasuryInfo condition = new ChannelSubtreasuryInfo();
            condition.setSubTreasuryCode(subCode);
            List<ChannelSubtreasuryInfo> subtreasuryInfoList =
                channelSubtreasuryInfoMapper.selectChannelSubtreasuryInfoList(condition);
            if (CollectionUtils.isEmpty(subtreasuryInfoList)) {
                log.warn(
                    "The sub-scenario that needs to automatically bind the relationship between the people library does not exist,subCode:[{}]",
                    subCode);
                return;
            }
            ChannelSubtreasuryInfo subtreasuryInfo = subtreasuryInfoList.get(0);
            String subId = subtreasuryInfo.getId();
            // 查询人库关系是否存在
            ChannelSubtreasuryBusi busiCondition = new ChannelSubtreasuryBusi();
            busiCondition.setPersonId(personId);
            busiCondition.setSubTreasuryId(subId);
            List<ChannelSubtreasuryBusi> businessList =
                channelSubtreasuryBusiMapper.selectChannelSubtreasuryBusiList(busiCondition);
            boolean exists = CollectionUtils.isNotEmpty(businessList);
            boolean isEnabled = exists ? DictConstants.Status.ENABLE.equals(businessList.get(0).getStatus()) : false;
            if (isEnabled) {
                log.debug(
                    "People are bound to people and sub-scene relationships,subId:[{}],subCode:[{}], uniqueId:[{]]",
                    subId, subCode, uniqueId);
                return;
            }
            // 人库关系自动绑定
            ChannelSubtreasuryBusi rel = exists ? businessList.get(0) : new ChannelSubtreasuryBusi();
            rel.setUniqueId(uniqueId);
            rel.setStatus(DictConstants.Status.ENABLE);
            rel.setPersonId(personId);
            rel.setCreateTime(DateUtils.getNowDate());
            rel.setSubTreasuryId(subId);
            rel.setChannelId(subtreasuryInfo.getChannelId());
            rel.setDatasource(event.getDataSource());
            if (!exists) {
                rel.setId(IdWorker.getNextStringId());
                channelSubtreasuryBusiMapper.insertChannelSubtreasuryBusi(rel);
            } else {
                rel.setUpdateTime(DateUtils.getNowDate());
                channelSubtreasuryBusiMapper.updateChannelSubtreasuryBusi(rel);
            }
            dataManagerLogicService.addLibraryPerson(subtreasuryInfo.getSubTreasuryCode(), uniqueId);
        });
    }

    /**
     * 查询是否需要自动绑定和接触绑定关系
     * 
     * @return
     */
    private boolean getIsAutoBindRelation() {
        String configByKey = configService.selectConfigByKey(SysConfigConstants.PLATFORM_AUTO_BIND_LIB_PERSON_REL_KEY);
        return DictConstants.YesOrNoState.YES.equals(configByKey);
    }

    /**
     * 查询需要自动绑定人库关系的场景编码
     * 
     * @return
     */
    private List<String> getAutoBindRelChannel() {
        String configByKey =
            configService.selectConfigByKey(SysConfigConstants.AUTO_BIND_LIB_PERSON_REL_CHANNEL_CODE_KEY);
        return StringUtils.isBlank(configByKey) ? null : Arrays.asList(configByKey.split(";"));
    }

    /**
     * 更新场景人员更新序列号
     * 
     * @param event
     */
    private void updateBusiUpdateSeq(PersonInfoChangeEvent event) {
        String personId = event.getPersonId();
        if (StringUtils.isBlank(personId)) {
            return;
        }
        ChannelBusiness busiCondition = new ChannelBusiness();
        busiCondition.setPersonId(personId);
        List<ChannelBusiness> businessList = channelBusinessMapper.selectChannelBusinessList(busiCondition);
        if (CollectionUtils.isEmpty(businessList)) {
            return;
        }
        List<String> channelCodeList = getBioChangeLiveUpdateChannel();
        boolean bioChanged = event.isFaceChanged() || event.isFingerChanged() || event.isIrisChanged()
            || event.isFveinChanged() || event.isIrisfaceChanged();
        businessList.stream().forEach(busi -> {
            String channelCode = busi.getChannelCode();
            // 生物特征变化触发更新
            if (channelCodeList.contains(channelCode) && !bioChanged) {
                return;
            }
            Long busiSeqNum = busiLiveUpdateSeqService.incrementAndGetChannelBusiSeqNum();
            busi.setUpdateSeriaNum(busiSeqNum);
            busi.setUpdateTime(DateUtils.getNowDate());
            channelBusinessMapper.updateChannelBusiness(busi);
        });
    }

    /**
     * 更新子场景人员更新序列号
     * 
     * @param event
     */
    private void updateSbusiUpdateSeq(PersonInfoChangeEvent event) {
        String personId = event.getPersonId();
        if (StringUtils.isBlank(personId)) {
            return;
        }
        ChannelSubtreasuryBusi sbusiCondition = new ChannelSubtreasuryBusi();
        sbusiCondition.setPersonId(personId);
        List<ChannelSubtreasuryBusi> sbusiList =
            channelSubtreasuryBusiMapper.selectChannelSubtreasuryBusiList(sbusiCondition);
        if (CollectionUtils.isEmpty(sbusiList)) {
            return;
        }
        sbusiList.stream().forEach(sbusi -> {
            Long sbusiSeqNum = busiLiveUpdateSeqService.incrementAndGetSubtreasuryBusiSeqNum();
            sbusi.setUpdateSeriaNum(sbusiSeqNum);
            sbusi.setUpdateTime(DateUtils.getNowDate());
            channelSubtreasuryBusiMapper.updateChannelSubtreasuryBusi(sbusi);
        });
    }

    /**
     * 查询生物特征变化触发实时更新的场景编码
     * 
     * @return
     */
    private List<String> getBioChangeLiveUpdateChannel() {
        String configByKey =
            configService.selectConfigByKey(SysConfigConstants.BIO_CHANGE_LIVE_UPDATE_CHANNEL_CODE_KEY);
        return StringUtils.isBlank(configByKey) ? Collections.emptyList() : Arrays.asList(configByKey.split(";"));
    }

    /**
     * 解除绑定人和场景关系
     * 
     * @param event
     */
    private void unbindPersonChannelRel(PersonInfoChangeEvent event) {
        String personId = event.getPersonId();
        String uniqueId = event.getUniqueId();
        if (StringUtils.isBlank(uniqueId) || StringUtils.isBlank(personId)) {
            return;
        }
        ChannelBusiness busiCondition = new ChannelBusiness();
        busiCondition.setPersonId(personId);
        busiCondition.setStatus(DictConstants.Status.ENABLE);
        List<ChannelBusiness> businessList = channelBusinessMapper.selectChannelBusinessList(busiCondition);
        if (CollectionUtils.isEmpty(businessList)) {
            return;
        }
        businessList.forEach(busi -> {
            Long busiSeqNum = busiLiveUpdateSeqService.incrementAndGetChannelBusiSeqNum();
            busi.setUpdateSeriaNum(busiSeqNum);
            busi.setStatus(DictConstants.Status.DISABLE);
            busi.setUpdateTime(DateUtils.getNowDate());
            channelBusinessMapper.updateChannelBusiness(busi);
            dataManagerLogicService.deleteLibraryPerson(busi.getChannelCode(), busi.getUniqueId());
            log.info(
                "The person is deleted and the person database relationship is automatically unbound, tenantId:[{}], channelCode:[{}], uniqueId:[{}]",
                TenantContextHolder.getTenantId(), busi.getChannelCode(), busi.getUniqueId());
        });
    }

    /**
     * 解除绑定人和子场景关系
     * 
     * @param event
     */
    private void unbindPersonSubtreasuryRel(PersonInfoChangeEvent event) {
        String personId = event.getPersonId();
        String uniqueId = event.getUniqueId();
        if (StringUtils.isBlank(uniqueId) || StringUtils.isBlank(personId)) {
            return;
        }
        ChannelSubtreasuryBusi sbusiCondition = new ChannelSubtreasuryBusi();
        sbusiCondition.setPersonId(personId);
        sbusiCondition.setStatus(DictConstants.Status.ENABLE);
        List<ChannelSubtreasuryBusi> sbusiList =
            channelSubtreasuryBusiMapper.selectChannelSubtreasuryBusiList(sbusiCondition);
        if (CollectionUtils.isEmpty(sbusiList)) {
            return;
        }
        sbusiList.forEach(sbusi -> {
            Long sbusiSeqNum = busiLiveUpdateSeqService.incrementAndGetSubtreasuryBusiSeqNum();
            sbusi.setUpdateSeriaNum(sbusiSeqNum);
            sbusi.setUpdateTime(DateUtils.getNowDate());
            sbusi.setStatus(DictConstants.Status.DISABLE);
            channelSubtreasuryBusiMapper.updateChannelSubtreasuryBusi(sbusi);
            dataManagerLogicService.deleteLibraryPerson(sbusi.getSubTreasuryCode(), sbusi.getUniqueId());
            log.info(
                "The person is deleted and the person database relationship is automatically unbound, tenantId:[{}], channelCode:[{}], subTreasuryCode:[{}], uniqueId:[{}]",
                TenantContextHolder.getTenantId(), sbusi.getChannelCode(), sbusi.getSubTreasuryCode(),
                sbusi.getUniqueId());
        });
    }
}
