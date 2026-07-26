# Sprint 1 Implementation Plan: Domain Foundation
*(Reference: Architecture Bible v1.1)*

## 1. Sprint Goal
Membangun fondasi **Domain Layer** yang sepenuhnya:
*   **Pure Kotlin**: Bebas dari framework Android, Room, Ktor, atau dependensi eksternal apa pun.
*   **Immutable**: Memanfaatkan `data class` Kotlin dan koleksi *read-only* (`List`, `Set`).
*   **Deterministic**: Output selalu konsisten berdasarkan input tanpa efek samping tak terduga.
*   **Testable**: 100% *unit test coverage* untuk seluruh model bisnis.

---

## 2. Phase 0: Ubiquitous Language Review
Sebelum mendefinisikan taksonomi kode, seluruh pihak wajib menyepakati **Ubiquitous Language** (Bahasa Universal).
*   **Objective**: Menyamakan istilah yang digunakan oleh bisnis, dokumentasi, dan penamaan dalam *codebase*.
*   **Deliverables**: Glosarium terpusat untuk istilah sistem.
*   **Definition of Done**: Istilah konsisten dan tidak ambigu (misal: penggunaan mutlak `KnowledgeCard` tanpa ada variasi `Flashcard` di dalam kode).

---

## 3. Domain Taxonomy
Berdasarkan filosofi *Domain Driven Design (DDD)* yang ketat, seluruh elemen sistem diklasifikasikan sebagai berikut:

### Value Objects
*Tidak memiliki identitas unik; kesamaan dinilai dari seluruh nilai atributnya secara utuh (structural equality).*
*   **Identifier Value Objects**: `KnowledgeCardId`, `UserId`, `LearningUnitId`, dsb. (Menggantikan penggunaan `UUID` atau `String` mentah guna menjamin *type-safety* dan mencegah tertukarnya ID antar entitas).
*   `Recommendation` (Ditinjau ulang: Merupakan hasil komputasi *engine* dan tidak memiliki siklus hidup yang perlu dilacak secara independen).
*   `LearningProgress` (Ditinjau ulang: Representasi pencapaian yang merupakan sekumpulan nilai/state dari *UserProfile* pada suatu titik waktu).
*   `Tag`
*   `Topic`
*   `SubTopic`
*   `KnowledgeScore`
*   `ReviewSchedule`
*   `Reference`
*   `CareerPath`

### Entities
*Memiliki identitas unik (berbasis Identifier Value Object) yang melacak siklus hidupnya. Entitas dibandingkan murni berdasarkan identity (ID)-nya, bukan berdasarkan atribut lainnya yang bisa berubah.*
*   `KnowledgeCard`
*   `LearningUnit`
*   `Quiz`
*   `QuizResult`
*   `LearningSession`

### Aggregate Roots
*Entitas utama yang membawahi kumpulan entitas/value object lain dan menjaga konsistensi transaksionalnya.*
*   `LearningRoadmap` (mengelola sekumpulan `LearningUnit`)
*   `LearningMemory` (mengelola sekumpulan `KnowledgeScore` dan jejak belajar)
*   `UserProfile` (mengelola `LearningProgress` dan referensi *CareerPath*)
*   `KnowledgePackage` (mengelola sekumpulan `Topic` dan `KnowledgeCard`)

### Domain Events
*Representasi dari sebuah kejadian di masa lalu.*
*   `LearningAnalyticsEvent`
*   `SystemEvent`

### Repository Contracts
*Abstraksi antarmuka (interface) pengaksesan data. Mengikuti prinsip DDD, Repository idealnya hanya dibuat untuk berinteraksi dengan Aggregate Roots.*
*   `KnowledgeRepository`
*   `MemoryRepository`
*   `QuizRepository`
*   `ProgressRepository`
*   `RoadmapRepository`
*   `UserRepository`
*   `PackageRepository`

---

## 4. Dependency Order (Urutan Implementasi)
Implementasi kode harus dilakukan lapis demi lapis, memastikan objek dependen tidak dibuat sebelum komponen dasarnya tersedia.

1.  **Level 1 (Fundamental Value Objects)**:
    `Tag` ➔ `Reference` ➔ `Topic` ➔ `SubTopic` ➔ `CareerPath`
2.  **Level 2 (Learning Values & Status)**:
    `KnowledgeScore` ➔ `ReviewSchedule`
3.  **Level 3 (Core Entities)**:
    `KnowledgeCard` ➔ `Quiz` ➔ `QuizResult`
4.  **Level 4 (Complex Entities)**:
    `LearningUnit` (bergantung pada Card & Quiz) ➔ `Recommendation` ➔ `LearningSession` ➔ `LearningProgress`
5.  **Level 5 (Aggregate Roots)**:
    `LearningRoadmap` ➔ `LearningMemory` ➔ `KnowledgePackage` ➔ `UserProfile`
6.  **Level 6 (Events & Contracts)**:
    `LearningAnalyticsEvent` ➔ Repository Interfaces

---

## 5. Implementation Phases

### Phase 0: Ubiquitous Language Review
*   **Objective**: Menyamakan dan memvalidasi istilah (*Ubiquitous Language*) sebelum taksonomi dikunci.
*   **Deliverables**: Glosarium istilah domain.
*   **Dependencies**: -
*   **Out of Scope**: Implementasi kode.
*   **Definition of Done**: Kesepakatan nama entitas dan value object dicapai secara tertulis.

