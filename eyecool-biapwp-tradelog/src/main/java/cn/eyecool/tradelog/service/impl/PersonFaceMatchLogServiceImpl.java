package cn.eyecool.tradelog.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;

import cn.eyecool.basedata.domain.BasePersonFace;
import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.basedata.manager.IPersonFaceRecogLogicService;
import cn.eyecool.basedata.mapper.BasePersonFaceMapper;
import cn.eyecool.basedata.mapper.BasePersonInfoMapper;
import cn.eyecool.common.annotation.DataScope;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.common.constant.UserConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.domain.entity.SysDept;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.framework.config.websocket.BigScreenWebSocketServer;
import cn.eyecool.system.service.ISysConfigService;
import cn.eyecool.system.service.ISysDeptService;
import cn.eyecool.tradelog.domain.FaceRealtimeTradeLog;
import cn.eyecool.tradelog.domain.PersonFaceMatchLog;
import cn.eyecool.tradelog.domain.PersonHealthCodeLog;
import cn.eyecool.tradelog.mapper.PersonFaceMatchLogMapper;
import cn.eyecool.tradelog.mapper.PersonHealthCodeLogMapper;
import cn.eyecool.tradelog.param.PersonFaceMatchBakLog;
import cn.eyecool.tradelog.service.IPersonFaceMatchLogService;
import lombok.extern.slf4j.Slf4j;

/**
 * 人脸比对日志Service业务层处理
 * 
 * @author admin
 * @date 2021-04-29
 */
@Service
@Slf4j
public class PersonFaceMatchLogServiceImpl implements IPersonFaceMatchLogService {
    @Autowired
    private PersonFaceMatchLogMapper personFaceMatchLogMapper;
    @Autowired
    private BasePersonInfoMapper basePersonInfoMapper;
    @Autowired
    private ISysConfigService configService;
    @Autowired
    private IPersonFaceRecogLogicService faceRecogLogicService;
    @Autowired
    private BasePersonFaceMapper basePersonFaceMapper;
    @Autowired
    private ISysDeptService sysDeptService;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private PersonHealthCodeLogMapper personHealthCodeLogMapper;

    /**
     * 查询人脸比对日志
     * 
     * @param id 人脸比对日志ID
     * @return 人脸比对日志
     */
    @Override
    public PersonFaceMatchLog selectPersonFaceMatchLogById(String id) {
        return personFaceMatchLogMapper.selectPersonFaceMatchLogById(id);
    }

    /**
     * 查询人脸比对日志列表
     * 
     * @param personFaceMatchLog 人脸比对日志
     * @return 人脸比对日志
     */
    @Override
    @DataScope(deptAlias = "log")
    public List<PersonFaceMatchLog> selectPersonFaceMatchLogList(PersonFaceMatchLog personFaceMatchLog) {
        return personFaceMatchLogMapper.selectPersonFaceMatchLogList(personFaceMatchLog);
    }

    /**
     * 查询人脸比对日志最新比对列表
     * 
     * @param personFaceMatchLog 人脸比对日志
     * @return 人脸比对日志
     */
    @Override
    @DataScope(deptAlias = "log")
    public List<PersonFaceMatchLog> selectLastPersonFaceMatchLogList(PersonFaceMatchLog personFaceMatchLog) {
        return personFaceMatchLogMapper.selectLastPersonFaceMatchLogList(personFaceMatchLog);
    }

    /**
     * 新增人脸比对日志
     * 
     * @param personFaceMatchLog 人脸比对日志
     * @return 结果
     */
    @Override
    public int insertPersonFaceMatchLog(PersonFaceMatchLog personFaceMatchLog) {
        personFaceMatchLog.setCreateTime(DateUtils.getNowDate());
        return personFaceMatchLogMapper.insertPersonFaceMatchLog(personFaceMatchLog);
    }

    /**
     * 修改人脸比对日志
     * 
     * @param personFaceMatchLog 人脸比对日志
     * @return 结果
     */
    @Override
    public int updatePersonFaceMatchLog(PersonFaceMatchLog personFaceMatchLog) {
        return personFaceMatchLogMapper.updatePersonFaceMatchLog(personFaceMatchLog);
    }

    /**
     * 批量删除人脸比对日志
     * 
     * @param ids 需要删除的人脸比对日志ID
     * @return 结果
     */
    @Override
    public int deletePersonFaceMatchLogByIds(String[] ids) {
        return personFaceMatchLogMapper.deletePersonFaceMatchLogByIds(ids);
    }

    /**
     * 删除人脸比对日志信息
     * 
     * @param id 人脸比对日志ID
     * @return 结果
     */
    @Override
    public int deletePersonFaceMatchLogById(String id) {
        return personFaceMatchLogMapper.deletePersonFaceMatchLogById(id);
    }

