package com.john.mysutando.controller;

import com.john.mysutando.dto.rs.ErrorRs;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 處理 Spring Validation (@Valid, @NotNull) 拋出的錯誤
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRs> handleValidationExceptions(
        MethodArgumentNotValidException ex,
        HttpServletRequest request) {

        String errorMessage = ex.getBindingResult().getAllErrors().stream()
            .map(error -> {
                String fieldName = ((FieldError) error).getField();
                String message = error.getDefaultMessage();
                return fieldName + ": " + message;
            })
            .collect(Collectors.joining(", "));

        log.warn("參數驗證失敗: {}", errorMessage);

        ErrorRs errorRs = ErrorRs.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .message(errorMessage)
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.badRequest().body(errorRs);
    }

    // 處理參數型別錯誤
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorRs> handleTypeMismatch(
        MethodArgumentTypeMismatchException ex,
        HttpServletRequest request) {

        String message = String.format("參數 '%s' 型別錯誤，應為 '%s'",
            ex.getName(), ex.getRequiredType().getSimpleName());

        log.warn("參數型別錯誤: {}", message);

        ErrorRs errorRs = ErrorRs.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Type Mismatch")
            .message(message)
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.badRequest().body(errorRs);
    }

    // 處理 Request Body 讀取失敗
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRs> handleHttpMessageNotReadable(
        org.springframework.http.converter.HttpMessageNotReadableException ex,
        HttpServletRequest request) {

        log.warn("請求內容無法讀取: {}", ex.getMessage());

        ErrorRs errorRs = ErrorRs.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Bad Request")
            .message("請求內容 (Request Body) 格式錯誤或遺失，請檢查 JSON 格式")
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.badRequest().body(errorRs);
    }

    // 處理業務邏輯錯誤 (IllegalArgumentException)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorRs> handleIllegalArgument(
        IllegalArgumentException ex,
        HttpServletRequest request) {

        log.warn("業務邏輯驗證失敗: {}", ex.getMessage());

        ErrorRs errorRs = ErrorRs.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Business Logic Error")
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.badRequest().body(errorRs);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorRs> handleIllegalState(
        Exception ex,
        HttpServletRequest request) {

        log.warn("狀態錯誤: {}", ex.getMessage());

        ErrorRs errorRs = ErrorRs.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.CONFLICT.value())
            .error(HttpStatus.CONFLICT.getReasonPhrase())
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorRs);
    }

    //  處理未知的例外
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRs> handleAllExceptions(
        Exception ex,
        HttpServletRequest request) {

        log.error("發生未預期的錯誤", ex);

        ErrorRs errorRs = ErrorRs.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
            .message("又是哪個猴子工程師搞壞了")
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorRs);
    }
}