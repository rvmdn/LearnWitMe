# Repository Structure (v1.4 - Legacy Alignment)

Dokumen ini memetakan struktur fisik repositori LearnWitMe dan prinsip-prinsip kesehatan *(health)* repositori. Struktur ini dirancang untuk mematuhi kaidah **Android Multi-Module** sekaligus merepresentasikan batas logis yang ditetapkan dalam *Architecture Bible v1.1*.

## 1. Directory Tree

```text
LearnWitMe/
├── app/                  # Android Entry Point (DI, NavGraph, Manifest)
├── core/                 # Platform Core Modules
│   ├── common/           # Utilitas umum platform
│   ├── data/             # Infrastructure layer (network, db, cache, dll)
│   ├── designsystem/     # Konsolidasi Tema, UI, Typografi
│   ├── domain/           # Business Logic Core (Pure Kotlin)
│   ├── testing/          # Utilitas Testing
│   └── [Legacy Modules]  # (e.g. analytics, network, model, theme) -> See LegacyMigration.md
├── developer/            # Perkakas Developer Internal
├── docs/                 # Documentation Hub (Source of Truth)
├── feature/              # Android Feature Modules (UI/UX)
│   ├── home/
│   ├── knowledge/
│   ├── quiz/
│   ├── review/
│   └── [Legacy Modules]  # (e.g. dailyfeed, knowledgecard) -> See LegacyMigration.md
├── PROJECT_STATE.md      # Dasbor Status Proyek
└── START_HERE.md         # Pintu Gerbang Developer & AI
```

## 2. Core Domain Structure Clarification
Modul `core/domain` menjadi **satu-satunya** tempat bagi:
- Model
- Engine
- Event
- Repository Contract
- UseCase (jika diperlukan)
- *Ubiquitous Language*

**Aturan Emas:** Domain Object baru **TIDAK BOLEH** dibuat di `core/model`, `feature/*`, ataupun `app`.

## 3. Eliminate Duplicate Responsibility
Jika dua modul fisik memiliki tanggung jawab yang sama (misal `core/network` dan `core/data`), repositori harus menentukan:
- **Primary Owner** (Target utama)
- **Secondary Owner** (Legacy)
- **Migration Plan**
Tidak boleh ada dua owner permanen untuk satu entitas sistem.

## 4. Design System & Feature Consolidation
- **Design System Consolidation**: Seluruh entitas Theme, Typography, Color, Component, dan UI Utility wajib di-migrasikan menuju satu wadah utuh: `core/designsystem`.
- **Feature Consolidation**: Fitur terpisah (seperti `feature/knowledgecard`) hanya dipertahankan apabila memiliki siklus hidup (*lifecycle*) dan kapabilitas bisnis yang mandiri. Komponen UI presentasional murni harus dilebur kembali (misal `knowledgecard` dilebur ke dalam `feature/knowledge`).

## 5. Repository Health Checklist
Repositori ini dianggap **sehat** apabila memenuhi syarat berikut:
- [ ] Tidak ada duplicate responsibility
- [ ] Tidak ada orphan module
- [ ] Tidak ada circular dependency
- [ ] Legacy module memiliki target migrasi yang jelas
- [ ] Stable module memiliki *owner*
- [ ] Dokumentasi tersinkronisasi secara akurat

## 6. Migration Policy
Proses migrasi terhadap modul *legacy* terikat pada aturan ketat:
- **Tidak diperbolehkan:** Migrasi massal/besar sekaligus, *rename* massal tanpa alasan, atau memindahkan *package* hanya demi estetika semata.
- **Wajib:** Seluruh pergerakan migrasi harus *incremental*, dapat diuji (*testable*), dapat ditinjau (*reviewable*), dan mudah dikembalikan (*rollback-friendly*).

## 7. Repository Evolution Notes (Roadmap)
Repositori tidak boleh berubah besar secara massal pada iterasi 1–6 kecuali melalui pengajuan **ADR**.

- **Sprint 1**: Repository Stabilization
- **Sprint 2**: Domain Foundation
- **Sprint 3**: Knowledge Engine
- **Sprint 4**: Progress Engine
- **Sprint 5**: Review Engine
- **Sprint 6**: Recommendation Engine
- **Sprint 7**: Android Integration
- **Sprint 8**: AI Integration
