package com.ct08.PharmacyManagement.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static net.logstash.logback.argument.StructuredArguments.v;

@Component
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 10) // Chạy sau cái trên để đảm bảo MDC đã có dữ liệu
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Wrap request và response để có thể đọc được body nhiều lần
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request,1024 * 10);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            // 2. Cho request đi qua các filter khác và vào Controller xử lý
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            // 3. Tính toán thời gian xử lý
            long timeTaken = System.currentTimeMillis() - startTime;
            int status = responseWrapper.getStatus();
            // 4. Lấy body (Lưu ý: Chỉ lấy khi cần thiết để tránh nặng log)
            String requestBody = getStringValue(requestWrapper.getContentAsByteArray(), request.getCharacterEncoding());
            String responseBody = getStringValue(responseWrapper.getContentAsByteArray(), response.getCharacterEncoding());


            Object[] logArgs = {
                    v("method", request.getMethod()),
                    v("uri", request.getRequestURI()),
                    v("status", status),
                    v("duration", timeTaken)};

            if (status >= 500) {
                log.error("Request failed with System Error", logArgs);
            } else if (status >= 400) {
                log.warn("Request failed with Client Error", logArgs);
            } else {
                log.info("Finished Request", logArgs);
            }

            // QUAN TRỌNG: Copy lại nội dung response để trả về cho client
            // Nếu quên dòng này, client (Postman/Frontend) sẽ nhận được trang trắng.
            responseWrapper.copyBodyToResponse();
        }
    }

    private String getStringValue(byte[] contentAsByteArray, String characterEncoding) {
        try {
            return new String(contentAsByteArray, 0, contentAsByteArray.length, characterEncoding != null ? characterEncoding : "UTF-8");
        } catch (Exception e) {
            return "[ERROR READING BODY]";
        }
    }


}