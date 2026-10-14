package io.nexuspay.core.account.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts", indexes = {
    @Index(name = "idx_accounts_iban", columnList = "iban", unique = true)
})
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "iban", length = 34, nullable = false, unique = true)
    private String iban;

    @Column(name = "owner_name", length = 120, nullable = false)
    private String ownerName;

    @Column(name = "balance", precision = 19, scale = 4, nullable = false)
    private BigDecimal balance;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Account() {}

    public Account(String iban, String ownerName, BigDecimal balance, String currency) {
        this.iban = iban;
        this.ownerName = ownerName;
        this.balance = balance;
        this.currency = currency;
        this.status = "ACTIVE";
        this.createdAt = Instant.now();
    }

    public void debit(BigDecimal amount) {
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds for IBAN: " + this.iban);
        }
        this.balance = this.balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public UUID getAccountId() { return accountId; }
    public String getIban() { return iban; }
    public String getOwnerName() { return ownerName; }
    public BigDecimal getBalance() { return balance; }
    public String getCurrency() { return currency; }
    public String getStatus() { return status; }
    public Long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
}
