package cn.eyecool.device.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.websocket.Session;

import cn.eyecool.common.enums.PersonTypeEnum;
import cn.eyecool.device.vo.*;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.device.constant.AdapterConstants;
import cn.eyecool.device.domain.DeviceAccessAdapter;
import cn.eyecool.device.domain.DeviceInfo;
import cn.eyecool.device.service.IDeviceAccessAdapterService;
import cn.eyecool.device.service.ISyncService;
import cn.eyecool.device.util.AdapterUtils;
import cn.eyecool.scene.trade.vo.BasePersonLiveUpdateVO;
import cn.eyecool.server.handler.PersonLiveUpdateHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 203设备接入园区平台Service业务层处理
 * 
 * @author 段存明
 * @date 2021-02-25
 */
@Slf4j
@Service
public class SyncServiceImpl implements ISyncService {

    @Autowired
    private IDeviceAccessAdapterService deviceAccessAdapterService;
    @Autowired
    private PersonLiveUpdateHandler personLiveUpdateHandler;
    @Autowired
    private TenantProperties tenantProperties;

    /**
     * 同步数据 syncData
     *
     * @param session session
     * @param serialNo 序列号
     * @param url 地址
     */
    @Override
    public void syncData(Session session, String serialNo, String url) {
        if (StringUtils.isEmpty(serialNo)) {
            log.error(" syncData serialNo is null ");
            return;
        }
        if (null == session || !session.isOpen()) {
            log.error(" syncData websevice session is null or closed ");
            return;
        }
        try {
            transferHandler(session, serialNo, url);
        } catch (Exception e) {
            log.error(" syncData error : " + e.toString());
        }
    }

    /**
     *
     * 数据中转站 transferHandler
     *
     * @param session session
     * @param devSn 序列号
     * @param url 地址
     */
    @Override
    public void transferHandler(Session session, String devSn, String url) {
        Lock lock = new ReentrantLock();
        lock.lock();
        try {
            log.info("try to connect the platform,serialNo:[{}] ,the start time is :[{}]", devSn, DateUtils.dateTime());
            int count = 0;
            DeviceInfo clientDeviceInfo = deviceAccessAdapterService.getDevBySn(devSn);
            if (clientDeviceInfo == null) {
                log.warn("try to connect the platform,serialNo:[{}] ,the device is unregistered,the end time :[{}]", devSn, DateUtils.dateTime());
                return;
            }
            if (tenantProperties.getEnabled()) {
                TenantContextHolder.setTenantId(clientDeviceInfo.getTenantId());
            }
            do {
                DeviceAccessAdapter adapter = deviceAccessAdapterService.getBySn(devSn);
                if (adapter == null) {
                    log.warn("serialNo :[{}] ,the synchronization parameters are not configured ", devSn);
                    break;
                }
                String seriaNum = adapter.getSeriaNum();
                SummaryVO summaryVO = fetchPersonInfo(devSn, seriaNum);
                // 告知设备进行清空人员后重新全量拉取，更新数据拉取序列号标志为0
                if (summaryVO != null && summaryVO.isNextPullAllData()) {
                    updateSeriaNum(summaryVO, adapter);
                    // TODO 需要下发消息通知设备清空人员数据，设备暂时未提供此接口
                    log.info("connect to the biological platform serialNo:[{}] and tell the device to re-pull the full amount after clearing the personnel", devSn);
                    break;
                }
                if (null == summaryVO || CollectionUtils.isEmpty(summaryVO.getList())) {
                    log.info("connect to the  biological platform serialNo:[{}] The current batch number is [0]", devSn);
                    break;
                }
                boolean syncResult = execSync(session, summaryVO, url, devSn);
                if (!syncResult) {
                    log.error("The serialNo:[{}] issued by the docking biological platform to the 203 device is abnormal", devSn);
                    break;
                }
                updateSeriaNum(summaryVO, adapter);
                int size = summaryVO.getList().size();
                count += size;
                log.info("connect to the  biological platform serialNo:[{}] ,Current batch number:[{}]", devSn, size);
            } while (true);
            log.info("connect to the platform,the serialNo:[{}],total sync people :[{}] ,start time :[{}]", devSn, count, DateUtils.dateTime());
        } catch (Exception e) {
            log.error("connect to the platfrom,the serialNo:[{}], error:[{}]", devSn, e.toString());
        } finally {
            lock.unlock();
        }
    }

