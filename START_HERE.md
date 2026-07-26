# Welcome to LearnWitMe

If you are an AI coding assistant or a new engineer:

**STOP.**

Before writing any code, you MUST follow the hierarchy below to establish your context.

## Mandatory Reading Hierarchy

1. `START_HERE.md`
        │
        ▼
2. `PROJECT_STATE.md`
        │
        ▼
3. `docs/AI_DEVELOPMENT_PROTOCOL.md`
        │
        ▼
4. `Architecture Bible (docs/ArchitectureBible/)`
        │
        ▼
5. `Architecture Mapping (docs/ArchitectureMapping.md)`
        │
        ▼
6. `Repository Structure (docs/RepositoryStructure.md)`
        │
        ▼
7. `ADR (docs/ADR/)`
        │
        ▼
8. `Current Sprint (docs/sprint/)`
        │
        ▼
9. `Module README`
        │
        ▼
**IMPLEMENTATION**

## Execution Policy

AI tidak boleh langsung menghasilkan kode tanpa memahami konteks proyek. Sebelum implementasi dimulai, AI **HARUS**:
1. Membaca seluruh dokumen wajib berdasarkan hierarki di atas.
2. Membuat **Context Summary** (lihat AI Development Protocol).
3. Memastikan bahwa Sprint aktif (sesuai `PROJECT_STATE.md`).
4. Memastikan Architecture masih *Frozen*.
5. Memastikan tugas tidak membutuhkan ADR (Architecture Decision Record).

Implementasi hanya boleh dimulai setelah semua langkah di atas selesai divalidasi.
