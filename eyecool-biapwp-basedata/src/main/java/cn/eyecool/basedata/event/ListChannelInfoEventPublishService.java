package cn.eyecool.basedata.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * 查询场景信息事件发布服务
 * 
 * @author mawj
 * @date 2024/04/22
 */
@Service
@Slf4j
public class ListChannelInfoEventPublishService implements ApplicationEventPublisherAware {

    private ApplicationEventPublisher publisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * 事件发布
     * 
     * @param channelCode
     * @param cascade
     * @param callback
     */
    public void publish(String uniqueId, String channelCode, Boolean cascade, EventCallback callback) {
        if (null == callback) {
            callback = defaultCallback();
        }
        publisher.publishEvent(new ListChannelInfoEvent(this, uniqueId, channelCode, cascade, callback));
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
                log.error("the list channel event processing error:[{}]", errmsg);
            }

            @Override
            public void onSuccess() {
                log.info("the list channel event processing processing succeeded");
            }

            @Override
            public void onSuccess(Object data) {
                log.debug("the list channel event processing processing succeeded, return data is: []",
                    data.toString());
            }
        };
    }

}
