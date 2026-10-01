package cn.eyecool.server.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.http.HttpAjaxResult;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.scene.trade.entity.PersonBusiOpen;
import cn.eyecool.scene.trade.service.IChannelBusiCommonHttpService;

/**
 * 场景人员信息HTTP请求处理器
 * 
 * @author admin
 * @date 2019年11月7日
 */
@Component
public class ChannelBusiHttpHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ChannelBusiHttpHandler.class);

    @Autowired
    private IChannelBusiCommonHttpService channelBusiCommonHttpService;

    /**
     * 开通人脸功能
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult openPersonFace(String bizContent) {
        return openOrCloseBusi(bizContent, true, DictConstants.BioAttestType.FACE);
    }

    /**
     * 关闭人脸功能
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult closePersonFace(String bizContent) {
        return openOrCloseBusi(bizContent, false, DictConstants.BioAttestType.FACE);
    }

    /**
     * 开通指纹
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult openPersonFinger(String bizContent) {
        return openOrCloseBusi(bizContent, true, DictConstants.BioAttestType.FINGER);
    }

    /**
     * 关闭指纹
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult closePersonFinger(String bizContent) {
        return openOrCloseBusi(bizContent, false, DictConstants.BioAttestType.FINGER);
    }

    /**
     * 开通虹膜 *
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult openPersonIris(String bizContent) {
        return openOrCloseBusi(bizContent, true, DictConstants.BioAttestType.IRIS);
    }

    /**
     * 关闭虹膜
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult closePersonIris(String bizContent) {
        return openOrCloseBusi(bizContent, false, DictConstants.BioAttestType.IRIS);
    }

    /**
     * 开通指静脉
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult openPersonFvein(String bizContent) {
        return openOrCloseBusi(bizContent, true, DictConstants.BioAttestType.FVEIN);
    }

    /**
     * 关闭指静脉
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult closePersonFvein(String bizContent) {
        return openOrCloseBusi(bizContent, false, DictConstants.BioAttestType.FVEIN);
    }

    /**
     * 开通人脸虹膜多模态
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult openPersonFaceIris(String bizContent) {
        return openOrCloseBusi(bizContent, true, DictConstants.BioAttestType.FACE_IRIS);
    }

    /**
     * 关闭人脸虹膜多模态
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult closePersonFaceIirs(String bizContent) {
        return openOrCloseBusi(bizContent, false, DictConstants.BioAttestType.FACE_IRIS);
    }

    /**
     * 开通或者关闭业务
     * 
     * @param bizContent
     * @param isOpen true开通 false关闭
     * @param bioAttestType 生物类型[DictConstants.BioAttestType]
     * @return
     */
    private AjaxResult openOrCloseBusi(String bizContent, boolean isOpen, String bioAttestType) {
        // json转换
        PersonBusiOpen personBusiOpen = null;
        try {
            personBusiOpen = JSONObject.parseObject(bizContent, PersonBusiOpen.class);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        // 参数校验
        AjaxResult ajaxResult = validateOpenOrCloseBusiParam(personBusiOpen);
        if (!HttpAjaxResult.HTTP_SUCC_CODE.equals(ajaxResult.get(AjaxResult.CODE_TAG))) {
            return ajaxResult;
        }
        try {
            if (isOpen) {// 开通
                channelBusiCommonHttpService.openPersonChannelBusi(personBusiOpen, bioAttestType);
            } else {// 关闭
                channelBusiCommonHttpService.closePersonChannelBusi(personBusiOpen, bioAttestType);
            }
            return HttpAjaxResult.httpSuccess();
        } catch (CustomException e) {
            LOG.error(isOpen ? "open" : "close" + "fail", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error(isOpen ? "open" : "close" + "fail", e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 校验开通、关闭功能参数
     * 
     * @param personBusiOpen
     * @return
     */
    private AjaxResult validateOpenOrCloseBusiParam(PersonBusiOpen personBusiOpen) {
        String msg = null;
        // 人员标识不能为空, 且长度不大于48
        String uniqueId = personBusiOpen.getUniqueId();
        if (StringUtils.isBlank(uniqueId) || uniqueId.length() > 48) {
            msg = MessageUtils.message("base.person.handler.uniqueid.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 业务流水号不能为空, 且长度不大于48
        String receivedSeq = personBusiOpen.getReceivedSeq();
        if (StringUtils.isBlank(receivedSeq) || receivedSeq.length() > 48) {
            msg = MessageUtils.message("base.person.handler.receivedseq.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 场景编码不能为空
        String channelCode = personBusiOpen.getChannelCode();
        if (StringUtils.isBlank(channelCode)) {
            msg = MessageUtils.message("bio.trade.handler.scenecode.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 业务号不为空时，不能超过25位
        String busiCodeFirst = personBusiOpen.getBusiCodeFirst();
        if (StringUtils.isNotBlank(busiCodeFirst) && busiCodeFirst.length() > 25) {
            msg = MessageUtils.message("channel.busi.handler.busicodefirst.max.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 业务号不为空时，不能超过25位
        String busiCodeSecond = personBusiOpen.getBusiCodeSecond();
        if (StringUtils.isNotBlank(busiCodeSecond) && busiCodeSecond.length() > 25) {
            msg = MessageUtils.message("channel.busi.handler.busicodesecond.max.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 业务号不为空时，不能超过25位
        String busiCodeThird = personBusiOpen.getBusiCodeThird();
        if (StringUtils.isNotBlank(busiCodeThird) && busiCodeThird.length() > 25) {
            msg = MessageUtils.message("channel.busi.handler.busicodethird.max.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        return HttpAjaxResult.httpSuccess();
    }

    /**
     * 删除场景人员
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult deleteChannelPerson(String bizContent) {
        // json转换
        JSONObject parseObject = null;
        try {
            parseObject = JSON.parseObject(bizContent);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        String msg = null;
        // 业务流水号不能为空, 且长度不大于48
        String receivedSeq = (String)parseObject.get("receivedSeq");
        if (StringUtils.isBlank(receivedSeq) || receivedSeq.length() > 48) {
            msg = MessageUtils.message("base.person.handler.receivedseq.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 场景编码不能为空
        String channelCode = (String)parseObject.get("channelCode");
        if (StringUtils.isBlank(channelCode)) {
            msg  =MessageUtils.message("bio.trade.handler.scenecode.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 人员唯一标识不能为空
        String uniqueId = (String)parseObject.get("uniqueId");
        if (StringUtils.isBlank(uniqueId)) {
            msg = MessageUtils.message("base.person.handler.uniqueid.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        try {
            channelBusiCommonHttpService.deleteChannelPerson(channelCode, uniqueId);
            return HttpAjaxResult.httpSuccess();
        } catch (CustomException e) {
            LOG.error("Delete scene library personnel exception", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("Delete scene library personnel exception", e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 查询场景库人员是否存在
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult queryChannelPersonExists(String bizContent) {
        // json转换
        JSONObject parseObject = null;
        try {
            parseObject = JSON.parseObject(bizContent);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        String msg = null;
        // 场景编码不能为空
        String channelCode = (String)parseObject.get("channelCode");
        if (StringUtils.isBlank(channelCode)) {
            msg = MessageUtils.message("bio.trade.handler.scenecode.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 人员唯一标识不能为空
        String uniqueId = (String)parseObject.get("uniqueId");
        if (StringUtils.isBlank(uniqueId)) {
            msg  =MessageUtils.message("base.person.handler.uniqueid.empty");
            //msg = "人员唯一标识[uniqueId]不能为空";
            return HttpAjaxResult.businessDataValidError(msg);
        }
        try {
            boolean exists = channelBusiCommonHttpService.queryChannelPersonExists(channelCode, uniqueId);
            return HttpAjaxResult.httpSuccess(Boolean.valueOf(exists));
        } catch (CustomException e) {
            LOG.error("Querying whether there is any abnormality in the scene library staff", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("Querying whether there is any abnormality in the scene library staff", e);
            return HttpAjaxResult.httpError();
        }
    }

}
