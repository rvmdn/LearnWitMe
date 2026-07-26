package com.learnwitme.core.domain.model

/**
 * Marker interface for DDD Value Objects.
 */
interface ValueObject

/**
 * Represents a DDD Entity with a unique identity.
 */
interface Entity<ID> {
    val id: ID
}

/**
 * Represents a DDD Aggregate Root.
 */
interface AggregateRoot<ID> : Entity<ID>
