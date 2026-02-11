# 📱 iThing Mobile App
## Branch: feature/ITHING-016 — Splash & Session Restore

---

## 🧭 Overview
Implements Splash screen and automatic session restoration.

On app launch:
- If JWT exists → navigate to Dashboard
- If no JWT → navigate to Login

---

## 🎯 Task Reference
- **Jira Ticket:** ITHING-016
- **Epic:** EPIC-3 – Authentication
- **Sprint:** Sprint 1
- **Status:** ✅ Completed

---

## 🧱 Implementation Details

### SplashScreen
- Simple loading UI

### SplashViewModel
- Reads token from AuthDataStore
- Determines destination

### SplashRoute
- Observes state
- Navigates accordingly
- Removes Splash from back stack

### Navigation
- Splash set as start destination

---

## 🧪 Verified Scenarios

- Fresh install → Splash → Login
- After login → Splash → Dashboard
- App restart preserves session

---

## 🧠 Architectural Notes

- No API calls during splash
- Token persistence reused
- Clean separation of UI and navigation
- Scalable for token expiry logic later


