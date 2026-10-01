/*******************************************************************************
 * 系统名称 ： 后台管理系统 开发部门 ： 山东眼神智能科技有限公司 文件名称 : MulitLogQueryRequest
 ******************************************************************************/
package cn.eyecool.tradelog.param;

/**
 * 多模态日志查询请求对象
 *
 * @author zfx
 * @since 2021/2/25 11:52
 **/
public class MulitLogQueryRequest {
    /** 分页页码 */
    private String pageNum;
    /** 场景编码 eyecool */
    private String channelCode;
    /** 查询时间范围_开始时间 2020-01-01 00:00:00 */
    private String startTime;
    /** 查询时间范围_结束时间 2020-01-04 00:00:00 */
    private String endTime;
    /** 人员唯一标识 20190001 */
    private String uniqueId;
    /** 结果(1通过，0未通过) */
    private String result;
    /** 设备编码 */
    private String deviceSn;
    /** 设备型号编码 */
    private String deviceModel;

    public String getPageNum() {
        return pageNum;
    }

    public void setPageNum(String pageNum) {
        this.pageNum = pageNum;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getDeviceSn() {
        return deviceSn;
    }

    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

}
