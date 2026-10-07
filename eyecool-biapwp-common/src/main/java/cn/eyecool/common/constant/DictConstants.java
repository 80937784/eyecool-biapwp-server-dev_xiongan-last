package cn.eyecool.common.constant;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * 字典常量
 *
 * @author mawj
 * @date 2020/11/06
 */
public interface DictConstants {

    /** HTTP请求接口字典类型 */
    static final String HTTP_INTREFACE_DICT_TYPE = "http_interface";
    /** 手指编号类型 */
    static final String BIO_FINGER_NO_DICT_TYPE = "bio_finger_code";
    /** 虹膜编码类型 */
    static final String BIO_EYE_CODE_DICT_TYPE = "bio_eye_code";
    /** 人员标记类型 */
    public static final String PERSON_FLAG_DICT_TYPE = "apply_person_flag";
    /** 多模态比对模式类型 */
    public static final String MULTI_MATCH_MODE_DICT_TYPE = "match_mode";
    /** 设备类型 */
    public static final String DEVICE_TYPE_DICT_TYPE = "client_device_type";

    /** 租户状态 */
    interface TenantState {
        public static final String NORMAL = "NORMAL"; // 正常
        public static final String FROZEN = "FROZEN";// 冻结
    }

    /** 租户创建来源 */
    interface TenantSource {
        public static final String REGIST = "REGIST"; // 注册
        public static final String BG_CREATE = "BG_CREATE";// 后台创建
    }

    /** 租户类型 */
    interface TenantType {
        public static final String TRIAL = "TRIAL"; // 试用租户
        public static final String FORMAL = "FORMAL";// 正式租户
    }

    /** 是否状态 */
    interface YesOrNoState {
        public static final String YES = "Y"; // 是
        public static final String NO = "N";// 否
    }

    /** 状态 */
    interface Status {
        public static final String ENABLE = "0"; // 正常
        public static final String DISABLE = "1";// 停用
    }

    /** 访客生效状态 */
    interface visitorEffectiveStatus {
        public static final String NOT_EFFECTIVE = "0"; // 未生效
        public static final String IN_EFFECT = "1";// 生效中
        public static final String EXPIRED = "2";// 已失效
    }

    /** 是否加密 */
    interface Encrypted {
        public static final String DISABLE = "0"; // 不加密
        public static final String ENABLE = "1";// 加密
    }

    /** 数据来源 */
    interface DataSource {
        public static final String HTTP_INTERFACE = "HTTP";// HTTP接口
        public static final String INTERFACE = "INTERFACE";// 内部接口
        public static final String IMP = "IMP";// 导入
        public static final String LIVE_UPDATE = "LIVEUPDATE";// 数据实时更新接口
        public static final String SYNC_UPDATE = "SYNC";// 中间表同步
    }

    /** 眼睛编码 */
    interface EyeCode {
        public static final String LEFT_EYE = "L"; // 左眼
        public static final String RIGHT_EYE = "R";// 右眼
    }

    /** 不确定指位手指编码 */
    interface UncertainFingerNo {
        public static final String LEFT_UNCERTAIN_FINGER = "98"; // 右手不确定指位
        public static final String RIGHT_UNCERTAIN_FINGER = "97"; // 左手不确定指位
        public static final String OTHER_UNCERTAIN_FINGER = "99"; // 右手不确定指位

        public static final List<String> uncertainFingerNoList =
            Lists.newArrayList(LEFT_UNCERTAIN_FINGER, RIGHT_UNCERTAIN_FINGER, OTHER_UNCERTAIN_FINGER);
    }

    /** 证件照片类型 */
    interface CertPhotoType {
        public static final String CERT_PHOTO = "0";// 证件照片
        public static final String ENTER_SCHOOL_PHOTO = "1";// 入学照片
        public static final String IN_SCHOOL_PHOTO = "2";// 在校照片
        public static final String GRADUATE_PHOTO = "3";// 毕业照片
    }

    /** 证件类型 */
    interface CertType {
        public static final String ID_CARD = "111";// 居民身份证
    }

