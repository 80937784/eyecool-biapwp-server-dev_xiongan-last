package cn.eyecool.noninductive.service;

import cn.eyecool.noninductive.disruptor.event.FaceSearchResultNoticeEvent;

/**
 * 通知消息处理接口
 * <p>
 * 应用应自行根据需要实现此接口
 *
 * @author 李强
 * @version [版本号, 2019年5月9日]
 * @since [应用/版本]
 */
public interface IMessageService {
    /**
     * @param message
     * @return
     */
    public String sendMsg(FaceSearchResultNoticeEvent.FaceSearchResultMessage message);

}
