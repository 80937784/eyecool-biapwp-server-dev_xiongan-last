package cn.eyecool.tradelog.domain.police;

import cn.eyecool.tradelog.domain.police.face.SubImageList;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author zfx
 * @ClassName PoliceFace
 * @description 公安接口中人脸对象
 * @since 2026/8/19 16:47
 **/
@Data
public class PoliceFace {
    // 48位
    @JSONField(name = "FaceID")
    private String FaceId;

    // 0其他 1 自动采集 2 人工采集
    @JSONField(name = "InfoKind")
    private Integer InfoKind;

    /**来源标识，BasicObjectIDType，R必选 图像id  41位*/
    @JSONField(name = "SourceID")
    private String SourceID;

    /**是否涉恐人员 int R：0否;1是;2不确定*/
    @JSONField(name = "IsSuspectedTerrorist")
    private Integer IsSuspectedTerrorist = 0;

    /**是否涉案人员 int R：0否;1是;2不确定*/
    @JSONField(name = "IsCriminalInvolved")
    private Integer IsCriminalInvolved = 0;

    /**是否在押人员 int R：0否;1是;2不确定，人工采集必填*/
    @JSONField(name = "IsDetainees")
    private Integer IsDetainees = 0;

    /**是否被害人 int R：0否;1是;2不确定*/
    @JSONField(name = "IsVictim")
    private Integer IsVictim = 0;

    /**是否可疑人员 int R：0否;1是;2不确定*/
    @JSONField(name = "IsSuspiciousPerson")
    private Integer IsSuspiciousPerson = 0;

    // ========== R/O 条件必选（自动采集必传） ==========
    /**设备编码 DevicelDType，R/O：设备编码，自动采集必选
     * 20 位纯数字 = 中心编码 (8 位)+ 行业编码 (2 位)+ 类型编码 (3 位)+ 序号 (7 位)
     *  13310001 11 120 6 六位序号
     * */
    @JSONField(name = "DeviceID")
    private String DeviceID;

    /**左上角X坐标 int，R/O*/
    @JSONField(name = "LeftTopX")
    private Integer LeftTopX = 0;

    /**左上角Y坐标 int，R/O*/
    @JSONField(name = "LeftTopY")
    private Integer LeftTopY = 0;

    /**右下角X坐标 int，R/O*/
    @JSONField(name = "RightBtmX")
    private Integer RightBtmX = 639;

    /**右下角Y坐标 int，R/O*/
    @JSONField(name = "RightBtmY")
    private Integer RightBtmY = 479;

    // ========== O 可选字段 ==========
    /**位置标记时间 dateTime YYYYMMDDhhmmssMMM:
     * 第一组MM表示月，第二组mm表示分.第三组MMM表示毫秒
     * */
    @JSONField(name = "LocationMarkTime")
    private String LocationMarkTime;

    /**人脸出现时间 dateTime，人工采集有效*/
    @JSONField(name = "FaceAppearTime")
    private String FaceAppearTime;

    @JSONField(name = "FaceDisAppearTime")
    private String FaceDisAppearTime;

    /**证件种类 IDType 3位 111 身份证 114 军官证 123 警官证  414 普通护照*/
    @JSONField(name = "IDType")
    private String IDType;

    /**证件号码 IDNumberType */
    @JSONField(name = "IDNumber")
    private String IDNumber;

    /**姓名 NameType 小于50位*/
    @JSONField(name = "Name")
    private String Name;

    /**曾用名 UsedNameType*/
    @JSONField(name = "UsedName")
    private String UsedName;

    /**绰号 AliasType*/
    @JSONField(name = "Alias")
    private String Alias;

    /**性别代码 GenderCode 0未知 1 男 2女 9未说明*/
    @JSONField(name = "GenderCode")
    private String GenderCode;

    /**是否驾驶员 int R/O：0否;1是;2不确定*/
    @JSONField(name = "IsDriver")
    private Integer IsDriver = 2;

    /**是否涉外人员 int R/O：0否;1是;2不确定*/
    @JSONField(name = "IsForeigner")
    private Integer IsForeigner = 0;

    /**年龄上限 int，最大可能年龄*/
    @JSONField(name = "AgeUpLimit")
    private Integer AgeUpLimit;

    /**年龄下限 int，最小可能年龄*/
    @JSONField(name = "AgeLowerLimit")
    private Integer AgeLowerLimit;

    /**民族代码 EthicCodeType*/
    @JSONField(name = "EthicCode")
    private String EthicCode;

    /**国籍代码 NationalityCodeType*/
    @JSONField(name = "NationalityCode")
    private String NationalityCode;

    /**籍贯省市县代码 PlaceCodeType*/
    @JSONField(name = "NativeCityCode")
    private String NativeCityCode;

    /**居住地行政区划 PlaceCodeType*/
    @JSONField(name = "ResidenceAdminDivision")
    private String ResidenceAdminDivision;

    /**汉语口音代码 ChineseAccentCodeType*/
    @JSONField(name = "ChineseAccentCode")
    private String ChineseAccentCode;

    /**职业类别代码 JobCategoryType*/
    @JSONField(name = "JobCategory")
    private String JobCategory;

