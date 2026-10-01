package cn.eyecool.visitor.service.impl;

import cn.eyecool.basedata.domain.BasePersonFace;
import cn.eyecool.basedata.domain.BasePersonFacePutInfo;
import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.basedata.domain.BasePersonPutInfo;
import cn.eyecool.basedata.manager.IPersonFaceRecogLogicService;
import cn.eyecool.basedata.mapper.BasePersonFaceMapper;
import cn.eyecool.basedata.mapper.BasePersonInfoMapper;
import cn.eyecool.basedata.service.IBasePersonInfoService;
import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.enums.PersonTypeEnum;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.*;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.common.utils.sign.Md5Utils;
import cn.eyecool.device.service.impl.DeviceAccessAdapterServiceImpl;
import cn.eyecool.msg.trade.entity.MsgSendResult;
import cn.eyecool.msg.trade.entity.MsgSmsSendInfo;
import cn.eyecool.msg.trade.service.IMsgSendService;
import cn.eyecool.scene.domain.ChannelBusiness;
import cn.eyecool.scene.domain.ChannelSubtreasuryBusi;
import cn.eyecool.scene.domain.ChannelSubtreasuryInfo;
import cn.eyecool.scene.mapper.ChannelBusinessMapper;
import cn.eyecool.scene.mapper.ChannelSubtreasuryBusiMapper;
import cn.eyecool.scene.mapper.ChannelSubtreasuryInfoMapper;
import cn.eyecool.scene.service.IBusiLiveUpdateSeqService;
import cn.eyecool.system.service.ISysConfigService;
import cn.eyecool.visitor.constant.VisitorSystemConstants;
import cn.eyecool.visitor.vo.InvitationMesVO;
import cn.eyecool.visitor.vo.InviterInfoVO;
import cn.eyecool.visitor.dto.UserSubSceneDTO;
import cn.eyecool.visitor.mapper.VisitorMapper;
import cn.eyecool.visitor.service.VisitorService;
import cn.eyecool.visitor.vo.SceneTreeMes;
import com.eyecool.abis.callmicroservice.common.CheckLiveResponse;
import com.eyecool.abis.callmicroservice.common.FaceExtractResult;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author Administrator
 * @create 2021/10/21 18:11
 */
@Service
@Slf4j
public class VisitorServiceImpl implements VisitorService {

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private IMsgSendService msgSendService;

    @Autowired
    private VisitorMapper visitorMapper;

    @Autowired
    private IBasePersonInfoService iBasePersonInfoService;

    @Autowired
    private BasePersonInfoMapper basePersonInfoMapper;

    @Autowired
    private IPersonFaceRecogLogicService personFaceRecogLogicService;

    @Autowired
    private BasePersonFaceMapper basePersonFaceMapper;

    @Autowired
    private ChannelSubtreasuryInfoMapper channelSubtreasuryInfoMapper;

    @Autowired
    private ChannelSubtreasuryBusiMapper channelSubtreasuryBusiMapper;

    @Autowired
    private ChannelBusinessMapper channelBusinessMapper;

    @Autowired
    private IBusiLiveUpdateSeqService busiLiveUpdateSeqService;

    @Autowired
    private DeviceAccessAdapterServiceImpl deviceAccessAdapterServiceImpl;

    @Autowired
    private TenantProperties tenantProperties;

    @Autowired
    private ISysConfigService configService;

    @Value("${visit.sms.tencent.sign}")
    private String sign;

    @Value("${visit.sms.tencent.register.templateId}")
    private String registerTemplateId;

    @Value("${visit.sms.tencent.visitor.templateId}")
    private String visitorTemplateId;

    @Value("${common.sms.appid}")
    private String appid;

    @Value("${common.sms.appSecrect}")
    private String appSecrect;




