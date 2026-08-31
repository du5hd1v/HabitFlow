# Implementation Plan - HabitFlow AI Planner, Pomodoro Service & ViewModels

Implement Gemini AI integration with API key settings, Foreground Service for Pomodoro timer, WorkManager background reminders, and core ViewModels (`HomeViewModel`, `StudyViewModel`, `AiPlannerViewModel`, `StatsViewModel`) integrating with Room DB.

## User Review Required

> [!IMPORTANT]
> - Requires POST_NOTIFICATIONS permission handling for Android 13+ (API 33+) for the Foreground Service and WorkManager reminders.
> - Gemini API key can be configured dynamically via UI / DataStore preferences or falls back to a placeholder / mock mode if not set.

## Proposed Changes

### Data & Preferences
#### [NEW] [PreferencesManager.kt](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/java/com/example/habitflow/data/PreferencesManager.kt)
- Manages local persistence for Gemini API Key using DataStore Preferences.

### Foreground Service & Workers
#### [NEW] [TimerForegroundService.kt](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/java/com/example/habitflow/data/service/TimerForegroundService.kt)
- Foreground service managing countdown timer (25:00 default), notification channel, actions (START, PAUSE, STOP), and broadcasting time remaining.
#### [NEW] [PomodoroWorker.kt](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/java/com/example/habitflow/data/worker/PomodoroWorker.kt)
- WorkManager worker for daily habit reminders and session notifications.

### ViewModels
#### [NEW] [HomeViewModel.kt](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/java/com/example/habitflow/ui/viewmodel/HomeViewModel.kt)
- Manages habit list, toggling completion, user XP/level-up calculation, and streaks.
#### [NEW] [StudyViewModel.kt](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/java/com/example/habitflow/ui/viewmodel/StudyViewModel.kt)
- Manages countdown timer state, ambient audio presets (Rain, Lo-Fi Beats, Cafe), starting/pausing/stopping timer, and rewarding +100 XP + logging study sessions on completion.
#### [NEW] [AiPlannerViewModel.kt](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/java/com/example/habitflow/ui/viewmodel/AiPlannerViewModel.kt)
- Manages Gemini prompt dispatch, API key settings, and saving generated micro-tasks to Room DB (`TaskDao`).
#### [NEW] [StatsViewModel.kt](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/java/com/example/habitflow/ui/viewmodel/StatsViewModel.kt)
- Aggregates weekly XP stats, focus hours, task counts, and streak calendar data from Room DB.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:testDebugUnitTest` to verify core logic and ViewModels.
- Run `./gradlew :app:assembleDebug` to ensure successful compilation.

### Manual Verification
- Verify Foreground Service starts and shows notification.
- Verify Gemini API integration with custom API key or fallback.
- Verify ViewModels correctly query Room DB and update UI state.
