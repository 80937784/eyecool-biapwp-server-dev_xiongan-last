/*******************************************************************************
 * 系统名称 ： 后台管理系统 开发部门 ： 山东眼神智能科技有限公司 文件名称 : XACardPassHeader
 ******************************************************************************/
package cn.eyecool.tradelog.domain;

import lombok.Data;

/**
 * 雄安一卡通回传数据请求头
 *
 * @author zfx
 * @since 2022/11/29 9:55
 **/
@Data
public class XACardPassHeader {
    /**
     * 一卡通系统接口 1001 门禁刷卡记录同步接口 1002 门禁权限状态回传接口 集成商实现接口 1201 门禁权限下发接口
     */
    private String commandID = "1001";
    private String transactionID;
    private String timestamp;
    /** openId(由一卡通系统分配) */
    private String openId;
    /** 1:密文模式 */
    private String mode = "1";
    private String token;
}
