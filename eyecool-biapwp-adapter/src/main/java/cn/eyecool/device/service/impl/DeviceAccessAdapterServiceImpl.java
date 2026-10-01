package cn.eyecool.device.service.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.eyecool.basedata.domain.BasePersonFace;
import cn.eyecool.basedata.service.IBasePersonFaceService;
import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.DictConstants.PassValidType;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.device.constant.AdapterConstants;
import cn.eyecool.device.domain.DeviceAccessAdapter;
import cn.eyecool.device.domain.DeviceInfo;
import cn.eyecool.device.mapper.DeviceAccessAdapterMapper;
import cn.eyecool.device.service.IDeviceAccessAdapterService;
import cn.eyecool.device.service.IDeviceInfoService;
import cn.eyecool.device.util.AdapterUtils;
import cn.eyecool.device.vo.AccessInfoVO;
import cn.eyecool.device.vo.CardVO;
import cn.eyecool.device.vo.CardsInfoVO;
import cn.eyecool.device.vo.CommInfoVO;
import cn.eyecool.device.vo.DevResultInfoVO;
import cn.eyecool.device.vo.EventsInfoVO;
import cn.eyecool.device.vo.FaceInfoVO;
import cn.eyecool.device.vo.FaceRecognizeInfoVO;
import cn.eyecool.device.vo.MatchLogVO;
import cn.eyecool.device.vo.ResultPersonInfoVO;
import cn.eyecool.server.handler.TradelogHttpHandler;
import cn.eyecool.system.service.ISysConfigService;
import cn.eyecool.tradelog.domain.PersonHealthCodeLog;
import cn.eyecool.tradelog.service.IPersonHealthCodeLogService;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

/**
 * 203设备接入园区平台Service业务层处理
 * 
 * @author 段存明
 * @date 2021-02-24 12
 */
@Slf4j
@Service
public class DeviceAccessAdapterServiceImpl implements IDeviceAccessAdapterService {

    @Autowired
    private DeviceAccessAdapterMapper deviceAccessAdapterMapper;
    @Autowired
    private IBasePersonFaceService basePersonFaceService;
    @Autowired
    private ISysConfigService configService;
    @Autowired
    private IDeviceInfoService DeviceInfoService;
    @Autowired
    private TradelogHttpHandler tradelogHttpHandler;
    @Autowired
    private TenantProperties tenantProperties;
    @Autowired
    private IPersonHealthCodeLogService healthCodeLogService;

    /**
     * getDevBySn
     *
     * @param sn 序列号123
     * @return
     */
    @Override
    public DeviceInfo getDevBySn(String sn) {
        DeviceInfo condition = new DeviceInfo();
        condition.setDeviceNo(sn);
        List<DeviceInfo> deviceList = DeviceInfoService.selectDeviceInfoList(condition);
        return CollectionUtils.isEmpty(deviceList) ? null : deviceList.get(0);
    }

    /**
     * 获取人员头像
     *
     * getPersonFace
     * 
     * @param snPersonId 人员id
     * @return
     */
    @Override
    public BasePersonFace getPersonFace(String snPersonId) {
        if (StringUtils.isEmpty(snPersonId)) {
            return null;
        }
        int arrLen = 2;
        String[] arr = snPersonId.split("_");
        if (arr == null || arr.length < arrLen) {
            return null;
        }
        String sn = arr[0];
        String personId = arr[1];

        DeviceInfo DeviceInfo = getDevBySn(sn);
        if (DeviceInfo == null) {
            log.info("The device [{}] is unregistered on the platform, the finished time is:[{}]", sn, DateUtils.dateTime());
            return null;
        }
        if (tenantProperties.getEnabled()) {
            TenantContextHolder.setTenantId(DeviceInfo.getTenantId());
        }
        BasePersonFace face = null;
        BasePersonFace faceCondition = new BasePersonFace();

        faceCondition.setUniqueId(personId);
        faceCondition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonFace> faceList = basePersonFaceService.selectBasePersonFaceList(faceCondition);
        if (!CollectionUtils.isEmpty(faceList)) {
            face = faceList.get(0);
        }
        return face;
    }