    /** 生物信息图片类型 */
    interface BioPhotoType {
        public static final String FACE_PHOTO = "1";// 人脸图片
        public static final String FINGER_PHOTO = "2";// 指纹图片
        public static final String IRIS_PHOTO = "3";// 虹膜图片
    }

    /** 生物特征认证类型 */
    interface BioAttestType {
        public static final String FACE = "0"; // 人脸
        public static final String FINGER = "1";// 指纹
        public static final String IRIS = "2";// 虹膜
        public static final String FVEIN = "3";// 指静脉
        public static final String FACE_IRIS = "4"; // 人脸虹膜多模态

        public static final List<String> bioAttestTypeList = Lists.newArrayList(FACE, FINGER, IRIS, FVEIN, FACE_IRIS);
    }

    /** 生物特征识别开通状态 */
    interface BioModeStatus {
        public static final String ENABLE = "1"; // 开通
        public static final String DISABLE = "0";// 不开通
    }

    /** 生物特征认证结果 */
    interface BioResult {
        public static final String PASS = "0"; // 通过
        public static final String NOTPASS = "1";// 未通过
    }

    /** 设备创建方式 */
    interface DeviceCreateMethod {
        public static final String BG_CREATE = "1";// 后台创建
        public static final String SELF_REGISTER = "2";// 自主注册
        public static final String BG_IMPORT = "3";// 后台导入
    }

    /** 设备升级任务结果 */
    interface DeviceUpgradeTaskResult {
        public static final String TO_BE_EXECUTE = "1";// 待执行
        public static final String UPGRADE_SUCCESS = "2";// 成功
        public static final String UPGRADE_FAIL = "3";// 失败
        public static final String SKIP_UPGRAD = "4";// 跳过
    }

    /** 设备升级状态（日志） */
    interface DeviceUpgradeStatus {
        public static final String TO_BE_DOWNLOAD = "1";// 待下载
        public static final String TO_BE_UPGRADE = "2";// 待升级
        public static final String UPGRADE_SUCCESS = "3";// 升级成功
        public static final String UPGRADE_FAIL = "4";// 升级失败
        public static final String SKIP_UPGRAD = "5";// 跳过
    }

    /** 设备在线状态 */
    interface DeviceOnlineState {
        public static final String ONLINE = "1"; // 设备在线
        public static final String OFFLINE = "2";// 设备离线
    }

    /** 设备动作日志类型 */
    interface DeviceActionLogType {
        public static final String REGIST = "1"; // 注册
        public static final String ONLINE = "2";// 上线
        public static final String OFFLINE = "3";// 下线
        public static final String UPDATE = "4";// 更新
    }

    /** 消息发送模式 */
    interface MsgType {
        public static final String NORMAL_MSG = "1";// 普通发送
        public static final String TEMPLATE_MSG = "2";// 模板发送
    }

    /** 消息通知方式 */
    interface MsgNoticeMethod {
        public static final String SMS_METHOD = "1";// 短信
        public static final String MAIL_METHOD = "2";// 邮件
        public static final String WECHAT_METHOD = "3";// 微信
        public static final String DING_METHOD = "4";// 钉钉
    }

    /** 消息推送结果 */
    interface MsgResult {
        public static final String SUCCESS = "0";// 成功
        public static final String FAIL = "1";// 失败
    }

    /** 发送邮件类型 */
    interface MailType {
        public static final String TEXT_MAIL = "1";// 文本邮件
        public static final String HTML_MAIL = "2";// HTMl邮件
    }

    /** 发送钉钉消息类型 */
    interface DingtalkType {
        public static final String TEXT = "text";// 文本消息
        public static final String MARKDOWN = "markdown";// MARKDOWN消息
        public static final String LINK = "link"; // 链接消息
        public static final String IMAGE = "image";// 图片消息
        public static final String FILE = "file";// 文件消息
        public static final String OA = "oa";// OA消息
        public static final String ACTION_CARD = "action_card";// ACTION_CARD消息
        public static final String VOICE = "voice";// VOICE消息
    }

