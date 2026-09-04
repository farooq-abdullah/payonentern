# Git command practice checklist

Run each command from `C:\intern shinanigins\servlet-learning` and write a short note beside the result if useful.

```powershell
git status
git branch
git log --oneline --decorate -5
git show --stat HEAD
git diff HEAD~1..HEAD
```

When you edit `my-notes.md` or `release-notes-draft.md`, use this sequence:

```powershell
git status
git diff
git add practice\git-lab\FILE-NAME.md
git diff --staged
git commit -m "Describe the real practice change"
git push
```
