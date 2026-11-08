package dbp.backend.meetup.exception;

import dbp.backend.common.exception.code.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;

public enum MeetupErrorCode implements ErrorCode {
    INVALID_TITLE(
            HttpServletResponse.SC_BAD_REQUEST,
            "모임 제목은 필수입니다."
    ),
    INVALID_TITLE_LENGTH(
            HttpServletResponse.SC_BAD_REQUEST,
            "모임 제목은 200자 이하여야 합니다."
    ),
    INVALID_CAPACITY(
            HttpServletResponse.SC_BAD_REQUEST,
            "모임 정원은 1명 이상 99999명 이하여야 합니다."
    ),
    INVALID_PLACE(
            HttpServletResponse.SC_BAD_REQUEST,
            "모임 장소는 필수입니다."
    ),
    INVALID_PLACE_LENGTH(
            HttpServletResponse.SC_BAD_REQUEST,
            "모임 장소는 255자 이하여야 합니다."
    ),
    INVALID_IMAGE_URL_LENGTH(
            HttpServletResponse.SC_BAD_REQUEST,
            "모임 이미지 URL은 500자 이하여야 합니다."
    ),
    INVALID_CATEGORY(
            HttpServletResponse.SC_BAD_REQUEST,
            "카테고리는 필수입니다."
    ),
    INVALID_STARTS_AT(
            HttpServletResponse.SC_BAD_REQUEST,
            "모임 시작 일시는 현재 이후여야 합니다."
    ),
    MEETUP_NOT_FOUND(
            HttpServletResponse.SC_NOT_FOUND,
            "모임을 찾을 수 없습니다."
    ),
    NOT_MEETUP_HOST(
            HttpServletResponse.SC_FORBIDDEN,
            "모임장만 모임을 수정하거나 삭제할 수 있습니다."
    ),
    COMPLETED_MEETUP_CANNOT_BE_CHANGED(
            HttpServletResponse.SC_CONFLICT,
            "완료된 모임은 수정하거나 삭제할 수 없습니다."
    ),
    CAPACITY_BELOW_PARTICIPANT_COUNT(
            HttpServletResponse.SC_CONFLICT,
            "모임 정원은 현재 참여 인원보다 적게 설정할 수 없습니다."
    );
    private final int statusCode;
    private final String detail;

    MeetupErrorCode(int statusCode, String detail){
        this.statusCode = statusCode;
        this.detail = detail;
    }

    @Override
    public int getStatusCode(){
        return statusCode;
    }

    @Override
    public String getDetail(){
        return detail;
    }
}
