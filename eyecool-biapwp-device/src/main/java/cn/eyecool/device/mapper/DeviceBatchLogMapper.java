package cn.eyecool.device.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.eyecool.device.domain.DeviceBatchLog;

/**
 * 设备批次Mapper接口
 * 
 * @author admin
 * @date 2021-04-07
 */
public interface DeviceBatchLogMapper {
    /**
     * 查询设备批次
     * 
     * @param id 设备批次ID
     * @return 设备批次
     */
    public DeviceBatchLog selectDeviceBatchLogById(String id);

    /**
     * 查询设备批次列表
     * 
     * @param deviceBatchLog 设备批次
     * @return 设备批次集合
     */
    public List<DeviceBatchLog> selectDeviceBatchLogList(DeviceBatchLog deviceBatchLog);

    /**
     * 新增设备批次
     * 
     * @param deviceBatchLog 设备批次
     * @return 结果
     */
    public int insertDeviceBatchLog(DeviceBatchLog deviceBatchLog);

    /**
     * 修改设备批次
     * 
     * @param deviceBatchLog 设备批次
     * @return 结果
     */
    public int updateDeviceBatchLog(DeviceBatchLog deviceBatchLog);

    /**
     * 删除设备批次
     * 
     * @param id 设备批次ID
     * @return 结果
     */
    public int deleteDeviceBatchLogById(String id);

    /**
     * 批量删除设备批次
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteDeviceBatchLogByIds(String[] ids);

    /**
     * 更新设备批次回滚状态
     * 
     * @param batchNums
     * @param rollbackTime
     * @return
     */
    public int updateBatchRollBackStatus(@Param("batchNums") String[] batchNums,
        @Param("rollbackTime") Date rollbackTime);
}
