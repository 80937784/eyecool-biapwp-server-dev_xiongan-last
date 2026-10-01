package cn.eyecool.noninductive.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import cn.eyecool.tradelog.domain.PersonFaceSearchLog;

public interface NonInductivePersonFaceSearchLogMapper {
       public List<PersonFaceSearchLog> selectLogByDeviceIdLimit(@Param("deviceCode") String deviceCode,
                     @Param("result") String result, @Param("alarmTem") String alarmTem,
                     @Param("channelCodes") String[] channelCodes);

       public List<PersonFaceSearchLog> selectLogsByTime(@Param("createTime") Date createTime,
                     @Param("channelCode") String channelCode);
}
