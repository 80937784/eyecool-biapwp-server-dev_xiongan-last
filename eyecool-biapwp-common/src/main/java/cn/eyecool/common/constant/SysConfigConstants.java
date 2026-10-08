package cn.eyecool.common.constant;

/**
 * 系统配置参数常量类
 * 
 * @author admin
 * @date 2019年10月16日
 */
public class SysConfigConstants {

    /** 人脸图片文件夹KEY值 */
    public static final String BASEDATA_FACE_DIR_KEY = "basedata.face.dir";

    /** 人脸1-N比对搜索图片文件夹KEY值 */
    public static final String BUSI_FACE_SEARCH_DIR_KEY = "busi.face.searchN.dir";

    /** 人脸1:1认证图片文件夹KEY值 */
    public static final String BUSI_FACE_COMPARE_DIR_KEY = "busi.face.compare.dir";

    /** 指纹图片文件夹KEY值 */
    public static final String BASEDATA_FINGER_DIR_KEY = "basedata.finger.dir";

    /** 指纹1-N比对搜索图片文件夹KEY值 */
    public static final String BUSI_FINGER_SEARCH_DIR_KEY = "busi.finger.searchN.dir";

    /** 指纹1:1认证图片文件夹KEY值 */
    public static final String BUSI_FINGER_COMPARE_DIR_KEY = "busi.finger.compare.dir";

    /** 虹膜图片文件夹KEY值 */
    public static final String BASEDATA_IRIS_DIR_KEY = "basedata.iris.dir";

    /** 虹膜1-N比对搜索图片文件夹KEY值 */
    public static final String BUSI_IRIS_SEARCH_DIR_KEY = "busi.iris.searchN.dir";

    /** 虹膜1:1认证图片文件夹KEY值 */
    public static final String BUSI_IRIS_COMPARE_DIR_KEY = "busi.iris.compare.dir";

    /** 证件照图片文件夹KEY值 */
    public static final String BASEDATA_CERT_DIR_KEY = "basedata.cert.dir";

    /** 人脸检活图片文件夹KEY值 */
    public static final String BUSI_CHECKLIVE_DIR_KEY = "busi.face.checklive.dir";

    /** 人脸图片质量检测阈值KEY值 */
    public static final String BASEDATA_FACE_QUALITY_DETECT_THRESHOLD_KEY = "basedata.face.quality.detect.threshold";

    /** 指纹图片质量检测阈值KEY值 */
    public static final String BASEDATA_FINGER_QUALITY_DETECT_THRESHOLD_KEY =
        "basedata.finger.quality.detect.threshold";

    /** 人脸1:1比对阈值KEY值 */
    public static final String BASEDATA_FACE_COMPARE_THRESHOLD_KEY = "basedata.face.compare.threshold";

    /** 人脸检活(静默)阈值KEY值 */
    public static final String BASEDATA_FACE_VIDEO_CHECKLIVE_THRESHOLD_KEY = "basedata.face.video.checklive.threshold";

    /** 人脸视频检活阈值KEY值 */
    public static final String BASEDATA_FACE_CHECKLIVE_THRESHOLD_KEY = "basedata.face.checklive.threshold";

    /** 人脸1-N搜索阈值KEY值 */
    public static final String BASEDATA_FACE_SEARCH_N_THRESHOLD_KEY = "basedata.face.searchN.threshold";

    /** 指纹1:1比对阈值KEY值 */
    public static final String BASEDATA_FINGER_COMPARE_THRESHOLD_KEY = "basedata.finger.compare.threshold";

    /** 指纹1-N搜索阈值KEY值 */
    public static final String BASEDATA_FINGER_SEARCH_N_THRESHOLD_KEY = "basedata.finger.searchN.threshold";

    /** 重复指纹阈值KEY值 */
    public static final String BASEDATA_FINGER_REPEAT_THRESHOLD_KEY = "basedata.finger.repeat.threshold";

