package dbp.backend.musictaste.exception;

import dbp.backend.common.exception.code.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;

public enum MusicTasteErrorCode implements ErrorCode {
    SPOTIFY_SEARCH_FAILED(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "스포티파이 API를 통한 검색 중 오류가 발생했습니다.");;

    private final int statusCode;
    private final String detail;

    MusicTasteErrorCode(int statusCode, String detail) {
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
