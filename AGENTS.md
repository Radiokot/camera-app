# Project Specification for Agents

## Kotlin Style & Conventions

### Naming Conventions
- **Classes/Objects:** `PascalCase` (e.g., `StampsActivity`, `FsStampRepository`).
- **Functions/Methods:** `camelCase` (e.g., `getStamps`, `toStamp`).
- **Variables/Properties:** `camelCase` (e.g., `isCacheInitialized`, `stampDirectory`).
- **Constants:** `SCREAMING_SNAKE_CASE` (e.g., `EXTENSION_WEBP`).
- **UI Components (Compose):** `PascalCase` (e.g., `StampsScreen`, `StampsNavHost`).

### Trailing Commas
- Use trailing commas in multi-line parameter lists, argument lists, and collection literals.
- This applies to:
  - Function declarations and calls.
  - Class constructors.
  - When using `listOf`, `setOf`, `mapOf`, etc.
  - Jetpack Compose component arguments **expect** the last modifier

### Logging Format
- **Tool:** Use `kotlin-logging` (wrapper around Slf4j).
- **Declaration:** Use the `lazyLogger` extension function provided in `ua.com.radiokot.camerapp.util`.
- **Naming:** The logger name should be a short version of the class name (e.g., `StampsScreenVM` for `StampsScreenViewModel`).
- **Instance Variable:** Name the logger property `log`.
- **Usage:**
  ```kotlin
  private val log by lazyLogger("ShortClassName")
  
  // Use lambda-based logging for performance
  log.debug { 
    "methodName(): starting something:" +
        "\nparam1=$param1" +
        "\nparam2=$param2"
  }
  // When there are no params
  log.debug {
    "methodName(): starting something"
  }
  // When there's an error (Throwable)
  log.error(error) {
    "methodName(): failed something"
  }
  ```

### File Structure
- **Package name:** `ua.com.radiokot.camerapp` followed by the module/feature name.
- **Imports:** Grouped and sorted alphabetically (handled by IDE, but maintain order if editing manually).
- **Class Organization:**
  1. Properties (alphabetical or by logical grouping, `log` usually first).
  2. Init block.
  3. Public methods.
  4. Private methods.
  5. Companion object (at the end).

### Jetpack Compose
- Use `SharedTransitionLayout` and `SharedTransitionScope` where applicable for screen transitions.
- Prefer `collectAsState()` for `Flow` and `StateFlow` in composables.
- Use `LaunchedEffect` for side effects.
- Hoist state where possible.

### Testing
- You do not have access to bash. Do not try running any gradlew commands.
