package com.learnwitme.core.network.repository

import com.learnwitme.core.domain.model.DifficultyLevel
import com.learnwitme.core.model.KnowledgeCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface KnowledgeRepository {
    fun getDailyInsight(): Flow<KnowledgeCard>
}

class MockKnowledgeRepository : KnowledgeRepository {
    override fun getDailyInsight(): Flow<KnowledgeCard> = flow {
        // Mocking a metadata-driven JSON response
        emit(
            KnowledgeCard(
                id = "kc_sql_select_star",
                title = "Kenapa SELECT * berbahaya?",
                summary = "Mengambil seluruh kolom meningkatkan I/O dan bandwidth database.",
                whyItMatters = "Pada tabel besar dapat meningkatkan latency secara signifikan dan menghabiskan memori aplikasi.",
                realWorldExample = "API monitoring menjadi lambat karena SELECT * digunakan pada endpoint dengan traffic tinggi, memuat data blob yang tidak ditampilkan ke UI.",
                commonMistakes = "Menggunakan SELECT * untuk seluruh query production karena malas mendefinisikan kolom.",
                whenNotToUse = "Saat Anda hanya butuh 1-2 kolom dari tabel yang memiliki 50+ kolom.",
                bestPractice = "Gunakan Explicit Column Selection (contoh: SELECT id, name, status).",
                reference = listOf("Microsoft Learn: SQL Performance", "PostgreSQL Documentation"),
                officialDocumentationLink = "https://learn.microsoft.com/en-us/sql/performance/",
                difficulty = DifficultyLevel.MID,
                estimatedReadingTimeSeconds = 45,
                careerImpact = "Mencegah insiden produksi akibat OOM (Out of Memory) dan lambatnya respon server.",
                tags = listOf("Database", "Performance", "SQL"),
                prerequisiteCardIds = listOf("kc_sql_basics"),
                relatedTopicIds = listOf("tp_indexes", "tp_query_optimization")
            )
        )
    }
}
