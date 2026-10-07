package cn.eyecool.tradelog.domain.police;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/** 订阅请求体：{"SubscribeListObject":{"SubscribeObject":[...]}} */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class SubscribeListObject {
    public List<SubscribeObject> SubscribeObject;
}
