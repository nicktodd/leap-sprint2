package com.fidelity.leap.paysprint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CurrentUserProvider currentUserProvider;

    // FIX (A01 — Broken Access Control):
    // After fetching the account, verify that the owner ID matches the currently
    // authenticated user before returning any data. Without this check any
    // authenticated user could enumerate account IDs and read other customers'
    // data (IDOR — Insecure Direct Object Reference).
    @GetMapping("/api/accounts/{accountId}")
    public Account getAccount(@PathVariable Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        Long currentUserId = currentUserProvider.currentUserId();
        if (!account.getOwnerId().equals(currentUserId)) {
            // Return 404, not 403, to avoid leaking whether the account exists.
            throw new NotFoundException("Account not found");
        }

        return account;
    }
}
