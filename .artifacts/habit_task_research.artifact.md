# Habit & Task Architecture Research Report

This report documents the current architecture, data structures, persistence layers, UI components, and state management for Habits and Tasks in **HabitFlow**, prepared for implementing update and delete options for tasks.

---

## 1. Data Structures & Entities

### **HabitEntity** (`HabitEntity.kt`)
- **Table Name**: `habits`
- **Fields**:
  - `id`: `Long` (Primary Key, autoGenerate = true)
  - `title`: `String`
  - `category`: `String`
  - `xpReward`: `Int`
  - `dueDate`: `String`
  - `isCompleted`: `Boolean` (default = `false`)
  - `details`: `String` (default = `""`)

### **TaskEntity** (`TaskEntity.kt`)
- **Table Name**: `tasks`
- **Fields**:
  - `id`: `Long` (Primary Key, autoGenerate = true)
  - `title`: `String`
  - `estimatedMins`: `Int`
  - `isCompleted`: `Boolean` (default = `false`)
  - `date`: `String`

---

## 2. Data Access Objects (DAOs) & Repositories

### **HabitDao** (`HabitDao.kt`) & **HabitRepository** (`HabitRepository.kt`)
- Supports:
  - `getAllHabits()` returning `Flow<List<HabitEntity>>`
  - `getHabitById(id: Long)`
  - `insertHabit`, `updateHabit`, `deleteHabit`
  - `toggleHabitCompletion`

### **TaskDao** (`TaskDao.kt`) & **TaskRepository** (`TaskRepository.kt`)
- Supports:
  - `getAllTasks()` returning `Flow<List<TaskEntity>>`
  - `getTasksByDate(date: String)` returning `Flow<List<TaskEntity>>`
  - `getTaskById(id: Long)`
  - `insertTask`, `updateTask`, `deleteTask`
  - `toggleTaskCompletion`

---

## 3. UI Components & Adapters

### **HabitTaskAdapter** (`HabitTaskAdapter.kt`)
- Currently accepts `HabitEntity` items via `ListAdapter<HabitEntity, HabitTaskAdapter.HabitViewHolder>`.
- **Callbacks**:
  - `onToggle: (HabitEntity) -> Unit`
  - `onDelete: (HabitEntity) -> Unit` (currently hooked to `btn_options` in `onBindViewHolder`).
- **Item Layout** (`item_habit_task.xml`):
  - Contains `Checkbox` (`checkbox_complete`)
  - Category pill (`tv_category_pill`)
  - Title (`tv_title`)
  - Sub info (`tv_sub_info`)
  - Options button (`btn_options`)

---

## 4. HomeFragment & HomeViewModel

### **HomeFragment** (`HomeFragment.kt`)
- Currently observes `viewModel.habits` and submits them to `HabitTaskAdapter`.
- Manages `AddHabitBottomSheetFragment` via FAB (`fab_add_habit`).
- Currently wires `onToggle` and `onDelete` for habits.

### **HomeViewModel** (`HomeViewModel.kt`)
- Exposes `habits` StateFlow from `HabitRepository`.
- Exposes `userProgress` StateFlow from `UserRepository`.
- Handles `toggleHabitCompletion`, `addHabit`, and `deleteHabit`.
- *Note*: Task repository and task list are currently not integrated into `HomeViewModel` or `HomeFragment` (they currently display habits, while tasks are managed in `TaskRepository`).

---

## 5. Requirements & Next Steps for Update & Delete Options

1. **Unified or Task-specific Adapter**: Extend or generalize `HabitTaskAdapter` (or create a unified wrapper/sealed model or separate Task adapter) to support `TaskEntity` items alongside or instead of `HabitEntity`.
2. **ViewModel Integration**: Add `TaskRepository` to `HomeViewModel` (or create task flows and methods for updating/deleting tasks).
3. **Options Menu / Popup Menu**: Implement a popup menu (`PopupMenu`) on `btn_options` in the adapter item view to present **Edit (Update)** and **Delete** actions.
4. **Edit / Update Dialog / BottomSheet**: Create a bottom sheet or dialog to edit existing habit/task details (title, category/estimated mins, details, etc.).