    /** 虹膜1:1比对阈值KEY值 */
    public static final String BASEDATA_IRIS_COMPARE_THRESHOLD_KEY = "basedata.iris.compare.threshold";

    /** 虹膜1-N搜索阈值KEY值 */
    public static final String BASEDATA_IRIS_SEARCH_N_THRESHOLD_KEY = "basedata.iris.searchN.threshold";

    /** 重复虹膜阈值KEY值 */
    public static final String BASEDATA_IRIS_REPEAT_THRESHOLD_KEY = "basedata.iris.repeat.threshold";

    /** 人脸虹膜融合特征1v1比对阈值KEY值 */
    public static final String BASEDATA_FACE_IRIS_FUSION_COMPARE_THRESHOLD_KEY =
        "multi.irisface.fusion.compare.threshold";

    /** 多模态虹膜特征1v1比对阈值KEY值 */
    public static final String BASEDATA_MULTI_IRIS_COMPARE_THRESHOLD_KEY = "multi.iris.compare.threshold";

    /** 多模态人脸特征1v1比对阈值KEY值 */
    public static final String BASEDATA_MULTI_FACE_COMPARE_THRESHOLD_KEY = "multi.face.compare.threshold";

    /** 人脸自助采集是否在没有底库照时校验证件照KEY值,VALUE为[Y/N] */
    public static final String BASEDATA_FACE_COLLECT_VALIDATE_CERT = "basedata.face.collect.validte.cert";

    /** 平台接口请求并发量KEY值 */
    public static final String PLATFORM_INTERFACE_CONCURRENT_KEY = "platform.interface.concurrent";

    /** 人脸入库是否进行1-N校验KEY值 */
    public static final String BASEDATA_FACE_ADD_ISVALIDN_KEY = "basedata.face.add.isValidN";

    /** 人脸入库是否进行活体检测KEY值 */
    public static final String BASEDATA_FACE_ADD_CHECKLIVE_KEY = "basedata.face.add.checkLive";

    /** 指纹入库是否进行1-N校验KEY值 */
    public static final String BASEDATA_FINGER_ADD_ISVALIDN_KEY = "basedata.finger.add.isValidN";

    /** 虹膜入库是否进行1-N校验KEY值 */
    public static final String BASEDATA_IRIS_ADD_ISVALIDN_KEY = "basedata.iris.add.isValidN";

    /** APP版本文件存放文件夹KEY值 */
    public static final String DEVICE_VERSION_FILE_DIR_KEY = "device.version.file.dir";

    /** ABIS微服务调用超时时间（单位:s） */
    public static final String ABIS_SERVICE_CALL_TIMEOUT_KEY = "abis.service.call.timeout";

    /** 系统组织架构默认部门编码 */
    public static final String DEFAULT_SYS_DEPT_CODE = "default";

    /** 消息推送子系统附件存放位置 */
    public static final String MSG_ATTACHMENT_DIR_KEY = "msg.attachment.dir";

    /** 人脸1-N日志是否比对去重 */
    public static final String BUSI_FACE_SEARCH_LOG_SAVE_DISTINCT_KEY = "busi.face.searchN.log.save.distinct";

    /** 人脸1-N日志保存去重时间 */
    public static final String BUSI_FACE_SEARCH_LOG_DUPLICATE_TIME_KEY = "busi.face.searchN.log.duplicate.time";

    /** 平台是否开启1-N功能KEY值 */
    public static final String PLATFORM_SEARCH_N_FUNCTION_OPEN_KEY = "platform.searchN.function.isOpen";

    /** 平台是否可以自动绑定和解绑人库关系 */
    public static final String PLATFORM_AUTO_BIND_LIB_PERSON_REL_KEY = "platform.libperson.isAutobind";

    /** 平台需要自动绑定人库关系的场景编码 */
    public static final String AUTO_BIND_LIB_PERSON_REL_CHANNEL_CODE_KEY = "platform.libperson.autobind.channel";

    /** 生物信息变化触发信息同步的场景编码 */
    public static final String BIO_CHANGE_LIVE_UPDATE_CHANNEL_CODE_KEY = "person.biochange.liveupdate.channel";