    /***
     * @Author zgy
     * @Date 2021/10/24 12:14
     * @Description 判断是否有租户相关信息，再禁止频繁请求，然后判断用户及手机号是否同时一起存在
     * @Param [inviterInfo]
     * @Return cn.eyecool.common.core.domain.AjaxResult
     */
    @Override
    public void getVerifyCode(InviterInfoVO inviterInfo) {
        //设置租户相关信息
        if (tenantProperties.getEnabled()) {
            String encryptTenantId= inviterInfo.getKey();
            if(StringUtils.isEmpty(encryptTenantId)){
                throw new CustomException("未获取到租户相关信息");
            }
            String tenantId;
            try {
                tenantId=AESUtils.decryptAES(new String(Base64.getDecoder().decode(encryptTenantId)));
            }catch (Exception e){
                throw new CustomException("请重新扫码登录");
            }
            TenantContextHolder.setTenantId(tenantId);
        }

        // 获取6位数字验证码
        String verifyCode = VerifyCodeUtils.generateVerifyCode(6, VisitorSystemConstants.SOURCE);
        String key= VisitorSystemConstants.VISITOR_SYSTEM_LOGIN_VERIFY_CODE_CACHE_PREFIX+inviterInfo.getPhone();
        // 返回剩余时间（秒），这里判断下从放入到获取花费时间，60秒内禁止重新获取
        Long expire = redisCache.getExpire(key);
        if(null == inviterInfo.getPhone() && null == inviterInfo.getUsername()){
            throw new CustomException("用户名以及手机号不能为空");
        }
        if(null != expire && VisitorSystemConstants.VERIFY_CODE_EFFECTIVE_TIME-expire< VisitorSystemConstants.VERIFY_CODE_REACQUIRE_TIME ){
            throw new CustomException("验证码请求频繁，请一分钟后再请求。");
        }
        // 判断用户及手机号是否同时一起存在
        judgePersonExist(inviterInfo);

        String cacheCode = redisCache.getCacheObject(key);
        if(null!=cacheCode){
            redisCache.deleteObject(key);
        }
        // 封装一下msgSmsSendInfo实例
        ArrayList<String> codeList = Lists.newArrayList();
        codeList.add(verifyCode);
        MsgSendResult msgSendResult=sendMes(inviterInfo.getPhone(),registerTemplateId,codeList);
        if(null==msgSendResult || DictConstants.MsgResult.FAIL.equals(msgSendResult.getStatusCode())){
            throw new CustomException(null==msgSendResult? "短信发送失败请重新发送" : msgSendResult.getErrmsg(), 500);
        }
        redisCache.setCacheObject(key,verifyCode, VisitorSystemConstants.VERIFY_CODE_EFFECTIVE_TIME, TimeUnit.SECONDS);

    }

    /***
     * @Author zgy
     * @Date 2021/10/29 12:14
     * @Description 判断验证码是否正确-》判断邀请人行手机号是否存在-》生成邀请人的token
     * @Param [inviterInfo]邀请人填写的的邀请信息
     * @Return String token 邀请人的token
     */
    @Override
    public String login(InviterInfoVO inviterInfo) {
        // 数据全部校验成功
        // 查看验证码是否正确
        String key= VisitorSystemConstants.VISITOR_SYSTEM_LOGIN_VERIFY_CODE_CACHE_PREFIX+inviterInfo.getPhone();
        String cacheCode = redisCache.getCacheObject(key);
        if(null==cacheCode){
            throw new CustomException("验证码失效，请重新获取。", 500);
        }
        if(!inviterInfo.getVerifyCode().equals(cacheCode)){
            throw new CustomException("验证码错误，请重新获取。",500);
        }
        // 验证码正确，查询邀请人的用户名以及手机号是否存在
        BasePersonInfo basePersonInfo = judgePersonExist(inviterInfo);
        // 查找到用户，封装一个token:要带用户id以及对应租户信息以及用户还是访客以及失效时间
        String id=basePersonInfo.getId();
        String tenantId = basePersonInfo.getTenantId();
        String initialToken=id+";"+tenantId+";"+ PersonTypeEnum.USER.value()+";"+System.currentTimeMillis();
        String token= new String(Base64.getEncoder().encode(AESUtils.encryptAES(initialToken).getBytes()));
        String tokenKey= VisitorSystemConstants.VISITOR_SYSTEM_TOKEN_CACHE_PREFIX+basePersonInfo.getId();
        redisCache.setCacheObject(tokenKey,token, VisitorSystemConstants.TOKEN_EXPIRE_TIME,TimeUnit.SECONDS);
        // 删除当前验证码
        redisCache.deleteObject(key);
        return token;
    }

    @Override
    public BasePersonInfo getInviterName(String visitorInfoId) {
        BasePersonInfo visitorInfo = basePersonInfoMapper.selectBasePersonInfoById(visitorInfoId);
        return basePersonInfoMapper.selectBasePersonInfoById(visitorInfo.getInviterId());
    }

