package com.ct08.PharmacyManagement.common.config;

import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.appender.Slf4JLogger;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class P6SpyLogger extends Slf4JLogger {
    private static final Logger log = LoggerFactory.getLogger("p6spy");

    @Override
    public void logSQL(int connectionId, String now, long elapsed, Category category, String prepared, String sql, String url) {
        // Nếu không có SQL (ví dụ log kết nối) thì bỏ qua hoặc log mặc định
        if (sql == null || sql.isEmpty()) {
            return;
        }

        // Ghi log với các trường JSON riêng biệt
        log.info("SQL Execution",
                StructuredArguments.kv("TYPE_LOG", "DATABASE_QUERY"), // Sẽ ghi đè giá trị trong XML
                StructuredArguments.kv("duration", elapsed),      // Trường duration số
                StructuredArguments.kv("category", category),      // Trường category (statement, commit...)
                StructuredArguments.kv("sql_query", sql.replaceAll("\\s+", " ")), // Format lại SQL trên 1 dòng
                StructuredArguments.kv("conn_id", connectionId)    // ID kết nối DB
        );    }
}
