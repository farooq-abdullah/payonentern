# Git and GitHub Crash Course: safe hands-on practice

This course lives on `learning/git-github-crash-course`, not `main`. You can practice here without changing the application branch.

Your project repository is:

```powershell
cd "C:\intern shinanigins\servlet-learning"
```

## 1. PowerShell navigation first

```powershell
Get-Location                 # show where you are
Get-ChildItem                # list files and folders (same idea as ls)
cd .\folder-name             # enter a child folder
cd ..                        # go up one folder
cd $HOME                     # go to C:\Users\MCC
New-Item -ItemType Directory notes  # create a folder named notes
Get-Content README.md         # print a text file
code .                        # open the current folder in VS Code, if installed
rg "text to find"            # fast search through project text
```

- Press `Tab` to complete or cycle forward through a file/folder name.
- Press `Shift+Tab` to cycle backward.
- Press `Ctrl+C` to cancel an unfinished command. It does not delete files.
- Git commands only work from a repository folder (or a folder inside it). `C:\intern shinanigins` is not the repository; `C:\intern shinanigins\servlet-learning` is.

For this Java/Maven project, these are the most useful build commands:

```powershell
mvn test                      # compile and run tests
mvn package                    # test and build the WAR/package
mvn clean                      # remove generated build output (target folder)
mvn clean package              # clean, test, then build from scratch
```

Run `git status` after builds. The generated `target/` folder should normally be ignored, not committed.

## 2. Git setup, cloning, and starting a repository

Check that Git is installed:

```powershell
git --version
```

Set the name and email that appear on commits once on your computer:

```powershell
git config --global user.name "Farooq Abdullah"
git config --global user.email "your-email@example.com"
git config --global --list
```

To download an existing GitHub repository for the first time:

```powershell
git clone https://github.com/OWNER/REPOSITORY.git
cd REPOSITORY
```

To start Git in a brand-new local coding folder:

```powershell
git init
git add .
git commit -m "Initial project setup"
```

Only use `git init` for a folder that is not already a Git repository. This internship project already has Git, so do not run `git init` inside it.

## 3. The three places Git tracks

```text
Working folder --git add--> Staging area --git commit--> Local history --git push--> GitHub
```

- **Working folder:** files you are editing now.
- **Staging area:** the exact changes selected for the next commit.
- **Local history:** commits stored on your PC.
- **GitHub:** the remote copy shared online.

## 4. Commands you will use every day

Run these from the repository folder:

```powershell
git status                    # what changed, and which branch you are on
git diff                      # unstaged changes only
git add path\to\file          # stage one deliberate file
git add .                     # stage all current changes; check status first
git diff --staged             # review exactly what will be committed
git commit -m "Describe the real change"
git push                      # upload current branch commits
git log --oneline --decorate -10
```

Use `git add <file>` when possible. `git add .` is convenient, but it can accidentally include a file you did not mean to commit.

## 5. Branch commands

```powershell
git branch                                     # list local branches; * marks the current one
git switch main                                # change to main
git switch -c feature/short-description        # create and enter a new branch
git switch learning/git-github-crash-course    # return to this learning branch
git push -u origin feature/short-description   # first push for a new branch
git pull --ff-only                             # safely download a linear update
```

A branch is a separate line of work. Use a feature branch for each real change, test it, then merge it into `main` only when it is ready.

## 6. Useful inspection and comparison commands

These commands answer questions before you change anything. They are safe because they only read Git information.

```powershell
git log --graph --oneline --decorate --all  # see branch history as a graph
git show HEAD                               # inspect the latest commit and its changed code
git show --stat HEAD                        # inspect only the latest commit's file summary
git diff main...HEAD                        # compare your feature branch with main
git diff --name-only main...HEAD            # list only the changed file names
git blame path\to\file                     # see the commit that last changed each line
git grep "search text"                      # search tracked project files
git ls-files                                # list files Git currently tracks
```

`git blame` is for understanding history, not blaming a person. It helps answer: “why does this line exist and which commit introduced it?”

## 7. Remote and GitHub collaboration commands

`origin` is just the usual nickname for the GitHub repository.

```powershell
git remote -v                               # show GitHub fetch/push addresses
git fetch origin                            # download GitHub information; do not change your files
git branch -r                               # list branches that exist on GitHub
git branch -a                               # list local and GitHub branches together
git pull --ff-only                          # fetch + safely update current branch if it can move forward
git push                                    # push current branch after its first push
git push -u origin feature/my-change        # first push; set GitHub tracking branch
git push origin --delete feature/old-name   # delete a remote branch only after it is no longer needed
```

Use `git fetch origin` when you only want to look at what changed online. Use `git pull --ff-only` when you want to bring that safe update into the branch you are currently using.

### Pull requests on GitHub

The normal team workflow is:

```text
main → create feature branch → commit + push feature branch
     → open Pull Request on GitHub → review/test → merge into main
```

If GitHub CLI (`gh`) is installed and you have signed in, these are useful:

```powershell
gh auth status                              # confirm GitHub CLI login
gh repo view --web                          # open this repository in a browser
gh pr create --fill                         # open a Pull Request from current branch
gh pr list                                  # list Pull Requests
gh pr view --web                            # open current branch's Pull Request in browser
```

The website is completely fine too; `gh` is optional.

