# Koin Dependency Injection Guide for Kotlin Multiplatform

This comprehensive guide documents Koin dependency injection patterns for Kotlin Multiplatform projects following Clean Architecture principles.

## Table of Contents

1. [Overview](#overview)
2. [Koin Initialization](#koin-initialization)
3. [Module Organization](#module-organization)
4. [Scopes and Lifecycle](#scopes-and-lifecycle)
5. [Named Qualifiers](#named-qualifiers)
6. [Parameter Injection](#parameter-injection)
7. [ViewModel Injection](#viewmodel-injection)
8. [Platform-Specific DI](#platform-specific-di)
9. [Lazy Loading Strategy](#lazy-loading-strategy)
10. [Advanced Patterns](#advanced-patterns)
11. [Best Practices](#best-practices)
12. [Feature Implementation Guide](#feature-implementation-guide)

---

## Overview

This guide covers **Koin 4.x** dependency injection across Android, iOS, Desktop, and Web platforms. The DI architecture follows:

- **Clean Architecture Separation**: Modules organized by layer (Domain, Data, Presentation)
- **Feature Modules**: Self-contained DI for feature-specific components
- **Platform Abstraction**: Expect/Actual pattern for platform-specific implementations
- **Lazy Loading**: Deferred initialization for non-critical dependencies
- **Named Qualifiers**: Multiple bindings of the same type

---

## Koin Initialization

### Android Initialization

**Location**: `composeApp/src/androidMain/kotlin/[package]/Application.kt`

```kotlin
class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            // Android-specific context
            androidContext(applicationContext)

            // Lazy modules (deferred until first use)
            lazyModules(mapperModule)
            lazyModules(networkModule)
            lazyModules(dataSourceModule)

            // Eager modules (loaded immediately)
            modules(repositoryModule)
            modules(viewModelModule)
            modules(useCaseModule)
            modules(appModule)
            modules(platformModule)
            modules(coreModule)
        }
    }
}
```

### iOS Initialization

**Location**: `composeApp/src/iosMain/kotlin/[package]/KoinInit.kt`

```kotlin
fun initKoin() {
    startKoin {
        // No androidContext() for iOS
        modules(repositoryModule)
        modules(useCaseModule)
        modules(viewModelModule)
        lazyModules(mapperModule)
        modules(appModule)
        lazyModules(dataSourceModule)
        modules(platformModule)
        lazyModules(networkModule)
    }
}
```

**Key Differences:**
- iOS doesn't use `androidContext()`
- Both platforms share common modules
- Platform-specific modules use expect/actual pattern

---

## Module Organization

### Layer-Based Organization

```
DI Modules
├── Core/Infrastructure Layer
│   ├── networkModule (HttpClient, Json)
│   ├── platformModule (Platform-specific utilities)
│   ├── prefsModule (Preferences storage)
│   ├── analyticsModule (Analytics adapters)
│   └── utilsModule (Utilities, Coroutine Dispatchers)
│
├── Data Layer
│   ├── repositoryModule (Repository implementations)
│   ├── dataSourceModule (Remote/Local data sources)
│   └── mapperModule (DTO to Domain mappers)
│
├── Domain Layer
│   └── useCaseModule (Use case implementations)
│
├── Presentation Layer
│   └── viewModelModule (ViewModels)
│
└── Feature Modules
    └── [feature]Module
        ├── viewModelModule
        ├── useCaseModule
        ├── repositoryModule
        ├── dataSourceModule
        └── mapperModule
```

### Module File Structure

```
composeApp/src/commonMain/kotlin/[package]/di/
├── AppModule.kt               # Main aggregator module
├── RepositoryModule.kt        # Repository implementations
├── DataSourceModule.kt        # Data sources (lazy)
├── MapperModule.kt            # Mappers (lazy)
├── UseCaseModule.kt           # Use cases
└── ViewModelModule.kt         # ViewModels

core/[module]/src/commonMain/kotlin/[package]/di/
├── NetworkModule.kt           # Network setup (lazy)
├── PlatformModule.kt          # Platform utilities (expect/actual)
├── PreferencesModule.kt       # Preferences (expect/actual)
├── AnalyticsModule.kt         # Analytics (expect/actual)
└── UtilsModule.kt             # Utils & dispatchers

features/[feature]/src/commonMain/kotlin/[package]/di/
├── [Feature]Module.kt         # Feature aggregator
├── ViewModelModule.kt         # Feature ViewModels
├── UseCaseModule.kt           # Feature use cases
├── RepositoryModule.kt        # Feature repositories
├── DataSourceModule.kt        # Feature data sources
└── MapperModule.kt            # Feature mappers
```

### App Module (Main Aggregator)

```kotlin
val appModule = module {
    // Infrastructure
    includes(utilsModule)
    includes(preferencesModule)
    includes(networkModule)
    includes(platformModule)
    includes(analyticsModule)

    // Presentation
    includes(viewModelModule)

    // Features
    includes(featureAModule)
    includes(featureBModule)

    // Application-level singletons
    single<AppState> {
        AppState()
    }
}
```

### Feature Module Structure

```kotlin
// Feature aggregator module
val featureModule = module {
    includes(viewModelModule)
    includes(dataSourceModule)
    includes(mapperModule)
    includes(repositoryModule)
    includes(useCaseModule)
}

// Feature ViewModel module
internal val viewModelModule = module {
    viewModel {
        FeatureViewModel(
            useCase = get(),
            analytics = get()
        )
    }

    viewModel { parameters ->
        FeatureDetailViewModel(
            id = parameters.get(),
            detailUseCase = get()
        )
    }
}
```

---

## Scopes and Lifecycle

### Available Scopes

| Scope | Lifecycle | Usage |
|-------|-----------|-------|
| `single` | Application-wide singleton | Repositories, use cases, network clients |
| `factory` | New instance every time | Rarely used, for specific cases |
| `viewModel` | ViewModel lifecycle | All ViewModels |
| `scoped` | Named scope lifecycle | Feature flows with shared state |

### Single Scope (Singleton)

Most common scope for repositories, use cases, and services:

```kotlin
val repositoryModule = module {
    single<UserRepository> {
        UserRepositoryImpl(
            dataSource = get(),
            mapper = get(qualifier(USER_MAPPER))
        )
    }

    single<ProductRepository> {
        ProductRepositoryImpl(
            dataSource = get(),
            productMapper = get(qualifier(PRODUCT_MAPPER)),
            categoryMapper = get(qualifier(CATEGORY_MAPPER))
        )
    }
}
```

### Factory Scope (New Instance)

Used when you need a new instance every time:

```kotlin
val useCaseModule = module {
    factory<DownloadFileUseCase> {
        DownloadFileUseCaseImpl(get())
    }
}
```

### ViewModel Scope

Standard scope for all ViewModels:

```kotlin
val viewModelModule = module {
    // Simple ViewModel
    viewModel {
        MainViewModel(get())
    }

    // ViewModel with parameters
    viewModel { parameters ->
        DetailViewModel(
            id = parameters.get(),
            useCase = get()
        )
    }
}
```

### Named Scopes

For feature-specific lifecycle management:

```kotlin
val viewModelModule = module {
    scope(named("CheckoutScope")) {
        scoped { parameters ->
            CheckoutViewModel(
                checkoutUseCase = get(),
                productId = parameters.get(),
                analytics = get()
            )
        }
    }
}
```

**Usage:**
```kotlin
// Create scope
val checkoutScope = getKoin().createScope("checkout", named("CheckoutScope"))

// Get scoped instance
val viewModel = checkoutScope.get<CheckoutViewModel>()

// Close scope when done
checkoutScope.close()
```

### Coroutine Dispatcher Qualifiers

Special pattern for injecting different dispatchers:

```kotlin
enum class DispatcherType {
    Main,
    IO,
    Default,
    Unconfined
}

internal val coroutineDispatcherModule = module {
    single<CoroutineDispatcher>(qualifier(DispatcherType.Main)) {
        Dispatchers.Main
    }
    single<CoroutineDispatcher>(qualifier(DispatcherType.IO)) {
        Dispatchers.IO
    }
    single<CoroutineDispatcher>(qualifier(DispatcherType.Default)) {
        Dispatchers.Default
    }
}
```

**Usage:**
```kotlin
viewModel {
    DataProcessingViewModel(
        useCase = get(),
        ioDispatcher = get(qualifier(DispatcherType.IO)),
        defaultDispatcher = get(qualifier(DispatcherType.Default))
    )
}
```

---

## Named Qualifiers

Named qualifiers allow multiple bindings of the same type.

### Defining Qualifiers

**Location**: `core/common/src/commonMain/kotlin/[package]/di/Qualifiers.kt`

```kotlin
// Mapper Qualifiers
const val USER_MAPPER = "user_mapper"
const val PRODUCT_MAPPER = "product_mapper"
const val CATEGORY_MAPPER = "category_mapper"
const val ORDER_MAPPER = "order_mapper"

// Platform Qualifiers
const val PLATFORM_PREFS_KEY = "platform_prefs"
const val CACHE_PREFS_KEY = "cache_prefs"
```

### Registering Named Bindings

```kotlin
val mapperModule = lazyModule {
    // Simple named mapper
    single<Mapper<UserDto, User>>(qualifier(USER_MAPPER)) {
        UserMapper()
    }

    // Named mapper with dependencies
    single<Mapper<ProductDto, Product>>(qualifier(PRODUCT_MAPPER)) {
        ProductMapper(
            categoryMapper = get(qualifier(CATEGORY_MAPPER))
        )
    }

    // Complex nested mapper
    single<Mapper<OrderDto, Order>>(qualifier(ORDER_MAPPER)) {
        OrderMapper(
            productMapper = get(qualifier(PRODUCT_MAPPER)),
            userMapper = get(qualifier(USER_MAPPER))
        )
    }
}
```

### Using Named Bindings

```kotlin
val repositoryModule = module {
    single<UserRepository> {
        UserRepositoryImpl(
            dataSource = get(),
            mapper = get(qualifier(USER_MAPPER))
        )
    }

    single<OrderRepository> {
        OrderRepositoryImpl(
            dataSource = get(),
            orderMapper = get(qualifier(ORDER_MAPPER)),
            productMapper = get(qualifier(PRODUCT_MAPPER))
        )
    }
}
```

---

## Parameter Injection

Koin supports runtime parameter passing for ViewModels and other components.

### ViewModel Parameter Patterns

#### Single Parameter

```kotlin
// Registration
viewModel { parameters ->
    ProfileViewModel(
        userId = parameters.get(),
        userUseCase = get()
    )
}

// Usage in Composable
val viewModel = koinViewModel<ProfileViewModel>(
    parameters = { parametersOf(userId) }
)
```

#### Multiple Named Parameters

```kotlin
// Registration
viewModel { parameters ->
    ProductDetailViewModel(
        productId = parameters.get(),
        categoryId = parameters.get(),
        source = parameters.getOrNull(),  // Optional
        productUseCase = get()
    )
}

// Usage
val viewModel = koinViewModel<ProductDetailViewModel>(
    parameters = { parametersOf(productId, categoryId, source) }
)
```

#### Indexed Parameter Access

```kotlin
// Registration
viewModel { parameters ->
    SearchViewModel(
        query = parameters[0],
        category = parameters[1],
        minPrice = parameters[2],
        maxPrice = parameters[3],
        sortBy = parameters[4],
        searchUseCase = get(),
        analytics = get()
    )
}

// Usage
val viewModel = koinViewModel<SearchViewModel>(
    parameters = {
        parametersOf(query, category, minPrice, maxPrice, sortBy)
    }
)
```

#### Optional Parameters

```kotlin
// Registration
viewModel { parameters ->
    CheckoutViewModel(
        cartId = parameters.get(),  // Required
        couponCode = parameters.getOrNull(),  // Optional
        checkoutUseCase = get()
    )
}

// Usage
koinViewModel<CheckoutViewModel>(
    parameters = { parametersOf(cartId, couponCode) }
)
```

### Use Case Parameter Injection

```kotlin
// Registration
single<ValidateUserUseCase> { parameters ->
    ValidateUserUseCaseImpl(
        repository = get(),
        minAge = parameters.get(),
        maxRetries = parameters.get()
    )
}

// Usage
val useCase = get<ValidateUserUseCase> {
    parametersOf(minAge = 18, maxRetries = 3)
}
```

---

## ViewModel Injection

### In Composables

#### Pattern A: Navigation-Level Injection (Recommended)

```kotlin
fun NavGraphBuilder.featureNavigation() {
    composable<FeatureRoute> {
        // Inject ViewModel
        val viewModel = koinViewModel<FeatureViewModel>()

        // Collect state and effects
        val state = viewModel.uiState.collectAsStateWithLifecycle()
        val effect = viewModel.effect.collectAsStateWithLifecycle(
            FeatureViewModel.Effect.None
        )

        // Pass to screen
        FeatureScreen(
            state = state,
            effect = effect,
            onEvent = viewModel::handleAction
        )
    }
}
```

#### Pattern B: Screen-Level Injection

```kotlin
@Composable
fun ProductDetailScreen(
    productId: String,
) {
    // Inject ViewModel with parameters
    val viewModel = koinViewModel<ProductDetailViewModel>(
        parameters = { parametersOf(productId) }
    )

    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val effect by viewModel.effect.collectAsStateWithLifecycle(
        ProductDetailViewModel.Effect.None
    )

    // Screen content
    // ...
}
```

#### Pattern C: Multi-Parameter Injection

```kotlin
@Composable
fun SearchScreen(
    query: String,
    categoryId: String? = null,
    filters: SearchFilters = SearchFilters(),
) {
    val viewModel = koinViewModel<SearchViewModel>(
        parameters = {
            parametersOf(query, categoryId, filters)
        }
    )

    val effect by viewModel.effect.collectAsStateWithLifecycle(
        SearchViewModel.Effect.None
    )
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    // Screen content
    // ...
}
```

### ViewModel Registration

```kotlin
val viewModelModule = module {
    // Simple ViewModel (no parameters)
    viewModel {
        HomeViewModel(get())
    }

    // ViewModel with dependencies
    viewModel {
        ProfileViewModel(
            userUseCase = get(),
            analyticsUseCase = get(),
            preferences = get()
        )
    }

    // ViewModel with parameters
    viewModel { parameters ->
        ProductDetailViewModel(
            productId = parameters.get(),
            productUseCase = get()
        )
    }

    // Scoped ViewModel
    scope(named("CheckoutScope")) {
        scoped { parameters ->
            CheckoutViewModel(
                cartId = parameters.get(),
                checkoutUseCase = get()
            )
        }
    }
}
```

---

## Platform-Specific DI

Use expect/actual pattern for platform-specific implementations.

### Common Module (expect)

```kotlin
internal expect val platformSpecificModule: Module

val platformModule = module {
    includes(platformSpecificModule)

    single<DeviceInfoUseCase> {
        DeviceInfoUseCaseImpl(get())
    }
}
```

### Android Module (actual)

```kotlin
actual val platformSpecificModule: Module = module {
    single {
        DeviceInfo(context = get())
    }

    single<Context> {
        get<Application>()
    }
}
```

### iOS Module (actual)

```kotlin
actual val platformSpecificModule: Module = module {
    single {
        DeviceInfo()
    }
}
```

### Preferences Module with Platform Qualifiers

**Common:**
```kotlin
const val PLATFORM_PREFS_KEY = "platform_prefs"
const val CACHE_PREFS_KEY = "cache_prefs"

internal expect val platformSpecificPrefsModule: Module

val preferencesModule = module {
    includes(platformSpecificPrefsModule)

    single<UserPreferences> {
        UserPreferencesImpl(get(qualifier(PLATFORM_PREFS_KEY)))
    }

    single<CacheManager> {
        CacheManagerImpl(get(qualifier(CACHE_PREFS_KEY)))
    }
}
```

**Android:**
```kotlin
actual val platformSpecificPrefsModule: Module = module {
    single<ObservableSettings>(
        createdAtStart = true,
        qualifier = qualifier(PLATFORM_PREFS_KEY)
    ) {
        SharedPreferencesSettings(
            get<Context>().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        )
    }

    single<ObservableSettings>(
        createdAtStart = true,
        qualifier = qualifier(CACHE_PREFS_KEY)
    ) {
        SharedPreferencesSettings(
            get<Context>().getSharedPreferences("cache", Context.MODE_PRIVATE)
        )
    }
}
```

**iOS:**
```kotlin
actual val platformSpecificPrefsModule: Module = module {
    single<ObservableSettings>(
        createdAtStart = true,
        qualifier = qualifier(PLATFORM_PREFS_KEY)
    ) {
        NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
    }

    single<ObservableSettings>(
        createdAtStart = true,
        qualifier = qualifier(CACHE_PREFS_KEY)
    ) {
        NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
    }
}
```

### Analytics Module (Platform-Specific)

**Common:**
```kotlin
val analyticsModule = module {
    includes(platformSpecificAnalyticsModule)

    single<AnalyticsDispatcher> {
        AnalyticsDispatcher(get())
    }
}
```

**Android:**
```kotlin
actual val platformSpecificAnalyticsModule: Module = module {
    single<Map<AnalyticsProvider, AnalyticsAdapter>> {
        mapOf(
            AnalyticsProvider.FIREBASE to FirebaseAnalytics(get()),
            AnalyticsProvider.CUSTOM to CustomAnalytics()
        )
    }
}
```

**iOS:**
```kotlin
actual val platformSpecificAnalyticsModule: Module = module {
    single<Map<AnalyticsProvider, AnalyticsAdapter>> {
        mapOf(
            AnalyticsProvider.FIREBASE to FirebaseAnalytics(),
            AnalyticsProvider.CUSTOM to CustomAnalytics()
        )
    }
}
```

---

## Lazy Loading Strategy

Lazy modules defer initialization until first access.

### Network Module (Lazy)

```kotlin
val networkModule = lazyModule {
    single<HttpClient> {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    explicitNulls = false
                })
            }
            install(Logging) {
                level = LogLevel.INFO
            }
        }
    }

    single<Json> {
        Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }
}
```

### Mapper Module (Lazy)

```kotlin
val mapperModule = lazyModule {
    single<UserMapper> { UserMapper() }
    single<ProductMapper> { ProductMapper() }
    single<OrderMapper> { OrderMapper() }
    // ... more mappers
}
```

### Data Source Module (Lazy)

```kotlin
val dataSourceModule = lazyModule {
    single<UserRemoteDataSource> {
        UserRemoteDataSourceImpl(httpClient = get())
    }

    single<ProductRemoteDataSource> {
        ProductRemoteDataSourceImpl(httpClient = get())
    }
    // ... more data sources
}
```

### Loading Strategy

```kotlin
startKoin {
    // Lazy modules (deferred)
    lazyModules(mapperModule)
    lazyModules(networkModule)
    lazyModules(dataSourceModule)

    // Eager modules (immediate)
    modules(repositoryModule)
    modules(viewModelModule)
    modules(useCaseModule)
    modules(appModule)
}
```

---

## Advanced Patterns

### Lazy Evaluation Inside Bindings

```kotlin
single<RemoteConfigDataSource> {
    val configProvider: ConfigProvider by lazy { get() }
    RemoteConfigDataSourceImpl(
        httpClient = get(),
        apiKey = lazy { configProvider.getApiKey() }
    )
}
```

### Service Registry Pattern

```kotlin
single<Map<ServiceType, ServiceAdapter>> {
    mapOf(
        ServiceType.ANALYTICS to AnalyticsService(get()),
        ServiceType.LOGGING to LoggingService(get()),
        ServiceType.CRASH_REPORTING to CrashReportingService()
    )
}
```

### Parametrized Mapper with Dependencies

```kotlin
single<Mapper<ComplexDto, ComplexDomain>>(
    qualifier("ComplexMapper")
) { parameters: ParametersHolder ->
    ComplexMapper(
        userMapper = get(qualifier(USER_MAPPER)),
        productMapper = get(qualifier(PRODUCT_MAPPER)),
        configParam = parameters.get()
    )
}
```

---

## Best Practices

### 1. Module Organization

**DO:**
- Separate modules by layer (Data, Domain, Presentation)
- Create feature modules for self-contained features
- Use `includes()` to compose modules hierarchically
- Keep related bindings together

**DON'T:**
- Mix layer concerns in single module
- Create circular module dependencies
- Duplicate bindings across modules

### 2. Scoping

**DO:**
- Use `single` for repositories, use cases, network clients
- Use `viewModel` for all ViewModels
- Use `factory` only when you need new instances
- Use named scopes for feature flows

**DON'T:**
- Overuse `factory` (impacts performance)
- Create unnecessary singletons
- Mix scope types without clear reason

### 3. Naming

**DO:**
- Use descriptive qualifier names (e.g., `USER_PROFILE_MAPPER`)
- Use consistent naming conventions
- Use UPPER_SNAKE_CASE for qualifier constants
- Document complex qualifier usage

**DON'T:**
- Use generic names like `MAPPER_1`, `MAPPER_2`
- Mix naming conventions
- Use magic strings directly

### 4. Parameters

**DO:**
- Use `parameters.get()` for required parameters
- Use `parameters.getOrNull()` for optional parameters
- Document expected parameter types
- Use indexed access for large parameter lists

**DON'T:**
- Pass too many parameters (>5, consider refactoring)
- Rely on parameter order without documentation
- Mix optional and required without clarity

### 5. Lazy Loading

**DO:**
- Use `lazyModule` for non-critical dependencies
- Load network, mappers, data sources lazily
- Load repositories, use cases eagerly
- Profile startup time impact

**DON'T:**
- Lazy load critical path dependencies
- Over-optimize without measurement
- Create deep lazy chains

### 6. Platform-Specific

**DO:**
- Use expect/actual for platform differences
- Keep common logic in common modules
- Use qualifiers for platform-specific instances
- Document platform differences

**DON'T:**
- Duplicate logic across platforms
- Mix platform code in common modules
- Create platform-specific public APIs

---

## Feature Implementation Guide

### Step 1: Create Feature Module Structure

```
features/[feature]/src/commonMain/kotlin/[package]/[feature]/
├── di/
│   ├── [Feature]Module.kt
│   ├── ViewModelModule.kt
│   ├── UseCaseModule.kt
│   ├── RepositoryModule.kt
│   ├── DataSourceModule.kt
│   └── MapperModule.kt
├── domain/
│   ├── model/
│   ├── usecase/
│   └── repository/
├── data/
│   ├── repository/
│   ├── datasource/
│   ├── model/
│   └── mapper/
└── presentation/
    └── [screens]/
```

### Step 2: Define Feature Module

```kotlin
val featureModule = module {
    includes(viewModelModule)
    includes(useCaseModule)
    includes(repositoryModule)
    includes(dataSourceModule)
    includes(mapperModule)
}
```

### Step 3: Create Mapper Module

```kotlin
const val FEATURE_ITEM_MAPPER = "feature_item_mapper"
const val FEATURE_DETAIL_MAPPER = "feature_detail_mapper"

internal val mapperModule = module {
    single<Mapper<FeatureItemDto, FeatureItem>>(
        qualifier(FEATURE_ITEM_MAPPER)
    ) {
        FeatureItemMapper()
    }

    single<Mapper<FeatureDetailDto, FeatureDetail>>(
        qualifier(FEATURE_DETAIL_MAPPER)
    ) {
        FeatureDetailMapper(
            itemMapper = get(qualifier(FEATURE_ITEM_MAPPER))
        )
    }
}
```

### Step 4: Create Data Source Module

```kotlin
internal val dataSourceModule = module {
    single<FeatureRemoteDataSource> {
        FeatureRemoteDataSourceImpl(httpClient = get())
    }
}
```

### Step 5: Create Repository Module

```kotlin
internal val repositoryModule = module {
    single<FeatureRepository> {
        FeatureRepositoryImpl(
            dataSource = get(),
            itemMapper = get(qualifier(FEATURE_ITEM_MAPPER)),
            detailMapper = get(qualifier(FEATURE_DETAIL_MAPPER))
        )
    }
}
```

### Step 6: Create Use Case Module

```kotlin
internal val useCaseModule = module {
    single<GetFeatureItemsUseCase> {
        GetFeatureItemsUseCaseImpl(
            repository = get()
        )
    }

    single<GetFeatureDetailUseCase> {
        GetFeatureDetailUseCaseImpl(
            repository = get()
        )
    }
}
```

### Step 7: Create ViewModel Module

```kotlin
internal val viewModelModule = module {
    viewModel {
        FeatureListViewModel(
            getItemsUseCase = get(),
            analytics = get()
        )
    }

    viewModel { parameters ->
        FeatureDetailViewModel(
            itemId = parameters.get(),
            getDetailUseCase = get()
        )
    }
}
```

### Step 8: Register in AppModule

```kotlin
val appModule = module {
    // ... existing includes
    includes(featureModule)
}
```

### Step 9: Use in Navigation

```kotlin
fun NavGraphBuilder.featureNavigation() {
    composable<FeatureRoute.List> {
        val viewModel = koinViewModel<FeatureListViewModel>()
        val state = viewModel.uiState.collectAsStateWithLifecycle()
        val effect = viewModel.effect.collectAsStateWithLifecycle(
            FeatureListViewModel.Effect.None
        )

        FeatureListScreen(
            state = state,
            effect = effect,
            onEvent = viewModel::handleAction
        )
    }
}
```

---

## Quick Reference

### Common Koin Functions

| Function | Usage | Example |
|----------|-------|---------|
| `get()` | Retrieve dependency | `val repo: UserRepo = get()` |
| `get<T>()` | Retrieve with explicit type | `get<UserRepo>()` |
| `get(qualifier(...))` | Retrieve named binding | `get(qualifier(USER_MAPPER))` |
| `getOrNull()` | Retrieve optional dependency | `val config = getOrNull<Config>()` |
| `koinViewModel<T>()` | Inject ViewModel in Composable | `koinViewModel<HomeViewModel>()` |
| `parametersOf(...)` | Pass parameters | `parametersOf(id, name)` |
| `parameters.get()` | Extract required parameter | `id = parameters.get()` |
| `parameters.getOrNull()` | Extract optional parameter | `config = parameters.getOrNull()` |
| `parameters[index]` | Extract by index | `id = parameters[0]` |

### Module DSL

| Function | Scope | Usage |
|----------|-------|-------|
| `module { }` | N/A | Define module |
| `lazyModule { }` | N/A | Define lazy module |
| `single { }` | Application | Singleton |
| `factory { }` | Transient | New instance every time |
| `viewModel { }` | ViewModel | ViewModel lifecycle |
| `scoped { }` | Named scope | Scoped instance |
| `scope(named("X")) { }` | N/A | Define named scope |
| `includes(module)` | N/A | Include other module |
| `qualifier("name")` | N/A | Create named qualifier |

---

## Summary

This Koin DI guide covers:

1. **Initialization**: Platform-specific startup (Android/iOS)
2. **Organization**: Layer-based and feature-based module structure
3. **Scopes**: Single, factory, viewModel, scoped patterns
4. **Qualifiers**: Named bindings for multiple instances of same type
5. **Parameters**: Runtime parameter passing for ViewModels and components
6. **ViewModels**: Registration and injection in Composables
7. **Platform-Specific**: Expect/actual pattern for platform implementations
8. **Lazy Loading**: Deferred initialization strategy
9. **Advanced**: Service registry, lazy evaluation, composition patterns
10. **Best Practices**: Guidelines for maintainable DI
11. **Feature Guide**: Step-by-step feature implementation

**Key Takeaways:**
- Use `single` for most components (repositories, use cases)
- Use `viewModel` scope for all ViewModels
- Use named qualifiers for multiple bindings of same type
- Leverage lazy loading for non-critical dependencies
- Use expect/actual for platform-specific implementations
- Organize by layer and feature for scalability
- Pass parameters via `parametersOf()` for runtime values

Follow these patterns for consistent, maintainable dependency injection across your KMP project.
