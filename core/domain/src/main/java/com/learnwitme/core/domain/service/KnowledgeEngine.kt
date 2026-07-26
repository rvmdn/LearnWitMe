package com.learnwitme.core.domain.service

import com.learnwitme.core.domain.model.*

/**
 * Responsibility: Serves as the Single Source of Truth for validating knowledge structures,
 * building graphs, checking prerequisites, and computing learning paths.
 */
class KnowledgeEngine {

    fun processKnowledge(
        packageData: KnowledgePackage,
        roadmap: LearningRoadmap,
        profile: UserProfile,
        policy: KnowledgePolicy
    ): KnowledgeDecision {
        
        // 1. Build Graph
        val nodes = mutableMapOf<KnowledgeCardId, KnowledgeNode>()
        val edges = mutableListOf<KnowledgeRelation>()
        
        for (card in packageData.cards) {
            val node = KnowledgeNode(
                cardId = card.id,
                relatedTopicCodes = card.relatedTopicCodes,
                prerequisites = card.prerequisiteCardIds
            )
            nodes[card.id] = node
            
            // Build edges for prerequisites (prerequisite -> card)
            for (prereq in card.prerequisiteCardIds) {
                edges.add(KnowledgeRelation(sourceId = prereq, targetId = card.id, type = RelationType.PREREQUISITE))
            }
        }
        
        val graph = KnowledgeGraph(nodes, edges)
        
        // 2. Circular Dependency Detection & Topological Sort (Kahn's Algorithm)
        val inDegree = mutableMapOf<KnowledgeCardId, Int>()
        val adjList = mutableMapOf<KnowledgeCardId, MutableList<KnowledgeCardId>>()
        
        // Initialize degrees
        for (node in nodes.keys) {
            inDegree[node] = 0
            adjList[node] = mutableListOf()
        }
        
        // Populate degrees and adjacency list from prerequisites
        for (node in nodes.values) {
            for (prereq in node.prerequisites) {
                if (adjList.containsKey(prereq)) {
                    adjList[prereq]?.add(node.cardId)
                    inDegree[node.cardId] = (inDegree[node.cardId] ?: 0) + 1
                } else {
                    // If prereq is not in the package, throw error if strict policy
                    if (policy.enforceStrictPrerequisites) {
                        throw IllegalStateException("Prerequisite \${prereq.value} for card \${node.cardId.value} is missing from the package.")
                    }
                }
            }
        }
        
        val queue = ArrayDeque<KnowledgeCardId>()
        for ((node, degree) in inDegree) {
            if (degree == 0) {
                queue.add(node)
            }
        }
        
        val sortedList = mutableListOf<KnowledgeCardId>()
        
        // To ensure deterministic behavior, we sort the queue lexicographically 
        // if there are multiple nodes with inDegree 0.
        // Actually, Kahn's algorithm processes as it goes, so we can just sort elements before adding to the real processing.
        // Let's use a priority queue or just sort the list at each step for determinism.
        // To keep it simple and deterministic, we sort the queue every time we modify it.
        
        while (queue.isNotEmpty()) {
            // Sort queue by cardId string value to guarantee deterministic output
            val sortedQueue = queue.sortedBy { it.value }
            queue.clear()
            queue.addAll(sortedQueue)
            
            val current = queue.removeFirst()
            sortedList.add(current)
            
            for (neighbor in adjList[current] ?: emptyList()) {
                inDegree[neighbor] = (inDegree[neighbor] ?: 0) - 1
                if (inDegree[neighbor] == 0) {
                    queue.add(neighbor)
                }
            }
        }
        
        if (sortedList.size != nodes.size) {
            throw IllegalStateException("Circular dependency detected in KnowledgePackage \${packageData.id.value}")
        }
        
        val learningPath = LearningPath(sortedList)
        
        // 3. Availability Check
        // A prerequisite is MET if the roadmap has a completed unit that corresponds to that card.
        val completedCardIds = roadmap.completedUnitIds.mapNotNull { unitId ->
            roadmap.units.find { it.id == unitId }?.cardId
        }.toSet()
        
        val availabilityMap = mutableMapOf<KnowledgeCardId, KnowledgeAvailability>()
        
        for (node in nodes.values) {
            val unmet = mutableListOf<KnowledgeCardId>()
            for (prereq in node.prerequisites) {
                if (!completedCardIds.contains(prereq)) {
                    unmet.add(prereq)
                }
            }
            
            val isAvailable = unmet.isEmpty() || !policy.enforceStrictPrerequisites
            availabilityMap[node.cardId] = KnowledgeAvailability(isAvailable, unmet)
        }
        
        return KnowledgeDecision(graph, learningPath, availabilityMap)
    }
}
