package cn.eyecool.device.vo;

import java.io.Serializable;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;

/**
 * <p>
 * FaceInfoVO 人脸信息
 * <p>
 *
 * @Author 段存明
 * @Since 2021-02-24
 */
@Data
public class FaceInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @JSONField(name = "ObjectID")
    private Long objectID;
    @JSONField(name = "BoundingBox")
    private List<Integer> boundingBox;
    @JSONField(name = "PosePitch")
    private double posePitch;
    @JSONField(name = "PoseRoll")
    private double poseRoll;
    @JSONField(name = "PoseYaw")
    private double poseYaw;
    @JSONField(name = "Blur")
    private double blur;
    @JSONField(name = "Sex")
    private String sex;
    @JSONField(name = "Age")
    private int age;
    @JSONField(name = "Minority")
    private String minority;
    @JSONField(name = "Temperature")
    private Double temperature;
    @JSONField(name = "TemperatureAlarm")
    private Integer TemperatureAlarm;
    @JSONField(name = "TemperatureResult")
    private String temperatureResult;
    @JSONField(name = "Mouthocc")
    private Integer mouthocc;

}
