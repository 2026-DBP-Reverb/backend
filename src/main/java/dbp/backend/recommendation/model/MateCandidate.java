package dbp.backend.recommendation.model;

public class MateCandidate {
    private Long memberId;
    private String nickname;
    private String profileImageUrl;
    private int matchRate;

    public MateCandidate(
            Long memberId,
            String nickname,
            String profileImageUrl,
            int matchRate
    ) {
        this.memberId = memberId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.matchRate = matchRate;
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

    public int getMatchRate() {
        return matchRate;
    }
}
