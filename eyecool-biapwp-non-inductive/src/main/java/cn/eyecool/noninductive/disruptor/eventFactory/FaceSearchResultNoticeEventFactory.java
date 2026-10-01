package cn.eyecool.noninductive.disruptor.eventFactory;

import com.lmax.disruptor.EventFactory;

import cn.eyecool.noninductive.disruptor.event.FaceSearchResultNoticeEvent;

public class FaceSearchResultNoticeEventFactory implements EventFactory<FaceSearchResultNoticeEvent>{

    @Override
    public FaceSearchResultNoticeEvent newInstance() {
        return new FaceSearchResultNoticeEvent();
    }
    
}
