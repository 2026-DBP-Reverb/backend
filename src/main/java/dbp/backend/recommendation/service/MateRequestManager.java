package dbp.backend.recommendation.service;

import dbp.backend.recommendation.dao.MateRequestDao;
import dbp.backend.recommendation.dto.response.ReceivedRequestResponse;
import org.springframework.stereotype.Service;

import java.util.List;
//내가 받은 메이트 요청 중 대기중(PENDING)인 목록 조회
//신청자 닉네임, 프로필 사진과 함께 최신순으로 반환
@Service
public class MateRequestManager {
    private final MateRequestDao mateRequestDao;

    public MateRequestManager(MateRequestDao mateRequestDao) {
        this.mateRequestDao = mateRequestDao;
    }
    public List<ReceivedRequestResponse> getReceivedRequests(Long memberId){
        return mateRequestDao.findReceivedRequests(memberId)
                .stream()
                .map(ReceivedRequestResponse::from)
                .toList();
    }
}
