package cn.eyecool.statistic.constant;

/**
 * 统计信息常量
 * 
 * @author mawj
 * @date 2021/08/31
 */
public interface StatisticConstants {

    /** 统计单位 */
    interface StatisticUnit {
        public static final String DAY = "day"; // 日
        public static final String MONTH = "month";// 月
        public static final String YEAR = "year";// 年
    }

    /** 大屏统计登录token */
    public static final String BIGSCREEN_LOGIN_TOKEN_CACHE_PREFIX = "bigscreen-token";
}
