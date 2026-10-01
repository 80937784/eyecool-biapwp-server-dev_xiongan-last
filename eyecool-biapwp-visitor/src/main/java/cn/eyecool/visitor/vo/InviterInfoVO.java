package cn.eyecool.visitor.vo;

import lombok.Data;

/**
 * 邀请人信息VO
 * @Author Administrator
 * @create 2021/10/21 14:57
 */
@Data
public class InviterInfoVO {

    /**用户名 **/
    private String username;
    /**手机号 **/
    private String phone;
    /**短信验证码 **/
    private String verifyCode;
    /**邀请人所在租户 **/
    private String key;


}
