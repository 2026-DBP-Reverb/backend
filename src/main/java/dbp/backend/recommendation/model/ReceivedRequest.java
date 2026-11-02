package dbp.backend.recommendation.model;

import java.time.LocalDateTime;
// 내가 받은 메이트 요청 한 건
public record ReceivedRequest(Long requestId, Long requesterId, String nickname, String profileImageUrl, LocalDateTime requestedAt) {
}
