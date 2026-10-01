package cn.eyecool.msg.task;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cn.eyecool.common.constant.DatabaseTypeConstants;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.msg.mapper.MsgLogMapper;

/**
 * 清空接口报文分区定时任务
 * 
 * @author admin
 * @date 2020年4月10日
 */
@Component("msgLogTask")
public class MsgLogTask {

    private static final Logger LOG = LoggerFactory.getLogger(MsgLogTask.class);

    @Autowired
    private MsgLogMapper msgLogMapper;
    @Value("${mybatis-plus.configuration.database-id}")
    private String databaseId;

    /**
     * 清除接口交易报文并维护分区
     * 
     * @param offsetDay
     * @param schemaName 数据库名称
     */
    public void clearAndAddPartition(String schemaName, Integer offsetDay) {
        LOG.info("clearAndAddMsgLogPartition start[{}] offsetDay[{}]", DateUtils.getTime(), offsetDay);
        if (offsetDay < 3) {
            LOG.warn("The retention days [{}] setting is unreasonable, at least three days of message logs are retained, and the default execution is retained for 3 days", offsetDay);
            offsetDay = 3;
        }
        Date date = DateUtils.addDays(new Date(), -offsetDay);
        String tableName = "msg_log";
        if (DatabaseTypeConstants.DB_MYSQL.equals(databaseId) || DatabaseTypeConstants.DB_ORACLE.equals(databaseId)) {
            // 创建新分区（连续创建30天）
            for (int i = -2; i < 30; i++) {
                msgLogMapper.addPartition(schemaName, tableName, i);
            }
        } else if (DatabaseTypeConstants.DB_PG.equals(databaseId)) {
            tableName = tableName + "_" + DateUtils.parseDateToStr("yyyyMMdd", DateUtils.addMonths(date, -1));
        }
        // 删除分区
        msgLogMapper.dropPartition(schemaName, tableName, date);
        LOG.info("clearAndAddMsgLogPartition end[{}] offsetDay[{}]", DateUtils.getTime(), offsetDay);
    }

}