    /**同行人员数 int*/
    @JSONField(name = "AccompanyNumber")
    private Integer AccompanyNumber;

    /**肤色 SkinColorType*/
    @JSONField(name = "SkinColor")
    private String SkinColor;

    /**发型 HairStyleType*/
    @JSONField(name = "HairStyle")
    private String HairStyle;

    /**发色 ColorType*/
    @JSONField(name = "HairColor")
    private String HairColor;

    /**脸型 FaceStyleType*/
    @JSONField(name = "FaceStyle")
    private String FaceStyle;

    /**脸部特征 FacialFeatureType*/
    @JSONField(name = "FacialFeature")
    private String FacialFeature;

    /**体貌特征 PhysicalFeatureType*/
    @JSONField(name = "PhysicalFeature")
    private String PhysicalFeature;

    /**口罩颜色 ColorType*/
    @JSONField(name = "RespiratorColor")
    private String RespiratorColor;

    /**帽子款式 HatStyleType*/
    @JSONField(name = "CapStyle")
    private String CapStyle;

    /**帽子颜色 ColorType*/
    @JSONField(name = "CapColor")
    private String CapColor;

    /**眼镜款式 GlassesStyleType*/
    @JSONField(name = "GlassStyle")
    private String GlassStyle;

    /**眼镜颜色 ColorType*/
    @JSONField(name = "GlassColor")
    private String GlassColor;

    /**护照证件种类 enPassportType*/
    @JSONField(name = "PassportType")
    private String PassportType;

    /**出入境人员类别代码 ImmigrantTypeCodeType*/
    @JSONField(name = "ImmigrantTypeCode")
    private String ImmigrantTypeCode;

    /**涉恐人员编号 SuspectedTerroristNumberType*/
    @JSONField(name = "SuspectedTerroristNumber")
    private String SuspectedTerroristNumber;

    /**涉案人员专长代码 CriminalInvolvedSpecilisationCodeType*/
    @JSONField(name = "SpecilisationCode")
    private String SpecilisationCode;

    /**体表特殊标记 BodySpecialMarkType*/
    @JSONField(name = "BodySpecialMark")
    private String BodySpecialMark;

    /**作案手段 CrimeMethodType*/
    @JSONField(name = "CrimeMethod")
    private String CrimeMethod;

    /**作案特点代码 CrimeCharacterCodeType*/
    @JSONField(name = "CrimeCharacterCode")
    private String CrimeCharacterCode;

    /**在逃人员编号 EscapedCriminalNumberType*/
    @JSONField(name = "EscapedCriminalNumber")
    private String EscapedCriminalNumber;

    /**看守所编码 DetentionHouseCodeType*/
    @JSONField(name = "DetentionHouseCode")
    private String DetentionHouseCode;

    /**在押人员身份 DetaineesIdentityType*/
    @JSONField(name = "DetaineesIdentity")
    private String DetaineesIdentity;

    /**在押人员特殊身份 DetaineesSpecialIdentityType*/
    @JSONField(name = "DetaineesSpecialIdentity")
    private String DetaineesSpecialIdentity;

    /**成员类型代码 MemberTypeCodeType*/
    @JSONField(name = "MemberTypeCode")
    private String MemberTypeCode;

    /**被害人种类 VictimType*/
    @JSONField(name = "VictimType")
    private String VictimType;

    /**受伤害程度 InjuredDegreeType*/
    @JSONField(name = "InjuredDegree")
    private String InjuredDegree;

    /**尸体状况代码 CorpseConditionCodeType*/
    @JSONField(name = "CorpseConditionCode")
    private String CorpseConditionCode;

    /**姿态分布 int 1平视;2微仰;3微俯;4左微侧脸...*/
    @JSONField(name = "Attitude")
    private Integer Attitude;

    /**相似度 Double [0‑1]*/
    @JSONField(name = "Similaritydegree")
    private Double Similaritydegree;

    /**眉型 string 32*/
    @JSONField(name = "EyebrowStyle")
    private String EyebrowStyle;

    /**鼻型 string 32*/
    @JSONField(name = "NoseStyle")
    private String NoseStyle;

    /**胡型 string 32*/
    @JSONField(name = "MustacheStyle")
    private String MustacheStyle;

    /**嘴唇 string 32*/
    @JSONField(name = "LipStyle")
    private String LipStyle;

    /**皱纹眼袋 string 32*/
    @JSONField(name = "WrinklePouch")
    private String WrinklePouch;

    /**痤疮色斑 string 32*/
    @JSONField(name = "AcneStain")
    private String AcneStain;

    /**黑痣胎记 string 32，多个英文分号;分隔*/
    @JSONField(name = "FreckleBirthmark")
    private String FreckleBirthmark;

    /**疤痕酒窝 string 32*/
    @JSONField(name = "ScarDimple")
    private String ScarDimple;

    /**其他特征 string32*/
    @JSONField(name = "OtherFeature")
    private String OtherFeature;

    /**图像列表：0或多个子图像对象*/
    @JSONField(name = "SubImageList")
    private SubImageList SubImageList;
}