    /** 设备外观图片存放文件夹KEY值 */
    public static final String DEVICE_MODEL_IMAGE_DIR_KEY = "device.model.image.dir";

    /** 平台是否接收场景回传日志（Y：是，N: 否） */
    public static final String PLATFORM_ACCEPT_BAK_LOG_OPEN_KEY = "platform.accept.baklog.open";

    /** 账号自助-是否开启用户注册功能 */
    public static final String SYS_ACCOUNT_REGISTERUSER_KEY = "sys.account.registerUser";

    /** 多模态1vN日志图片文件夹对应参数的KEY值 */
    public static final String BUSI_MULIT_SEARCH_DIR_KEY = "busi.mulit.search.dir";

    /** 多模态1v1日志图片文件夹对应参数的KEY值 */
    public static final String BUSI_MULIT_COMPARE_DIR_KEY = "busi.mulit.compare.dir";

    /** 多模态基础数据存储路径 */
    public static final String BASE_MULIT_DIR_KEY = "base.mulit.dir";

    /** SDk文件存储路径 */
    public static final String SYS_TOOL_SDK_FILE_DIR_KEY = "sys.tool.sdkFile.dir";

    /** 开通钉钉考勤数据同步的钉钉APPKEY */
    public static final String DINGTALK_SYNCDATA_APPKEY = "dingtalk.syncdata.appkey";

    /** 联网核查接口地址 */
    public static final String IDENTITY_VERIFICATION_URL_KEY = "identity_verification_url";

    /** 联网核查授权码 */
    public static final String IDENTITY_VERIFICATION_AUTHCODE_KEY = "identity_verification_authCode";

    /** 异常健康码推送微信公众号参数（格式为'{"appId":"123","templateId":"123","phone":"13011112222,13011113333"}'） */
    public static final String ABNORMAL_HEALTHCODE_PUSH_WEIXIN_PARAMS = "abnormal.recog.push.weixin.params";

    /** 异常体温推送微信公众号参数（格式为'{"appId":"123","templateId":"123","phone":"13011112222,13011113333"}'） */
    public static final String ABNORMAL_TEMPURATURE_PUSH_WEIXIN_PARAMS = "abnormal.temperature.push.weixin.params";

    /** 访客模块租户所在公司名称 */
    public static final String VISITOR_VISIT_COMPANY_NAME = "visitor.visit.company.name";

    /** 雄安一卡通请求 */
    public static final String XA_CARD_PASS_SYSNAME = "xa.cardpass.sysname";
    public static final String XA_CARD_PASS_APPID = "xa.cardpass.appid";
    public static final String XA_CARD_PASS_APPSECRET = "xa.cardpass.appsecret";
    public static final String XA_CARD_PASS_TOKEN_URL = "xa.cardpass.token.url";
    public static final String XA_CARD_PASS_RECORD_URL = "xa.cardpass.record.url";
    public static final String XA_CARD_PASS_AREACODE = "xa.cardpass.areacode";
    public static final String XA_AUTHORITY_STATUS_URL = "xa.authority.status.url";


    public static final String XA_POLICE_URL = "xa.police.url";
    public static final String XA_POLICE_DEVICE_ID = "xa.police.device.id";
    /** 订阅数据消息  */
    public static final String XA_POLICE_DEVICE_SUB_SCRIBEID = "xa.police.device.sub.subscribeId";
    public static final String XA_POLICE_DEVICE_SUB_USER_IDENTIFY = "xa.police.device.sub.userIdentify";

    public static final String XA_POLICE_DEVICE_URL = "xa.police.device.url";

    public static final String XA_POLICE_DATA_SUB_SCRIBEID = "xa.police.data.sub.subscribeId";
    public static final String XA_POLICE_DATA_SUB_USER_IDENTIFY = "xa.police.data.sub.userIdentify";
    public static final String XA_POLICE_DATA_IMAGE_URL = "xa.police.data.image.url";

}