    /** 发送钉钉消息媒体文件类型 */
    interface DingtalkMediaType {
        public static final String IMAGE = "image";// 图片消息
        public static final String FILE = "file";// 文件消息
        public static final String VOICE = "voice";// VOICE消息
    }

    /** HTTP接口交易码 */
    interface HttpInterfaceTransCode {
        public static final String PERSON_INSERT = "PERSON_INFO_INSERT"; // 新增人员基础信息
        public static final String PERSON_UPDATE = "PERSON_INFO_UPDATE"; // 修改人员基础信息
        public static final String PERSON_DELETE = "PERSON_INFO_DELETE"; // 删除人员基础信息
        public static final String PERSON_SELECT = "PERSON_INFO_SELECT"; // 查询人员基础信息
        public static final String PERSON_FLAG_UPDATE = "PERSON_FLAG_UPDATE"; // 修改人员标记（黑白名单）

        public static final String PERSON_FACE_OPEN = "PERSON_FACE_OPEN"; // 开通人脸
        public static final String PERSON_FACE_CLOSE = "PERSON_FACE_CLOSE"; // 关闭人脸
        public static final String PERSON_FACE_RECOG = "PERSON_FACE_RECOG"; // 人脸识别(1:N)
        public static final String PERSON_FACE_VERIFY = "PERSON_FACE_VERIFY";// 人脸认证(1:1)

        public static final String PERSON_FINGER_OPEN = "PERSON_FINGER_OPEN";// 开通指纹
        public static final String PERSON_FINGER_CLOSE = "PERSON_FINGER_CLOSE";// 关闭指纹
        public static final String PERSON_FINGER_RECOG = "PERSON_FINGER_RECOG"; // 指纹识别(1:N)
        public static final String PERSON_FINGER_VERIFY = "PERSON_FINGER_VERIFY";// 指纹认证(1:1)

        public static final String PERSON_IRIS_OPEN = "PERSON_IRIS_OPEN";// 开通虹膜
        public static final String PERSON_IRIS_CLOSE = "PERSON_IRIS_CLOSE";// 关闭虹膜
        public static final String PERSON_IRIS_RECOG = "PERSON_IRIS_RECOG"; // 虹膜识别(1:N)
        public static final String PERSON_IRIS_VERIFY = "PERSON_IRIS_VERIFY";// 虹膜认证(1:1)

        public static final String PERSON_FVEIN_OPEN = "PERSON_FVEIN_OPEN";// 开通指静脉
        public static final String PERSON_FVEIN_CLOSE = "PERSON_FVEIN_CLOSE";// 关闭指静脉
        public static final String PERSON_FVEIN_RECOG = "PERSON_FVEIN_RECOG"; // 指静脉识别(1:N)
        public static final String PERSON_FVEIN_VERIFY = "PERSON_FVEIN_VERIFY";// 指静脉认证(1:1)

        public static final String MESSAGE_SMS = "MESSAGE_SEND_SMS"; // 短信推送
        public static final String MESSAGE_WECHAT = "MESSAGE_SEND_WECHAT"; // 微信服务号推送
        public static final String MESSAGE_EMAIL = "MESSAGE_SEND_EMAIL"; // 邮件推送
        public static final String MESSAGE_DING = "MESSAGE_SEND_DING"; // 钉钉推送
        public static final String MESSAGE_DING_RESULT = "MESSAGE_SEND_DING_RESULT"; // 钉钉推送结果查询
        public static final String MESSAGE_DING_UPOLOAD = "MESSAGE_DING_MEDIA_UPLOAD";// 钉钉媒体文件上传

        public static final String PERSON_LIVE_UPDATE = "PERSON_LIVE_UPDATE";// 人员信息实时更新
        public static final String PERSON_FACE_SEARCH_LOG_BAK = "PERSON_FACE_SEARCH_LOG_BAK";// 人脸识别日志回传接口
        public static final String PERSON_SUB_TREASURY_OPERATE = "PERSON_SUB_TREASURY_OPERATE";// 子场景操作
        public static final String CLIENT_VERSION_DOWNLOAD = "CLIENT_VERSION_DOWNLOAD";// 设备版本下载接口
        public static final String CLIENT_UPGRADE_SIGNAL = "CLIENT_UPGRADE_SIGNAL";// 设备是否需要更新确认接口
        public static final String CLIENT_UPGRADE_RESULT_BAK = "CLIENT_UPGRADE_RESULT_BAK";// 设备版本升级结果回写

