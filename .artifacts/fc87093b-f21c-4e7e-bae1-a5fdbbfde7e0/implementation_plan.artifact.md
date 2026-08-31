# Implementation Plan - HabitFlow Material Design 3 XML Layouts & Fragments

Implement Material Design 3 XML layouts, Navigation Component with BottomNavigationView, Fragments, and Adapters connecting to existing MVVM ViewModels across Home, Study, AI Planner, and Stats screens.

## User Review Required

- **Theme & Colors**: Setting up 60-30-10 color system in `colors.xml` and Material 3 themes in `themes.xml` (`#F8FAFC` light background, `#0F172A` dark background, `#1E293B` cards/containers, `#8B5CF6` electric violet accent, and accent gold).
- **Navigation Architecture**: Using Android Jetpack Navigation Component (`NavHostFragment`, `NavController`, `BottomNavigationView`) with 4 destinations: Home, Study, AI Planner, and Stats.

## Proposed Changes

### Theme & Color System
#### [MODIFY] [colors.xml](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/res/values/colors.xml)
- Define 60-30-10 color palette tokens (background, surface, primary accent, secondary, etc.).
#### [MODIFY] [themes.xml](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/res/values/themes.xml)
- Define `Theme.HabitFlow` inheriting from `Theme.Material3.DayNight.NoActionBar` with Material 3 color attributes.

### Dependencies & Navigation Graph
#### [MODIFY] [libs.versions.toml](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/gradle/libs.versions.toml) & [build.gradle.kts](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/build.gradle.kts)
- Add dependencies for `fragment-ktx`, `navigation-fragment-ktx`, `navigation-ui-ktx`, `recyclerview`, `constraintlayout`, and update material version.
#### [NEW] [nav_graph.xml](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/res/navigation/nav_graph.xml)
- Define navigation graph for Home, Study, AI Planner, and Stats fragments.

### Main Activity & Layout
#### [NEW] [activity_main.xml](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/res/layout/activity_main.xml)
- FrameLayout / FragmentContainerView for NavHostFragment and BottomNavigationView.
#### [MODIFY] [MainActivity.kt](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/java/com/example/habitflow/MainActivity.kt)
- Setup NavController with BottomNavigationView in traditional XML Activity style (`setContentView`).

### Screen 1: Dashboard / Home
#### [NEW] [fragment_home.xml](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/res/layout/fragment_home.xml)
#### [NEW] [item_habit_task.xml](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/res/layout/item_habit_task.xml)
#### [NEW] [dialog_add_habit.xml](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/res/layout/dialog_add_habit.xml)
#### [NEW] [HomeFragment.kt](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/java/com/example/habitflow/ui/HomeFragment.kt)
#### [NEW] [HabitTaskAdapter.kt](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/java/com/example/habitflow/ui/adapter/HabitTaskAdapter.kt)
#### [NEW] [AddHabitBottomSheetFragment.kt](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/java/com/example/habitflow/ui/dialog/AddHabitBottomSheetFragment.kt)

### Screen 2: Deep Focus Pomodoro Timer
#### [NEW] [fragment_study.xml](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/res/layout/fragment_study.xml)
#### [NEW] [StudyFragment.kt](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/java/com/example/habitflow/ui/StudyFragment.kt)

### Screen 3: AI Study & Habit Planner
#### [NEW] [fragment_ai_planner.xml](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/res/layout/fragment_ai_planner.xml)
#### [NEW] [item_ai_step.xml](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/res/layout/item_ai_step.xml)
#### [NEW] [AiPlannerFragment.kt](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/java/com/example/habitflow/ui/AiPlannerFragment.kt)
#### [NEW] [AiStepsAdapter.kt](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/java/com/example/habitflow/ui/adapter/AiStepsAdapter.kt)

### Screen 4: Performance & Analytics
#### [NEW] [fragment_stats.xml](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/res/layout/fragment_stats.xml)
#### [NEW] [StatsFragment.kt](file:///C:/Users/dushm/AndroidStudioProjects/HabitFlow/app/src/main/java/com/example/habitflow/ui/StatsFragment.kt)

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to verify compilation and build success.

### Manual Verification
- Deploy app to emulator/device, verify bottom navigation between Home, Study, AI Planner, and Stats screens, test Pomodoro timer start/pause/reset, AI study plan generation, and habit/task checking.
