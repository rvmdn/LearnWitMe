# Domain Catalog - LearnWitMe

Dokumen ini mendokumentasikan taksonomi model domain yang ada pada modul `:core:domain` (package `com.learnwitme.core.domain.model`) yang dibangun pada Sprint 1.

---

## 1. Base Contracts (Kontrak Dasar DDD)
*   **`ValueObject`**: Penanda objek nilai murni tanpa identitas unik. Kesamaan nilai didasarkan pada seluruh atributnya.
*   **`Entity<ID>`**: Entitas bisnis yang memiliki identitas unik (`id`) tipe `ID`. Kesamaan didasarkan murni pada ID.
*   **`AggregateRoot<ID>`**: Entitas khusus yang bertindak sebagai gerbang tunggal untuk sekumpulan entitas terkait dan menjaga konsistensi transaksi/invariant bisnis.

---

## 2. Strongly Typed Identifiers (Identifier Value Objects)
Inline class `@JvmInline value class` pembungkus string mentah guna memastikan keandalan kompilasi:
*   `UserId`
*   `KnowledgeCardId`
*   `LearningUnitId`
*   `QuizId`
*   `QuizResultId`
*   `LearningSessionId`
*   `LearningRoadmapId`
*   `LearningMemoryId`
*   `KnowledgePackageId`

---

## 3. Value Objects (Objek Nilai)
*   `Tag`: Label pemetaan topik (string tanpa spasi berlebih).
*   `Reference`: Rujukan tautan eksternal (judul dokumen dan URL opsional).
*   `Topic`: Topik materi (menggunakan kode berawalan `tp_`).
*   `SubTopic`: Sub-topik materi (menggunakan kode berawalan `sub_`).
*   `CareerPath`: Pilihan track karir (menggunakan kode berawalan `cp_`).
*   `KnowledgeScore`: Skor pemahaman topik pengguna (berkisar antara `0.0..1.0`).
*   `ReviewSchedule`: Jadwal pengulangan spaced repetition (ease factor minimal `1.3`).
*   `LearningProgress`: Metrik performa akumulatif pengguna.
*   `LearningGoal`: Rencana sasaran belajar pengguna.
*   `Recommendation`: Rekomendasi kartu materi belajar.
*   `LearningPreference`: Preferensi belajar harian dan topik minat.

---

## 4. Domain Entities
*   `KnowledgeCard`: Kartu materi pembelajaran atomik berisi summary, contoh real-world, best practice, referensi, tingkat kesulitan, dan prasyarat.
*   `Quiz`: Kuis dinamis yang menampung daftar pertanyaan (`QuizQuestion`) dan pilihan jawaban (`QuizOption`).
*   `QuizResult`: Riwayat pengerjaan kuis beserta daftar jawaban detail (`QuestionAnswer`).
*   `LearningUnit`: Unit belajar terkecil yang merelasikan satu kartu pengetahuan dengan asesmen kuis opsional.

---

## 5. Aggregate Roots
*   `UserProfile`: Mengatur status kelayakan level belajar pengguna dan preferensi karir.
*   `LearningRoadmap`: Mengatur urutan unit belajar, status penyelesaian, dan lifecycle roadmap belajar.
*   `KnowledgePackage`: Paket pustaka materi belajar (`Topic` dan `KnowledgeCard`) pendukung dengan lifecycle (`Draft -> Published -> Deprecated -> Archived`).
*   `LearningMemory` (Planned): Mengelola kekuatan retensi, stabilitas memori jangka panjang, ulasan adaptif, dan metrik kognitif (didesain pada [LearningMemoryWhitepaper.md](file:///e:/Project/Android/LearnWitMe/docs/ArchitectureBible/LearningMemoryWhitepaper.md)).

---

## 6. Domain Engine Specifications
Kontrak teknis dan arsitektur dependensi dari 12 mesin domain dikelola di [EngineSpecification.md](file:///e:/Project/Android/LearnWitMe/docs/ArchitectureBible/EngineSpecification.md).
