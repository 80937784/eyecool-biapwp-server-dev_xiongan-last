package cn.eyecool.common.enums;


import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.MessageUtils;

/**
 * @Author Administrator
 * @create 2021/10/24 12:25
 */
public enum PersonTypeEnum {
    /**
     * USER,用户
     */
    USER("user"),
    /**
     * VISITOR，访客
     */
    VISITOR("visitor");

    private final String value;

    PersonTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

    public static PersonTypeEnum parse(String value) {
        for (PersonTypeEnum item : PersonTypeEnum.values()) {
            if (item.value().equals(value)) {
                return item;
            }
        }
        throw new CustomException(MessageUtils.message("person.type.enum.not.found.type"));
    }

}
