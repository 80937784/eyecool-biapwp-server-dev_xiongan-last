package cn.eyecool.noninductive.domain;

import java.util.List;

import lombok.Data;

/***
 *
 * 普通ipc视频人脸检测解析http推送数据
 *
 * @author 李强
 * @version [版本号, 2019年10月29日]
 * @since [应用/版本]
 */
@Data
public class CommonIpcImageBean {
    /**
     * 抓拍时间
     */
    private String capture_time;

    /**
     * 设备序列号
     */
    private String serial_no;

    /**
     * 设备业务编号
     */
    private String device_no;

    /**
     * 设备ip
     */
    private String device_ip;

    /**
     * 人脸相关
     */
    private List<CommonIpcImageBeanFace> face;

    /**
     * 原始图像Base64
     */
    private CommonIpcImageBeanSnapshot snapshot;

    @Data
    public class CommonIpcImageBeanFace {
        /**
         * 人脸追踪faceId
         */
        private int face_id;

        /**
         * 人脸置信度
         */
        private int confidence;

        /**
         * 瞳距
         */
        private int eye_dist;

        /**
         * 歪头角度
         */
        private int yaw;

        /**
         * 抬低头角度
         */
        private int pitch;

        /**
         * 转头角度
         */
        private int roll;

        /**
         * 性别
         */
        private int sex;

        /**
         * 年龄
         */
        private int age;

        /**
         * 种族得分
         */
        private int enth;

        /**
         * 帽子得分
         */
        private int hat;

        /**
         * 打电话得分
         */
        private int phone;

        /**
         * 眼镜佩戴得分
         */
        private int glassess;

        /**
         * 发型
         */
        private CommonIpcImageBeanFaceHairdo hairdo;

        /**
         * 眼睛相关
         */
        private CommonIpcImageBeanFaceEye eye;

        /**
         * 嘴巴相关
         */
        private CommonIpcImageBeanFaceMouth mouth;

        /**
         * 鼻子相关
         */
        private CommonIpcImageBeanFaceNode node;

        /**
         * 清晰度得分
         */
        private int nqty;

        /**
         * 表情
         */
        private CommonIpcImageBeanFaceLook look;

        /**
         * 人脸矩形
         */
        private CommonIpcImageBeanFaceRect rect;

        /**
         * 人脸图像base64
         */
        private String data;

        @Data
        public class CommonIpcImageBeanFaceRect {
            /**
             * 左上角X坐标
             */
            private int x;

            /**
             * 左上角Y坐标
             */
            private int y;

            /**
             * 人脸矩形宽度
             */
            private int width;

            /**
             * 人脸矩形高度
             */
            private int height;

        }

        @Data
        public class CommonIpcImageBeanFaceLook {

            /**
             * 惊讶得分
             */
            private int surprised;

            /**
             * 恐惧得分
             */
            private int fear;

            /**
             * 厌恶得分
             */
            private int hate;

            /**
             * 喜悦得分
             */
            private int joy;

            /**
             * 悲伤得分
             */
            private int sadness;

            /**
             * 愤怒得分
             */
            private int anger;

            /**
             * 中立得分
             */
            private int neutral;
        }

        @Data
        public class CommonIpcImageBeanFaceNode {
            /**
             * 鼻子遮挡得分
             */
            private int occ;
        }

        @Data
        public class CommonIpcImageBeanFaceHairdo {
            /**
             * 发型中光头得分
             */
            private int bareheaded;
            /**
             * 发型中谢顶得分
             */
            private int balding;
            /**
             * 男士短发或寸头
             */
            private int manshorthair;
            /**
             * 男长发或短发
             */
            private int manlonghair;
            /**
             * 中长头发
             */
            private int mediumlonghair;
            /**
             * 长发飘飘
             */
            private int longhair;
            /**
             * 扎起来的发型
             */
            private int tie;
        }
    }

    @Data
    public class CommonIpcImageBeanSnapshot {
        private String data;
    }

    @Data
    public class CommonIpcImageBeanFaceMouth {
        /**
         * 嘴巴张开程度
         */
        private int open;

        /**
         * 嘴巴遮挡得分
         */
        private int occ;

    }

    @Data
    public class CommonIpcImageBeanFaceEye {
        private CommonIpcImageBeanFaceEyeOpen open;
        private CommonIpcImageBeanFaceEyeOcc occ;

        @Data
        public class CommonIpcImageBeanFaceEyeOpen {
            /**
             * 左眼睁开得分
             */
            private int left;

            /**
             * 右眼睁开得分
             */
            private int right;
        }

        @Data
        public class CommonIpcImageBeanFaceEyeOcc {
            /**
             * 左眼遮挡
             */
            private int left;

            /**
             * 右眼遮挡
             */
            private int right;

        }
    }
}
