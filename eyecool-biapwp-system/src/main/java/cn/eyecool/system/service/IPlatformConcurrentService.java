package cn.eyecool.system.service;

/**
 * 平台接口并发量控制
 * 
 * @author admin
 * @date 2019年11月18日
 */
public interface IPlatformConcurrentService {

    /**
     * 清空缓存并发控制信号
     */
    void clearSemaphore();

    /**
     * 获取并发信号量许可
     * 
     * @return
     */
    boolean acquireSemaphore();

    /**
     * 释放并发许可证数量
     */
    void releseSemaphore();

}
