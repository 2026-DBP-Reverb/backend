package dbp.backend.member.exception;

import dbp.backend.common.exception.code.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;

public enum MemberErrorCode implements ErrorCode {
    DUPLICATE_EMAIL(
            HttpServletResponse.SC_CONFLICT,
            "이미 사용 중인 이메일입니다."
    ),
    INVALID_EMAIL_FORMAT(
            HttpServletResponse.SC_BAD_REQUEST,
            "이메일 형식이 올바르지 않습니다."
    ),
    INVALID_PASSWORD_FORMAT(
            HttpServletResponse.SC_BAD_REQUEST,
            "비밀번호는 8자 이상의 영문과 숫자 조합이어야 합니다."
    ),
    PASSWORD_CONFIRM_MISMATCH(
            HttpServletResponse.SC_BAD_REQUEST,
            "비밀번호와 비밀번호 확인이 일치하지 않습니다."
    ),
    INVALID_LOGIN_CREDENTIALS(
            HttpServletResponse.SC_UNAUTHORIZED,
            "이메일 또는 비밀번호가 올바르지 않습니다."
    ),
    MEMBER_NOT_FOUND(
            HttpServletResponse.SC_NOT_FOUND,
            "회원을 찾을 수 없습니다."
    );

    private final int statusCode;
    private final String detail;

    MemberErrorCode(int statusCode, String detail){
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
