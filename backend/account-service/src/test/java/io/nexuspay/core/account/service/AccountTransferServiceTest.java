package io.nexuspay.core.account.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.nexuspay.core.account.entity.Account;
import io.nexuspay.core.account.entity.OutboxEvent;
import io.nexuspay.core.account.repository.AccountRepository;
import io.nexuspay.core.account.repository.OutboxEventRepository;
import io.nexuspay.core.events.TransactionEvent;
import io.nexuspay.core.events.TransactionEvent.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountTransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private OutboxEventRepository outboxRepository;

    @Mock
    private KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    private AccountTransferService transferService;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        transferService = new AccountTransferService(accountRepository, outboxRepository, kafkaTemplate, mapper);
    }

    @Test
    void shouldDebitSourceCreditTargetAndSaveOutboxEventAtomically() throws Exception {
        String sourceIban = "IT60X0542811101000000123456";
        String targetIban = "IT60X0542811101000000654321";
        Account source = new Account(sourceIban, "Marco Rossi", new BigDecimal("5000.00"), "EUR");
        Account target = new Account(targetIban, "Giulia Bianchi", new BigDecimal("1000.00"), "EUR");

        when(accountRepository.findByIban(sourceIban)).thenReturn(Optional.of(source));
        when(accountRepository.findByIban(targetIban)).thenReturn(Optional.of(target));

        TransactionEvent event = new TransactionEvent(
            UUID.randomUUID(), sourceIban, targetIban,
            new BigDecimal("1500.00"), "EUR", TransactionStatus.INITIATED, "NONE", Instant.now()
        );

        transferService.processInitiatedTransfer(event);

        assertEquals(new BigDecimal("3500.00"), source.getBalance());
        assertEquals(new BigDecimal("2500.00"), target.getBalance());

        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxRepository, times(1)).save(outboxCaptor.capture());
        assertEquals("tx-validated", outboxCaptor.getValue().getEventType());
    }

    @Test
    void shouldRejectTransferWhenInsufficientBalance() {
        String sourceIban = "IT60X0542811101000000123456";
        String targetIban = "IT60X0542811101000000654321";
        Account source = new Account(sourceIban, "Marco Rossi", new BigDecimal("200.00"), "EUR");
        Account target = new Account(targetIban, "Giulia Bianchi", new BigDecimal("1000.00"), "EUR");

        when(accountRepository.findByIban(sourceIban)).thenReturn(Optional.of(source));
        when(accountRepository.findByIban(targetIban)).thenReturn(Optional.of(target));

        TransactionEvent event = new TransactionEvent(
            UUID.randomUUID(), sourceIban, targetIban,
            new BigDecimal("900.00"), "EUR", TransactionStatus.INITIATED, "NONE", Instant.now()
        );

        assertThrows(IllegalStateException.class, () -> transferService.processInitiatedTransfer(event));
        verify(outboxRepository, never()).save(any());
    }
}