    /**
     * 获取人员头像base64
     *
     * getPersonFace
     * 
     * @param snPersonId 人员id
     * @return
     */
    @Override
    public String getPersonFaceBase64(String snPersonId) {
        String base64 = "";
        BasePersonFace face = getPersonFace(snPersonId);
        if (face == null || StringUtils.isEmpty(face.getImageUrl())) {
            return base64;
        }
        base64 = PlatformFileUtils.getImageBase64(face.getImageUrl());
        if (StringUtils.isNotBlank(base64) && DictConstants.Encrypted.ENABLE.equals(face.getEncrypted())) {
            base64 = PlatformCryptUtils.decryptImageBase64(base64);
        }
        return base64;
    }

    /**
     * 查询203设备接入园区平台
     * 
     * @param id 203设备接入园区平台ID
     * @return 203设备接入园区平台
     */
    @Override
    public DeviceAccessAdapter selectDeviceAccessAdapterById(String id) {
        return deviceAccessAdapterMapper.selectDeviceAccessAdapterById(id);
    }

    /**
     * 查询203设备接入园区平台列表
     * 
     * @param deviceAccessAdapter 203设备接入园区平台
     * @return 203设备接入园区平台
     */
    @Override
    public List<DeviceAccessAdapter> selectDeviceAccessAdapterList(DeviceAccessAdapter deviceAccessAdapter) {
        return deviceAccessAdapterMapper.selectDeviceAccessAdapterList(deviceAccessAdapter);
    }

    /**
     * 新增203设备接入园区平台
     * 
     * @param deviceAccessAdapter 203设备接入园区平台
     * @return 结果
     */
    @Override
    public int insertDeviceAccessAdapter(DeviceAccessAdapter deviceAccessAdapter) {
        deviceAccessAdapter.setCreateTime(DateUtils.getNowDate());
        return deviceAccessAdapterMapper.insertDeviceAccessAdapter(deviceAccessAdapter);
    }

    /**
     * 修改203设备接入园区平台
     * 
     * @param deviceAccessAdapter 203设备接入园区平台
     * @return 结果
     */
    @Override
    public int updateDeviceAccessAdapter(DeviceAccessAdapter deviceAccessAdapter) {
        deviceAccessAdapter.setUpdateTime(DateUtils.getNowDate());
        return deviceAccessAdapterMapper.updateDeviceAccessAdapter(deviceAccessAdapter);
    }

    /**
     * 删除203设备接入园区平台对象
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteDeviceAccessAdapterByIds(String[] ids) {
        return deviceAccessAdapterMapper.deleteDeviceAccessAdapterByIds(ids);
    }

    /**
     * 删除203设备接入园区平台信息
     * 
     * @param id 203设备接入园区平台ID
     * @return 结果
     */
    @Override
    public int deleteDeviceAccessAdapterById(String id) {
        return deviceAccessAdapterMapper.deleteDeviceAccessAdapterById(id);
    }

    /**
     * 检查接口时效性
     *
     * checkValidity
     * 
     * @param personId 人员id
     * @param date 日期
     * @return
     */
    @Override
    public boolean checkValidity(String personId, Date date) {
        boolean flag = true;
        return flag;
    }

    /**
     * 获取平台url
     *
     * @return
     */
    @Override
    public String getUrl() {
        String url = configService.selectConfigByKey(AdapterConstants.DevDictConf.BIAPWP_URL);
        return url;
    }

    /**
     * 获取配置信息
     *
     * @param sn
     * @return
     */
    @Override
    public DeviceAccessAdapter getBySn(String sn) {
        DeviceAccessAdapter adapter = null;
        DeviceAccessAdapter condition = new DeviceAccessAdapter();
        condition.setDeviceSn(sn);
        List<DeviceAccessAdapter> list = this.selectDeviceAccessAdapterList(condition);
        if (!CollectionUtils.isEmpty(list)) {
            adapter = list.get(0);
        }
        return adapter;
    }

