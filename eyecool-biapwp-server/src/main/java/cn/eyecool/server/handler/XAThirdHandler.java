/*******************************************************************************
 * 系统名称 ： 后台管理系统 开发部门 ： 山东眼神智能科技有限公司 文件名称 : XAThirdHandler
 ******************************************************************************/
package cn.eyecool.server.handler;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eastcompeace.kmc.EcpAESCipherTools;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.basedata.event.PersonChangeEventPublishService;
import cn.eyecool.basedata.service.IBasePersonInfoService;
import cn.eyecool.common.constant.Constants;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.scene.domain.ChannelBusiness;
import cn.eyecool.scene.domain.ChannelInfo;
import cn.eyecool.scene.domain.ChannelSubtreasuryBusi;
import cn.eyecool.scene.domain.ChannelSubtreasuryInfo;
import cn.eyecool.scene.mapper.ChannelBusinessMapper;
import cn.eyecool.scene.mapper.ChannelInfoMapper;
import cn.eyecool.scene.mapper.ChannelSubtreasuryBusiMapper;
import cn.eyecool.scene.mapper.ChannelSubtreasuryInfoMapper;
import cn.eyecool.server.http.param.CardPassPerson;
import cn.eyecool.server.http.param.CardPassResHeader;
import cn.eyecool.server.http.param.DoorArea;
import cn.eyecool.system.service.ISysConfigService;
import cn.eyecool.tradelog.domain.PersonFaceSearchLog;
import cn.eyecool.tradelog.domain.XAAuthorityStatusBody;
import cn.eyecool.tradelog.domain.XADoorArea;
import cn.eyecool.tradelog.service.IPersonFaceSearchLogService;
import lombok.extern.slf4j.Slf4j;

/**
 * 雄安三方接口实现 一卡通等
 *
 * @author zfx
 * @since 2022/11/28 9:22
 **/
@Component
@Slf4j
public class XAThirdHandler {

    private static final Logger logger = LoggerFactory.getLogger(XAThirdHandler.class);
    public static final String SUCCESS_CODE = "0000";
    public static final String SUCCESS_MSG = "操作成功";
    public static final String CODE_1001 = "1001";
    public static final String CODE_1002 = "1002";
    public static final String CODE_1003 = "1003";
    public static final String CODE_1004 = "1004";
    public static final String MSG_1004 = "请求报文body不能为空";
    public static final String MSG_1002 = "下发的权限数据请求不能为空";
    public static final String MSG_1003 = "请求报文header不能为空";
    public static final String MSG_PERSONNO_ERROR = "人员标识[personNo]不能为空且长度不能大于48";
    public static final String MSG_BODY_ERROR = "body数据转换失败";
    public static final String MSG_CARDNO_NULL = "人员卡号[cardNo]不能为空";
    public static final String KEY_HEADER = "header";
    public static final String KEY_BODY = "body";
    public static final String KEY_STARTDATE = "startDate";
    public static final String KEY_ENDDATE = "endDate";
    public static final String KEY_INTERVAL = "interval";
    public static final String KEY_AREALIST = "areaList";

    @Autowired
    private IBasePersonInfoService basePersonInfoService;
    @Autowired
    private PersonChangeEventPublishService personChangeEventPublishService;
    @Autowired
    private IPersonFaceSearchLogService faceSearchLogService;

    @Autowired
    private ChannelBusinessMapper channelBusinessMapper;
    @Autowired
    private ChannelSubtreasuryBusiMapper channelSubtreasuryBusiMapper;
    @Autowired
    private ChannelSubtreasuryInfoMapper channelSubtreasuryInfoMapper;
    @Autowired
    private ChannelInfoMapper channelInfoMapper;
    @Autowired
    private ISysConfigService configService;

