# Lab 05 — Pipeline Failure Analysis

Source log: `demos/05-devops-cicd-deeper-dive/sample-pipeline-log.txt`

---

## 1. Pipeline stages — pass/fail summary

| Stage           | Result | Notes |
|-----------------|--------|-------|
| Checkout        | PASS   | Commit f3a8c21 "feat: add spending category endpoint" |
| Build           | PASS   | `mvn -B clean package` — 18s, 11 source files compiled |
| Test            | PASS   | 14 unit tests, 0 failures |
| Acceptance Test | PASS   | 8/8 Newman/Postman scenarios against staging |
| Security Scan   | **FAIL** | 1 CRITICAL CVE found in dependency |

**Test stage**: Runs the project's JUnit suite against compiled code in isolation. Checks that
individual classes behave correctly, no database or network required.

**Acceptance Test stage**: Deploys the build to a staging environment and runs Postman
(Newman) scenarios end-to-end, checking that the full API behaves correctly from an external
caller's perspective — closer to what a real user or integration partner would experience.

The key difference: unit tests verify internal logic; acceptance tests verify observable
behaviour of the running system.

---

## 2. The failure — exact error lines

```
[WARN] jackson-databind:2.9.8 has 1 known vulnerability
[WARN] CVE-2019-12384: Polymorphic Typing issue, allows remote code execution
[WARN] Severity: CRITICAL (CVSS 8.1)
[ERROR] Build failed: 1 CRITICAL vulnerability found, threshold is 0 CRITICAL
```

**Tool that produced this output**: OWASP Dependency-Check (`dependency-check`), a
software composition analysis (SCA) tool. It scans the build artefacts' dependency list
against the NVD (National Vulnerability Database) and fails the build when a finding meets or
exceeds the configured severity threshold.

**Specific flag**: `jackson-databind` version 2.9.8 contains CVE-2019-12384 — a Polymorphic
Typing deserialization vulnerability with a CVSS score of 8.1 (CRITICAL). The pipeline is
configured with a threshold of 0 allowed CRITICAL findings.

---

## 3. Verification of CVE-2019-12384

CVE-2019-12384 is documented in the NVD at https://nvd.nist.gov/vuln/detail/CVE-2019-12384.
The vulnerability is a **Polymorphic Typing** flaw in `jackson-databind` before 2.9.9. When
Jackson is configured to enable default typing (`enableDefaultTyping`) or to use certain
subtypes, an attacker can supply a crafted JSON payload that causes Jackson to deserialize an
object of a type the attacker controls — leading to Remote Code Execution if a suitable
"gadget chain" (e.g. via the `logback` JNDI gadget) is present on the classpath. The CVSS 8.1
score reflects network-accessible exploitation with no authentication required, but a required
precondition (specific Jackson configuration). Fixed in `jackson-databind` 2.9.9.1 and later.

---

## 4. Plain-English explanation for a non-technical stakeholder

The build passed all its normal tests but was stopped by an automated security check that
scans the third-party libraries the application depends on. One of those libraries
(`jackson-databind 2.9.8`) has a known, publicly documented flaw that would allow an attacker
to run malicious code on our servers by sending a specially crafted request. Because the
severity was rated Critical, the pipeline is configured to refuse to proceed — the same way a
quality gate on a car assembly line stops the whole line when a safety defect is detected,
rather than letting the car reach the customer.

---

## 5. Connection to branch protection

If `main` required this pipeline to pass before a PR could be merged, the vulnerable
`jackson-databind 2.9.8` dependency could never have reached `main` — the branch protection
rule would have blocked the merge until the CVE was resolved (by upgrading to 2.9.9.1+).

---

## Bonus: CVE-2019-12384 deep-dive

NVD entry confirms CVSS 8.1 (HIGH/CRITICAL), network-exploitable, low complexity once
preconditions are met. Exploitation requires the application to have enabled default typing in
Jackson's `ObjectMapper`. The GitHub Security Advisory (GHSA-4x6m-wq7p-84p9) confirms the fix
landed in 2.9.9.1. The GenAI explanation of "allows remote code execution" and "CRITICAL
severity" is accurate and verified against the NVD record.
