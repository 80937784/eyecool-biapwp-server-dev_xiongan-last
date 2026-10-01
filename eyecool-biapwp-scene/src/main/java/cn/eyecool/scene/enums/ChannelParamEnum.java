package cn.eyecool.scene.enums;

import java.util.List;
import java.util.Map;

import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;

import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;

/**
 * 场景参数说明枚举
 * 
 * @author admin
 * @date 2019年12月11日
 */
public enum ChannelParamEnum {

    FACE_COMPARE_CHECK_LIVE_PARAM("face.compare.checklive", "人脸1:1比对是否检活", "人脸1-1是否执行检活，参数值为[Y/N]之一，严格区分大小写",
        DictConstants.BioAttestType.FACE, Lists.newArrayList("Y", "N")),

    FACE_SEARCHN_CHECK_LIVE_PARAM("face.searchN.checklive", "人脸1:N搜索是否检活", "人脸1-N是否执行检活，参数值为[Y/N]之一，严格区分大小写",
        DictConstants.BioAttestType.FACE, Lists.newArrayList("Y", "N")),

    FACE_COMPARE_CHECK_LIVE_THRESHOLD_PARAM("face.compare.checklive.threshold", "人脸1:1比对静默检活阈值",
        "人脸1-1执行静默检活时的阈值，参数值必须是数字", DictConstants.BioAttestType.FACE, null),

    FACE_SEARCHN_CHECK_LIVE_THRESHOLD_PARAM("face.searchN.checklive.threshold", "人脸1:N搜索静默检活阈值",
        "人脸1-N执行静默检活时的阈值，参数值必须是数字", DictConstants.BioAttestType.FACE, null),

    FACE_COMPARE_VIDEO_CHECK_LIVE_THRESHOLD_PARAM("face.compare.video.checklive.threshold", "人脸1:1比对视频检活阈值",
        "人脸1-1执行视频检活时的阈值，参数值必须是数字", DictConstants.BioAttestType.FACE, null),

    FACE_SEARCHN_VIDEO_CHECK_LIVE_THRESHOLD_PARAM("face.searchN.video.checklive.threshold", "人脸1:N搜索视频检活阈值",
        "人脸1-N执行视频检活时的阈值，参数值必须是数字", DictConstants.BioAttestType.FACE, null),

    FACE_COMPARE_THRESHOLD_PARAM("face.compare.threshold", "人脸1:1比对阈值", "人脸1-1比对时的阈值，参数值必须是数字",
        DictConstants.BioAttestType.FACE, null),

    FACE_COMPARE_UNIQUE_ID_VALIDATE_PARAM("face.compare.uniqueId.validate", "人脸1:1比对是否校验人员合法性",
        "人脸1-1比对是否校验人员合法性及当前场景下是否开通人脸，参数值为[Y/N]之一，严格区分大小写", DictConstants.BioAttestType.FACE,
        Lists.newArrayList("Y", "N")),

    FACE_SEARCH_N_THRESHOLD_PARAM("face.searchN.threshold", "人脸1:N搜索阈值", "人脸1-N搜索时的阈值，参数值必须是数字",
        DictConstants.BioAttestType.FACE, null),

    FINGER_COMPARE_THRESHOLD_PARAM("finger.compare.threshold", "指纹1:1比对阈值", "指纹1-1比对时的阈值，参数值必须是数字",
        DictConstants.BioAttestType.FINGER, null),

    FINGER_SEARCH_N_THRESHOLD_PARAM("finger.searchN.threshold", "指纹1:N搜索阈值", "指纹1-N搜索时的阈值，参数值必须是数字",
        DictConstants.BioAttestType.FINGER, null),

    IRIS_COMPARE_THRESHOLD_PARAM("iris.compare.threshold", "虹膜1:1比对阈值", "虹膜1-1比对时的阈值，参数值必须是数字",
        DictConstants.BioAttestType.IRIS, null),

    IRIS_SEARCH_N_THRESHOLD_PARAM("iris.searchN.threshold", "虹膜1:N搜索阈值", "虹膜1-N搜索时的阈值，参数值必须是数字",
        DictConstants.BioAttestType.IRIS, null);

