package cn.eyecool.basedata.event;

import org.springframework.context.ApplicationEvent;

/**
 * 列举场景列表事件
 * 
 * @author mawj
 * @date 2024/04/22
 */
public class ListChannelInfoEvent extends ApplicationEvent {

    private static final long serialVersionUID = 4572871756201050506L;

    /** 人员标识，如果传入则查询该人员的绑定场景及子场景信息 */
    private String uniqueId;

    /** 场景编码，如果传入则只查询该场景及子场景信息 */
    private String channelCode;

    /** 是否级联查询 */
    private Boolean cascade;

    /** 事件执行回调 */
    private EventCallback callback;

    public ListChannelInfoEvent(Object source, String uniqueId, String channelCode, Boolean cascade,
        EventCallback callback) {
        super(source);
        this.setUniqueId(uniqueId);
        this.setChannelCode(channelCode);
        this.setCascade(cascade);
        this.setCallback(callback);
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public Boolean getCascade() {
        return cascade;
    }

    public void setCascade(Boolean cascade) {
        this.cascade = cascade;
    }

    public EventCallback getCallback() {
        return callback;
    }

    public void setCallback(EventCallback callback) {
        this.callback = callback;
    }

}
