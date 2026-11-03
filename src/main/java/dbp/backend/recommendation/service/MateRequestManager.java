package dbp.backend.recommendation.service;

import dbp.backend.common.exception.ApiException;
import dbp.backend.recommendation.dao.MateRequestDao;
import dbp.backend.recommendation.dto.response.ReceivedRequestResponse;
import dbp.backend.recommendation.exception.MateErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MateRequestManager {
    private final MateRequestDao mateRequestDao;

    public MateRequestManager(MateRequestDao mateRequestDao) {
        this.mateRequestDao = mateRequestDao;
    }

    /**
    *내가 받은 메이트 요청 중 대기중(PENDING)인 목록 조회
    *신청자 닉네임, 프로필 사진과 함께 최신순으로 반환
     */
    public List<ReceivedRequestResponse> getReceivedRequests(Long memberId){
        return mateRequestDao.findReceivedRequests(memberId)
                .stream()
                .map(ReceivedRequestResponse::from)
                .toList();
    }

    /**
     *내가 받은 대기 중인 메이트 요청을 거절 처리
     *내 요청이 아니거나 이미 처리된 요청이면 REQUEST_NOT_FOUND 에러
     */
    public void rejectRequest(Long memberId, Long requestId){
        if(mateRequestDao.rejectRequest(requestId, memberId) == false)
            throw new ApiException(MateErrorCode.REQUEST_NOT_FOUND);
    }

    /**
     *내가 받은 대기 중인 메이트 요청을 수락 처리 + 매칭 생성
     */
    public void acceptRequest(Long memberId, Long requestId){
        if(mateRequestDao.acceptRequest(requestId, memberId) == false)
            throw new ApiException(MateErrorCode.REQUEST_NOT_FOUND);
    }
}