    /***
     * @Author zgy
     * @Date 2021/10/24 12:14
     * @Description 获取邀请人所在的子场景相关信息
     * @Param [personInfoId]邀请人的用户信息主键
     * @Return List<SceneTreeMes> 邀请人的所在的子场景相关信息
     */
    @Override
    public List<SceneTreeMes> getPersonSubScene(String personInfoId) {
        List<UserSubSceneDTO> list = visitorMapper.getPersonSubScene(personInfoId);
        HashSet<SceneTreeMes> channelSet=new HashSet<>();
        // 把主场景选出来
        list.forEach(t->{
            SceneTreeMes sceneTreeMes=new SceneTreeMes();
            sceneTreeMes.setSceneId(t.getChannelId());
            sceneTreeMes.setSceneName(t.getChannelName());
            channelSet.add(sceneTreeMes);
        });
        // 遍历主场景，循环list，找出list中主场景跟当前遍历的一致的数据并保存
        List<SceneTreeMes> channelList=new ArrayList<>(channelSet);
        channelList.forEach(t->{
            list.forEach(s->{
                if(s.getChannelId().equals(t.getSceneId())){
                    SceneTreeMes sceneTreeMes=new SceneTreeMes();
                    sceneTreeMes.setSceneId(s.getSubId());
                    sceneTreeMes.setSceneName(s.getSubName());
                    sceneTreeMes.setParentId(s.getChannelId());
                    t.getChildren().add(sceneTreeMes);
                }
            });
        });
        return channelList;
    }

    /**
     * @Author zgy
     * @Date 2021/10/25 11:38
     * @Description 1.保存访客基本信息，
     *              2.绑定访客跟子场景信息
     *              3.绑定访客跟主场景信息
     *              4.发送短信,用户token删除，访客token放入redis中
     *
     * @Param [invitationMesVO, inviterId]
     * @Return cn.eyecool.common.core.domain.AjaxResult
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveInvitationMes(InvitationMesVO invitationMesVO,String inviterId) {
        // 1.保存访客基本信息
        BasePersonPutInfo basePersonPutInfo = saveVisitorInfo(invitationMesVO, inviterId);

        // 2.绑定访客跟子场景信息
        Map<String,List<String>>resultMap=saveVisitorSubSceneBus(invitationMesVO,basePersonPutInfo);
        List<String> channelList =resultMap.get("channelList");
        List<String> subNameList =resultMap.get("subNameList");

        String subNames =subNameList.stream().reduce("", (str1, str2) -> str1 + str2+"、");
        subNames=subNames.substring(0,subNames.lastIndexOf("、"));
        // 3.绑定访客跟主场景信息
        saveVisitorChannelSceneBus(invitationMesVO, basePersonPutInfo, channelList);

        // 4.封装短信
        // 封装一下msgSmsSendInfo实例
        // 模拟封装一个url连接,带一个token，现在token组成暂时使用   访客basePersonInfo的主键id+tenantId+用户还是访客+时间截止日期跟token过期时间的毫秒数
        long endTime = invitationMesVO.getEffectiveEndTime().getTime()- VisitorSystemConstants.TOKEN_EXPIRE_TIME*1000L;
        // 初始访客token
        String initialToken=basePersonPutInfo.getId()+";"+TenantContextHolder.getTenantId()+";"+PersonTypeEnum.VISITOR.value()+";"+endTime;
        // 加密后的访客token
        String encryptAES=new String(Base64.getEncoder().encode(AESUtils.encryptAES(initialToken).getBytes()));
        // 将邀请人姓名转换成UnlEncode发送出去
        String nameEncode = null;
        try {
            nameEncode = java.net.URLEncoder.encode(invitationMesVO.getInviterName(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        SimpleDateFormat sdf=new SimpleDateFormat("MM月dd日HH:mm");
        String startDate=sdf.format(invitationMesVO.getEffectiveBeginTime());
        String endDate=sdf.format(invitationMesVO.getEffectiveEndTime());
        String visitorURL="token="+encryptAES+"&name="+nameEncode;
        // 不同租户的所在公司名称要租户自己配置好，否则发送短信的时候，到访公司无法确定
        String companyName = configService.selectConfigByKey(SysConfigConstants.VISITOR_VISIT_COMPANY_NAME);
        if(StringUtils.EMPTY.equals(companyName) || "-".equals(companyName)){
            log.error("[{}]未配置当前租户所在公司的公司名称",TenantContextHolder.getTenantId());
            throw new CustomException("未配置当前租户所在公司的公司名称，请配置好后再邀请访客",502);
        }
        // 短信模板参数
        ArrayList<String> codeList = Lists.newArrayList();
        codeList.add(invitationMesVO.getVisitorName());
        codeList.add(companyName);
        codeList.add(startDate+"至"+endDate);
        codeList.add(subNames);
        codeList.add(visitorURL);

        MsgSendResult msgSendResult=sendMes(invitationMesVO.getVisitorPhone(), visitorTemplateId, codeList);
        if(null==msgSendResult || DictConstants.MsgResult.FAIL.equals(msgSendResult.getStatusCode())){
            throw new CustomException(null==msgSendResult? "短信发送失败请重新发送" : msgSendResult.getErrmsg());
        }
        // 短信发送成功，给邀请人的token要删除掉，然后访客的token放入，先不删了，邀请人可能会再次邀请其他人
        // String inviterTokenKey= VisitorSystemConstants.VISITOR_SYSTEM_TOKEN_CACHE_PREFIX+inviterId;
        // redisCache.deleteObject(inviterTokenKey);
        // redis中访客token的key：visitor-system:token:12345643334
        String visitorTokenKey= VisitorSystemConstants.VISITOR_SYSTEM_TOKEN_CACHE_PREFIX+basePersonPutInfo.getId();
        // 失效时间，当前时间减去访客到访截止时间
        long visitorTokenExpireTime=invitationMesVO.getEffectiveEndTime().getTime()-System.currentTimeMillis();
        redisCache.setCacheObject(visitorTokenKey,encryptAES, visitorTokenExpireTime,TimeUnit.MILLISECONDS);
        return visitorURL;
    }

    
    /**
     * @Author zgy
     * @Date 2021/10/25 12:04
     * @Description 查找访客基本信息
     * @Param [basePersonInfoId]
     * @Return cn.eyecool.common.core.domain.AjaxResult
     */
    @Override
    public InvitationMesVO getInvitationMes(BasePersonInfo basePersonInfo) {
        List<BasePersonInfo> list = iBasePersonInfoService.selectBasePersonInfoList(basePersonInfo);
        if(CollectionUtils.isEmpty(list)){
            throw new CustomException("访客信息不存在", 504);
        }
        basePersonInfo=list.get(0);
        InvitationMesVO invitationMesVO=new InvitationMesVO();
        invitationMesVO.setVisitorName(basePersonInfo.getName());
        invitationMesVO.setVisitorPhone(basePersonInfo.getPhone());
        // 先不加身份证号
        return invitationMesVO;
    }


