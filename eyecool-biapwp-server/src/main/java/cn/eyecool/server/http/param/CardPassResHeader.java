/*******************************************************************************
 * 系统名称 ： 后台管理系统 开发部门 ： 山东眼神智能科技有限公司 文件名称 : CardPassResHeader
 ******************************************************************************/
package cn.eyecool.server.http.param;

import lombok.Data;

/**
 * 一卡通调用返回报文对象header
 *
 * @author zfx
 * @since 2022/12/1 16:45
 **/
@Data
public class CardPassResHeader {
    private String commandID;
    private String openId;
    private String transactionID;
    private String timestamp;
    private String rspCode;
    private String rspMessage;
    private String mode;

    public CardPassResHeader() {}

    public CardPassResHeader(String rspCode, String rspMessage) {
        this.rspCode = rspCode;
        this.rspMessage = rspMessage;
    }
}
