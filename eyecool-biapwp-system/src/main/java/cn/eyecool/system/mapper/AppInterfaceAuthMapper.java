package cn.eyecool.system.mapper;

import java.util.List;
import cn.eyecool.system.domain.AppInterfaceAuth;

/**
 * 应用接口授权Mapper接口
 * 
 * @author admin
 * @date 2021-04-14
 */
public interface AppInterfaceAuthMapper 
{
    /**
     * 查询应用接口授权
     * 
     * @param id 应用接口授权ID
     * @return 应用接口授权
     */
    public AppInterfaceAuth selectAppInterfaceAuthById(String id);

    /**
     * 查询应用接口授权列表
     * 
     * @param appInterfaceAuth 应用接口授权
     * @return 应用接口授权集合
     */
    public List<AppInterfaceAuth> selectAppInterfaceAuthList(AppInterfaceAuth appInterfaceAuth);

    /**
     * 新增应用接口授权
     * 
     * @param appInterfaceAuth 应用接口授权
     * @return 结果
     */
    public int insertAppInterfaceAuth(AppInterfaceAuth appInterfaceAuth);

    /**
     * 修改应用接口授权
     * 
     * @param appInterfaceAuth 应用接口授权
     * @return 结果
     */
    public int updateAppInterfaceAuth(AppInterfaceAuth appInterfaceAuth);

    /**
     * 删除应用接口授权
     * 
     * @param id 应用接口授权ID
     * @return 结果
     */
    public int deleteAppInterfaceAuthById(String id);

    /**
     * 批量删除应用接口授权
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteAppInterfaceAuthByIds(String[] ids);
}
