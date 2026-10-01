package cn.eyecool.noninductive.disruptor.eventFactory;

import cn.eyecool.noninductive.disruptor.event.RecognizeStrangerEvent;
import com.lmax.disruptor.EventFactory;

/**
 * Created by kl on 2018/8/24.
 * Content :进程日志事件工厂类
 */
public class RecognizeStrangerEventFactory implements EventFactory<RecognizeStrangerEvent> {
    @Override
    public RecognizeStrangerEvent newInstance() {
        return new RecognizeStrangerEvent();
    }
}
