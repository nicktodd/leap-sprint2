# ============================================================
# ANNOTATED TERRAFORM CONFIG — Lab 07
# Source: demos/07-iac-concepts/sample-terraform-config.tf
# ============================================================

# ── terraform block ──────────────────────────────────────────────────────────
# Declares which provider plugins are needed and pins them to a version range.
# "~> 5.0" means "any 5.x but not 6.x" — gives us patch-level updates
# automatically while preventing surprise breaking changes from a major bump.
# Without this, `terraform init` would grab whatever the latest version is,
# which could break the config silently on the next run.
terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

# ── provider block ───────────────────────────────────────────────────────────
# Tells Terraform we're targeting AWS and which region to operate in.
# Using a variable here (rather than hardcoding "eu-west-2") means the same
# config works across regions without edits — useful for DR or multi-region
# deployments.
provider "aws" {
  region = var.aws_region
}

# ── variable: aws_region ─────────────────────────────────────────────────────
# An input the operator can override at plan/apply time.
# The default of "eu-west-2" (London) means it works without any flag if you're
# deploying to the UK; override with -var="aws_region=eu-central-1" for
# Frankfurt, etc. Having a sensible default reduces friction for the common case
# while still making the config reusable.
variable "aws_region" {
  description = "AWS region to deploy PaySprint's static assets bucket into"
  type        = string
  default     = "eu-west-2"
}

# ── variable: environment ────────────────────────────────────────────────────
# A required input — no default, so Terraform will error if it isn't provided.
# Requiring this explicitly prevents accidentally deploying to production when
# you meant staging. The value is embedded in the bucket name (below) and in
# resource tags, making it easy to see at a glance which environment any
# resource belongs to.
variable "environment" {
  description = "Deployment environment name, e.g. dev, staging, production"
  type        = string
}

# ── resource: aws_s3_bucket ──────────────────────────────────────────────────
# Creates an S3 bucket to hold PaySprint's static web assets (JS, CSS, images).
# The bucket name includes ${var.environment} so dev, staging, and production
# each get their own isolated bucket — no risk of a dev deploy clobbering
# production files.
# Tags are used for cost allocation and to identify Terraform-managed resources
# (as opposed to resources created manually, which won't have ManagedBy=terraform).
resource "aws_s3_bucket" "static_assets" {
  bucket = "paysprint-static-assets-${var.environment}"

  tags = {
    Project     = "PaySprint Mobile"
    Environment = var.environment
    ManagedBy   = "terraform"
  }
}

# ── resource: aws_s3_bucket_versioning ───────────────────────────────────────
# Enables S3 versioning on the bucket, meaning every overwritten or deleted
# object gets its previous version preserved rather than permanently lost.
# For static assets this means a bad deploy (corrupted JS, broken image) can be
# rolled back by restoring the previous object version, without needing a
# full re-deploy.
# References the bucket by its Terraform ID (aws_s3_bucket.static_assets.id)
# rather than hardcoding the bucket name — Terraform resolves this dependency
# automatically and creates the bucket first.
resource "aws_s3_bucket_versioning" "static_assets_versioning" {
  bucket = aws_s3_bucket.static_assets.id

  versioning_configuration {
    status = "Enabled"
  }
}

# ── resource: aws_s3_bucket_public_access_block ──────────────────────────────
# Locks down all four public-access vectors on the bucket, acting as a safety
# net even if someone later accidentally sets a permissive bucket policy or ACL.
#
# AWS verification (Terraform provider docs, hashicorp/aws resource
# "aws_s3_bucket_public_access_block"): all four settings must be true to fully
# block public access:
#   block_public_acls       — ignores PUT requests that would add a public ACL
#   block_public_policy     — rejects bucket policies that grant public access
#   ignore_public_acls      — treats any existing public ACLs as non-existent
#   restrict_public_buckets — prevents public access via bucket-level settings
#
# Why this matters: S3 buckets have been the source of numerous high-profile
# data leaks (Capital One, GoDaddy, etc.) because public access was left on
# by default or set accidentally. Blocking it explicitly here means even if
# a future operator makes a mistake in an ACL or policy, this block prevents
# the data from becoming public.
resource "aws_s3_bucket_public_access_block" "static_assets_block" {
  bucket = aws_s3_bucket.static_assets.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# ── output: bucket_name ──────────────────────────────────────────────────────
# Prints the actual bucket name after apply. Useful because the name contains
# ${var.environment}, which is only resolved at runtime. The output lets a
# downstream CI step capture the bucket name with `terraform output bucket_name`
# and use it to upload assets without having to reconstruct the name formula.
output "bucket_name" {
  description = "Name of the S3 bucket created for static assets"
  value       = aws_s3_bucket.static_assets.bucket
}
