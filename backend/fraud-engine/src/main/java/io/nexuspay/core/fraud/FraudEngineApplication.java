package io.nexuspay.core.fraud;

import io.nexuspay.core.events.TransactionEvent;
import io.nexuspay.core.events.TransactionEvent.TransactionStatus;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@SpringBootApplication
@RestController
@RequestMapping("/api/v1/fraud-alerts")
@CrossOrigin(origins = "*")
public class FraudEngineApplication {

    private static final BigDecimal HIGH_RISK_THRESHOLD = new BigDecimal("10000.00");
    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;
    private final List<TransactionEvent> flaggedAlerts = new CopyOnWriteArrayList<>();

    public FraudEngineApplication(KafkaTemplate<String, TransactionEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(FraudEngineApplication.class, args);
    }

    @KafkaListener(topics = "tx-initiated", groupId = "fraud-engine-group")
    public void evaluateTransactionRisk(TransactionEvent event) {
        if (event.amount().compareTo(HIGH_RISK_THRESHOLD) > 0) {
            TransactionEvent flagged = new TransactionEvent(
                event.transactionId(),
                event.sourceIban(),
                event.targetIban(),
                event.amount(),
                event.currency(),
                TransactionStatus.FRAUD_FLAGGED,
                "AML_THRESHOLD_EXCEEDED_10K_EUR",
                Instant.now()
            );
            flaggedAlerts.add(0, flagged);
            kafkaTemplate.send("tx-fraud-flagged", event.transactionId().toString(), flagged);
        }
    }

    @GetMapping
    public List<TransactionEvent> getLiveFraudAlerts() {
        return flaggedAlerts;
    }
}
