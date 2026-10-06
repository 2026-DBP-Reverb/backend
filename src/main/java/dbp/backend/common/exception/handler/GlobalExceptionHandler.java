package dbp.backend.common.exception.handler;

import dbp.backend.common.exception.ApiException;
import dbp.backend.common.exception.code.CommonErrorCode;
import dbp.backend.common.exception.code.ErrorCode;
import dbp.backend.common.response.ApiError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 서비스 내 ApiException을 받아 처리
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatusCode())
                .body(new ApiError(errorCode));
    }

    // 서비스 내 SqlException을 받아 처리
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ApiError> handleSQLException(SQLException e) {
        return ResponseEntity
                .status(CommonErrorCode.DATABASE_ERROR.getStatusCode())
                .body(new ApiError(CommonErrorCode.DATABASE_ERROR));
    }

    // 기타 Exception들을 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(Exception e) {
        return ResponseEntity
                .status(CommonErrorCode.INTERNAL_SERVER_ERROR.getStatusCode())
                .body(new ApiError(CommonErrorCode.INTERNAL_SERVER_ERROR));
    }
}
