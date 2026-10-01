package cn.eyecool.noninductive.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import cn.eyecool.basedata.service.IBasePersonInfoService;
import cn.eyecool.common.core.text.Convert;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.eyecool.noninductive.mapper.NonInductivePersonFaceSearchLogMapper;
import cn.eyecool.noninductive.service.INonInductivePersonFaceSearchLogService;
import cn.eyecool.tradelog.domain.PersonFaceSearchLog;
import cn.eyecool.tradelog.service.impl.PersonFaceSearchLogServiceImpl;

@Service("nonInductivePersonFaceSearchLogService")
public class NonInductivePersonFaceSearchLogServiceImpl extends PersonFaceSearchLogServiceImpl
        implements INonInductivePersonFaceSearchLogService {
    @Autowired
    private NonInductivePersonFaceSearchLogMapper nonInductivePersonFaceSearchLogMapper;
    @Autowired
    private IBasePersonInfoService basePersonInfoService;

    @Override
    public List<PersonFaceSearchLog> selectLogByDeviceIdLimit(String deviceCode, String result, String alarmTem,
            String channelCodes) {
        String[] channelCodeArray = null;
        if (StringUtils.isNotBlank(channelCodes)) {
            channelCodeArray = Convert.toStrArray(channelCodes);
        }
        List<PersonFaceSearchLog> personFaceSearchLogs = nonInductivePersonFaceSearchLogMapper
                .selectLogByDeviceIdLimit(deviceCode, result, alarmTem, channelCodeArray);
        Optional.ofNullable(personFaceSearchLogs).ifPresent(list -> {
            list.forEach(item -> {
                String stringBase64 = PlatformFileUtils.getImageBase64(item.getSceneImage());
                if (StringUtils.isNotBlank(stringBase64)) {
                    stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                    item.setSceneImage(stringBase64);
                }
                if (StringUtils.isNotEmpty(item.getUniqueId())) {
                    Optional.ofNullable(basePersonInfoService.selectBasePersonInfoById(item.getUniqueId()))
                            .ifPresent(info -> item.setUniqueId(info.getName()));
                }
            });
        });
        return personFaceSearchLogs;
    }

    @Override
    public List<PersonFaceSearchLog> selectLogsByTime(Date createTime, String channelCode) {
        List<PersonFaceSearchLog> personFaceSearchLogs = nonInductivePersonFaceSearchLogMapper
                .selectLogsByTime(createTime, channelCode);
        Optional.ofNullable(personFaceSearchLogs).ifPresent(list -> 
            list.forEach(item -> {
                String stringBase64 = PlatformFileUtils.getImageBase64(item.getSceneImage());
                if (StringUtils.isNotBlank(stringBase64)) {
                    stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                    item.setSceneImage(stringBase64);
                }
                if (StringUtils.isNotEmpty(item.getUniqueId())) {
                    item.setUniqueId(
                            basePersonInfoService.selectBasePersonInfoById(item.getUniqueId()).getName());
                }
            })
        );
        return personFaceSearchLogs;
    }
}
