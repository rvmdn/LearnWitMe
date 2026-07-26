package com.learnwitme.core.domain.service

/**
 * Responsibility: Abstract time provider to keep domain logic purely deterministic.
 */
interface Clock {
    fun currentTimeMillis(): Long
}
