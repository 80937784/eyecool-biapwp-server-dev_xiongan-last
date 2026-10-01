package cn.eyecool.basedata.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.basedata.domain.BasePersonPutInfo;
import cn.eyecool.common.core.domain.AjaxResult;

/**
 * 人员基础信息Service接口
 *
 * @author mawj
 * @date 2021-01-27
 */
public interface IBasePersonInfoService {
    /**
     * 查询人员基础信息
     *
     * @param id 人员基础信息ID
     * @return 人员基础信息
     */
    public BasePersonInfo selectBasePersonInfoById(String id);

    /**
     * 查询人员基础信息列表
     *
     * @param basePersonInfo 人员基础信息
     * @return 人员基础信息集合
     */
    public List<BasePersonInfo> selectBasePersonInfoList(BasePersonInfo basePersonInfo);

    /**
     * 新增人员基础信息
     *
     * @param basePersonInfo 人员基础信息
     * @return 结果
     */
    public int insertBasePersonInfo(BasePersonPutInfo basePersonInfo);

    /**
     * 修改人员基础信息
     *
     * @param basePersonInfo 人员基础信息
     * @return 结果
     */
    public int updateBasePersonInfo(BasePersonPutInfo basePersonInfo);

    /**
     * 批量删除人员基础信息
     *
     * @param ids 需要删除的人员基础信息ID
     * @return 结果
     */
    public int deleteBasePersonInfoByIds(String[] ids);

    /**
     * 删除人员基础信息信息
     *
     * @param id 人员基础信息ID
     * @return 结果
     */
    public int deleteBasePersonInfoById(String id);

    /**
     * 保存批量导入人员
     *
     * @param excelFile 数据文件
     * @param isUpdateSupport 是否覆盖更新
     * @return
     * @throws IOException
     * @throws Exception
     */
    public String saveImportData(MultipartFile excelFile, Boolean isUpdateSupport) throws IOException, Exception;

    /**
     * 保存批量导入人员
     *
     * @param inputStream excel文件数据流
     * @param isUpdateSupport 是否覆盖更新
     * @return
     * @throws IOException
     * @throws Exception
     */
    public String saveImportData(InputStream inputStream, Boolean updateSupport) throws IOException, Exception;

    /**
     * 同步人员数据到Datamanager
     *
     * @param basePersonInfo 人员基础信息
     * @return
     */
    public AjaxResult syncdata(BasePersonInfo basePersonInfo);

    /**
     * 新增Http接口数据源人员信息
     *
     * @param basePersonInfo
     * @return
     */
    public int insertHttpBasePersonInfo(BasePersonPutInfo basePersonInfo);

    /**
     * 修改Http接口数据源人员信息
     *
     * @param basePersonInfo
     * @return
     */
    public int updateHttpBasePersonInfo(BasePersonPutInfo basePersonInfo);

    /**
     * 查询人员数量
     *
     * @param basePersonInfo
     * @return
     */
    public int selectBasePersonCount(BasePersonInfo basePersonInfo);

    /**
     * 根据姓名或者Uid模糊查询
     *
     * @param basePersonInfo
     * @return
     */
    public List<BasePersonInfo> selectByUidOrName(BasePersonInfo basePersonInfo);

    /**
     * 根据人员标识修改标志（黑白名单）
     *
     * @param uniqueIds
     * @param flag
     */
    public int updatePersonFlagByUniqueIds(String uniqueIds, String flag);

    /**
     * 新增人员基础信息
     * 
     * @param personInfo
     * @return
     */
    int insertOnlyPersonInfo(BasePersonInfo personInfo);

    /**
     * 修改人员基础信息
     * 
     * @param personInfo
     * @return
     */
    int updateOnlyPersonInfo(BasePersonInfo personInfo);

    /**
     * 启用/停用 人员
     */
    int isStopAndEnable(String ids, String type);
}
