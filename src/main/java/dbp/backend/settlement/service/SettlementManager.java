package dbp.backend.settlement.service;

import dbp.backend.common.exception.ApiException;
import dbp.backend.common.exception.code.CommonErrorCode;
import dbp.backend.settlement.dao.SettlementRepository;
import dbp.backend.settlement.dto.request.CreateSettlementRequest;
import dbp.backend.settlement.dto.response.SettlementSummaryResponse;
import dbp.backend.settlement.exception.SettlementErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SettlementManager {

    private final SettlementRepository settlementRepository;

    public SettlementManager(SettlementRepository settlementRepository) {
        this.settlementRepository = settlementRepository;
    }

    /**
     * 새로운 정산 내역 생성
     *
     * @param memberId 정산 생성자 (즉 대표 결제자)
     */
    public void createSettlement(Long memberId, CreateSettlementRequest request) {
        // 요청 검증
        request.validate();
        // 정산 insert
        settlementRepository.insertSettlement(memberId, request);
    }

    /**
     * 모임의 정산 내역을 간략한 응답 목록으로 반환
     */
    public List<SettlementSummaryResponse> getSettlementsList(Long memberId, Long meetupId) {
        // 모임 id 검증
        if (meetupId == null || meetupId <= 0) {
            throw new ApiException(SettlementErrorCode.INVALID_MEETUP_ID);
        }

        // 모임 참여자 아닌 경우 정산 목록 조회 불가
        List<Long> participantIds = settlementRepository.findMeetupParticipantMemberIds(meetupId);
        if (!participantIds.contains(memberId)) {
            throw new ApiException(CommonErrorCode.FORBIDDEN);
        }

        return settlementRepository.findSettlementsByMeetupId(meetupId);
    }
}
