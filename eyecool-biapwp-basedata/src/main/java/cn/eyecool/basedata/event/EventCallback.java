package cn.eyecool.basedata.event;

/**
 * 发布事件处理回调
 * 
 * @author mawj
 * @date 2021/01/08
 */
public interface EventCallback {

    /**
     * 事件发布处理错误回调
     * 
     * @param errmsg
     */
    public void onError(String errmsg);

    /**
     * 成功并返回数据回掉
     * 
     * @param data
     */
    public void onSuccess(Object data);

    /**
     * 成功处理回调
     */
    public default void onSuccess() {};

}