### Phase 1: Domain Taxonomy & Interfaces
*   **Objective**: Menetapkan arsitektur *marker interface* dan tipe dasar.
*   **Deliverables**: File Kotlin terisolasi untuk tipe dasar (opsional jika menggunakan sealed class/interfaces).
*   **Dependencies**: -
*   **Out of Scope**: Properti bisnis.
*   **Definition of Done**: Disetujui struktur awalnya.

### Phase 2: Value Objects
*   **Objective**: Menerapkan kelas *value* fundamental tanpa *identity*.
*   **Deliverables**: `Tag.kt`, `KnowledgeScore.kt`, dll.
*   **Dependencies**: Phase 1.
*   **Out of Scope**: Entity ID, relasi kompleks.
*   **Definition of Done**: Semua properti *immutable* dan melewati *Unit Test* validasi sederhana.

### Phase 3: Entities
*   **Objective**: Membuat kelas dengan identitas unik (`UUID` atau `String` ID).
*   **Deliverables**: `KnowledgeCard.kt`, `Quiz.kt`, dll.
*   **Dependencies**: Phase 2.
*   **Out of Scope**: Proses agregasi bisnis.
*   **Definition of Done**: Memiliki properti pengenal (*Identifier*) dan Unit Test *equality check*.

### Phase 4: Aggregate Roots
*   **Objective**: Membangun pohon relasi data teratas yang melindungi entitas di bawahnya.
*   **Deliverables**: `LearningRoadmap.kt`, `LearningMemory.kt`, dll.
*   **Dependencies**: Phase 2, 3.
*   **Out of Scope**: Pemanggilan database / *loading logic*.
*   **Definition of Done**: Semua struktur kompleks terbentuk dalam status statis.

### Phase 5: Repository Contracts
*   **Objective**: Mendaftarkan operasi *Input/Output* yang dibutuhkan Domain.
*   **Deliverables**: `KnowledgeRepository.kt` (Interfaces).
*   **Dependencies**: Phase 4.
*   **Out of Scope**: Implementasi konkrit Retrofit/Room.
*   **Definition of Done**: Interface berisi fungsi suspend atau Flow.

### Phase 6: Domain Validation
*   **Objective**: Melindungi integritas data sejak inisiasi objek.
*   **Deliverables**: Pengecekan *invariant* di blok `init { require(...) }`.
*   **Dependencies**: Phase 2, 3, 4.
*   **Out of Scope**: Validasi input Form Android.
*   **Definition of Done**: Nilai tidak valid menolak proses inisiasi (Throw Exception).

### Phase 7: Unit Testing Strategy
*   **Objective**: Membuktikan bahwa Domain beroperasi semestinya secara independen.
*   **Deliverables**: Test class di direktori `test/` untuk seluruh model.
*   **Dependencies**: Phase 2-6.
*   **Out of Scope**: *Instrumented Test*, UI Test.
*   **Definition of Done**: Lulus pengujian Gradle.

---

## 6. Implementation Rules
*   Semua data *WAJIB* diwujudkan dalam `data class`, `enum class`, atau `sealed class / interface`.
*   Tidak boleh menggunakan anomali eksternal (`@Entity`, `@PrimaryKey`, `@Inject`, `@SerializedName`).
*   Tidak boleh ada tipe *mutable* (Gunakan `val` bukan `var`, gunakan `List` bukan `MutableList`).
*   Tidak boleh menempatkan fungsi HTTP atau I/O (*Repository Implementation*) di folder ini.
*   Tidak boleh menggunakan dependensi `android.*`. Hanya `kotlin.*`.

---

## 7. Validation Strategy
Setiap *Domain Object* memiliki garis ketahanan mandiri (Self-Validating):
1.  **Invariant Checking**: Evaluasi blok `require()` di blok `init`. (Misal: `require(score in 0.0..1.0) { "Score must be between 0 and 1" }`).
2.  **Required Fields**: Sistem *null-safety* alami Kotlin. Kolom wajib = *non-nullable*.
3.  **Identity**: Entity dibedakan menggunakan properti *Identifier Value Object* (seperti `KnowledgeCardId`), bukan tipe primitif atau `UUID` generik, serta bukan berdasarkan perbandingan objek di memori.
4.  **Immutability Check**: Seluruh atribut direlasikan dengan `val`, modifikasi hanya terjadi via metode `copy()`.

---

## 8. Testing Strategy
*   **Apa yang diuji**: Blok *invariant* `init` (apakah melempar `IllegalArgumentException`), *equality* ID entitas, pembuatan salinan fungsi pembantu (jika ada properti turunan murni).
*   **Apa yang tidak diuji**: Integrasi database, jaringan, rendering Compose, JSON serialization (selama belum disisipkan *layer* di luarnya).
*   **Fake Repository**: File test akan berisi objek data statis sementara (Dummy/Fake Data) yang mensimulasikan hasil pemanggilan, disuntikkan tanpa framework *Mocking* berat apabila memungkinkan.

---

## 9. Out of Scope (Forbidden)
Tugas-tugas di bawah ini dilarang keras dikerjakan pada fase ini:
*   Android SDK / UI / Jetpack Compose / ViewModels
*   Room / SQLite / Database Caching
*   Network / Retrofit / Ktor / API Clients
*   AI Generation Logic / Gemini API Integration
*   Event Bus Implementation
*   Engine Logic (*Recommendation Engine*, *Knowledge Engine*)
*   Repository Implementation
