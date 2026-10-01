package cn.eyecool.basedata.constant;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 操作锁对象
 * 
 * @author admin
 * @date 2020年4月20日
 */
public class OperateLock {

    /** 同步人脸数据到Datamanager加锁 */
    public static final Lock syncBaseDataLock = new ReentrantLock();
    /** 一键更新人脸特征操作加锁 */
    public static final Lock updateFaceFeatureLock = new ReentrantLock();
    /** 一键更新指纹特征操作加锁 */
    public static final Lock updateFingerFeatureLock = new ReentrantLock();
    /** 一键更新虹膜特征操作加锁 */
    public static final Lock updateIrisFeatureLock = new ReentrantLock();

}
