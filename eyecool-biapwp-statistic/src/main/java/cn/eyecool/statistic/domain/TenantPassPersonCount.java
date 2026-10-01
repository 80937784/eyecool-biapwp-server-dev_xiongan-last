package cn.eyecool.statistic.domain;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * 租户通行人员数量信息
 * 
 * @author mawj
 * @date 2021/08/31
 */
@Getter
@Setter
public class TenantPassPersonCount implements Serializable {

    private static final long serialVersionUID = 1L;

    private String tenantId;

    private Integer passNum;

}
