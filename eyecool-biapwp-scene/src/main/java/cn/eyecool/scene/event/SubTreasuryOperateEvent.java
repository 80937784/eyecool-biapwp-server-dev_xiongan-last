package cn.eyecool.scene.event;

import java.util.List;

import org.springframework.context.ApplicationEvent;

/**
 * 删除子场景或者清空子场景事件
 * 
 * @author mawj
 * @date 2021/01/06
 */
public class SubTreasuryOperateEvent extends ApplicationEvent {

    private static final long serialVersionUID = 4862094181090623950L;

    /** 事件类型 */
    SubTreasuryOperateEventType subTreasuryOperateEventType;
    /** 进行操作的子场景编码 */
    private List<String> subtreasuryCodes;
    /** 进行操作的子场景所属场景编码 */
    private String channelCode;
    /** 事件执行回调 */
    private ISubTreasuryEventCallback callback;

    public SubTreasuryOperateEvent(Object source, SubTreasuryOperateEventType subTreasuryOperateEventType,
        String channelCode, List<String> subtreasuryCodes, ISubTreasuryEventCallback callback) {
        super(source);
        this.setSubTreasuryOperateEventType(subTreasuryOperateEventType);
        this.setSubtreasuryCodes(subtreasuryCodes);
        this.setChannelCode(channelCode);
        this.setCallback(callback);
    }

    public SubTreasuryOperateEventType getSubTreasuryOperateEventType() {
        return subTreasuryOperateEventType;
    }

    public void setSubTreasuryOperateEventType(SubTreasuryOperateEventType subTreasuryOperateEventType) {
        this.subTreasuryOperateEventType = subTreasuryOperateEventType;
    }

    public List<String> getSubtreasuryCodes() {
        return subtreasuryCodes;
    }

    public void setSubtreasuryCodes(List<String> subtreasuryCodes) {
        this.subtreasuryCodes = subtreasuryCodes;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public ISubTreasuryEventCallback getCallback() {
        return callback;
    }

    public void setCallback(ISubTreasuryEventCallback callback) {
        this.callback = callback;
    }

}
