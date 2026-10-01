package cn.eyecool.scene.service;

/**
 * 实时同步更新数据标识流水码服务
 * 
 * @author admin
 * @date 2019年12月6日
 */
public interface IBusiLiveUpdateSeqService {

    /**
     * 自增后获取场景人员SerialNum
     * 
     * @return
     */
    public Long incrementAndGetChannelBusiSeqNum();

    /**
     * 自增后获取子场景人员SerialNum
     * 
     * @return
     */
    public Long incrementAndGetSubtreasuryBusiSeqNum();
}
