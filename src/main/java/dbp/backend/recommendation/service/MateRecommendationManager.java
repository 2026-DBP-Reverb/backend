package dbp.backend.recommendation.service;

import dbp.backend.recommendation.dao.MateRecommendationDao;
import dbp.backend.recommendation.dto.response.MateRecommendationResponse;
import dbp.backend.recommendation.model.CommonTag;
import dbp.backend.recommendation.model.MateCandidate;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class MateRecommendationManager {
    private final MateRecommendationDao mateRecommendationDao;

    private static final int RECOMMENDATION_LIMIT = 10;

    public MateRecommendationManager(
            MateRecommendationDao mateRecommendationDao
    ){
        this.mateRecommendationDao = mateRecommendationDao;
    }

    public List<MateRecommendationResponse> getRecommendations(Long memberId) {
        List<MateCandidate> candidates =
                mateRecommendationDao.findTopCandidates(memberId, RECOMMENDATION_LIMIT);
        List<CommonTag> commonTags =
                mateRecommendationDao.findCommonTags((memberId));

        return candidates.stream()
                .map(candidate -> MateRecommendationResponse.of(
                        candidate,
                        namesOf(commonTags, candidate.getMemberId(),"ARTIST"),
                        namesOf(commonTags, candidate.getMemberId(), "GENRE"),
                        namesOf(commonTags, candidate.getMemberId(), "TRACK")
                ))
                .toList();
    }

    private List<String> namesOf(List<CommonTag> tags, Long memberId, String type) {
        return tags.stream()
                .filter(tag -> tag.memberId().equals(memberId) && tag.type().equals(type))
                .map(tag -> tag.name())
                .toList();
    }

}
