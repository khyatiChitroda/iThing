# 📱 iThing Mobile App
## Branch: feature/ITHING-012 — Login UI (Jetpack Compose)

---

## 🧭 Overview
This task implements the **Login UI** for the iThing Android application using **Jetpack Compose**, following Clean Architecture and unidirectional data flow principles.

The Login feature is built with a clear separation between UI, state, ViewModel, and domain logic, ensuring scalability and consistency across all future screens.

---

## 🎯 Task Reference
- **Jira Ticket:** ITHING-012
- **Epic:** EPIC-3 – Authentication Module
- **Sprint:** Sprint 1 – Foundation & Authentication
- **Task Name:** Login UI (Jetpack Compose)
- **Status:** ✅ Completed

---

## 🧩 Objective
- Build a clean and testable Login screen
- Follow stateless UI and state-driven rendering
- Integrate authentication use cases via ViewModel
- Establish a reusable UI pattern for all future screens

---

## 🧱 Architecture Pattern Used

Each screen follows the same layered approach:

UiState
↓
ViewModel
↓
UseCases
↓
Repository


UI is fully driven by immutable state and callback events.

---

## 🧱 Implementation Details

### LoginUiState
presentation/feature/login/LoginUiState.kt

- Represents the single source of truth for the Login screen
- Holds input values, loading state, error state, and success flag

---

### LoginViewModel
presentation/feature/login/LoginViewModel.kt

- Owns and updates LoginUiState
- Handles validation, loading, success, and error scenarios
- Calls LoginUseCase to perform authentication
- Does not contain UI or navigation logic

---

### LoginScreen (Composable UI)
presentation/feature/login/LoginScreen.kt

- Stateless Jetpack Compose UI
- Renders UI based solely on LoginUiState
- Emits user actions via callbacks
- Displays loading indicator and error messages

---

### LoginRoute
presentation/feature/login/LoginRoute.kt

- Connects LoginViewModel with LoginScreen
- Acts as the entry point for navigation integration
- Keeps UI and ViewModel responsibilities separated

---

## 📂 Files Added / Updated

### Added
- `presentation/feature/login/LoginUiState.kt`
- `presentation/feature/login/LoginViewModel.kt`
- `presentation/feature/login/LoginScreen.kt`
- `presentation/feature/login/LoginRoute.kt`

---

## 🧪 Verification

```bash
./gradlew clean build
