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
import cn.eyecool.tradelog.mapper.PersonFaceMatchLogMapper;
import cn.eyecool.tradelog.mapper.PersonFaceSearchLogMapper;
import cn.eyecool.tradelog.mapper.PersonFaceirisMatchLogMapper;
import cn.eyecool.tradelog.mapper.PersonFaceirisSearchLogMapper;
import cn.eyecool.tradelog.mapper.PersonFingerMatchLogMapper;
import cn.eyecool.tradelog.mapper.PersonFingerSearchLogMapper;
import cn.eyecool.tradelog.mapper.PersonHealthCodeLogMapper;
import cn.eyecool.tradelog.mapper.PersonIrisMatchLogMapper;
import cn.eyecool.tradelog.mapper.PersonIrisSearchLogMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * 删除比对日志
 * 
 * @author admin
 * @date 2020年4月14日
 */
@Component("clearBioMatchLogTask")
@Slf4j
public class ClearBioMatchLogTask {

    @Autowired
    private ISysConfigService configService;
    @Autowired
    private PersonFaceMatchLogMapper personFaceMatchLogMapper;
    @Autowired
    private PersonFaceSearchLogMapper personFaceSearchLogMapper;
    @Autowired
    private PersonFingerMatchLogMapper personFingerMatchLogMapper;
    @Autowired
    private PersonFingerSearchLogMapper personFingerSearchLogMapper;
    @Autowired
    private PersonIrisMatchLogMapper personIrisMatchLogMapper;
    @Autowired
    private PersonIrisSearchLogMapper personIrisSearchLogMapper;
    @Autowired
    private PersonFaceirisSearchLogMapper personFaceirisSearchLogMapper;
    @Autowired
    private PersonFaceirisMatchLogMapper personFaceirisMatchLogMapper;
    @Autowired
    private PersonHealthCodeLogMapper personHealthCodeLogMapper;
    @Value("${mybatis-plus.configuration.database-id}")
    private String databaseId;

    /**
     * 清除人脸1:1日志并维护分区
     * 
     * @param offsetDay 保留天数
     * @param onlyClearImg 只删除图片
     * @param schemaName 数据库名称
     */
    public void clearFaceMatchLog(Integer offsetDay, Boolean onlyClearImg, String schemaName) {
        TenantContextHolder.clear();
        log.info("clearFaceMatchLog start[{}] offsetDay[{}]", DateUtils.getTime(), offsetDay);
        if (offsetDay < 3) {
            log.warn("The retention days [{}] setting is unreasonable. At least three days of log information will be retained, and the default execution will be retained for 3 days.", offsetDay);
            offsetDay = 3;
        }
        Date date = DateUtils.addDays(new Date(), -offsetDay);
        String tableName = "person_face_match_log";
        if (DatabaseTypeConstants.DB_MYSQL.equals(databaseId) || DatabaseTypeConstants.DB_ORACLE.equals(databaseId)) {
            // 创建新分区（连续创建30天）
            for (int i = -2; i < 30; i++) {
                personFaceMatchLogMapper.addPartition(schemaName, tableName, i);
            }
        } else if (DatabaseTypeConstants.DB_PG.equals(databaseId)) {
            tableName = tableName + "_" + DateUtils.parseDateToStr("yyyyMMdd", DateUtils.addMonths(date, -1));
        }
        if (!onlyClearImg) {
            // 删除分区
            personFaceMatchLogMapper.dropPartition(schemaName, tableName, date);
        }
        clearLogImg(date, SysConfigConstants.BUSI_FACE_COMPARE_DIR_KEY, null);
        log.info("clearFaceMatchLog end[{}] offsetDay[{}]", DateUtils.getTime(), offsetDay);
    }

