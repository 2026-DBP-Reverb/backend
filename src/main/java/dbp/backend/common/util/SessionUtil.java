package dbp.backend.common.util;

import dbp.backend.common.exception.ApiException;
import dbp.backend.common.exception.code.CommonErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class SessionUtil {
    private static final String LOGIN_MEMBER_ID = "loginMemberId";

    private SessionUtil() {
    }

    /**
     * 로그인 성공 시 회원 ID를 새 HTTP 세션에 저장 <br>
     * 기존 세션이 있으면 먼저 무효화한 뒤 새 세션을 생성
     *
     * @param request  현재 HTTP 요청
     * @param memberId 로그인한 회원 ID
     * @throws ApiException memberId가 null인 경우
     */
    public static void login(HttpServletRequest request, Long memberId) {
        if (memberId == null) {
            throw new ApiException(CommonErrorCode.BAD_REQUEST);
        }

        logout(request);
        HttpSession session = request.getSession(true);
        session.setAttribute(LOGIN_MEMBER_ID, memberId);
    }

    /**
     * 현재 요청의 세션을 무효화해서 로그아웃 처리 <br>
     * 세션이 없는 요청이면 아무 작업도 하지 않음
     *
     * @param request 현재 HTTP 요청
     */
    public static void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * 현재 요청이 로그인 상태인지 확인
     *
     * @param request 현재 HTTP 요청
     * @return 로그인 회원 ID가 세션에 있으면 true, 아니면 false
     */
    public static boolean isLoggedIn(HttpServletRequest request) {
        return getLoginMemberId(request) != null;
    }

    /**
     * 현재 세션에 저장된 로그인 회원 ID를 조회
     *
     * <p>세션이 없거나 로그인 정보가 없으면 null을 반환,
     * 세션 값이 Number 타입이면 Long으로 변환해서 반환</p>
     *
     * @param request 현재 HTTP 요청
     * @return 로그인 회원 ID, 없으면 null
     */
    public static Long getLoginMemberId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Object value = session.getAttribute(LOGIN_MEMBER_ID);
        if (value instanceof Long memberId) {
            return memberId;
        }
        if (value instanceof Number numberValue) {
            return numberValue.longValue();
        }
        return null;
    }

    /**
     * 현재 로그인한 회원이 전달받은 회원 ID와 같은지 확인
     *
     * @param request  현재 HTTP 요청
     * @param memberId 비교할 회원 ID
     * @return 로그인 회원 ID와 memberId가 같으면 true, 아니면 false
     */
    public static boolean isLoginMember(HttpServletRequest request, Long memberId) {
        Long loginMemberId = getLoginMemberId(request);
        return loginMemberId != null && loginMemberId.equals(memberId);
    }
}
