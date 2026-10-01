package cn.eyecool.device.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.device.service.IDeviceAccessAdapterService;
import cn.eyecool.device.service.IDeviceUpgradeResultHandleService;
import cn.eyecool.device.service.ISyncService;
import lombok.extern.slf4j.Slf4j;

/**
 * 203设备接入园区平台Controller
 *
 * @author : 段存明
 * @version V1.0
 * @Project: eyecool-biapwp
 * @Package cn.eyecool.biapwp.adapter.controller
 * @date Date : 2021年02月24日 上午11:33
 */
@Component
@Slf4j
@ServerEndpoint("/api/websocket/sync/{serialNo}")
public class WebsocketController {
    private static CopyOnWriteArraySet<WebsocketController> webSockets = new CopyOnWriteArraySet<>();
    private static ConcurrentHashMap<String, Session> sessionPool = new ConcurrentHashMap<String, Session>();
    private static IDeviceAccessAdapterService iDeviceAccessAdapterService;
    private static ISyncService iSyncService;
    private static IDeviceUpgradeResultHandleService deviceUpgradeResultHandleService;

    @Autowired
    public void setIDeviceAccessAdapterService(IDeviceAccessAdapterService iDeviceAccessAdapterService) {
        WebsocketController.iDeviceAccessAdapterService = iDeviceAccessAdapterService;
    }

    @Autowired
    public void setISyncService(ISyncService iSyncService) {
        WebsocketController.iSyncService = iSyncService;
    }

    @Autowired
    public void
        setDeviceUpgradeResultHandleService(IDeviceUpgradeResultHandleService deviceUpgradeResultHandleService) {
        WebsocketController.deviceUpgradeResultHandleService = deviceUpgradeResultHandleService;
    }

    public static ConcurrentHashMap<String, Session> getSessionPool() {
        return sessionPool;
    }

    public static CopyOnWriteArraySet<WebsocketController> getWebSockets() {
        return webSockets;
    }

    /**
     * 同步所有设备
     */
    public synchronized void syncAllDev() {
        if (sessionPool == null || sessionPool.size() == 0) {
            log.info("[WebsocketController-syncAllDev] ,there is no device connected");
            return;
        }
        String url = iDeviceAccessAdapterService.getUrl();
        try {
            for (Map.Entry<String, Session> entry : sessionPool.entrySet()) {
                String serialNo = entry.getKey();
                Session session = entry.getValue();
                if (serialNo == null) {
                    webSockets.remove(this);
                    log.info("[WebsocketController-device synchronized] serialNo:[{}] is null webSockets.remove(serialNo)", serialNo);
                    continue;
                }
                if (session == null) {
                    webSockets.remove(this);
                    sessionPool.remove(serialNo);
                    log.info(
                        "[WebsocketController-device synchronized] serialNo:[{}] session is null webSockets.remove(serialNo) sessionPool.remove(serialNo)",
                        serialNo);
                    continue;
                }

                log.info("[WebsocketController-device synchronized],the device serial number is:[{}]", serialNo);
                iSyncService.syncData(session, serialNo, url);
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    /**
     * onOpen 连接
     * 
     * @param session session
     * @param serialNo serialNo
     */
    @OnOpen
    public void onOpen(Session session, @PathParam(value = "serialNo") String serialNo) {
        try {
            boolean result = iDeviceAccessAdapterService.checkAndCreateAdapterInfo(serialNo);
            if (!result) {
                if (session.isOpen()) {
                    session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, MessageUtils.message("device.no.exist.or.create.configuration.failed")));
                }
                return;
            }
            webSockets.add(this);
            sessionPool.put(serialNo, session);
            System.err.println(webSockets);
            String url = iDeviceAccessAdapterService.getUrl();
            iSyncService.syncData(session, serialNo, url);
            log.info("[WebsocketController-device connect] ,there is a new connection,serialNo:[{}],total :[{}]", serialNo, webSockets.size());
        } catch (Exception e) {
            log.error(" onOpen error : " + e.toString());
        }
    }

    /**
     * onClose 关闭连接
     */
    @OnClose
    public void onClose() {
        try {
            webSockets.remove(this);
            log.info("[WebsocketController-device connect] disconnect，total :" + webSockets.size());
        } catch (Exception e) {
            log.error(" onClose error : " + e.toString());
        }
    }

    /**
     * onMessage 发送消息
     * 
     * @param message message
     * @param session session
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("[WebsocketController-device] receive the message from client:" + message);
        if (StringUtils.isBlank(message)) {
            return;
        }
        // 保存设备升级结果
        deviceUpgradeResultHandleService.updateDeviceUpgradeResult(session, message);
    }

}
