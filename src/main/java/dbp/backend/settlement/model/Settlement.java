package dbp.backend.settlement.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Settlement {

    private Long settlementId;
    private Long meetupId;
    private Long paidByMemberId;
    private String title;
    private int totalAmount;
    private String account;
    private SplitMethod splitMethod;
    private LocalDate spentAt;
    private LocalDateTime createdAt;
    private boolean completed;

    public Settlement(Long settlementId, Long meetupId, Long paidByMemberId,
                      String title, int totalAmount, String account, SplitMethod splitMethod,
                      LocalDate spentAt, LocalDateTime createdAt, boolean completed) {
        this.settlementId = settlementId;
        this.meetupId = meetupId;
        this.paidByMemberId = paidByMemberId;
        this.title = title;
        this.totalAmount = totalAmount;
        this.account = account;
        this.splitMethod = splitMethod;
        this.spentAt = spentAt;
        this.createdAt = createdAt;
        this.completed = completed;
    }
}
