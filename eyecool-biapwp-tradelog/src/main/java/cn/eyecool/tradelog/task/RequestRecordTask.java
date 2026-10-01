package cn.eyecool.tradelog.task;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cn.eyecool.common.constant.DatabaseTypeConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.file.FileUtils;
import cn.eyecool.tradelog.constant.TradelogConstants;
import cn.eyecool.tradelog.mapper.TradeReqRecordMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * 清空接口报文分区定时任务
 * 
 * @author admin
 * @date 2020年4月10日
 */
@Component("requestRecordTask")
@Slf4j
public class RequestRecordTask {

    @Autowired
    private TradeReqRecordMapper tradeReqRecordMapper;
    @Value("${mybatis-plus.configuration.database-id}")
    private String databaseId;

    /**
     * 清除接口交易报文并维护分区
     * 
     * @param offsetDay
     * @param schemaName 数据库名称
     */
    public void clearAndAddPartition(String schemaName, Integer offsetDay) {
        TenantContextHolder.clear();
        log.info("clearAndAddReqRecordPartition start[{}] offsetDay[{}]", DateUtils.getTime(), offsetDay);
        if (offsetDay < 3) {
            log.warn("The retention days [{}] setting is unreasonable. At least three days of log information will be retained, and the default execution will be retained for 3 days.", offsetDay);
            offsetDay = 3;
        }
        Date date = DateUtils.addDays(new Date(), -offsetDay);
        String tableName = "trade_req_record";
        if (DatabaseTypeConstants.DB_MYSQL.equals(databaseId) || DatabaseTypeConstants.DB_ORACLE.equals(databaseId)) {
            // 创建新分区（连续创建30天）
            for (int i = -2; i < 30; i++) {
                tradeReqRecordMapper.addPartition(schemaName, tableName, i);
            }
        } else if (DatabaseTypeConstants.DB_PG.equals(databaseId)) {
            tableName = tableName + "_" + DateUtils.parseDateToStr("yyyyMMdd", DateUtils.addMonths(date, -1));
        }
        // 删除分区
        tradeReqRecordMapper.dropPartition(schemaName, tableName, date);
        log.info("clearAndAddReqRecordPartition end[{}] offsetDay[{}]", DateUtils.getTime(), offsetDay);
        // 删除报文详情文件
        CompletableFuture.runAsync(() -> {
            File dirFile = new File(TradelogConstants.TRADE_REQ_RECORD_FILE_BASEDIR);
            if (!dirFile.exists() || !dirFile.isDirectory()) {
                return;
            }
            File[] subDirFiles = dirFile.listFiles();
            for (File subDirFile : subDirFiles) {
                if (date.compareTo(DateUtils.dateTime("yyyyMMdd", subDirFile.getName())) > 0) {
                    try {
                        FileUtils.deleteDirectory(subDirFile);
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        });
    }
}