    /*
     * syncPersonData
     * 
     * @param body
     * @return cn.eyecool.common.core.domain.AjaxResult
     * @author zfx
     * @since 2022/11/28 9:49
     */
    public CardPassResHeader syncPersonData(String jsonData) {
        CardPassResHeader cpHead = validateSyncDoorRecordRequest(jsonData);
        if (!SUCCESS_CODE.equals(cpHead.getRspCode())) {
            logger.warn("syncDoorRecord参数校验失败[{}]", cpHead);
            return cpHead;
        }
        String deBodyStr = cpHead.getRspMessage();
        try {
            CardPassPerson cardPassPerson = JSONObject.parseObject(deBodyStr, CardPassPerson.class);
            if (cardPassPerson == null) {
                return operateCpHead(cpHead, CODE_1001, MSG_BODY_ERROR);
            }
            validateCardPassPerson(cardPassPerson);
            String personNo = cardPassPerson.getPersonNo();
            BasePersonInfo condition = new BasePersonInfo();
            condition.setUniqueId(personNo);
            List<BasePersonInfo> list = basePersonInfoService.selectBasePersonInfoList(condition);
            if (CollectionUtils.isEmpty(list)) {
                // 新增人员
                BasePersonInfo personInfo = new BasePersonInfo();
                String personId = IdWorker.getNextStringId();
                personInfo.setStatus(DictConstants.Status.ENABLE);
                personInfo.setId(personId);
                personInfo.setUniqueId(personNo);
                personInfo.setDatasource(DictConstants.DataSource.SYNC_UPDATE);
                personInfo.setCreateTime(DateUtils.getNowDate());
                personInfo.setDatasource(DictConstants.DataSource.INTERFACE);
                convertToPerson(personInfo, cardPassPerson);
                basePersonInfoService.insertOnlyPersonInfo(personInfo);
                /** 绑定人库关系 */
                bindPersonChannelRel(personInfo, cardPassPerson.getAreaList(), Constants.STATUS_ZERO);
                // 发布人员信息改变事件
                personChangeEventPublishService.personAddPublish(personId, personNo, false, false, false, false, false,
                    null);
            } else {
                BasePersonInfo personInfo = list.get(0);
                boolean shouldUpdate = checkPersonShoudBeUpdate(cardPassPerson, personInfo);
                personInfo.setStatus(DictConstants.Status.ENABLE);
                personInfo.setUpdateTime(DateUtils.getNowDate());
                convertToPerson(personInfo, cardPassPerson);
                basePersonInfoService.updateOnlyPersonInfo(personInfo);
                /** 绑定人库关系 */
                bindPersonChannelRel(personInfo, cardPassPerson.getAreaList(), Constants.STATUS_ONE);
                if (shouldUpdate) {
                    // 发布人员信息改变事件
                    personChangeEventPublishService.personUpdatePublish(personInfo.getId(), personNo, false, false,
                        false, false, false, null);
                }
            }
            /** 调用已开通接口 回复完成 */
            CardPassResHeader cardPassResHeader = operateCpHead(cpHead, SUCCESS_CODE, SUCCESS_MSG);
            /** 调用权限状态回传接口 回传权限数据 */
            syncAuthorityStatus(cardPassPerson);
            return cardPassResHeader;
        } catch (Exception e) {
            log.error("处理人员失败[{}]", e.getMessage(), e);
            return operateCpHead(cpHead, CODE_1001, e.getMessage());
        }

    }