    static {
        FACE_COMPARE_CHECK_LIVE_PARAM.name=MessageUtils.message("channel.param.enum.face.compare.checklive.name");
        FACE_COMPARE_CHECK_LIVE_PARAM.desc =MessageUtils.message("channel.param.enum.face.compare.checklive.description");
        FACE_SEARCHN_CHECK_LIVE_PARAM.name=MessageUtils.message("channel.param.enum.face.searchN.checklive.name");
        FACE_SEARCHN_CHECK_LIVE_PARAM.desc=MessageUtils.message("channel.param.enum.face.searchN.checklive.description");
        FACE_COMPARE_CHECK_LIVE_THRESHOLD_PARAM.name=MessageUtils.message("channel.param.enum.face.compare.checklive.threshold.name");
        FACE_COMPARE_CHECK_LIVE_THRESHOLD_PARAM.desc=MessageUtils.message("channel.param.enum.face.compare.checklive.threshold.description");
        FACE_SEARCHN_CHECK_LIVE_THRESHOLD_PARAM.name=MessageUtils.message("channel.param.enum.face.searchN.checklive.threshold.name");
        FACE_SEARCHN_CHECK_LIVE_THRESHOLD_PARAM.desc=MessageUtils.message("channel.param.enum.face.searchN.checklive.threshold.description");
        FACE_COMPARE_VIDEO_CHECK_LIVE_THRESHOLD_PARAM.name=MessageUtils.message("channel.param.enum.face.compare.video.checklive.threshold.name");
        FACE_COMPARE_VIDEO_CHECK_LIVE_THRESHOLD_PARAM.desc=MessageUtils.message("channel.param.enum.face.compare.video.checklive.threshold.description");
        FACE_SEARCHN_VIDEO_CHECK_LIVE_THRESHOLD_PARAM.name=MessageUtils.message("channel.param.enum.face.searchN.video.checklive.threshold.name");
        FACE_SEARCHN_VIDEO_CHECK_LIVE_THRESHOLD_PARAM.desc=MessageUtils.message("channel.param.enum.face.searchN.video.checklive.threshold.description");

        FACE_COMPARE_THRESHOLD_PARAM.name=MessageUtils.message("channel.param.enum.face.compare.threshold.name");
        FACE_COMPARE_THRESHOLD_PARAM.desc=MessageUtils.message("channel.param.enum.face.compare.threshold.description");

        FACE_COMPARE_UNIQUE_ID_VALIDATE_PARAM.name=MessageUtils.message("channel.param.enum.face.compare.uniqueId.validate.name");
        FACE_COMPARE_UNIQUE_ID_VALIDATE_PARAM.desc=MessageUtils.message("channel.param.enum.face.compare.uniqueId.validate.description");

        FACE_SEARCH_N_THRESHOLD_PARAM.name=MessageUtils.message("channel.param.enum.face.searchN.threshold.name");
        FACE_SEARCH_N_THRESHOLD_PARAM.desc=MessageUtils.message("channel.param.enum.face.searchN.threshold.description");

        FINGER_COMPARE_THRESHOLD_PARAM.name=MessageUtils.message("channel.param.enum.finger.compare.threshold.name");
        FINGER_COMPARE_THRESHOLD_PARAM.desc=MessageUtils.message("channel.param.enum.finger.compare.threshold.description");
        FINGER_SEARCH_N_THRESHOLD_PARAM.name = MessageUtils.message("channel.param.enum.finger.searchN.threshold.name");
        FINGER_SEARCH_N_THRESHOLD_PARAM.desc = MessageUtils.message("channel.param.enum.finger.searchN.threshold.description");

        IRIS_COMPARE_THRESHOLD_PARAM.name= MessageUtils.message("channel.param.enum.iris.compare.threshold.name");
        IRIS_COMPARE_THRESHOLD_PARAM.desc= MessageUtils.message("channel.param.enum.iris.compare.threshold.description");

        IRIS_SEARCH_N_THRESHOLD_PARAM.name=MessageUtils.message("channel.param.enum.iris.searchN.threshold.name");
        IRIS_SEARCH_N_THRESHOLD_PARAM.desc=MessageUtils.message("channel.param.enum.iris.searchN.threshold.description");

    }
    /** 参数编码 */
    private final String code;
    /** 参数名称 */
    private  String name;
    /** 参数说明 */
    private  String desc;
    /** 认证类型 */
    private final String bioAttestType;
    /** 可选值 */
    private final List<String> valList;

    private ChannelParamEnum(String code, String name, String desc, String bioAttestType, List<String> valList) {
        this.code = code;
        this.name = name;
        this.desc = desc;
        this.bioAttestType = bioAttestType;
        this.valList = valList;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public String getBioAttestType() {
        return bioAttestType;
    }

    public List<String> getValList() {
        return valList;
    }

    public static List<Map<String, Object>> list() {
        List<Map<String, Object>> list = Lists.newArrayList();
        for (ChannelParamEnum it : ChannelParamEnum.values()) {
            Map<String, Object> map = Maps.newHashMap();
            map.put("code", it.getCode());
            map.put("name", it.getName());
            map.put("desc", it.getDesc());
            map.put("bioAttestType", it.getBioAttestType());
            map.put("valList", it.getValList());
            list.add(map);
        }
        return list;
    }

    public static ChannelParamEnum parse(String code) {
        if (StringUtils.isBlank(code)) {
            throw new CustomException("参数编码不能为空");
        }
        for (ChannelParamEnum it : ChannelParamEnum.values()) {
            if (it.getCode().equals(code)) {
                return it;
            }
        }
        throw new CustomException("无法匹配的参数编码:" + code);
    }

}
