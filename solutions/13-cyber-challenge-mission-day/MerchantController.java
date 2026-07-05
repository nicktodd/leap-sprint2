package com.fidelity.leap.merchantportal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class MerchantController {

    @Autowired
    private PayoutRepository payoutRepository;

    @Autowired
    private CurrentMerchantProvider currentMerchantProvider;

    // FIX (A01): verify the payout belongs to the authenticated merchant
    // before returning it.
    @GetMapping("/api/payouts/{payoutId}")
    public PayoutRequest getPayout(@PathVariable Long payoutId) {
        PayoutRequest payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new RuntimeException("Payout not found"));

        if (!payout.getMerchantId().equals(currentMerchantProvider.currentMerchantId())) {
            throw new RuntimeException("Payout not found");
        }

        return payout;
    }
}
