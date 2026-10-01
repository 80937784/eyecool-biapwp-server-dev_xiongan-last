package cn.eyecool.device.service;

import javax.websocket.Session;

/**
 * 设备升级结果回写Service
 * 
 * @author mawj
 * @date 2021/10/29
 */
public interface IDeviceUpgradeResultHandleService {

    /**
     * 更新设备升级结果
     * 
     * @param session
     * @param message
     */
    public void updateDeviceUpgradeResult(Session session, String message);

}
