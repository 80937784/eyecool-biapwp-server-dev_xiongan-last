package cn.eyecool.scene.domain;

import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;

/**
 * 场景人员添加参数
 * 
 * @author mawj
 * @date 2021/03/24
 */
public class ChannelSubBusiParam extends ChannelSubtreasuryBusi {

    private static final long serialVersionUID = 1L;

    /** 部门ID */
    private Long deptId;

    /** 添加类型 */
    private String bindType;

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getBindType() {
        return bindType;
    }

    public void setBindType(String bindType) {
        this.bindType = bindType;
    }

    /**
     * 添加类型枚举
     */
    public enum BindTypeEnum {
        BIND_ALL("1"), BIND_BY_DEPT("2"), BIND_SELF_DEFINE("3");

        private BindTypeEnum(String value) {
            this.value = value;
        }

        private String value;

        public String getValue() {
            return value;
        }

        public static BindTypeEnum parse(String value) {
            if (StringUtils.isBlank(value)) {
                throw new CustomException(MessageUtils.message("channel.param.type.empty"));
            }
            for (BindTypeEnum it : BindTypeEnum.values()) {
                if (it.getValue().equals(value)) {
                    return it;
                }
            }
            throw new CustomException(MessageUtils.message("channel.param.type.unsupported", value));
        }

    }
}
