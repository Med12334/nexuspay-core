package io.nexuspay.core.account.controller;

import io.nexuspay.core.account.entity.Account;
import io.nexuspay.core.account.repository.AccountRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@CrossOrigin(origins = "*")
public class AccountController {

    private final AccountRepository accountRepository;

    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping
    public List<Account> listAllAccounts() {
        return accountRepository.findAll();
    }

    @PostMapping
    public Account createAccount(@RequestBody Account account) {
        return accountRepository.save(new Account(
            account.getIban(),
            account.getOwnerName(),
            account.getBalance(),
            account.getCurrency()
        ));
    }
}