        public static final String SYNC_OFFICE_INFO = "SYNC_OFFICE_INFO";// 组织机构中间表数据同步
        public static final String DEL_CHANNEL_PERSON = "DEL_CHANNEL_PERSON";// 删除场景人员
        public static final String QUERY_CHANNEL_PERSON = "QUERY_CHANNEL_PERSON_EXISTS";// 查询场景库人员是否存在

        public static final String QUREY_DEVICE_ACCESS_AUTH = "QUREY_DEVICE_ACCESS_AUTH";// 查询设备接入权限

        public static final String PERSON_FACE_FEATURE = "PERSON_FACE_FEATURE";// 人脸特征提取
        public static final String PERSON_FINGER_FEATURE = "PERSON_FINGER_FEATURE";// 指纹特征提取
        public static final String PERSON_IRIS_FEATURE = "PERSON_IRIS_FEATURE";// 虹膜特征提取
        public static final String PERSON_FACE_IMG_COMPARE = "PERSON_FACE_IMG_COMPARE";// 人脸图片比对
        public static final String PERSON_FINGER_IMG_COMPARE = "PERSON_FINGER_IMG_COMPARE";// 指纹图片比对
        public static final String PERSON_IRIS_IMG_COMPARE = "PERSON_IRIS_IMG_COMPARE";// 虹膜图片比对
        public static final String PERSON_FACE_CHECKLIVE = "PERSON_FACE_CHECKLIVE";// 人脸图片或视频检活
        public static final String PERSON_FACE_QUALITY_DETECT = "PERSON_FACE_QUALITY_DETECT";// 人脸图片质量检测
        public static final String PERSON_FINGER_QUALITY_DETECT = "PERSON_FINGER_QUALITY_DETECT";// 指纹图片质量检测
        public static final String PERSON_FACE_CHECKLIVE_AND_COMPARE = "PERSON_FACE_CHECKLIVE_AND_COMPARE";// 人脸视频检活和比对

        public static final String OCR_ATTR_DETECT_LOG = "OCR"; // ocr识别接口

        public static final String MULIT_IRIS_FACE_REGISTER = "MULIT_IRIS_FACE_REGISTER";// 人脸虹膜多模态注册
        public static final String PERSON_FACE_REGISTER = "PERSON_FACE_REGISTER";// 人脸注册

        public static final String MULIT_IRIS_FACE_SEARCH = "MULIT_IRIS_FACE_SEARCH";// 人脸虹膜多模态搜索1：N
        public static final String MULIT_IRIS_FACE_VERIFY = "MULIT_IRIS_FACE_VERIFY";// 人脸虹膜多模态搜索1：1

        public static final String PERSON_FACE_SEARCH_LOG_QUERY = "PERSON_FACE_SEARCH_LOG_QUERY"; // 人脸识别日志查询
        public static final String SUB_TREASURY_OPERATE = "SUB_TREASURY_OPERATE";// 子场景管理
        public static final String QUREY_DEVICE = "QUREY_DEVICE"; // 设备查询
        public static final String SYNC_DEVICE_PERSON_COUNT = "SYNC_DEVICE_PERSON_COUNT"; // 同步设备人员数


        public static final String HEALTH_CODE_SEARCH = "HEALTH_CODE_SEARCH";// 健康码查询

