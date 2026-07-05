package com.fidelity.leap.paysprint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CurrentUserProvider currentUserProvider;

    // FIX (A01): verify the account belongs to the authenticated caller
    // before returning it.
    @GetMapping("/api/accounts/{accountId}")
    public Account getAccount(@PathVariable Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (!account.getOwnerId().equals(currentUserProvider.currentUserId())) {
            throw new NotFoundException("Account not found");
        }

        return account;
    }
}
