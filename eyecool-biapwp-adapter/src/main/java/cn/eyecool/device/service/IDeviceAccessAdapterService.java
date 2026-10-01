package cn.eyecool.device.service;

import java.io.File;
import java.util.Date;
import java.util.List;

import cn.eyecool.basedata.domain.BasePersonFace;
import cn.eyecool.device.domain.DeviceAccessAdapter;
import cn.eyecool.device.domain.DeviceInfo;

/**
 * 203设备接入园区平台Service接口
 * 
 * @author 段存明
 * @date 2021-02-24
 */
public interface IDeviceAccessAdapterService {
    /**
     * 获取设备 getDevBySn
     *
     * @param sn
     * @return
     */
    public DeviceInfo getDevBySn(String sn);

    /**
     * 检查接口时效性 checkValidity
     * 
     * @param personId
     * @param date
     * @return
     */
    public boolean checkValidity(String personId, Date date);

    /**
     * 获取人员头像base64
     *
     * getPersonFace
     * 
     * @param personId
     * @return
     */
    public String getPersonFaceBase64(String personId);

    /**
     * 获取人员头像
     *
     * getPersonFace
     * 
     * @param personId
     * @return
     */
    public BasePersonFace getPersonFace(String personId);

    /**
     * 查询203设备接入园区平台
     * 
     * @param id 203设备接入园区平台ID
     * @return 203设备接入园区平台
     */
    public DeviceAccessAdapter selectDeviceAccessAdapterById(String id);

    /**
     * 查询203设备接入园区平台列表
     * 
     * @param deviceAccessAdapter 203设备接入园区平台
     * @return 203设备接入园区平台集合
     */
    public List<DeviceAccessAdapter> selectDeviceAccessAdapterList(DeviceAccessAdapter deviceAccessAdapter);

    /**
     * 新增203设备接入园区平台
     * 
     * @param deviceAccessAdapter 203设备接入园区平台
     * @return 结果
     */
    public int insertDeviceAccessAdapter(DeviceAccessAdapter deviceAccessAdapter);

    /**
     * 修改203设备接入园区平台
     * 
     * @param deviceAccessAdapter 203设备接入园区平台
     * @return 结果
     */
    public int updateDeviceAccessAdapter(DeviceAccessAdapter deviceAccessAdapter);

    /**
     * 批量删除203设备接入园区平台
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteDeviceAccessAdapterByIds(String[] ids);

    /**
     * 删除203设备接入园区平台信息
     * 
     * @param id 203设备接入园区平台ID
     * @return 结果
     */
    public int deleteDeviceAccessAdapterById(String id);

    /**
     * 获取平台url
     *
     * @return
     */
    public String getUrl();

    /**
     * 获取配置信息
     *
     * @param sn
     * @return
     */
    public DeviceAccessAdapter getBySn(String sn);

    /**
     * base64ToFile
     * 
     * @param base64
     * @param savePath
     */
    public File base64ToFile(String base64, String savePath, String fileName);

    /**
     * handleLog
     *
     * @param json
     * @return
     */
    public String handleLog(String json);

    /**
     * 处理图像
     *
     * @param imgPath
     */
    public File dealImage(String imgPath);

    /**
     * 验证接口时效性
     *
     * @param denId
     * @return
     */
    public boolean checkValid(String denId);

    /**
     * 检查并创建配置信息
     *
     * @param sn
     */
    public boolean checkAndCreateAdapterInfo(String sn);
}
