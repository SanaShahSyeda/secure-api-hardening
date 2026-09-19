# Git Tags — What They Are and How to Create One

## What is a tag?

A tag is a permanent, named bookmark pointing at one specific commit. Unlike a branch (which keeps moving forward as you commit), a tag stays fixed on that exact commit forever; it's a snapshot label.

Common uses:
- Marking a release (v1.0.0, v2.3.1)
- Marking a meaningful project milestone: which is what we're using it for here: v0-vulnerable marks the exact commit where the app is deliberately insecure, before any hardening fixes are applied.

Why this matters for this repo specifically: the README claims "before" and "after" states for each security fix. A tag makes that claim provable; anyone can check out v0-vulnerable and see the exact insecure code, instead of just trusting a written description.

## Two kinds of tags

- *Lightweight tag*: just a name pointing at a commit, nothing else. Created with git tag <name>.
- *Annotated tag*: also stores a message, the tagger's name, and a date, similar to a commit. Created with git tag -a <name> -m "<message>".

Use annotated tags for anything you'll reference later (like this one) — the stored message documents why the tag exists, which a bare lightweight tag can't do.

## Step-by-step: creating the v0-vulnerable tag

*1. Check what's changed and commit it first.*
A tag points at a commit — so the code must be committed before you can tag it.
bash
git status
git diff
git add .
git commit -m "feat: add vulnerable user directory API baseline (v0-vulnerable)"


*2. Create the annotated tag.*
bash
git tag -a v0-vulnerable -m "Baseline: unauthenticated /admin/users + commons-text 1.9 (CVE-2022-42889)"

- -a v0-vulnerable — creates an annotated tag named v0-vulnerable.
- -m "..." — the message stored with the tag, explaining what this snapshot represents. Without -m, git would open an editor and ask you to type one.
- By default, this tags whatever commit HEAD currently points to (the commit you just made in step 1). You can tag an older commit instead by adding its hash at the end: git tag -a v0-vulnerable -m "..." <commit-hash>.

*3. Verify the tag was created correctly.*
bash
git tag -n

- Lists every tag in the repo along with its message (-n shows the annotation message; without it, you'd only see tag names).

bash
git show v0-vulnerable

- Shows the tag's message, the commit it points to, and that commit's diff — useful to confirm it's pointing at the right place before you push it.

*4. Push the tag to GitHub.*
bash
git push origin main
git push origin v0-vulnerable

- git push origin main: pushes your regular commits on the main branch. This does *not* include tags.
- git push origin v0-vulnerable: tags are separate from branches and don't get pushed automatically; you push each tag by name explicitly.
- (Alternative: git push origin --tags pushes all local tags at once; handy later when you have several, but explicit is safer while you only have one or two.)

*5. Confirm it on GitHub.*
On the repo page, use the branch/tag dropdown (or go to github.com/<you>/<repo>/tags) — v0-vulnerable should now appear there, and you can link directly to it, e.g.:
https://github.com/<you>/secure-api-hardening-showcase/tree/v0-vulnerable

## Quick reference

| Command | What it does |
|---|---|
| git tag | List all local tags (names only) |
| git tag -n | List all local tags with their messages |
| git tag -a <name> -m "<msg>" | Create an annotated tag on the current commit |
| git show <tag> | Show the tag's message and the commit/diff it points to |
| git push origin <tag> | Push a single tag to GitHub |
| git push origin --tags | Push all local tags to GitHub |
| git tag -d <tag> | Delete a tag locally (add git push origin :refs/tags/<tag> to also remove it from GitHub) |