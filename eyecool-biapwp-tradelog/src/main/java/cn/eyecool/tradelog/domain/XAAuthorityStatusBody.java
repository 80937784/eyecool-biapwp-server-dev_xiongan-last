/*******************************************************************************
 * 系统名称 ： 后台管理系统 开发部门 ： 山东眼神智能科技有限公司 文件名称 : XAAuthorityStatusBody
 ******************************************************************************/
package cn.eyecool.tradelog.domain;

import java.util.List;

import lombok.Data;

/**
 * 一卡通 权限状态回传报文body
 *
 * @author zfx
 * @since 2023/4/17 10:04
 **/
@Data
public class XAAuthorityStatusBody {
    /** 卡号 */
    private String cardNo;
    /** 门点编码（门锁级别 */
    private List<XADoorArea> areaList;
}
