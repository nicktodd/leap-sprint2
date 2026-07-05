# Demo: Module 12 — SRE Principles Introduction

**Duration:** 10 minutes
**Prerequisite:** none. Conceptual, with one worked calculation.

## Part 1: What SRE is (2 min)

Narration: Site Reliability Engineering treats operating software as an engineering problem,
not a separate "ops" function bolted on afterwards. The same rigour applied to writing code
(measurement, automation, iteration) gets applied to keeping it running reliably. At Fidelity,
this means the team that builds a feature shares real accountability for how it behaves in
production, not just for shipping it.

## Part 2: SLI, SLO, SLA (3 min)

Narration, pointing at the diagram: three related but distinct terms, easy to blur together.

- **SLI (Service Level Indicator)**: what you actually measure, e.g. "percentage of requests
  that succeeded in under 200ms"
- **SLO (Service Level Objective)**: the internal target for that measurement, e.g. "99.9% of
  requests succeed in under 200ms, measured over 30 days"
- **SLA (Service Level Agreement)**: a contractual commitment, usually to an external party,
  usually looser than the internal SLO, with real consequences (credits, penalties) if missed

Ask the room: why would a team deliberately set their SLO tighter than their SLA? Answer: so
that if things start slipping, the team notices and acts *before* they're at risk of breaching
an actual contractual commitment.

## Part 3: The error budget, worked (4 min)

Narration: if the SLO is 99.9% availability over 30 days, the **error budget** is the remaining
0.1%, the amount of unreliability the team is explicitly allowed before they've broken their own
promise.

Work the calculation on the whiteboard:

```text
30 days = 43,200 minutes total
SLO: 99.9% availability
Error budget = 0.1% of 43,200 minutes = 43.2 minutes of allowed downtime
```

Narration: this reframes "reliability" from a vague aspiration into a number a team can actually
manage against. If the budget isn't spent, the team has room to take some risk, ship faster,
try something new. If it's already spent (or overspent), the team's priority shifts to
reliability work until they're back in budget. This is the mechanism that balances feature
delivery against reliability, not a values statement, a number.

## Part 4: Toil (1 min)

Narration: toil is manual, repetitive, automatable operational work that scales linearly with
service growth and provides no lasting engineering value, restarting a service by hand every
time it wedges, for example. SRE treats reducing toil as a first-class goal: time spent on toil
is time not spent on the engineering work that would actually prevent the next incident.

## Key message

SLIs, SLOs, and error budgets turn "is this reliable enough" from a feeling into a number a team
can calculate, track, and make real trade-off decisions against, exactly what today's lab
practises.
