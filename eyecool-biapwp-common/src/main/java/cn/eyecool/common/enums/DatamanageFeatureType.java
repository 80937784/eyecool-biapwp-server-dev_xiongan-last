package cn.eyecool.common.enums;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;

/**
 * datamanager特征类型枚举
 * 
 * @author admin
 * @date 2019年11月20日
 */
public enum DatamanageFeatureType {
    
    OTHER_UNKNOWN(0, "其他Unknown", "若含有多种生物特征 不要用Unknown", null),

    FACE(300, "300", "FaceFeature", null),

    IRIS_UNKNOWN(100, "虹膜Unknown", "IrisFeatureUnknown", null),

    IRIS_LEFT(101, "左眼", "IrisFeatureLeft", Arrays.asList("L")),

    IRIS_RIGHT(102, "右眼", "IrisFeatureRight", Arrays.asList("R")),

    IRIS_ALL(103, "两个眼", "IrisFeatureAll", null),

    FINGER_UNKNOWN(200, "指纹Unknown", "FingerFeatureUnknown", Arrays.asList("97", "98", "99")),

    FINGER_LEFT_THUMB(201, "左大拇指", "FingerFeatureLeftThumb", Arrays.asList("16")),

    FINGER_LEFT_INDEX(202, "左食指", "FingerFeatureLeftIndex", Arrays.asList("17")),

    FINGER_LEFT_MIDDLE(203, "左中指", "FingerFeatureLeftMiddle", Arrays.asList("18")),

    FINGER_LEFT_RING(204, "左无名指", "FingerFeatureLeftRing", Arrays.asList("19")),

    FINGER_LEFT_LITTLE(205, "左小拇指", "FingerFeatureLeftLittle", Arrays.asList("20")),

    FINGER_RIGHY_THUMB(206, "右大拇指", "FingerFeatureRightThumb", Arrays.asList("11")),

    FINGER_RIGHY_INDEX(207, "右食指", "FingerFeatureRightIndex", Arrays.asList("12")),

    FINGER_RIGHY_MIDDLE(208, "右中指", "FingerFeatureRightMiddle", Arrays.asList("13")),

    FINGER_RIGHY_RING(209, "右无名指", "FingerFeatureRightRing", Arrays.asList("14")),

    FINGER_RIGHY_LITTLE(210, "右小拇指", "FingerFeatureRightLittle", Arrays.asList("15"));


    /** 特征码 */
    private final Integer code;
    /** 特征类型名称 */
    private String name;
    /** 说明 */
    private String desc;
    /** 对应平台字典编码 */
    private final List<String> dictCodes;
    
    static {
        OTHER_UNKNOWN.setName(MessageUtils.message("data.feature.type.other.unknown"));
        OTHER_UNKNOWN.setDesc(MessageUtils.message("data.feature.type.other.unknown.msg"));
        IRIS_UNKNOWN.setName(MessageUtils.message("data.feature.type.iris.unknown"));
        IRIS_LEFT.setName(MessageUtils.message("data.feature.type.left.eye"));
        IRIS_RIGHT.setName(MessageUtils.message("data.feature.type.right.eye"));
        IRIS_ALL.setName(MessageUtils.message("data.feature.type.two.eye"));
        FINGER_UNKNOWN.setName(MessageUtils.message("data.feature.type.finger.unknown"));
        FINGER_LEFT_THUMB.setName(MessageUtils.message("data.feature.type.left.thumb"));
        FINGER_LEFT_INDEX.setName(MessageUtils.message("data.feature.type.left.index"));
        FINGER_LEFT_MIDDLE.setName(MessageUtils.message("data.feature.type.left.middle"));
        FINGER_LEFT_RING.setName(MessageUtils.message("data.feature.type.left.ring"));
        FINGER_LEFT_LITTLE.setName(MessageUtils.message("data.feature.type.left.little"));
        FINGER_RIGHY_THUMB.setName(MessageUtils.message("data.feature.type.right.thumb"));
        FINGER_RIGHY_INDEX.setName(MessageUtils.message("data.feature.type.right.index"));
        FINGER_RIGHY_MIDDLE.setName(MessageUtils.message("data.feature.type.right.middle"));
        FINGER_RIGHY_RING.setName(MessageUtils.message("data.feature.type.right.ring"));
        FINGER_RIGHY_LITTLE.setName(MessageUtils.message("data.feature.type.right.little"));
    }

    private DatamanageFeatureType(Integer code, String name, String desc, List<String> dictCodes) {
        this.code = code;
        this.name = name;
        this.desc = desc;
        this.dictCodes = dictCodes;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }
    
    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<String> getDictCodes() {
        return dictCodes;
    }

    public static DatamanageFeatureType parse(String dictCode) {
        if (StringUtils.isBlank(dictCode)) {
            throw new CustomException(MessageUtils.message("data.feature.type.dictcode.empty"));
        }
        for (DatamanageFeatureType type : DatamanageFeatureType.values()) {
            List<String> dictCodes = type.getDictCodes();
            if (CollectionUtils.isNotEmpty(dictCodes) && dictCodes.contains(dictCode)) {
                return type;
            }
        }
        throw new CustomException(MessageUtils.message("data.feature.type.not.found.type"));
    }
}
