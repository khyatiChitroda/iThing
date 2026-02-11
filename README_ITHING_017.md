# 📱 iThing Mobile App
## Branch: feature/ITHING-017 — Logout Implementation

---

## 🧭 Overview
Implements logout functionality by clearing stored JWT
and resetting navigation state.

---

## 🎯 Task Reference
- **Jira Ticket:** ITHING-017
- **Epic:** EPIC-3 – Authentication
- **Sprint:** Sprint 1
- **Status:** ✅ Completed

---

## 🧱 Implementation Details

### Repository Layer
- Added `logout()` in AuthRepository
- Clears token using AuthDataStore

### Domain Layer
- Added LogoutUseCase

### Presentation Layer
- DashboardViewModel calls LogoutUseCase
- DashboardScreen exposes Logout button
- Navigation resets to Login
- Splash restores correct session state

---

## 🧪 Verified Scenarios

- Login → Dashboard
- Logout → Login
- App restart after logout → Splash → Login
- No token remains in DataStore

---

## 🧠 Architectural Notes

- Clean separation of layers maintained
- No direct DataStore access from UI
- Logout lifecycle fully aligned with Splash session restore


