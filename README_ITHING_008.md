# 📱 iThing Mobile App
## Branch: feature/ITHING-008 — Authentication API Service

---

## 🧭 Overview
This task introduces the **authentication API service layer** for the iThing Android application.  
The goal is to define a clean Retrofit interface for authentication and wire it into the dependency injection graph using Hilt.

This task does not include business logic, repositories, token handling, or UI components.

---

## 🎯 Task Reference
- **Jira Ticket:** ITHING-008
- **Epic:** EPIC-3 – Authentication Module
- **Sprint:** Sprint 1 – Foundation & Authentication
- **Task Name:** Authentication API Service
- **Status:** ✅ Completed

---

## 🧩 Objective
- Define authentication API contract using Retrofit
- Reuse DTOs created in ITHING-007
- Provide API service via Hilt dependency injection
- Maintain Clean Architecture boundaries

---

## 🧱 Implementation Details

### Auth API Interface
data/remote/api/AuthApiService.kt

- Defines authentication endpoint(s)
- Uses Kotlin coroutines (`suspend`)
- Operates on DTOs only
- Does not contain business logic

---

### Dependency Injection
di/NetworkModule.kt

- Provides `AuthApiService` using Retrofit
- Uses shared Retrofit instance
- Scoped as a singleton

---

## 📂 Files Added / Updated

### Added
- `data/remote/api/AuthApiService.kt`

### Updated
- `di/NetworkModule.kt` (AuthApiService provider)

---

## 🧪 Verification

```bash
./gradlew clean build
