package dbp.backend.meetup.model;

public class MeetupParticipant {
    private final Long memberId;
    private final String nickname;
    private final String profileImageUrl;

    public MeetupParticipant(
            Long memberId,
            String nickname,
            String profileImageUrl
    ) {
        this.memberId = memberId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}
