package cn.eyecool.scene.constant;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 操作锁对象
 * 
 * @author admin
 * @date 2020年4月20日
 */
public class OperateLock {

    /** 同步场景库人员到Datamanager、同步子场景人员到Datamanager加锁，这些操作共享一把锁，全部互斥 */
    public static final Lock synclock = new ReentrantLock();
}
