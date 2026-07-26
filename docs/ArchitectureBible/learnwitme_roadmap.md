# LearnWitMe - Master Architecture Blueprint (Architecture Bible v1.1)

## Executive Summary
*   **Purpose**: Memberikan panduan rekayasa perangkat lunak skala enterprise untuk platform LearnWitMe.
*   **Vision**: LearnWitMe adalah *Learning Intelligence Platform* jangka panjang (5-10 tahun), bukan sekadar aplikasi Android. Platform ini adalah pendamping adaptif yang membantu insinyur membangun intuisi teknis.
*   **Core Asset**: *Learning Memory Engine*. Nilai tertinggi aplikasi bukan pada model AI, melainkan pada data historis personalisasi pembelajaran (*Forgetting Curve*) setiap pengguna.
*   **Primary Goal**: Membangun *Learning Habit* (kebiasaan belajar) secara konsisten melalui sesi *microlearning*.
*   **Design Philosophy**: *Offline by Default, Platform First, AI as Enhancement*. UI hanyalah lapisan presentasi yang dapat diganti.
*   **Technology Philosophy**: *Clean Architecture, Domain Driven Design, Event Driven Architecture*.
*   **Long Term Direction**: Platform generik yang dapat diekspansi menjadi *Knowledge Marketplace* untuk disiplin ilmu lain di masa depan.

---

## 1. Vision

Visi utama dari arsitektur ini adalah memisahkan seluruh *business logic* ke dalam **Domain Core**. Platform ini didesain agar berbagai klien dapat mengonsumsi Domain yang sama tanpa modifikasi logika.

```text
       [ LearnWitMe Platform ]
                 ↓
          [ Domain Core ]
                 ↓
  -------------------------------
  ↓         ↓          ↓        ↓
Android    Web      Desktop    CLI
```

### 1.1 In Scope
Fitur dan kapabilitas berikut adalah tanggung jawab utama platform LearnWitMe saat ini:
*   Learning Platform & Offline Learning
*   Recommendation Engine & Spaced Repetition
*   Knowledge Package Download
*   AI Enhancement (Generasi kuis, analogi, penyederhanaan)
*   Cross Device Sync

### 1.2 Out of Scope
Fitur berikut secara sengaja tidak dikerjakan pada versi sekarang demi menjaga fokus produk (Daftar ini dapat berubah melalui ADR):
*   Microservice Architecture
*   Knowledge Marketplace (Transaksi jual beli)
*   Real-time Collaboration & Virtual Classroom
*   Plugin SDK Eksternal
*   Enterprise Multi-Tenant
*   AI Model Training / Fine-tuning
*   Live Streaming

---

## 2. Architecture Principles

### 2.1 Product & Deterministic Philosophy
*   **Habit over Features**: Semua keputusan arsitektur dioptimalkan untuk mendukung konsistensi belajar harian.
*   **Scalability Principle**: Domain cukup generik untuk ekspansi lintas keilmuan tanpa merombak arsitektur dasar.
*   **Deterministic Engine Principle**: Semua *Domain Engine* harus deterministik (Input sama ➔ Output sama). AI merupakan *enhancement* dan bukan bagian dari komputasi deterministik. Hal ini direkomendasikan untuk mempermudah *Testing, Debugging, Replay*, dan *Simulation*.

### 2.2 Coding Principles
Standar rekayasa untuk semua penulisan kode sumber:
`SOLID`, `KISS`, `DRY`, `YAGNI`, `Composition over Inheritance`, `Dependency Inversion`, `Immutable Domain Object`, `Pure Function` (pada Domain), `Small Function`, `High Cohesion`, `Low Coupling`.

### 2.3 Module Dependency Rules
Aliran dependensi (*Dependency Flow*) dirancang **Top-Down**:
`Android App` ➔ `Presentation (UI)` ➔ `Domain (The Engine)` ➔ `Repository Interface` ➔ `Infrastructure (Local, Remote, Plugin, AI Gateway)`
*Tidak disarankan memiliki dependency ke atas atau Circular Dependency.*

### 2.4 Data Ownership
Setiap *entity* direkomendasikan hanya memiliki **satu** *owner* (Engine pengelola).
*   **Knowledge Engine** memiliki: `Knowledge Unit`, `Topic`, `Reference`
*   **Quiz Engine** memiliki: `Quiz`, `Quiz Result`
*   **Memory Engine** memiliki: `Retention`, `Memory Curve`
*   **Progress Engine** memiliki: `Progress`, `Completion`, `Streak`

### 2.5 State Machine
Setiap siklus hidup dikelola melalui status yang jelas:
*   **Learning Unit**: `Draft` ➔ `Generated` ➔ `Reviewed` ➔ `Published` ➔ `Deprecated` ➔ `Archived`
*   **Learning Session**: `Started` ➔ `Paused` ➔ `Completed` ➔ `Cancelled`
*   **Quiz**: `Created` ➔ `Started` ➔ `Submitted` ➔ `Reviewed` ➔ `Completed`
*   **Sync**: `Pending` ➔ `Syncing` ➔ `Completed` ➔ `Conflict` ➔ `Retry`

