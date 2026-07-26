# LearnWitMe - Project State

**Project Name**: LearnWitMe
**Current Version**: 1.0.0-alpha
**Architecture Version**: 1.1 (Architecture Bible v1.1)
**Architecture Status**: ACTIVE FREEZE
**Repository Status**: Physical Refactor Completed (Sprint 1)
**Current Milestone**: Milestone 1 - Domain Foundation
**Current Sprint**: Sprint 1 (Repository Stabilization) ➔ Transisi ke Sprint 2 (Domain Foundation)
**Current Objective**: Menyelesaikan fondasi struktur fisik dan migrasi *legacy* untuk bersiap membangun Core Domain Model.

---

## Completed & Covered Components (Status Saat Ini)

### 1. Core Domain Models (Pure Kotlin & Framework-Independent)
- **Identifier Value Objects**: `KnowledgeCardId`, `UserId`, `LearningUnitId`, `QuizId`, `LearningRoadmapId`, `KnowledgePackageId`, `LearningMemoryId`.
- **Value Objects**: `Tag`, `Reference`, `Topic`, `SubTopic`, `CareerPath`, `KnowledgeScore`, `ReviewSchedule`, `LearningPreference`, `LearningProgress`, `DifficultyLevel`.
- **Entities & Aggregate Roots**: `KnowledgeCard`, `LearningUnit`, `Quiz`, `QuizResult`, `LearningRoadmap`, `LearningMemory`, `KnowledgePackage`, `UserProfile`.
- **Domain Contracts & Events**: `LearningAnalyticsEvent`, `SystemEvent`, `DomainContracts`.

### 2. Domain Repository Contracts (Interfaces)
- `KnowledgeRepository`, `KnowledgePackageRepository`, `LearningMemoryRepository`, `QuizRepository`, `ProgressRepository`, `RoadmapRepository`, `UserProfileRepository`, `LearningSessionRepository`.

### 3. Pure Domain Workflows & Use Cases
- `InitializeApplicationWorkflow`: Seed default package manifest & roadmap.
- `BuildDailyFeedWorkflow`: Komputasi feed harian berbasis memory & schedule.
- `StartQuizWorkflow`, `SubmitQuizAnswerWorkflow`, `FinishQuizWorkflow`: Logic quiz & akumulasi skor.
- `LoadKnowledgeCardWorkflow`, `CompleteLearningSessionWorkflow`: Interaksi kartu pengetahuan & sesi belajar.
- `FinishReviewWorkflow`: Algoritma review & interval pengulangan.
- `GenerateRecommendationWorkflow`: Algoritma rekomendasi unit belajar.
- `ImportPackageWorkflow`, `LoadUserProfileWorkflow`: Manajemen data package & profil.

### 4. App UI & Navigation Engine (Base Layer)
- `MainActivity`: Startup initialization flow & state handling (`Loading`, `OnboardingRequired`, `Success`).
- `DailyFeedScreen` (Beranda): Tampilan rekomendasi harian, streak, & progress.
- `ProfileScreen`, `KnowledgeCardScreen`, `QuizScreen`.
- `OnboardingScreen`: Base onboarding flow (default profile setup).

### 5. Deferred / Backlog Status
- **Kuesioner Minat & AI Extractor**: Ditangguhkan (deferred) sesuai kriteria *ACTIVE FREEZE* domain foundation, tersimpan rapi di [Deferred_Onboarding_Backlog.md](file:///e:/Project/Android/LearnWitMe/docs/sprint/Deferred_Onboarding_Backlog.md).

---

**Allowed Activities**: 
Pembuatan data class, enums, pure Kotlin logic, dan Unit Test di dalam modul `core/domain`.

**Forbidden Activities**: 
Implementasi Android SDK, Room, Ktor, Jetpack Compose, AI Gateway, perubahan struktur folder, pembuatan modul baru, dan penambahan *dependency* di luar instruksi sprint.

**Required Reading Flow**:
`START_HERE.md` ➔ `PROJECT_STATE.md` ➔ `docs/AI_DEVELOPMENT_PROTOCOL.md` ➔ `Architecture Bible`

**Current Development Mode**: 
Implementation & Pure Domain Logic (No UI, No Database, No Network).

