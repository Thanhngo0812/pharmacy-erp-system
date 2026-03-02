package com.ct08.PharmacyManagement.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ct08.PharmacyManagement.common.dto.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // Log lỗi 403 kèm TYPE_LOG và thông tin User (đã có trong MDC)
        log.warn("Unauthorized error: {}", authException.getMessage(),
                net.logstash.logback.argument.StructuredArguments.kv("TYPE_LOG", "SECURITY_LOG"),
                net.logstash.logback.argument.StructuredArguments.kv("uri", request.getRequestURI()));

        ApiResponse<String> apiResponse = new ApiResponse<>(false, "Unauthorized: " + authException.getMessage(), null);

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), apiResponse);
    }
}
