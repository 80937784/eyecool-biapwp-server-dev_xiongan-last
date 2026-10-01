package cn.eyecool.common.core.domain.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.annotation.Excel.ColumnType;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 字典类型表 sys_dict_type
 * 
 * @author admin
 */
public class SysDictType extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 字典主键 */
    @Excel(name = "sys.dict.type.dictid", cellType = ColumnType.NUMERIC)
    private Long dictId;

    /** 字典名称 */
    @Excel(name = "sys.dict.type.dictname")
    private String dictName;

    /** 字典类型 */
    @Excel(name = "sys.dict.type.dicttype")
    private String dictType;

    /** 状态（0正常 1停用） */
    @Excel(name = "sys.dict.type.status", readConverterExp = "0=sys.dict.type.status.normal,1=sys.dict.type.status.disabled")
    private String status;

    public Long getDictId()
    {
        return dictId;
    }

    public void setDictId(Long dictId)
    {
        this.dictId = dictId;
    }

    @NotBlank(message = "sys.dict.type.name.empty")
    @Size(min = 0, max = 100, message = "sys.dict.type.name.max.length.limit")
    public String getDictName()
    {
        return dictName;
    }

    public void setDictName(String dictName)
    {
        this.dictName = dictName;
    }

    @NotBlank(message = "sys.dict.type.dicttype.empty")
    @Size(min = 0, max = 100, message = "sys.dict.type.dicttype.max.length.limit")
    public String getDictType()
    {
        return dictType;
    }

    public void setDictType(String dictType)
    {
        this.dictType = dictType;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("dictId", getDictId())
            .append("dictName", getDictName())
            .append("dictType", getDictType())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