    /**
     * @Author zgy
     * @Date 2021/10/27 10:29
     * @Description 人脸检活-》提取人脸特征-》图像质量检测
     * @Param [invitationMesVO]
     * @Return cn.eyecool.common.core.domain.AjaxResult
     */
    @Override
    public boolean checkVisitorFace(InvitationMesVO invitationMesVO){
        if (null == invitationMesVO || StringUtils.isBlank(invitationMesVO.getVisitorFaceBase64())) {
            throw new CustomException("人脸图片未上传");
        }
        String imageBase64 = invitationMesVO.getVisitorFaceBase64();


        // 超过500K,压缩图片;  把前台传过来的base64，先存成文件，然后压缩这个文件，然后从这个压缩文件中重新提取出base64，然后删除这个文件
        String filePathName = personFaceRecogLogicService.uploadFaceImg(false,null,imageBase64,null);
        deviceAccessAdapterServiceImpl.dealImage(filePathName);
        imageBase64=PlatformFileUtils.getImageBase64(filePathName);
        CompletableFuture.runAsync(() -> PlatformFileUtils.deleteFile(filePathName));

        // 这里我先判断上传的照片是否重复
        findRepeatFaceImage(imageBase64);

        // 活体检测
        if (personFaceRecogLogicService.getFaceAddIsCheckLive()) {
            CheckLiveResponse checkLiveResponse =
                personFaceRecogLogicService.checkLive(imageBase64, null);
            if (!checkLiveResponse.getResult()) {
                throw new CustomException("人脸图片活体检测未通过，请检查!");
            }
        }

        // 获取图像特征
        FaceExtractResult faceExtractResult = personFaceRecogLogicService.getFaceExtractResult(imageBase64);
        List<String> features = faceExtractResult.getFeatures();
        if (CollectionUtils.isEmpty(features)) {
            log.error("图片未检测到人脸,请上传清晰人脸图片");
            throw new CustomException("图片未检测到人脸,请上传清晰人脸图片");
        }
        if (features.size() > 1) {
            log.error("图片检测到多人脸,请上传单人脸图片");
            throw new CustomException("图片检测到多人脸,请上传单人脸图片");
        }
        // 图像质量检测
        personFaceRecogLogicService.qualityDetect(imageBase64, null, null);

        return true;
    }


