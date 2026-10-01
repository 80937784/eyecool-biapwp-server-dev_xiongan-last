package cn.eyecool.noninductive.disruptor.queue;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.IgnoreExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import cn.eyecool.noninductive.disruptor.event.FaceSearchEvent;
import cn.eyecool.noninductive.disruptor.eventFactory.FaceSearchEventFactory;
import cn.eyecool.noninductive.disruptor.handler.FaceSearchHandler;

@Component
public class FaceSearchDisruptorQueue {
    private static final Logger LOGGER = LoggerFactory.getLogger(FaceSearchDisruptorQueue.class);
    private static RingBuffer<FaceSearchEvent> ringBuffer;

    @Autowired
    FaceSearchDisruptorQueue(FaceSearchHandler eventHandler) {
        int bufferSize = 32 * 1024 * 32;
        ThreadFactory executor = Executors.defaultThreadFactory();
        Disruptor<FaceSearchEvent> disruptor = new Disruptor<>(new FaceSearchEventFactory(), bufferSize, executor,
                ProducerType.MULTI, new BlockingWaitStrategy());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("***********RecognizeHitDisruptorQueue start***********");
        }
        disruptor.handleEventsWith(eventHandler);
        ringBuffer = disruptor.getRingBuffer();
        disruptor.setDefaultExceptionHandler(new IgnoreExceptionHandler());
        disruptor.start();
    }

    public static void publishEvent(FaceSearchEvent.FaceSearchMessage log) {
        long sequence = ringBuffer.next();
        try {
            FaceSearchEvent event = ringBuffer.get(sequence);
            event.setResult(log);
        } finally {
            ringBuffer.publish(sequence);
        }
    }
}
