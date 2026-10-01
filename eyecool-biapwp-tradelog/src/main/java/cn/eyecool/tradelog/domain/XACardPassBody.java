/*******************************************************************************
 * 系统名称 ： 后台管理系统 开发部门 ： 山东眼神智能科技有限公司 文件名称 : XACardPassBody
 ******************************************************************************/
package cn.eyecool.tradelog.domain;

import lombok.Data;

/**
 * 雄安一卡通日志回传对象
 *
 * @author zfx
 * @since 2022/11/29 9:53
 **/
@Data
public class XACardPassBody {
    /** 卡号 */
    private String cardNo;
    /** 开门时间(yyyy-MM-dd hh:mm:ss) */
    private String recordTime;
    /** 门点编码（门锁级别 */
    private String areaCode;
}