    /**
     * bindPersonChannelRel 绑定人库关系，绑定的是一卡通传入的子场景对应的场景和子场景信息
     * 
     * @param personInfo
     * @param areaList 子场景编号
     * @return void
     * @author zfx
     * @since 2022/12/19 15:43
     */
    private void bindPersonChannelRel(BasePersonInfo personInfo, List<DoorArea> areaList, String type) {
        if (CollectionUtils.isNotEmpty(areaList)) {
            String personId = personInfo.getId();
            String uniqueId = personInfo.getUniqueId();
            if (cn.eyecool.common.utils.StringUtils.isBlank(uniqueId)
                || cn.eyecool.common.utils.StringUtils.isBlank(personId)) {
                return;
            }
            for (int i = 0; i < areaList.size(); i++) {
                 String subCode = areaList.get(i).getAreaCode();
                //String subCode = "CJ0001_ZCJ000001";
                /** 验证子场景是否存在 */
                ChannelSubtreasuryInfo condition = new ChannelSubtreasuryInfo();
                condition.setSubTreasuryCode(subCode);
                List<ChannelSubtreasuryInfo> subtreasuryInfoList =
                    channelSubtreasuryInfoMapper.selectChannelSubtreasuryInfoList(condition);
                if (CollectionUtils.isEmpty(subtreasuryInfoList)) {
                    log.error("需要自动绑定人库关系的子场景不存在,areaCode:[{}]", subCode);
                    throw new CustomException("areaCode[" + subCode + "]对应的子场景信息不存在");
                }
                ChannelSubtreasuryInfo subtreasuryInfo = subtreasuryInfoList.get(0);
                String subId = subtreasuryInfo.getId();
                // 查询人库关系是否存在
                ChannelSubtreasuryBusi sbusiCondition = new ChannelSubtreasuryBusi();
                sbusiCondition.setSubTreasuryId(subId);
                sbusiCondition.setPersonId(personId);
                List<ChannelSubtreasuryBusi> sbusinessList =
                    channelSubtreasuryBusiMapper.selectChannelSubtreasuryBusiList(sbusiCondition);
                if (CollectionUtils.isNotEmpty(sbusinessList)) {
                    log.debug("人员和子场景关系已绑定,subId:[{}],areaCode:[{}]uniqueId[{]]", subId, subCode, uniqueId);
                    continue;
                }
                String channelId = subtreasuryInfo.getChannelId();
                /** 验证场景是否存在，若不存在直接返回错误 */
                ChannelInfo channelInfo = channelInfoMapper.selectChannelInfoById(channelId);
                if (channelInfo == null) {
                    throw new CustomException("areaCode[" + subCode + "]对应的场景信息不存在");
                }
                /** 验证场景是否绑定 */
                ChannelBusiness busiCondition = new ChannelBusiness();
                busiCondition.setChannelId(channelInfo.getId());
                busiCondition.setPersonId(personId);
                List<ChannelBusiness> businessList = channelBusinessMapper.selectChannelBusinessList(busiCondition);
                if (CollectionUtils.isNotEmpty(businessList)) {
                    if (log.isDebugEnabled()) {
                        log.debug("人员已绑定人库关系,channelCode:[{}], uniqueId:[{]]", channelInfo.getChannelCode(), uniqueId);
                    }
                } else {
                    /** 添加场景人员关系 */
                    insertChannelBus(channelInfo, personInfo);
                    // if (Constants.STATUS_ONE.equals(type)){
                    // dataManagerLogicService.addLibraryPerson(channelInfo.getChannelCode(), uniqueId);
                    // }
                }
                /** 添加子场景人员关系 */
                ChannelSubtreasuryBusi rel = new ChannelSubtreasuryBusi();
                rel.setUniqueId(uniqueId);
                rel.setPersonId(personId);
                rel.setStatus(DictConstants.Status.ENABLE);
                rel.setCreateTime(DateUtils.getNowDate());
                rel.setSubTreasuryId(subId);
                rel.setChannelId(subtreasuryInfo.getChannelId());
                rel.setId(IdWorker.getNextStringId());
                rel.setUpdateTime(DateUtils.getNowDate());
                channelSubtreasuryBusiMapper.insertChannelSubtreasuryBusi(rel);
                // if (Constants.STATUS_ONE.equals(type)){
                // dataManagerLogicService.addLibraryPerson(subtreasuryInfo.getSubTreasuryCode(), uniqueId);
                // }
            }
            /** 处理自动绑定的人库关系 */
            String configByKey =
                configService.selectConfigByKey(SysConfigConstants.PLATFORM_AUTO_BIND_LIB_PERSON_REL_KEY);
            if (DictConstants.YesOrNoState.YES.equals(configByKey)) {
                String configByKeys =
                    configService.selectConfigByKey(SysConfigConstants.AUTO_BIND_LIB_PERSON_REL_CHANNEL_CODE_KEY);
                if (StringUtils.isNotEmpty(configByKeys)) {
                    List<String> autoChannelCodes = Arrays.asList(configByKey.split(";"));
                    autoChannelCodes.forEach(channelCode -> {
                        ChannelInfo condition = new ChannelInfo();
                        condition.setChannelCode(channelCode);
                        List<ChannelInfo> channelInfoList = channelInfoMapper.selectChannelInfoList(condition);
                        if (CollectionUtils.isEmpty(channelInfoList)) {
                            log.warn("需要自动绑定人库关系的场景不存在,channleCode[{}]", channelCode);
                            return;
                        }
                        ChannelInfo channel = channelInfoList.get(0);
                        // 查询人库关系是否存在
                        ChannelBusiness busiCondition = new ChannelBusiness();
                        busiCondition.setPersonId(personId);
                        busiCondition.setChannelId(channel.getId());
                        List<ChannelBusiness> businessList =
                            channelBusinessMapper.selectChannelBusinessList(busiCondition);
                        if (CollectionUtils.isNotEmpty(businessList)) {
                            if (log.isDebugEnabled()) {
                                log.debug("人员已绑定人库关系,channelCode[{}], uniqueId[{]]", channelCode, uniqueId);
                            }
                        } else {
                            // 人库关系自动绑定
                            insertChannelBus(channel, personInfo);
                        }
                    });
                }
            }
        } else {
            logger.error("psnNo[{}]cardNo[{}]areaList is null", personInfo.getUniqueId(), personInfo.getCardNo());
        }
    }

