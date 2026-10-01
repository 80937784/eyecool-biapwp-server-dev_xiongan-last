package cn.eyecool.noninductive.disruptor.queue;

import cn.eyecool.noninductive.disruptor.event.RecognizeHitEvent;
import cn.eyecool.noninductive.disruptor.eventFactory.RecognizeHitEventFactory;
import cn.eyecool.noninductive.disruptor.handler.RecognizeHitHandler;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by kl on 2018/8/24. Content :Disruptor 环形队列
 */
@Component
public class RecognizeHitDisruptorQueue {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecognizeHitDisruptorQueue.class);
    private static RingBuffer<RecognizeHitEvent> ringBuffer;

    @Autowired
    RecognizeHitDisruptorQueue(RecognizeHitHandler eventHandler) {
        int bufferSize = 32 * 1024 * 32;
        ThreadFactory executor = Executors.defaultThreadFactory();
        Disruptor<RecognizeHitEvent> disruptor = new Disruptor<>(new RecognizeHitEventFactory(), bufferSize, executor,
                ProducerType.MULTI, new BlockingWaitStrategy());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("***********RecognizeHitDisruptorQueue start***********");
        }
        disruptor.handleEventsWith(eventHandler);
        ringBuffer = disruptor.getRingBuffer();
        disruptor.start();
    }

    public static void publishEvent(RecognizeHitEvent.RecognizeHitResultMessage log) {
        long sequence = ringBuffer.next();
        try {
            RecognizeHitEvent event = ringBuffer.get(sequence);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("RecognizeResultPushEvent get {}", log.toString());
            }
            event.setResult(log);
        } finally {
            ringBuffer.publish(sequence);
        }
    }

}
