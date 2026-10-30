package dbp.backend.recommendation.service;

import dbp.backend.recommendation.dao.MateRecommendationDao;
import dbp.backend.recommendation.dto.response.MateRecommendationResponse;
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

    public List<MateRecommendationResponse> getRecommendations(
            Long memberId
    ) {
        return mateRecommendationDao.findTopCandidates(memberId, RECOMMENDATION_LIMIT)
                .stream()
                .map(MateRecommendationResponse::from)
                .toList();
    }

}
