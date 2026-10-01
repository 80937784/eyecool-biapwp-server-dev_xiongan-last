package cn.eyecool.device.service;

import javax.websocket.Session;

import cn.eyecool.device.vo.SummaryVO;

/**
 * 203设备接入园区平台Service接口
 * 
 * @author 段存明
 * @date 2021-02-25
 */
public interface ISyncService {

    /**
     * 同步数据 syncData
     * 
     * @param serialNo
     */
    public void syncData(Session session, String serialNo, String url);

    /**
     * transferHandler 数据中转站
     *
     * @param session
     * @param serialNo
     * @param url
     */
    public void transferHandler(Session session, String serialNo, String url);

    /**
     * 执行同步 execSync
     *
     * @param session
     * @param summaryVO
     * @param url
     * @return
     */
    public boolean execSync(Session session, SummaryVO summaryVO, String url, String devSn);

    /**
     * 获取实时同步数据 fetchPersonInfo
     *
     * @param devSn
     * @param updateSeriaNum
     * @return
     */
    public SummaryVO fetchPersonInfo(String devSn, String updateSeriaNum);

}
