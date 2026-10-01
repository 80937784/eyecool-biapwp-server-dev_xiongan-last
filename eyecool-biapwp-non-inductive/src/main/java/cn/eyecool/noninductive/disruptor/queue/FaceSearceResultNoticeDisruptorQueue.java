package cn.eyecool.noninductive.disruptor.queue;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.eyecool.noninductive.disruptor.event.FaceSearchResultNoticeEvent;
import cn.eyecool.noninductive.disruptor.eventFactory.FaceSearchResultNoticeEventFactory;
import cn.eyecool.noninductive.disruptor.handler.FaceSearchResultNoticeHandler;


@Component
public class FaceSearceResultNoticeDisruptorQueue {
    private ThreadFactory executor = Executors.defaultThreadFactory();

    // The factory for the event
    private FaceSearchResultNoticeEventFactory factory = new FaceSearchResultNoticeEventFactory();

    // Specify the size of the ring buffer, must be power of 2.
    private int bufferSize = 2 * 1024 * 32;

    // Construct the Disruptor
    private Disruptor<FaceSearchResultNoticeEvent> disruptor = new Disruptor<>(factory, bufferSize, executor);;

    private static RingBuffer<FaceSearchResultNoticeEvent> ringBuffer;

    @Autowired
    FaceSearceResultNoticeDisruptorQueue(FaceSearchResultNoticeHandler eventHandler) {
        disruptor.handleEventsWith(eventHandler);
        ringBuffer = disruptor.getRingBuffer();
        disruptor.start();
    }

    public static void publishEvent(FaceSearchResultNoticeEvent.FaceSearchResultMessage log) {
        long sequence = ringBuffer.next(); // Grab the next sequence
        try {
            FaceSearchResultNoticeEvent event = ringBuffer.get(sequence); // Get the entry in the Disruptor
            // for the sequence
            event.setResult(log); // Fill with data
        } finally {
            ringBuffer.publish(sequence);
        }
    }
}
