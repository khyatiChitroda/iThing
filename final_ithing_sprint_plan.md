# iThing Mobile App Sprint Plan (WebApp -> Native Android)

## Context
- Android codebase: `/Users/abhishek.upadhyay/Downloads/iThing/AndroidApp`
- Web reference app: `/Users/abhishek.upadhyay/Downloads/iThing/ithing-redesign-main/WebApp`
- Mobile UI references: `/Users/abhishek.upadhyay/Downloads/iThing/Screens`
- Tech stack: Kotlin, Jetpack Compose, Hilt, Retrofit, Room, DataStore, Clean Architecture
- Date baseline: February 16, 2026

## Goal
Build a production-ready native Android app by reusing backend APIs and business rules from WebApp, while implementing native UI/UX in Compose.

## Planning Principles
- Reuse API contracts and permission model from WebApp.
- Prioritize mobile user journeys first (Auth, Dashboard, Reports).
- Keep admin-heavy flows lower priority unless explicitly required for V1.
- Ship incrementally with test and release gates every sprint.

## Mandatory Delivery Tracks (non-negotiable)
1. UI implementation must use Jetpack Compose across all V1 modules.
2. API integration is required for every delivered module (Retrofit + repository/use-case).
3. Website visual parity is required:
   - same brand color theme
   - equivalent icon style/meaning across common actions
4. Test writing is mandatory each sprint:
   - unit tests for domain/data/presentation logic
   - integration/UI coverage for critical flows

---

## Current Baseline (already present in AndroidApp)

### Implemented
- Project setup, Gradle catalog, Hilt foundation.
- Auth API login call, login UI, login ViewModel/use case/repository.
- Token storage via DataStore.
- Splash + session restore.
- Logout flow.
- Basic navigation graph.

### Partially implemented
- Auth interceptor exists but no complete 401-refresh strategy.
- Login screen misses some web parity items (remember me/forgot password full flow).
- Dashboard/Reports modules are not yet implemented to web parity.
- Tests and coverage gates are mostly pending.

---

## WebApp -> Mobile Scope Mapping

### V1 In Scope (must ship)
- Auth: Login, Forgot Password, Reset Password, Change Password.
- Intro/Home (mobile-first welcome flow).
- Dashboard: filters, widgets, refresh/live state, empty/error states.
- Reports: report list/dashboard, date range, chart rendering, export trigger.
- Device Logs (list + filters + pagination).
- Notifications (list + badge + read states).
- Profile/logout/session handling.

### V1.5 / V2 (defer unless needed)
- Industry/OEM/Customer/User CRUD full admin workflows.
- Device create/edit/mapping advanced setup flows.
- Config Dashboard create/edit flows.
- Alarm and FOTA management full CRUD.

---

## Sprint Structure
- Sprint length: 2 weeks
- Total plan: 6 delivery sprints + 1 stabilization sprint
- Release model:
  - Internal build every sprint
  - Beta after Sprint 4
  - Production candidate after Sprint 7

---

## Sprint 0 (Week 0-1): Discovery + Contracts + Architecture Lock

### Objective
Lock parity scope and API contracts to avoid rework.

### Tickets
1. MOB-0001 API contract baseline from WebApp actions/routes.
2. MOB-0002 Route parity matrix (`Web route -> Android destination`).
3. MOB-0003 Permission/resource-action matrix import.
4. MOB-0004 Android architecture standards (MVI + layer contracts).
5. MOB-0005 Definition of Done and quality gates.

### Acceptance Criteria
- Auth, dashboard, reports, logs API payloads documented.
- Error model standardized (HTTP + domain errors).
- Route parity matrix approved.
- Prioritized backlog tagged `V1` and `V2`.

---

## Sprint 1 (Week 1-2): Foundation Hardening + Auth Completion

### Objective
Finish authentication to production quality.

### Tickets
1. MOB-0101 Complete Auth API set (login, logout, refresh/session verify).
2. MOB-0102 Implement token strategy (header + refresh + expiry recovery).
3. MOB-0103 Login parity with web (`remember me`, show/hide password, validation).
4. MOB-0104 Forgot/Reset/Change password flows.
5. MOB-0105 Session bootstrap/splash routing hardening.
6. MOB-0106 Auth module unit tests.

### Acceptance Criteria
- App handles expired token without manual intervention.
- Login + forgot/reset/change password fully functional.
- Session restore reliable after app restart.
- Auth tests pass with >=80% coverage for auth domain/repository/viewmodel.

---

## Sprint 2 (Week 3-4): App Shell + Intro/Home + Navigation

### Objective
Build stable shell and post-login navigation foundation.

### Tickets
1. MOB-0201 Bottom nav + top app bar + profile/logout.
2. MOB-0202 Intro/Home screens aligned to `/Screens` mockups.
3. MOB-0203 Global components library (buttons, text fields, empty/error/loading, selectors).
4. MOB-0204 Permission-aware menu visibility (from web resource/action model).
5. MOB-0205 App-wide state handling for loading/errors/snackbar.
6. MOB-0206 Navigation deep link + back stack policies.
7. MOB-0207 Website design token parity (colors, icons, spacing, typography).

### Acceptance Criteria
- Navigation shell stable across auth/non-auth states.
- Intro/Home visual parity accepted against screen references.
- Shared components reused across at least 3 screens.
- No broken back stack paths in critical journeys.

