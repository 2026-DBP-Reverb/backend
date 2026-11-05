package dbp.backend.recommendation.model;

/**
 * 나의 메이트 목록 한 줄 (매칭 완료 / 보낸 요청 / 받은 요청)
 */
public record MyMate(
        String status,           // "MATCHED" / "SENT" / "RECEIVED"
        Long requestId,          // 받은/보낸 요청 번호 (매칭이면 null)
        Long memberId,           // 상대 회원
        String nickname,
        String profileImageUrl,
        String instagramId       // 매칭일 때만 값이 있음
) {
}