    /**
     * 保存1v1回传日志
     * 
     * @param faceMatchBakLog
     */
    @Override
    @Transactional
    public void savePersonFaceMatchBakLog(PersonFaceMatchBakLog faceMatchBakLog) {
        PersonFaceMatchLog faceMatchLog = new PersonFaceMatchLog();
        faceMatchLog.setId(IdWorker.getNextStringId());
        faceMatchLog.setHealthcodeLogId(faceMatchBakLog.getHealthcodeLogId());
        faceMatchLog.setAlgsVersion(faceMatchBakLog.getAlgsVersion());
        faceMatchLog.setChannelCode(faceMatchBakLog.getChannelCode());
        faceMatchLog.setDeviceCode(faceMatchBakLog.getDeviceCode());
        faceMatchLog.setDeviceName(faceMatchBakLog.getDeviceName());
        faceMatchLog.setDeviceIp(faceMatchBakLog.getDeviceIp());
        faceMatchLog.setDeviceModel(faceMatchBakLog.getDeviceModel());
        faceMatchLog.setDeviceAddr(faceMatchBakLog.getDeviceAddr());
        faceMatchLog.setDeviceLongitude(StringUtils.isBlank(faceMatchBakLog.getDeviceLongitude()) ? null
            : Double.valueOf(faceMatchBakLog.getDeviceLongitude()));
        faceMatchLog.setDeviceDimension(StringUtils.isBlank(faceMatchBakLog.getDeviceDimension()) ? null
            : Double.valueOf(faceMatchBakLog.getDeviceDimension()));
        faceMatchLog.setDeviceDirection(faceMatchBakLog.getDeviceDirection());
        faceMatchLog.setReceivedSeq(faceMatchBakLog.getReceivedSeq());
        faceMatchLog
            .setReceivedTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, faceMatchBakLog.getReceivedTime()));
        faceMatchLog.setCreateTime(faceMatchLog.getReceivedTime());
        if (StringUtils.isNotBlank(faceMatchBakLog.getTimeUsed())) {
            faceMatchLog.setTimeUsed(Long.valueOf(faceMatchBakLog.getTimeUsed()));
        }
        faceMatchLog.setSceneStockScore(StringUtils.isBlank(faceMatchBakLog.getSceneStockScore()) ? null
            : Double.valueOf(faceMatchBakLog.getSceneStockScore()));
        faceMatchLog.setSceneChipScore(StringUtils.isBlank(faceMatchBakLog.getSceneChipScore()) ? null
            : Double.valueOf(faceMatchBakLog.getSceneChipScore()));
        faceMatchLog.setSceneOnlineScore(StringUtils.isBlank(faceMatchBakLog.getSceneOnlineScore()) ? null
            : Double.valueOf(faceMatchBakLog.getSceneOnlineScore()));
        faceMatchLog.setOnlineChipScore(StringUtils.isBlank(faceMatchBakLog.getOnlineChipScore()) ? null
            : Double.valueOf(faceMatchBakLog.getOnlineChipScore()));
        faceMatchLog.setCheckliveScore(StringUtils.isBlank(faceMatchBakLog.getCheckliveScore()) ? null
            : Double.valueOf(faceMatchBakLog.getCheckliveScore()));
        faceMatchLog.setCheckliveResult(faceMatchBakLog.getCheckliveResult());
        faceMatchLog.setResult(faceMatchBakLog.getResult());
        faceMatchLog.setOnlineChipResult(faceMatchBakLog.getOnlineChipResult());
        faceMatchLog.setSceneChipResult(faceMatchBakLog.getSceneChipResult());
        faceMatchLog.setSceneOnlineResult(faceMatchBakLog.getSceneOnlineResult());
        faceMatchLog.setSceneStockResult(faceMatchBakLog.getSceneStockResult());
        faceMatchLog.setVendorCode(faceMatchBakLog.getVendorCode());
        faceMatchLog.setAlgsVersion(faceMatchBakLog.getAlgsVersion());
        faceMatchLog.setTemperature(StringUtils.isBlank(faceMatchBakLog.getTemperature()) ? null
            : Double.valueOf(faceMatchBakLog.getTemperature()));
        faceMatchLog.setTemperatureFloor(StringUtils.isBlank(faceMatchBakLog.getTemperatureFloor()) ? null
            : Double.valueOf(faceMatchBakLog.getTemperatureFloor()));
        faceMatchLog.setTemperatureTop(StringUtils.isBlank(faceMatchBakLog.getTemperatureTop()) ? null
            : Double.valueOf(faceMatchBakLog.getTemperatureTop()));
        String temperature = faceMatchBakLog.getTemperature();
        String temperatureFloor = faceMatchBakLog.getTemperatureFloor();
        String temperatureTop = faceMatchBakLog.getTemperatureTop();
        faceMatchLog.setTemperature(StringUtils.isBlank(temperature) ? null : Double.valueOf(temperature));
        faceMatchLog
            .setTemperatureFloor(StringUtils.isBlank(temperatureFloor) ? null : Double.valueOf(temperatureFloor));
        faceMatchLog.setTemperatureTop(StringUtils.isBlank(temperatureTop) ? null : Double.valueOf(temperatureTop));
        if (StringUtils.isNotBlank(faceMatchBakLog.getTemperatureResult())) {
            faceMatchLog.setTemperatureResult(faceMatchBakLog.getTemperatureResult());
        } else if (null != faceMatchLog.getTemperature()) {
            Double temperatureVal = faceMatchLog.getTemperature();
            Double temperatureTopVal = faceMatchLog.getTemperatureTop();
            Double temperatureFloorVal = faceMatchLog.getTemperatureFloor();
            if (null != temperatureTopVal && temperatureVal >= temperatureTopVal) {
                faceMatchLog.setTemperatureResult(DictConstants.TemperatureResult.HIGHER);
            } else if (null != temperatureFloorVal && temperatureVal < temperatureFloorVal) {
                faceMatchLog.setTemperatureResult(DictConstants.TemperatureResult.LOWER);
            } else {
                faceMatchLog.setTemperatureResult(DictConstants.TemperatureResult.NORMAL);
            }
        }
        String uniqueId = faceMatchBakLog.getUniqueId();
        faceMatchLog.setUniqueId(uniqueId);
        if (StringUtils.isNotBlank(uniqueId)) {
            // 查询人员信息确定部门
            BasePersonInfo personCondition = new BasePersonInfo();
            personCondition.setUniqueId(uniqueId);
            personCondition.setStatus(DictConstants.Status.ENABLE);
            List<BasePersonInfo> personList = basePersonInfoMapper.selectBasePersonInfoList(personCondition);
            if (CollectionUtils.isNotEmpty(personList)) {
                BasePersonInfo person = personList.get(0);
                faceMatchLog.setPersonName(person.getName());
                Long deptId = person.getDeptId();
                if (null != deptId) {
                    faceMatchLog.setDeptId(deptId);
                    SysDept sysDept = sysDeptService.selectDeptById(deptId);
                    faceMatchLog.setDeptName(null != sysDept ? sysDept.getDeptName() : null);
                }
            }
        }
        String sceneImageBase64 = faceMatchBakLog.getSceneImageBase64();
        // 进行比对图片的上传
        String baseDir = getSysFaceMatchPicBaseDir();
        // 加密上传现场照
        faceMatchLog.setSceneImage(faceRecogLogicService.uploadFaceImg(true, null, sceneImageBase64, baseDir));
        String stockImageBase64 = faceMatchBakLog.getStockImageBase64();
        if (StringUtils.isNotBlank(stockImageBase64)) {
            // 加密上传底库照
            faceMatchLog.setStockImage(faceRecogLogicService.uploadFaceImg(true, null, stockImageBase64, baseDir));
        } else if (StringUtils.isNotBlank(uniqueId)) {
            // 查询人员底库照
            BasePersonFace faceCondition = new BasePersonFace();
            faceCondition.setUniqueId(uniqueId);
            faceCondition.setStatus(DictConstants.Status.ENABLE);
            List<BasePersonFace> faceList = basePersonFaceMapper.selectBasePersonFaceList(faceCondition);
            if (CollectionUtils.isNotEmpty(faceList)) {
                String imageUrl = faceList.get(0).getImageUrl();
                String encrypted = faceList.get(0).getEncrypted();
                stockImageBase64 = PlatformFileUtils.getImageBase64(imageUrl);
                boolean isEncrypted = DictConstants.Encrypted.ENABLE.equals(encrypted);
                stockImageBase64 =
                    isEncrypted ? PlatformCryptUtils.decryptImageBase64(stockImageBase64) : stockImageBase64;
                faceMatchLog.setStockImage(faceRecogLogicService.uploadFaceImg(true, null, stockImageBase64, baseDir));
            }
        }
        if (StringUtils.isNotBlank(faceMatchBakLog.getChipImageBase64())) {
            // 加密上传芯片照
            faceMatchLog.setChipImage(
                faceRecogLogicService.uploadFaceImg(true, null, faceMatchBakLog.getChipImageBase64(), baseDir));
        }
        if (StringUtils.isNotBlank(faceMatchBakLog.getOnlineImageBase64())) {
            // 加密上传联网核查照
            faceMatchLog.setOnlineImage(
                faceRecogLogicService.uploadFaceImg(true, null, faceMatchBakLog.getOnlineImageBase64(), baseDir));
        }
        personFaceMatchLogMapper.insertPersonFaceMatchLog(faceMatchLog);
        execWebsocketSendMsgToBigScreen(faceMatchLog);
    }

    /**
     * 查询实时交易，推送给大屏监控页面
     */
    private void execWebsocketSendMsgToBigScreen(PersonFaceMatchLog faceMatchLog) {
        String tenantId = StringUtils.isBlank(TenantContextHolder.getTenantId()) ? UserConstants.SUPER_TENANT
            : TenantContextHolder.getTenantId();
        CompletableFuture.runAsync(() -> {
            List<String> bigScreenTokenList = redisCache.getCacheList("bigscreen-token:" + tenantId);
            if (CollectionUtils.isEmpty(bigScreenTokenList)) {
                return;
            }
            FaceRealtimeTradeLog realtimeTradeLog = new FaceRealtimeTradeLog();
            realtimeTradeLog.setReceivedSeq(faceMatchLog.getReceivedSeq());
            realtimeTradeLog.setReceivedTime(faceMatchLog.getReceivedTime());
            realtimeTradeLog.setTenantId(tenantId);
            realtimeTradeLog.setUniqueId(faceMatchLog.getUniqueId());
            realtimeTradeLog.setPersonName(faceMatchLog.getPersonName());
            realtimeTradeLog.setResult(faceMatchLog.getResult());
            realtimeTradeLog.setDeviceNo(faceMatchLog.getDeviceCode());
            realtimeTradeLog.setDeviceName(faceMatchLog.getDeviceName());
            realtimeTradeLog.setDeviceAddr(faceMatchLog.getDeviceAddr());
            realtimeTradeLog.setTemperature(faceMatchLog.getTemperature());
            realtimeTradeLog.setTemperatureFloor(faceMatchLog.getTemperatureFloor());
            realtimeTradeLog.setTemperatureTop(faceMatchLog.getTemperatureTop());
            realtimeTradeLog.setTemperatureResult(faceMatchLog.getTemperatureResult());
            String healthcodeLogId = faceMatchLog.getHealthcodeLogId();
            if (StringUtils.isNotEmpty(healthcodeLogId)) {
                PersonHealthCodeLog healthCodeLog =
                    personHealthCodeLogMapper.selectPersonHealthCodeLogById(healthcodeLogId);
                if (null != healthCodeLog) {
                    String message = healthCodeLog.getMessage();
                    realtimeTradeLog.setHealthResult(healthCodeLog.getResult());
                    realtimeTradeLog.setHealthMessage(message);
                    realtimeTradeLog.setHealthCodeState(transHealthCode(message));
                }
            }
            bigScreenTokenList.parallelStream().forEach(cid -> {
                try {
                    BigScreenWebSocketServer.sendInfo(JSON.toJSONString(realtimeTradeLog), cid);
                } catch (IOException e) {
                    log.error("Pushing face recognition real-time transactions to the big screen is abnormal:[{}]",
                        e.getMessage(), e);
                }
            });
        });
    }

    /**
     * 健康码状态转换
     * 
     * @param message
     * @return
     */
    private String transHealthCode(String message) {
        String code = null;
        if (StringUtils.isEmpty(message)) {
            return code;
        }
        switch (message) {
            case "绿码":
                code = "0";
                break;
            case "黄码":
                code = "1";
                break;
            case "红码":
                code = "2";
                break;
            default:
                code = "-1";
                break;
        }
        return code;
    }

    /**
     * 获取人脸1:1搜索图片存放文件夹
     * 
     * @return
     */
    private String getSysFaceMatchPicBaseDir() {
        String baseDir = configService.selectConfigByKey(SysConfigConstants.BUSI_FACE_COMPARE_DIR_KEY);
        if (StringUtils.isBlank(baseDir)) {
            throw new CustomException(MessageUtils.message("channel.face.service.image.match.folder.need",
                SysConfigConstants.BUSI_FACE_COMPARE_DIR_KEY));
        }
        return baseDir;
    }

    /**
     * 根据时间范围查询日志
     * 
     * @param personFaceMatchLog
     * @return
     */
    @Override
    public List<PersonFaceMatchLog> selectPersonFaceMatchLogByTimeRange(PersonFaceMatchLog personFaceMatchLog) {
        return personFaceMatchLogMapper.selectPersonFaceMatchLogByTimeRange(personFaceMatchLog);
    }

}
