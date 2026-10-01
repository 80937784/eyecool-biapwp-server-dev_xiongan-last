package cn.eyecool.device.event.listener;

import java.util.Base64;

import javax.websocket.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.utils.AESUtils;
import cn.eyecool.device.constant.AdapterConstants;
import cn.eyecool.device.controller.WebsocketController;
import cn.eyecool.device.util.CustomIdGenerator;
import lombok.extern.slf4j.Slf4j;

/**
 * 203设备升级任务下载相关redis-key过期监听
 * 
 * @author mawj
 * @date 2021/06/01
 */
@Slf4j
@Component
public class ECF203UpgradeTaskRedisKeyExpireListener extends KeyExpirationEventMessageListener {

    @Autowired
    private RedisCache redisCache;

    public ECF203UpgradeTaskRedisKeyExpireListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.info("redis expiredKey=========" + expiredKey);
        if (!expiredKey.startsWith(AdapterConstants.DEVICE_UPGRADE_TASK_CACHE_PREFIX)) {
            return;
        }
        String encryptFlag = expiredKey.split(":")[2];
        String flag = AESUtils.decryptAES(new String(Base64.getDecoder().decode(encryptFlag)));
        String[] array = flag.split(":");
        String taskId = array[0];
        String deviceNo = array[1];
        // String taskId = "903734363840188416";
        // String deviceNo = "5C12R050004";
        // 获取设备的升级版本
        Session session = WebsocketController.getSessionPool().get(deviceNo);
        if (session == null || !session.isOpen()) {
            log.error("The device is not connected or has been closed, we can not get the version information");
            return;
        }
        // 会话ID
        String sessionId = session.getId();
        // 回复ID编号
        CustomIdGenerator customIdGenerator = new CustomIdGenerator();
        Number number = customIdGenerator.nextId(new Object());
        int id = number.intValue();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("method", "sysHelper.getSystemInfo");
        jsonObject.put("id", id);
        jsonObject.put("session", sessionId);
        // 值按照回复编号ID、升级任务ID、设备编码顺序通过:拼接字符串存入redis，等设备端有webssocket回复后，根据会话ID取出信息，进行任务升级结果判断和保存
        redisCache.setCacheObject(AdapterConstants.DEVICE_VERSION_QUERY_CACHE_PREFIX + id,
            id + ":" + taskId + ":" + deviceNo);
        log.info("get the version information after the device upgrade,deviceNo:[{}], taskId:[{}], msg:[{}]", deviceNo, taskId, jsonObject.toJSONString());
        
        session.getAsyncRemote().sendText(jsonObject.toJSONString());
    }

}