### 2.6 Error Handling Strategy
Semua aliran bisnis utama direkomendasikan menggunakan **Result Pattern** (`Success`, `Failure`). Kategori *Failure* mencakup: `Validation Error`, `Not Found`, `Conflict`, `Retryable Error`, `Fatal Error`.
Penggunaan *Exception* sebagai kontrol *business flow* sebaiknya dihindari.

---

## 3. Domain Architecture

### 3.1 Core Domain Model
Seluruh Domain Engine menggunakan *Domain Model* yang sama (*Ubiquitous Language*). Redundansi model antar *engine* harus dihindari.
*   `LearningUnit`, `KnowledgeCard`, `Quiz`, `QuizResult`
*   `LearningSession`, `LearningGoal`, `LearningRoadmap`
*   `Recommendation`, `KnowledgeScore`, `LearningMemory`, `ReviewSchedule`
*   `KnowledgePackage`, `Topic`, `SubTopic`, `Reference`, `Tag`
*   `CareerPath`, `UserProfile`, `LearningProgress`, `LearningAnalyticsEvent`, `SystemEvent`

### 3.2 Ubiquitous Language
Glosarium istilah resmi proyek:
*   **Learning Unit**: Modul atomik terkecil dari pengetahuan.
*   **Knowledge Card**: Representasi visual/data dari Learning Unit.
*   **Mastery**: Tingkat penguasaan pada suatu topik.
*   **Review**: Proses mengulang materi (Spaced Repetition).
*   **Retention**: Persentase kemampuan memori menyimpan informasi.
*   **Recommendation**: Sugesti materi berikutnya dari engine.
*   **Learning Memory**: Rekam jejak pola retensi dan kelupaan pengguna.
*   **Knowledge Package**: Bundel materi terisolasi (Misal: *Docker Package*).
*   **Learning Session**: Rentang waktu pengguna aktif belajar.
*   **Weak Topic**: Topik dengan *mastery* dan *retention* terendah.
*   **Roadmap**: Peta jalan pembelajaran personal.
*   **Progress**: Persentase penyelesaian kurikulum.
*   **Engine**: Modul pemroses logika bisnis murni.
*   **Provider**: Sumber layanan eksternal.
*   **Plugin**: Ekstensi sistem (*IT, Finance*).
*   **Event**: Sinyal komunikasi inter-*engine*.

### 3.3 The Engines (Learning Intelligence Foundation)
#### 🧠 Core Learning Engines
*   **Knowledge Engine**: Mengelola entitas pengetahuan (*Topic, Tags*).
*   **Content Engine**: Mengelola siklus hidup konten, versi, dan lokalisasi.
*   **Recommendation Engine**: Memilih materi deterministik berdasarkan *Progress, Weak Topic, Goal*.
*   **Progress Engine**: Melacak *Completion, Mastery, Streak*.
*   **Knowledge Score Engine**: Menghitung skor absolut topik (Misal: Backend: 82).
*   **Feed Engine**: Meracik *Daily Feed* secara deterministik (non-random).
*   **Quiz Engine**: Menghasilkan kuis menargetkan *Weak Topic*.
*   **Review Engine**: Mengeksekusi *Spaced Repetition*.
*   **Roadmap Engine**: Menyusun *Tree* pembelajaran adaptif.
*   **Learning Analytics Engine**: Mengelola analitik belajar lokal.
*   **Learning Memory Engine (The Crown Jewel)**: Mencatat pola kelupaan (*Forgetting Curve*) spesifik pengguna.

#### ⚙️ Infrastructure Engines
*   **Sync Engine**: Mengelola *Incremental Sync, Delta Update, Conflict Resolution*.

### 3.4 Engine Dependency Rules
Ketergantungan antar-*engine* (DAG) diarahkan satu jalan, tanpa *Circular Dependency*:
*   `Knowledge Engine` ➔ `Recommendation Engine` ➔ `Feed Engine` ➔ `UI`
*   `Progress Engine` ➔ `Recommendation Engine`
*   `Review Engine` ➔ `Memory Engine` ➔ `Recommendation Engine`

---

## 4. Infrastructure Architecture

### 4.1 Abstraction & Integration Layers
*   **Event Bus**: Arsitektur *Event Driven* (EDA) di mana *engine* berkomunikasi tanpa dependensi langsung.
*   **AI Gateway**: Abstraksi LLM (OpenAI, Gemini, Local LLM) agar *Domain* tidak terkait pada satu penyedia.
*   **Plugin Architecture**: Sumber pengetahuan terpisah (IT, Medis) diproses via antarmuka standar (tanpa *if-else* kategori).

### 4.2 Cache Strategy
Implementasi *Knowledge, Image, Search,* dan *Recommendation Cache*. Seluruh *cache* wajib memiliki **TTL** dan strategi invalidasi yang terdefinisi.

