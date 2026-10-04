package dbp.backend.member.model;

import java.time.LocalDateTime;

public class Member{
    private final Long memberId;
    private String email;
    private String passwordHash;
    private final LocalDateTime createdAt;

    public Member(
            Long memberId,
            String email,
            String passwordHash,
            LocalDateTime createdAt
    ) {
        this.memberId = memberId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public Long getMemberId(){
        return memberId;
    }

    public String getEmail(){
        return email;
    }

    public String getPasswordHash(){
        return passwordHash;
    }

    public LocalDateTime getCreatedAt(){
        return createdAt;
    }
}