    /**
     * 更新序列号 updateSeriaNum
     *
     * @param summaryVO
     * @param adapter
     */
    private void updateSeriaNum(SummaryVO summaryVO, DeviceAccessAdapter adapter) {
        if (summaryVO == null || adapter == null || StringUtils.isEmpty(summaryVO.getSbusiUpdateSeriaNum())) {
            return;
        }
        String seriaNum = summaryVO.getSbusiUpdateSeriaNum();
        adapter.setSeriaNum(seriaNum);
        this.deviceAccessAdapterService.updateDeviceAccessAdapter(adapter);
    }

    /**
     * 获取实时同步数据 fetchPersonInfo
     *
     * @param devSn
     * @param updateSeriaNum
     * @return
     */
    @Override
    public SummaryVO fetchPersonInfo(String devSn, String updateSeriaNum) {
        SummaryVO summaryVO = null;
        String receivedSeq = IdWorker.getNextStringId();
        String bizContent = convertToBizContent(receivedSeq, updateSeriaNum, devSn);
        AjaxResult ajaxResult = this.personLiveUpdateHandler.liveUpdateBasePersonInfo(bizContent);
        if (ajaxResult == null) {
            return null;
        }
        JSONObject json = (JSONObject)JSONObject.toJSON(ajaxResult);
        String dataStr = json.getString("data");
        if (!StringUtils.isEmpty(dataStr)) {
            summaryVO = JSONObject.parseObject(dataStr, SummaryVO.class);
        }
        return summaryVO;
    }

    /**
     * 封装业务数据 convertToBizContent
     *
     * @param receivedSeq
     * @param updateSeriaNum
     * @param devSn
     * @return
     */
    private String convertToBizContent(String receivedSeq, String updateSeriaNum, String devSn) {
        Map<String, String> map = new HashMap<>(0);
        map.put("receivedSeq", receivedSeq);
        map.put("sbusiUpdateSeriaNum", updateSeriaNum);
        map.put("deviceNo", devSn);
        // 需要返回身份证信息
        map.put("needCertInfo", "Y");
        JSONObject jsonObj = (JSONObject)JSONObject.toJSON(map);
        String str;
        str = jsonObj.toString();
        return str;
    }

    /**
     * 执行同步 execSync
     *
     * @param session session
     * @param summaryVO summaryVO
     * @param url url
     */
    @Override
    public synchronized boolean execSync(Session session, SummaryVO summaryVO, String url, String devSn) {
        for (BasePersonLiveUpdateVO infoVo : summaryVO.getList()) {
            boolean sendResult = sendText(session, infoVo, url, devSn);
            if (!sendResult) {
                return false;
            }
        }
        return true;
    }