        public static final String PERSON_FACE_IRIS_OPEN = "PERSON_FACE_IRIS_OPEN";// 开通人脸虹膜多模态
        public static final String PERSON_FACE_IRIS_CLOSE = "PERSON_FACE_IRIS_CLOSE";// 关闭人脸虹膜多模态
        public static final String PERSON_FACE_IRIS_MULTI_LOG_BAK = "PERSON_FACE_IRIS_MULTI_LOG_BAK";// 人脸虹膜多模态识别日志回传
        public static final String PERSON_FACE_MATCH_LOG_BAK = "PERSON_FACE_MATCH_LOG_BAK";// 人脸1V1比对日志回传接口
        public static final String PERSON_FACE_IRIS_MULTI_LOG_QUERY = "PERSON_FACE_IRIS_MULTI_LOG_QUERY"; // 人脸虹膜多模态识别日志查询
        public static final String PERSON_IDENTITY_VERIFICATION = "PERSON_IDENTITY_VERIFICATION"; // 联网身份核查接口
        public static final String PERSON_FACE_MATCH_LOG_QUERY = "PERSON_FACE_MATCH_LOG_QUERY";// 人脸1v1日志查询接口
    }

    /** 1-N校验方式 */
    interface SearchNType {
        public static final String SEARCH_N_TYPE_SUB = "1"; // 校验子场景
        public static final String SEARCH_N_TYPE_CHANNEL = "2";// 校验场景库
        public static final String SEARCH_N_TYPE_ALL = "3";// 校验全库
        public static final String SEARCH_N_TYPE_SEQ = "4";// 依次查询 子场景->场景库->全库(查询到就截止)
    }

    /** 1-N搜索日志场景类型 */
    interface SearchNLogSceneType {
        public static final String BASE_INFO_PUT = "1"; // 基础信息入库1:N
        public static final String SEARCH_N_HTTP = "2";// 比对搜索接口1:N
        public static final String SEARCH_N_LOG_BAK = "3";// 子系统日志回传
    }

    /** 设备类型 */
    interface DeviceType {
        public static final String COMMNON_DEVICE = "1";// 常规
        public static final String NONINDUCTIVE_DEVICE = "2";// 无感
        public static final String FALLING_OBJECTS_DEVICE = "3";// 高空坠物
        public static final String ATTENDANCE_DEVICE = "4";// 考勤设备
    }

    /** 生物特征类型 */
    interface BioFeatureType {
        public static final String FACE_FEATURE = "face";
        public static final String FINGER_FEATURE_UNKNOWN = "finger";
        public static final String IRIS_FEATURE_UNKNOWN = "iris";
        public static final String FVEIN_FEATURE_UNKNOWN = "fvein";
        public static final String FACE_IRIS_FEATURE = "faceiris";
    }

    /** 人员标记 */
    interface PersonFlag {
        public static final String NORMAL = "1";// 正常
        public static final String RED = "2";// 红名单
        public static final String BLACK = "3";// 黑明单
    }

    /** 测温结果 */
    interface TemperatureResult {
        public static final String NOT_CONTROLL = "0";// 未部控
        public static final String NORMAL = "1";// 体温正常
        public static final String HIGHER = "2";// 高温报警
        public static final String LOWER = "3";// 低温报警
        public static final String UN_DETECTED = "4";// 未检测到
    }

    /** 通行核验方式 */
    interface PassValidType {
        public static final String ID_CARD = "01";// 身份证核验
        public static final String FACE = "02";// 生物(人脸、虹膜)核验
        public static final String HEALTH_CODE = "03";// 健康码核验
        public static final String CARD = "04";// 刷卡核验
    }

    /** 多模态人脸虹膜认证识别方式 */
    interface MuliIrisFaceVerifyType {
        public static final String VERIFY_IRIS = "0"; // 虹膜比对
        public static final String VERIFY_FACE = "1"; // 人脸比对
        public static final String VERIFY_IRIS_AND_FACE = "2";// 人脸和虹膜比对
        public static final String VERIFY_IRIS_OR_FACE = "3";// 人脸或虹膜比对
        public static final String VERIFY_IRIS_FACE_MULTI = "4";// 虹膜人脸多模态融合比对

        public static final List<String> muliIrisFaceVerifyTypeList = Lists.newArrayList(VERIFY_IRIS, VERIFY_FACE,
            VERIFY_IRIS_AND_FACE, VERIFY_IRIS_OR_FACE, VERIFY_IRIS_FACE_MULTI);
    }
}
