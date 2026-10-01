package cn.eyecool.noninductive.disruptor.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author sunhuayu
 */
@Data
public class FaceSearchResultNoticeEvent {
    private FaceSearchResultMessage result;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FaceSearchResultMessage {
        private String personId;
        private String result;
        private String matchTime;
        private double matchScore;
        private String deviceNo;
        private String tmplImageUrl;
        private String liveFaceDataUrl;
        private String liveFaceDataB64;
        private String tmplImageData64;
        private String deviceAddr;
    }
}
