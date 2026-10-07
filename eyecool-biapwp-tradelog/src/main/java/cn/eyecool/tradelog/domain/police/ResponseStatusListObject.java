package cn.eyecool.tradelog.domain.police;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/** 批量响应体：{"ResponseStatusListObject":{"ResponseStatusObject":[...]}} */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class ResponseStatusListObject {
    public List<ResponseStatusObject> ResponseStatusObject;

    public ResponseStatusListObject() {}

    public ResponseStatusListObject(List<ResponseStatusObject> list) {
        this.ResponseStatusObject = list;
    }
}