    /**
     * @Author zgy
     * @Date 2021/10/25 16:04
     * @Description 访客更新访客信息
     * @Param [personInfoId, invitationMesVO]
     * @Return cn.eyecool.common.core.domain.AjaxResult
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInvitationMes(String personInfoId, InvitationMesVO invitationMesVO) {
        // 保存人脸信息需要人员信息主键跟uniqueId，要去库里查找一下
        BasePersonInfo basePersonInfo=basePersonInfoMapper.selectBasePersonInfoById(personInfoId);
        if(null==basePersonInfo){
            throw new CustomException("访客基本信息不存在", 500);
        }
        BasePersonPutInfo basePersonPutInfo=new BasePersonPutInfo();
        basePersonPutInfo.setId(personInfoId);
        basePersonPutInfo.setUniqueId(basePersonInfo.getUniqueId());
        basePersonPutInfo.setTenantId(TenantContextHolder.getTenantId());
        basePersonPutInfo.setName(invitationMesVO.getVisitorName());
        basePersonPutInfo.setPhone(invitationMesVO.getVisitorPhone());

        BasePersonFacePutInfo basePersonFacePutInfo=new BasePersonFacePutInfo();
        basePersonFacePutInfo.setImageBase64(invitationMesVO.getVisitorFaceBase64());
        basePersonFacePutInfo.setEncrypted(DictConstants.Encrypted.ENABLE);
        basePersonPutInfo.setFacePutInfo(basePersonFacePutInfo);
        // 更新访客基本信息
        basePersonInfoMapper.updateBasePersonInfo(basePersonPutInfo);
        // 新增访客人脸信息
        saveVisitorFace(basePersonPutInfo);
        // 更新访客跟子场景信息
        updateVisitorSubSceneBus(basePersonPutInfo);

        // 成功后，销毁token
        String tokenKey= VisitorSystemConstants.VISITOR_SYSTEM_TOKEN_CACHE_PREFIX+basePersonPutInfo.getId();
        redisCache.deleteObject(tokenKey);
    }



    /***
     * @Author zgy
     * @Date 2021/10/24 14:12
     * @Description 判断用户及手机号是否同时一起存在
     * @Param [inviterInfo]
     * @Return cn.eyecool.common.core.domain.AjaxResult
     */
    private BasePersonInfo judgePersonExist(InviterInfoVO inviterInfo){

        BasePersonInfo basePersonInfo=new BasePersonInfo();
        basePersonInfo.setName(inviterInfo.getUsername());
        basePersonInfo.setPhone(inviterInfo.getPhone());
        basePersonInfo.setStatus("0");
        basePersonInfo.setFlag("1");
        basePersonInfo.setPersonType(PersonTypeEnum.USER.value());
        List<BasePersonInfo> list = visitorMapper.selectBasePersonInfoList(basePersonInfo);
        if(CollectionUtils.isEmpty(list) || list.size()==0){
            throw new CustomException("请核查您的基础信息", 500);
        }
        return list.get(0);
    }


    /**
     * @Author zgy
     * @Date 2021/10/25 11:33
     * @Description 发送短信并保存token
     * @param phone 手机号
     * @param templateId 短信模板id
     * @param templateParams 短信模板中的参数
     */
    private MsgSendResult sendMes(String phone, String templateId, ArrayList<String> templateParams){
        MsgSmsSendInfo msgSmsSendInfo=new MsgSmsSendInfo();
        msgSmsSendInfo.setToUserPhoneStr(phone);
        msgSmsSendInfo.setAppId(appid);
        msgSmsSendInfo.setAppSecrect(appSecrect);
        msgSmsSendInfo.setMsgType(DictConstants.MsgType.TEMPLATE_MSG);
        msgSmsSendInfo.setSign(sign);
        msgSmsSendInfo.setTemplateId(templateId);
        msgSmsSendInfo.setTemplateParams(templateParams);
        return msgSendService.sendSmsMessage(msgSmsSendInfo);
    }