### 4.3 Future Cloud Architecture
Seluruh *Domain Engine* berjalan 100% offline. Peran Cloud dibatasi pada: Authentication, Backup, Knowledge Update, AI Gateway, Analytics, dan Sync.
*Jika Cloud mati, kapabilitas belajar inti tetap berjalan tanpa kendala.*

### 4.4 Security Foundation & Observability
*   **Security**: Standar penerapan `SQLCipher`, rotasi kunci, *Integrity Check, Certificate Pinning*, dan *Tamper Detection*.
*   **Observability vs Analytics**: *Learning Analytics* mencatat pola belajar pengguna; *System Observability* memantau latensi *Engine*, *Event Bus Queue*, *Crash Report*, dan latensi API.

---

## 5. Engineering Standards

### 5.1 Architecture Governance (ADR) & Review Process
Setiap perubahan arsitektur harus tercatat dalam **Architecture Decision Record (ADR)** (Contoh: `ADR-001 Clean Architecture`).
**Architecture Review Process**:
`Proposal` ➔ `Discussion` ➔ `ADR` ➔ `Implementation` ➔ `Review` ➔ `Merge` (Tidak ada perubahan Blueprint secara instan tanpa melalui siklus ini).

### 5.2 Version Governance
Blueprint ini mematuhi **Semantic Versioning**:
*   `v1.0` : Major Architecture
*   `v1.1` : Documentation Improvement
*   `v1.2` : Minor Architecture
*   `v2.0` : Breaking Architecture (Perubahan Major hanya melalui ADR).

### 5.3 Documentation Rules
Setiap *Engine* yang dibangun harus memiliki kelengkapan dokumentasi berikut:
`Purpose, Responsibilities, Input, Output, Dependencies, Events, State Machine, Performance Target, Unit Test, Example Flow, Sequence Diagram`.

### 5.4 Technical Debt Policy
*Technical Debt* (Utang Teknis) diperbolehkan **hanya** untuk: *Prototype, Experiment, Hotfix*, dan *Deadline*.
Semua *Technical Debt* harus didokumentasikan dengan: `Owner`, `Reason`, `Target Removal`, dan `Review Date`.

### 5.5 Risk Register
Daftar risiko sistem yang dipantau (Contoh format: *Risk | Impact | Mitigation | Owner | Review Frequency*):
*   AI Provider berubah / *Deprecation* API.
*   Ukuran *Knowledge Package* membesar secara eksponensial.
*   *Recommendation Engine* menjadi terlalu kompleks.
*   *Offline Sync Conflict* pada status perangkat ganda.
*   *Version Migration* & *Package Compatibility*.

### 5.6 Quality Gates & Definition of Done
**Quality Gates (Arsitektur):**
*   Critical Business Rules Coverage `≥95%`
*   Overall Domain Coverage `≥90%`
*   Lint & Detekt lulus tanpa isu kritikal.
*   Bebas dari *Circular Dependency*.
*   Mode Luring (*Offline*) berfungsi penuh.

**Definition of Done (Per Engine):**
*   *Business Rule* lengkap (Bebas logika *hardcoded*).
*   *Fake Repository* tersedia.
*   Dokumentasi engine komprehensif (Tidak memiliki TODO).
*   *Benchmark* performa memenuhi target.
*   *Code Review* selesai.

### 5.7 Performance Budget
*   Cold Start: `< 2 detik`
*   Recommendation & Feed Generation: `< 50 ms`
*   Roadmap, Quiz, & Search: `< 100 ms`
*   Memory Update: `< 20 ms`
*   Database Read: `< 30 ms`

### 5.8 Future Extension Rules
Fitur baru di masa depan harus memenuhi syarat:
*   Tidak mengubah *Domain Core* atau menambah *coupling*.
*   Tidak melanggar batas *Clean Architecture*.
*   Mendukung fitur luring penuh.
*   Tidak memanggil infrastruktur (seperti AI) secara langsung dari Domain.

---

## 6. Delivery Roadmap

### 6.1 Release Strategy
Siklus rilis terstruktur berdasarkan fase kematangan:
`Prototype` ➔ `Alpha` ➔ `Internal` ➔ `Closed Beta` ➔ `Open Beta` ➔ `Production` ➔ `LTS (Long Term Support)`
*Setiap tahap wajib memenuhi objektif rilis yang ditetapkan.*

### 6.2 Android Client Execution Phases
1.  **Phase 1: Framework & Data Infrastructure**
2.  **Phase 2: Onboarding & Personalization**
3.  **Phase 3: Knowledge Rendering**
4.  **Phase 4: Adaptive Daily Feed**
5.  **Phase 5: Assessment & Mentorship**
6.  **Phase 6: Search & Knowledge Packages**
7.  **Phase 7: Polish & Release**

---

## Engineering Motto

*Build for Decades.*
*Keep Domain Pure.*
*Offline by Default.*
*AI is Replaceable.*
*Knowledge is the Asset.*
*Learning Habit is the Product.*
*Keep It Simple.*
*Measure Before Optimizing.*
*Architecture Evolves Through ADR.*
*Code Exists to Serve Learning.*
