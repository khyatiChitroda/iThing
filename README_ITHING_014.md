# 📱 iThing Mobile App
## Branch: feature/ITHING-014 — Auth Interceptor

---

## 🧭 Overview
This ticket introduces a centralized **authentication interceptor**
to manage the `Authorization` header for all network requests.

The interceptor removes the need to manually pass auth headers
in API interfaces and prepares the app for future authentication
token strategies.

---

## 🎯 Task Reference
- **Jira Ticket:** ITHING-014
- **Epic:** EPIC-3 – Authentication
- **Sprint:** Sprint 1
- **Task Name:** Implement Auth Interceptor
- **Status:** ✅ Completed

---

## 🧱 Scope of This Ticket

### ✅ Included
- Central `AuthInterceptor` using OkHttp
- `AuthTokenProvider` abstraction
- Temporary static token provider for development
- Interceptor wired into Retrofit / OkHttp
- Removal of manual `Authorization` headers from APIs

### ❌ Explicitly Excluded
- Bootstrap / guest token strategy
- Token refresh logic
- Login UI changes
- Logout flow
- Dashboard integration

---

## 🧩 Implementation Details

### AuthTokenProvider
core/auth/AuthTokenProvider.kt
core/auth/StaticAuthTokenProvider.kt

- Single source of truth for auth token
- Temporary static implementation for development
- Easily replaceable in future tickets

---

### AuthInterceptor
core/network/AuthInterceptor.kt

- Injects `Authorization` header automatically
- Safe when token is null or empty
- Stateless and reusable

---

### Network Integration
di/NetworkModule.kt

- Interceptor added to `OkHttpClient`
- All Retrofit APIs inherit auth behavior automatically

---

## 🧪 Verification

```bash
./gradlew clean build
