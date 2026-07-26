# Dependency Graph - LearnWitMe

Diagram berikut menunjukkan grafik ketergantungan modul di dalam proyek *LearnWitMe*, memperlihatkan modul `:core:domain` sebagai inti dari arsitektur platform.

```mermaid
graph TD
    %% Modules
    App[":app"]
    FeatureOnboarding[":feature:onboarding"]
    FeatureKnowledgeCard[":feature:knowledgecard"]
    FeatureMentor[":feature:mentor"]
    FeatureDailyFeed[":feature:dailyfeed"]
    FeatureQuiz[":feature:quiz"]
    
    CoreNetwork[":core:network"]
    CoreStorage[":core:storage"]
    CoreModel[":core:model"]
    CoreDomain[":core:domain (JVM Core)"]

    %% Dependencies
    App --> FeatureOnboarding
    App --> FeatureKnowledgeCard
    App --> FeatureMentor
    App --> FeatureDailyFeed
    App --> FeatureQuiz
    App --> CoreDomain

    FeatureOnboarding --> CoreModel
    FeatureKnowledgeCard --> CoreModel
    FeatureMentor --> CoreModel
    FeatureDailyFeed --> CoreModel
    FeatureQuiz --> CoreModel

    CoreNetwork --> CoreModel
    CoreStorage --> CoreModel
    
    CoreModel -- api --> CoreDomain

    classDef core fill:#f9f,stroke:#333,stroke-width:2px;
    classDef domain fill:#85C1E9,stroke:#333,stroke-width:4px;
    class CoreDomain domain;
```

### Penjelasan:
1.  **`:core:domain`** adalah modul pustaka Kotlin JVM murni tanpa dependensi eksternal, menjadikannya fondasi terdasar dari sistem.
2.  **`:core:model`** mengekspos tipe domain secara transitif menggunakan konfigurasi `api(project(":core:domain"))`.
3.  Modul jaringan (**`:core:network`**), penyimpanan (**`:core:storage`**), dan fitur-fitur (**`:feature:*`**) bergantung pada model dan domain untuk melakukan pertukaran data yang aman dan deterministik.
