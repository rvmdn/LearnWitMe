package com.learnwitme.core.domain.service

import com.learnwitme.core.domain.model.*

/**
 * Responsibility: Stateless engine acting as the Single Source of Truth for learning progress.
 */
class ProgressEngine {

    fun calculateProgress(
        roadmap: LearningRoadmap,
        memory: LearningMemory,
        sessions: List<LearningSession>,
        profile: UserProfile,
        clock: Clock,
        policy: ProgressPolicy
    ): ProgressDecision {
        
        val currentTime = clock.currentTimeMillis()
        val ONE_DAY_MS = 24 * 60 * 60 * 1000L
        
        // 1. Completion
        val totalUnits = roadmap.units.size
        val completedUnits = roadmap.completedUnitIds.size
        val completionPercentage = if (totalUnits > 0) {
            completedUnits.toDouble() / totalUnits
        } else {
            0.0
        }
        
        // 2. Mastery
        val totalRecords = memory.records.size
        val masteryPercentage = if (totalRecords > 0) {
            memory.records.sumOf { it.retention } / totalRecords
        } else {
            0.0
        }
        
        // 3. Learning Streak
        var currentStreak = profile.progress.activeStreakDays
        var lastActiveDate = profile.progress.lastActiveDate
        
        // Evaluate streak from the provided new sessions history
        if (sessions.isNotEmpty()) {
            val sortedSessions = sessions.sortedBy { it.endTime }
            
            for (session in sortedSessions) {
                // Ignore sessions older than or equal to lastActiveDate
                if (session.endTime <= lastActiveDate && lastActiveDate != 0L) continue
                
                val lastActiveDay = lastActiveDate / ONE_DAY_MS
                val sessionDay = session.endTime / ONE_DAY_MS
                val daysBetween = sessionDay - lastActiveDay
                
                if (lastActiveDate == 0L) {
                    currentStreak = 1
                } else if (daysBetween == 1L) {
                    currentStreak += 1
                } else if (daysBetween > 1L) {
                    val timeSinceLastActive = session.endTime - lastActiveDate
                    if (timeSinceLastActive > policy.streakResetThresholdMillis) {
                        currentStreak = 1 // Reset if missed threshold
                    } else {
                        currentStreak += 1 // Maintained if still within threshold
                    }
                }
                lastActiveDate = session.endTime
            }
        }
        
        // Check if current time breaks the streak
        if (lastActiveDate > 0L) {
            val timeSinceLastActive = currentTime - lastActiveDate
            if (timeSinceLastActive > policy.streakResetThresholdMillis) {
                currentStreak = 0 // Streak broken until next session
            }
        }
        
        // 4. Daily & Weekly Progress
        val currentDay = currentTime / ONE_DAY_MS
        val dailySessions = sessions.filter { it.endTime / ONE_DAY_MS == currentDay }
        val weeklySessions = sessions.filter { (currentTime - it.endTime) <= 7 * ONE_DAY_MS }
        
        val dailyProgressUnits = dailySessions.sumOf { it.completedUnitIds.size }
        val weeklyProgressUnits = weeklySessions.sumOf { it.completedUnitIds.size }
        
        // 5. Velocity
        val totalMinutes = dailySessions.sumOf { it.durationMinutes }
        val learningVelocity = if (totalMinutes > 0.0) {
            dailyProgressUnits.toDouble() / totalMinutes
        } else {
            0.0
        }
        
        val summary = ProgressSummary(
            completionPercentage = completionPercentage,
            masteryPercentage = masteryPercentage,
            currentStreak = currentStreak,
            dailyProgressUnits = dailyProgressUnits,
            weeklyProgressUnits = weeklyProgressUnits,
            learningVelocityUnitsPerMinute = learningVelocity
        )
        
        val newProgress = profile.progress.copy(
            totalUnitsCompleted = completedUnits,
            totalCardsMastered = memory.records.count { it.state == MemoryState.MASTERED },
            activeStreakDays = currentStreak,
            lastActiveDate = lastActiveDate
        )
        
        val updatedProfile = profile.updateProgress(newProgress)
        
        return ProgressDecision(updatedProfile, summary)
    }
}
