package cn.eyecool.noninductive.domain;

import java.util.List;

import cn.eyecool.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
public class CommonVideoParseBean extends BaseEntity {

    private static final long serialVersionUID = 1L;
    // 视频设备号
    private String channel_id;
    // Atlas设备号
    private String device_number;
    // 被识别人员所属大图
    private String image;
    // 本次识别出人员
    private List<CommonVideoParsePersonBean> person_list;

    private Long time;

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class CommonVideoParsePersonBean extends BaseEntity {
        private static final long serialVersionUID = 1L;
        // 人员ID
        private String person_id;
        // 人员裁剪后的小图
        private String image;
        // 比对得分
        private Integer score;
        // 保留字段
        private Integer type;
        // 人员属性
        private CommonVideoParsePersonRectBean rect;

        @EqualsAndHashCode(callSuper = true)
        @Data
        @NoArgsConstructor
        public static class CommonVideoParsePersonRectBean extends BaseEntity {

            private static final long serialVersionUID = 1L;
            // 用途，低1字节，1指纹2人脸3虹膜
            private Long ufo;
            // 人脸框置信度得分，0~100
            private Long score;
            // 模糊度，0~100
            private Long fuzzy;
            // 性别，有效值为0~100
            private Long age;
            // 年龄，有效值为0~100
            private Long gender;
            // 图像内左上角X坐标，有可能为负值
            private Long left;
            // 图像内左上角Y坐标，有可能为负值
            private Long top;
            // 矩形宽度
            private Long width;
            // 矩形高度
            private Long height;

        }
    }
}