    private void insertChannelBus(ChannelInfo channelInfo, BasePersonInfo personInfo) {
        ChannelBusiness rel = new ChannelBusiness();
        rel.setUniqueId(personInfo.getUniqueId());
        rel.setStatus(DictConstants.Status.ENABLE);
        rel.setIrisMode(channelInfo.getIrisMode());
        rel.setPersonId(personInfo.getId());
        rel.setFveinMode(channelInfo.getFveinMode());
        rel.setFingerMode(channelInfo.getFingerMode());
        rel.setFaceMode(channelInfo.getFaceMode());
        rel.setCreateTime(DateUtils.getNowDate());
        rel.setFaceIrisMode(channelInfo.getFaceIrisMode());
        rel.setChannelId(channelInfo.getId());
        rel.setId(IdWorker.getNextStringId());
        channelBusinessMapper.insertChannelBusiness(rel);
    }

    /**
     * 给返回的cpHead 赋code和msg
     */
    private CardPassResHeader operateCpHead(CardPassResHeader cpHead, String code, String msg) {
        cpHead.setRspCode(code);
        cpHead.setRspMessage(msg);
        return cpHead;
    }

    /**
     * 将接收的对象对应的值赋给person对象中的值
     */
    private void convertToPerson(BasePersonInfo personInfo, CardPassPerson cardPassPerson) {
        Map<String, Object> map = Maps.newHashMap();
        try {
            // personInfo.setCardStatus(cardPassPerson.getState());
            personInfo.setName(cardPassPerson.getFullName());
            personInfo.setCardNo(cardPassPerson.getCardNo());
            map.put(KEY_INTERVAL, cardPassPerson.getInterval());
            map.put(KEY_AREALIST, cardPassPerson.getAreaList());
            personInfo.setEffectiveBeginTime(DateUtils.getDateTime(cardPassPerson.getStartDate()));
            personInfo.setEffectiveEndTime(DateUtils.getDateTime(cardPassPerson.getEndDate()));
        } catch (Exception e) {
            map.put(KEY_STARTDATE, cardPassPerson.getStartDate());
            map.put(KEY_ENDDATE, cardPassPerson.getEndDate());
            log.warn(e.getMessage(), e);
        } finally {
            personInfo.setRemark(JSONObject.toJSONString(map));
        }
    }

    /**
     * 校验人员信息是否需要同步更新
     *
     * @param response
     * @param personInfo
     * @return
     */
    private boolean checkPersonShoudBeUpdate(CardPassPerson response, BasePersonInfo personInfo) {
        String name = response.getFullName();
        String cardNo = response.getCardNo();
        // 已存在人员如果被逻辑删除掉了，也认为需要同步
        if (DictConstants.Status.DISABLE.equals(personInfo.getStatus())) {
            return true;
        }
        if (cn.eyecool.common.utils.StringUtils.isNotBlank(name) && !name.equals(personInfo.getName())) {
            return true;
        }
        if (cn.eyecool.common.utils.StringUtils.isNotBlank(cardNo) && !cardNo.equals(personInfo.getCardNo())) {
            return true;
        }
        return false;
    }

    private void validateCardPassPerson(CardPassPerson cardPassPerson) {
        String uniqueId = cardPassPerson.getPersonNo();
        if (cn.eyecool.common.utils.StringUtils.isBlank(uniqueId) || uniqueId.length() > 48) {
            throw new CustomException(MSG_PERSONNO_ERROR);
        }
        String cardNo = cardPassPerson.getCardNo();
        if (cn.eyecool.common.utils.StringUtils.isBlank(cardNo)) {
            throw new CustomException(MSG_CARDNO_NULL);
        }
    }

