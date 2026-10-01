package com.github.pagehelper.dialect.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;

import com.github.pagehelper.Page;
import com.github.pagehelper.dialect.AbstractHelperDialect;
import com.github.pagehelper.util.MetaObjectUtil;

import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.utils.spring.SpringUtils;

public class MySqlDialect extends AbstractHelperDialect {

    /** 执行SQL优化查询的最小数据行数 */
    private static final Integer OPTIMIZE_SQL_MIN_ROWS = 5000;

    @SuppressWarnings("rawtypes")
    @Override
    public Object processPageParameter(MappedStatement ms, Map<String, Object> paramMap, Page page, BoundSql boundSql,
        CacheKey pageKey) {
        String sql = boundSql.getSql();
        if (!isOptimizeSql(page, sql)) {
            paramMap.put(PAGEPARAMETER_FIRST, page.getStartRow());
            paramMap.put(PAGEPARAMETER_SECOND, page.getPageSize());
        }
        // 处理pageKey
        pageKey.update(page.getStartRow());
        pageKey.update(page.getPageSize());
        // 处理参数配置
        if (boundSql.getParameterMappings() != null) {
            List<ParameterMapping> newParameterMappings = new ArrayList<ParameterMapping>();
            if (boundSql != null && boundSql.getParameterMappings() != null) {
                newParameterMappings.addAll(boundSql.getParameterMappings());
            }
            if (!isOptimizeSql(page, sql)) {
                if (page.getStartRow() == 0) {
                    newParameterMappings.add(
                        new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_SECOND, int.class).build());
                } else {
                    // pagehelper1.2.5版本是 int.class， 1.3.0是long.class，否则会类造型异常
                    newParameterMappings.add(
                        new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_FIRST, long.class).build());
                    newParameterMappings.add(
                        new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_SECOND, int.class).build());
                }
            }
            MetaObject metaObject = MetaObjectUtil.forObject(boundSql);
            metaObject.setValue("parameterMappings", newParameterMappings);
        }
        return paramMap;
    }

    /**
     * SQL拼接优化，暂时只能优化单表大数据量分页查询 注意：使用此优化方法，如果SQL语句携带排序，则排序方式必须要写（asc|desc），asc不可以省略，否则SQL拼接会出错
     * 由于系统sys_开头的表和gen_开头的表主键字段不是id，需要特殊梳理一下
     * 
     * 优化方法一（目前SQL拼接采用方案） ： Select id, trans_code, from (Select id as id2,(@rowNum:=@rowNum+1) as rowNo From
     * base_request_record ,(Select (@rowNum :=0) ) b order by received_time desc) r,base_request_record t where r.id2=
     * t.id and r.rowNo> 7520 LIMIT 10;
     * 
     * 优化方法二： SELECT a.id, trans_code FROM base_request_record a JOIN (select id from base_request_record ORDER BY
     * received_time DESC limit 7520, 10 ) b ON a.id = b.id;
     */
    @SuppressWarnings("rawtypes")
    @Override
    public String getPageSql(String sql, Page page, CacheKey pageKey) {
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 14);
        sql = sql.toLowerCase();// 全部转换成小写形式
        // 多个空格合并成一个空格并收尾去空,去掉字符串中的空格、回车符、换行符、制表符
        Pattern p = Pattern.compile("\\t|\r|\n");
        Matcher m = p.matcher(sql);
        sql = m.replaceAll(" ");
        sql = sql.replaceAll(" +", " ");
        if (page.getStartRow() == 0) {
            sqlBuilder.append(sql);
            sqlBuilder.append(" LIMIT ? ");
        } else if (isOptimizeSql(page, sql)) {// 判断是否是大页码并且单表查询
            String[] tables = this.getTableName(sql);
            String sql1 = sql.split(tables[0])[0];
            String idColName = getIdColName(tables[0]);
            sqlBuilder.append(sql1);
            sqlBuilder.append(" (Select " + idColName + " as id2,(@rowNum:=@rowNum+1) as rowNo From ");
            sqlBuilder.append(tables[0]);
            sqlBuilder.append(",(Select (@rowNum :=0) ) b");
            // 注意： 需要在b后边拼接order by语句（源SQL有order by的情况下，这样排序才会正常起所用）.
            // 使用此优化方法，如果SQL语句携带排序，则排序SQL必须要写（asc|desc），asc不可以省略，否则SQL拼接会出错
            if (sql.contains("order by")) {
                int orderByIndex = sql.indexOf(" order by ");
                int ascIndex = sql.lastIndexOf(" asc") + 4;
                int descIndex = sql.lastIndexOf(" desc") + 5;
                String orderByStr = sql.substring(orderByIndex, Math.max(ascIndex, descIndex));
                sqlBuilder.append(orderByStr);
            }
            sqlBuilder.append(" ) r ,");
            sqlBuilder.append(tables[0]);
            sqlBuilder.append(" ");
            sqlBuilder.append(tables[1] != null ? tables[1] : " ");
            sqlBuilder.append(" where r.id2= ");
            sqlBuilder.append(tables[1] != null ? tables[1] : tables[0]);
            sqlBuilder.append("." + idColName + " ");
            sqlBuilder.append(" and r.rowNo> ");
            sqlBuilder.append(page.getStartRow());

            if (sql.contains("where")) {// 拼接原来SQL语句中的where语句后面的语句
                sqlBuilder.append(" and ");
                sqlBuilder.append(sql.split("where")[1]);
            } else {
                // 拼接原有的SQL表名后面的一段后面
                if (tables[1] != null) {// 表有别名
                    String[] sql2 = sql.split(" " + tables[1] + " ");
                    sqlBuilder.append(" ");
                    sqlBuilder.append(sql2.length > 1 ? sql2[1] : " ");
                } else {
                    String[] sql2 = sql.split(tables[0]);
                    sqlBuilder.append(" ");
                    sqlBuilder.append(sql2.length > 1 ? sql2[1] : " ");
                }
            }
            sqlBuilder.append(" LIMIT ");
            sqlBuilder.append(page.getPageSize());
        } else {
            sqlBuilder.append(sql);
            sqlBuilder.append(" LIMIT ?, ? ");
        }
        pageKey.update(page.getPageSize());
        return sqlBuilder.toString();
    }

    // 是否走优化分支（大页码查询而且是单表）
    @SuppressWarnings("rawtypes")
    private boolean isOptimizeSql(Page page, String sql) {
        // TODO 多租户模式下暂时不开启优化分支，防止sqlParser冲突报错，后边考虑优化拦截器顺序(pageHelper和mybatisPlus tenantInteceptor)解决
        TenantProperties tenantProperties = SpringUtils.getBean(TenantProperties.class);
        if (tenantProperties.getEnabled()) {
            return false;
        }
        return page.getStartRow() > OPTIMIZE_SQL_MIN_ROWS && this.inSingletonTable(sql);
    }

    // 是否是单表
    private boolean inSingletonTable(String sql) {
        if (sql.contains("join") || sql.contains("JOIN") || sql.contains("union") || sql.contains("UNION")) {
            return false;
        }

        if (sql.contains("where")) {
            if (sql.contains("from")) {
                String tables = sql.split("from")[1].split("where")[0];
                if (tables.contains(",")) {
                    return false;
                }
            }
        }
        return true;
    }

    // 获取表名
    private String[] getTableName(String sql) {
        String[] tables = new String[2];
        if (sql.contains("where")) {
            String tablenames = sql.split("from")[1].split("where")[0];
            tablenames = this.removekg(tablenames);// 删除表名前后的空格
            if (tablenames.contains(" ")) {
                tables = tablenames.split(" ");
                return tables;
            } else {
                tables[0] = tablenames;
                return tables;
            }
        } else if (sql.contains("group") && !sql.contains("order")) {
            String tablenames = sql.split("from")[1].split("group")[0];
            tablenames = this.removekg(tablenames);// 删除表名前后的空格
            if (tablenames.contains(" ")) {
                tables = tablenames.split(" ");
                return tables;
            } else {
                tables[0] = tablenames;
                return tables;
            }
        } else if (sql.contains("order") && !sql.contains("group")) {
            String tablenames = sql.split("from")[1].split("order")[0];
            tablenames = this.removekg(tablenames);// 删除表名前后的空格
            if (tablenames.contains(" ")) {
                tables = tablenames.split(" ");
                return tables;
            } else {
                tables[0] = tablenames;
                return tables;
            }
        } else if (sql.contains("order") && sql.contains("group")) {
            int orderIndex = sql.indexOf("order");
            int groupIndex = sql.indexOf("group");
            if (orderIndex < groupIndex) {
                String tablenames = sql.split("from")[1].split("order")[0];
                tablenames = this.removekg(tablenames);// 删除表名前后的空格
                if (tablenames.contains(" ")) {
                    tables = tablenames.split(" ");
                    return tables;
                } else {
                    tables[0] = tablenames;
                    return tables;
                }
            } else {
                String tablenames = sql.split("from")[1].split("group")[0];
                tablenames = this.removekg(tablenames);// 删除表名前后的空格
                if (tablenames.contains(" ")) {
                    tables = tablenames.split(" ");
                    return tables;
                } else {
                    tables[0] = tablenames;
                    return tables;
                }
            }
        } else if (!sql.contains("where") && !sql.contains("order") && !sql.contains("group")) {
            String tablenames = sql.split("from")[1];
            tablenames = this.removekg(tablenames);// 删除表名前后的空格
            if (tablenames.contains(" ")) {
                tables = tablenames.split(" ");
                return tables;
            } else {
                tables[0] = tablenames;
                return tables;
            }
        }
        return tables;
    }

    // 删除字符串两头的空格
    private String removekg(String textContent) {
        textContent = textContent.trim();
        while (textContent.startsWith(" ")) {// 这里判断是不是全角空格
            textContent = textContent.substring(1, textContent.length()).trim();
        }
        while (textContent.endsWith("　")) {
            textContent = textContent.substring(0, textContent.length() - 1).trim();
        }
        return textContent;
    }

    // 特殊处理sys_和qrtz_开头的表主键列名
    private String getIdColName(String tableName) {
        switch (tableName) {
            case "sys_config":
                return "config_id";
            case "sys_dept":
                return "dept_id";
            case "sys_dict_data":
                return "dict_code";
            case "sys_dict_type":
                return "dict_id";
            case "sys_job":
                return "job_id";
            case "sys_job_log":
                return "job_log_id";
            case "sys_logininfor":
                return "info_id";
            case "sys_menu":
                return "menu_id";
            case "sys_notice":
                return "notice_id";
            case "sys_post":
                return "post_id";
            case "sys_role":
                return "role_id";
            case "sys_oper_log":
                return "oper_id";
            case "sys_user":
                return "user_id";
            case "sys_user_online":
                return "sessionId";
            case "gen_table":
                return "table_id";
            case "gen_table_column":
                return "column_id";
            case "sys_client_details":
                return "client_id";
            default:
                return "id";
        }
    }

}