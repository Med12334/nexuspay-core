package io.nexuspay.core.db2bridge.repository;

import io.nexuspay.core.db2bridge.entity.LegacyLedgerRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LegacyLedgerRepository extends JpaRepository<LegacyLedgerRecord, Long> {

    Optional<LegacyLedgerRecord> findByTxReferenceCode(String txReferenceCode);

    @Query("SELECT l FROM LegacyLedgerRecord l WHERE l.sourceIban = :iban OR l.targetIban = :iban ORDER BY l.bookingDate DESC")
    Page<LegacyLedgerRecord> findAuditTrailByIban(@Param("iban") String iban, Pageable pageable);
}
