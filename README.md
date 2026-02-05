# iThing Mobile Application (Android)

The **iThing Mobile Application** is the official Android client for the **iThing IoT Platform**.  
It provides secure mobile access to monitor machines, view dashboards, analyze reports, and manage connected devices in real time.

This repository contains the Android implementation aligned with the iThing product roadmap and enterprise development standards.

---

## 🔗 Product Information

- **Web Platform:** https://ithing.in/login  
- **Mobile Platform:** Android  
- **Product Type:** IoT Monitoring & Analytics  

---

## 📱 Application Overview

The iThing Mobile App is designed to:
- Provide secure authentication for iThing users
- Display real-time dashboards and device metrics
- Enable monitoring of connected machines and sensors
- Offer reporting and analytics capabilities
- Support scalable, maintainable, and testable architecture

---

## 🛠️ Technology Stack

### Core
- **Language:** Kotlin
- **UI Toolkit:** Jetpack Compose
- **Architecture:** Clean Architecture + MVI
- **Dependency Injection:** Hilt
- **Asynchronous Programming:** Kotlin Coroutines

### Data & Networking
- **Networking:** Retrofit + OkHttp
- **Serialization:** Kotlinx Serialization
- **Local Storage:** Room Database
- **Preferences:** DataStore

### Tooling
- **Build System:** Gradle (Version Catalog)
- **Version Control:** Git (GitHub)
- **Testing:** JUnit, AndroidX Test, Espresso

---

## 🏗️ Project Architecture

The project follows **Clean Architecture principles** with clear separation of concerns:

com.ithing.mobile
├── data # API, database, repository implementations
├── domain # Business logic, models, use cases
├── presentation # UI, ViewModels, navigation
├── di # Dependency injection modules



This structure ensures:
- Scalability
- Testability
- Maintainability
- Clear responsibility boundaries

---

## 🔀 Development Workflow

The project follows a structured Git workflow:

main → stable, client-ready code
develop → active development
feature/* → Jira issue–based branches


- Each Jira task is developed in its own feature branch
- Feature branches contain task-specific documentation
- Only stable and reviewed code is merged into `main`

---

## 📄 Documentation Strategy

- **Main branch (`main`)**  
  - Contains product-level documentation only
- **Feature branches (`feature/*`)**  
  - Include task-specific README files (e.g. `README_ITHING_002.md`)
- This keeps the main branch clean and client-focused

---

## 🚀 Current Development Status

- Project foundation established
- Gradle and dependency setup completed
- Architecture and tooling in place
- Feature development ongoing as per roadmap

---

## 👤 Ownership

- **Project:** iThing Mobile Application  
- **Client:** iThing  
- **Platform:** Android  

---

## 📌 Notes

This repository represents an actively developed production application.  
Implementation details evolve sprint by sprint while maintaining architectural consistency and code quality.

