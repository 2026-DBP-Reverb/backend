package dbp.backend.settlement.dto.request;

import dbp.backend.common.exception.ApiException;
import dbp.backend.settlement.exception.SettlementErrorCode;
import dbp.backend.settlement.model.SplitMethod;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record CreateSettlementRequest(
        Long meetupId,
        String title,
        int totalAmount,
        String account,
        SplitMethod splitMethod,
        LocalDate spentAt,
        List<SettlementParticipant> participants
) {
    public void validate() {
        // 요청의 빈 값 검증
        if (meetupId == null || meetupId <= 0) {
            throw new ApiException(SettlementErrorCode.INVALID_MEETUP_ID);
        }
        if (title == null || title.isBlank()) {
            throw new ApiException(SettlementErrorCode.INVALID_TITLE);
        }
        if (account == null || account.isBlank()) {
            throw new ApiException(SettlementErrorCode.INVALID_ACCOUNT);
        }
        if (splitMethod == null) {
            throw new ApiException(SettlementErrorCode.INVALID_SPLIT_METHOD);
        }
        if (spentAt == null) {
            throw new ApiException(SettlementErrorCode.INVALID_SPENT_AT);
        }

        // 총 금액이 음수인지 검증
        if (totalAmount <= 0) {
            throw new ApiException(SettlementErrorCode.INVALID_TOTAL_AMOUNT);
        }

        // 정산 참여자가 없는지 검증
        if (participants == null || participants.isEmpty()) {
            throw new ApiException(SettlementErrorCode.NO_PARTICIPANTS);
        }

        Set<Long> memberIds = new HashSet<>();
        long sum = 0;
        for (SettlementParticipant participant : participants) {
            // 개별 정산 참여자 중 null이 있는 경우
            if (participant == null) {
                throw new ApiException(SettlementErrorCode.INVALID_PARTICIPANT);
            }
            // 개별 정산 참여자 값 검증
            if (participant.memberId() == null || participant.memberId() <= 0) {
                throw new ApiException(SettlementErrorCode.INVALID_MEMBER_ID);
            }
            if (participant.amount() <= 0) {
                throw new ApiException(SettlementErrorCode.INVALID_PARTICIPANT_AMOUNT);
            }
            // 정산 참여자 목록에 중복된 회원이 있는 경우
            if (!memberIds.add(participant.memberId())) {
                throw new ApiException(SettlementErrorCode.DUPLICATED_PARTICIPANTS);
            }
            sum += participant.amount();
        }

        // 총 정산 금액과 참여자별 정산금 합이 안 맞는 경우
        if (sum != totalAmount) {
            throw new ApiException(SettlementErrorCode.INVALID_TOTAL_AMOUNT);
        }
    }

    public record SettlementParticipant(
            Long memberId,
            int amount
    ) {
    }
}
