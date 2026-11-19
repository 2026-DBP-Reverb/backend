package dbp.backend.settlement.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SettlementDetail(
        Long settlementId,
        Long meetupId,          // 응답엔 안 넣지만, 모임 참여자인지 권한 체크할 때 필요
        String title,
        int totalAmount,
        LocalDate spentAt,
        LocalDateTime createdAt,
        String account,
        String splitMethod,
        Long paidByMemberId,
        String paidByNickname
) {
}
