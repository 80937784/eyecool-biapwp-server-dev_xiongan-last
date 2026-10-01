package cn.eyecool.device.event;

import org.springframework.context.ApplicationEvent;

/**
 * 203s升级任务发布事件
 * 
 * @author mawj
 * @date 2021/01/06
 */
public class EFC203UpgradeTaskEvent extends ApplicationEvent {

    private static final long serialVersionUID = 4862094181090623950L;

    private ECF203UpgradeTaskEventAttrs eventAttrs;
    /** 事件执行回调 */
    private IECF203UpgradeEventCallback callback;

    public EFC203UpgradeTaskEvent(Object source) {
        super(source);
    }

    public EFC203UpgradeTaskEvent(Object source, ECF203UpgradeTaskEventAttrs eventAttrs,
        IECF203UpgradeEventCallback callback) {
        super(source);
        this.eventAttrs = eventAttrs;
        this.callback = callback;
    }

    public ECF203UpgradeTaskEventAttrs getEventAttrs() {
        return eventAttrs;
    }

    public void setEventAttrs(ECF203UpgradeTaskEventAttrs eventAttrs) {
        this.eventAttrs = eventAttrs;
    }

    public IECF203UpgradeEventCallback getCallback() {
        return callback;
    }

    public void setCallback(IECF203UpgradeEventCallback callback) {
        this.callback = callback;
    }

}
