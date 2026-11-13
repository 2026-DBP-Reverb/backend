package dbp.backend.settlement.service;

import dbp.backend.common.exception.ApiException;
import dbp.backend.common.exception.code.CommonErrorCode;
import dbp.backend.settlement.dao.SettlementRepository;
import dbp.backend.settlement.dto.request.CreateSettlementRequest;
import dbp.backend.settlement.dto.response.SettlementSummaryResponse;
import dbp.backend.settlement.exception.SettlementErrorCode;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SettlementManager {

    private final SettlementRepository settlementRepository;

    public SettlementManager(SettlementRepository settlementRepository) {
        this.settlementRepository = settlementRepository;
    }

    public void createSettlement(Long memberId, CreateSettlementRequest request) {
        // 요청 검증
        request.validate();
        Set<Long> meetupParticipantIds = new HashSet<>(
                settlementRepository.findMeetupParticipantMemberIds(request.meetupId())
        );
        for (CreateSettlementRequest.SettlementParticipant participant : request.participants()) {
            // 정산 참여자 중 해당 모임 회원이 아닌 사람이 있는 경우
            if (!meetupParticipantIds.contains(participant.memberId())) {
                throw new ApiException(SettlementErrorCode.NOT_MEETUP_PARTICIPANT);
            }
        }
        if (!meetupParticipantIds.contains(memberId)) {  // 정산 등록자가 해당 모임 회원이 아닌 경우
            throw new ApiException(SettlementErrorCode.NOT_MEETUP_PARTICIPANT);
        }
        // 정산 insert
        settlementRepository.insertSettlement(memberId, request);
    }

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
