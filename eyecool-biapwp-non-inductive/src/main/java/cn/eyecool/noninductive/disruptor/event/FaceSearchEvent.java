package cn.eyecool.noninductive.disruptor.event;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author sunhuayu
 */
@Data
public class FaceSearchEvent {
    private FaceSearchMessage result;
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FaceSearchMessage {

        private byte[] imageContent;

        private String deviceSerialNo;

        private JSONObject comment;

    }
}
