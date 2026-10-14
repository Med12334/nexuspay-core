package io.nexuspay.core.db2bridge.kafka;

import io.nexuspay.core.db2bridge.entity.LegacyLedgerRecord;
import io.nexuspay.core.db2bridge.repository.LegacyLedgerRepository;
import io.nexuspay.core.events.TransactionEvent;
import io.nexuspay.core.events.TransactionEvent.TransactionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Db2LedgerEventConsumerTest {

    @Mock
    private LegacyLedgerRepository repository;

    @Mock
    private KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    @InjectMocks
    private Db2LedgerEventConsumer consumer;

    @Test
    void shouldSkipDuplicateBookingWhenTransactionAlreadyExistsInDb2() {
        UUID txId = UUID.randomUUID();
        TransactionEvent event = new TransactionEvent(
            txId, "IT60SOURCE", "IT60TARGET",
            new BigDecimal("500.00"), "EUR", TransactionStatus.VALIDATED, "NONE", Instant.now()
        );

        when(repository.findByTxReferenceCode(txId.toString()))
            .thenReturn(Optional.of(new LegacyLedgerRecord()));

        consumer.consumeValidatedTransaction(event);

        verify(repository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }
}
