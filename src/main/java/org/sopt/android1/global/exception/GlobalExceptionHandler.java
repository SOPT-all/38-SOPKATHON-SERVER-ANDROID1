package org.sopt.android1.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.sopt.android1.global.response.ApiResponseBody;
import org.sopt.android1.global.response.ErrorMeta;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseBody<Void, ErrorMeta>> handleHttpMessageNotReadable(
        HttpMessageNotReadableException ex,
        HttpServletRequest request
    ) {
        Throwable rootCause = ex;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }

        if (rootCause instanceof BusinessException businessException) {
            ErrorCode errorCode = businessException.getErrorCode();
            log.warn("[{}] {}", errorCode.getCode(), businessException.getMessage());
            return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponseBody.onFailure(errorCode, ErrorMeta.of(request.getRequestURI())));
        }

        log.warn("HttpMessageNotReadable: {}", ex.getMessage());
        return ResponseEntity
            .status(ErrorCode.INVALID_MAPPING_PARAMETER.getStatus())
            .body(ApiResponseBody.onFailure(
                ErrorCode.INVALID_MAPPING_PARAMETER,
                ErrorMeta.of(request.getRequestURI())
            ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseBody<Void, ErrorMeta>> handleValidation(
        MethodArgumentNotValidException ex,
        HttpServletRequest request
    ) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .findFirst()
            .orElse(ErrorCode.INVALID_INPUT_VALUE.getMessage());

        log.warn("Validation failed: {}", detail);
        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
            .body(ApiResponseBody.onFailure(
                ErrorCode.INVALID_INPUT_VALUE,
                detail,
                ErrorMeta.of(request.getRequestURI())
            ));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponseBody<Void, ErrorMeta>> handleBusinessException(
        BusinessException e,
        HttpServletRequest request
    ) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("[{}] {}", errorCode.getCode(), e.getMessage());
        return ResponseEntity
            .status(errorCode.getStatus())
            .body(ApiResponseBody.onFailure(errorCode, ErrorMeta.of(request.getRequestURI())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseBody<Void, ErrorMeta>> handleUnhandled(
        Exception e,
        HttpServletRequest request
    ) {
        log.error("Unhandled exception", e);
        return ResponseEntity
            .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
            .body(ApiResponseBody.onFailure(
                ErrorCode.INTERNAL_SERVER_ERROR,
                ErrorMeta.of(request.getRequestURI())
            ));
    }
}