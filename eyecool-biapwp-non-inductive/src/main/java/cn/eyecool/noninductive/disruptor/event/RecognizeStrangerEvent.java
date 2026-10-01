package cn.eyecool.noninductive.disruptor.event;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author sunhuayu
 */
@Data
public class RecognizeStrangerEvent {

    private RecognizeStrangerMessage result;
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecognizeStrangerMessage implements Serializable {
        private static final long serialVersionUID = 1L;
        private String matchTime;
        private String deviceNo;
        private String imageBase64;
        private String comment;
    }
}
