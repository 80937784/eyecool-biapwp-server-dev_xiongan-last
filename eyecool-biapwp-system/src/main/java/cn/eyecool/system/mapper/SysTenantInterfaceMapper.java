package cn.eyecool.system.mapper;

import java.util.List;

import cn.eyecool.common.core.domain.entity.SysDictData;
import cn.eyecool.system.domain.SysTenantInterface;

/**
 * 租户和接口关联Mapper接口
 * 
 * @author admin
 * @date 2021-02-22
 */
public interface SysTenantInterfaceMapper {

    /**
     * 查询租户和接口关联列表
     * 
     * @param sysTenantInterface 租户和接口关联
     * @return 租户和接口关联集合
     */
    public List<SysTenantInterface> selectSysTenantInterfaceList(SysTenantInterface sysTenantInterface);

    /**
     * 新增租户和接口关联
     * 
     * @param sysTenantInterface 租户和接口关联
     * @return 结果
     */
    public int insertSysTenantInterface(SysTenantInterface sysTenantInterface);

    /**
     * 修改租户和接口关联
     * 
     * @param sysTenantInterface 租户和接口关联
     * @return 结果
     */
    public int updateSysTenantInterface(SysTenantInterface sysTenantInterface);

    /**
     * 删除租户和接口关联
     * 
     * @param sysTenantInterface
     * @return 结果
     */
    public int deleteSysTenantInterface(SysTenantInterface sysTenantInterface);

    /**
     * 查询未分配的接口列表
     * 
     * @param dictData
     * @return
     */
    public List<SysDictData> selectUnallocatedInterfaceList(SysDictData dictData);

}
