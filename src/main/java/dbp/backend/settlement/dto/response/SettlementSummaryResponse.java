package dbp.backend.settlement.dto.response;

import java.time.LocalDate;

public record SettlementSummaryResponse(
        Long settlementId,
        String title,
        int totalAmount,
        LocalDate spentAt,
        String paidByMemberNickname,
        boolean completed
) {
}
