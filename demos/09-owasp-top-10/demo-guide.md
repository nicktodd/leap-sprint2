# Demo: Module 09 — OWASP Top 10

**Duration:** 25 minutes
**Prerequisite:** The [`vulnerable-examples/`](../../labs/09-owasp-top-10/vulnerable-examples)
folder from the lab. GitHub Copilot Chat available. This module is longer than most, OWASP is
ten categories deep and worth the time.

## Part 1: What OWASP is, and why it matters here (3 min)

Narration: OWASP (Open Web Application Security Project) is a non-profit that publishes
freely-available security research, most famously the **OWASP Top 10**, a ranked list of the
most critical web application security risks, updated periodically (this module uses the 2021
edition). It's not a compliance checklist invented by Fidelity, it's an industry-standard
reference point, which is exactly why it's worth learning properly rather than treating as a
box to tick. In financial services specifically: PaySprint handles money and personal financial
data, the categories in this list map directly onto real regulatory and reputational risk, not
just abstract "best practice."

Mention up front: **A01 (Broken Access Control), A03 (Injection), and A07 (Identification and
Authentication Failures)** get a full dedicated deep-dive in Sprint 8. Today is conceptual
breadth across all ten, not depth on those three specifically.

## Part 2: Walk the ten categories, briefly (5 min)

Talk through all ten at a glance, using the slide deck's summary slide. Don't linger, this is
priming before the worked examples: A01 Broken Access Control, A02 Cryptographic Failures, A03
Injection, A04 Insecure Design, A05 Security Misconfiguration, A06 Vulnerable and Outdated
Components, A07 Identification and Authentication Failures, A08 Software and Data Integrity
Failures, A09 Security Logging and Monitoring Failures, A10 Server-Side Request Forgery.

## Part 3: Four worked examples, in depth (14 min)

Live-demo these four from `vulnerable-examples/`, roughly 3-4 minutes each. For each: read the
code together, ask the room to name the category before you confirm it, then ask Copilot Chat
to explain the risk, and critique its explanation before accepting it.

### A03 Injection — `03-transaction-search.java`

The SQL query is built by string concatenation. Ask Copilot: *"What could go wrong if
merchantName contains a single quote?"* Narration: a merchant name of
`' OR '1'='1` turns the WHERE clause into something that matches every row. The fix is a
parameterised query (`PreparedStatement`), never string concatenation with user input, full
stop.

### A05 Security Misconfiguration — `05-Dockerfile`

Point out three separate problems in one file: a remote debug port left open in production
(`5005`), a full JDK image where Module 06 taught a JRE-only runtime stage is enough, and a
database password sitting in plain text as an `ENV` line, visible to anyone who can run `docker
inspect` on the container. Tie back to Module 06: this is exactly the kind of thing a
least-privilege, minimal-base-image Dockerfile avoids by construction.

### A06 Vulnerable and Outdated Components — `06-pom-dependencies.xml`

Recognise `jackson-databind:2.9.8`, this is the same CVE-2019-12384 from Module 05's pipeline
log. Narration: that failure wasn't an arbitrary teaching example, this is precisely the OWASP
category it belongs to, and precisely the kind of thing a Security Scan pipeline stage exists to
catch automatically, before a human ever has to spot it by eye.

### A10 Server-Side Request Forgery — `10-avatar-fetch.java`

The server fetches whatever URL the customer supplies. Ask Copilot to explain what an attacker
could do with this. Narration: a customer could supply a URL pointing at an internal-only
service (for example, a cloud provider's metadata endpoint, which often has no
authentication and can leak credentials), and the server, not the attacker's own browser,
makes that request from inside the network. This is what makes SSRF distinct from a normal
client-side request forgery risk.

## Part 4: Handing off to the lab (3 min)

Narration: you've seen four worked in depth. The lab has all ten, including the four you just
saw, work through the remaining six with a partner the same way, name the category, explain the
risk, propose a fix, before checking the model answers.

## Key message

Every one of these ten categories has a recognisable shape once you've seen it named once.
That's the actual goal of this module, not memorising OWASP's numbering, but building the
pattern recognition to say "that looks like an injection risk" or "that looks like broken access
control" on sight, in code you've never seen before.
