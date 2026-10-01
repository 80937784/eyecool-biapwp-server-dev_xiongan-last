package cn.eyecool.basedata.service;

import java.io.InputStream;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import cn.eyecool.basedata.domain.BasePersonFinger;
import cn.eyecool.common.core.domain.AjaxResult;

/**
 * 指纹图像信息Service接口
 * 
 * @author mawj
 * @date 2021-01-27
 */
public interface IBasePersonFingerService {
    /**
     * 查询指纹图像信息
     * 
     * @param id 指纹图像信息ID
     * @return 指纹图像信息
     */
    public BasePersonFinger selectBasePersonFingerById(String id);

    /**
     * 查询指纹图像信息列表
     * 
     * @param basePersonFinger 指纹图像信息
     * @return 指纹图像信息集合
     */
    public List<BasePersonFinger> selectBasePersonFingerList(BasePersonFinger basePersonFinger);

    /**
     * 新增指纹图像信息
     * 
     * @param basePersonFinger 指纹图像信息
     * @return 结果
     */
    public int insertBasePersonFinger(BasePersonFinger basePersonFinger);

    /**
     * 修改指纹图像信息(强制更新)
     * 
     * @param basePersonFinger 指纹图像信息
     * @return 结果
     */
    public int updateBasePersonFinger(BasePersonFinger basePersonFinger);

    /**
     * 批量删除指纹图像信息
     * 
     * @param ids 需要删除的指纹图像信息ID
     * @return 结果
     */
    public int deleteBasePersonFingerByIds(String[] ids);

    /**
     * 删除指纹图像信息信息
     * 
     * @param id 指纹图像信息ID
     * @return 结果
     */
    public int deleteBasePersonFingerById(String id);

    /**
     * 一键更新所有指纹特征
     * 
     * @param algsVersion 算法版本
     * @return
     */
    public AjaxResult batchUpdateFingerFeature(String algsVersion);

    /**
     * 更新指纹特征
     *
     * @param finger
     * @return
     */
    public void updateFingerFeature(BasePersonFinger finger);

    /**
     * 下载指纹图片
     * 
     * @param basePersonFinger
     * @return
     */
    public String downloadImages(BasePersonFinger basePersonFinger);

    /**
     * 保存批量导入人员
     * 
     * @param zipFile 指纹图片压缩包
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
     * 保存上传指纹数据到关系库和datamanager
     * 
     * @param destFinger 指纹图片信息
     * @param personName 人员姓名
     * @param isUpdate 是否更新（true: 更新 false：新增）
     */
    public void saveUploadFinger(BasePersonFinger destFinger, String personName, boolean isUpdate);

    /**
     * 校验人员是否有指纹数据
     * 
     * @param personId
     * @return
     */
    public boolean checkPersonHasFinger(String personId);

}
