package cn.eyecool.basedata.vo;

import java.util.Date;

import cn.eyecool.basedata.domain.BasePersonInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 访客后台展示信息类
 * 
 * @Author Administrator
 * @create 2021/10/28 11:13
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class BaseVisitorInfoVO extends BasePersonInfo {

    private static final long serialVersionUID = 1L;

    /** 邀请人姓名 **/
    private String inviterName;
    /** 到访时段 **/
    private String visitTimePeriod;
    /** 生效状态 0：未生效 1：生效中 2：已失效 **/
    private String effectiveStatus;
    /** 访客系统-生效开始时间-的查询区间 */
    private Date effectiveBeginTimeStart;
    private Date effectiveBeginTimeEnd;
    /** 访客系统-生效结束时间-的查询区间 */
    private Date effectiveEndTimeStart;
    private Date effectiveEndTimeEnd;

}
