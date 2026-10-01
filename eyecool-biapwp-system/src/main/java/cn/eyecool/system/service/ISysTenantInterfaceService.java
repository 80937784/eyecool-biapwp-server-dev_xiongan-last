package cn.eyecool.system.service;

import java.util.List;

import cn.eyecool.common.core.domain.entity.SysDictData;
import cn.eyecool.system.domain.SysTenantInterface;

/**
 * 租户和接口关联Service接口
 * 
 * @author admin
 * @date 2021-02-22
 */
public interface ISysTenantInterfaceService {

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
     * 删除租户和接口关联信息
     * 
     * @param sysTenantInterface
     * @return
     */
    public int deleteSysTenantInterface(SysTenantInterface sysTenantInterface);

    /**
     * 新增租户接口授权
     * 
     * @param tenantInterface
     * @return
     */
    public int insertAuthInterfaces(SysTenantInterface tenantInterface);

    /**
     * 查询未分配的接口列表
     * 
     * @param dictData
     * @return
     */
    public List<SysDictData> selectUnallocatedInterfaceList(SysDictData dictData);

    /**
     * 查询租户单个接口权限
     * 
     * @param tenantId
     * @param transcode
     * @return
     */
    public SysTenantInterface selectSysTenantInterface(String tenantId, String transcode);

}
