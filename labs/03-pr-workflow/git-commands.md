# Lab 03 — PR Workflow: Git Command Walkthrough

## Bug note in README
The README states "Git 2.49" but as of the lab date the current stable release is 2.47.x.
Git 2.49 does not exist yet — this appears to be a forward-dated version number in the
materials.

---

## Part A — Solo: branch, commit, push, open a PR

```bash
# 1. Copy starter/ into a new local repo and push to GitHub
cp -r starter/ ~/sprint2-pr-exercise
cd ~/sprint2-pr-exercise
git init
git add .
git commit -m "chore: initial project skeleton"
git branch -M main
git remote add origin https://github.com/<your-username>/sprint2-pr-exercise.git
git push -u origin main

# 2. Create a feature branch
git switch -c feat/add-greeting-method

# 3. Make a small, clear change to Main.java, then commit with Conventional Commits format
git add src/main/java/com/neueda/leap/Main.java
git commit -m "feat: add greeting method to Main"

# 4. Push the branch and open a PR via the GitHub UI (or gh CLI)
git push -u origin feat/add-greeting-method
gh pr create \
  --title "feat: add greeting method to Main" \
  --body "## What changed
Added a \`greet(String name)\` helper method to Main.java.

## Why
Demonstrates a conventional commit and a minimal PR for the lab exercise.

## How to verify
Run \`mvn test\` — all existing tests should still pass."
```

---

## Part B — With your partner: review and merge

```bash
# 5. Add partner as reviewer on your PR
gh pr edit --add-reviewer <partner-github-username>

# Partner reviews, leaves a comment, approves, then:
# 7. Merge the approved PR (via GitHub UI "Squash and merge", or CLI)
gh pr merge --squash
```

---

## Part C — Branch protection

Navigate to: GitHub repo → Settings → Branches → Add branch protection rule

- Branch name pattern: `main`
- Check: "Require a pull request before merging"
- Check: "Require approvals" → set minimum to **1**
- Check: "Require status checks to pass before merging" (once CI is wired up from Lab 08)
- Check: "Do not allow bypassing the above settings"

**Why these rules matter**: Without them, anyone with write access can push directly to `main`,
bypassing review. The approval requirement ensures a second pair of eyes on every change before
it reaches the protected branch.

---

## Part D — In pairs: induce and resolve a merge conflict via a PR

```bash
# 9. Both partners branch from the same commit on main
git switch main && git pull
git switch -c feat/change-greeting-alice   # Partner A
# Partner B does: git switch -c feat/change-greeting-bob

# 10. Both edit the same line in Main.java to something different, commit, push, open a PR
git add src/main/java/com/neueda/leap/Main.java
git commit -m "feat: update greeting to say Hello Alice"
git push -u origin feat/change-greeting-alice
gh pr create --title "feat: update greeting to say Hello Alice" --body "..."

# 11. Partner A's PR merges cleanly first.
# 12. Partner B's PR now shows a conflict on the same line.

# 13. Resolve the conflict locally:
git switch feat/change-greeting-bob
git fetch origin
git merge origin/main
# Git reports CONFLICT in Main.java — open the file, remove <<<<<<<, =======, >>>>>>> markers
# Choose the correct final text, then:
git add src/main/java/com/neueda/leap/Main.java
git commit -m "chore: resolve merge conflict with main"
git push
# The PR now shows as mergeable → merge it.
```

**Why the second PR conflicted**: Both branches edited the same line from the same base commit.
When Partner A's change was merged into `main`, `main` now contains a different version of that
line than what Partner B's branch was based on. Git cannot automatically decide which change
wins, so it surfaces the conflict for a human to resolve.

---

## Acceptance criteria checklist

- [x] Conventional Commit message format demonstrated (`feat:`, `fix:`, etc.)
- [x] PR description covers what changed, why, and how to verify
- [x] Branch protection rules documented with rationale
- [x] Conflict induction and resolution steps shown end-to-end
- [x] Explanation of why the second PR conflicted written in plain English
