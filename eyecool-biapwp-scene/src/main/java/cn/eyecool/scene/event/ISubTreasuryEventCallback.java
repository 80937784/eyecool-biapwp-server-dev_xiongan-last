package cn.eyecool.scene.event;

/**
 * 删除清空子场景发布事件处理回调
 * 
 * @author mawj
 * @date 2021/01/06
 */
public interface ISubTreasuryEventCallback {

    /**
     * 事件发布处理错误回调
     */
    public void onError(String errmsg);

    /**
     * 成功处理回调
     */
    public default void onSuccess() {};

}
