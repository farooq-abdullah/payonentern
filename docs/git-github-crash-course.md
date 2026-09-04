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
```

- Press `Tab` to complete or cycle forward through a file/folder name.
- Press `Shift+Tab` to cycle backward.
- Press `Ctrl+C` to cancel an unfinished command. It does not delete files.
- Git commands only work from a repository folder (or a folder inside it). `C:\intern shinanigins` is not the repository; `C:\intern shinanigins\servlet-learning` is.

## 2. The three places Git tracks

```text
Working folder --git add--> Staging area --git commit--> Local history --git push--> GitHub
```

- **Working folder:** files you are editing now.
- **Staging area:** the exact changes selected for the next commit.
- **Local history:** commits stored on your PC.
- **GitHub:** the remote copy shared online.

## 3. Commands you will use every day

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

## 4. Branch commands

```powershell
git branch                                     # list local branches; * marks the current one
git switch main                                # change to main
git switch -c feature/short-description        # create and enter a new branch
git switch learning/git-github-crash-course    # return to this learning branch
git push -u origin feature/short-description   # first push for a new branch
git pull --ff-only                             # safely download a linear update
```

A branch is a separate line of work. Use a feature branch for each real change, test it, then merge it into `main` only when it is ready.

## 5. Exercise 1: make your first real practice commit

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

## 6. Exercise 2: practice a separate feature branch

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

## 7. Recover from common safe mistakes

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

## 8. Rules that protect your project

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

## 9. Normal workflow for the real application

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
