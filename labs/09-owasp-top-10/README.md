# Module 09 Lab — Find the OWASP Top 10, in the Wild

## Objectives

By the end of this lab you will have:

- Reviewed ten short code/config/design examples, each exhibiting one OWASP Top 10 (2021)
  category
- Identified the category, explained the real-world risk, and proposed a fix for each
- Discussed your reasoning with a partner before checking the model answers
- Understood which categories (A01, A03, A07) get a full deep-dive in Sprint 8

## Setup

- The [`vulnerable-examples/`](vulnerable-examples) folder in this lab, ten files, each a short
  excerpt from the fictional PaySprint Mobile codebase
- GitHub Copilot Chat
- A partner

## Task sheet

Work through all ten files. Four of them (`03`, `05`, `06`, `10`) were demoed live, the
remaining six are new. For **every** file, write down:

1. **Which OWASP Top 10 (2021) category** it belongs to (A01-A10)
2. **What the real-world risk is**, in plain English, as if explaining to a non-technical
   stakeholder
3. **A proposed fix**, specific enough that a developer could act on it

Discuss each one with your partner before you both look at the model answers, comparing notes
is more valuable than reaching the same conclusion silently.

**The ten files:**

- `01-account-lookup.java`
- `02-password-storage.java`
- `03-transaction-search.java`
- `04-password-reset-flow.md`
- `05-Dockerfile`
- `06-pom-dependencies.xml`
- `07-login-endpoint.java`
- `08-update-checker.java`
- `09-login-error-handling.java`
- `10-avatar-fetch.java`

## A note on using GenAI here

Feel free to ask Copilot Chat to help explain any file you're stuck on, this is still a
learning-aid use case. But identify the category and articulate the risk in your own words
before checking the model answer, the exercise is in the recognition, not in collecting an AI's
output.

## Acceptance criteria

- All ten files have a named OWASP category, a plain-English risk explanation, and a proposed
  fix, written down (not just discussed verbally).
- You can explain, for at least three of the ten, *why* it's that specific category and not a
  neighbouring one (for example, why `07-login-endpoint.java` is an authentication failure and
  not broken access control).
- You've compared your answers against the model answers and noted anywhere you disagreed or
  were unsure.

If you finish early, pick one file and sketch the actual code fix, not just a description of
what should change.
