package dbp.backend.settlement.exception;

import dbp.backend.common.exception.code.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;

public enum SettlementErrorCode implements ErrorCode {
    INVALID_MEETUP_ID(HttpServletResponse.SC_BAD_REQUEST, "모임 ID는 양수여야 합니다."),
    INVALID_TITLE(HttpServletResponse.SC_BAD_REQUEST, "정산 제목은 공백이 아닌 255자 이하의 문자열이어야 합니다."),
    INVALID_ACCOUNT(HttpServletResponse.SC_BAD_REQUEST, "계좌 정보는 공백이 아닌 127자 이하의 문자열이어야 합니다."),
    INVALID_SPENT_AT(HttpServletResponse.SC_BAD_REQUEST, "지출일은 필수입니다."),
    INVALID_SPLIT_METHOD(HttpServletResponse.SC_BAD_REQUEST, "정산 방식은 필수입니다."),
    INVALID_PARTICIPANT(HttpServletResponse.SC_BAD_REQUEST, "정산 참여자 항목은 null일 수 없습니다."),
    INVALID_MEMBER_ID(HttpServletResponse.SC_BAD_REQUEST, "정산 참여자의 회원 ID는 양수여야 합니다."),
    INVALID_PARTICIPANT_AMOUNT(HttpServletResponse.SC_BAD_REQUEST, "참여자별 정산 금액은 양수여야 합니다."),
    NO_PARTICIPANTS(HttpServletResponse.SC_BAD_REQUEST, "정산 참여 멤버가 없습니다."),
    DUPLICATED_PARTICIPANTS(HttpServletResponse.SC_BAD_REQUEST, "정산 참여 멤버에 중복된 멤버가 있습니다."),
    INVALID_TOTAL_AMOUNT(HttpServletResponse.SC_BAD_REQUEST, "총 정산 금액은 양수이며 멤버별 정산 금액 합계와 같아야 합니다."),
    NOT_MEETUP_PARTICIPANT(HttpServletResponse.SC_BAD_REQUEST, "정산 참여자 중 해당 모임 멤버가 아닌 사용자가 존재합니다.");

    private final int statusCode;
    private final String detail;

    SettlementErrorCode(int statusCode, String detail) {
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
