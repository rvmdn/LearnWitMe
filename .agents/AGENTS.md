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

## Versioning & Changelog Protocol
- Tiap ada perubahan minor/patch (bugfix, sanitasi, hardening): bump patch version (misal: 1.0.1) dan naikkan `versionCode`.
- Tiap ada perubahan major/fitur: bump minor/major version (misal: 1.1.0) dan naikkan `versionCode`.
- Selalu catat detail perubahan dalam `CHANGELOG.md` di root repository mengikuti format Keep a Changelog.
- Upload/push branch dan buat git tag versi terkait (misal: `v1.0.1`).

