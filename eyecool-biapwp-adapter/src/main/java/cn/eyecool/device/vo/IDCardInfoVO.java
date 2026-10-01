package cn.eyecool.device.vo;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * <p>
 * IDCardInfoVO 卡信息
 * <p>
 *
 * @Author 段存明
 * @Since 2021-02-24
 */
@Data
public class IDCardInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String sex;
    private String nation;
    private String number;
    private String address;
    private String office;
    private String validTimeStart;
    private String validTimeStop;
    @JsonProperty(value = "ProfilePic")
    private String profilePic;

    /**
     * formate
     *
     * @param str
     * @return
     */
    public static String formate(String str) {
        String result = str;
        if (str == null) {
            return result;
        }
        result = result.replaceAll("ProfilePic", "profilePic");
        return result;
    }
}
