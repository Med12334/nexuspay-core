package io.nexuspay.core.db2bridge.kafka;

import io.nexuspay.core.db2bridge.entity.LegacyLedgerRecord;
import io.nexuspay.core.db2bridge.repository.LegacyLedgerRepository;
import io.nexuspay.core.events.TransactionEvent;
import io.nexuspay.core.events.TransactionEvent.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class Db2LedgerEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(Db2LedgerEventConsumer.class);
    private final LegacyLedgerRepository repository;
    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    public Db2LedgerEventConsumer(LegacyLedgerRepository repository,
                                  KafkaTemplate<String, TransactionEvent> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "tx-validated", groupId = "db2-ledger-group")
    @Transactional
    public void consumeValidatedTransaction(TransactionEvent event) {
        log.info("[IBM-DB2-BRIDGE] Booking transaction {} into Mainframe Ledger", event.transactionId());

        String txRef = event.transactionId().toString();
        if (repository.findByTxReferenceCode(txRef).isPresent()) {
            log.warn("[IBM-DB2-BRIDGE] Idempotency check: Transaction {} already booked", txRef);
            return;
        }

        String batchId = "MF-BATCH-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        LegacyLedgerRecord record = new LegacyLedgerRecord(
            txRef,
            event.sourceIban(),
            event.targetIban(),
            event.amount(),
            event.currency(),
            "BOOKED_OK",
            batchId
        );
        repository.save(record);

        TransactionEvent bookedEvent = new TransactionEvent(
            event.transactionId(),
            event.sourceIban(),
            event.targetIban(),
            event.amount(),
            event.currency(),
            TransactionStatus.DB2_LEDGER_BOOKED,
            "NONE",
            Instant.now()
        );

        kafkaTemplate.send("tx-completed", txRef, bookedEvent);
    }
}
