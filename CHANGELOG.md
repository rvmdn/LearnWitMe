# Changelog

All notable changes to the LearnWitMe Learning Intelligence Platform will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.1] - 2026-10-31

### Fixed
- **AI Prompt Sanitization & Credential Detection**:
  - Hardened pattern matching to intercept credential assignments (`api_key=`, `access_token=`, `refresh_token=`, `client_secret=`, `password=`, `token=`, `bearer=`, `Authorization: Bearer <token>`) across arbitrary whitespace, tabs, and multiline variations.
  - Eliminated false negatives for bearer tokens containing non-word character boundaries (e.g. `Bearer token-123secret`, `Bearer token.secret`).
  - Ensured credentials are never echoed into generated explanations, lessons, quizzes, or simplified summaries.
- **Metadata Boundary Isolation**:
  - Refined prompt wrapper detection (`CURRENT REQUEST`, `LEARNER PERSONALIZATION CONTEXT`, `Selected Answer:`, `Expected Answer:`) using structural line and header matching.
  - Resolved false positives on valid programming concepts like `JavaScript == vs ===` and `How to Handle the Current Request in Ktor`.
- **Educational Topic Preservation**:
  - Expanded and validated preservation of legitimate security and OAuth topics (e.g., `Bearer Token Authentication`, `Bearer Token Architecture`, `OAuth 2.0 Bearer Token Flow`, `Bearer Token Security`, `API Key Rotation`, `SQL Injection Prevention`).
- **Quiz Serialization Reliability**:
  - Validated strict JSON serialization/deserialization with `kotlinx.serialization` under attack vectors, guaranteeing schema compliance and field safety.

## [1.0.0] - 2026-10-28

### Added
- **Core Platform Baseline**:
  - Offline-first deterministic domain engine with 23 curated seed knowledge cards.
  - Encrypted SQLCipher database persistence and Room v3 DAO architecture.
  - MVI presentation architecture covering Daily Feed, Knowledge Card review, Quiz evaluation, and Onboarding.
  - Multi-provider AI gateway with offline mock response factory.
  - Comprehensive unit and integration test coverage across all modules.

