package cn.eyecool.noninductive.service;

import java.util.List;

import cn.eyecool.tradelog.domain.PersonFaceSearchLog;
import cn.eyecool.tradelog.service.IPersonFaceSearchLogService;

import java.util.Date;

public interface INonInductivePersonFaceSearchLogService extends IPersonFaceSearchLogService {

        public List<PersonFaceSearchLog> selectLogByDeviceIdLimit(String deviceCode, String result, String alarmTem,
                        String channelCode);

        public List<PersonFaceSearchLog> selectLogsByTime(Date createTime, String channelCode);

}
