package com.ct08.PharmacyManagement.common.security;

import com.ct08.PharmacyManagement.common.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class CustomAccessDeniedHandler implements org.springframework.security.web.access.AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       org.springframework.security.access.AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        // Log lỗi 403 kèm TYPE_LOG và thông tin User (đã có trong MDC)
        log.warn("Access Denied: {}", accessDeniedException.getMessage(),
                net.logstash.logback.argument.StructuredArguments.kv("TYPE_LOG", "SECURITY_LOG"),
                net.logstash.logback.argument.StructuredArguments.kv("uri", request.getRequestURI()));

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // Trả về 403

        ApiResponse<String> apiResponse = new ApiResponse<>(false, "Forbidden: You don't have permission to access this resource", null);

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), apiResponse);
    }
}
