package cn.eyecool.tradelog.task;

import java.io.File;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cn.eyecool.common.constant.DatabaseTypeConstants;
import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.system.service.ISysConfigService;
import cn.eyecool.tradelog.mapper.PersonFaceCheckliveLogMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * 定时维护检活日志
 * 
 * @author mawj
 * @date 2021/09/02
 */
@Component("checkliveLogTask")
@Slf4j
public class CheckliveLogTask {

    @Value("${mybatis-plus.configuration.database-id}")
    private String databaseId;
    @Autowired
    private ISysConfigService configService;
    @Autowired
    private PersonFaceCheckliveLogMapper personFaceCheckliveLogMapper;

    /**
     * 删除检活日志并维护分区
     * 
     * @param offsetDay 保留天数
     * @param onlyClearImg 只删除图片
     * @param schemaName 数据库名称
     */
    public void clearAndAddPartition(Integer offsetDay, Boolean onlyClearImg, String schemaName) {
        TenantContextHolder.clear();
        log.info("clearCheckliveLog start[{}] offsetDay[{}]", DateUtils.getTime(), offsetDay);
        if (offsetDay < 3) {
            log.warn("The retention days [{}] setting is unreasonable. At least three days of log information will be retained, and the default execution will be retained for 3 days.", offsetDay);
            offsetDay = 3;
        }
        Date date = DateUtils.addDays(new Date(), -offsetDay);
        String tableName = "person_face_checklive_log";
        if (DatabaseTypeConstants.DB_MYSQL.equals(databaseId) || DatabaseTypeConstants.DB_ORACLE.equals(databaseId)) {
            // 创建新分区（连续创建30天）
            for (int i = -2; i < 30; i++) {
                personFaceCheckliveLogMapper.addPartition(schemaName, tableName, i);
            }
        } else if (DatabaseTypeConstants.DB_PG.equals(databaseId)) {
            tableName = tableName + "_" + DateUtils.parseDateToStr("yyyyMMdd", DateUtils.addMonths(date, -1));
        }
        if (!onlyClearImg) {
            // 删除分区
            personFaceCheckliveLogMapper.dropPartition(schemaName, tableName, date);
        }
        clearLogImg(date, SysConfigConstants.BUSI_CHECKLIVE_DIR_KEY, null);
        log.info("clearCheckliveLog end[{}] offsetDay[{}]", DateUtils.getTime(), offsetDay);
    }

    /**
     * 删除日志图片
     * 
     * @param date
     * @param configKey
     * @param subDir
     */
    private void clearLogImg(Date date, String configKey, String subDir) {
        String dateStr = DateFormatUtils.format(date, "yyyyMMdd");
        String baseDir = configService.selectConfigByKey(configKey);
        if (!baseDir.endsWith(File.separator)) {
            baseDir = baseDir + File.separator;
        }
        if (StringUtils.isNotBlank(subDir)) {
            baseDir = baseDir + subDir + File.separator;
        }
        if (!baseDir.startsWith(File.separator) && !baseDir.startsWith(":/", 1)) {
            baseDir = System.getProperty("user.dir") + File.separator + baseDir;
        }
        File file = new File(baseDir);
        if (file.isDirectory()) {
            File[] listFiles = file.listFiles(item -> {
                return item.getName().compareTo(dateStr) < 0;
            });
            /** 删除文件夹 */
            if (null == listFiles) {
                return;
            }
            Arrays.stream(listFiles).parallel().forEach(it -> {
                PlatformFileUtils.deleteDir(it.getAbsolutePath());
            });
        }
    }
}
