package cn.eyecool.basedata.service;

import cn.eyecool.basedata.domain.BasePersonFace;
import cn.eyecool.basedata.domain.FaceRegister;
import cn.eyecool.common.core.domain.AjaxResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * 人脸图像信息Service接口
 *
 * @author mawj
 * @date 2021-01-27
 */
public interface IBasePersonFaceService {
    /**
     * 查询人脸图像信息
     *
     * @param id 人脸图像信息ID
     * @return 人脸图像信息
     */
    public BasePersonFace selectBasePersonFaceById(String id);

    /**
     * 查询人脸图像信息列表
     *
     * @param basePersonFace 人脸图像信息
     * @return 人脸图像信息集合
     */
    public List<BasePersonFace> selectBasePersonFaceList(BasePersonFace basePersonFace);

    /**
     * 新增人脸图像信息
     *
     * @param basePersonFace 人脸图像信息
     * @return 结果
     */
    public int insertBasePersonFace(BasePersonFace basePersonFace);

    /**
     * 修改人脸图像信息（强制更新，不做任何校验）
     *
     * @param basePersonFace 人脸图像信息
     * @return 结果
     */
    public int updateBasePersonFace(BasePersonFace basePersonFace);

    /**
     * 批量删除人脸图像信息
     *
     * @param ids 需要删除的人脸图像信息ID
     * @return 结果
     */
    public int deleteBasePersonFaceByIds(String[] ids);

    /**
     * 删除人脸图像信息信息
     *
     * @param id 人脸图像信息ID
     * @return 结果
     */
    public int deleteBasePersonFaceById(String id);

    /**
     * 更新人人脸特征
     *
     * @param face 人脸图像信息
     */
    public void updateFaceFeature(BasePersonFace face);

    /**
     * 一键更新人脸特征
     *
     * @param algsVersion 算法版本
     * @return
     */
    public AjaxResult batchUpdateFaceFeature(String algsVersion);

    /**
     * 图片下载
     *
     * @param basePersonFace
     * @param ids
     * @return
     */
    public String downloadImages(BasePersonFace basePersonFace);

    /**
     * 保存批量导入人员
     *
     * @param zipFile 图片zip压缩文件
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
     * 保存上传人脸数据到关系库和datamanager
     *
     * @param destFace   人脸信息
     * @param personName 人员姓名
     * @param isUpdate   是否是更新(true: 更新， false：新增)
     */
    public void saveUploadFace(BasePersonFace destFace, String personName, boolean isUpdate);

    /**
     * 校验人员是否有人脸数据
     *
     * @param personId
     * @return
     */
    public boolean checkPersonHasFace(String personId);

    /**
     * 进行人脸注册
     * @param faceRegister
     * @param channleCode
     * @param primarySubCode
     */
    void faceRegister(FaceRegister faceRegister, String channleCode,String primarySubCode);
}
