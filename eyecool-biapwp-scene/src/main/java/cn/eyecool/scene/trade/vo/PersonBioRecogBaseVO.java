package cn.eyecool.scene.trade.vo;

import java.io.Serializable;
import java.util.List;

/**
 * 生物特征识别(1:N)VO
 * 
 * @author admin
 * @date 2019年11月14日
 */
public class PersonBioRecogBaseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 唯一标识 */
    private String uniqueId;
    /** 人员姓名 */
    private String name;
    /** 部门名称 */
    private String deptName;
    /** 搜索比对得分 */
    private String score;
    /** 1-N校验方式 */
    private String searchNType;
    /** 场景信息 */
    private List<PersonBioRecogSceneVO> scenesInfo;

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getSearchNType() {
        return searchNType;
    }

    public void setSearchNType(String searchNType) {
        this.searchNType = searchNType;
    }

    public List<PersonBioRecogSceneVO> getScenesInfo() {
        return scenesInfo;
    }

    public void setScenesInfo(List<PersonBioRecogSceneVO> scenesInfo) {
        this.scenesInfo = scenesInfo;
    }

}
