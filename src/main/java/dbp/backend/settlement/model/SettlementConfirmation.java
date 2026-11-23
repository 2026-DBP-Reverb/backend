package dbp.backend.settlement.model;

import java.time.LocalDateTime;

public record SettlementConfirmation(PaidStatus status, LocalDateTime confirmedAt) {
}
