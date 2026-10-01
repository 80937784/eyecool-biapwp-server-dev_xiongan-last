package cn.eyecool.tool.mapper;

import java.util.List;
import cn.eyecool.tool.domain.SysSdkFile;

/**
 * SDK文件上传Mapper接口
 *
 * @author admin
 * @date 2021-03-31
 */
public interface SysSdkFileMapper
{
    /**
     * 查询SDK文件上传
     *
     * @param id SDK文件上传ID
     * @return SDK文件上传
     */
    public SysSdkFile selectSysSdkFileById(String id);

    /**
     * 查询SDK文件上传列表
     *
     * @param sysSdkFile SDK文件上传
     * @return SDK文件上传集合
     */
    public List<SysSdkFile> selectSysSdkFileList(SysSdkFile sysSdkFile);

    /**
     * 新增SDK文件上传
     *
     * @param sysSdkFile SDK文件上传
     * @return 结果
     */
    public int insertSysSdkFile(SysSdkFile sysSdkFile);

    /**
     * 修改SDK文件上传
     *
     * @param sysSdkFile SDK文件上传
     * @return 结果
     */
    public int updateSysSdkFile(SysSdkFile sysSdkFile);

    /**
     * 删除SDK文件上传
     *
     * @param id SDK文件上传ID
     * @return 结果
     */
    public int deleteSysSdkFileById(String id);

    /**
     * 批量删除SDK文件上传
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteSysSdkFileByIds(String[] ids);

    public List<SysSdkFile> selectLastSdkUploads();
}