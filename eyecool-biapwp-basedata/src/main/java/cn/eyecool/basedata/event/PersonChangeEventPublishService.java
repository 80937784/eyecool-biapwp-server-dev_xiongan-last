package cn.eyecool.basedata.event;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;

import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.basedata.mapper.BasePersonInfoMapper;
import cn.eyecool.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 人员信息改变事件发布服务
 * 
 * @author mawj
 * @date 2021/01/08
 */
@Service
@Slf4j
public class PersonChangeEventPublishService implements ApplicationEventPublisherAware {

    private ApplicationEventPublisher publisher;
    @Autowired
    private BasePersonInfoMapper basePersonInfoMapper;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * 新增人员事件发布
     * 
     * @param personId 人员主键
     * @param uniqueId 人员唯一标识
     * @param faceChanged 是否有人脸
     * @param fingerChanged 是否有指纹
     * @param irisChanged 是否有虹膜
     * @param fveinChanged 是否有指静脉
     * @param irisfaceChanged 是否有虹膜人脸多模态
     * @param callback 事件执行回调
     */
    public void personAddPublish(String personId, String uniqueId, boolean faceChanged, boolean fingerChanged,
        boolean irisChanged, boolean fveinChanged, boolean irisfaceChanged, EventCallback callback) {
        this.publish(PersonChangeEventType.PERSON_ADD, personId, uniqueId, faceChanged, fingerChanged, irisChanged,
            fveinChanged, irisfaceChanged, callback);
    }

    /**
     * 新增人员事件发布
     * 
     * @param personId 人员主键
     * @param uniqueId 人员唯一标识
     * @param faceChanged 是否有人脸
     * @param fingerChanged 是否有指纹
     * @param irisChanged 是否有虹膜
     * @param fveinChanged 是否有指静脉
     * @param irisfaceChanged 是否有虹膜人脸多模态
     * @param callback 事件执行回调
     * @param channleCodes 需要自动绑定的场景编码
     * @param subCodes 需要自动绑定的子场景编码
     * @param dataSource 人员信息数据来源
     */
    public void personAddPublish(String personId, String uniqueId, boolean faceChanged, boolean fingerChanged,
        boolean irisChanged, boolean fveinChanged, boolean irisfaceChanged, EventCallback callback, String channelCodes,
        String subCodes, String dataSource) {
        this.publish(PersonChangeEventType.PERSON_ADD, personId, uniqueId, faceChanged, fingerChanged, irisChanged,
            fveinChanged, irisfaceChanged, callback, channelCodes, subCodes, dataSource);
    }

    /**
     * 人员改变事件发布
     * 
     * @param personId 人员主键
     * @param faceChanged 是否有人脸
     * @param fingerChanged 是否有指纹
     * @param irisChanged 是否有虹膜
     * @param fveinChanged 是否有指静脉
     * @param irisfaceChanged 是否有虹膜人脸多模态
     * @param callback 事件执行回调
     * @param channleCodes 需要自动绑定的场景编码
     * @param subCodes 需要自动绑定的子场景编码
     * @param dataSource 人员信息数据来源
     */
    public void personUpdatePublish(String personId, String uniqueId, boolean faceChanged, boolean fingerChanged,
        boolean irisChanged, boolean fveinChanged, boolean irisfaceChanged, EventCallback callback, String channelCodes,
        String subCodes, String dataSource) {
        this.publish(PersonChangeEventType.PERSON_UPDATE, personId, uniqueId, faceChanged, fingerChanged, irisChanged,
            fveinChanged, irisfaceChanged, callback, channelCodes, subCodes, dataSource);
    }

    /**
     * 人员改变事件发布
     * 
     * @param personId 人员主键
     * @param faceChanged 是否有人脸
     * @param fingerChanged 是否有指纹
     * @param irisChanged 是否有虹膜
     * @param fveinChanged 是否有指静脉
     * @param irisfaceChanged 是否有虹膜人脸多模态
     * @param callback 事件执行回调
     */
    public void personUpdatePublish(String personId, String uniqueId, boolean faceChanged, boolean fingerChanged,
        boolean irisChanged, boolean fveinChanged, boolean irisfaceChanged, EventCallback callback) {
        this.publish(PersonChangeEventType.PERSON_UPDATE, personId, uniqueId, faceChanged, fingerChanged, irisChanged,
            fveinChanged, irisfaceChanged, callback);
    }

    /**
     * 删除人员事件发布
     * 
     * @param personId 人员主键
     * @param callback 事件执行回调
     */
    public void personDelPublish(String personId, String uniqueId, EventCallback callback) {
        this.publish(PersonChangeEventType.PERSON_DELETE, personId, uniqueId, true, true, true, true, true, callback);
    }

