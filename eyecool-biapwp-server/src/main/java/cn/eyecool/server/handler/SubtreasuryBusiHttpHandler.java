package cn.eyecool.server.handler;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.internal.Lists;

import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.http.HttpAjaxResult;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.scene.constant.ChannelParamConstants;
import cn.eyecool.scene.constant.ChannelParamConstants.ChannelSubtreasuryOperateType;
import cn.eyecool.scene.constant.ChannelParamConstants.SubtreasuryOperateType;
import cn.eyecool.scene.domain.ChannelInfo;
import cn.eyecool.scene.domain.ChannelSubtreasuryInfo;
import cn.eyecool.scene.service.IChannelInfoService;
import cn.eyecool.scene.service.IChannelSubtreasuryInfoService;
import cn.eyecool.scene.trade.entity.SubtreasuryBusiOperateParam;
import cn.eyecool.scene.trade.entity.SubtreasuryOperateParam;
import cn.eyecool.scene.trade.service.IChannelBusiCommonHttpService;

/**
 * 场景人员信息HTTP请求处理器
 * 
 * @author admin
 * @date 2019年11月7日
 */
@Component
public class SubtreasuryBusiHttpHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SubtreasuryBusiHttpHandler.class);

    @Autowired
    private IChannelBusiCommonHttpService channelBusiCommonHttpService;
    @Autowired
    private IChannelInfoService channelInfoService;
    @Autowired
    private IChannelSubtreasuryInfoService subtreasuryInfoService;

    /**
     * 子场景人员信息操作
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult operatePersonSubtreasury(String bizContent) {
        // json转换
        SubtreasuryBusiOperateParam channelSubtreasuryBusiSaveParam = null;
        try {
            channelSubtreasuryBusiSaveParam = JSONObject.parseObject(bizContent, SubtreasuryBusiOperateParam.class);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        String msg = null;
        // 业务流水号不能为空, 且长度不大于48
        String receivedSeq = channelSubtreasuryBusiSaveParam.getReceivedSeq();
        if (StringUtils.isBlank(receivedSeq) || receivedSeq.length() > 48) {
            msg  =MessageUtils.message("base.person.handler.receivedseq.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 场景编码不能为空
        String channelCode = channelSubtreasuryBusiSaveParam.getChannelCode();
        if (StringUtils.isBlank(channelCode)) {
            msg  =MessageUtils.message("bio.trade.handler.scenecode.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 子场景编码不能为空，格式必须为[场景编码_子场景编码]
        String subTreasuryCode = channelSubtreasuryBusiSaveParam.getSubTreasuryCode();
        if (StringUtils.isBlank(subTreasuryCode) || !subTreasuryCode.startsWith(channelCode + "_")) {
            msg  =MessageUtils.message("bio.trade.handler.subscene.format");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        if (subTreasuryCode.length() > 80) {
            msg  =MessageUtils.message("sub.busi.handler.subscene.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 操作类型不能为空
        String operateType = channelSubtreasuryBusiSaveParam.getOperateType();
        if (StringUtils.isBlank(operateType)
            || !ChannelParamConstants.ChannelSubtreasuryOperateType.typeList.contains(operateType)) {
            msg  =MessageUtils.message("sub.busi.handler.optiontype");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        String isAppend = channelSubtreasuryBusiSaveParam.getIsAppend();
        List<String> YesOrNoStateList =
            Lists.newArrayList(DictConstants.YesOrNoState.YES, DictConstants.YesOrNoState.NO);
        if (ChannelSubtreasuryOperateType.OPERATE_TYPE_ADD.equals(operateType)
            && (StringUtils.isBlank(isAppend) || !YesOrNoStateList.contains(isAppend))) {
            msg  =MessageUtils.message("sub.busi.handler.isappend.invalid");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 人员唯一标识不能为空
        String uniqueIds = channelSubtreasuryBusiSaveParam.getUniqueIds();
        if (StringUtils.isBlank(uniqueIds) && !ChannelSubtreasuryOperateType.OPERATE_TYPE_CLEAR.equals(operateType)) {
            msg  =MessageUtils.message("sub.busi.handler.uniqueIds.min.one");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        try {
            Map<String, Object> resultMap =
                channelBusiCommonHttpService.operatePersonSubtreasury(channelSubtreasuryBusiSaveParam);
            if (resultMap != null && resultMap.size() > 0) {
                Integer succNum = null == resultMap.get("succNum") ? 0 : (Integer)resultMap.get("succNum");
                Integer failNum = null == resultMap.get("failNum") ? 0 : (Integer)resultMap.get("failNum");
                if (succNum > 0 && failNum == 0) {
                    return HttpAjaxResult.httpSuccess(resultMap);
                } else if (succNum > 0 && failNum > 0) {
                    return HttpAjaxResult.httpSuccess(MessageUtils.message("sub.busi.handler.part.success"), resultMap);
                } else if (succNum == 0 && failNum > 0) {
                    return HttpAjaxResult.httpError(resultMap);
                }
            }
            return HttpAjaxResult.httpSuccess();
        } catch (CustomException e) {
            LOG.error("Person sub-scenario operation failed", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("Person sub-scenario operation failed", e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 子场景信息操作
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult operateSubtreasury(String bizContent) {
        // json转换
        SubtreasuryOperateParam subtreasuryOperateParam = null;
        try {
            subtreasuryOperateParam = JSONObject.parseObject(bizContent, SubtreasuryOperateParam.class);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        String msg = null;
        // 业务流水号不能为空, 且长度不大于48
        String receivedSeq = subtreasuryOperateParam.getReceivedSeq();
        if (StringUtils.isBlank(receivedSeq) || receivedSeq.length() > 48) {
            msg  =MessageUtils.message("base.person.handler.receivedseq.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 场景编码不能为空
        String channelCode = subtreasuryOperateParam.getChannelCode();
        if (StringUtils.isBlank(channelCode)) {
            msg  =MessageUtils.message("bio.trade.handler.scenecode.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 查询场景是否存在
        ChannelInfo channelCondition = new ChannelInfo();
        channelCondition.setChannelCode(channelCode);
        List<ChannelInfo> channelInfoList = channelInfoService.selectChannelInfoList(channelCondition);
        if (CollectionUtils.isEmpty(channelInfoList)) {
            msg  = MessageUtils.message("channel.common.service.channelcode.not.exists",channelCode);
            LOG.error(msg);
            return HttpAjaxResult.businessError(msg);
        }
        String channelId = channelInfoList.get(0).getId();
        // 子场景编码不能为空，格式必须为[场景编码_子场景编码]
        String subTreasuryCode = subtreasuryOperateParam.getSubTreasuryCode();
        if (StringUtils.isBlank(subTreasuryCode) || !subTreasuryCode.startsWith(channelCode + "_")) {
            msg  =MessageUtils.message("bio.trade.handler.subscene.format");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        if (subTreasuryCode.length() > 80) {
            msg  =MessageUtils.message("sub.busi.handler.subscene.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 操作类型不能为空
        String operateType = subtreasuryOperateParam.getOperateType();
        if (StringUtils.isBlank(operateType)
            || !ChannelParamConstants.SubtreasuryOperateType.typeList.contains(operateType)) {
            msg  =MessageUtils.message("sub.busi.handler.optiontype");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 操作类型为ADD或UPDATE时，子场景名称必填
        if (SubtreasuryOperateType.OPERATE_TYPE_ADD.equals(subtreasuryOperateParam.getOperateType())
            || SubtreasuryOperateType.OPERATE_TYPE_UPDATE.equals(subtreasuryOperateParam.getOperateType())) {
            String name = subtreasuryOperateParam.getSubTreasuryName();
            if (StringUtils.isBlank(name)) {
                msg  =MessageUtils.message("sub.busi.handler.subscene.empty");
                return HttpAjaxResult.businessDataValidError(msg);
            }
            if (name.length() > 50) {
                msg  =MessageUtils.message("sub.busi.handler.subscene.length.limit");
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }
        // 备注信息校验
        String remark = subtreasuryOperateParam.getRemark();
        if (!StringUtils.isBlank(remark) && remark.length() > 100) {
            msg  =MessageUtils.message("sub.busi.handler.remark.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        try {
            // 首先查询子场景是否存在
            ChannelSubtreasuryInfo channelSubtreasuryInfo = new ChannelSubtreasuryInfo();
            channelSubtreasuryInfo.setChannelId(channelId);
            channelSubtreasuryInfo.setSubTreasuryCode(subTreasuryCode);
            List<ChannelSubtreasuryInfo> channelSubtreasuryInfos =
                subtreasuryInfoService.selectChannelSubtreasuryInfoList(channelSubtreasuryInfo);
            boolean isExists = true;
            if (CollectionUtils.isEmpty(channelSubtreasuryInfos)) {
                isExists = false;
            }
            if (SubtreasuryOperateType.OPERATE_TYPE_ADD.equals(operateType)) {
                if (isExists) {// 如果存在则更新
                    channelSubtreasuryInfo = channelSubtreasuryInfos.get(0);
                    if (!StringUtils.isBlank(remark)) {
                        channelSubtreasuryInfo.setRemark(remark);
                    }
                    channelSubtreasuryInfo.setSubTreasuryName(subtreasuryOperateParam.getSubTreasuryName());
                    subtreasuryInfoService.updateChannelSubtreasuryInfo(channelSubtreasuryInfo);
                } else {// 否则新增
                    if (!StringUtils.isBlank(remark)) {
                        channelSubtreasuryInfo.setRemark(remark);
                    }
                    channelSubtreasuryInfo.setSubTreasuryName(subtreasuryOperateParam.getSubTreasuryName());
                    subtreasuryInfoService.insertChannelSubtreasuryInfo(channelSubtreasuryInfo);
                }
            } else if (SubtreasuryOperateType.OPERATE_TYPE_UPDATE.equals(operateType)) {
                if (!isExists) {
                    msg  =MessageUtils.message("sub.busi.handler.update.subscene.not.exists");
                    return HttpAjaxResult.businessError(msg);
                }
                channelSubtreasuryInfo = channelSubtreasuryInfos.get(0);
                if (!StringUtils.isBlank(remark)) {
                    channelSubtreasuryInfo.setRemark(remark);
                }
                channelSubtreasuryInfo.setSubTreasuryName(subtreasuryOperateParam.getSubTreasuryName());
                subtreasuryInfoService.updateChannelSubtreasuryInfo(channelSubtreasuryInfo);
            }

            else if (SubtreasuryOperateType.OPERATE_TYPE_DELETE.equals(operateType)) {
                if (!isExists) {
                    msg  =MessageUtils.message("sub.busi.handler.delete.subscene.not.exists");
                    return HttpAjaxResult.businessError(msg);
                }
                // 删除
                subtreasuryInfoService.deleteChannelSubtreasuryInfoById(channelSubtreasuryInfos.get(0).getId());
            }
            return HttpAjaxResult.httpSuccess();
        } catch (Exception e) {
            LOG.error("Person sub-scenario operation failed", e);
            return HttpAjaxResult.httpError(e.getMessage());
        }
    }

}
