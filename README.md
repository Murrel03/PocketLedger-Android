# PocketLedger — Personal Expense Tracker (Android)

A beginner-friendly Android app for tracking personal expenses: add what you spent, see the month's total at a glance, and see exactly which categories your money went to. All data is stored **on the device** with Room (SQLite) — no account, no internet, no backend.

Built as a portfolio project for an **Android Developer** role, using the standard modern Android stack: **Kotlin + Jetpack Compose + MVVM + Room**.

## Stack

| Piece | Choice | Version |
|---|---|---|
| Language | Kotlin | 2.0.20 |
| Build | Android Gradle Plugin | 8.5.2 |
| Gradle | Wrapper | 8.7 |
| UI | Jetpack Compose (Material 3) | Compose BOM 2024.09.00 |
| Architecture | MVVM + Repository | — |
| Database | Room (over SQLite) | 2.6.1 |
| Code generation | KSP (for Room) | 2.0.20-1.0.25 |
| Navigation | Navigation Compose | 2.7.7 |
| Async / state | Kotlin Coroutines + StateFlow (via Lifecycle 2.8.4) | — |
| Min / Target SDK | 26 (Android 8) / 34 (Android 14) | Java 17 |

## Features

- **Expense list** for the selected month, newest first
- **Monthly total** card + **category breakdown** (Food, Transport, Bills, Shopping, Entertainment, Health, Other), highest first
- **Month switcher** — step back/forward through months; totals and breakdown update instantly
- **Add expense** — title, amount, category chips, optional note
- **Edit expense** — tap any expense to change it
- **Delete expense** — trash icon in the list, or the delete button on the edit screen
- **Form validation** — blank title or a zero/invalid amount shows an inline error instead of saving
- Data **survives closing the app** (Room/SQLite on-device storage)
- Light and dark Material 3 themes (follows the system setting)

## How to open and run it in Android Studio

**You need:** Android Studio (Ladybug or newer) installed on your own machine, with its bundled JDK 17. No Android SDK is needed in any other environment — Android Studio installs it for you.

1. **Open the project:** Launch Android Studio → *Open* → select this `pocketledger-android` folder → *Trust project* if asked.
2. **Let Gradle sync:** Android Studio will download Gradle 8.7 and all dependencies on first open (needs internet once). Wait for "Gradle sync finished" and for indexing to complete. If it offers to generate/upgrade the Gradle wrapper, accept.
   - If sync complains about the SDK location, let Android Studio install **Android SDK Platform 34** when prompted (SDK Manager).
3. **Create an emulator:** *Tools → Device Manager → Create Device* → pick a phone (e.g. Pixel 8) → choose a system image with **API 34** → download it → Finish.
4. **Run:** Select the emulator in the device dropdown at the top, press the green **Run ▶** button (or Shift+F10). The app builds, installs, and opens on the emulator.
5. **Try it:** Tap **+**, add a few expenses in different categories (try one in a previous month, then use the `‹ ›` arrows), tap an expense to edit it, and use the trash icon to delete one. Note how the total and breakdown update the instant you save — that's the Flow → StateFlow → Compose chain working.
6. **Run the unit tests:** In the Project view, right-click `app/src/test/java/com/murrel/pocketledger/ExpenseCalculatorTest.kt` → *Run 'ExpenseCalculatorTest'*. All 4 tests should pass — these run on your computer, no emulator needed.

## Project structure

```
pocketledger-android/
├── settings.gradle.kts / build.gradle.kts / gradle.properties
├── gradle/libs.versions.toml          ← all dependency versions in one place
└── app/
    ├── build.gradle.kts               ← app module: SDK levels, Compose on, dependencies
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml    ← app label/icon; declares MainActivity as launcher
        │   ├── res/                   ← launcher icon + colours
        │   └── java/com/murrel/pocketledger/
        │       ├── MainActivity.kt            ← the single Activity; hosts Compose
        │       ├── data/
        │       │   ├── Expense.kt             ← Room @Entity + the category list
        │       │   ├── ExpenseDao.kt          ← SQL queries (Flow reads, suspend writes)
        │       │   ├── PocketLedgerDatabase.kt← Room database + singleton
        │       │   └── ExpenseRepository.kt   ← single source of truth over the DAO
        │       ├── domain/
        │       │   └── ExpenseCalculator.kt   ← pure Kotlin: monthly total, category sums
        │       └── ui/
        │           ├── PocketLedgerApp.kt     ← builds DB→Repository→ViewModel; NavHost
        │           ├── theme/Theme.kt         ← Material 3 light/dark colour schemes
        │           ├── viewmodel/ExpenseViewModel.kt ← UI state as StateFlow + actions
        │           └── screens/
        │               ├── ExpenseListScreen.kt      ← month total, breakdown, list
        │               └── AddEditExpenseScreen.kt   ← add/edit form (+ delete)
        └── test/java/com/murrel/pocketledger/
            └── ExpenseCalculatorTest.kt       ← JVM unit tests for the calculator
```

### What each layer does (the MVVM story)

- **UI (Compose screens)** — draws the current state and forwards taps to the ViewModel. It holds *no* data logic and never touches the database. When state changes, Compose automatically redraws ("recomposes") only what changed.
- **ViewModel** — holds the screen's state as a `StateFlow<PocketLedgerUiState>` (this month's expenses, total, category breakdown). It survives screen rotation, combines the expense stream with the selected month, and exposes simple actions: `addExpense`, `updateExpense`, `deleteExpense`, month forward/back.
- **Repository** — the single source of truth. The ViewModel asks it for data and tells it about changes; the repository decides where data lives. Today that's Room — the ViewModel would not change if a network API were added later.
- **Room (data layer)** — a type-safe layer over SQLite. `Expense` is the table, `ExpenseDao` is the queries, `PocketLedgerDatabase` ties them together. Reads are `Flow`s, so any insert/update/delete automatically pushes a fresh list up through Repository → ViewModel → UI.

