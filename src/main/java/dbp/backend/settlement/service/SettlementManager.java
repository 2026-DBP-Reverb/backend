package dbp.backend.settlement.service;

import dbp.backend.common.exception.ApiException;
import dbp.backend.common.exception.code.CommonErrorCode;
import dbp.backend.settlement.dao.SettlementRepository;
import dbp.backend.settlement.dto.request.CreateSettlementRequest;
import dbp.backend.settlement.dto.response.SettlementDetailResponse;
import dbp.backend.settlement.dto.response.SettlementSummaryResponse;
import dbp.backend.settlement.exception.SettlementErrorCode;
import dbp.backend.settlement.model.SettlementDetail;
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

    /**
     * 정산 상세 조회 (기본 정보 + 멤버별 정산 항목)
     *
     * @param memberId     로그인한 회원 (권한 체크용)
     * @param settlementId 조회할 정산 id
     */
    public SettlementDetailResponse getSettlementDetail(Long memberId, Long settlementId) {
        // 1. 정산 기본 정보 조회
        SettlementDetail detail = settlementRepository.findSettlementDetail(settlementId);

        // 2. 없는 정산이면 404
        if(detail == null) {
            throw new ApiException(SettlementErrorCode.SETTLEMENT_NOT_FOUND);
        }

        // 3. 이 정산이 속한 모임의 참여자가 아니면 403
        List<Long> participantIds = settlementRepository.findMeetupParticipantMemberIds(detail.meetupId());
        if (!participantIds.contains(memberId)) {
            throw new ApiException(CommonErrorCode.FORBIDDEN);
        }

        // 4. 멤버별 정산 항목 조회
        List<SettlementDetailResponse.ShareItem> shares = settlementRepository.findSettlementShares(settlementId);

        // 5. 기본 정보 + 멤버별 항목을 합쳐서 응답 생성
        return new SettlementDetailResponse(
                detail.settlementId(),
                detail.title(),
                detail.totalAmount(),
                detail.spentAt(),
                detail.createdAt(),
                detail.account(),
                detail.splitMethod(),
                detail.paidByMemberId(),
                detail.paidByNickname(),
                shares
        );
    }
}
