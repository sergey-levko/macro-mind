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

2. **Squash and merge the PR**

   ```bash
   gh pr merge <number> --squash
   ```

3. **Switch to master and pull**

   ```bash
   git checkout master && git pull origin master
   ```

4. **Display summary**

   Show a short confirmation:
   - PR number and title that was merged
   - Latest commit on master after the pull
