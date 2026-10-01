package cn.eyecool.statistic.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.annotation.SqlParser;

import cn.eyecool.statistic.domain.DeviceLogNumInfo;
import cn.eyecool.statistic.domain.StatisticFaceRecogLog;
import cn.eyecool.statistic.domain.StatisticIdcardVerifyLog;
import cn.eyecool.statistic.domain.StatisticTradeLog;
import cn.eyecool.statistic.domain.TenantPassPersonCount;
import cn.eyecool.statistic.domain.TenantTradeLogCount;
import cn.eyecool.tradelog.domain.FaceRealtimeTradeLog;
import cn.eyecool.tradelog.domain.RealtimeTradeLog;

/**
 * 交易日志统计数据层
 * 
 * @author mawj
 * @date 2021/08/26
 */
@SuppressWarnings("deprecation")
public interface TradeLogStatisticMapper {

    /**
     * 查询人脸1v1比对通过人员数量
     * 
     * @param startTime
     * @param endTime
     * @return
     */
    public int countFace1v1PassPerson(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 查询人脸1vN识别通过人员数量
     * 
     * @param startTime
     * @param endTime
     * @return
     */
    public int countFace1vNPassPerson(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 查询指纹1vN识别通过人员数量
     * 
     * @param startTime
     * @param endTime
     * @return
     */
    public int countFinger1vNPassPerson(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 查询虹膜1vN识别通过人员数量
     * 
     * @param startTime
     * @param endTime
     * @return
     */
    public int countIris1vNPassPerson(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 查询虹膜人脸多模态通过人员数量
     * 
     * @param startTime
     * @param endTime
     * @return
     */
    public int countIrisFace1vNPassPerson(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 查询人脸1vN识别日志数量
     * 
     * @param startTime
     * @param endTime
     * @param result
     * @param validType
     * @return
     */
    public int countFace1vNTradeLog(@Param("startTime") Date startTime, @Param("endTime") Date endTime,
        @Param("result") String result, @Param("validType") String validType);

    /**
     * 查询指纹1vN识别日志数量
     * 
     * @param startTime
     * @param endTime
     * @return
     */
    public int countFinger1vNTradeLog(@Param("startTime") Date startTime, @Param("endTime") Date endTime,
        @Param("result") String result);

    /**
     * 查询虹膜1vN识别日志数量
     * 
     * @param startTime
     * @param endTime
     * @return
     */
    public int countIris1vNTradeLog(@Param("startTime") Date startTime, @Param("endTime") Date endTime,
        @Param("result") String result);

    /**
     * 查询虹膜人脸多模态识别日志数量
     * 
     * @param startTime
     * @param endTime
     * @return
     */
    public int countIrisFace1vNTradeLog(@Param("startTime") Date startTime, @Param("endTime") Date endTime,
        @Param("result") String result);

    /**
     * 查询设备人脸1v1日志数量信息
     * 
     * @param startTime
     * @param endTime
     * @param result
     * @return
     */
    public List<DeviceLogNumInfo> getFace1v1DeviceLogNumInfo(@Param("startTime") Date startTime,
        @Param("endTime") Date endTime, @Param("result") String result);

    /**
     * 查询区域人脸1v1日志数量
     * 
     * @param startTime
     * @param endTime
     * @param result
     * @param areaId
     * @return
     */
    public int getFace1v1AreaLogNum(@Param("startTime") Date startTime, @Param("endTime") Date endTime,
        @Param("result") String result, @Param("areaId") Long areaId);

    /**
     * 查询区域人脸1vN日志数量
     * 
     * @param startTime
     * @param endTime
     * @param result
     * @param areaId
     * @return
     */
    public int getFace1vNAreaLogNum(@Param("startTime") Date startTime, @Param("endTime") Date endTime,
        @Param("result") String result, @Param("areaId") Long areaId);

    /**
     * 查询设备人脸1vN日志数量信息
     * 
     * @param startTime
     * @param endTime
     * @param result
     * @return
     */
    public List<DeviceLogNumInfo> getFace1vNDeviceLogNumInfo(@Param("startTime") Date startTime,
        @Param("endTime") Date endTime, @Param("result") String result);

    /**
     * 查询设备虹膜人脸多模态1vN日志数量信息
     * 
     * @param startTime
     * @param endTime
     * @param result
     * @return
     */
    public List<DeviceLogNumInfo> getIrisFace1vNDeviceLogNumInfo(@Param("startTime") Date startTime,
        @Param("endTime") Date endTime, @Param("result") String result);

    /**
     * 查询人脸1v1比对日志数量
     * 
     * @param startTime
     * @param endTime
     * @param result
     * @return
     */
    public int countFace1v1TradeLog(@Param("startTime") Date startTime, @Param("endTime") Date endTime,
        @Param("result") String result);

    /**
     * 查询实时交易
     * 
     * @return
     */
    public List<RealtimeTradeLog> getRealtimeTradeLog();

    /**
     * 获取交易日志数量趋势
     * 
     * @param statisticUnit
     * @param startDate
     * @param endDate
     * @return
     */
    public List<StatisticTradeLog> getTradeLogTrend(@Param("statisticUnit") String statisticUnit,
        @Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 获取人脸识别日志数量趋势
     * 
     * @param statisticUnit
     * @param startDate
     * @param endDate
     * @return
     */
    public List<StatisticFaceRecogLog> getFaceRecogLogTrend(@Param("statisticUnit") String statisticUnit,
        @Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 获取身份证比对日志数量趋势
     * 
     * @param statisticUnit
     * @param startDate
     * @param endDate
     * @return
     */
    public List<StatisticIdcardVerifyLog> getIdcardVerifyLogTrend(@Param("statisticUnit") String statisticUnit,
        @Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 查询人脸1vN识别通过人员数量
     * 
     * @param startTime
     * @param endTime
     * @return
     */
    @SqlParser(filter = true)
    public List<TenantPassPersonCount> countFace1vNPassPersonByTenantId(@Param("startTime") Date startTime,
        @Param("endTime") Date endTime);

    /**
     * 查询指纹1vN识别通过人员数量
     * 
     * @param startTime
     * @param endTime
     * @return
     */
    @SqlParser(filter = true)
    public List<TenantPassPersonCount> countFinger1vNPassPersonByTenantId(@Param("startTime") Date startTime,
        @Param("endTime") Date endTime);

    /**
     * 查询虹膜1vN识别通过人员数量
     * 
     * @param startTime
     * @param endTime
     * @return
     */
    @SqlParser(filter = true)
    public List<TenantPassPersonCount> countIris1vNPassPersonByTenantId(@Param("startTime") Date startTime,
        @Param("endTime") Date endTime);

    /**
     * 查询虹膜人脸多模态通过人员数量
     * 
     * @param startTime
     * @param endTime
     * @return
     */
    @SqlParser(filter = true)
    public List<TenantPassPersonCount> countIrisFace1vNPassPersonByTenantId(@Param("startTime") Date startTime,
        @Param("endTime") Date endTime);

    /**
     * 插入日志统计信息
     * 
     * @param statisticTradeLog
     * @return
     */
    @SqlParser(filter = true)
    public int insertStatisticTradeLog(StatisticTradeLog statisticTradeLog);

    /**
     * 根据统计日期查询日志统计信息
     * 
     * @param statisticDate
     * @param statisticUnit
     * @param tenantId
     * @return
     */
    @SqlParser(filter = true)
    public StatisticTradeLog getStatistictTradeLogByDate(@Param("statisticDate") String statisticDate,
        @Param("statisticUnit") String statisticUnit, @Param("tenantId") String tenantId);

    /**
     * 查询人脸1vN识别日志数量
     * 
     * @param startTime
     * @param endTime
     * @param result
     * @return
     */
    @SqlParser(filter = true)
    public List<TenantTradeLogCount> countFace1vNTradeLogByTenantId(@Param("startTime") Date startTime,
        @Param("endTime") Date endTime, @Param("result") String result, @Param("validType") String validType);

    /**
     * 查询指纹1vN识别日志数量
     * 
     * @param startTime
     * @param endTime
     * @return
     */
    @SqlParser(filter = true)
    public List<TenantTradeLogCount> countFinger1vNTradeLogByTenantId(@Param("startTime") Date startTime,
        @Param("endTime") Date endTime, @Param("result") String result);

    /**
     * 查询虹膜1vN识别日志数量
     * 
     * @param startTime
     * @param endTime
     * @return
     */
    @SqlParser(filter = true)
    public List<TenantTradeLogCount> countIris1vNTradeLogByTenantId(@Param("startTime") Date startTime,
        @Param("endTime") Date endTime, @Param("result") String result);

    /**
     * 查询虹膜人脸多模态识别日志数量
     * 
     * @param startTime
     * @param endTime
     * @return
     */
    @SqlParser(filter = true)
    public List<TenantTradeLogCount> countIrisFace1vNTradeLogByTenantId(@Param("startTime") Date startTime,
        @Param("endTime") Date endTime, @Param("result") String result);

    /**
     * 根据统计日期查询人脸识别日志统计信息
     * 
     * @param statisticDate
     * @param statisticUnit
     * @param tenantId
     * @return
     */
    @SqlParser(filter = true)
    public StatisticFaceRecogLog getStatistictFaceRecogLogByDate(@Param("statisticDate") String statisticDate,
        @Param("statisticUnit") String statisticUnit, @Param("tenantId") String tenantId);

    /**
     * 插入人脸识别日志统计信息
     * 
     * @param statisticTradeLog
     * @return
     */
    @SqlParser(filter = true)
    public void insertStatisticFaceRecogLog(StatisticFaceRecogLog statisticTradeLog);

    /**
     * 根据统计日期查询身份证比对日志统计信息
     * 
     * @param statisticDate
     * @param statisticUnit
     * @param tenantId
     * @return
     */
    @SqlParser(filter = true)
    public StatisticIdcardVerifyLog getStatistictIdcardVerifyLogByDate(@Param("statisticDate") String statisticDate,
        @Param("statisticUnit") String statisticUnit, @Param("tenantId") String tenantId);

    /**
     * 插入身份证比对日志统计信息
     * 
     * @param statisticTradeLog
     * @return
     */
    @SqlParser(filter = true)
    public void insertStatisticIdcardVerifyLog(StatisticIdcardVerifyLog statisticTradeLog);

    /**
     * 查询人脸1v1比对日志数量
     * 
     * @param startTime
     * @param endTime
     * @param result
     * @return
     */
    @SqlParser(filter = true)
    public List<TenantTradeLogCount> countFace1v1TradeLogByTenantId(@Param("startTime") Date startTime,
        @Param("endTime") Date endTime, @Param("result") String result);

    /**
     * 查询人脸实时交易
     * 
     * @return
     */
    public List<FaceRealtimeTradeLog> getFaceRealtimeTradeLog();

    /**
     * 查询异常人脸实时交易
     * 
     * @return
     */
    public List<FaceRealtimeTradeLog> getFaceAbnormalRealtimeTrade();

}
