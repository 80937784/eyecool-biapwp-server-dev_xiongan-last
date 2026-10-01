package cn.eyecool.visitor.dto;

import lombok.Data;

/**
 * 数据库查询用户下拥有的子场景封装类
 * @Author Administrator
 * @create 2021/10/22 14:29
 */
@Data
public class UserSubSceneDTO {
    /** 渠道名称 */
    private String channelName;

    /** 渠道id */
    private String channelId;

    /** 子场景名称 */
    private String subName;

    /** 子场景id */
    private String subId;
}
