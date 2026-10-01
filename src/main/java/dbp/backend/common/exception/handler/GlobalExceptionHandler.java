package dbp.backend.common.exception.handler;

import dbp.backend.common.exception.ApiException;
import dbp.backend.common.exception.code.CommonErrorCode;
import dbp.backend.common.exception.code.ErrorCode;
import dbp.backend.common.response.ApiError;
import dbp.backend.common.response.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ResponseBody<Void>> handleApiException(ApiException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatusCode())
                .body(ResponseBody.error(new ApiError(errorCode)));
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ResponseBody<Void>> handleSQLException(SQLException e) {
        return ResponseEntity
                .status(CommonErrorCode.DATABASE_ERROR.getStatusCode())
                .body(ResponseBody.error(new ApiError(CommonErrorCode.DATABASE_ERROR)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseBody<Void>> handleException(Exception e) {
        return ResponseEntity
                .status(CommonErrorCode.INTERNAL_SERVER_ERROR.getStatusCode())
                .body(ResponseBody.error(new ApiError(CommonErrorCode.INTERNAL_SERVER_ERROR)));
    }
}
