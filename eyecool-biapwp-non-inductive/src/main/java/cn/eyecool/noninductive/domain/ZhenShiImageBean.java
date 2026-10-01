package cn.eyecool.noninductive.domain;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor

public class ZhenShiImageBean {
    private String cmd;
    private int id;
    private Body body;

    @Data
    @NoArgsConstructor

    public static class Body {
        private int event_type;
        private int trigger;
        private String time_str;
        private int num;
        private Timestamp timestamp;
        private int sex;
        private int age;
        private int have_hat;
        private int have_glasses;
        private int have_mask;
        private List<FaceData> face;
        private Snapshot snapshot;
        private List<Picture> picture;
        private String serialno;

        @Data
        @NoArgsConstructor

        public class Timestamp {

            private int sec;
            private int msec;

        }

        @Data
        @NoArgsConstructor

        public static class FaceData {

            private int face_id;

            private int confidence;

            private int eye_dist;

            private int yaw;

            private int pitch;

            private int roll;

            private Rect rect;

            @Data
            @NoArgsConstructor

            public static class Rect {
                private int left;
                private int top;
                private int right;
                private int bottom;
            }

        }

        @Data
        @NoArgsConstructor

        public static class Snapshot {
            private int type;
            private int length;
            private String data;
        }

        @Data
        @NoArgsConstructor
        public static class Picture {
            private int type;
            private int length;
            private String data;

        }
    }
}
