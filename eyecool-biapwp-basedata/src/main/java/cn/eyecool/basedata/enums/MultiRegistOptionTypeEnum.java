package cn.eyecool.basedata.enums;

import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.MessageUtils;

/**
 * 多模态注册操作类型枚举
 * 
 * @author mawj
 * @date 2021/04/28
 */
public enum MultiRegistOptionTypeEnum {
    /**
     * 成功
     */
    ADD("add"),
    /**
     * 警告
     */
    UPDATE("update"),
    /**
     * 错误
     */
    DELETE("delete");

    private final String value;

    MultiRegistOptionTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

    public static MultiRegistOptionTypeEnum parse(String value) {
        for (MultiRegistOptionTypeEnum item : MultiRegistOptionTypeEnum.values()) {
            if (item.value().equals(value)) {
                return item;
            }
        }
        throw new CustomException(MessageUtils.message("match.action.type.failed"));
    }
}