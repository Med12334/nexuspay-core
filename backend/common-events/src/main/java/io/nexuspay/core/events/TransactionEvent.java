package io.nexuspay.core.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionEvent(
    UUID transactionId,
    String sourceIban,
    String targetIban,
    BigDecimal amount,
    String currency,
    TransactionStatus status,
    String riskReason,
    Instant timestamp
) {
    public enum TransactionStatus {
        INITIATED,
        VALIDATED,
        FRAUD_FLAGGED,
        DB2_LEDGER_BOOKED,
        COMPLETED,
        FAILED
    }
}
