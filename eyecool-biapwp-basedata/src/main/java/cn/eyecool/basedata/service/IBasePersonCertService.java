package cn.eyecool.basedata.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import cn.eyecool.basedata.domain.BasePersonCert;

/**
 * 人员证件信息Service接口
 * 
 * @author mawj
 * @date 2021-01-27
 */
public interface IBasePersonCertService {
    /**
     * 查询人员证件信息
     * 
     * @param id 人员证件信息ID
     * @return 人员证件信息
     */
    public BasePersonCert selectBasePersonCertById(String id);

    /**
     * 查询人员证件信息列表
     * 
     * @param basePersonCert 人员证件信息
     * @return 人员证件信息集合
     */
    public List<BasePersonCert> selectBasePersonCertList(BasePersonCert basePersonCert);

    /**
     * 新增人员证件信息
     * 
     * @param basePersonCert 人员证件信息
     * @return 结果
     */
    public int insertBasePersonCert(BasePersonCert basePersonCert);

    /**
     * 修改人员证件信息
     * 
     * @param basePersonCert 人员证件信息
     * @return 结果
     */
    public int updateBasePersonCert(BasePersonCert basePersonCert);

    /**
     * 批量删除人员证件信息
     * 
     * @param ids 需要删除的人员证件信息ID
     * @return 结果
     */
    public int deleteBasePersonCertByIds(String[] ids);

    /**
     * 删除人员证件信息信息
     * 
     * @param id 人员证件信息ID
     * @return 结果
     */
    public int deleteBasePersonCertById(String id);

    /**
     * 保存批量导入
     * 
     * @param excelFile excel证件数据文件
     * @param zipFile 证件照压缩文件
     * @param photoType 照片类型
     * @param certType 证件类型
     * @param updateSupport 支持覆盖更新
     * @return
     * @throws Exception
     * @throws IOException
     */
    public String saveImportData(MultipartFile excelFile, MultipartFile zipFile, String photoType, String certType,
        Boolean updateSupport) throws IOException, Exception;

    /**
     * 保存批量导入
     * 
     * @param excelIStream excel证件数据文件流
     * @param zipIStream 证件照压缩文件流
     * @param photoType 照片类型
     * @param certType 证件类型
     * @param updateSupport 支持覆盖更新
     * @return
     * @throws Exception
     * @throws IOException
     */
    public String saveImportData(InputStream excelIStream, InputStream zipIStream, String photoType, String certType,
        Boolean updateSupport) throws IOException, Exception;

    /**
     * 下载用户证件图片
     * 
     * @param basePersonCert 证件信息
     * @param photoType 照片类型
     * @return
     */
    public String downloadImages(BasePersonCert basePersonCert, String photoType);

}
