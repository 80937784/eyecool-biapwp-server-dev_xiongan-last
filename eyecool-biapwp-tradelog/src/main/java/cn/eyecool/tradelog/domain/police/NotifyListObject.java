package cn.eyecool.tradelog.domain.police;

import com.fasterxml.jackson.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 通知对象 NotifyListObject（7.2.21）
 * 固定字段 + 任意数据体（FaceListObject / MotorVehicleListObject / PersonListObject ...）
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class NotifyListObject {

    /** 通知ID，唯一 */
    public String NotificationID;

    /** 所属订阅ID */
    public String SubscribeID;

    /** 通知时间 YYYYMMDDHHMMSS */
    public String NotificationTime;

    /** 资源类型，如 Faces */
    public String ResourceType;

    /** 承载的数据对象（FaceListObject 等），用 Any 存取避免写死类型 */
    @JsonIgnore
    private final Map<String, Object> data = new LinkedHashMap<>();

    @JsonAnySetter
    public void put(String key, Object value) {
        data.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getData() {
        return data;
    }
}
