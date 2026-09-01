# LearnWitMe (Learning Intelligence Platform)

LearnWitMe is an offline-first microlearning platform designed for modern Android & Kotlin engineering. It features intelligent spaced repetition, deterministic daily recommendations, interactive quiz evaluations, and controlled AI knowledge generation with SQLCipher encryption.

---

## Architecture Overview

LearnWitMe follows Clean Architecture principles with unidirectional data flow (MVI) and an Active Freeze on core architecture:

- **Presentation Layer (`:app`, `:feature:*`)**: Jetpack Compose UI with MVI ViewModels communicating exclusively with Domain Use Cases.
- **Domain Layer (`:core:domain`)**: Pure Kotlin core (zero Android/UI dependencies) containing canonical aggregates (`KnowledgeCard`, `KnowledgePackage`, `LearningMemory`, `LearningRoadmap`), engines (`RecommendationEngine`, `ProgressEngine`, `LearningMemoryEngine`), and workflows.
- **Infrastructure Layer (`:core:infrastructure`, `:core:analytics`)**: Multi-provider AI gateway (Gemini, OpenAI, Ollama, OpenRouter) and offline-first analytics pipeline.
- **Storage Layer (`:core:storage`)**: Room v3 persistence encrypted with SQLCipher.

---

## Core Features & Verified Flows

1. **Curriculum Seeding (P38–P41)**:
   - Automated offline parsing of 23 curated Android & Kotlin knowledge cards across English (`default-package.json`) and Indonesian (`default-package-id.json`) assets.
   - Enforced schema validation and structural invariants (`package.id == card.packageId`).
2. **Personalized Daily Feed (P42–P43)**:
   - Deterministic candidate ranking based on review urgency, weak topics, difficulty matches, and active roadmap progress.
3. **Active Learning & Spaced Repetition (P44–P46)**:
   - Real-time session tracking, quality-scored memory retention decay models, and scheduled review queues.
4. **Interactive Quiz & Progress Engine (P45, P47)**:
   - Full quiz evaluation lifecycle updating retention, active daily streaks, and topic mastery.
5. **Controlled AI Knowledge Generation (P49)**:
   - AI outputs are treated as untrusted, verified through a validation gate, checked against existing titles to prevent duplication, and persisted offline.

---

## Development & Build Setup

### Prerequisites
- **JDK**: Java 21 (OpenJDK / Oracle JDK 21)
- **Android SDK**: Compile SDK `34`, Target SDK `34`, Min SDK `26`
- **Gradle**: 8.13 (via `./gradlew` wrapper)

### Build Commands

```bash
# Clean and assemble debug APK
./gradlew clean assembleDebug

# Run all unit and integration test suites across all modules
./gradlew test
```

### Generated Artifacts
- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk` (~32.8 MB)

---

## Governance & Protocol

Before contributing or modifying any code, review the required documentation hierarchy:
1. `START_HERE.md`
2. `PROJECT_STATE.md`
3. `docs/AI_DEVELOPMENT_PROTOCOL.md`
4. `docs/ArchitectureBible/ArchitectureBible.md`
