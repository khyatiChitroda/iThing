# MOB-0102 – Auth Hardening (Stateless JWT Strategy)

## Overview

This PR refactors the authentication layer to align with the backend’s stateless JWT architecture and completes Sprint 1 (Auth Foundation Hardening).

The previous implementation included a temporary token provider abstraction and development token logic. This has now been replaced with a production-ready SessionManager backed by DataStore.

---

## Key Changes

### 1. Stateless JWT Strategy Finalized

- Backend confirmed to return only JWT (no refresh token).
- Token validity: 24 hours.
- No refresh flow implemented (correct for backend design).
- 401 → automatic logout flow validated.

---

### 2. Session Management Refactor

Removed:
- `AuthTokenProvider`
- `RealAuthTokenProvider`
- `AuthModule` binding abstraction
- Static/bootstrap token logic

Added:
- `SessionManager` (DataStore-backed)
- Proper Hilt `DataStoreModule` provider
- Clean dependency graph

Architecture:

DataStore → SessionManager → AuthInterceptor → OkHttp → Retrofit → Repository

---

### 3. Interceptor Cleanup

- Correct `Authorization: Bearer <token>` header format.
- Removed navigation logic from interceptor.
- Interceptor now strictly injects token only.

---

### 4. Splash Bootstrap Stabilized

- Session check implemented using single token read.
- Prevented infinite loading when token is null.
- Proper navigation to Login when token missing.

---

### 5. 401 Handling Validated

- Invalid token tested manually.
- API returns 401.
- Session clears.
- App navigates to Login.
- No crash.

---

### 6. Unit Tests Added

#### Session Layer
- `SessionManagerTest`
    - saveToken
    - getToken
    - clearSession

#### Domain Layer
- `LoginUseCaseTest`
    - Delegation to repository
    - Exception propagation

#### Data Layer
- `AuthRepositoryImplTest`
    - API call verification
    - Token persistence verification
    - Exception propagation
    - Logout datastore clear verification

All tests passing.

---

## Technical Improvements

- Removed unnecessary abstraction layers.
- Cleaned DI graph.
- Stabilized coroutine testing setup.
- Improved test coverage of authentication module.

---

## Risk Assessment

Low.

Changes isolated to authentication module.
Manual QA performed:
- Login flow
- Logout flow
- App restart
- Clearing app data
- Invalid token scenario

---

## Sprint Status

Sprint 1 – Auth Foundation Hardening: ✅ Completed

Next Sprint: App Shell + Intro/Home + Navigation
