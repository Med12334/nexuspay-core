package io.nexuspay.core.orchestrator.controller;

import io.nexuspay.core.events.TransactionEvent;
import io.nexuspay.core.events.TransactionEvent.TransactionStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/transactions")
@CrossOrigin(origins = "*")
public class TransactionSagaController {

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;
    private final Map<UUID, TransactionEvent> sagaStateStore = new ConcurrentHashMap<>();

    public TransactionSagaController(KafkaTemplate<String, TransactionEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public record TransferRequest(String sourceIban, String targetIban, BigDecimal amount, String currency) {}

    @PostMapping("/initiate")
    public TransactionEvent initiateTransfer(@RequestBody TransferRequest req) {
        UUID txId = UUID.randomUUID();
        TransactionEvent event = new TransactionEvent(
            txId,
            req.sourceIban(),
            req.targetIban(),
            req.amount(),
            req.currency() != null ? req.currency() : "EUR",
            TransactionStatus.INITIATED,
            "NONE",
            Instant.now()
        );
        sagaStateStore.put(txId, event);
        kafkaTemplate.send("tx-initiated", txId.toString(), event);
        return event;
    }

    @GetMapping
    public Collection<TransactionEvent> getAllSagaStates() {
        return sagaStateStore.values();
    }

    @KafkaListener(topics = {"tx-validated", "tx-fraud-flagged", "tx-completed"}, groupId = "saga-orchestrator-group")
    public void updateSagaState(TransactionEvent event) {
        sagaStateStore.put(event.transactionId(), event);
    }
}