    /**
     * 清除人脸1:N日志并维护分区
     * 
     * @param offsetDay
     * @param onlyClearImg
     * @param schemaName 数据库名称
     */
    public void clearFaceSearchLog(Integer offsetDay, Boolean onlyClearImg, String schemaName) {
        TenantContextHolder.clear();
        log.info("clearFaceSearchLog start[{}] offsetDay[{}]", DateUtils.getTime(), offsetDay);
        if (offsetDay < 3) {
            log.warn("The retention days [{}] setting is unreasonable. At least three days of log information will be retained, and the default execution will be retained for 3 days.", offsetDay);
            offsetDay = 3;
        }
        Date date = DateUtils.addDays(new Date(), -offsetDay);
        String tableName = "person_face_search_log";
        if (DatabaseTypeConstants.DB_MYSQL.equals(databaseId) || DatabaseTypeConstants.DB_ORACLE.equals(databaseId)) {
            // 创建新分区（连续创建30天）
            for (int i = -2; i < 30; i++) {
                personFaceSearchLogMapper.addPartition(schemaName, tableName, i);
            }
        } else if (DatabaseTypeConstants.DB_PG.equals(databaseId)) {
            tableName = tableName + "_" + DateUtils.parseDateToStr("yyyyMMdd", DateUtils.addDays(date, -1));
        }
        if (!onlyClearImg) {
            // 删除分区
            personFaceSearchLogMapper.dropPartition(schemaName, tableName, date);
        }
        clearLogImg(date, SysConfigConstants.BUSI_FACE_SEARCH_DIR_KEY, null);
        log.info("clearFaceSearchLog end[{}] offsetDay[{}]", DateUtils.getTime(), offsetDay);
    }

    /**
     * 清除指纹1:1日志并维护分区
     * 
     * @param offsetDay
     * @param onlyClearImg
     * @param schemaName 数据库名称
     */
    public void clearFingerMatchLog(Integer offsetDay, Boolean onlyClearImg, String schemaName) {
        TenantContextHolder.clear();
        log.info("clearFingerMatchLog start[{}] offsetDay[{}]", DateUtils.getTime(), offsetDay);
        if (offsetDay < 3) {
            log.warn("The retention days [{}] setting is unreasonable. At least three days of log information will be retained, and the default execution will be retained for 3 days.", offsetDay);
            offsetDay = 3;
        }
        Date date = DateUtils.addDays(new Date(), -offsetDay);
        String tableName = "person_finger_match_log";
        if (DatabaseTypeConstants.DB_MYSQL.equals(databaseId) || DatabaseTypeConstants.DB_ORACLE.equals(databaseId)) {
            // 创建新分区（连续创建30天）
            for (int i = -2; i < 30; i++) {
                personFingerMatchLogMapper.addPartition(schemaName, tableName, i);
            }
        } else if (DatabaseTypeConstants.DB_PG.equals(databaseId)) {
            tableName = tableName + "_" + DateUtils.parseDateToStr("yyyyMMdd", DateUtils.addDays(date, -1));
        }
        if (!onlyClearImg) {
            // 删除分区
            personFingerMatchLogMapper.dropPartition(schemaName, tableName, date);
        }
        clearLogImg(date, SysConfigConstants.BUSI_FINGER_COMPARE_DIR_KEY, null);
        log.info("clearFingerMatchLog end[{}] offsetDay[{}]", DateUtils.getTime(), offsetDay);
    }

    /**
     * 清除指纹1:N日志并维护分区
     * 
     * @param offsetDay
     * @param onlyClearImg
     * @param schemaName 数据库名称
     */
    public void clearFingerSearchLog(Integer offsetDay, Boolean onlyClearImg, String schemaName) {
        TenantContextHolder.clear();
        log.info("clearFingerSearchLog start[{}] offsetDay[{}]", DateUtils.getTime(), offsetDay);
        if (offsetDay < 3) {
            log.warn("The retention days [{}] setting is unreasonable. At least three days of log information will be retained, and the default execution will be retained for 3 days.", offsetDay);
            offsetDay = 3;
        }
        Date date = DateUtils.addDays(new Date(), -offsetDay);
        String tableName = "person_finger_search_log";
        if (DatabaseTypeConstants.DB_MYSQL.equals(databaseId) || DatabaseTypeConstants.DB_ORACLE.equals(databaseId)) {
            // 创建新分区（连续创建30天）
            for (int i = -2; i < 30; i++) {
                personFingerSearchLogMapper.addPartition(schemaName, tableName, i);
            }
        } else if (DatabaseTypeConstants.DB_PG.equals(databaseId)) {
            tableName = tableName + "_" + DateUtils.parseDateToStr("yyyyMMdd", DateUtils.addMonths(date, -1));
        }
        if (!onlyClearImg) {
            // 删除分区
            personFingerSearchLogMapper.dropPartition(schemaName, tableName, date);
        }
        clearLogImg(date, SysConfigConstants.BUSI_FINGER_SEARCH_DIR_KEY, null);
        log.info("clearFingerSearchLog end[{}] offsetDay[{}]", DateUtils.getTime(), offsetDay);
    }

