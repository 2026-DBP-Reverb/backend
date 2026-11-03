package dbp.backend.recommendation.service;

import dbp.backend.common.exception.ApiException;
import dbp.backend.common.exception.code.CommonErrorCode;
import dbp.backend.member.dao.MemberDao;
import dbp.backend.member.model.Member;
import dbp.backend.musictaste.dao.MusicTasteDao;
import dbp.backend.musictaste.dto.response.ArtistResponse;
import dbp.backend.musictaste.dto.response.GenreResponse;
import dbp.backend.musictaste.dto.response.TrackResponse;
import dbp.backend.recommendation.dao.MateRecommendationDao;
import dbp.backend.recommendation.dto.request.MateRequestCreateRequest;
import dbp.backend.recommendation.dto.response.MateProfileResponse;
import dbp.backend.recommendation.dto.response.MateRecommendationResponse;
import dbp.backend.recommendation.exception.MateErrorCode;
import dbp.backend.recommendation.model.CommonTag;
import dbp.backend.recommendation.model.MateCandidate;
import dbp.backend.recommendation.model.RequestStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service
public class MateRecommendationManager {
    private final MateRecommendationDao mateRecommendationDao;
    private final MemberDao memberDao;
    private static final int RECOMMENDATION_LIMIT = 10;
    private final MusicTasteDao musicTasteDao;
    private static final String IMAGE_SERVER_URI = "http://localhost:8080";

    public MateRecommendationManager(
            MateRecommendationDao mateRecommendationDao,
            MemberDao memberDao, MusicTasteDao musicTasteDao
    ){
        this.mateRecommendationDao = mateRecommendationDao;
        this.memberDao = memberDao;
        this.musicTasteDao = musicTasteDao;
    }

    public List<MateRecommendationResponse> getRecommendations(Long memberId) {
        List<MateCandidate> candidates =
                mateRecommendationDao.findTopCandidates(memberId, RECOMMENDATION_LIMIT);
        List<CommonTag> commonTags =
                mateRecommendationDao.findCommonTags((memberId));
        Map<Long, RequestStatus> requestStatuses = mateRecommendationDao.findPendingRequestStatuses((memberId));

        return candidates.stream()
                .map(candidate -> MateRecommendationResponse.of(
                        candidate,
                        namesOf(commonTags, candidate.getMemberId(),"ARTIST"),
                        namesOf(commonTags, candidate.getMemberId(), "GENRE"),
                        namesOf(commonTags, candidate.getMemberId(), "TRACK"),
                        requestStatuses.getOrDefault(candidate.getMemberId(), RequestStatus.NONE)
                ))
                .toList();
    }

    private List<String> namesOf(List<CommonTag> tags, Long memberId, String type) {
        return tags.stream()
                .filter(tag -> tag.memberId().equals(memberId) && tag.type().equals(type))
                .map(tag -> tag.name())
                .toList();
    }

    //메이트 신청이 들어오면 신청해도 되는지 한단계씩 검사(MateErrorCode)하고, 다 통과하면 저장
    public void requestMate(Long memberId, MateRequestCreateRequest request) {

        if(request == null || request.recipientId() == null)
            throw new ApiException(CommonErrorCode.BAD_REQUEST);
        else if(memberId.equals(request.recipientId()))
            throw new ApiException(MateErrorCode.CANNOT_REQUEST_SELF);
        else if(memberDao.findById(request.recipientId()) == null)
            throw new ApiException(MateErrorCode.MATE_NOT_FOUND);
        else if(mateRecommendationDao.isMatched(memberId, request.recipientId()) == true)
            throw new ApiException(MateErrorCode.ALREADY_MATCHED);

        RequestStatus status = mateRecommendationDao.findPendingRequestStatuses(memberId)
                .getOrDefault(request.recipientId(), RequestStatus.NONE);
        if(status == RequestStatus.SENT)
            throw new ApiException(MateErrorCode.REQUEST_ALREADY_SENT);
        if(status == RequestStatus.RECEIVED)
            throw new ApiException(MateErrorCode.REQUEST_ALREADY_RECEIVED);
        mateRecommendationDao.saveRequest(memberId, request.recipientId());
    }

    /**
     * 상대 회원의 상세 취향 프로필 조회 (아티스트/장르/플레이리스트)
     * 인스타그램 아이디는 매칭된 상대일 때만 공개
     */
    public MateProfileResponse getMateProfile(Long memberId, Long targetMemberId) {
        // memberId = 나, targetMemberId = 보고 싶은 상대
       Member target = memberDao.findById(targetMemberId);
        if(target == null)
            throw new ApiException(MateErrorCode.MATE_NOT_FOUND);

        String instagramId = null;
        if(mateRecommendationDao.isMatched(memberId, targetMemberId))
            instagramId = target.getInstagramId();

        List<TrackResponse> tracks = musicTasteDao.getTrackTastes(targetMemberId)
                .stream().map(TrackResponse::from).toList();

        List<ArtistResponse> artists = musicTasteDao.getArtistTastes(targetMemberId)
                .stream().map(ArtistResponse::from).toList();

        List<GenreResponse> genres = musicTasteDao.getGenres(targetMemberId)
                .stream()
                .filter(genre -> genre.isSelected())
                .map(GenreResponse::from)
                .toList();

        RequestStatus requestStatus = mateRecommendationDao.findPendingRequestStatuses(memberId)
                .getOrDefault(targetMemberId, RequestStatus.NONE);
//                            ──────────────
//                            프로필을 보는 상대

        return new MateProfileResponse(
                target.getMemberId(),
                target.getNickname(),
                IMAGE_SERVER_URI + target.getProfileImageUrl(),
                instagramId,
                artists,
                genres,
                tracks,
                requestStatus
        );

    }

}
