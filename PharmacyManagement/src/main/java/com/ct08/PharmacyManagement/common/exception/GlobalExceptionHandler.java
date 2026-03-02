package com.ct08.PharmacyManagement.common.exception;

import com.ct08.PharmacyManagement.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Nhóm 1: Lỗi xác thực (Log WARN - Không cần stack trace)
    @ExceptionHandler({BadCredentialsException.class, DisabledException.class, LockedException.class})
    public ResponseEntity<ApiResponse<String>> handleSecurityException(Exception ex) {
        log.warn("Security exception occurred: {}", ex.getMessage(),
                StructuredArguments.kv("TYPE_LOG", "SECURITY_LOG"));

        String message = "Authentication failed";
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        if (ex instanceof DisabledException) message = "Account is disabled";
        if (ex instanceof LockedException) {
            message = "Account is locked";
            status = HttpStatus.LOCKED;
        }
        return ResponseEntity.status(status).body(ApiResponse.<String>error(message, null));
    }

    // Nhóm 2: Lỗi nghiệp vụ - Không tìm thấy (Log INFO/WARN)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.info("Resource not found: {}", ex.getMessage(),
                StructuredArguments.kv("TYPE_LOG", "BUSINESS_LOG"));

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.<String>error(ex.getMessage(), null));
    }

    // Nhóm 3: Lỗi quyền truy cập (Log WARN)
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiResponse<String>> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException ex) {
        log.warn("Access denied for user: {}", ex.getMessage(),
                StructuredArguments.kv("TYPE_LOG", "SECURITY_LOG"));

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.<String>error("Access denied", null));
    }

    // Nhóm 4: Lỗi hệ thống không mong muốn (Log ERROR - Bắt buộc kèm Stack Trace)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGlobalException(Exception ex) {
        // Log mức ERROR kèm đối tượng ex để in toàn bộ Stack Trace ra JSON
        log.error("Critical system error: {}", ex.getMessage(), ex,
                StructuredArguments.kv("TYPE_LOG", "SYSTEM_ERROR"));

        // Không trả message lỗi chi tiết của Exception ra Client để bảo mật
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<String>error("An unexpected error occurred. Please contact admin.", null));
    }

    // Nhóm 5: Lỗi Validation dữ liệu đầu vào (Log WARN)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<java.util.Map<String, String>>> handleValidationExceptions(
           MethodArgumentNotValidException ex) {

        java.util.Map<String, String> errors = new java.util.HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation failed for request: {}", errors,
                StructuredArguments.kv("TYPE_LOG", "BUSINESS_LOG"));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<java.util.Map<String, String>>error("Input data in not valid:", errors));
    }

    // Thêm hàm này vào GlobalExceptionHandler
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Tham số '%s' phải có kiểu dữ liệu là %s (Giá trị nhận được: '%s')",
                ex.getName(), ex.getRequiredType().getSimpleName(), ex.getValue());

        log.warn("Type mismatch error: {}", message,
                StructuredArguments.kv("TYPE_LOG", "BUSINESS_LOG"));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message, null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage(),
                StructuredArguments.kv("TYPE_LOG", "BUSINESS_LOG"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<String>error(ex.getMessage(), null));
    }

    @ExceptionHandler(ConflictException.class) // Bạn tự định nghĩa class này
    public ResponseEntity<ApiResponse<String>> handleConflictException(ConflictException ex) {
        log.warn("conflict: {}", ex.getMessage(),
                StructuredArguments.kv("TYPE_LOG", "BUSINESS_LOG"));
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.<String>error(ex.getMessage(), null));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<String>> handleBadRequestException(BadRequestException ex) {
        log.warn("Bad request: {}", ex.getMessage(),
                StructuredArguments.kv("TYPE_LOG", "BUSINESS_LOG"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<String>error(ex.getMessage(), null));
    }
}
