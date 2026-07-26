# Legacy Migration Matrix

Dokumen ini memetakan strategi migrasi modul-modul *legacy* yang sudah ada ke dalam struktur arsitektur baru. Selama proses migrasi, fitur baru **TIDAK BOLEH** dibuat pada modul legacy; hanya *bug fixing* yang diizinkan. Penghapusan akan dilakukan secara bertahap (incremental) dan dapat diuji.

| Legacy Module | Target Module | Status | Priority | Reason & Migration Notes |
|---------------|---------------|--------|----------|--------------------------|
| `core/network` | `core/data/network` | Planned | High | Penyatuan infrastruktur; Menghindari *gradle module explosion*. |
| `core/storage` | `core/data/storage` | Planned | Medium | Infrastruktur penyimpanan yang terpusat. |
| `core/security` | `core/data/security` | Planned | Medium | Infrastruktur keamanan terpusat. |
| `core/logging` | `core/data/logger` | Planned | Low | Infrastruktur logging terpusat. |
| `core/theme` | `core/designsystem` | Planned | High | Konsolidasi seluruh warna, font, dan utilitas tema. |
| `core/ui` | `core/designsystem` | Planned | High | Konsolidasi komponen UI terpusat. |
| `core/model` | `core/domain/model` | Planned | Critical | *Ubiquitous Language*: Domain object harus murni (Pure Kotlin). |
| `feature/knowledgecard`| `feature/knowledge`| Planned | Medium | *Feature Consolidation*: Bukan fitur mandiri, melainkan bagian dari *Knowledge*. |