    /***
     * @Author zgy
     * @Date 2021/10/24 14:12
     * @Description 保存访客信息
     * @Param [invitationMesVO,inviterId]
     * @Return cn.eyecool.common.core.domain.AjaxResult
     */
    private BasePersonPutInfo saveVisitorInfo(InvitationMesVO invitationMesVO,String inviterId){
        BasePersonPutInfo basePersonPutInfo=new BasePersonPutInfo();
        // 主键使用IdWorker来生成
        String visitorInfoId = IdWorker.getNextStringId();
        basePersonPutInfo.setId(visitorInfoId);
        basePersonPutInfo.setFlag(DictConstants.PersonFlag.NORMAL);
        basePersonPutInfo.setStatus(DictConstants.Status.ENABLE);
        basePersonPutInfo.setPhone(invitationMesVO.getVisitorPhone());
        basePersonPutInfo.setName(invitationMesVO.getVisitorName());
        basePersonPutInfo.setDatasource(DictConstants.DataSource.INTERFACE);
        // 访客唯一标识暂时使用IdWorker来生成
        String visitorUniqueId = IdWorker.getNextStringId();
        basePersonPutInfo.setUniqueId(visitorUniqueId);
        basePersonPutInfo.setTenantId(TenantContextHolder.getTenantId());

        basePersonPutInfo.setPersonType(PersonTypeEnum.VISITOR.value());
        basePersonPutInfo.setInviterId(inviterId);
        basePersonPutInfo.setVisitorStatus(DictConstants.Status.DISABLE);
        basePersonPutInfo.setEffectiveBeginTime(invitationMesVO.getEffectiveBeginTime());
        basePersonPutInfo.setEffectiveEndTime(invitationMesVO.getEffectiveEndTime());
        basePersonPutInfo.setCreateBy(invitationMesVO.getInviterName());
        basePersonPutInfo.setCreateTime(new Date());
        // 保存访客基本信息
        basePersonInfoMapper.insertBasePersonInfo(basePersonPutInfo);
        return basePersonPutInfo;
    }


    /***
     * @Author zgy
     * @Date 2021/10/24 14:12
     * @Description 验证一下传入的子场景id是否正确,保存访客跟子场景的关系
     * @Param [invitationMesVO,basePersonPutInfo]
     * @Return Map<String,List<String>> 返回主场景id集合以及子场景名称，用来后面绑定主场景跟发送短信
     */
    private Map<String,List<String>> saveVisitorSubSceneBus(InvitationMesVO invitationMesVO,BasePersonPutInfo basePersonPutInfo){
        String[] subSceneIds = invitationMesVO.getSubSceneInfoIds();
        if(null==subSceneIds || subSceneIds.length==0){
            throw new CustomException("邀请人["+invitationMesVO.getInviterName()+"]未给访客添加访问区域");
        }
        // 判断前台传来的子场景id是否在邀请人范围内
        List<UserSubSceneDTO> subSceneList=visitorMapper.getPersonSubScene(basePersonPutInfo.getInviterId());
        List<String> subSceneIdList = subSceneList.stream().map(UserSubSceneDTO::getSubId).collect(Collectors.toList());
        for (String subSceneId: subSceneIds) {
            if(!subSceneIdList.contains(subSceneId)){
                throw new CustomException("邀请人["+invitationMesVO.getInviterName()+"]添加访问区域不在邀请人所在场景中");
            }
        }
        Map<String,List<String>> returnMap= Maps.newHashMap();
        // 传入一个set用来保存主场景id
        HashSet<String> set= Sets.newHashSet();
        // 传入一个list用来保存子场景名称
        List<String> subNameList=Lists.newArrayList();
        Arrays.asList(subSceneIds).forEach(subId -> {
            ChannelSubtreasuryInfo condition = new ChannelSubtreasuryInfo();
            condition.setId(subId);
            ChannelSubtreasuryInfo channelSubtreasuryInfo =
                channelSubtreasuryInfoMapper.selectChannelSubtreasuryInfoById(subId);
            if (null == channelSubtreasuryInfo) {
                throw new CustomException("需要绑定的人库关系的子场景不存在");
            }
            set.add(channelSubtreasuryInfo.getChannelId());
            subNameList.add(channelSubtreasuryInfo.getSubTreasuryName());
            // 人库关系绑定
            ChannelSubtreasuryBusi rel =new ChannelSubtreasuryBusi();
            rel.setUniqueId(basePersonPutInfo.getUniqueId());
            rel.setStatus(DictConstants.Status.ENABLE);
            rel.setPersonId(basePersonPutInfo.getId());
            rel.setCreateTime(DateUtils.getNowDate());
            rel.setSubTreasuryId(subId);
            rel.setChannelId(channelSubtreasuryInfo.getChannelId());
            rel.setId(IdWorker.getNextStringId());
            rel.setRemark("访客");
            rel.setUpdateSeriaNum(0L);
            channelSubtreasuryBusiMapper.insertChannelSubtreasuryBusi(rel);
        });
        List<String> channelList=new ArrayList<>(set);
        returnMap.put("channelList",channelList);
        returnMap.put("subNameList",subNameList);
        return returnMap;
    }

