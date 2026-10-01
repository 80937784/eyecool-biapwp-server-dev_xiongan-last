package cn.eyecool.scene.event;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * 子场景事件发布服务
 * 
 * @author mawj
 * @date 2021/01/06
 */
@Service
@Slf4j
public class SubtreasuryEventPublishlService implements ApplicationEventPublisherAware {

    private ApplicationEventPublisher publisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * 清空子场景数据事件发布
     * 
     * @param subtreasuryCodes
     * @param callback
     */
    public void clearSubDataPublish(List<String> subtreasuryCodes, ISubTreasuryEventCallback callback) {
        this.publish(SubTreasuryOperateEventType.SUB_TREASURY_CLEAR, null, subtreasuryCodes, callback);
    }

    /**
     * 删除子场景事件发布
     * 
     * @param subtreasuryCodes
     * @param callback
     */
    public void delSubtreasuryPublish(List<String> subtreasuryCodes, ISubTreasuryEventCallback callback) {
        this.publish(SubTreasuryOperateEventType.SUB_TREASURY_DELETE, null, subtreasuryCodes, callback);
    }

    /**
     * 发布事件
     * 
     * @param type
     * @param channelCode
     * @param subtreasuryCodes
     * @param callback
     */
    public void publish(SubTreasuryOperateEventType type, String channelCode, List<String> subtreasuryCodes,
        ISubTreasuryEventCallback callback) {
        if (null == callback) {
            callback = new ISubTreasuryEventCallback() {

                @Override
                public void onError(String errmsg) {
                    log.error("Subscene event release processing error:[{}]", errmsg);
                }

                @Override
                public void onSuccess() {
                    log.info("Sub-scene event publishing and processing succeeded");
                }
            };
        }
        publisher.publishEvent(new SubTreasuryOperateEvent(this, type, channelCode, subtreasuryCodes, callback));
    }
}