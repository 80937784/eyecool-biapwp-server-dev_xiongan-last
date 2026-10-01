package cn.eyecool.device.event.listener;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.device.mapper.DeviceInfoMapper;
import cn.eyecool.scene.event.SubTreasuryOperateEvent;
import cn.eyecool.scene.event.SubTreasuryOperateEventType;
import lombok.extern.slf4j.Slf4j;

/**
 * 子场景事件监听类
 * 
 * @author mawj
 * @date 2021/01/06
 */
@Component
@Slf4j
public class SubtreasuryEventListener {

    @Autowired
    private DeviceInfoMapper deviceInfoMapper;

    /**
     * 处理子场景清空或删除事件
     * 
     * @param event
     */
    @TransactionalEventListener(fallbackExecution = true, phase = TransactionPhase.BEFORE_COMMIT)
    public void processSubtreasuryEvent(SubTreasuryOperateEvent event) {
        try {
            String channelCode = event.getChannelCode();
            List<String> subtreasuryCodes = event.getSubtreasuryCodes();
            SubTreasuryOperateEventType eventType = event.getSubTreasuryOperateEventType();
            switch (eventType) {
                case SUB_TREASURY_DELETE:
                    log.info("Perform delete sub-scene event listener processing,tenantId:[{}],subCodes:[]", TenantContextHolder.getTenantId(),
                        subtreasuryCodes.toString());
                    break;
                case SUB_TREASURY_CLEAR:
                    log.info("Perform clearing sub-scene event monitoring processing,tenantId:[{}],subCodes:[]", TenantContextHolder.getTenantId(),
                        subtreasuryCodes.toString());
                    break;
                default:
                    break;
            }
            handleSubEvent(channelCode, subtreasuryCodes);
            event.getCallback().onSuccess();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            event.getCallback().onError(e.getMessage());
        }
    }

    /**
     * 清空子场景事件
     * 
     * @param channelCode 场景编码
     * @param subCodes 子场景编码列表
     */
    private void handleSubEvent(String channelCode, List<String> subCodes) {
        if (CollectionUtils.isNotEmpty(subCodes)) {
            subCodes.stream().forEach(subCode -> {
                deviceInfoMapper.resetPullAllDataFlagBySubCode(subCode);
            });
        } else if (StringUtils.isNotBlank(channelCode)) {
            deviceInfoMapper.resetPullAllDataFlagByChannelCode(channelCode);
        }
    }

}
