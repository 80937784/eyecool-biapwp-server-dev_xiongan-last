package cn.eyecool.device.util;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;

import cn.hutool.core.collection.ConcurrentHashSet;
import lombok.extern.slf4j.Slf4j;

/**
 * 自定义ID生成
 * 
 * @author mawj
 * @date 2021/11/01
 */
@Slf4j
public class CustomIdGenerator implements IdentifierGenerator {

    private static AtomicInteger atomicInteger = new AtomicInteger(0);

    // 由于JS最多识别16位长度，因此这里控制长度不超过16位，这里控制为16位
    private static int ID_LENGTH = 9;

    @Override
    public Number nextId(Object entity) {
        // 生成最大4位随技术
        int i2 = ThreadLocalRandom.current().nextInt(9999);
        String timeStr = String.valueOf(System.currentTimeMillis());
        // 取出时间串前面相同的部分
        timeStr = timeStr.substring(5);
        // 递增生成最大9999的递增ID
        if (atomicInteger.get() == 9999) {
            atomicInteger.set(0);
        }
        int i1 = atomicInteger.getAndIncrement();
        String id = timeStr.concat(String.valueOf(i2)).concat(i1 + "");
        // 严格控制ID长度，如果过长 从最前面截取
        if (id.length() > ID_LENGTH) {
            // 计算多了多少位
            int surplusLenth = id.length() - ID_LENGTH;
            id = id.substring(surplusLenth);
        }
        return Integer.valueOf(id);
    }

    public static void main(String[] args) throws InterruptedException {
        CustomIdGenerator customIdGenerator = new CustomIdGenerator();
        customIdGenerator.nextId(null).intValue();
        final CountDownLatch latch = new CountDownLatch(100000);
        Set<Integer> set = new ConcurrentHashSet<>();
        for (int i = 0; i < 100000; i++) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    for (int j = 0; j < 1; j++) {
                        Number number = customIdGenerator.nextId(new Object());
                        int l = number.intValue();
                        boolean add = set.add(l);
                        if (!add) {
                            System.out.println("repeat" + l);
                        }
                    }
                    latch.countDown();
                }
            };
            thread.start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }
}