    /**
     * 发送数据 sendText
     *
     * @param session
     * @param infoVo
     * @param url
     */
    private boolean sendText(Session session, BasePersonLiveUpdateVO infoVo, String url, String devSn) {
        SyncDataInfoVO syncDataInfo = getSyncDataInfo(infoVo, url, devSn);
        if (syncDataInfo == null) {
            return false;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            String str = mapper.writeValueAsString(syncDataInfo);
            if (session.isOpen()) {
                log.info("execSync data info:/n/t [{}]", str);
                session.getAsyncRemote().sendText(str);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error(" execSync error :[{}] ", e.toString());
            return false;
        }
    }

    /**
     * 封装同步数据 getSyncDataInfo
     *
     * @param infoVo
     * @param url
     * @return
     */
    private SyncDataInfoVO getSyncDataInfo(BasePersonLiveUpdateVO infoVo, String url, String devSn) {
        SyncDataInfoVO syncDataInfo = new SyncDataInfoVO();
        String id = infoVo.getUniqueId();
        String method = "";
        String personSex = AdapterConstants.MALE_CODE;
        String psnType = PersonTypeEnum.VISITOR.value().equals(infoVo.getPersonType()) ? AdapterConstants.PersonType.TYPE2 : AdapterConstants.PersonType.TYPE1;
        String birthday = AdapterConstants.DEF_BIRTHDAY;
        String strDate = AdapterUtils.getDateFormatForSecond(new Date());
        String encryptStr = AdapterUtils.encrypt(devSn + "_" + id + "_" + strDate, AdapterConstants.SECRET);
        String fetchFaceUrl = url + encryptStr;
        Map<String, Object> idCardInfo = infoVo.getIdCardInfo();
        String idNum = "";
        if (null != idCardInfo && idCardInfo.size() > 0) {
            idNum = (String)idCardInfo.get("certNum");
        }

        String code = infoVo.getUniqueId();
        String groupName = AdapterConstants.DEF_GROUP;
        String name = infoVo.getName();
        String sex = AdapterConstants.MALE_CODE.equals(personSex) ? AdapterConstants.MALE : AdapterConstants.FEMALE;

        if (AdapterConstants.OperFlag.ADD.equals(infoVo.getStatus())) {
            method = AdapterConstants.OperMethod.ADD;
        } else if (AdapterConstants.OperFlag.DELETE.equals(infoVo.getStatus())) {
            method = AdapterConstants.OperMethod.DELETE;
        }
        List<Object> params = new ArrayList<>();
        PersonInfoVO personInfo = new PersonInfoVO();
        int type = 1;
        if (AdapterConstants.PersonType.TYPE1.equals(psnType)) {
            type = 1;
        }
        if (AdapterConstants.PersonType.TYPE2.equals(psnType)) {
            type = 2;
        }
        if (AdapterConstants.PersonType.TYPE3.equals(psnType)) {
            type = 3;
        }

        GuestInfoVO guestInfoVO=null;
        if(type==Integer.valueOf(AdapterConstants.PersonType.TYPE2)){
            Date effectiveBeginTime = infoVo.getEffectiveBeginTime();
            Date effectiveEndTime = infoVo.getEffectiveEndTime();
            if(null != effectiveBeginTime && null != effectiveEndTime){
                List<AccessTimeVO> accessTimeList= Lists.newArrayList();
                AccessTimeVO accessTimeVO=new AccessTimeVO();

                accessTimeVO.setFrom(effectiveBeginTime.getTime()/1000);
                accessTimeVO.setTo(effectiveEndTime.getTime()/1000);
                accessTimeList.add(accessTimeVO);

                guestInfoVO=new GuestInfoVO();
                guestInfoVO.setPhone(infoVo.getPhone());
                guestInfoVO.setAccessTime(accessTimeList);
            }else{
                guestInfoVO=null;
            }
        }


        syncDataInfo.setId(IdWorker.getNextLongId());
        syncDataInfo.setMethod(method);

        // personInfo
        personInfo.setType(type);
        personInfo.setCode(code);
        personInfo.setGroupName(groupName);
        personInfo.setName(name);
        personInfo.setSex(sex);
        personInfo.setBirthday(birthday);
        personInfo.setCredentialNo(idNum);

        // guestInfo
        personInfo.setGuestInfo(guestInfoVO);

        // img
        List<String> urlList = new ArrayList<>(0);
        urlList.add(fetchFaceUrl);
        personInfo.setUrl(urlList);
        // List<BasePersonFaceVO> faceList = infoVo.getBasePersonFaceVOList();
        // BasePersonFaceVO faceVO = faceList != null && faceList.size() > 0 ? faceList.get(0) : null;
        // 下发url
        personInfo.setImages(null);

        // cards
        List<CardInfoVO> cards = new ArrayList<>(0);
        List<String> validity = new ArrayList<>(0);
        List<String> validityTime = new ArrayList<>(0);
        CardInfoVO card = new CardInfoVO();

        validity.add(AdapterConstants.PermitDate.START_DATE);
        validity.add(AdapterConstants.PermitDate.END_DATE);

        card.setId(infoVo.getCardNo());
        card.setType(1);
        card.setValidity(validity);
        card.setValidityTime(validityTime);

        cards.add(card);
        if (StringUtils.isNotEmpty(card.getId())) {
            personInfo.setCards(cards);
        } else {
            personInfo.setCards(null);
        }
        PersonItemVO item = new PersonItemVO();
        if (AdapterConstants.OperFlag.DELETE.equals(infoVo.getStatus())) {
            Map<String, String> map = new HashMap<>(0);
            map.put("Code", code);
            params.add(map);
        } else {
            personInfo.setBirthday(AdapterConstants.DEF_BIRTHDAY);
            item.setPerson(personInfo);
            params.add(item);
        }

        syncDataInfo.setParams(params);
        return syncDataInfo;
    }

}
