package com.learnwitme.app.seeder

import android.content.Context
import com.learnwitme.core.domain.model.*
import org.json.JSONObject

object DefaultPackageParser {
    fun parse(context: Context, languageCode: String = "id"): PackageManifest {
        val targetFileName = "default-package-$languageCode.json"
        val jsonString = try {
            context.assets.open(targetFileName).use { it.bufferedReader().readText() }
        } catch (e: Exception) {
            context.assets.open("default-package.json").use { it.bufferedReader().readText() }
        }
        val root = JSONObject(jsonString)

        val metadataObj = root.getJSONObject("metadata")
        val versionObj = metadataObj.getJSONObject("version")
        val version = PackageVersion(
            versionObj.getInt("major"),
            versionObj.getInt("minor"),
            versionObj.getInt("patch")
        )
        val metadata = PackageMetadata(
            id = KnowledgePackageId(metadataObj.getString("id")),
            title = metadataObj.getString("title"),
            description = metadataObj.getString("description"),
            version = version,
            checksum = PackageChecksum(metadataObj.getString("checksum"))
        )

        val topicsArr = root.optJSONArray("topics")
        val topics = mutableListOf<Topic>()
        if (topicsArr != null) {
            for (i in 0 until topicsArr.length()) {
                val obj = topicsArr.getJSONObject(i)
                topics.add(Topic(obj.getString("code"), obj.getString("name"), obj.getString("description")))
            }
        }

        val cardsArr = root.optJSONArray("cards")
        val cards = mutableListOf<KnowledgeCard>()
        if (cardsArr != null) {
            for (i in 0 until cardsArr.length()) {
                val obj = cardsArr.getJSONObject(i)
                val refArr = obj.optJSONArray("reference")
                val references = mutableListOf<Reference>()
                if (refArr != null) {
                    for (j in 0 until refArr.length()) {
                        val refObj = refArr.getJSONObject(j)
                        references.add(Reference(refObj.getString("title"), refObj.optString("url", null)))
                    }
                }
                
                val tagArr = obj.optJSONArray("tags")
                val tags = mutableListOf<Tag>()
                if (tagArr != null) {
                    for (j in 0 until tagArr.length()) {
                        val tagObj = tagArr.getJSONObject(j)
                        tags.add(Tag(tagObj.getString("name")))
                    }
                }

                val prereqArr = obj.optJSONArray("prerequisiteCardIds")
                val prereqs = mutableListOf<KnowledgeCardId>()
                if (prereqArr != null) {
                    for (j in 0 until prereqArr.length()) {
                        prereqs.add(KnowledgeCardId(prereqArr.getString(j)))
                    }
                }

                val relatedArr = obj.optJSONArray("relatedTopicCodes")
                val related = mutableListOf<String>()
                if (relatedArr != null) {
                    for (j in 0 until relatedArr.length()) {
                        related.add(relatedArr.getString(j))
                    }
                }

                cards.add(
                    KnowledgeCard(
                        id = KnowledgeCardId(obj.getString("id")),
                        title = obj.getString("title"),
                        summary = obj.getString("summary"),
                        whyItMatters = obj.getString("whyItMatters"),
                        realWorldExample = obj.getString("realWorldExample"),
                        commonMistakes = obj.getString("commonMistakes"),
                        whenNotToUse = obj.getString("whenNotToUse"),
                        bestPractice = obj.getString("bestPractice"),
                        reference = references,
                        officialDocumentationLink = obj.getString("officialDocumentationLink"),
                        difficulty = DifficultyLevel.valueOf(obj.getString("difficulty")),
                        estimatedReadingTimeSeconds = obj.getInt("estimatedReadingTimeSeconds"),
                        careerImpact = obj.getString("careerImpact"),
                        tags = tags,
                        prerequisiteCardIds = prereqs,
                        relatedTopicCodes = related
                    )
                )
            }
        }

        val unitsArr = root.optJSONArray("units")
        val units = mutableListOf<LearningUnit>()
        if (unitsArr != null) {
            for (i in 0 until unitsArr.length()) {
                val obj = unitsArr.getJSONObject(i)
                units.add(
                    LearningUnit(
                        id = LearningUnitId(obj.getString("id")),
                        title = obj.getString("title"),
                        cardId = KnowledgeCardId(obj.getString("cardId")),
                        quizId = if (obj.isNull("quizId")) null else QuizId(obj.getString("quizId")),
                        estimatedMinutes = obj.getInt("estimatedMinutes"),
                        topicCode = obj.getString("topicCode")
                    )
                )
            }
        }

        val quizzesArr = root.optJSONArray("quizzes")
        val quizzes = mutableListOf<Quiz>()
        if (quizzesArr != null) {
            for (i in 0 until quizzesArr.length()) {
                val obj = quizzesArr.getJSONObject(i)
                val qArr = obj.getJSONArray("questions")
                val questions = mutableListOf<QuizQuestion>()
                for (j in 0 until qArr.length()) {
                    val qObj = qArr.getJSONObject(j)
                    val optArr = qObj.getJSONArray("options")
                    val options = mutableListOf<QuizOption>()
                    for (k in 0 until optArr.length()) {
                        val optObj = optArr.getJSONObject(k)
                        options.add(QuizOption(optObj.getString("text"), optObj.getString("explanation")))
                    }
                    questions.add(
                        QuizQuestion(
                            id = qObj.getString("id"),
                            questionText = qObj.getString("questionText"),
                            options = options,
                            correctOptionIndex = qObj.getInt("correctOptionIndex")
                        )
                    )
                }
                quizzes.add(
                    Quiz(
                        id = QuizId(obj.getString("id")),
                        title = obj.getString("title"),
                        questions = questions,
                        targetDifficulty = DifficultyLevel.valueOf(obj.getString("targetDifficulty"))
                    )
                )
            }
        }

        val depArr = root.optJSONArray("dependencies")
        val dependencies = mutableListOf<KnowledgePackageId>()
        if (depArr != null) {
            for (i in 0 until depArr.length()) {
                dependencies.add(KnowledgePackageId(depArr.getString(i)))
            }
        }

        return PackageManifest(metadata, topics, cards, units, quizzes, dependencies)
    }
}
