package io.nexuspay.core.account.repository;

import io.nexuspay.core.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByIban(String iban);
}