    /**
     * base64ToFile
     * 
     * @param base64
     * @param savePath
     * @param fileName
     */
    @Override
    public File base64ToFile(String base64, String savePath, String fileName) {
        File file = null;
        // 创建文件目录
        String filePath = savePath + File.separator + fileName;
        File dir = new File(savePath);
        if (!dir.exists() && !dir.isDirectory()) {
            dir.mkdirs();
        }
        BufferedOutputStream bos = null;
        java.io.FileOutputStream fos = null;
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            file = new File(filePath);
            fos = new java.io.FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bytes);
        } catch (Exception e) {
            log.error("base64ToFile error : [{}]", e.getMessage());
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    log.error("base64ToFile error : [{}]", e.getMessage());
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    log.error("base64ToFile error : [{}]", e.getMessage());
                }
            }
        }
        return file;
    }

    /**
     * 封装结果 getResult
     *
     * @param isOk 结果状态
     * @param picId 图像id
     * @return
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String getResult(boolean isOk, String picId) {
        Map map = new HashMap(0);
        Map item = new HashMap(0);
        item.put("Result", isOk);
        item.put("PicID", picId);
        map.put("PictureAck", item);
        String result = JSON.toJSONString(map);
        return result;
    }

    /**
     * 处理日志 handleLog
     *
     * @param json 前端json字符串
     * @return
     */
    @Override
    public String handleLog(String json) {
        log.debug("receive the message [{}] that sent by 203 device", json);
        
        if (StringUtils.isBlank(json)) {
            log.warn("the message sent by 203 device is null");
            return getResult(true, StringUtils.EMPTY);
        }

        EventsInfoVO eventsInfo = JSON.parseObject(json, EventsInfoVO.class);
        // 上送识别记录为空
        boolean isEventsNull = null == eventsInfo || CollectionUtils.isEmpty(eventsInfo.getEvents());
        // 图片编号(设备端递增的)
        String picId = eventsInfo == null ? StringUtils.EMPTY : eventsInfo.getPicID();
        if (isEventsNull) {
            log.warn(" eventsInfo is null json:{}", json);
            String ret = getResult(true, picId);
            return ret;
        }

        // 设备事件信息
        DevResultInfoVO devResultInfo = eventsInfo.getEvents().get(0);
        // 事件公共信息
        CommInfoVO commInfo = devResultInfo.getCommInfo();
        // 通行信息
        AccessInfoVO accessInfo = devResultInfo.getAccessInfo();
        // 身份证信息
        CardsInfoVO cardsInfo = devResultInfo.getCardsInfo();
        // 抠图人脸信息
        FaceInfoVO faceInfo = devResultInfo.getObject();
        // 刷卡或者二维码信息
        CardVO card = devResultInfo.getCard();
        // 人脸识别比对结果
        List<FaceRecognizeInfoVO> recognizeResults = devResultInfo.getRecognizeResults();
        FaceRecognizeInfoVO faceRecognizeInfo =
            CollectionUtils.isEmpty(recognizeResults) ? null : recognizeResults.get(0);
        // 人员信息
        ResultPersonInfoVO resultPersonInfo = null == faceRecognizeInfo ? null : faceRecognizeInfo.getPersonInfo();

        // 查询设备信息是否存在，并根据设备所属租户设置租户信息隔离
        if (null == commInfo) {
            log.warn("The public information of the device event is empty, the device SN information cannot be obtained, and save the return log fails.");
            String ret = getResult(true, picId);
            return ret;
        }
        String deviceCode = commInfo.getSerialNo();
        if (StringUtils.isBlank(deviceCode)) {
            log.warn("The public information of the device event is empty, the device SN information cannot be obtained, and save the return log fails.");
            String ret = getResult(true, picId);
            return ret;
        }
        DeviceInfo deviceInfo = getDevBySn(deviceCode);
        if (deviceInfo == null) {
            log.warn("the deivice is unregistered,and its serial number is[{}],save the returend log fails", deviceCode);
            String ret = getResult(true, picId);
            return ret;
        }
        if (tenantProperties.getEnabled()) {
            TenantContextHolder.setTenantId(deviceInfo.getTenantId());
        }
        // 事件码
        String eventCode = devResultInfo.getCode();
        if (StringUtils.isBlank(eventCode)) {
            log.warn("The event code returned by the device[{}] is empty,eventCode:[{}]", deviceCode, eventCode);
            String ret = getResult(true, picId);
            return ret;
        }
        /* 定义日志属性 */
        // 交易时间
        Date receivedTime = getDate(devResultInfo);
        receivedTime = receivedTime == null ? DateUtils.getNowDate() : receivedTime;
        // 温度
        Double temperature = null == faceInfo ? null : faceInfo.getTemperature();
        Integer temperatureAlarm = null == faceInfo ? null : faceInfo.getTemperatureAlarm();
        // 现场照
        String sceneImage = StringUtils.isEmpty(devResultInfo.getImage()) ? null
            : devResultInfo.getImage().replaceAll("[\\s*\t\n\r]", "");
        // 人脸是否通过识别,0-对比不通过1-通过2-活体不通过
        Integer bioPass = devResultInfo.getPass();
        // 最终通行结果(1表示通行成功)
        int passResult = accessInfo.getPassResult();
        // 人员唯一标识
        String uniqueId = null;
        // 比对得分
        Double score = null;
        // 健康码状态码
        String healthCode = null;
        // 卡类型
        String cardType = null;

        // 健康码状态、人员唯一标识
        if (resultPersonInfo == null) {
            log.warn("the resultPersonInfo is null, picId:[{}]", picId);
        } else {
            uniqueId = resultPersonInfo.getId();
            healthCode = resultPersonInfo.getHealthCode();
        }

        // 人证比对事件
        if (AdapterConstants.OperMethod.FACE_IDCORD.equals(eventCode)) {
            if (cardsInfo != null) {
                uniqueId = StringUtils.trimToEmpty(cardsInfo.getNumber());
                // 身份证照片base64
                String profilePic = StringUtils.isEmpty(cardsInfo.getProfilePic()) ? null
                    : cardsInfo.getProfilePic().replaceAll("[\\s*\t\n\r]", "");
                sceneImage = StringUtils.isBlank(sceneImage) ? profilePic : sceneImage;
            }
            score =
                BigDecimal.valueOf(devResultInfo.getSearchScore()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        }

        // 人脸识别抠图事件
        if (AdapterConstants.OperMethod.FACE_CODE.equals(eventCode)
            || AdapterConstants.OperMethod.MATCH_CODE.equals(eventCode)) {
            if (0 == bioPass) {
                // 识别不通过的设备会默认返回top1,我们直接置空
                uniqueId = null;
            }
            if (faceRecognizeInfo == null) {
                log.warn("the faceRecognizeInfo is null, picId:[{}]", picId);
            } else {
                score = BigDecimal.valueOf(faceRecognizeInfo.getSearchScore()).setScale(2, BigDecimal.ROUND_HALF_UP)
                    .doubleValue();
            }
        }

        // 刷卡(二维码事件)
        if (AdapterConstants.OperMethod.CARD_CODE2.equals(eventCode)) {
            if (null != card) {
                cardType = card.getCardType();
                // QR:二维码；ID：刷卡信息；IC：身份证，此处认为二维码就是健康码
                if ("QR".equals(cardType) && null == healthCode) {
                    healthCode = String.valueOf(passResult);
                }
            }
        }

        // 检活结果
        String checkliveResult =
            null != bioPass && bioPass == 2 ? DictConstants.BioResult.NOTPASS : DictConstants.BioResult.PASS;
        // 通过结果
        String result = passResult == 1 ? DictConstants.BioResult.PASS : DictConstants.BioResult.NOTPASS;
        String healthCodeLogId = null;
        if (StringUtils.isNotEmpty(healthCode) && !"0".equals(healthCode)) {
            healthCodeLogId = saveHealthcodeLog(deviceCode, receivedTime, healthCode, uniqueId, temperature);
        }
        String validType = parseValidType(eventCode, cardType);
        MatchLogVO logVO = convertMatchLogVO(uniqueId, sceneImage, score, result, checkliveResult, deviceCode,
            receivedTime, temperature, temperatureAlarm, healthCodeLogId, validType);
        saveLog(logVO);
        log.debug(" the 203 equipment reporting identification record：picId:{} json:{}", picId, json);
        return getResult(true, picId);
    }

    /**
     * 保存健康码日志
     * 
     * @param deviceCode
     * @param receivedTime
     * @param healthCode
     * @param uniqueId
     * @param temperature
     * @return
     */
    private String saveHealthcodeLog(String deviceCode, Date receivedTime, String healthCode, String uniqueId,
        Double temperature) {
        PersonHealthCodeLog healthCodeLog = new PersonHealthCodeLog();
        healthCodeLog.setId(IdWorker.getNextStringId());
        healthCodeLog.setReceivedSeq(healthCodeLog.getId());
        healthCodeLog.setReceivedTime(receivedTime);
        healthCodeLog.setDeviceCode(deviceCode);
        healthCodeLog.setUniqueId(null == uniqueId ? StringUtils.EMPTY : uniqueId);
        healthCodeLog.setMessage(transHealthcodeMsg(healthCode));
        healthCodeLog.setResult(getHealthcodeResult(healthCode));
        if (null != temperature) {
            healthCodeLog.setTemperature(String.valueOf(temperature));
        }
        healthCodeLog.setCreateTime(DateUtils.getNowDate());
        healthCodeLogService.insertPersonHealthCodeLog(healthCodeLog);
        return healthCodeLog.getId();
    }

    /**
     * 获取健康码识别结果
     * 
     * @return
     */
    private String getHealthcodeResult(String healthCode) {
        return "1".equals(healthCode) ? DictConstants.BioResult.PASS : DictConstants.BioResult.NOTPASS;
    }

    /**
     * 健康码状态转换消息
     * 
     * @param healthCode
     * @return
     */
    private String transHealthcodeMsg(String healthCode) {
        String msg = StringUtils.EMPTY;
        if (StringUtils.isBlank(healthCode)) {
            return msg;
        }
        switch (healthCode) {
            // -1~8取值来自resultPersonInfo.getHealthCode()
            case "0":
                msg = MessageUtils.message("health.code.unopened") ;
                break;
            case "1":
                msg = MessageUtils.message("health.code.blue");
                break;
            case "2":
                msg = MessageUtils.message("health.code.yellow");
                break;
            case "3":
                msg = MessageUtils.message("health.code.red");
                break;
            case "4":
                msg = MessageUtils.message("health.code.orange");
                break;
            case "5":
                msg = MessageUtils.message("health.code.null");
                break;
            case "6":
                msg = MessageUtils.message("health.code.not.applied");
                break;
            case "7":
                msg = MessageUtils.message("health.code.obtain.exception");
                break;
            case "8":
                msg = MessageUtils.message("health.code.risk.area");
                break;
            case "-1":
                msg = MessageUtils.message("health.code.protocol.change");
                break;
            // 设备直接扫描二维码通行，取值来自AccessInfo.PassResult
            case "23":
                msg = MessageUtils.message("health.code.verify.failed");
                break;
            default:
                msg = MessageUtils.message("health.code.unknown");
                break;
        }
        return msg;
    }

    /**
     * 保存日志 saveLog
     *
     * @param logVO 日志
     */
    private void saveLog(MatchLogVO logVO) {
        String bizContent = getBizContent(logVO);
        log.debug("The content for the log：[{}]", bizContent);
        AjaxResult ajaxResult = tradelogHttpHandler.savePersonFaceSearchBakLog(bizContent);
        String str = JSONObject.toJSONString(ajaxResult);
        log.info(" The result for saving the log： ajaxResult:{}", str);
    }

    /**
     * 获取日志内容 getBizContent
     *
     * @param logVO 日志
     * @return
     */
    private String getBizContent(MatchLogVO logVO) {
        String bizContent = JSONObject.toJSONString(logVO);
        return bizContent;
    }

    /**
     * 转换日期 getDate
     *
     * @param devResultInfo 设备日期
     * @return
     */
    private Date getDate(DevResultInfoVO devResultInfo) {
        if (devResultInfo == null || devResultInfo.getUtc() == null) {
            return null;
        }
        try {
            String timeZone = StringUtils.trimToEmpty(devResultInfo.getTimeZone());
            timeZone = timeZone.length() == 0 ? AdapterConstants.TIME_ZONE : timeZone;
            Date date = new Date(devResultInfo.getUtc() * 1000);
            return formateTimeZone(timeZone, date);
        } catch (Exception e) {
            log.error("getDate: [{}]", e.getMessage());
            return null;
        }
    }

    /**
     * 数据转换 convertMatchLogVO
     *
     * @param uniqueId 人员唯一标识
     * @param sceneImage 场景图片
     * @param sceneStockScore 分数
     * @param result 结果
     * @param checkliveResult 检活结果
     * @param deviceCode 设备编码
     * @param receivedTime 识别时间
     * @param temperature 温度
     * @param temperatureAlarm 测温告警结果
     * @param healthcodeLogId 健康码日志ID
     * @param validType 核验类型
     * @return
     */
    private MatchLogVO convertMatchLogVO(String uniqueId, String sceneImage, Double sceneStockScore, String result,
        String checkliveResult, String deviceCode, Date receivedTime, Double temperature, Integer temperatureAlarm,
        String healthcodeLogId, String validType) {
        MatchLogVO vo = new MatchLogVO();
        vo.setId(IdWorker.getNextStringId());
        vo.setReceivedSeq(IdWorker.getNextStringId());
        vo.setSceneImage(sceneImage);
        vo.setSceneStockScore(null == sceneStockScore ? null : String.valueOf(sceneStockScore));
        vo.setResult(result);
        vo.setCheckliveResult(checkliveResult);
        vo.setHealthcodeLogId(healthcodeLogId);
        vo.setDeviceCode(deviceCode);
        vo.setReceivedTime(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, receivedTime));
        vo.setTemperature(null == temperature ? null : String.valueOf(temperature));
        vo.setTemperatureResult(null == temperatureAlarm ? null : String.valueOf(temperatureAlarm));
        vo.setUniqueId(uniqueId);
        vo.setValidType(validType);
        return vo;
    }

    /**
     * 转换时间 formateTimeZone
     *
     * @param fromTimeZone 时区
     * @param inDate 日期
     * @return
     */
    public static Date formateTimeZone(String fromTimeZone, Date inDate) {
        Date date = new Date();
        try {
            String toTimeZone = "GMT+0";
            String strDate = AdapterUtils.getDateFormatForSecond(inDate);
            SimpleDateFormat format = new SimpleDateFormat(AdapterUtils.PATTERN_YMD_HMS);
            format.setTimeZone(TimeZone.getTimeZone(fromTimeZone));
            date = format.parse(strDate);
            format.setTimeZone(TimeZone.getTimeZone(toTimeZone));
            strDate = format.format(date);
            date = toDate(strDate);
        } catch (Exception e) {
            log.error("formateTimeZone error:[{}]", e.toString());
        }
        return date;
    }

    /**
     * 转换日期 toDate
     *
     * @param beginDate
     * @return
     */
    public static Date toDate(String beginDate) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(AdapterUtils.PATTERN_YMD_HMS);
            Date start = format.parse(beginDate);
            return start;
        } catch (Exception e) {
            log.error("toDate error:[{}]", e.toString());
        }
        return null;
    }

    /**
     * 处理图像
     *
     * @param imgPath 路径
     */
    @Override
    public File dealImage(String imgPath) {
        if (StringUtils.isEmpty(imgPath)) {
            return null;
        }
        File file = new File(imgPath);
        if (!AdapterUtils.checkFileSize(file, AdapterConstants.MAX_FILESIZE, AdapterConstants.Unit.K)) {
            log.info("The picture is bigger than 500k,start to compres it,[{}]", imgPath);
            try {
                Thumbnails.of(imgPath).size(AdapterConstants.IMG_WIDTH, AdapterConstants.IMG_HEIGH).toFile(imgPath);
            } catch (Exception e) {
                String msg = MessageUtils.message("picture.compres.failed.format.wrong");
                log.error("msg:[{},[]]", msg, e.toString());
            }
            return new File(imgPath);
        }
        return file;
    }

    /**
     * 验证接口时效性
     *
     * @param denId id
     * @return
     */
    @Override
    public boolean checkValid(String denId) {
        boolean flag = false;
        if (StringUtils.isEmpty(denId)) {
            return false;
        }
        int len = 3;
        String[] arr = denId.split("_");
        if (arr.length < len) {
            return false;
        }
        String paramDate = arr[2];
        Date nowDate = new Date();
        try {
            Date inDate = AdapterUtils.getDate(paramDate);
            Date lastDate = AdapterUtils.addDate(inDate, AdapterConstants.VALID_HOUR_NUM);
            if (lastDate == null) {
                return false;
            }
            if (nowDate.before(lastDate)) {
                flag = true;
            }
        } catch (Exception e) {
            log.error("checkValid is error :[{}]", e.toString());
        }

        return flag;
    }

    /**
     * 检查并创建配置信息
     *
     * @param sn 序列号
     */
    @Override
    public boolean checkAndCreateAdapterInfo(String sn) {
        String deviceName = "";
        DeviceAccessAdapter adapter = null;
        try {
            DeviceInfo DeviceInfo = getDevBySn(sn);
            if (DeviceInfo == null) {
                return false;
            }
            if (tenantProperties.getEnabled()) {
                TenantContextHolder.setTenantId(DeviceInfo.getTenantId());
            }
            deviceName = DeviceInfo.getDeviceName();
            adapter = getBySn(sn);
            if (adapter != null) {
                return true;
            }
            adapter = new DeviceAccessAdapter();
            Date date = new Date();
            adapter.setId(IdWorker.getNextStringId());
            adapter.setSeriaNum(AdapterConstants.DEF_SERINUM);
            adapter.setDeviceSn(sn);
            adapter.setDeviceName(deviceName);
            adapter.setCreateBy(AdapterConstants.DEF_ADMIN);
            adapter.setUpdateBy(AdapterConstants.DEF_ADMIN);
            adapter.setCreateTime(date);
            adapter.setUpdateTime(date);
            int row = insertDeviceAccessAdapter(adapter);
            return row > 0;
        } catch (Exception e) {
            log.error("checkAndCreateAdapterInfo error : [{}]", e.toString());
            return false;
        }
    }

    /**
     * 转换核验方式
     * 
     * @param eventCode 事件码
     * @param cardType 卡类型
     * @return
     */
    private String parseValidType(String eventCode, String cardType) {
        String validType = StringUtils.EMPTY;
        if (StringUtils.isBlank(eventCode)) {
            return validType;
        }
        switch (eventCode) {
            case AdapterConstants.OperMethod.FACE_IDCORD:
                // 身份证核验
                validType = PassValidType.ID_CARD;
                break;
            case AdapterConstants.OperMethod.FACE_CODE:
            case AdapterConstants.OperMethod.MATCH_CODE:
                // 刷脸核验
                validType = PassValidType.FACE;
                break;
            case AdapterConstants.OperMethod.CARD_CODE2:
                // 健康码核验
                if ("QR".equals(cardType)) {
                    validType = PassValidType.HEALTH_CODE;
                }
                break;
            default:
                break;
        }
        log.info("change the verify type,eventCode:[{}],cardType:[{}], validType:[{}]", eventCode, cardType, validType);
        return validType;
    }
}