    /***
     * @Author zgy
     * @Date 2021/11/03 15:12
     * @Description 验证一下传入的子场景id是否正确,保存访客跟主场景的关系，这边不用管是新增还是更新，一律按照新增来，场景有误，直接删除该条访客记录，H5重新邀请一遍
     * @Param   invitationMesVO 邀请表数据，只要里面的邀请人姓名用
     *          basePersonPutInfo 访客基本信息数据
     *          channelList   访客要绑定的主场景id
     * @Return
     */
    private void saveVisitorChannelSceneBus(InvitationMesVO invitationMesVO,BasePersonPutInfo basePersonPutInfo, List<String> channelList){
        if(CollectionUtils.isEmpty(channelList)){
            throw new CustomException("邀请人所在主场景为空");
        }
        // 判断主场景id是否在邀请人范围内
        channelList.forEach(entity->{
            ChannelBusiness condition = new ChannelBusiness();
            condition.setChannelId(entity);
            condition.setPersonId(basePersonPutInfo.getInviterId());
            List<ChannelBusiness> resultList = channelBusinessMapper.selectChannelBusinessList(condition);
            // 有错误直接抛出异常，整个回滚，
            if(CollectionUtils.isEmpty(resultList)){
                throw new CustomException("邀请人不在该主场景中");
            }
            ChannelBusiness channelBusiness = new ChannelBusiness();
            channelBusiness.setChannelId(entity);
            channelBusiness.setId(IdWorker.getNextStringId());
            channelBusiness.setPersonId(basePersonPutInfo.getId());
            channelBusiness.setUniqueId(basePersonPutInfo.getUniqueId());
            // 数据字典:0-不开通 1-开通
            channelBusiness.setFaceIrisMode(DictConstants.Status.DISABLE);
            channelBusiness.setFingerMode(DictConstants.Status.ENABLE);
            channelBusiness.setIrisMode(DictConstants.Status.ENABLE);
            channelBusiness.setFveinMode(DictConstants.Status.ENABLE);
            channelBusiness.setFaceIrisMode(DictConstants.Status.ENABLE);
            channelBusiness.setRemark("访客");
            channelBusiness.setCreateBy(invitationMesVO.getInviterName());
            channelBusiness.setCreateTime(new Date());
            channelBusiness.setTenantId(TenantContextHolder.getTenantId());
            channelBusiness.setUpdateSeriaNum(0L);
            channelBusinessMapper.insertChannelBusiness(channelBusiness);
        });

    }


    /***
     * @Author zgy
     * @Date 2021/10/24 14:12
     * @Description 更新访客跟子场景的关系
     * @Param [invitationMesVO,basePersonPutInfo]
     * @Return
     */
    private void updateVisitorSubSceneBus(BasePersonPutInfo basePersonPutInfo){
        String personId = basePersonPutInfo.getId();
        ChannelSubtreasuryBusi sbusiCondition = new ChannelSubtreasuryBusi();
        sbusiCondition.setPersonId(personId);
        List<ChannelSubtreasuryBusi> sbusiList =
            channelSubtreasuryBusiMapper.selectChannelSubtreasuryBusiList(sbusiCondition);
        if (CollectionUtils.isEmpty(sbusiList)) {
            throw new CustomException("您的该次邀请出错，请联系相关人员");
        }
        sbusiList.forEach(sbusi -> {
            Long sbusiSeqNum = busiLiveUpdateSeqService.incrementAndGetSubtreasuryBusiSeqNum();
            sbusi.setUpdateSeriaNum(sbusiSeqNum);
            sbusi.setUpdateTime(DateUtils.getNowDate());
            channelSubtreasuryBusiMapper.updateChannelSubtreasuryBusi(sbusi);
        });
    }