    /**
     * 人员基础信息变化事件发布
     * 
     * @param personId 人员主键
     * @param uniqueId 人员标识
     * @param callback 事件执行回调
     */
    public void personChangePublish(String personId, String uniqueId, EventCallback callback) {
        this.publish(PersonChangeEventType.PERSON_UPDATE, personId, uniqueId, false, false, false, false, false,
            callback);
    }

    /**
     * 人员证件信息变化事件发布
     * 
     * @param personId 人员主键
     * @param uniqueId 人员标识
     * @param callback 事件执行回调
     */
    public void personCertUpdatePublish(String personId, String uniqueId, EventCallback callback) {
        this.publish(PersonChangeEventType.PERSON_CERT_UPDATE, personId, uniqueId, false, false, false, false, false,
            callback);
    }

    /**
     * 人员基础信息属性变化事件发布
     * 
     * @param personId 人员主键
     * @param uniqueId 人员标识
     * @param callback 事件执行回调
     * @param channleCodes 需要自动绑定的场景编码
     * @param subCodes 需要自动绑定的子场景编码
     * @param dataSource 人员信息数据来源
     */
    public void personAttrChangePublish(String personId, String uniqueId, EventCallback callback, String channelCodes,
        String subCodes, String dataSource) {
        this.publish(PersonChangeEventType.PERSON_ATTR_UPDATE, personId, uniqueId, false, false, false, false, false,
            callback, channelCodes, subCodes, dataSource);
    }

    /**
     * 人脸信息变化事件发布
     * 
     * @param personId 人员主键
     * @param callback 事件执行回调
     */
    public void personFaceChangePublish(String personId, String uniqueId, EventCallback callback) {
        this.publish(PersonChangeEventType.PERSON_BIO_UPDATE, personId, uniqueId, true, false, false, false, false,
            callback);
    }

    /**
     * 指纹信息变化事件发布
     * 
     * @param personId 人员主键
     * @param personId 人员唯一编码
     * @param callback 事件执行回调
     */
    public void personFingerChangePublish(String personId, String uniqueId, EventCallback callback) {
        this.publish(PersonChangeEventType.PERSON_BIO_UPDATE, personId, uniqueId, false, true, false, false, false,
            callback);
    }

    /**
     * 虹膜信息变化事件发布
     * 
     * @param personId 人员主键
     * @param callback 事件执行回调
     */
    public void personIrisChangePublish(String personId, String uniqueId, EventCallback callback) {
        this.publish(PersonChangeEventType.PERSON_BIO_UPDATE, personId, uniqueId, false, false, true, false, false,
            callback);
    }

    /**
     * 指静脉信息变化事件发布
     * 
     * @param personId 人员主键
     * @param callback 事件执行回调
     */
    public void personFveinChangePublish(String personId, String uniqueId, EventCallback callback) {
        this.publish(PersonChangeEventType.PERSON_BIO_UPDATE, personId, uniqueId, false, false, false, true, false,
            callback);
    }

    /**
     * 虹膜人脸多模态信息变化事件发布
     * 
     * @param personId 人员主键
     * @param callback 事件执行回调
     */
    public void personIrisFaceChangePublish(String personId, String uniqueId, EventCallback callback) {
        this.publish(PersonChangeEventType.PERSON_BIO_UPDATE, personId, uniqueId, false, false, false, false, true,
            callback);
    }

    /**
     * 虹膜人脸多模态信息注冊事件发布
     * 
     * @param personId 人员主键
     * @param channleCodes 需要自动绑定的场景编码列表
     * @param subIds 子场景编码集合（逗号分隔）
     * @param callback 事件执行回调
     * @param dataSource 人员信息数据来源
     */
    public void personIrisFaceRegisterPublish(String personId, String uniqueId, String channleCodes, String subCodes,
        EventCallback callback, String dataSource) {
        this.publish(PersonChangeEventType.PERSON_REGISTER, personId, uniqueId, false, false, false, false, true,
            callback, channleCodes, subCodes, dataSource);
    }

    /**
     * 虹膜人脸多模态信息变化事件发布
     * 
     * @param personId 人员主键
     * @param callback 事件执行回调
     */
    public void devicePersonDeletePublish(String personId, String uniqueId, EventCallback callback) {
        this.publish(PersonChangeEventType.DEVICE_PERSON_DELETE, personId, uniqueId, false, false, false, false, false,
            callback);
    }

