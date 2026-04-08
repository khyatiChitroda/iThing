# 📱 iThing Mobile App
## Branch: feature/ITHING-009 — Token Storage (DataStore)

---

## 🧭 Overview
This task introduces **secure token persistence** for the iThing Android application using **Preferences DataStore**.
The goal is to provide a simple, reactive, and testable mechanism to store, read, and clear authentication tokens.

This task focuses only on local storage and dependency injection. No API calls, repositories, or UI components are included.

---

## 🎯 Task Reference
- **Jira Ticket:** ITHING-009
- **Epic:** EPIC-3 – Authentication Module
- **Sprint:** Sprint 1 – Foundation & Authentication
- **Task Name:** Token Storage (DataStore)
- **Status:** ✅ Completed

---

## 🧩 Objective
- Persist authentication tokens locally
- Use modern Android DataStore (Preferences)
- Expose reactive APIs using Kotlin Flow
- Provide DataStore via Hilt dependency injection
- Maintain Clean Architecture boundaries

---

## 🧱 Implementation Details

### AuthDataStore
data/local/datastore/AuthDataStore.kt

- Uses Preferences DataStore
- Stores access token securely
- Exposes read, write, and clear operations
- Uses Kotlin Flow for reactive access
- Scoped as a singleton

---

### Dependency Injection
di/AppModule.kt

- Provides `AuthDataStore` via Hilt
- Uses application context
- Makes DataStore available across the app

---

## 📂 Files Added / Updated

### Added
- `data/local/datastore/AuthDataStore.kt`

### Updated
- `di/AppModule.kt` (AuthDataStore provider)

---

## 🧪 Verification

```bash
./gradlew clean build
