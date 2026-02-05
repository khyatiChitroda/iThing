**📱 iThing Mobile App**
Branch: ithing002 — Configure Gradle Dependencies

**🧭 Overview**

This branch focuses on configuring and stabilizing all required Gradle dependencies for the iThing Mobile Application.
The work in this branch establishes a strong technical foundation for upcoming features such as dependency injection, networking, local storage, and clean architecture.

**🎯 Task Reference**

Jira Ticket: **ITHING-002**

Sprint: Sprint 1 – Foundation & Authentication

Task Name: Configure Gradle Dependencies

Status: ✅ Completed

**🧩 Objective**

Centralize dependency versions using Gradle Version Catalog

Add core libraries required for networking, database, and async operations

Ensure compatibility between Kotlin, Compose, and AGP

Prepare the project for Hilt, Retrofit, and Room setup

**🛠️ Tech Stack (Relevant to This Branch)
Core**

Kotlin

Android Gradle Plugin

Gradle Version Catalog

Libraries / Tools

Jetpack Compose (BOM + Material 3)

Navigation Compose

Hilt (with KSP)

Retrofit

OkHttp + Logging Interceptor

Room Database

Kotlin Coroutines

DataStore Preferences

Coil (Image loading)

JUnit & AndroidX Testing libraries

Note: Only libraries relevant to dependency configuration are listed here.

**📂 Key Changes Added**

Retrofit and OkHttp for REST API communication

Room database dependencies for local persistence

Kotlin Coroutines for asynchronous programming

DataStore Preferences for secure key-value storage

OkHttp logging interceptor for network debugging

Updated

libs.versions.toml with centralized version management

app/build.gradle.kts to include new implementations and KSP processors

Removed

N/A

**🧪 Verification & Testing**
Build
./gradlew clean build

**Validation Checklist**

 Project builds without errors

 App launches successfully

 No dependency conflicts or version issues

 Acceptance criteria for ITHING-002 satisfied

**📌 Notes & Decisions**

Version Catalog is used to ensure consistency and easier upgrades

KSP is preferred over KAPT for better build performance

Dependencies were added incrementally and verified via clean build

**🚀 Next Steps**

ITHING-003: Setup Hilt Dependency Injection

Create Application class with @HiltAndroidApp

Add AppModule, NetworkModule, and DatabaseModule

Verify dependency graph and injection flow

**🔀 Branch Strategy**

One Jira task per branch

Atomic commits focused on a single responsibility

No direct commits to main

Branch merged only after task completion

**👤 Ownership**

Project: iThing Mobile App

Client: iThing

Developer: Khyati

**📝 Usage**

This README documents only the scope of ITHING-002.
Each subsequent branch will contain its own README following the same template.
