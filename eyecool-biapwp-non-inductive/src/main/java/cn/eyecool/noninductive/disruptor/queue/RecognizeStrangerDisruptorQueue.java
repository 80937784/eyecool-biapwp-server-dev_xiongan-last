package cn.eyecool.noninductive.disruptor.queue;

import cn.eyecool.noninductive.disruptor.event.RecognizeStrangerEvent;
import cn.eyecool.noninductive.disruptor.eventFactory.RecognizeStrangerEventFactory;
import cn.eyecool.noninductive.disruptor.handler.RecognizeStrangerHandler;

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
public class RecognizeStrangerDisruptorQueue {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecognizeStrangerDisruptorQueue.class);

    private static RingBuffer<RecognizeStrangerEvent> ringBuffer;

    @Autowired
    RecognizeStrangerDisruptorQueue(RecognizeStrangerHandler eventHandler) {
        int bufferSize = 32 * 1024 * 32;
        ThreadFactory executor = Executors.defaultThreadFactory();
        Disruptor<RecognizeStrangerEvent> disruptor = new Disruptor<>(new RecognizeStrangerEventFactory(), bufferSize,
                executor, ProducerType.MULTI, new BlockingWaitStrategy());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("***********RecognizeStrangerDisruptorQueue start***********");
        }
        disruptor.handleEventsWith(eventHandler);
        ringBuffer = disruptor.getRingBuffer();
        disruptor.start();
    }

    public static void publishEvent(RecognizeStrangerEvent.RecognizeStrangerMessage log) {
        long sequence = ringBuffer.next();
        try {
            RecognizeStrangerEvent event = ringBuffer.get(sequence);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("RecognizeStrangerDisruptorQueue get {}", log.toString());
            }
            event.setResult(log);
        } finally {
            ringBuffer.publish(sequence);
        }
    }

}
