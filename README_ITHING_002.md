# 📱 iThing Mobile App
## Branch: feature/ITHING-002 — Configure Gradle Dependencies

---

## 🧭 Overview
This branch focuses on configuring and stabilizing all required Gradle dependencies for the iThing Mobile Application.  
The work in this branch establishes a strong technical foundation for upcoming features such as dependency injection, networking, local storage, and clean architecture.

---

## 🎯 Task Reference
- **Jira Ticket:** ITHING-002
- **Sprint:** Sprint 1 – Foundation & Authentication
- **Task Name:** Configure Gradle Dependencies
- **Status:** ✅ Completed

---

## 🧩 Objective
- Centralize dependency versions using Gradle Version Catalog
- Add core libraries required for networking, database, and async operations
- Ensure compatibility between Kotlin, Jetpack Compose, and AGP
- Prepare the project for Hilt, Retrofit, and Room setup

---

## 🛠️ Tech Stack (Relevant to This Branch)

### Core
- Kotlin
- Android Gradle Plugin
- Gradle Version Catalog

### Libraries / Tools
- Jetpack Compose (BOM + Material 3)
- Navigation Compose
- Hilt (with KSP)
- Retrofit
- OkHttp & Logging Interceptor
- Room Database
- Kotlin Coroutines
- DataStore Preferences
- Coil (Image loading)
- JUnit & AndroidX Testing libraries

---

## ✅ Next Commands (Quick Reminder)

```bash
git add README_ITHING_002.md
git commit -m "docs: add README for ITHING-002"
git pull --rebase origin feature/ITHING-002
git push origin feature/ITHING-002