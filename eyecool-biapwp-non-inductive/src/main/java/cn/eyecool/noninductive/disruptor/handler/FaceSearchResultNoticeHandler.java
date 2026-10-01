package cn.eyecool.noninductive.disruptor.handler;

import com.lmax.disruptor.EventHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.eyecool.noninductive.disruptor.event.FaceSearchResultNoticeEvent;
import cn.eyecool.noninductive.service.IMessageService;

@Component
public class FaceSearchResultNoticeHandler implements EventHandler<FaceSearchResultNoticeEvent> {
    @Autowired
    private IMessageService messageService;

    @Override
    public void onEvent(FaceSearchResultNoticeEvent event, long sequence, boolean endOfBatch) throws Exception {
        messageService.sendMsg(event.getResult());
    }
}
