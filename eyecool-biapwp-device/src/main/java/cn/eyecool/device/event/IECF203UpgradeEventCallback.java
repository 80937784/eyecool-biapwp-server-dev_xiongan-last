package cn.eyecool.device.event;

/**
 * 203升级任务发布事件处理回调
 * 
 * @author mawj
 * @date 2021/10/29
 */
public interface IECF203UpgradeEventCallback {

    /**
     * 事件发布处理错误回调
     */
    public void onError(String errmsg);

    /**
     * 成功处理回调
     */
    public default void onSuccess() {};

}