## 8. Add, remove, rename, and ignore files correctly

```powershell
git add path\to\file                        # stage one existing file
git add -p                                  # choose changes section-by-section (advanced but useful)
git rm path\to\file                         # delete a tracked file and stage its deletion
git mv old-name.txt new-name.txt             # rename a tracked file and stage the rename
git restore path\to\file                    # discard unstaged edits to one file; use carefully
git restore --source=HEAD -- path\to\file   # restore a file to its latest committed version
```

Put files that must never be committed in `.gitignore`, for example:

```gitignore
.env
target/
.idea/
*.log
```

After adding a rule, verify it:

```powershell
git check-ignore -v .env
```

`.gitignore` does not remove a secret that was already committed. If a real secret is ever pushed, change/revoke it immediately.

## 9. Temporarily save unfinished work with stash

Use this only when you need to change branches quickly and your current edits are not ready to commit.

```powershell
git stash push -m "WIP: describe unfinished task"  # hide current tracked edits temporarily
git stash list                                      # list saved temporary work
git stash pop                                       # restore newest stash and remove it from stash list
git stash apply 'stash@{0}'                         # restore a stash but keep its backup
```

Prefer a normal commit when the work is a meaningful checkpoint. A stash is temporary personal storage, not a replacement for pushing work.

## 10. Undo and correction commands

Use the smallest correction that solves the problem.

```powershell
git restore --staged path\to\file                  # unstage a file; keep your edits
git restore path\to\file                           # throw away unstaged edits; be careful
git commit --amend -m "Better message"              # fix latest local, unpushed commit message
git revert COMMIT_HASH                               # make a new commit that safely reverses an older shared commit
```

`git revert` is the usual safe way to undo a commit that is already on GitHub because it preserves the truthful history.

## 11. Merge and conflict commands

```powershell
git switch main
git pull --ff-only
git merge feature/my-change              # combine feature into current branch
git merge --abort                        # cancel a merge only while Git says a merge is in progress
git status                               # Git tells you exactly which files conflict
```

During a conflict, open each marked file, decide the correct final code, remove the conflict markers, test it, then:

```powershell
git add path\to\resolved-file
git commit
```

## 12. Releases and tags

Tags name a specific finished commit, such as a version submitted to a supervisor or deployed to users.

```powershell
git tag                                   # list local tags
git tag -a v1.0.0 -m "First release"     # tag current commit with a message
git push origin v1.0.0                    # upload one tag to GitHub
git show v1.0.0                           # inspect the tagged version
```

Do not move or reuse a release tag after sharing it.

## 13. Exercise 1: make your first real practice commit

1. Confirm you are on this branch:

   ```powershell
   git status
   ```

2. Open `practice\git-lab\my-notes.md` in an editor. Replace the answer placeholders with your own answers.
3. Inspect your change:

   ```powershell
   git status
   git diff
   ```

4. Stage only that file and review it:

   ```powershell
   git add practice\git-lab\my-notes.md
   git diff --staged
   ```

5. Make an honest commit and inspect it:

   ```powershell
   git commit -m "Complete Git practice notes"
   git log --oneline --decorate -5
   ```

6. Upload this branch to GitHub:

   ```powershell
   git push -u origin learning/git-github-crash-course
   ```

## 14. Exercise 2: practice a separate feature branch

This simulates normal development, but does not touch `main`.

```powershell
git switch -c practice/add-a-git-tip
```

Add one useful Git tip to `practice\git-lab\my-notes.md`, then run:

```powershell
git status
git diff
git add practice\git-lab\my-notes.md
git diff --staged
git commit -m "Add a Git learning tip"
git push -u origin practice/add-a-git-tip
```

Compare the practice branch with the course branch:

```powershell
git diff learning/git-github-crash-course...HEAD
```

Then merge the practice branch back into the learning branch:

```powershell
git switch learning/git-github-crash-course
git pull --ff-only
git merge practice/add-a-git-tip
git push
```

Do not run those merge commands while on `main` for this exercise.

## 15. Recover from common safe mistakes

You staged a file but have not committed it:

```powershell
git restore --staged path\to\file
```

This removes it from staging but keeps your edits in the working folder.

You made the latest commit locally with a bad message and have not pushed it:

```powershell
git commit --amend -m "Better commit message"
```

After a commit is pushed, prefer a new correction commit. Do not rewrite shared history unless you understand the consequences.

## 16. Rules that protect your project

- Never commit real passwords, database URLs with credentials, API keys, reset links, or `.env` files with secrets. Commit a `.env.example` with variable names only.
- Never use commits to pretend work happened. A commit should describe a real change.
- Avoid these until you fully understand them:

  ```powershell
  git reset --hard
  git push --force
  git clean -fd
  git commit --allow-empty
  ```

  They can remove work, overwrite shared history, or create misleading history.

## 17. Normal workflow for the real application

```powershell
cd "C:\intern shinanigins\servlet-learning"
git switch main
git pull --ff-only
git switch -c feature/what-you-are-changing

# edit and test the application
git status
git diff
git add path\to\changed-file
git diff --staged
git commit -m "Add concise description of real change"
git push -u origin feature/what-you-are-changing
```

After review, merge the finished feature into `main` and push `main`. Keep `main` stable and deployable.
