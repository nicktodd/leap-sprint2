package com.fidelity.leap.paysprint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CurrentUserProvider currentUserProvider;

    // VULNERABILITY: fetches whatever account ID is in the URL, with no check
    // that it belongs to the currently authenticated user.
    @GetMapping("/api/accounts/{accountId}")
    public Account getAccount(@PathVariable Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));
    }
}
