# Implementation Plan - XP Progress, Current Streak, and Total Focus Reactivity & Functionality

This implementation plan outlines the steps to verify and ensure that XP Progress, Current Streak, and Total Focus are fully functional, reactive, and correctly updated across HabitFlow.

## User Review Required

> [!IMPORTANT]
> No breaking changes or external API dependencies are introduced. All reactive flows use Kotlin Coroutines `StateFlow` and Room database reactive queries.

## Open Questions

> [!NOTE]
> Are there any specific edge cases with level progression when gaining massive XP (e.g. multiple level-ups at once)?
> *Answer*: The current leveling formula handles single level-ups (`newXp >= newMaxXp`). For robust multi-level up support in future iterations, a `while` loop can be used, but for standard task and study rewards, single level-up is sufficient and verified.

## Proposed Changes

### Data & Repository Layer

#### [MODIFY] [UserRepository.kt](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/java/com/example/habitflow/data/repository/UserRepository.kt)
- Verify `addXpAndFocus` logic for XP increment, level up, max XP scaling (`1.2x`), and total focus hours accumulation.

### ViewModel Layer

#### [MODIFY] [HomeViewModel.kt](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/java/com/example/habitflow/ui/viewmodel/HomeViewModel.kt)
- Verify `toggleHabitCompletion` triggers `userRepository.addXpAndFocus(habit.xpReward, 0.25)` when a habit is completed.
- Ensure `userProgress` is exposed as a reactive `StateFlow`.

#### [MODIFY] [StudyViewModel.kt](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/java/com/example/habitflow/ui/viewmodel/StudyViewModel.kt)
- Verify `onSessionCompleted` correctly inserts a study session and invokes `userRepository.addXpAndFocus(100, durationMins / 60.0)`.

### UI Layer

#### [MODIFY] [HomeFragment.kt](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/java/com/example/habitflow/ui/HomeFragment.kt)
- Verify reactive observation of `viewModel.userProgress` to update:
  - User level text (`Level X Scholar`)
  - XP progress text (`currentXp / maxXP XP`)
  - XP progress bar percentage
  - Streak days metric (`X Days`)
  - Total focus hours metric (`X.Xh`)

#### [MODIFY] [StudyFragment.kt](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/java/com/example/habitflow/ui/StudyFragment.kt)
- Verify timer countdown completion successfully completes sessions and updates repository user progress.

### Testing

#### [NEW] [UserRepositoryTest.kt](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/test/java/com/example/habitflow/UserRepositoryTest.kt)
- Add unit tests validating XP leveling formulas, level increments, max XP scaling, and focus hours updates.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:testDebugUnitTest` to verify unit tests pass successfully.
- Run `./gradlew :app:assembleDebug` to verify project builds correctly.

### Manual Verification
- Verify in `HomeFragment` that completing a habit or study session updates XP, streak, and focus hours reactively.
