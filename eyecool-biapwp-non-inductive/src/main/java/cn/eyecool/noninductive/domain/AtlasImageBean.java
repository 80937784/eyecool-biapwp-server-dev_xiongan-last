package cn.eyecool.noninductive.domain;

import java.util.List;

import lombok.Data;

/**
 * Description Package cn.eyecool.dfrs.domain
 *
 * @author sunhuayu Date on 2020/2/26
 */
@Data
public class AtlasImageBean {
    private String corpId;
    private String strTESn;
    private String bussType;
    private String protocolNo;
    private String sTimeStamp;
    private DATA DATA;

    @Data
    public static class DATA {
        private String imgType;
        private String imgScene;
        private String sceneId;
        private List<imgFace> imgFace;

        @Data
        public static class imgFace {
            private String img;
            private String coordinate;
        }
    }
}
