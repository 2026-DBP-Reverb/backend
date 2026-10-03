package dbp.backend.member.model;

import java.time.LocalDateTime;

public record Member(
    Long memberId,
    String email,
    String passwordHash,
    LocalDateTime createdAt
) {

}