    /**
     * @Author zgy
     * @Date 2021/10/27 10:14
     * @Description 保存访客人脸图片信息
     * @Param [basePersonPutInfo]
     * @Return void
     */
    private void saveVisitorFace(BasePersonPutInfo basePersonPutInfo){
        BasePersonFacePutInfo facePutInfo = basePersonPutInfo.getFacePutInfo();
        if (null == facePutInfo || StringUtils.isBlank(facePutInfo.getImageBase64())) {
            throw new CustomException("人脸图片未上传");
        }

        // 超过500K,压缩图片;  把前台传过来的base64，先存成文件，然后压缩这个文件，然后从这个压缩文件中重新提取出base64，然后删除这个文件
        // 这里压缩图片，不要加密，压缩的目的是得到压缩后的base64，不是加密后的base64
        String filePathName = personFaceRecogLogicService.uploadFaceImg(false,null,facePutInfo.getImageBase64(),null);
        deviceAccessAdapterServiceImpl.dealImage(filePathName);
        String compressImageBase64=PlatformFileUtils.getImageBase64(filePathName);
        facePutInfo.setImageBase64(compressImageBase64);
        CompletableFuture.runAsync(() -> PlatformFileUtils.deleteFile(filePathName));


        // 这里我先判断上传的照片是否重复
        findRepeatFaceImage(facePutInfo.getImageBase64());

        // 活体检测
        if (personFaceRecogLogicService.getFaceAddIsCheckLive()) {
            CheckLiveResponse checkLiveResponse =
                personFaceRecogLogicService.checkLive(facePutInfo.getImageBase64(), null);
            if (!checkLiveResponse.getResult()) {
                throw new CustomException("人脸图片活体检测未通过，请检查!");
            }
        }
        String stockImgFeature = null;
        BasePersonFace srcFace = new BasePersonFace();
        srcFace.setUniqueId(basePersonPutInfo.getUniqueId());
        srcFace.setPersonId(basePersonPutInfo.getId());
        srcFace.setEncrypted(StringUtils.isBlank(facePutInfo.getEncrypted()) ? DictConstants.Encrypted.ENABLE
            : facePutInfo.getEncrypted());
        srcFace.setDatasource(DictConstants.DataSource.INTERFACE);
        // 这里调用abis服务只进行人脸特征提取人脸图片保存，不进行其他操作
        BasePersonFace destFace = personFaceRecogLogicService.execCheckAndUploadFace(facePutInfo.getImageBase64(), null,
            srcFace, stockImgFeature, false, false);
        // 把人脸base64加密一下，后面要用这个来判断是否有重复的照片在库里
        String faceImageMd5 = Md5Utils.encryption(facePutInfo.getImageBase64());
        destFace.setFaceImageMd5(faceImageMd5);
        basePersonFaceMapper.insertBasePersonFace(destFace);
    }

    /**
     * @Author zgy
     * @Date 2021/11/9 18:06
     * @Description 保存访客人脸图片的时候，查找一下库里是否有同一张照片，有就不可以继续后续流程
     * @Param [imageBase64]
     * @Return void
     */
    void findRepeatFaceImage(String imageBase64){
        // 这里我先判断上传的照片是否重复
        String encryption = Md5Utils.encryption(imageBase64);
        BasePersonFace condition=new BasePersonFace();
        condition.setPersonType(PersonTypeEnum.VISITOR.value());
        condition.setStatus(DictConstants.Status.ENABLE);
        condition.setFaceImageMd5(encryption);
        List<BasePersonFace> faceList = visitorMapper.findRepeatFaceImage(condition);
        if(!CollectionUtils.isEmpty(faceList)){
            // 如果查找到了相同的人脸信息，提示照片重复，请重新上传
            throw new CustomException("请不要选择同一张照片",501);
        }
    }

    /**
     * 字符串转换unicode
     * @param string
     * @return
     */
    public static String string2Unicode(String string) {
        StringBuffer unicode = new StringBuffer();
        for (int i = 0; i < string.length(); i++) {
            // 取出每一个字符
            char c = string.charAt(i);
            // 转换为unicode
            unicode.append("\\u" + Integer.toHexString(c));
        }

        return unicode.toString();
    }

}
