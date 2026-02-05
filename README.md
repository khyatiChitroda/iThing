# iThing Mobile Application (Android)

Android mobile application for the **iThing IoT Platform**, providing secure access and mobile experiences for monitoring, dashboards, and reports related to connected machines and devices.

This repository contains the Android client implementation aligned with the iThing product roadmap and sprint-based delivery plan.

---

## 🔗 Product Information

- Web Platform: https://ithing.in/login
- Mobile Platform: Android

---

## 📌 Current Status

- **Sprint:** Sprint 1  
- **Completed Issue:** `ITHING-001`  
- **Status:** ✅ Completed  
- **Last Updated:** Feb 2026  

---

## ✅ ITHING-001: Initialize Android Project

### Jira Reference
- Issue: ITHING-001 – Initialize Android Project

### Objective
Establish a stable and modern Android project foundation to support future feature development.

---

### ✔ Deliverables Completed

- Android Studio project initialized
- Kotlin configured as the primary language
- Jetpack Compose enabled for UI development
- Gradle build system configured
- Project builds successfully without errors
- Git repository initialized with proper `.gitignore`
- Branching strategy established
- Initial CI groundwork prepared

---

### 🛠️ Technical Configuration

| Category | Details |
|--------|--------|
| Language | Kotlin |
| UI Toolkit | Jetpack Compose |
| Minimum SDK | 26 |
| Target SDK | 34 |
| Build Tool | Gradle |
| Version Control | Git (GitHub) |

---

## 🔀 Git Workflow

The project follows a structured Git workflow aligned with Jira-based development.

```text
main        → stable, client-ready code
develop     → active development
feature/*   → issue-based feature branches

---

## ✅ How to commit this README

```bash
git add README.md
git commit -m "ITHING-001 Add initial project documentation"
git push
