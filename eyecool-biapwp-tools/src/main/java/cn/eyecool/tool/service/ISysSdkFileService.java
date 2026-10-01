package cn.eyecool.tool.service;

import java.io.IOException;
import java.util.List;
import cn.eyecool.tool.domain.SysSdkFile;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * SDK文件上传Service接口
 *
 * @author admin
 * @date 2021-03-31
 */
public interface ISysSdkFileService
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
     * 批量删除SDK文件上传
     *
     * @param ids 需要删除的SDK文件上传ID
     * @return 结果
     */
    public int deleteSysSdkFileByIds(String[] ids);

    /**
     * 删除SDK文件上传信息
     *
     * @param id SDK文件上传ID
     * @return 结果
     */
    public int deleteSysSdkFileById(String id);

    public int addFileForm(MultipartFile sdkFile,SysSdkFile sysSdkFile);

    public void sdkDownload(String id, HttpServletRequest request,  HttpServletResponse response) throws IOException;

    public List<SysSdkFile> selectLastSdkUploads();
}