    /*
     *  validatSyncDoorRecordRequest 校验一卡通权限请求参数
     * @param jsonData
     * @return cn.eyecool.common.core.domain.AjaxResult
     * @author zfx
     * @since 2022/11/28 9:49
     */
    private CardPassResHeader validateSyncDoorRecordRequest(String jsonData) {
        CardPassResHeader cpHeader;
        if (StringUtils.isEmpty(jsonData)) {
            return new CardPassResHeader(CODE_1002, MSG_1002);
        }
        try {
            JSONObject obj = JSON.parseObject(jsonData);
            String header = obj.getString(KEY_HEADER);
            if (StringUtils.isEmpty(header)) {
                return new CardPassResHeader(CODE_1003, MSG_1003);
            }
            cpHeader = JSONObject.parseObject(header, CardPassResHeader.class);
            String body = obj.getString(KEY_BODY);
            if (StringUtils.isEmpty(body)) {
                return new CardPassResHeader(CODE_1004, MSG_1004);
            }
            // 对body进行解密
            // 解密
            String deBodyStr = EcpAESCipherTools.decrypt(body);
            cpHeader.setRspCode(SUCCESS_CODE);
            cpHeader.setRspMessage(deBodyStr);
            // log.info("加密制定串[{}]",EcpAESCipherTools.encrypt("{\"cardNo\":\"12345678\",\"state\":\"1\",\"startDate\":\"2022-01-01
            // 00:00:00\",\"endDate\":\"2023-01-01
            // 00:00:00\",\"interval\":\"\",\"personNo\":\"12345678\",\"fullName\":\"test1\",\"areaList\":[{\"areaCode\":\"1\"}]}"));
            logger.info("解密后的请求数据[{}]", deBodyStr);
            return cpHeader;
        } catch (Exception e) {
            logger.error("syncDoorRecord request[{}] error[{}]", jsonData, e.getMessage(), e);
            return new CardPassResHeader(CODE_1002, e.getMessage());

        }
    }

    public AjaxResult xaThirdHandler(String cardNo, String areaCode) {
        PersonFaceSearchLog faceSearchLog = new PersonFaceSearchLog();
        faceSearchLog.setCardNo(cardNo);
        faceSearchLog.setDeviceCode(areaCode);
        faceSearchLog.setReceivedTime(DateUtils.getNowDate());
        faceSearchLogService.execXACardPassSendMsg(faceSearchLog);
        return AjaxResult.success();
    }

    /**
     * 往一卡通中心 推送权限状态
     * 
     * @param cardPassPerson
     * @return void
     * @author zfx
     * @since 2023/4/23 10:07
     */
    public void syncAuthorityStatus(CardPassPerson cardPassPerson) {

        String cardNo = cardPassPerson.getCardNo();
        List<DoorArea> areaList = cardPassPerson.getAreaList();
        if (StringUtils.isEmpty(cardNo) || CollectionUtils.isEmpty(areaList)) {
            log.info("门禁权限状态回传到一卡通异常cardNo is null或者areaList is null");
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                XAAuthorityStatusBody body = new XAAuthorityStatusBody();
                body.setCardNo(cardNo);
                List<XADoorArea> aList = Lists.newArrayList();
                for (DoorArea area : areaList) {
                    String areaCode = area.getAreaCode();
                    aList.add(new XADoorArea(areaCode));
                }
                body.setAreaList(aList);
                String bodyStr = JSONObject.toJSONString(body);
                /** 权限状态回传 和日志记录回传是一个接口 */
                // String url = configService.selectConfigByKey(SysConfigConstants.XA_AUTHORITY_STATUS_URL);
                String url = configService.selectConfigByKey(SysConfigConstants.XA_CARD_PASS_RECORD_URL);
                log.info("权限状态回传到一卡通body[{}]", bodyStr);
                faceSearchLogService.sendToOneCardPass(bodyStr, url, Constants.STATUS_ONE);
            } catch (Exception e) {
                log.error("权限状态回传到一卡通实时交易异常:[{}]", e.getMessage(), e);
            }
        });
    }
}
