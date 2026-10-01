package cn.eyecool.server.handler;

import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.DictConstants.HttpInterfaceTransCode;
import cn.eyecool.common.constant.UserConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.entity.SysDictData;
import cn.eyecool.common.core.domain.entity.SysTenant;
import cn.eyecool.common.core.domain.http.HttpAjaxResult;
import cn.eyecool.common.core.domain.http.StandardHttpParam;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.sign.Md5Utils;
import cn.eyecool.system.domain.AppInfo;
import cn.eyecool.system.domain.AppInterfaceAuth;
import cn.eyecool.system.domain.SysTenantInterface;
import cn.eyecool.system.service.*;
import org.apache.commons.validator.routines.LongValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 标准HTTP请求API接口服务
 *
 * @author admin
 * @date 2019年11月4日
 */
@Component
public class RequestDistributeHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RequestDistributeHandler.class);
    // 客户端和服务器允许的最大时间差,设置10分钟
    private static final Long MAX_TIME_DIFF_CLIENT_SERVER = 10 * 60 * 1000L;
    // http接口请求唯一标识缓存Key前缀
    private static final String HTTP_NONCE_CACHE_KEY_PREFIX = "http:nonce:";

    /** 是否开启时间戳、唯一请求和签名验证(只能在测试环境压测时关闭) */
    @Value("${eyecool.apiAuthEnabled:true}")
    private boolean authEnabled;

    @Autowired
    private RedisCache redisCache;
    @Autowired
    private TenantProperties tenantProperties;
    @Autowired
    private ISysDictTypeService dictTypeService;
    @Autowired
    private IAppInfoService appInfoService;
    @Autowired
    private IAppInterfaceAuthService appInterfaceAuthService;
    @Autowired
    private ISysTenantService tenantService;
    @Autowired
    private ISysTenantInterfaceService tenantInterfaceService;
    @Autowired
    private BasePersonInfoHttpHandler basePersonInfoHttpHandler;
    @Autowired
    private ChannelBusiHttpHandler channelBusiHttpHandler;
    @Autowired
    private SubtreasuryBusiHttpHandler subtreasuryBusiHttpHandler;
    @Autowired
    private BioTradeHttpHandler bioTradeHttpHandler;
    @Autowired
    private BioTradeToolHttpHandler bioTradeToolHandler;
    @Autowired
    private PersonLiveUpdateHandler personLiveUpdateHandler;
    @Autowired
    private DeviceHttpHandler deviceHttpHandler;
    @Autowired
    private MsgSendHttpHandler msgSendHttpHandler;
    @Autowired
    private OcrLogHttpHandler ocrLogHttpHandler;
    @Autowired
    private TradelogHttpHandler tradelogHttpHandler;
    @Autowired
    private HealthCodeHandler healthCodeHandler;

    /**
     * 请求分发处理器
     *
     * @param httpParam
     * @param request
     * @param response
     * @return
     */
    public AjaxResult distributeHandler(StandardHttpParam httpParam, HttpServletRequest request,
        HttpServletResponse response) {
        // 处理公共的请求校验
        AjaxResult ajaxResult = validateParamAndAuth(httpParam);
        if (!HttpAjaxResult.HTTP_SUCC_CODE.equals(ajaxResult.get(AjaxResult.CODE_TAG))) {
            return ajaxResult;
        }
        return doDistributeHandler(httpParam, request, response);
    }

    /**
     * 请求分发处理器
     *
     * @param httpParam
     * @param request
     * @param response
     * @return
     */
    private AjaxResult doDistributeHandler(StandardHttpParam httpParam, HttpServletRequest request,
        HttpServletResponse response) {
        AjaxResult ajaxResult = null;
        // 调用具体的接口处理请求
        switch (httpParam.getTransCode()) {
            case HttpInterfaceTransCode.PERSON_INSERT:// 人员基本信息新增
                ajaxResult = basePersonInfoHttpHandler.insertPersonInfo(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_UPDATE:// 人员基本信息修改
                ajaxResult = basePersonInfoHttpHandler.updatePersonInfo(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_DELETE:// 人员基本信息删除
                ajaxResult = basePersonInfoHttpHandler.deletePersonInfo(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_SELECT:// 人员基本信息查询
                ajaxResult = basePersonInfoHttpHandler.getBasePersonInfo(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_FLAG_UPDATE:// 修改人员标记（黑白名单）
                ajaxResult = basePersonInfoHttpHandler.updatePersonFlag(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_LIVE_UPDATE: // 基础数据实时同步
                ajaxResult = personLiveUpdateHandler.liveUpdateBasePersonInfo(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_FACE_VERIFY:// 人脸1:1认证
                ajaxResult = bioTradeHttpHandler.verifyPersonFace(httpParam.getBizContent(), request);
                break;
            case HttpInterfaceTransCode.PERSON_FACE_FEATURE:// 人脸特征提取
                ajaxResult = bioTradeToolHandler.getPersonFaceFeature(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_FINGER_FEATURE:// 指纹特征提取
                ajaxResult = bioTradeToolHandler.getPersonFingerFeature(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_IRIS_FEATURE:// 虹膜特征提取
                ajaxResult = bioTradeToolHandler.getPersonIrisFeature(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_FACE_IMG_COMPARE:// 人脸比对
                ajaxResult = bioTradeToolHandler.compareTwoFaceImg(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_FINGER_IMG_COMPARE:// 指纹比对
                ajaxResult = bioTradeToolHandler.compareTwoFingerImg(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_IRIS_IMG_COMPARE:// 虹膜比对
                ajaxResult = bioTradeToolHandler.compareTwoIrisImg(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_FACE_CHECKLIVE:// 人脸检活
                ajaxResult = bioTradeToolHandler.checklivePersonFaceOrVideo(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_FACE_QUALITY_DETECT:// 人脸图片质量检测
                ajaxResult = bioTradeToolHandler.personFaceQualityDetect(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_FINGER_QUALITY_DETECT:// 指纹图片质量检测
                ajaxResult = bioTradeToolHandler.personFingerQualityDetect(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_FACE_CHECKLIVE_AND_COMPARE:// 人脸视频检活和比对
                ajaxResult = bioTradeToolHandler.personFaceVideoCheckliveAndCompare(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_FACE_RECOG:// 人脸1:N识别
                ajaxResult = bioTradeHttpHandler.recogPersonFace(httpParam.getBizContent(), request);
                break;
            case HttpInterfaceTransCode.PERSON_FACE_OPEN:// 开通人脸
                ajaxResult = channelBusiHttpHandler.openPersonFace(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_FACE_CLOSE:// 关闭人脸
                ajaxResult = channelBusiHttpHandler.closePersonFace(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_FINGER_VERIFY:// 指纹1:1认证
                ajaxResult = bioTradeHttpHandler.verifyPersonFinger(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_FINGER_RECOG:// 指纹1:N识别
                ajaxResult = bioTradeHttpHandler.recogPersonFinger(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_FINGER_OPEN:// 开通指纹
                ajaxResult = channelBusiHttpHandler.openPersonFinger(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_FINGER_CLOSE:// 关闭指纹
                ajaxResult = channelBusiHttpHandler.closePersonFinger(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_IRIS_VERIFY:// 虹膜1:1认证
                ajaxResult = bioTradeHttpHandler.verifyPersonIris(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_IRIS_RECOG:// 虹膜1:N识别
                ajaxResult = bioTradeHttpHandler.recogPersonIris(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_IRIS_OPEN:// 开通虹膜
                ajaxResult = channelBusiHttpHandler.openPersonIris(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_IRIS_CLOSE:// 关闭虹膜
                ajaxResult = channelBusiHttpHandler.closePersonIris(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_FVEIN_OPEN:// 开通指静脉
                ajaxResult = channelBusiHttpHandler.openPersonFvein(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_FVEIN_CLOSE:// 关闭指静脉
                ajaxResult = channelBusiHttpHandler.closePersonFvein(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_FACE_IRIS_OPEN:// 开通人脸虹膜多模态
                ajaxResult = channelBusiHttpHandler.openPersonFaceIris(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_FACE_IRIS_CLOSE:// 关闭人脸虹膜多模态
                ajaxResult = channelBusiHttpHandler.closePersonFaceIirs(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.DEL_CHANNEL_PERSON:// 删除场景库人员
                ajaxResult = channelBusiHttpHandler.deleteChannelPerson(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.QUERY_CHANNEL_PERSON:// 删除场景库人员
                ajaxResult = channelBusiHttpHandler.queryChannelPersonExists(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.MESSAGE_EMAIL: // 邮件消息推送
                ajaxResult = msgSendHttpHandler.sendMailMessage(httpParam.getBizContent(), request);
                break;
            case HttpInterfaceTransCode.MESSAGE_SMS: // 短信消息推送
                ajaxResult = msgSendHttpHandler.sendSmsMessage(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.MESSAGE_WECHAT: // 微信消息推送
                ajaxResult = msgSendHttpHandler.sendWechatMessage(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.MESSAGE_DING: // 钉钉消息推送
                ajaxResult = msgSendHttpHandler.sendDingMessage(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.MESSAGE_DING_UPOLOAD: // 钉钉媒体文件上传
                ajaxResult = msgSendHttpHandler.uploadDingMedia(httpParam.getBizContent(), request);
                break;
            case HttpInterfaceTransCode.MESSAGE_DING_RESULT: // 钉钉消息推送结果获取
                ajaxResult = msgSendHttpHandler.getSendDingMsgResult(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.SUB_TREASURY_OPERATE:// 子场景操作
                ajaxResult = subtreasuryBusiHttpHandler.operateSubtreasury(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_SUB_TREASURY_OPERATE: // 子场景人员操作
                ajaxResult = subtreasuryBusiHttpHandler.operatePersonSubtreasury(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.CLIENT_UPGRADE_SIGNAL: // 客户端设备版本更新查询
                ajaxResult = deviceHttpHandler.checkDeviceLastUpgradeTask(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.CLIENT_VERSION_DOWNLOAD: // 客户端设备版本下载
                ajaxResult = deviceHttpHandler.downloadVersion(httpParam.getBizContent(), request, response);
                if (HttpAjaxResult.HTTP_SUCC_CODE.equals(ajaxResult.get(AjaxResult.CODE_TAG))) {
                    ajaxResult = null;
                }
                break;
            case HttpInterfaceTransCode.CLIENT_UPGRADE_RESULT_BAK: // 客户端设备版本更新结果回传
                ajaxResult = deviceHttpHandler.postbackUpgradeResult(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.QUREY_DEVICE:// 查询设备列表
                ajaxResult = deviceHttpHandler.queryDevice(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_IDENTITY_VERIFICATION:// 身份核验
                ajaxResult = bioTradeHttpHandler.personIdentityVerification(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.MULIT_IRIS_FACE_REGISTER:// 人脸虹膜多模态注册
                ajaxResult = basePersonInfoHttpHandler.irisFaceRegister(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_FACE_REGISTER:// 人脸注册
                ajaxResult = basePersonInfoHttpHandler.faceRegister(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_FACE_SEARCH_LOG_BAK: // 保存人脸识别回传日志
                ajaxResult = tradelogHttpHandler.savePersonFaceSearchBakLog(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_FACE_IRIS_MULTI_LOG_BAK:// 人脸多模态识别日志回传
                ajaxResult = tradelogHttpHandler.savePersonFaceIrisMultiBakLog(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_FACE_MATCH_LOG_BAK:// 人脸1v1比对日志回传
                ajaxResult = tradelogHttpHandler.savePersonFaceMatchBakLog(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_FACE_MATCH_LOG_QUERY:// 人脸1v1比对日志查询
                ajaxResult = tradelogHttpHandler.personFaceMatchLogQuery(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_FACE_SEARCH_LOG_QUERY:// 人脸识别日志查询
                ajaxResult = tradelogHttpHandler.personFaceSearchLogQuery(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.PERSON_FACE_IRIS_MULTI_LOG_QUERY:// 人脸虹膜多模态识别日志查询
                ajaxResult = tradelogHttpHandler.personFaceIrisLogQuery(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.OCR_ATTR_DETECT_LOG: // OCR属性识别
                ajaxResult = ocrLogHttpHandler.getOcrAttr(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.HEALTH_CODE_SEARCH:// 健康码查询
                ajaxResult = healthCodeHandler.healthCodeSearch(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.MULIT_IRIS_FACE_SEARCH:// 人脸虹膜多模态搜索1：N
                ajaxResult = bioTradeHttpHandler.irisFaceSearch(httpParam.getBizContent());
                break;
            case HttpInterfaceTransCode.MULIT_IRIS_FACE_VERIFY:// 人脸虹膜多模态比对1：1
                ajaxResult = bioTradeHttpHandler.irisFaceVerify(httpParam.getBizContent());
                break;
            default:
                ajaxResult = HttpAjaxResult.transCodeNotExistsError(httpParam.getTransCode());
                break;
        }
        return ajaxResult;
    }

    /**
     * 公共请求参数和接口校验 返回AjaxResult code=="0"表示校验通过
     *
     * @param httpParam
     * @return
     */
    private AjaxResult validateParamAndAuth(StandardHttpParam httpParam) {
        AjaxResult ajaxResult = null;
        // 1、公共参数合法性校验
        ajaxResult = globalValidate(httpParam);
        if (!HttpAjaxResult.HTTP_SUCC_CODE.equals(ajaxResult.get(AjaxResult.CODE_TAG))) {
            return ajaxResult;
        }
        AppInfo app = (AppInfo)ajaxResult.get(AjaxResult.DATA_TAG);
        // 2、校验时间戳是否合法
        String timestamp = httpParam.getTimestamp();
        long currentTime = System.currentTimeMillis();
        try {
            long requestTime = Long.valueOf(timestamp);
            if (authEnabled && Math.abs(requestTime - currentTime) > MAX_TIME_DIFF_CLIENT_SERVER) {
                ajaxResult = HttpAjaxResult.timestampValidError();
                LOG.error("code:{},msg:{}", ajaxResult.get(AjaxResult.CODE_TAG), ajaxResult.get(AjaxResult.MSG_TAG));
                return ajaxResult;
            }
        } catch (Exception e) {
            ajaxResult = HttpAjaxResult.globalValidError(MessageUtils.message("request.dis.handler.timestamp.format.error"));
            LOG.error("code:{},msg:{}", ajaxResult.get(AjaxResult.CODE_TAG), ajaxResult.get(AjaxResult.MSG_TAG));
            return ajaxResult;
        }

        // 3、校验请求唯一码是否已存在(防止重复请求)
        String nonce = httpParam.getNonce();
        // 使用缓存存储用过的nonce[使用timestamp来优化nonce的存储,存储10分钟时间差范围内的nonce即可]
        // 这里的key一定只能使用参与MD5签名的字段或者MD5签名结果计算
        String key =
            Md5Utils.hash(httpParam.getAppKey() + httpParam.getTransCode() + nonce + timestamp + httpParam.getSign());
        Object cacheNonce = redisCache.getCacheObject(HTTP_NONCE_CACHE_KEY_PREFIX + key);
        if (authEnabled && null != cacheNonce) {
            ajaxResult = HttpAjaxResult.nonceRepeatError();
            LOG.error("code:{},msg:{}", ajaxResult.get(AjaxResult.CODE_TAG), ajaxResult.get(AjaxResult.MSG_TAG));
            return ajaxResult;
        }
        // 将请求唯一码放入缓存
        redisCache.setCacheObject(HTTP_NONCE_CACHE_KEY_PREFIX + key, nonce, 10, TimeUnit.MINUTES);

        // 4、为了尽早设置TenantContextHolder，本步app校验提前到公共参数校验中(第1步去做)，见checkAppAndSetTenantContext方法

        // 5、验证交易接口是否存在
        String transCode = httpParam.getTransCode();
        List<SysDictData> dictDataList = dictTypeService.selectDictDataByType(DictConstants.HTTP_INTREFACE_DICT_TYPE);
        boolean transCodeExists = dictDataList.stream().anyMatch(it -> transCode.equals(it.getDictValue()));
        if (!transCodeExists) {
            ajaxResult = HttpAjaxResult.transCodeNotExistsError(transCode);
            LOG.error("code:{},msg:{}", ajaxResult.get(AjaxResult.CODE_TAG), ajaxResult.get(AjaxResult.MSG_TAG));
            return ajaxResult;
        }

        // 6、验证签名
        String validateSign = generateSign(httpParam, app.getAppSecret());
        if (authEnabled && !validateSign.equals(httpParam.getSign())) {
            ajaxResult = HttpAjaxResult.signIllegalError();
            LOG.error("code:{},msg:{}", ajaxResult.get(AjaxResult.CODE_TAG), ajaxResult.get(AjaxResult.MSG_TAG));
            return ajaxResult;
        }

        // 7、验证是否有接口权限
        // 7.1 验证多租户模式下，该租户是否具备当前接口的代理权限
        if (tenantProperties.getEnabled() && !UserConstants.SUPER_TENANT.equals(TenantContextHolder.getTenantId())) {
            SysTenantInterface tenantInterface =
                tenantInterfaceService.selectSysTenantInterface(TenantContextHolder.getTenantId(), transCode);
            if (null == tenantInterface) {
                ajaxResult = HttpAjaxResult.interfaceAuthFailedError(transCode);
                LOG.error("code:[{}],msg:[{}], Current tenant [{}] does not have proxy permission for interface [{}],", ajaxResult.get(AjaxResult.CODE_TAG),
                    ajaxResult.get(AjaxResult.MSG_TAG), TenantContextHolder.getTenantId(), transCode);
                return ajaxResult;
            }
            Date expireTime = tenantInterface.getExpireTime();
            if (null != expireTime && expireTime.getTime() < currentTime) {
                ajaxResult = HttpAjaxResult.interfaceAuthExpireError(transCode);
                LOG.error("code:[{}],msg:[{}], The current tenant [{}] has expired proxy permission to interface [{}],", ajaxResult.get(AjaxResult.CODE_TAG),
                    ajaxResult.get(AjaxResult.MSG_TAG), TenantContextHolder.getTenantId(), transCode);
                return ajaxResult;
            }
        }
        // 7.2 验证当前appKey是否分配了当前接口的访问权限
        AppInterfaceAuth appInterfaceAuth =
            appInterfaceAuthService.selectAppInterfaceAuth(app.getId(), app.getAppKey(), transCode);
        if (null == appInterfaceAuth) {
            ajaxResult = HttpAjaxResult.interfaceAuthFailedError(transCode);
            LOG.error("code:{},msg:{}", ajaxResult.get(AjaxResult.CODE_TAG), ajaxResult.get(AjaxResult.MSG_TAG));
            return ajaxResult;
        }
        Date transEndTime = appInterfaceAuth.getTransEndTime();
        if (null != transEndTime && transEndTime.getTime() < currentTime) {
            ajaxResult = HttpAjaxResult.interfaceAuthExpireError(transCode);
            LOG.error("code:{},msg:{}", ajaxResult.get(AjaxResult.CODE_TAG), ajaxResult.get(AjaxResult.MSG_TAG));
            return ajaxResult;
        }
        return HttpAjaxResult.httpSuccess(app);
    }

    /**
     * 公共参数合法性校验 返回AjaxResult code=="0"表示校验通过
     *
     * @param param
     * @return
     */
    private AjaxResult globalValidate(StandardHttpParam param) {
        // 校验AppKey并设置租户ID
        AjaxResult ajaxResult = checkAppAndSetTenantContext(param);
        if (!HttpAjaxResult.HTTP_SUCC_CODE.equals(ajaxResult.get(AjaxResult.CODE_TAG))) {
            LOG.error("code:{},msg:{}", ajaxResult.get(AjaxResult.CODE_TAG), ajaxResult.get(AjaxResult.MSG_TAG));
            return ajaxResult;
        }
        // 其他参数校验
        String msg = null;
        if (StringUtils.isBlank(param.getSign())) {
            msg  = MessageUtils.message("request.dis.handler.sign.empty");
        } else if (StringUtils.isBlank(param.getTimestamp())) {
            msg  = MessageUtils.message("request.dis.handler.timestamp.empty");
        } else if (param.getTimestamp().length() != 13 || !LongValidator.getInstance().isValid(param.getTimestamp())) {
            msg  = MessageUtils.message("request.dis.handler.timestamp.length.invalid");
        } else if (StringUtils.isBlank(param.getNonce())) {
            msg  = MessageUtils.message("request.dis.handler.nonce.empty");
        } else if (param.getNonce().length() > 48) {
            msg  = MessageUtils.message("request.dis.handler.nonce.length.invalid");
        } else if (StringUtils.isBlank(param.getTransCode())) {
            msg  = MessageUtils.message("request.dis.handler.transcode.empty");
        } else if (param.getTransCode().length() > 48) {
            msg  = MessageUtils.message("request.dis.handler.transcode.length.invalid");
        } else if (StringUtils.isBlank(param.getBizContent())) {
            msg  = MessageUtils.message("request.dis.handler.bizcontent.empty");
        } else {
            return ajaxResult;
        }
        ajaxResult = HttpAjaxResult.globalValidError(msg);
        LOG.error("code:{},msg:{}", ajaxResult.get(AjaxResult.CODE_TAG), ajaxResult.get(AjaxResult.MSG_TAG));
        return ajaxResult;
    }

    /**
     * 校验AppKey并设置租户ID
     *
     * @param param
     * @return
     */
    private AjaxResult checkAppAndSetTenantContext(StandardHttpParam param) {
        if (StringUtils.isBlank(param.getAppKey())) {
            String msg =  MessageUtils.message("request.dis.handler.appkey.empty");
            return HttpAjaxResult.globalValidError(msg);
        }
        if (param.getAppKey().length() != 8) {
            String msg  = MessageUtils.message("request.dis.handler.appkey.length.invalid");
            return HttpAjaxResult.globalValidError(msg);
        }
        // 验证appKey是否存在(这一步校验 放在此处是为了尽量早的设置TenantContextHolder)
        AppInfo appInfo = appInfoService.selectAppInfoByAppkey(param.getAppKey());
        if (null == appInfo) {
            return HttpAjaxResult.appKeyNotExistsError();
        }
        // 开启多租户支持，为线程设置tenantId
        if (tenantProperties.getEnabled()) {
            String tenantId = appInfo.getTenantId();
            TenantContextHolder.setTenantId(tenantId);
            LOG.info("The current request appKey: [{}], the corresponding tenant ID is: [{}]", param.getAppKey(), tenantId);
            // 验证租户是否有效
            SysTenant tenant = tenantService.selectSysTenantByTenantId(tenantId);
            boolean isSuperTenant = UserConstants.SUPER_TENANT.equals(tenantId);
            // 超级租户不做判断，永远有效
            if (!isSuperTenant) {
                if (StringUtils.isNull(tenant)) {
                    LOG.error("appKey=[{}]'s tenant [{}] information does not exist!", param.getAppKey(), tenantId);
                    
                    return HttpAjaxResult.businessError(MessageUtils.message("request.dis.handler.tenant.not.exists"));
                }
                if (DictConstants.TenantState.FROZEN.equalsIgnoreCase(tenant.getTenantState())) {
                    LOG.error("The tenant [{}] to which appKey=[{}] belongs is frozen!", param.getAppKey(), tenantId);
                    return HttpAjaxResult.businessError(MessageUtils.message("request.dis.handler.tenant.locked"));
                }
                if (System.currentTimeMillis() < tenant.getEffectiveTime().getTime()) {
                    LOG.error("appKey=[{}]'s tenant [{}] has not taken effect yet", param.getAppKey(), tenantId);
                    return HttpAjaxResult.businessError(MessageUtils.message("request.dis.handler.tenant.invalid"));
                }
                if (System.currentTimeMillis() > tenant.getExpireTime().getTime()) {
                    LOG.error("appKey=[{}]'s tenant [{}] has expired!", param.getAppKey(), tenantId);
                    return HttpAjaxResult.businessError(MessageUtils.message("request.dis.handler.tenant.expired"));
                }
            }
        }
        return HttpAjaxResult.httpSuccess(appInfo);
    }

    /**
     * 服务端生成签名, 使用公共参数即可
     *
     * @return
     */
    private String generateSign(StandardHttpParam httpParam, String appSecrect) {
        String appKey = httpParam.getAppKey();
        String timestamp = httpParam.getTimestamp();
        String transCode = httpParam.getTransCode();
        String nonce = httpParam.getNonce();
        // 源字符串拼接,按照请求参数名的字母升序排列非空请求参数(appkey->nonce->timestamp->transCode)
        String originalSignStr =
            "appkey=" + appKey + "&nonce=" + nonce + "&timestamp=" + timestamp + "&transCode=" + transCode;
        // 拼接appSecrect
        originalSignStr = originalSignStr + "&appSecrect=" + appSecrect;
        // 进行MD5加密并转为大写
        String md5 = Md5Utils.hash(originalSignStr);
        return md5.toUpperCase();
    }

    public static void main(String[] args) {
        String appKey = "FW2X38cI";
        String transCode = "CLIENT_VERSION_DOWNLOAD";
        long time = System.currentTimeMillis();
        String timestamp = String.valueOf(time);
        String nonce = String.valueOf(time);
        // 源字符串拼接,按照请求参数名的字母升序排列非空请求参数(appkey->nonce->timestamp->transCode)
        String originalSignStr =
            "appkey=" + appKey + "&nonce=" + nonce + "&timestamp=" + timestamp + "&transCode=" + transCode;
        // 拼接appSecrect
        originalSignStr = originalSignStr + "&appSecrect=9d29eb02d9fdd60c4041ff56f77db527ada492a8";
        // 进行MD5加密并转为大写
        String md5 = Md5Utils.hash(originalSignStr);
        System.err.println(time);
        System.err.println(md5.toUpperCase());
    }

}
