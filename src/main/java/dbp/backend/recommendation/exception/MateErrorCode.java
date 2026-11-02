package dbp.backend.recommendation.exception;
import dbp.backend.common.exception.code.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;

public enum MateErrorCode implements ErrorCode {
    CANNOT_REQUEST_SELF(
            HttpServletResponse.SC_BAD_REQUEST,
            "나 자신에게 메이트신청을 할 수 없습니다."
    ),
    MATE_NOT_FOUND(
            HttpServletResponse.SC_NOT_FOUND,
            "없는 회원입니다."
    ),
    ALREADY_MATCHED(
            HttpServletResponse.SC_CONFLICT,
            "이미 메이트 매칭이 되었습니다."
    ),
    REQUEST_ALREADY_SENT(
            HttpServletResponse.SC_CONFLICT,
            "이미 메이트 신청을 하였습니다."
    ),
    REQUEST_ALREADY_RECEIVED(
            HttpServletResponse.SC_CONFLICT,
            "상대가 이미 나에게 메이트 신청을 하였습니다."
    );

    private final int statusCode;
    private final String detail;

    MateErrorCode(int statusCode, String detail){
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
