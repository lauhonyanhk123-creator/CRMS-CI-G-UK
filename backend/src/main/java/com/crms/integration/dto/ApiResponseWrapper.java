package com.crms.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Generic API response wrapper for external integrations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseWrapper<T> {
    
    private boolean success;
    private T data;
    private String errorCode;
    private String errorMessage;
    private LocalDateTime timestamp;
    
    public static <T> ApiResponseWrapper<T> success(T data) {
        return ApiResponseWrapper.<T>builder()
                .success(true)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static <T> ApiResponseWrapper<T> error(String errorCode, String errorMessage) {
        return ApiResponseWrapper.<T>builder()
                .success(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
