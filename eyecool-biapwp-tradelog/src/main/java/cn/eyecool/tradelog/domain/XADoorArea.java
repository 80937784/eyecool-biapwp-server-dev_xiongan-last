/*******************************************************************************
 * 系统名称 ： 后台管理系统 开发部门 ： 山东眼神智能科技有限公司 文件名称 : XADoorArea
 ******************************************************************************/
package cn.eyecool.tradelog.domain;

import lombok.Data;

/**
 * 权限状态回传 area对象
 *
 * @author zfx
 * @since 2023/4/17 10:09
 **/
@Data
public class XADoorArea {
    /** 门点编码（门锁级别） */
    private String areaCode;
    /** 门禁权限下发状态[2-下发成功、3-下发失败] */
    private String state;
    /** 失败描述 */
    private String errMessage;

    public XADoorArea(String areaCode) {
        this.areaCode = areaCode;
        this.state = "2";
        this.errMessage = "操作成功";
    }
}
