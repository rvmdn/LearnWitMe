# Sprint 1 — Domain Foundation

## Sprint Goal

Membangun Core Domain Model sebagai fondasi seluruh Learning Intelligence Platform.

Seluruh Domain Model harus bersifat Pure Kotlin, immutable, deterministic, framework-independent, dan sepenuhnya dapat diuji menggunakan Unit Test.

Sprint ini belum mengimplementasikan business logic maupun Android.

---

## Scope

Layer:

core/domain

Tidak diperbolehkan menggunakan:

- Android SDK
- Jetpack Compose
- Room
- SQLCipher
- Ktor
- Retrofit
- AI
- Internet
- Database
- Framework Android

---

## Deliverables

### Domain Model

- [x] LearningUnit
- [x] KnowledgeCard
- [x] Quiz
- [x] QuizResult
- [x] LearningSession
- [x] LearningGoal
- [x] LearningRoadmap
- [x] Recommendation
- [x] KnowledgeScore
- [x] LearningMemory
- [x] ReviewSchedule
- [x] KnowledgePackage
- [x] Topic
- [x] SubTopic
- [x] Reference
- [x] Tag
- [x] CareerPath
- [x] UserProfile
- [x] LearningProgress
- [x] LearningAnalyticsEvent
- [x] SystemEvent

---

### Design Rules

Semua Domain Object wajib:

- immutable
- Pure Kotlin
- framework independent
- serializable bila diperlukan
- memiliki dokumentasi
- memiliki unit test
- menggunakan naming yang konsisten dengan Architecture Bible

Tidak diperbolehkan:

- Entity Framework Annotation
- Room Annotation
- Android Annotation
- Dependency Injection
- Repository Implementation
- Business Logic
- Helper Function yang tidak terkait Domain

---

### Validation

Sprint dianggap selesai apabila:

- Semua Domain Object selesai.
- Tidak ada dependency Android.
- Tidak ada dependency Room.
- Tidak ada dependency Network.
- Build berhasil.
- Unit Test lulus.
- Architecture Bible tetap konsisten.

---

## Out of Scope

Sprint ini TIDAK mencakup:

- Engine
- Repository
- UseCase
- Event Bus
- Room
- SQLCipher
- API
- AI
- Compose
- Navigation
- UI

Semua akan dikerjakan pada Sprint berikutnya.

---

## Definition of Done

- Build Success
- Unit Test Pass
- Immutable Domain Model
- Naming sesuai Ubiquitous Language
- Dokumentasi selesai
- Code Review selesai
