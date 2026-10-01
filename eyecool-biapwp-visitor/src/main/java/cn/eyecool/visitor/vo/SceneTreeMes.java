package cn.eyecool.visitor.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 邀请人所在场景子场景树形信息
 * @Author Administrator
 * @create 2021/10/21 14:57
 */
@Data
public class SceneTreeMes {

    /** 父节点ID(子场景才有) */
    private String parentId;

    /** 场景编码 */
    private String sceneId;

    /** 场景名称 */
    private String sceneName;

    /** 子场景 */
    private List<SceneTreeMes> children = new ArrayList<>();

}
