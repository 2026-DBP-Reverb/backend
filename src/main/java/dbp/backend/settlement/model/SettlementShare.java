package dbp.backend.settlement.model;

import java.time.LocalDateTime;

public class SettlementShare {

    private Long settlementId;
    private Long memberId;
    private Long meetupId;
    private int amount;
    private PaidStatus paidStatus;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;

    public SettlementShare(Long settlementId, Long memberId, Long meetupId, int amount,
                           PaidStatus paidStatus, LocalDateTime createdAt, LocalDateTime confirmedAt) {
        this.settlementId = settlementId;
        this.memberId = memberId;
        this.meetupId = meetupId;
        this.amount = amount;
        this.paidStatus = paidStatus;
        this.createdAt = createdAt;
        this.confirmedAt = confirmedAt;
    }
}
