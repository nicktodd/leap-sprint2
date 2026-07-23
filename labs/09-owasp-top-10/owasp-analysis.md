# Lab 09 — OWASP Top 10 (2025): Vulnerable Examples Analysis

> Using OWASP Top 10:2025 edition as specified in the README.

---

## 01 — `01-account-lookup.java`
**Category**: A01: Broken Access Control

**Real-world risk**: Any authenticated user who discovers that account IDs are sequential
integers can request `/api/accounts/1`, `/api/accounts/2`, etc. and read account details
and statements belonging to other customers — classic Insecure Direct Object Reference (IDOR).
In a payment app this means exposing balances, IBANs, and transaction history to anyone who
can log in.

**Fix**: After fetching the account, verify ownership before returning it.
```java
Account account = accountRepository.findById(accountId)
        .orElseThrow(() -> new NotFoundException("Account not found"));
if (!account.getOwnerId().equals(currentUser.getId())) {
    throw new AccessDeniedException("Forbidden");
}
return account;
```

**Why A01 and not A07**: The user is already authenticated — the failure is that the
authorisation check (does this user own this resource?) is missing, not the authentication
step (proving who you are).

---

## 02 — `02-Dockerfile`
**Category**: A05: Security Misconfiguration (jumped from #5 to #2 in 2025 edition)

**Real-world risk**: Three distinct misconfigurations in one file:
1. Uses the full JDK image instead of a JRE-only image — larger attack surface.
2. Hardcodes `DB_PASSWORD=Winter2024!` in the image layer — anyone who pulls or inspects the
   image gets the production database password. This secret is baked into every image layer and
   visible in `docker history`.
3. JDWP debug port (5005) exposed in production — an attacker who can reach that port gets
   full remote code execution on the JVM with no authentication.

**Fix**: Use a JRE-only base image; inject secrets via environment variables at runtime (not
in the Dockerfile); remove the `-agentlib:jdwp` flag and the `EXPOSE 5005` line entirely in
production builds.

---

## 03 — `03-pom-dependencies.xml`
**Category**: A03: Software Supply Chain Failures (formerly "Vulnerable and Outdated Components")

**Real-world risk**: Three pinned-but-outdated dependencies, all with known critical CVEs:
- `jackson-databind 2.9.8` — CVE-2019-12384, RCE via polymorphic typing (seen in Lab 05)
- `log4j 1.2.17` — multiple CVEs including deserialization RCE; this version is End of Life
- `spring-web 5.2.0.RELEASE` — predates numerous Spring Security patches

The comment "Last dependency review: over 3 years ago" and lack of any automated update
checking means vulnerabilities accumulate undetected.

**Fix**: Upgrade to current non-EOL versions; add OWASP Dependency-Check to the build pipeline
(as seen in Lab 05) to fail the build on new CVEs; use Dependabot or Renovate for automated
PR-based updates.

---

## 04 — `04-password-storage.java`
**Category**: A04: Cryptographic Failures

**Real-world risk**: MD5 is a fast, general-purpose hash with no salt. If the database is
compromised, an attacker can crack every user's password in minutes using precomputed rainbow
tables or GPU brute-force — tools like Hashcat process hundreds of millions of MD5 hashes per
second. A financial app's entire user base could be compromised in hours.

**Fix**: Replace `md5()` with a purpose-built password hashing function that is deliberately
slow and includes a per-password salt. In Java, use Spring Security's `BCryptPasswordEncoder`
or `Argon2PasswordEncoder`:
```java
PasswordEncoder encoder = new BCryptPasswordEncoder(12);
String hashed = encoder.encode(rawPassword);
```

---

## 05 — `05-transaction-search.java`
**Category**: A05: Injection (SQL Injection)

**Real-world risk**: The merchant name is concatenated directly into a SQL string. An attacker
submitting `' OR '1'='1` as the merchant name would return every transaction in the database.
With `'; DROP TABLE transactions; --` they could delete all data. In a payment context this
could expose every customer's transaction history to any user who can reach the search endpoint.

**Fix**: Use a parameterised query (PreparedStatement) so the driver handles escaping:
```java
String sql = "SELECT id, merchant_name, amount FROM transactions WHERE merchant_name = ?";
try (PreparedStatement stmt = connection.prepareStatement(sql)) {
    stmt.setString(1, merchantName);
    ResultSet rs = stmt.executeQuery();
    return mapResults(rs);
}
```

**Why A05 and not A01**: The vulnerability is in how user-supplied data is interpolated into
a command (SQL), not in a missing authorisation check on a resource.

---

## 06 — `06-password-reset-flow.md`
**Category**: A06: Insecure Design

**Real-world risk**: Four design-level failures:
1. Security questions are weak — mother's maiden name is publicly discoverable via social media.
2. The system emails the *current* plaintext password, proving passwords are stored reversibly
   (itself a cryptographic failure) and leaks it over email.
3. No rate limiting on security question attempts — an attacker can brute-force the answer.
4. The question and answer cannot be changed — one compromise lasts forever.

**Fix**: This cannot be fixed with code alone — the design must change. Use a token-based
reset: send a one-time, time-limited link to the verified email address; never email or display
the current password; enforce a short TTL on the reset token; rate-limit reset attempts.

**Why A06 and not A07**: The flaw is in the design of the feature, not in a specific
implementation bug. No amount of careful coding of this design produces a secure outcome.

---

## 07 — `07-login-endpoint.java`
**Category**: A07: Authentication Failures

**Real-world risk**: No rate limiting, no delay, no lockout after failed attempts. An attacker
can make unlimited login attempts at full network speed, enabling credential stuffing (trying
leaked username/password lists from other breaches) or brute-force attacks. Combined with the
MD5 password storage from example 04, this is particularly dangerous.

**Fix**: Add exponential backoff after failed attempts, account lockout after N failures (with
a notification to the account owner), and CAPTCHA or IP-based rate limiting at the API
gateway level.

**Why A07 and not A01**: The user is not yet authenticated — the failure is in the
authentication mechanism itself (it can be bypassed through repetition), not in an
authorisation check on a resource.

---

## 08 — `08-update-checker.java`
**Category**: A08: Software or Data Integrity Failures

**Real-world risk**: The app fetches a configuration bundle over plain HTTP with no signature
check or checksum verification, then applies it directly. An attacker who can perform a
man-in-the-middle attack (on the same network, or via DNS poisoning) can replace the config
with a malicious payload and have it applied to every running instance of the app at startup.
In a payment app, this could redirect payments, disable fraud checks, or exfiltrate credentials.

**Fix**: Use HTTPS for the config URL; verify the response against a known public key or
HMAC signature before applying; consider pinning the expected config hash.

---

## 09 — `09-login-error-handling.java`
**Category**: A09: Security Logging and Alerting Failures

**Real-world risk**: Failed logins — including repeated brute-force attempts and IDOR probes
(example 01) — are silently swallowed. The security team has no visibility into attacks in
progress. By the time a breach is discovered (if it ever is), there is no audit trail to
determine what was accessed, when, or from where — making regulatory reporting (GDPR, FCA)
and forensic investigation impossible.

**Fix**: Log every authentication failure with timestamp, IP address, and the email/account
targeted (not the password). Set up alerting on anomalous failure rates (e.g., >10 failures
per minute from a single IP, or >5 failures against the same account in an hour).

---

## 10 — `10-payment-transfer.java`
**Category**: A10: Mishandling of Exceptional Conditions

**Real-world risk**: If `ledger.credit()` fails after `ledger.debit()` has already succeeded,
the catch block logs a warning and reports success. Money has left the source account but
never arrived at the destination — a "lost" transfer. Worse, re-running the batch could
debit the source account again. In a financial system this is both a regulatory risk and a
direct financial loss.

**Fix**: The debit and credit must be atomic. Either use a database transaction that rolls
back the debit if the credit fails, or implement a compensating transaction (re-credit the
source) in the catch block. Never report success if any part of the operation failed:
```java
try {
    ledger.debit(from, amount);
    ledger.credit(to, amount);
} catch (LedgerException e) {
    log.error("Transfer failed, rolling back: {}", e.getMessage());
    // attempt compensating re-credit if debit already applied
    throw new TransferFailedException("Transfer could not be completed", e);
}
// only reach here if both succeeded
notificationService.sendConfirmation(from, to, amount);
return TransferResult.success();
```

---

## 11 — `11-avatar-fetch-ssrf.java`
**Category**: A01: Broken Access Control (SSRF folded into A01 in 2025 edition)

**Real-world risk**: The server fetches whatever URL the customer provides — including internal
addresses like `http://169.254.169.254/latest/meta-data/` (AWS EC2 instance metadata),
`http://localhost:8080/admin`, or internal services not exposed to the internet. An attacker
can use the app as a proxy to enumerate and attack internal infrastructure. In a cloud
environment this could leak IAM credentials from the metadata service, compromising the entire
AWS account.

**Fix**: Validate the URL before fetching: reject non-HTTPS schemes, resolve the hostname and
block private/loopback IP ranges (10.x, 172.16-31.x, 192.168.x, 127.x, 169.254.x), and
restrict to a whitelist of known CDN domains if possible.

**Why A01 (2025) and not its own category**: The 2025 edition treats SSRF as a subtype of
Broken Access Control — the server is accessing internal resources (to which it should not
grant access) on behalf of an external actor. The access control failure is the root cause.

---

## Summary table

| File | OWASP 2025 Category |
|------|---------------------|
| 01-account-lookup.java | A01: Broken Access Control |
| 02-Dockerfile | A05: Security Misconfiguration |
| 03-pom-dependencies.xml | A03: Software Supply Chain Failures |
| 04-password-storage.java | A04: Cryptographic Failures |
| 05-transaction-search.java | A05: Injection |
| 06-password-reset-flow.md | A06: Insecure Design |
| 07-login-endpoint.java | A07: Authentication Failures |
| 08-update-checker.java | A08: Software or Data Integrity Failures |
| 09-login-error-handling.java | A09: Security Logging & Alerting Failures |
| 10-payment-transfer.java | A10: Mishandling of Exceptional Conditions |
| 11-avatar-fetch-ssrf.java | A01: Broken Access Control (SSRF) |