    /**
     * 人员所有基础信息变化事件发布
     * 
     * @param eventType 事件类型
     * @param personId 人员主键
     * @param uniqueId 人员唯一标识
     * @param faceChanged 人脸是否变化
     * @param fingerChanged 指纹是否变化
     * @param irisChanged 虹膜是否变化
     * @param fveinChanged 指静脉是否变化
     * @param irisfaceChanged 虹膜人脸多模态是否变化
     * @param callback 事件执行回调
     * @param channleCodes 需要自动绑定的场景编码
     * @param subCodes 需要自动绑定的子场景编码
     * @param dataSource 人员信息数据来源
     */
    private void publish(PersonChangeEventType eventType, String personId, String uniqueId, boolean faceChanged,
        boolean fingerChanged, boolean irisChanged, boolean fveinChanged, boolean irisfaceChanged,
        EventCallback callback, String channleCodes, String subCodes, String dataSource) {
        if (null == callback) {
            callback = defaultCallback();
        }
        if (StringUtils.isBlank(personId) || StringUtils.isBlank(uniqueId)) {
            BasePersonInfo info = selectBasePersonInfo(personId, uniqueId);
            if (null != info) {
                personId = info.getId();
                uniqueId = info.getUniqueId();
            }
        }
        publisher.publishEvent(new PersonInfoChangeEvent(this, eventType, personId, uniqueId, faceChanged,
            fingerChanged, irisChanged, fveinChanged, irisfaceChanged, callback, channleCodes, subCodes, dataSource));
    }

    /**
     * 人员所有基础信息变化事件发布
     * 
     * @param eventType 事件类型
     * @param personId 人员主键
     * @param uniqueId 人员唯一标识
     * @param faceChanged 人脸是否变化
     * @param fingerChanged 指纹是否变化
     * @param irisChanged 虹膜是否变化
     * @param fveinChanged 指静脉是否变化
     * @param irisfaceChanged 虹膜人脸多模态是否变化
     * @param callback 事件执行回调
     */
    private void publish(PersonChangeEventType eventType, String personId, String uniqueId, boolean faceChanged,
        boolean fingerChanged, boolean irisChanged, boolean fveinChanged, boolean irisfaceChanged,
        EventCallback callback) {
        if (null == callback) {
            callback = defaultCallback();
        }
        if (StringUtils.isBlank(personId) || StringUtils.isBlank(uniqueId)) {
            BasePersonInfo info = selectBasePersonInfo(personId, uniqueId);
            if (null != info) {
                personId = info.getId();
                uniqueId = info.getUniqueId();
            }
        }
        publisher.publishEvent(new PersonInfoChangeEvent(this, eventType, personId, uniqueId, faceChanged,
            fingerChanged, irisChanged, fveinChanged, irisfaceChanged, callback));
    }

    /**
     * 默认回调
     * 
     * @return
     */
    private EventCallback defaultCallback() {
        return new EventCallback() {

            @Override
            public void onError(String errmsg) {
                // TODO 执行失败的话，可能需要考虑重新发布
                log.error("the personnel information change event release processing error:[{}]", errmsg);
            }

            @Override
            public void onSuccess() {
                log.info("the personnel information change event release processing succeeded");
            }

            @Override
            public void onSuccess(Object data) {
                // TODO
            }
        };
    }

    /**
     * 根据主键或唯一标识查询人员信息
     * 
     * @param id
     * @param uniqueId
     * @return
     */
    private BasePersonInfo selectBasePersonInfo(String id, String uniqueId) {
        if (StringUtils.isNotBlank(id)) {
            return basePersonInfoMapper.selectBasePersonInfoById(id);
        }
        if (StringUtils.isNotBlank(uniqueId)) {
            BasePersonInfo condition = new BasePersonInfo();
            condition.setUniqueId(uniqueId);
            List<BasePersonInfo> list = basePersonInfoMapper.selectBasePersonInfoList(condition);
            return CollectionUtils.isEmpty(list) ? null : list.get(0);
        }
        return null;
    }

    /**
     * 人脸注册
     * 
     * @param personId 人员id
     * @param uniqueId 人员业务id
     * @param channelCode 场景
     * @param primarySubCode 子场景
     * @param dataSource 人脸信息数据来源
     */
    public void personFaceRegisterPublish(String personId, String uniqueId, String channelCode, String primarySubCode,
        String dataSource) {
        this.publish(PersonChangeEventType.PERSON_REGISTER, personId, uniqueId, true, false, false, false, false, null,
            channelCode, primarySubCode, dataSource);
    }
}