package dbp.backend.member.model;

import java.time.LocalDateTime;

public class Member {
    private final Long memberId;
    private String email;
    private String passwordHash;
    private String profileImageUrl;
    private String instagramId;
    private String nickname;
    private final LocalDateTime createdAt;

    public Member(
            Long memberId,
            String email,
            String passwordHash,
            String profileImageUrl,
            String instagramId,
            String nickname,
            LocalDateTime createdAt
    ) {
        this.memberId = memberId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.profileImageUrl = profileImageUrl;
        this.instagramId = instagramId;
        this.nickname = nickname;
        this.createdAt = createdAt;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getInstagramId() {
        return instagramId;
    }

    public String getNickname() {
        return nickname;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void updateProfile(
            String profileImageUrl,
            String instagramId,
            String nickname
    ){
        this.profileImageUrl = profileImageUrl;
        this.instagramId = instagramId;
        this.nickname = nickname;
    }
}
