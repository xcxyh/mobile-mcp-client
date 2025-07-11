
// Project Architecture and Best Practices

const androidJetpackComposeBestPractices = [
    "Adapt to existing project architecture while maintaining clean code principles",
    "Follow Material Design 3 guidelines and components",
    "Implement clean architecture with domain, data, and presentation layers",
    "Use Kotlin coroutines and Flow for asynchronous operations",
    "Implement dependency injection using Koin",
    "Follow unidirectional data flow with ViewModel and UI State",
    "Use Compose navigation for screen management",
    "Implement proper state hoisting and composition",
];

// Folder Structure

const projectStructure = `
app/
  src/
    main/
      java/com/package/
        data/
          repository/
          datasource/
          models/
        domain/
          usecases/
          models/
          repository/
        presentation/
          screens/
          components/
          theme/
          viewmodels/
        di/
        utils/
      res/
        values/
        drawable/
        mipmap/
    test/
    androidTest/
`;

// Compose UI Guidelines

const composeGuidelines = `
1. Use remember and derivedStateOf appropriately
2. Implement proper recomposition optimization
3. Use proper Compose modifiers ordering
4. Follow composable function naming conventions
5. Implement proper preview annotations
6. Use proper state management with MutableState
7. Implement proper error handling and loading states
8. Use proper theming with MaterialTheme
9. Follow accessibility guidelines
10. Implement proper animation patterns
11. Automatic imports
12. NEVER use import *
`;

// Testing Guidelines

const testingGuidelines = `
1. Write unit tests for ViewModels and UseCases
2. Implement UI tests using Compose testing framework
3. Use fake repositories for testing
4. Implement proper test coverage
5. Use proper testing coroutine dispatchers
`;

// Performance Guidelines

const performanceGuidelines = `
1. Minimize recomposition using proper keys
2. Use proper lazy loading with LazyColumn and LazyRow
3. Implement efficient image loading
4. Use proper state management to prevent unnecessary updates
5. Follow proper lifecycle awareness
6. Implement proper memory management
7. Use proper background processing
`;

// Dependency management

const performanceGuidelines = `
1. Always use libs.versions.toml to manage and add dependencies
`;