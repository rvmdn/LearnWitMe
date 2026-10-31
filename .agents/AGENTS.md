# LearnWitMe Project AI Rules

You are an AI working on the LearnWitMe Learning Intelligence Platform.
This repository has an incredibly strict architecture, governance, and development protocol.

CRITICAL RULE:
Before writing any code or suggesting any changes, you MUST read the following files located in the root of the workspace:
1. `START_HERE.md`
2. `PROJECT_STATE.md`
3. `docs/AI_DEVELOPMENT_PROTOCOL.md`

Follow the instructions in those documents perfectly. Do not bypass the Architecture Bible or the Engineering Manifest.

## Conflict Resolution Priority
Jika terdapat konflik, Anda **WAJIB** mengikuti hierarki berikut:
1. Architecture Bible
2. Approved ADR
3. Architecture Mapping
4. Repository Structure
5. Current Sprint
6. Module README
7. Existing Code

**IMPORTANT**: Jika `Existing Code` bertentangan dengan dokumentasi, AI HARUS mengikuti dokumentasi dan melaporkan inkonsistensi. **AI tidak boleh memperbaiki Architecture secara diam-diam.**

## Anti-Yes-Man & Critical Engineering Mandate
- Jangan pernah menyetujui ide/arsitektur yang memiliki celah fatal, performa buruk, atau overengineering hanya demi menyenangkan pengguna.
- Berikan telaah kritis (pros, cons, trade-offs, edge cases, dan alternatif yang lebih robust/simpel) setiap kali mengevaluasi solusi teknis.

## Release, Versioning, Changelog & Git Push Policy

This policy is mandatory for every implementation task that changes the application or its deliverable behavior.

### 1. Semantic Versioning
Use `MAJOR.MINOR.PATCH`:
- **PATCH increment** (`1.0.0` → `1.0.1`): bug fixes, minor corrections, small internal improvements that affect the deliverable.
- **MINOR increment** (`1.0.1` → `1.1.0`): new backward-compatible features, significant capability additions, or meaningful product enhancements.
- **MAJOR increment** (`1.1.0` → `2.0.0`): breaking changes to public contracts, data formats, APIs, architecture guarantees, or compatibility.

Do not classify changes based on effort or number of files. Assess their actual compatibility and user impact.

### 2. Version Source of Truth
- Inspect the existing project versioning mechanism before making changes.
- Maintain one authoritative application version source (`app/build.gradle.kts`: `versionName` and `versionCode`).
- Keep Android `versionName` and `versionCode` consistent with the project's release strategy.
- Increment `versionCode` monotonically for every distributable Android release.
- Do not introduce a second competing version source.
- Never downgrade or reuse an existing release version.

### 3. Changelog
Maintain a root-level `CHANGELOG.md` using a consistent Keep a Changelog-style structure.

For every release:
- Add a new entry at the top.
- Include version and release date (`## [X.Y.Z] - YYYY-MM-DD`).
- Categorize relevant changes under `Added`, `Changed`, `Fixed`, `Security`, and `Breaking Changes`.
- Describe user-visible or developer-relevant changes accurately.
- Include migration notes when a breaking change requires action.
- Preserve all historical entries.
- Do not claim features, fixes, or tests that were not actually implemented or executed.

Documentation-only and internal maintenance changes that do not constitute a product release do not automatically require a version increment. Record them in Git history as appropriate.

### 4. Verification Before Release
Run the applicable checks before creating a release:
- Relevant module tests (`./gradlew :core:domain:test`, etc.).
- Full test suite (`./gradlew test`).
- Android lint (`./gradlew :app:lintDebug`).
- Debug build / release build (`./gradlew :app:assembleDebug`).
- `git diff --check`.

Do not publish a release when required checks fail. Report blockers explicitly.

### 5. Git Commit, Push & Release Tag
After successful verification:
1. Commit changes using Conventional Commits.
2. Push the working branch to its configured remote.
3. Verify that the remote branch points to the expected commit.
4. For an actual version release, create an annotated Git tag in the format `vX.Y.Z`.
5. Push the tag to the remote.
6. Verify the tag points to the intended release commit.

Do not create a release tag for every commit. Do not force-push or rewrite shared history.

### 6. Final Report
Always report:
- Previous version and new version, or explain why no version increment was needed.
- Versioning classification and rationale.
- Changelog entries added.
- Commit SHA and message.
- Branch and remote push status.
- Release tag, when applicable.
- Test, lint, and build results.
- CI status when available.

Never claim that a push, tag, or release succeeded without verifying it against the remote repository.

