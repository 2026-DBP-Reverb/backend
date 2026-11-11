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
    ),
    EMAIL_HMAC_KEY_INVALID(
            HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            "이메일 인증코드 검증 키의 형식이 유효하지 않습니다."
    ),
    EMAIL_VERIFICATION_DELIVERY_FAILED(
            HttpServletResponse.SC_SERVICE_UNAVAILABLE,
            "인증 메일을 발송하지 못했습니다. 잠시 후 다시 시도해 주세요."
    ),
    INVALID_EMAIL_VERIFICATION_CODE(
            HttpServletResponse.SC_BAD_REQUEST,
            "인증 코드가 올바르지 않거나 만료되었습니다."
    ),
    INVALID_NICKNAME(
            HttpServletResponse.SC_BAD_REQUEST,
            "닉네임은 비어 있을 수 없으며 100바이트 이하여야 합니다."
    ),
    INVALID_INSTAGRAM_ID(
            HttpServletResponse.SC_BAD_REQUEST,
            "인스타그램 아이디는 영문, 숫자, 밑줄, 마침표로 이루어진 30자 이하여야 합니다."
    ),
    INVALID_PROFILE_IMAGE_URL(
            HttpServletResponse.SC_BAD_REQUEST,
            "프로필 사진 URL은 500바이트 이하의 전체 HTTP 또는 HTTPS 주소여야 합니다."
    ),
    ;

    private final int statusCode;
    private final String detail;

    MemberErrorCode(int statusCode, String detail) {
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
