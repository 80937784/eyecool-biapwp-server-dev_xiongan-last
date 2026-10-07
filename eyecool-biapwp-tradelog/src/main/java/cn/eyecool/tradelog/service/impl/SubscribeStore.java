package cn.eyecool.tradelog.service.impl;

import cn.eyecool.tradelog.domain.police.SubscribeObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 订阅任务存储。
 * 生产环境请替换为数据库（MyBatis/JPA），这里用内存 Map 便于直接跑通。
 */
@Component
public class SubscribeStore {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final Map<String, SubscribeObject> store = new ConcurrentHashMap<>();

    public SubscribeObject get(String id) {
        return store.get(id);
    }

    public Collection<SubscribeObject> all() {
        return store.values();
    }

    /** 新增或覆盖 */
    public SubscribeObject save(SubscribeObject sub) {
        store.put(sub.SubscribeID, sub);
        return sub;
    }

    public SubscribeObject remove(String id) {
        return store.remove(id);
    }

    /** 判断订阅是否在有效期内 */
    public boolean isActive(SubscribeObject sub) {
        if (sub == null || sub.SubscribeID == null) return false;
        if (sub.SubscribeStatus != null && sub.SubscribeStatus != 0) return false;
        String now = LocalDateTime.now().format(TS);
        if (sub.BeginTime != null && !sub.BeginTime.isEmpty() && now.compareTo(sub.BeginTime) < 0) return false;
        if (sub.EndTime != null && !sub.EndTime.isEmpty() && now.compareTo(sub.EndTime) > 0) return false;
        return true;
    }

    /**
     * 按资源类型筛选生效的订阅。
     * 匹配规则：订阅的 ResourceURI 中包含该资源名（忽略大小写），如 /VIID/Faces 匹配 "Faces"
     */
    public List<SubscribeObject> matchByResource(String resourceType) {
        List<SubscribeObject> hit = new ArrayList<>();
        for (SubscribeObject sub : store.values()) {
            if (!isActive(sub)) continue;
            if (sub.ResourceURI == null) continue;
            if (sub.ResourceURI.toUpperCase().contains(resourceType.toUpperCase())) {
                hit.add(sub);
            }
        }
        return hit;
    }
}
