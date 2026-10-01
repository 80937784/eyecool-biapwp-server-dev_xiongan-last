package cn.eyecool.msg.domain;

import com.alibaba.fastjson.JSON;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 钉钉团队(企业)对象 msg_ding_team
 * 
 * @author admin
 * @date 2021-04-15
 */
public class MsgDingTeam extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 团队(企业)CorpId */
    @Excel(name = "msg.ding.team.corpid")
    private String corpId;

    /** 团队(企业)名称 */
    @Excel(name = "msg.ding.team.teamname")
    private String teamName;

    /** 团队(企业)说明 */
    @Excel(name = "msg.ding.team.teamdesc")
    private String teamDes;

    /** 租户ID */
    private String tenantId;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setCorpId(String corpId) {
        this.corpId = corpId;
    }

    public String getCorpId() {
        return corpId;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamDes(String teamDes) {
        this.teamDes = teamDes;
    }

    public String getTeamDes() {
        return teamDes;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
