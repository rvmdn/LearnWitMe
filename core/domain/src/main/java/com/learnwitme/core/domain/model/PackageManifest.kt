package com.learnwitme.core.domain.model

data class PackageVersion(
    val major: Int,
    val minor: Int,
    val patch: Int
) : ValueObject, Comparable<PackageVersion> {
    init {
        require(major >= 0) { "Major version cannot be negative" }
        require(minor >= 0) { "Minor version cannot be negative" }
        require(patch >= 0) { "Patch version cannot be negative" }
    }

    override fun compareTo(other: PackageVersion): Int {
        val majorCompare = major.compareTo(other.major)
        if (majorCompare != 0) return majorCompare
        val minorCompare = minor.compareTo(other.minor)
        if (minorCompare != 0) return minorCompare
        return patch.compareTo(other.patch)
    }

    override fun toString(): String = "$major.$minor.$patch"
}

fun PackageVersion.toInt(): Int = major * 1000000 + minor * 1000 + patch

fun Int.toPackageVersion(): PackageVersion {
    val major = this / 1000000
    val minor = (this % 1000000) / 1000
    val patch = this % 1000
    return PackageVersion(major, minor, patch)
}

data class PackageChecksum(val value: String) : ValueObject {
    init {
        require(value.isNotBlank()) { "Checksum cannot be blank" }
    }
}

data class PackageMetadata(
    val id: KnowledgePackageId,
    val title: String,
    val description: String,
    val version: PackageVersion,
    val checksum: PackageChecksum
) : ValueObject

data class PackageManifest(
    val metadata: PackageMetadata,
    val topics: List<Topic> = emptyList(),
    val cards: List<KnowledgeCard> = emptyList(),
    val units: List<LearningUnit> = emptyList(),
    val quizzes: List<Quiz> = emptyList(),
    val dependencies: List<KnowledgePackageId> = emptyList()
) : ValueObject

fun calculateChecksum(manifest: PackageManifest): PackageChecksum {
    val contentString = "${manifest.metadata.id.value}:${manifest.metadata.title}:${manifest.metadata.version}:" +
            manifest.topics.joinToString(",") { "${it.code}:${it.name}" } + ":" +
            manifest.cards.joinToString(",") { "${it.id.value}:${it.content.title}" } + ":" +
            manifest.units.joinToString(",") { "${it.id.value}:${it.title}" } + ":" +
            manifest.quizzes.joinToString(",") { "${it.id.value}:${it.title}" }
            
    val bytes = contentString.toByteArray(Charsets.UTF_8)
    val md = java.security.MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    val hex = digest.joinToString("") { "%02x".format(it) }
    return PackageChecksum(hex)
}

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList()
) : ValueObject

data class ImportSummary(
    val packageId: KnowledgePackageId,
    val version: PackageVersion,
    val topicsImported: Int,
    val cardsImported: Int,
    val unitsImported: Int,
    val quizzesImported: Int,
    val status: String
) : ValueObject
