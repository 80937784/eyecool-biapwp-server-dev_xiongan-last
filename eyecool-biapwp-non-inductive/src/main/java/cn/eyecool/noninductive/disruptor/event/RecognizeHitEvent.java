package cn.eyecool.noninductive.disruptor.event;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author sunhuayu
 */
@Data
public class RecognizeHitEvent {
    private RecognizeHitResultMessage result;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecognizeHitResultMessage implements Serializable{
        private static final long serialVersionUID = 1L;
        private String personCode;
        private String matchTime;
        private String deviceNo;
        private String imageBase64;
        private String personName;
        private double score;
        private String personFlag;
        private String comment;
    }
}
