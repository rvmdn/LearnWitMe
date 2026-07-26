# Backlog: Deferred Onboarding & EDA Components
*(Deferred from Sprint 1 to ensure strict alignment with the Domain Foundation scope)*

Dokumen ini melestarikan desain dan kode sumber komponen UseCase, Repository Contract, Domain Event, Event Publisher, dan AI Extractor agar dapat diintegrasikan kembali pada sprint yang sesuai (seperti Sprint 8 untuk AI Integration, dsb).

---

## 1. Repository Contract

### [OnboardingRepository.kt](file:///e:/Project/Android/LearnWitMe/core/domain/src/main/java/com/learnwitme/core/domain/repository/OnboardingRepository.kt)
```kotlin
package com.learnwitme.core.domain.repository

import com.learnwitme.core.domain.model.LearningProfile

interface OnboardingRepository {
    suspend fun saveProfile(profile: LearningProfile)
    suspend fun getProfile(): LearningProfile?
    suspend fun clearProfile()
}
```

---

## 2. AI Extractor Service

### [AiOnboardingExtractor.kt](file:///e:/Project/Android/LearnWitMe/core/domain/src/main/java/com/learnwitme/core/domain/service/AiOnboardingExtractor.kt)
```kotlin
package com.learnwitme.core.domain.service

import com.learnwitme.core.domain.model.LearningProfile

interface AiOnboardingExtractor {
    suspend fun extractProfile(goalText: String): LearningProfile
}
```

---

## 3. Event Bus & EDA Components

### [DomainEventPublisher.kt](file:///e:/Project/Android/LearnWitMe/core/domain/src/main/java/com/learnwitme/core/domain/event/DomainEventPublisher.kt)
```kotlin
package com.learnwitme.core.domain.event

interface DomainEventPublisher {
    fun publish(event: Any)
}
```

### [OnboardingCompletedEvent.kt](file:///e:/Project/Android/LearnWitMe/core/domain/src/main/java/com/learnwitme/core/domain/event/OnboardingCompletedEvent.kt)
```kotlin
package com.learnwitme.core.domain.event

import com.learnwitme.core.domain.model.LearningProfile

data class OnboardingCompletedEvent(
    val profile: LearningProfile,
    val timestamp: Long = System.currentTimeMillis()
)
```

---

## 4. Use Cases

### [SubmitManualOnboardingUseCase.kt](file:///e:/Project/Android/LearnWitMe/core/domain/src/main/java/com/learnwitme/core/domain/usecase/SubmitManualOnboardingUseCase.kt)
```kotlin
package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.event.DomainEventPublisher
import com.learnwitme.core.domain.event.OnboardingCompletedEvent
import com.learnwitme.core.domain.model.DifficultyLevel
import com.learnwitme.core.domain.model.EstimatedFocus
import com.learnwitme.core.domain.model.LearningProfile
import com.learnwitme.core.domain.repository.OnboardingRepository

class SubmitManualOnboardingUseCase(
    private val repository: OnboardingRepository,
    private val eventPublisher: DomainEventPublisher
) {
    suspend fun execute(
        careerPath: String,
        currentLevel: DifficultyLevel,
        targetLevel: DifficultyLevel,
        preferredTopics: List<String>
    ): Result<LearningProfile> {
        if (careerPath.isBlank()) {
            return Result.failure(IllegalArgumentException("Career Path cannot be blank"))
        }
        if (preferredTopics.isEmpty()) {
            return Result.failure(IllegalArgumentException("Preferred topics cannot be empty"))
        }
        if (currentLevel.ordinal > targetLevel.ordinal) {
            return Result.failure(IllegalArgumentException("Current level cannot be higher than target level"))
        }

        val focusPercentage = 100f / preferredTopics.size
        val estimatedFocus = preferredTopics.map { topic ->
            EstimatedFocus(topic = topic, percentage = focusPercentage)
        }

        val profile = LearningProfile(
            learningGoal = null,
            careerPath = careerPath,
            currentLevel = currentLevel,
            targetLevel = targetLevel,
            detectedTechnologies = emptyList(),
            detectedInterests = preferredTopics,
            recommendedTopics = preferredTopics,
            recommendedRoadmap = "Standard Roadmap for $careerPath",
            suggestedKnowledgePackages = emptyList(),
            estimatedLearningFocus = estimatedFocus
        )

        repository.saveProfile(profile)
        eventPublisher.publish(OnboardingCompletedEvent(profile))

        return Result.success(profile)
    }
}
```

### [ExtractOnboardingProfileUseCase.kt](file:///e:/Project/Android/LearnWitMe/core/domain/src/main/java/com/learnwitme/core/domain/usecase/ExtractOnboardingProfileUseCase.kt)
```kotlin
package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.model.LearningProfile
import com.learnwitme.core.domain.service.AiOnboardingExtractor

class ExtractOnboardingProfileUseCase(
    private val extractor: AiOnboardingExtractor
) {
    suspend fun execute(goalText: String): Result<LearningProfile> {
        if (goalText.isBlank()) {
            return Result.failure(IllegalArgumentException("Goal description text cannot be blank"))
        }

        return try {
            val profile = extractor.extractProfile(goalText)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### [ConfirmOnboardingProfileUseCase.kt](file:///e:/Project/Android/LearnWitMe/core/domain/src/main/java/com/learnwitme/core/domain/usecase/ConfirmOnboardingProfileUseCase.kt)
```kotlin
package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.event.DomainEventPublisher
import com.learnwitme.core.domain.event.OnboardingCompletedEvent
import com.learnwitme.core.domain.model.LearningProfile
import com.learnwitme.core.domain.repository.OnboardingRepository

class ConfirmOnboardingProfileUseCase(
    private val repository: OnboardingRepository,
    private val eventPublisher: DomainEventPublisher
) {
    suspend fun execute(profile: LearningProfile): Result<Unit> {
        if (profile.careerPath.isBlank()) {
            return Result.failure(IllegalArgumentException("Career Path cannot be blank"))
        }
        if (profile.currentLevel.ordinal > profile.targetLevel.ordinal) {
            return Result.failure(IllegalArgumentException("Current level cannot be higher than target level"))
        }

        return try {
            repository.saveProfile(profile)
            eventPublisher.publish(OnboardingCompletedEvent(profile))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

## 5. Deferred Unit Tests

### [OnboardingUseCasesTest.kt](file:///e:/Project/Android/LearnWitMe/core/domain/src/test/java/com/learnwitme/core/domain/OnboardingUseCasesTest.kt)
Dapat dilihat di riwayat komit git atau dipulihkan langsung dari dokumentasi ini bila sprint integrasi onboarding telah aktif.
