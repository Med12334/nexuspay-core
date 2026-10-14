package io.nexuspay.core.db2bridge.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "LEGACY_CORE_LEDGER", indexes = {
    @Index(name = "IDX_DB2_TX_REF", columnList = "TX_REFERENCE_CODE"),
    @Index(name = "IDX_DB2_BOOKING_DT", columnList = "BOOKING_DATE")
})
public class LegacyLedgerRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LEDGER_SEQ_ID")
    private Long ledgerSeqId;

    @Column(name = "TX_REFERENCE_CODE", length = 36, nullable = false, unique = true)
    private String txReferenceCode;

    @Column(name = "SOURCE_IBAN", length = 34, nullable = false)
    private String sourceIban;

    @Column(name = "TARGET_IBAN", length = 34, nullable = false)
    private String targetIban;

    @Column(name = "AMOUNT", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Column(name = "CURRENCY", length = 3, nullable = false)
    private String currency;

    @Column(name = "BOOKING_DATE", nullable = false)
    private Instant bookingDate;

    @Column(name = "AUDIT_STATUS", length = 12, nullable = false)
    private String auditStatus;

    @Column(name = "MAINFRAME_BATCH_ID", length = 24)
    private String mainframeBatchId;

    public LegacyLedgerRecord() {}

    public LegacyLedgerRecord(String txReferenceCode, String sourceIban, String targetIban,
                              BigDecimal amount, String currency, String auditStatus, String mainframeBatchId) {
        this.txReferenceCode = txReferenceCode;
        this.sourceIban = sourceIban;
        this.targetIban = targetIban;
        this.amount = amount;
        this.currency = currency;
        this.bookingDate = Instant.now();
        this.auditStatus = auditStatus;
        this.mainframeBatchId = mainframeBatchId;
    }

    public Long getLedgerSeqId() { return ledgerSeqId; }
    public String getTxReferenceCode() { return txReferenceCode; }
    public String getSourceIban() { return sourceIban; }
    public String getTargetIban() { return targetIban; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public Instant getBookingDate() { return bookingDate; }
    public String getAuditStatus() { return auditStatus; }
    public String getMainframeBatchId() { return mainframeBatchId; }
}
