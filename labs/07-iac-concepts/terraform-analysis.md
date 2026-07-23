# Lab 07 — Terraform: Analysis and Answers

## Declarative vs imperative

This file is declarative because it describes **what** the infrastructure should look like
(an S3 bucket named `paysprint-static-assets-<env>`, versioning on, all public access blocked),
not **how** to achieve that state. Terraform itself works out the sequence of API calls needed.

An imperative equivalent — a shell script using `aws` CLI commands — would have to:
- Check whether the bucket already exists before trying to create it (to avoid an error).
- Handle partial failures (e.g., bucket created but versioning block failed — what do you do
  on re-run?).
- Implement its own idempotency logic so running the script twice doesn't create duplicates or
  error out.
- Track every resource it created so it can delete them again later.

Terraform handles all of that automatically: `plan` computes the diff between the declared
state and actual AWS state; `apply` makes the minimum changes needed to reach the declared
state; `destroy` knows which resources to tear down because they're in the state file.

## CI/CD connection: why gate `apply` behind a human approval

`terraform plan` is read-only and safe to run automatically on every PR — it shows what would
change without touching anything. `terraform apply` is destructive: it can delete production
resources, change security settings, or incur significant cost.

A human approval gate before `apply` gives an operator a chance to read the plan output and
confirm that "destroy 1 resource / create 1 resource" means what they think it means — for
example, catching that renaming a variable would cause Terraform to delete the old bucket and
create a new one (losing all data), rather than renaming it in place. Application deploys
rarely have this risk profile; infrastructure changes do.

## Verified claims

1. **`aws_s3_bucket_public_access_block` controls four independent access vectors** —
   confirmed in the Terraform AWS provider documentation for `aws_s3_bucket_public_access_block`.
   Each of the four boolean arguments maps to a distinct AWS S3 account/bucket-level setting.

2. **`~> 5.0` version constraint** — confirmed in the Terraform language reference
   (https://developer.hashicorp.com/terraform/language/expressions/version-constraints):
   `~> 5.0` allows `5.x` but not `6.0`. This is the "pessimistic constraint operator."

## Stretch: adding a read-only IAM role for the bucket

```hcl
resource "aws_iam_role" "static_assets_reader" {
  name = "paysprint-static-assets-reader-${var.environment}"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "ec2.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy" "static_assets_read_policy" {
  role = aws_iam_role.static_assets_reader.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = ["s3:GetObject", "s3:ListBucket"]
      Resource = [
        aws_s3_bucket.static_assets.arn,
        "${aws_s3_bucket.static_assets.arn}/*"
      ]
    }]
  })
}
```

This gives EC2 instances that assume the role read-only access (GetObject, ListBucket) to the
static assets bucket — no write or delete permissions.
