/*******************************************************************************
 * 系统名称 ： 后台管理系统 开发部门 ： 山东眼神智能科技有限公司 文件名称 : CardPassResponse
 ******************************************************************************/
package cn.eyecool.server.http.param;

import cn.eyecool.common.utils.StringUtils;
import lombok.Data;

/**
 * 一卡通权限下发 返回数据对象
 *
 * @author zfx
 * @since 2022/12/1 17:03
 **/
@Data
public class CardPassResponse {
    private CardPassResHeader cardPassResHeader;
    private String body;

    public CardPassResponse(CardPassResHeader cardPassResHeader) {
        this.cardPassResHeader = cardPassResHeader;
        this.body = StringUtils.EMPTY;
    }
}
