package cn.eyecool.common.enums;

import cn.eyecool.common.utils.MessageUtils;

/**
 * 用户状态
 * 
 * @author admin
 */
public enum UserStatus
{
    OK("0", "正常"), DISABLE("1", "停用"), DELETED("2", "删除");

    private final String code;
    private String info;

    static {
        OK.info=MessageUtils.message("user.status.normal");
        DISABLE.info=MessageUtils.message("user.status.disabled");
        DELETED.info=MessageUtils.message("user.status.deleted");
    }
    UserStatus(String code, String info)
    {
        this.code = code;
        this.info = info;
    }

    public String getCode()
    {
        return code;
    }

    public String getInfo()
    {
        return info;
    }
}
