# LearnWitMe Architecture Guide

LearnWitMe is structured under Architecture Bible v1.1 with strict layer separation and unidirectional data flow.

## Layer Dependency Rules

```
:app / :feature:*  ──>  :core:domain  <──  :core:storage / :core:infrastructure
```

1. **`:core:domain` is the Center of the Architecture**:
   - 0 Android framework imports.
   - 0 Room / SQLite annotations or imports.
   - Contains all business logic, aggregates, domain events, domain services, repository interfaces, and use cases.
2. **`:core:storage` Implements Persistence**:
   - Room v3 with SQLCipher encryption.
   - Implements repository interfaces defined in `:core:domain`.
3. **`:core:infrastructure` Implements AI & External Services**:
   - Encapsulates multi-provider AI HTTP clients and mappers.
   - Implements `AIProvider` abstractions from `:core:domain`.
4. **`:feature:*` & `:app` Implement UI**:
   - AndroidX Compose + ViewModel (MVI).
   - Injects Use Cases and Workflows from `:core:domain`.
   - Never accesses DAOs or Raw Storage directly.