Data flow in one line: **tap Save → ViewModel → Repository → Room insert → Flow emits new list → ViewModel recomputes totals → StateFlow emits → Compose recomposes.**

## Android concepts this project teaches

- **Compose state & recomposition** — UI as a function of state; `collectAsState()` on a StateFlow, and `remember { mutableStateOf(...) }` for local form state in the add/edit screen.
- **StateFlow** — a hot, always-has-a-value stream for UI state; `combine()` merges two flows (expenses + selected month), and `stateIn(viewModelScope, WhileSubscribed)` turns a cold Flow into a StateFlow that only queries while the UI is listening.
- **Room** — `@Entity`, `@Dao`, `@Database`; compile-time checked SQL; KSP code generation; why DB work must be off the main thread (`suspend` + `Flow` make that the default).
- **MVVM** — why UI, state holder, repository, and data source are separate classes, and what breaks if you merge them (untestable maths in click handlers, DB calls in Composables).
- **Coroutines** — `viewModelScope.launch { }` for writes; structured concurrency tied to the ViewModel's lifetime, so work is cancelled automatically when the ViewModel is cleared.
- **Navigation Compose** — a `NavHost` with three destinations and a typed argument (`edit/{expenseId}`).
- **Testable design** — keeping business logic (`ExpenseCalculator`) free of Android imports so it runs in plain JVM unit tests.

Deliberately *not* in v1 (good "next steps" to talk about): Hilt dependency injection (the ViewModel factory is manual so you can see the wiring), a date picker in the form (new expenses use today's date; editing keeps the original date), cloud sync/accounts, and charts for the breakdown.

## Resume bullets (use only AFTER you have built and run this yourself)

- "Built PocketLedger, a personal expense tracker Android app in Kotlin with Jetpack Compose and Material 3 — MVVM architecture with a Repository layer, Room (SQLite) persistence via Flow-based DAOs, and Navigation Compose across list and add/edit screens."
- "Implemented month-wise expense summaries (totals and category breakdown) as pure Kotlin logic separated from the UI and covered by JUnit tests; state managed with StateFlow and coroutines in the ViewModel."

Do not claim Android experience beyond this project — the honest framing is "first Android project, built to learn the modern stack," and the interview answers below are written for exactly that framing.

## Interview questions about THIS project (with model answers)

**1. Walk me through what happens when I add an expense and press Save.**
The form state (held in `remember` in the Add/Edit composable) is validated, then the screen calls `viewModel.addExpense(...)`. The ViewModel launches a coroutine in `viewModelScope` and calls the Repository, which calls the DAO's suspend insert into Room/SQLite. Because the list query returns a `Flow`, Room immediately emits the new list; the ViewModel's `combine` recomputes the monthly total and category totals, emits a new `PocketLedgerUiState` on its StateFlow, and the list screen — collecting that StateFlow — recomposes with the new expense and totals.

**2. Why use a Repository if the app only has one data source?**
Separation of concerns and a single source of truth. The ViewModel depends on an abstraction ("give me expenses, save this expense"), not on Room classes. That keeps the ViewModel testable with a fake repository, and if I later add cloud sync, only the Repository changes. It's the architecture Google recommends, and even with one source it keeps the DAO and SQL out of the UI layer.

**3. What's the difference between the `Flow` from Room and the `StateFlow` in your ViewModel?**
Room's Flow is cold — it only runs while collected, and it has no "current value" you can read synchronously. UI state needs to always have a latest value (the screen can be drawn at any moment), so the ViewModel converts it with `stateIn` into a StateFlow, using `SharingStarted.WhileSubscribed(5000)` so the database query pauses when no UI is observing but survives brief gaps like rotation.

**4. Why is your monthly-total code in a separate `ExpenseCalculator` object instead of the ViewModel or the screen?**
So it can be unit-tested without Android. `ExpenseCalculator` imports only the `Expense` data class and `java.time`, so its tests run on the JVM in milliseconds — I have tests for the total, for ignoring other months, and for category grouping/sorting. If that maths lived in a Composable or an AndroidViewModel, I'd need an emulator just to test addition.

**5. The expense list updates instantly after an edit without you re-querying — how?**
Because reads are reactive end-to-end. The DAO query returns `Flow<List<Expense>>`; Room re-runs the query and emits whenever the expenses table changes. The Repository exposes that Flow unchanged, the ViewModel combines it with the selected-month Flow and maps it to UI state, and Compose collects the StateFlow — so an insert, update, or delete anywhere propagates to the screen automatically. There's no manual refresh call anywhere in the app.

---

*Build note: this project was authored as source files and consistency-checked (package names, imports, cross-references, and Gradle/plugin version compatibility), but it has not been compiled here — building and running it requires Android Studio on your own machine, following the steps above. Versions chosen for mutual compatibility: AGP 8.5.2, Kotlin 2.0.20, KSP 2.0.20-1.0.25, Compose BOM 2024.09.00, Room 2.6.1, Navigation Compose 2.7.7, Lifecycle 2.8.4, Gradle 8.7, compile/target SDK 34, min SDK 26, JDK 17.*
