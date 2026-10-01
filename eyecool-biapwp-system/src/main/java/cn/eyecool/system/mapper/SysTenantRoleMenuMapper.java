package cn.eyecool.system.mapper;

import java.util.List;

import com.baomidou.mybatisplus.annotation.SqlParser;

import cn.eyecool.system.domain.SysTenantRoleMenu;

/**
 * 租户角色与菜单关联表 数据层
 * 
 * @author admin
 */
@SuppressWarnings("deprecation")
public interface SysTenantRoleMenuMapper {
    /**
     * 查询菜单使用数量
     * 
     * @param menuId 菜单ID
     * @return 结果
     */
    @SqlParser(filter = true)
    public int checkMenuExistRole(Long menuId);

    /**
     * 通过角色ID删除角色和菜单关联
     * 
     * @param roleId 角色ID
     * @return 结果
     */
    @SqlParser(filter = true)
    public int deleteRoleMenuByRoleId(String roleId);

    /**
     * 批量新增角色菜单信息
     * 
     * @param roleMenuList 角色菜单列表
     * @return 结果
     */
    @SqlParser(filter = true)
    public int batchRoleMenu(List<SysTenantRoleMenu> roleMenuList);
}
