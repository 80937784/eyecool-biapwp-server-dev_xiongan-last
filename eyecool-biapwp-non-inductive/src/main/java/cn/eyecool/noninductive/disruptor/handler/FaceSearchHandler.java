package cn.eyecool.noninductive.disruptor.handler;

import java.util.List;

import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.device.domain.DeviceInfo;
import cn.eyecool.device.service.IDeviceInfoService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.lmax.disruptor.EventHandler;

import cn.eyecool.noninductive.disruptor.event.FaceSearchEvent;
import cn.eyecool.noninductive.service.IBioFaceService;

@Component
public class FaceSearchHandler implements EventHandler<FaceSearchEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FaceSearchHandler.class);
    @Autowired
    private IBioFaceService bioFaceService;
    @Autowired
    private IDeviceInfoService deviceInfoService;

    @Autowired
    private TenantProperties tenantProperties;

    @Override
    public void onEvent(FaceSearchEvent event, long sequence, boolean endOfBatch) throws Exception {
        FaceSearchEvent.FaceSearchMessage message = event.getResult();
        try {
            String deviceSerialNo = message.getDeviceSerialNo();
            DeviceInfo condition = new DeviceInfo();
            condition.setDeviceNo(deviceSerialNo);
            // condition.setChannelCode(dfrsChannelCode);
            List<DeviceInfo> clientDeviceInfos = deviceInfoService.selectDeviceInfoList(condition);
            String tenantId;
            if (CollectionUtils.isEmpty(clientDeviceInfos)) {
                LOGGER.error("serialNo [{}] is not register in clientDevice", deviceSerialNo);
            } else {
                if (Boolean.TRUE.equals(tenantProperties.getEnabled())) {
                    if (StringUtils.isBlank(clientDeviceInfos.get(0).getTenantId())) {
                        LOGGER.error("serialNo [{}] tenantId is null", deviceSerialNo);
                        return;
                    } else {
                        tenantId = clientDeviceInfos.get(0).getTenantId();
                        TenantContextHolder.setTenantId(tenantId);
                    }
                }
                bioFaceService.search(message.getImageContent(), message.getDeviceSerialNo(), message.getComment());
            }
        } catch (Exception e) {
            throw new CustomException(e.getMessage(), e);
        } finally {
            TenantContextHolder.clear();
        }
    }

}
