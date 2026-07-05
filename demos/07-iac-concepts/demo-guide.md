# Demo: Module 07 — Infrastructure as Code Concepts

**Duration:** 10 minutes
**Prerequisite:** `sample-terraform-config.tf` open. GitHub Copilot Chat available. No Terraform
installation or cloud account needed, this module is conceptual and read-only.

## Part 1: You've already written IaC (2 min)

Narration: Module 06's `docker-compose.yml` was Infrastructure as Code, just for containers
rather than cloud infrastructure. It described the desired end state (two services, a network,
a port mapping) in a version-controlled file, and a tool made that state real. Terraform and
CloudFormation apply exactly the same idea to VMs, networks, storage, and entire cloud
environments.

## Part 2: Why IaC matters (2 min)

- **Repeatability**: the same config produces the same environment, every time, no manual
  clicking through a cloud console
- **Auditability**: every change to infrastructure is a diff in git history, exactly like code
- **Version control**: infrastructure changes go through the same PR review as Module 03/04,
  a second pair of eyes before anything real changes

## Part 3: Declarative vs imperative (2 min)

Narration, pointing at the comparison diagram: an imperative script says exactly *how* to get
somewhere, step by step (create this, then that, then configure this). A declarative config
says *what* the end state should look like, and leaves the tool to work out how to get there,
including figuring out what already exists and what needs to change. Terraform and
CloudFormation are declarative. A shell script that runs `aws` CLI commands in sequence is
imperative.

## Part 4: Reading the sample config with GenAI (3 min)

Open `sample-terraform-config.tf`. Select the whole file and ask Copilot Chat:

```text
Explain what each block in this Terraform file does, in plain English.
```

Narration: read the explanation aloud. It should describe the provider block (which cloud,
which region), the two variables (region, environment), the S3 bucket resource, the versioning
and public-access-block resources attached to it, and the output. Critique it, same habit as
every prior module: does the explanation correctly identify that `block_public_acls` and
friends exist specifically to stop the bucket being made public by accident?

## Part 5: Common tools, briefly (1 min)

- **Terraform**: multi-cloud, declarative, HCL syntax, what this sample config uses
- **CloudFormation**: AWS-native equivalent, declarative, JSON or YAML
- **Ansible**: configuration management, more procedural in how playbooks run, agentless

## Key message

Infrastructure as Code takes the same discipline you already apply to application code, review,
version control, repeatable builds, and applies it to the environments that code runs in. You
don't need to be a Terraform expert to read a config and understand what it's asking for, that's
exactly the skill this lab practises.
