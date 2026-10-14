package io.nexuspay.core.db2bridge.controller;

import io.nexuspay.core.db2bridge.entity.LegacyLedgerRecord;
import io.nexuspay.core.db2bridge.repository.LegacyLedgerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/db2-ledger")
@CrossOrigin(origins = "*")
public class LegacyAuditController {

    private final LegacyLedgerRepository repository;

    public LegacyAuditController(LegacyLedgerRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/audit")
    public Page<LegacyLedgerRecord> getLedgerAudit(
            @RequestParam(required = false) String iban,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (iban != null && !iban.isBlank()) {
            return repository.findAuditTrailByIban(iban, PageRequest.of(page, size));
        }
        return repository.findAll(PageRequest.of(page, size));
    }
}
