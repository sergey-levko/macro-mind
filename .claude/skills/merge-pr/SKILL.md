---
name: merge-pr
description: Squash and merge a pull request, then pull the latest master. Use when the user wants to merge a PR and sync master.
license: MIT
---

Squash and merge a pull request, then pull the latest master branch.

**Input**: Optionally specify a PR number. If omitted, infer from context (e.g. a PR that was just created in the conversation). If ambiguous, prompt the user.

**Steps**

1. **Resolve PR number**

   If the PR number was not provided and cannot be inferred from context, run:
   ```bash
   gh pr list
   ```
   Use the **AskUserQuestion tool** to let the user select the PR to merge.

2. **Push any local changes to the PR branch**

   Check for uncommitted changes and push them before merging:
   ```bash
   git status
   ```
   If there are staged or unstaged changes, commit and push them to the PR branch:
   ```bash
   git add <relevant files>
   git commit -m "<message>"
   git push origin <branch>
   ```
   If the working tree is clean, skip this step.

3. **Squash and merge the PR**

   ```bash
   gh pr merge <number> --squash
   ```

4. **Switch to master and pull**

   ```bash
   git checkout master && git pull origin master
   ```

5. **Display summary**

   Show a short confirmation:
   - PR number and title that was merged
   - Latest commit on master after the pull
