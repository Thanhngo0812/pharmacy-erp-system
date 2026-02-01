package com.ct08.PharmacyManagement.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1) // Chạy rất sớm để nạp dữ liệu
public class CustomTracingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String userId = request.getHeader("X-User-Id");
            MDC.put("userId", userId != null ? userId : "anonymous");

            // 2. Xử lý TraceId (Định danh toàn bộ hành trình)
            if (MDC.get("traceId") == null) {
                MDC.put("traceId", UUID.randomUUID().toString().replace("-", "").substring(0, 16));
            }

            // 3. Xử lý SpanId (Định danh chặng xử lý hiện tại)
            // Trong một service đơn lẻ, bạn có thể tạo mới một ID ngắn hơn
            MDC.put("spanId", UUID.randomUUID().toString().replace("-", "").substring(0, 8));
            filterChain.doFilter(request, response);
        } finally {
            // Quan trọng: Phải clear sau khi request kết thúc để tránh leak data sang thread khác
            MDC.remove("userId");
        }
    }
}