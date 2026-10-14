package io.nexuspay.core.account.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nexuspay.core.account.entity.Account;
import io.nexuspay.core.account.entity.OutboxEvent;
import io.nexuspay.core.account.repository.AccountRepository;
import io.nexuspay.core.account.repository.OutboxEventRepository;
import io.nexuspay.core.events.TransactionEvent;
import io.nexuspay.core.events.TransactionEvent.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AccountTransferService {

    private static final Logger log = LoggerFactory.getLogger(AccountTransferService.class);

    private final AccountRepository accountRepository;
    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public AccountTransferService(AccountRepository accountRepository,
                                  OutboxEventRepository outboxRepository,
                                  KafkaTemplate<String, TransactionEvent> kafkaTemplate,
                                  ObjectMapper objectMapper) {
        this.accountRepository = accountRepository;
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "tx-initiated", groupId = "account-service-group")
    @Transactional
    public void processInitiatedTransfer(TransactionEvent event) throws Exception {
        log.info("[POSTGRES-ACCOUNT] Validating & executing transfer {}", event.transactionId());

        Account source = accountRepository.findByIban(event.sourceIban())
                .orElseThrow(() -> new IllegalArgumentException("Source IBAN not found: " + event.sourceIban()));
        Account target = accountRepository.findByIban(event.targetIban())
                .orElseThrow(() -> new IllegalArgumentException("Target IBAN not found: " + event.targetIban()));

        source.debit(event.amount());
        target.credit(event.amount());

        accountRepository.save(source);
        accountRepository.save(target);

        TransactionEvent validatedEvent = new TransactionEvent(
            event.transactionId(),
            event.sourceIban(),
            event.targetIban(),
            event.amount(),
            event.currency(),
            TransactionStatus.VALIDATED,
            "NONE",
            Instant.now()
        );

        // Save to PostgreSQL Outbox table in the exact same ACID transaction
        OutboxEvent outbox = new OutboxEvent(
            "TRANSACTION",
            event.transactionId().toString(),
            "tx-validated",
            objectMapper.writeValueAsString(validatedEvent)
        );
        outboxRepository.save(outbox);
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void relayOutboxEventsToKafka() throws Exception {
        List<OutboxEvent> pending = outboxRepository.findTop50ByProcessedFalseOrderByCreatedAtAsc();
        for (OutboxEvent item : pending) {
            TransactionEvent payload = objectMapper.readValue(item.getPayload(), TransactionEvent.class);
            kafkaTemplate.send(item.getEventType(), item.getAggregateId(), payload);
            item.markProcessed();
            outboxRepository.save(item);
            log.info("[OUTBOX-RELAY] Published event {} to topic {}", item.getAggregateId(), item.getEventType());
        }
    }
}
