package cn.eyecool.scene.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.scene.domain.ChannelBusiParam;
import cn.eyecool.scene.domain.ChannelBusiness;

/**
 * 场景人员Service接口
 * 
 * @author admin
 * @date 2021-03-22
 */
public interface IChannelBusinessService {
    /**
     * 查询场景人员
     * 
     * @param id 场景人员ID
     * @return 场景人员
     */
    public ChannelBusiness selectChannelBusinessById(String id);

    /**
     * 查询场景人员列表
     * 
     * @param channelBusiness 场景人员
     * @return 场景人员集合
     */
    public List<ChannelBusiness> selectChannelBusinessList(ChannelBusiness channelBusiness);

    /**
     * 新增场景人员
     * 
     * @param channelBusiness 场景人员
     * @return 结果
     */
    public int insertChannelBusiness(ChannelBusiParam channelBusiness);

    /**
     * 修改场景人员
     * 
     * @param channelBusiness 场景人员
     * @return 结果
     */
    public int updateChannelBusiness(ChannelBusiness channelBusiness);

    /**
     * 批量删除场景人员
     * 
     * @param ids 需要删除的场景人员ID
     * @return 结果
     */
    public int deleteChannelBusinessByIds(String[] ids);

    /**
     * 删除场景人员信息
     * 
     * @param id 场景人员ID
     * @return 结果
     */
    public int deleteChannelBusinessById(String id);

    /**
     * 查询未绑定场景的人员列表
     * 
     * @param basePersonInfo 人员信息
     * @param channelId 场景主键
     * @return
     */
    public List<BasePersonInfo> selectUnbindPersonInfoList(BasePersonInfo basePersonInfo, String channelId);

    /**
     * 单个人库关系绑定
     * 
     * @param targetBusi 场景人员
     * @param channelCode 场景编码
     */
    public void insertSingleChannelBusiData(ChannelBusiness targetBusi, String channelCode);

    /**
     * 同步关系到datamananger
     * 
     * @param channelBusiness 场景人员
     * @return
     */
    public AjaxResult syncdata(ChannelBusiness channelBusiness);

    /**
     * 保存导入数据
     * 
     * @param excelFile excel文件
     * @param channelId 场景主键
     * @param updateSupport 支持更新
     * @return
     * @throws Exception
     * @throws IOException
     */
    public String saveImportData(MultipartFile excelFile, String channelId, Boolean updateSupport)
        throws IOException, Exception;

    /**
     * 保存导入数据
     * 
     * @param inputStream excel文件流
     * @param channelId 场景主键
     * @param updateSupport 支持更新
     * @return
     * @throws Exception
     * @throws IOException
     */
    public String saveImportData(InputStream inputStream, String channelId, Boolean updateSupport)
        throws IOException, Exception;

}
