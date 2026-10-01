package cn.eyecool.system.domain;

import java.io.Serializable;

/**
 * 服务次数查询条件
 * 
 * @author admin 
 * @date 2020年1月9日
 */
public class ServiceCountCondition implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 场景编码*/
    private String channelCode;
    /** 结果（0成功，1失败）*/
    private String result;
    /** 开始时间*/
    private String beginTime;
    /** 结束时间*/
    private String endTime;
    /** 分组条件日期格式*/
    private String groupByDateFormatter;

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getGroupByDateFormatter() {
        return groupByDateFormatter;
    }

    public void setGroupByDateFormatter(String groupByDateFormatter) {
        this.groupByDateFormatter = groupByDateFormatter;
    }

}
