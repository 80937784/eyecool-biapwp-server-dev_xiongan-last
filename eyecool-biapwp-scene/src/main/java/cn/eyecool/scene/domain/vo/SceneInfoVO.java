package cn.eyecool.scene.domain.vo;

import java.util.ArrayList;
import java.util.List;

import cn.eyecool.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 场景信息视图对象
 * 
 * @author mawj
 * @date 2021/08/05
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SceneInfoVO extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 父节点ID(子场景才有) */
    private String parentId;

    /** 场景编码 */
    private String sceneCode;

    /** 场景名称 */
    private String sceneName;

    /** 开通人脸(数据字典:0-不开通 1-开通) */
    private String faceMode;

    /** 开通指纹(数据字典:0-不开通 1-开通) */
    private String fingerMode;

    /** 开通虹膜(数据字典:0-不开通 1-开通) */
    private String irisMode;

    /** 开通指静脉(数据字典:0-不开通 1-开通) */
    private String fveinMode;

    /** 开通人脸虹膜多模态(数据字典:0-不开通 1-开通) */
    private String faceIrisMode;

    /** 1-N识别方式(数据字典:1-校验子场景、2-校验场景库、3-校验全库, 4-依次校验) */
    private String searchN;

    /** 租户ID */
    private String tenantId;

    /** 子场景 */
    private List<SceneInfoVO> children = new ArrayList<SceneInfoVO>();

}