    /**
     * 清除虹膜1:1日志并维护分区
     * 
     * @param offsetDay
     * @param onlyClearImg
     * @param schemaName 数据库名称
     */
    public void clearIrisMatchLog(Integer offsetDay, Boolean onlyClearImg, String schemaName) {
        TenantContextHolder.clear();
        log.info("clearIrisMatchLog start[{}] offsetDay[{}]", DateUtils.getTime(), offsetDay);
        if (offsetDay < 3) {
            log.warn("The retention days [{}] setting is unreasonable. At least three days of log information will be retained, and the default execution will be retained for 3 days.", offsetDay);
            offsetDay = 3;
        }
        Date date = DateUtils.addDays(new Date(), -offsetDay);
        String tableName = "person_iris_match_log";
        if (DatabaseTypeConstants.DB_MYSQL.equals(databaseId) || DatabaseTypeConstants.DB_ORACLE.equals(databaseId)) {
            // 创建新分区（连续创建30天）
            for (int i = -2; i < 30; i++) {
                personIrisMatchLogMapper.addPartition(schemaName, tableName, i);
            }
        } else if (DatabaseTypeConstants.DB_PG.equals(databaseId)) {
            tableName = tableName + "_" + DateUtils.parseDateToStr("yyyyMMdd", DateUtils.addMonths(date, -1));
        }
        if (!onlyClearImg) {
            // 删除分区
            personIrisMatchLogMapper.dropPartition(schemaName, tableName, date);
        }
        clearLogImg(date, SysConfigConstants.BUSI_IRIS_COMPARE_DIR_KEY, null);
        log.info("clearIrisMatchLog end[{}] offsetDay[{}]", DateUtils.getTime(), offsetDay);
    }

    /**
     * 清除虹膜1:N日志并维护分区
     * 
     * @param offsetDay
     * @param onlyClearImg
     * @param schemaName 数据库名称
     */
    public void clearIrisSearchLog(Integer offsetDay, Boolean onlyClearImg, String schemaName) {
        TenantContextHolder.clear();
        log.info("clearIrisSearchLog start[{}] offsetDay[{}]", DateUtils.getTime(), offsetDay);
        if (offsetDay < 3) {
            log.warn("The retention days [{}] setting is unreasonable. At least three days of log information will be retained, and the default execution will be retained for 3 days.", offsetDay);
            offsetDay = 3;
        }
        Date date = DateUtils.addDays(new Date(), -offsetDay);
        String tableName = "person_iris_search_log";
        if (DatabaseTypeConstants.DB_MYSQL.equals(databaseId) || DatabaseTypeConstants.DB_ORACLE.equals(databaseId)) {
            // 创建新分区（连续创建30天）
            for (int i = -2; i < 30; i++) {
                personIrisSearchLogMapper.addPartition(schemaName, tableName, i);
            }
        } else if (DatabaseTypeConstants.DB_PG.equals(databaseId)) {
            tableName = tableName + "_" + DateUtils.parseDateToStr("yyyyMMdd", DateUtils.addMonths(date, -1));
        }
        if (!onlyClearImg) {
            // 删除分区
            personIrisSearchLogMapper.dropPartition(schemaName, tableName, date);
        }
        clearLogImg(date, SysConfigConstants.BUSI_IRIS_SEARCH_DIR_KEY, null);
        log.info("clearIrisSearchLog end[{}] offsetDay[{}]", DateUtils.getTime(), offsetDay);
    }

