package cn.eyecool.noninductive.disruptor.eventFactory;

import com.lmax.disruptor.EventFactory;

import cn.eyecool.noninductive.disruptor.event.FaceSearchEvent;

public class FaceSearchEventFactory implements EventFactory<FaceSearchEvent> {

    @Override
    public FaceSearchEvent newInstance() {
        return new FaceSearchEvent();
    }

}
