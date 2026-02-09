# 📱 iThing Mobile App
## Branch: feature/ITHING-007 — Authentication Data Models

---

## 🧭 Overview
This task focuses on defining the **authentication-related data models** following Clean Architecture principles.
The goal is to establish a clear separation between **domain models**, **API DTOs**, **local database entities**, and **mapping logic**, without introducing business logic or UI dependencies.

This branch lays the foundation for upcoming authentication API integration and repository implementation.

---

## 🎯 Task Reference
- **Jira Ticket:** ITHING-007
- **Epic:** EPIC-3 – Authentication Module
- **Sprint:** Sprint 1 – Foundation & Authentication
- **Task Name:** Create Authentication Data Models
- **Status:** ✅ Completed

---

## 🧩 Objective
- Define authentication-related models in each architecture layer
- Maintain strict separation of concerns
- Ensure domain models remain framework-independent
- Prepare the project for API, persistence, and repository layers

---

## 🧱 Architecture Alignment

The implementation follows **Clean Architecture**:

- **Domain layer** → Business models
- **Data layer** → API DTOs, database entities, and mappers
- **Presentation layer** → Not touched in this task

No Android framework dependencies are introduced into the domain layer.

---

## 📂 Files & Structure

### Domain Model
domain/model/User.kt

- Pure Kotlin data class
- Represents authenticated user in business logic

---

### Remote API DTOs
data/remote/dto/LoginRequestDto.kt
data/remote/dto/LoginResponseDto.kt
data/remote/dto/UserDto.kt

- Matches backend API contract
- Uses Kotlinx Serialization
- Designed strictly for network communication

---

### Local Database Entity
data/local/entity/UserEntity.kt

- Room-compatible entity
- Represents cached user data
- Optimized for persistence

---

### Mappers
data/mapper/UserMapper.kt

- Converts between:
  - DTO ↔ Domain
  - Entity ↔ Domain
- Centralizes transformation logic
- Keeps layers decoupled

---

## 🧪 Verification

```bash
./gradlew clean build