    /**
     * 清除人脸虹膜多模态1:N日志并维护分区
     * 
     * @param offsetDay
     * @param onlyClearImg
     * @param schemaName 数据库名称
     */
    public void clearMulitSearchLog(Integer offsetDay, Boolean onlyClearImg, String schemaName) {
        TenantContextHolder.clear();
        log.info("clearMulitSearchLog start[{}] offsetDay[{}]", DateUtils.getTime(), offsetDay);
        if (offsetDay < 3) {
            log.warn("The retention days [{}] setting is unreasonable. At least three days of log information will be retained, and the default execution will be retained for 3 days.", offsetDay);
            offsetDay = 3;
        }
        Date date = DateUtils.addDays(new Date(), -offsetDay);
        String tableName = "person_faceiris_search_log";
        if (DatabaseTypeConstants.DB_MYSQL.equals(databaseId) || DatabaseTypeConstants.DB_ORACLE.equals(databaseId)) {
            // 创建新分区（连续创建30天）
            for (int i = -2; i < 30; i++) {
                personFaceirisSearchLogMapper.addPartition(schemaName, tableName, i);
            }
        } else if (DatabaseTypeConstants.DB_PG.equals(databaseId)) {
            tableName = tableName + "_" + DateUtils.parseDateToStr("yyyyMMdd", DateUtils.addMonths(date, -1));
        }
        if (!onlyClearImg) {
            // 删除分区
            personFaceirisSearchLogMapper.dropPartition(schemaName, tableName, date);
        }
        clearLogImg(date, SysConfigConstants.BUSI_MULIT_SEARCH_DIR_KEY, "face");
        clearLogImg(date, SysConfigConstants.BUSI_MULIT_SEARCH_DIR_KEY, "iris");
        log.info("clearMulitSearchLog end[{}] offsetDay[{}]", DateUtils.getTime(), offsetDay);
    }

    /**
     * 清除人脸虹膜多模态1:1日志并维护分区
     * 
     * @param offsetDay
     * @param onlyClearImg
     * @param schemaName 数据库名称
     */
    public void clearMulitMatchLog(Integer offsetDay, Boolean onlyClearImg, String schemaName) {
        TenantContextHolder.clear();
        log.info("clearMulitMatchLog start[{}] offsetDay[{}]", DateUtils.getTime(), offsetDay);
        if (offsetDay < 3) {
            log.warn("The retention days [{}] setting is unreasonable. At least three days of log information will be retained, and the default execution will be retained for 3 days.", offsetDay);
            offsetDay = 3;
        }
        Date date = DateUtils.addDays(new Date(), -offsetDay);
        String tableName = "person_faceiris_match_log";
        if (DatabaseTypeConstants.DB_MYSQL.equals(databaseId) || DatabaseTypeConstants.DB_ORACLE.equals(databaseId)) {
            // 创建新分区（连续创建30天）
            for (int i = -2; i < 30; i++) {
                personFaceirisMatchLogMapper.addPartition(schemaName, tableName, i);
            }
        } else if (DatabaseTypeConstants.DB_PG.equals(databaseId)) {
            tableName = tableName + "_" + DateUtils.parseDateToStr("yyyyMMdd", DateUtils.addMonths(date, -1));
        }
        if (!onlyClearImg) {
            // 删除分区
            personFaceirisMatchLogMapper.dropPartition(schemaName, tableName, date);
        }
        clearLogImg(date, SysConfigConstants.BUSI_MULIT_COMPARE_DIR_KEY, "face");
        clearLogImg(date, SysConfigConstants.BUSI_MULIT_COMPARE_DIR_KEY, "iris");
        log.info("clearMulitMatchLog end[{}] offsetDay[{}]", DateUtils.getTime(), offsetDay);
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

    /**
     * 清除健康码请求日志并维护分区
     * 
     * @param offsetDay
     * @param schemaName 数据库名称
     */
    public void clearHealthcodeLog(Integer offsetDay, String schemaName) {
        TenantContextHolder.clear();
        log.info("clearHealthcodeLog start[{}] offsetDay[{}]", DateUtils.getTime(), offsetDay);
        if (offsetDay < 3) {
            log.warn("The retention days [{}] setting is unreasonable. At least three days of log information will be retained, and the default execution will be retained for 3 days.", offsetDay);
            offsetDay = 3;
        }
        Date date = DateUtils.addDays(new Date(), -offsetDay);
        String tableName = "person_health_code_log";
        if (DatabaseTypeConstants.DB_MYSQL.equals(databaseId) || DatabaseTypeConstants.DB_ORACLE.equals(databaseId)) {
            // 创建新分区（连续创建30天）
            for (int i = -2; i < 30; i++) {
                personHealthCodeLogMapper.addPartition(schemaName, tableName, i);
            }
        } else if (DatabaseTypeConstants.DB_PG.equals(databaseId)) {
            tableName = tableName + "_" + DateUtils.parseDateToStr("yyyyMMdd", DateUtils.addMonths(date, -1));
        }
        // 删除分区
        personHealthCodeLogMapper.dropPartition(schemaName, tableName, date);
        log.info("clearHealthcodeLog end[{}] offsetDay[{}]", DateUtils.getTime(), offsetDay);
    }

}