---

## Sprint 3 (Week 5-6): Dashboard V1

### Objective
Deliver usable dashboard with mobile-optimized filters/widgets.

### Web References
- `src/pages/Dashboard/Dashboard.tsx`
- `src/components/DeviceSelector.tsx`
- `src/components/widgets/*`

### Tickets
1. MOB-0301 Dashboard API service + DTO/domain mapping.
2. MOB-0302 Filter cascade (Industry -> OEM -> Customer -> Device).
3. MOB-0303 Dashboard screen layout from mockups.
4. MOB-0304 Widget renderer (number, line, bar, area, pie, gauge, status).
5. MOB-0305 Refresh + last-updated + live badge behavior.
6. MOB-0306 Empty/error/loading/retry states.
7. MOB-0307 Dashboard tests (viewmodel/use cases/repository).

### Acceptance Criteria
- User can select filters and view dashboard data reliably.
- Refresh updates dashboard without app restart.
- At least core widget set works with API data.
- Dashboard module test coverage >=75%.

---

## Sprint 4 (Week 7-8): Reports V1 + Export Hooks

### Objective
Ship mobile reports browsing and generation flow.

### Web References
- `src/pages/Reports/List.tsx`
- `src/pages/Reports/ReportDashboard.tsx`
- `src/pages/Reports/AnalyticReport.tsx`
- `src/pages/Reports/ScheduleCreate.tsx`

### Tickets
1. MOB-0401 Reports API integration (categories, report data, scheduled reports).
2. MOB-0402 Reports list and category cards UI.
3. MOB-0403 Report dashboard screen with chart rendering.
4. MOB-0404 Date range/device filter integration.
5. MOB-0405 Export trigger flow (PDF/Excel backend-driven).
6. MOB-0406 Scheduled report list/read states (create can be deferred if API risk).
7. MOB-0407 Reports module tests.

### Acceptance Criteria
- User can open reports, apply filters, and view chart outputs.
- Export requests can be triggered and user gets success/failure feedback.
- Scheduled reports list works for active account.

---

## Sprint 5 (Week 9-10): Device Logs + Notifications + Profile

### Objective
Complete operational monitoring flows.

### Web References
- `src/pages/DeviceLogs/List.tsx`
- `src/pages/Notification/List.tsx`
- Topbar/profile dropdown components

### Tickets
1. MOB-0501 Device logs API integration.
2. MOB-0502 Device logs screen (filters, pagination/infinite scroll).
3. MOB-0503 Notifications list + unread badge + mark read.
4. MOB-0504 Profile summary + change password entry.
5. MOB-0505 Offline/cached recent data for logs + reports summary.
6. MOB-0506 Instrumentation tests for core flows.

### Acceptance Criteria
- Device logs and notifications are stable and performant.
- Unread badge and read-state updates are consistent.
- Core journeys (login -> dashboard -> report -> logs) pass smoke tests.

---

## Sprint 6 (Week 11-12): Optional Admin Scope (V1.5)

### Objective
Implement only if required by product decision.

### Candidate Modules (from WebApp)
- Industry management
- OEM/customer/user lists
- Device management + mapping
- Config dashboard basics
- Alarm/FOTA list views

### Delivery Rule
- Prefer read-only/list-first on mobile.
- Keep full CRUD/admin power workflows on web unless business-critical.

---

## Sprint 7 (Week 13-14): Stabilization + Release

### Objective
Production readiness and rollout.

### Tickets
1. MOB-0701 Performance optimization (startup, scroll, recomposition hot spots).
2. MOB-0702 Crash/ANR hardening and network resiliency.
3. MOB-0703 Accessibility and localization baseline.
4. MOB-0704 Security review (token handling, logs, sensitive data).
5. MOB-0705 Test coverage target >=80% critical modules.
6. MOB-0706 Release build signing, store assets, release notes.

### Acceptance Criteria
- No critical blockers in UAT.
- Crash-free beta target achieved.
- Play Store release candidate generated.

---

## Suggested Jira Epic Structure
- `EPIC-AUTH` Authentication & Session
- `EPIC-CORE` App Shell, Navigation, Shared Components
- `EPIC-DASH` Dashboard & Widgets
- `EPIC-REP` Reports & Exports
- `EPIC-OPS` Device Logs & Notifications
- `EPIC-REL` Quality, Security, Release

---

## Definition of Done (per ticket)
- Feature implemented in `data/domain/presentation` layers.
- Unit tests added for business logic.
- UI states: loading, empty, error, success handled.
- Analytics/logging hooks present where applicable.
- Documentation updated (API assumptions + known limitations).

---

## Risks and Mitigation
- API drift from web app:
  - Mitigation: freeze OpenAPI/Postman per sprint.
- Widget complexity on mobile:
  - Mitigation: ship core widgets first, defer low-usage variants.
- Over-scoping admin workflows:
  - Mitigation: enforce V1 vs V1.5 scope gate at Sprint 2 review.
- Token/refresh instability:
  - Mitigation: finish auth hardening before dashboard rollout.

---

## Immediate Next Actions (this week)
1. Confirm V1 scope includes or excludes admin CRUD modules.
2. Create Jira board with `MOB-*` tickets above.
3. Start Sprint 1 with auth hardening and password flows.
4. Add API contract docs under AndroidApp docs folder.
