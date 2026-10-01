/*******************************************************************************
 * 系统名称 ： 后台管理系统 开发部门 ： 山东眼神智能科技有限公司 文件名称 : CardPassPerson
 ******************************************************************************/
package cn.eyecool.server.http.param;

import java.util.List;

import lombok.Data;

/**
 * 一卡通请求数据对象
 *
 * @author zfx
 * @since 2022/11/28 10:06
 **/
@Data
public class CardPassPerson {
    /** 一卡通卡号 */
    private String cardNo;
    /** 卡状态 卡片状态[1-开通，2-注销，3-挂失，4-解挂] */
    private String state;
    /** 权限开始时间(yyyy-MM-dd hh:mm:ss) */
    private String startDate;
    /** 权限结束时间(yyyy-MM-dd hh:mm:ss) */
    private String endDate;
    /** 时段拓展字段，所有门锁统一时段设置 */
    private String interval;
    /** 人员编号 */
    private String personNo;
    /** 人员姓名 */
    private String fullName;
    /** 门点编码集合 集合内为门点编码（门锁级别） */
    private List<DoorArea> areaList;

}
