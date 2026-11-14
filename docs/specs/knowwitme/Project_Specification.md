# KnowWitMe - Project Specification

## 1. Project Overview
**KnowWitMe** is a modern Android application that acts as an AI-powered Knowledge Feed. It delivers one microlearning knowledge card at a time, maximizing knowledge retention through small, high-quality, verifiable content.

**Key Concepts:**
*   It is **NOT** an AI chatbot. It is a "TikTok for knowledge".
*   Users don't ask random questions; the app provides carefully selected Knowledge Cards (one concept per card).
*   Reading time per card: 30–60 seconds.
*   Cards can be read, saved, reviewed, used in quizzes, and connected to related concepts.
*   The architecture is completely **metadata-driven**, allowing support for unlimited categories (IT, Finance, Business, Psychology, Languages, Science, History, Health, Law, Photography, Cooking, etc.) without changing business logic.

## 2. User Flow
1. **First Launch**
2. **Select Interests**
3. **Select Difficulty**
4. **Select Learning Goal**
5. **Home Feed**
6. **Knowledge Card** -> Read, Save, Already Know, Not Interested, Read Later
7. **Repeat**
8. **Every 10 saved cards** -> **Quiz**
9. **Review weak concepts**

## 3. Core Features

### Home
*   AI Knowledge Feed displaying one card at a time.
*   Actions: Swipe Next, Save, Already Know, Not Interested, Read Later.

### Categories
*   Unlimited categories loaded from metadata (never hardcoded).

### Library
*   Local storage for saved Knowledge Cards.
*   Search, Filter, Sort.
*   Offline support.
*   Organization via Categories, Tags, Favorites, Recently Read.

### Quiz
*   Automatically generated from saved cards (every 10 new cards).
*   Multiple choice, tracking score and weak concepts.

### Review (Spaced Repetition)
*   Review intervals (e.g., Day 1 -> Day 3 -> Day 7 -> Day 14 -> Day 30).
*   Cards answered incorrectly appear sooner.

### Knowledge Graph
*   Cards contain Related Topics, Previous Topic, Next Topic.
*   Visual graph view of learning progression.

### References
*   Cards must contain verified references (Official Documentation, Government, University, Standards).
*   Never display unsupported claims.

## 4. AI & Engine Details

### AI Providers & Bring Your Own AI
*   Business logic must not depend on a specific provider.
*   Support for: OpenAI, Gemini, Groq, OpenRouter, Anthropic, Ollama, and future providers.
*   Users can connect their own AI via API keys (OpenAI, Gemini, OpenRouter, Groq, Ollama Local).
*   API keys must be securely encrypted and never stored in plaintext.

### Provider Resolver
*   Automatically selects provider based on Priority, Availability, Quota, Cost, Capabilities, User Preference, and handles fallbacks.

### AI Engine Module
Contains: Planner, Knowledge Generator, Quiz Generator, Review Generator, Validator, Provider Factory, Prompt Engine, Metadata Engine.

### Prompt Engine
*   Prompts use templates and support versioning and metadata injection (No hardcoded prompts).

## 5. Technical Architecture

### Tech Stack
*   **Language:** Kotlin
*   **UI:** Jetpack Compose (Material 3, Dark Mode, Dynamic Color, Animations)
*   **Navigation:** Navigation Compose
*   **Architecture Pattern:** MVVM, Clean Architecture, Repository Pattern
*   **Dependency Injection:** Supported (e.g., Hilt/Koin)
*   **Async/Concurrency:** Coroutines, Flow
*   **Local Database:** Room, DataStore
*   **Background Processing:** WorkManager
*   **Pagination:** Paging 3

### Data & Metadata
*   Everything is driven from metadata (Categories, Prompts, Provider Priority, Difficulty, Reading Time, Reference Policy, Learning Goals).
*   **Local Storage Requirements:** Knowledge Cards, Quiz Results, Review Schedule, Learning History, Bookmarks, Metadata Cache, Offline Reading.

### Security
*   Encrypt API Keys (Secure local storage / EncryptedSharedPreferences).
*   No secrets inside APK.
*   Support certificate pinning.

## 6. Analytics & Statistics
Track: Knowledge Learned, Knowledge Remembered, Weak Concepts, Reading Streak, Quiz Accuracy, Learning Time, Category Progress.

## 7. Future Features
*   Cloud Sync, Google Login, Multi-device support.
*   Voice Mode, Image Knowledge Cards, Flashcards.
*   Daily Challenges, Interview Mode, Certification Mode.
*   Roadmaps, Community Sharing, Knowledge Marketplace, Team Workspace.
*   AI Mentor, Widget, Wear OS, Desktop, Web.

## 8. Design & Goal
*   **Design:** Minimal, Modern, Fast, Focused. No clutter, no chat interface. A knowledge-first experience.
*   **Goal:** Build an application that helps users remember knowledge instead of simply reading information. Every feature should increase long-term knowledge retention.
