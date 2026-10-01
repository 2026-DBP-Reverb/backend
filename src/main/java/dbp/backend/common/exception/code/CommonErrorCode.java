package dbp.backend.common.exception.code;

import jakarta.servlet.http.HttpServletResponse;

public enum CommonErrorCode implements ErrorCode {
    BAD_REQUEST(HttpServletResponse.SC_BAD_REQUEST, "잘못된 요청입니다."),
    UNAUTHORIZED(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다."),
    FORBIDDEN(HttpServletResponse.SC_FORBIDDEN, "접근 권한이 없습니다."),
    API_NOT_FOUND(HttpServletResponse.SC_NOT_FOUND, "요청한 API를 찾을 수 없습니다."),
    DATABASE_ERROR(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "데이터베이스 처리 중 오류가 발생했습니다."),
    INTERNAL_SERVER_ERROR(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final int statusCode;
    private final String detail;

    CommonErrorCode(int statusCode, String detail) {
        this.statusCode = statusCode;
        this.detail = detail;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
