package com.learnwitme.core.domain.service

import com.learnwitme.core.domain.model.KnowledgePackage

/**
 * Responsibility: Pure, stateless domain service validating structural invariants, version progression,
 * and checksum integrity of KnowledgePackage domain aggregates.
 */
class PackageValidationEngine {

    /**
     * Validates structural integrity (non-empty title, positive version, non-duplicate topic codes and card IDs).
     */
    fun validatePackage(pkg: KnowledgePackage): Boolean {
        if (pkg.title.isBlank()) return false
        if (pkg.version <= 0) return false

        val topicCodes = pkg.topics.map { it.code }
        if (topicCodes.size != topicCodes.toSet().size) return false

        val cardIds = pkg.cards.map { it.id }
        if (cardIds.size != cardIds.toSet().size) return false

        return true
    }

    /**
     * Validates semantic version progression (new version must be strictly greater than existing version).
     */
    fun validateVersionProgression(existingPkg: KnowledgePackage, newPkg: KnowledgePackage): Boolean {
        require(existingPkg.id == newPkg.id) { "Package IDs must match to validate version progression" }
        return newPkg.version > existingPkg.version
    }

    /**
     * Validates package checksum match against expected hash string.
     */
    fun validateChecksum(pkg: KnowledgePackage, expectedChecksum: String): Boolean {
        if (expectedChecksum.isBlank()) return false
        return pkg.validateChecksum(expectedChecksum)
    }
}
