package cn.eyecool.device.vo;

import java.io.Serializable;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;

/**
 * <p>
 * CommInfoVO 通用信息
 * <p>
 *
 * @Author 段存明
 * @Since 2021-02-24
 */
@Data
public class CommInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @JSONField(name = "MachineAddress")
    private String machineAddress;
    @JSONField(name = "MachineName")
    private String machineName;
    @JSONField(name = "PictureType")
    private String pictureType;
    @JSONField(name = "Resolution")
    private String resolution;
    @JSONField(name = "SerialNo")
    private String serialNo;

}
