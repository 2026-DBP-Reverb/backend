package dbp.backend.home.service;

import dbp.backend.home.dto.response.HomeMeetupCardResponse;
import dbp.backend.home.dto.response.HomeMemberCardResponse;
import dbp.backend.meetup.dao.MeetupDao;
import dbp.backend.recommendation.dao.MateRecommendationDao;
import dbp.backend.recommendation.model.CommonTag;
import dbp.backend.recommendation.model.MateCandidate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HomeManager {

    private static final int RECENT_MEMBER_LIMIT = 3;
    private static final int HASHTAG_LIMIT = 3;

    private final MeetupDao meetupDao;
    private final MateRecommendationDao mateRecommendationDao;


    public HomeManager(
            MeetupDao meetupDao,
            MateRecommendationDao mateRecommendationDao
    ) {
        this.meetupDao = meetupDao;
        this.mateRecommendationDao =
                mateRecommendationDao;
    }

    public List<HomeMeetupCardResponse>
    getRecentMeetups() {
        return meetupDao.findRecentMeetups();
    }

    public List<HomeMemberCardResponse> getRecentMembers(
            Long memberId
    ) {
        List<MateCandidate> candidates =
                mateRecommendationDao.findRecentCandidates(
                        memberId,
                        RECENT_MEMBER_LIMIT
                );

        List<Long> candidateIds = candidates.stream()
                .map(MateCandidate::getMemberId)
                .toList();

        List<CommonTag> commonTags =
                mateRecommendationDao.findCommonTags(
                        memberId,
                        candidateIds
                );

        Map<Long, List<String>> hashtagsByMember =
                new HashMap<>();

        for (CommonTag commonTag : commonTags) {
            List<String> hashtags =
                    hashtagsByMember.computeIfAbsent(
                            commonTag.memberId(),
                            key -> new ArrayList<>()
                    );

            if (
                    hashtags.size() < HASHTAG_LIMIT
                            && !hashtags.contains(commonTag.name())
            ) {
                hashtags.add(commonTag.name());
            }
        }

        return candidates.stream()
                .map(candidate ->
                        HomeMemberCardResponse.of(
                                candidate,
                                hashtagsByMember.getOrDefault(
                                        candidate.getMemberId(),
                                        List.of()
                                )
                        )
                )
                .toList();
    }
}
