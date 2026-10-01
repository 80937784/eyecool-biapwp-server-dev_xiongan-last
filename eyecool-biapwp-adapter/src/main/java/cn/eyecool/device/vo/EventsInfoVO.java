package cn.eyecool.device.vo;

import java.io.Serializable;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;

/**
 * <p>
 * EventsInfoVO 事件信息
 * <p>
 *
 * @Author 段存明
 * @Since 2021-02-24
 */
@Data
public class EventsInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @JSONField(name = "Channel")
    private Integer channel;

    @JSONField(name = "Events")
    private List<DevResultInfoVO> events;

    @JSONField(name = "PicID")
    private String picID;

    @JSONField(name = "SendTimes")
    private Integer sendTimes;

    @JSONField(name = "Transfer")
    private String transfer;

}
