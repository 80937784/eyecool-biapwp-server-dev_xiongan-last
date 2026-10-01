package cn.eyecool.basedata.service;

import java.io.InputStream;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import cn.eyecool.basedata.domain.BasePersonIris;
import cn.eyecool.common.core.domain.AjaxResult;

/**
 * 虹膜图像信息Service接口
 * 
 * @author mawj
 * @date 2021-01-27
 */
public interface IBasePersonIrisService {
    /**
     * 查询虹膜图像信息
     * 
     * @param id 虹膜图像信息ID
     * @return 虹膜图像信息
     */
    public BasePersonIris selectBasePersonIrisById(String id);

    /**
     * 查询虹膜图像信息列表
     * 
     * @param basePersonIris 虹膜图像信息
     * @return 虹膜图像信息集合
     */
    public List<BasePersonIris> selectBasePersonIrisList(BasePersonIris basePersonIris);

    /**
     * 新增虹膜图像信息
     * 
     * @param basePersonIris 虹膜图像信息
     * @return 结果
     */
    public int insertBasePersonIris(BasePersonIris basePersonIris);

    /**
     * 修改虹膜图像信息(强制更新)
     * 
     * @param basePersonIris 虹膜图像信息
     * @return 结果
     */
    public int updateBasePersonIris(BasePersonIris basePersonIris);

    /**
     * 批量删除虹膜图像信息
     * 
     * @param ids 需要删除的虹膜图像信息ID
     * @return 结果
     */
    public int deleteBasePersonIrisByIds(String[] ids);

    /**
     * 删除虹膜图像信息信息
     * 
     * @param id 虹膜图像信息ID
     * @return 结果
     */
    public int deleteBasePersonIrisById(String id);

    /**
     * 更新虹膜特征
     * 
     * @param iris
     */
    public void updateIrisFeature(BasePersonIris iris);

    /**
     * 一键更新所有虹膜特征
     * 
     * @param algsVersion 算法版本
     * @return
     */
    public AjaxResult batchUpdateIrisFeature(String algsVersion);

    /**
     * 下载虹膜图像
     * 
     * @param basePersonIris
     * @return
     */
    public String downloadImages(BasePersonIris basePersonIris);

    /**
     * 保存批量导入人员
     * 
     * @param zipFile
     * @return
     */
    public String saveImportData(MultipartFile zipFile);

    /**
     * 保存批量导入人员
     * 
     * @param zipFileIStream 图片zip压缩文件数据流
     * @return
     */
    public String saveImportData(InputStream zipFileIStream);

    /**
     * 保存上传虹膜数据到关系库和datamanager
     * 
     * @param destIris 虹膜图片信息
     * @param personName 人员姓名
     * @param isUpdate 是否更新（true: 更新 false：新增）
     */
    public void saveUploadIris(BasePersonIris destIris, String personName, boolean isUpdate);

    /**
     * 校验人员是否有虹膜数据
     * 
     * @param personId
     * @return
     */
    public boolean checkPersonHasIris(String personId);

}
