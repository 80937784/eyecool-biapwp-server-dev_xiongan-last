package cn.eyecool.noninductive.disruptor.eventFactory;

import com.lmax.disruptor.EventFactory;

import cn.eyecool.noninductive.disruptor.event.RecognizeHitEvent;

/**
 * Created by kl on 2018/8/24.
 * Content :进程日志事件工厂类
 */
public class RecognizeHitEventFactory implements EventFactory<RecognizeHitEvent> {
    @Override
    public RecognizeHitEvent newInstance() {
        return new RecognizeHitEvent();
    }
}
