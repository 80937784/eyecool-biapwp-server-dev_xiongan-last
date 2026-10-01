package cn.eyecool.scene.event.listener;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.internal.Lists;

import cn.eyecool.basedata.event.ListChannelInfoEvent;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.scene.domain.ChannelBusiness;
import cn.eyecool.scene.domain.ChannelInfo;
import cn.eyecool.scene.domain.ChannelSubtreasuryBusi;
import cn.eyecool.scene.domain.ChannelSubtreasuryInfo;
import cn.eyecool.scene.mapper.ChannelBusinessMapper;
import cn.eyecool.scene.mapper.ChannelInfoMapper;
import cn.eyecool.scene.mapper.ChannelSubtreasuryBusiMapper;
import cn.eyecool.scene.mapper.ChannelSubtreasuryInfoMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * 查询场景信息列表事件监听类
 * 
 * @author mawj
 * @date 2024/04/22
 */
@Component
@Slf4j
public class ListChannelInfoEventListener {

    @Autowired
    private ChannelInfoMapper channelInfoMapper;
    @Autowired
    private ChannelSubtreasuryInfoMapper channelSubtreasuryInfoMapper;
    @Autowired
    private ChannelBusinessMapper channelBusinessMapper;
    @Autowired
    private ChannelSubtreasuryBusiMapper channelSubtreasuryBusiMapper;

    /**
     * 人员基础信息改变事件处理
     * 
     * @param event
     */
    @Async
    @TransactionalEventListener(fallbackExecution = true)
    public void processListChanneEvent(ListChannelInfoEvent event) {
        log.info("list channel info event monitoring and processing,tenantId:[{}],cascade:[{}],channelCode:[{}]",
            TenantContextHolder.getTenantId(), event.getCascade(), event.getChannelCode());
        try {
            String uniqueId = event.getUniqueId();
            List<String> channelCodeScopeList = Lists.newArrayList();
            List<String> subCodeScopeList = Lists.newArrayList();
            if (StringUtils.isNotBlank(uniqueId)) {
                ChannelBusiness channelBusi = new ChannelBusiness();
                channelBusi.setUniqueId(uniqueId);
                channelBusi.setStatus(DictConstants.Status.ENABLE);
                List<ChannelBusiness> businessList = channelBusinessMapper.selectChannelBusinessList(channelBusi);
                channelCodeScopeList.addAll(
                    businessList.stream().map(it -> it.getChannelCode()).distinct().collect(Collectors.toList()));
                ChannelSubtreasuryBusi subBusi = new ChannelSubtreasuryBusi();
                subBusi.setUniqueId(uniqueId);
                subBusi.setStatus(DictConstants.Status.ENABLE);
                List<ChannelSubtreasuryBusi> subBusiList =
                    channelSubtreasuryBusiMapper.selectChannelSubtreasuryBusiList(subBusi);
                subCodeScopeList.addAll(
                    subBusiList.stream().map(it -> it.getSubTreasuryCode()).distinct().collect(Collectors.toList()));
            }
            ChannelInfo channelInfo = new ChannelInfo();
            if (StringUtils.isNotBlank(event.getChannelCode())) {
                channelInfo.setChannelCode(event.getChannelCode());
            }
            List<ChannelInfo> channelList = channelInfoMapper.selectChannelInfoList(channelInfo);
            if (CollectionUtils.isEmpty(channelList)) {
                event.getCallback().onSuccess(Collections.EMPTY_LIST);
                return;
            }
            List<JSONObject> list = channelList.stream()
                .filter(el -> StringUtils.isNotBlank(uniqueId) && channelCodeScopeList.contains(el.getChannelCode())
                    || StringUtils.isBlank(uniqueId))
                .map(it -> {
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("channelCode", it.getChannelCode());
                    jsonObj.put("channelName", it.getChannelName());
                    if (!Boolean.TRUE.equals(event.getCascade())) {
                        return jsonObj;
                    }
                    ChannelSubtreasuryInfo subInfo = new ChannelSubtreasuryInfo();
                    subInfo.setChannelId(it.getId());
                    List<ChannelSubtreasuryInfo> subList =
                        channelSubtreasuryInfoMapper.selectChannelSubtreasuryInfoList(subInfo);
                    jsonObj.put("subList", subList.stream()
                        .filter(subEl -> StringUtils.isNotBlank(uniqueId)
                            && subCodeScopeList.contains(subEl.getSubTreasuryCode()) || StringUtils.isBlank(uniqueId))
                        .map(subItem -> {
                            JSONObject subObj = new JSONObject();
                            subObj.put("subTreasuryCode", subItem.getSubTreasuryCode());
                            subObj.put("subTreasuryName", subItem.getSubTreasuryName());
                            return subObj;
                        }).collect(Collectors.toList()));
                    return jsonObj;
                }).collect(Collectors.toList());
            event.getCallback().onSuccess(list);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            event.getCallback().onError(e.getMessage());
        }
    }
}
