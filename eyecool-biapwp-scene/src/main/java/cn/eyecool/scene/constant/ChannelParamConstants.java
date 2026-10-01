package cn.eyecool.scene.constant;

import java.util.List;

import com.beust.jcommander.internal.Lists;

import cn.eyecool.scene.enums.ChannelParamEnum;

/**
 * 场景参数配置常量
 * 
 * @author admin
 * @date 2019年11月13日
 */
public interface ChannelParamConstants {

    /** 人脸1-N搜索默认返回条数 */
    public static final Integer FACE_SEARCH_TOP_N_VALUE = 1;
    /** 指纹1-N搜索默认返回条数 */
    public static final Integer FINGER_SEARCH_TOP_N_VALUE = 1;
    /** 虹膜1-N搜索默认返回条数 */
    public static final Integer IRIS_SEARCH_TOP_N_VALUE = 1;

    /** 子场景操作类型 */
    public interface ChannelSubtreasuryOperateType {
        public static String OPERATE_TYPE_ADD = "ADD";
        public static String OPERATE_TYPE_DELETE = "DELETE";
        public static String OPERATE_TYPE_CLEAR = "CLEAR";

        public static List<String> typeList =
            Lists.newArrayList(OPERATE_TYPE_ADD, OPERATE_TYPE_DELETE, OPERATE_TYPE_CLEAR);
    }

    /** 人脸1-1是否检活 */
    public static final String FACE_COMPARE_CHECK_LIVE_CODE = ChannelParamEnum.FACE_COMPARE_CHECK_LIVE_PARAM.getCode();
    /** 人脸1-1检活阈值（静默） */
    public static final String FACE_COMPARE_CHECK_LIVE_THRESHOLD_CODE =
        ChannelParamEnum.FACE_COMPARE_CHECK_LIVE_THRESHOLD_PARAM.getCode();
    /** 人脸1-1检活阈值（视频） */
    public static final String FACE_COMPARE_VIDEO_CHECK_LIVE_THRESHOLD_CODE =
        ChannelParamEnum.FACE_COMPARE_VIDEO_CHECK_LIVE_THRESHOLD_PARAM.getCode();
    /** 人脸1-N是否检活 */
    public static final String FACE_SEARCH_N_CHECK_LIVE_CODE = ChannelParamEnum.FACE_SEARCHN_CHECK_LIVE_PARAM.getCode();
    /** 人脸1-N检活阈值（静默） */
    public static final String FACE_SEARCH_N_CHECK_LIVE_THRESHOLD_CODE =
        ChannelParamEnum.FACE_SEARCHN_CHECK_LIVE_THRESHOLD_PARAM.getCode();
    /** 人脸1-N检活阈值（视频） */
    public static final String FACE_SEARCH_N_VIDEO_CHECK_LIVE_THRESHOLD_CODE =
        ChannelParamEnum.FACE_SEARCHN_VIDEO_CHECK_LIVE_THRESHOLD_PARAM.getCode();
    /** 人脸1:1比对阈值 */
    public static final String FACE_COMPARE_THRESHOLD_CODE = ChannelParamEnum.FACE_COMPARE_THRESHOLD_PARAM.getCode();
    /** 人脸1:1比对是否校验人员合法性 */
    public static final String FACE_COMPARE_UNIQUE_ID_VALIDATE_CODE =
        ChannelParamEnum.FACE_COMPARE_UNIQUE_ID_VALIDATE_PARAM.getCode();
    /** 人脸1-N搜索阈值 */
    public static final String FACE_SEARCH_N_THRESHOLD_CODE = ChannelParamEnum.FACE_SEARCH_N_THRESHOLD_PARAM.getCode();
    /** 指纹1:1比对阈值 */
    public static final String FINGER_COMPARE_THRESHOLD_CODE =
        ChannelParamEnum.FINGER_COMPARE_THRESHOLD_PARAM.getCode();
    /** 指纹1-N搜索阈值 */
    public static final String FINGER_SEARCH_N_THRESHOLD_CODE =
        ChannelParamEnum.FINGER_SEARCH_N_THRESHOLD_PARAM.getCode();
    /** 虹膜1:1比对阈值 */
    public static final String IRIS_COMPARE_THRESHOLD_CODE = ChannelParamEnum.IRIS_COMPARE_THRESHOLD_PARAM.getCode();
    /** 虹膜1-N搜索阈值 */
    public static final String IRIS_SEARCH_N_THRESHOLD_CODE = ChannelParamEnum.IRIS_SEARCH_N_THRESHOLD_PARAM.getCode();

    /** 子场景操作类型 */
    public interface SubtreasuryOperateType {
        public static String OPERATE_TYPE_ADD = "ADD";
        public static String OPERATE_TYPE_DELETE = "DELETE";
        public static String OPERATE_TYPE_UPDATE = "UPDATE";

        public static List<String> typeList =
            Lists.newArrayList(OPERATE_TYPE_ADD, OPERATE_TYPE_DELETE, OPERATE_TYPE_UPDATE);
    }

}
