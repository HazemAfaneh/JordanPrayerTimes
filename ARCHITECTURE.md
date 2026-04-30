# KMP Clean Architecture Guide

This document provides comprehensive architectural patterns for implementing new features in Kotlin Multiplatform projects following Clean Architecture principles.

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Domain Layer](#domain-layer)
3. [Data Layer](#data-layer)
4. [Presentation Layer](#presentation-layer)
5. [Dependency Injection](#dependency-injection)
6. [Feature Implementation Checklist](#feature-implementation-checklist)

---

## Architecture Overview

The architecture follows a three-layer clean architecture pattern:

```
┌──────────────────────────────────────────────────┐
│           Presentation Layer                      │
│  (ViewModels, UiState, Actions, Effects)         │
└──────────────────┬───────────────────────────────┘
                   │
┌──────────────────▼───────────────────────────────┐
│             Domain Layer                          │
│    (Use Cases, Repository Interfaces,            │
│     Domain Models, Business Logic)                │
└──────────────────┬───────────────────────────────┘
                   │
┌──────────────────▼───────────────────────────────┐
│              Data Layer                           │
│  (Repository Impls, Data Sources,                │
│   DTOs, Mappers, Network)                        │
└──────────────────────────────────────────────────┘
```

**Key Principles:**
- Dependencies point inward (Presentation → Domain ← Data)
- Domain layer has no platform dependencies
- Use cases encapsulate single business operations
- Repositories abstract data source details
- Immutable data models throughout

---

## Domain Layer

### Use Cases

Use cases represent single business operations or user intents. They encapsulate business logic and orchestrate data flow.

#### Pattern Structure

Every use case consists of two files:

1. **Interface** - Defines the contract
2. **Implementation** - Implements business logic (suffixed with `Imp`)

#### Naming Conventions

- **Interface**: `[Domain]UseCase` (e.g., `CheckAppVersionUseCase`, `HomeUseCase`)
- **Implementation**: `[Domain]UseCaseImp` (e.g., `CheckAppVersionUseCaseImp`, `HomeUseCaseImp`)
- **Methods**:
  - Standard: `get*()`, `fetch*()`, `check*()`, `toggle*()`, `submit*()`
  - Operator invoke: `suspend operator fun invoke(params): ResultData<T>`
  - Reactive: Non-suspend functions returning `Flow<T>` or `StateFlow<T>`

#### Use Case Examples

**Example 1: Simple Wrapper Use Case**

```kotlin
// Interface
interface CheckAppVersionUseCase {
    suspend fun isAppNeedUpdates(): ResultData<String?>
}

// Implementation
class CheckAppVersionUseCaseImp(
    private val appServiceRepo: AppServiceRepo
) : CheckAppVersionUseCase {
    override suspend fun isAppNeedUpdates(): ResultData<String?> {
        return appServiceRepo.getAppVersion()
    }
}
```

**Example 2: Multi-Method Use Case**

```kotlin
// Interface
interface HomeUseCase {
    suspend fun getHomeData(): ResultData<HomeData>
    suspend fun getCarRecommendationData(
        params: Map<String, String?>?
    ): ResultData<List<SearchResultItemData>?>
    suspend fun getDealsData(): ResultData<List<Deal>>
}

// Implementation
class HomeUseCaseImp(
    private val repo: HomeRepo
) : HomeUseCase {
    override suspend fun getHomeData(): ResultData<HomeData> {
        return repo.fetchHomeData()
    }

    override suspend fun getCarRecommendationData(
        params: Map<String, String?>?
    ): ResultData<List<SearchResultItemData>?> {
        return repo.fetchCarRecommendationData(params = params)
    }

    override suspend fun getDealsData(): ResultData<List<Deal>> {
        return repo.fetchDealsData()
    }
}
```

**Example 3: Operator Invoke Pattern**

```kotlin
// Interface
interface PromoCodeUseCase {
    suspend operator fun invoke(
        payLoad: Map<String, String>
    ): ResultData<PromoCodeData>
}

// Implementation
class PromoCodeUseCaseImp(
    private val checkoutRepo: CheckoutRepo
) : PromoCodeUseCase {
    override suspend fun invoke(
        payLoad: Map<String, String>
    ): ResultData<PromoCodeData> {
        return checkoutRepo.submitPromoCode(payLoad)
    }
}
```

**Example 4: Use Case with Local State & Business Logic**

```kotlin
// Interface
interface FavoriteUseCase {
    suspend fun addPendingFavorite(postID: Int)
    suspend fun toggleFavorite(postID: Int): ResultData<Boolean>
    suspend fun getFavoriteListData(): ResultData<FavoriteData?>
    suspend fun isFavorite(postId: Int): Boolean
    suspend fun clear(): ResultData<Unit>
    fun isFavouriteFlow(postID: Int): Flow<Boolean>
    fun isFavouritePending(postID: Int): Boolean
}

// Implementation
class FavoriteUseCaseImp(
    private val repo: FavoriteRepo,
    private val local: FavouriteLocalCache
) : FavoriteUseCase {
    private val coroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default
    )
    private var favouriteList = MutableStateFlow(emptyList<Int>())
    private var pendingFavourite: Int? = null

    init {
        coroutineScope.launch {
            local.getFavoritesFlow().collectLatest { newList ->
                favouriteList.update { newList }
            }
        }
    }

    override fun isFavouriteFlow(postID: Int): Flow<Boolean> =
        favouriteList.map { it.any { it == postID } }

    override suspend fun toggleFavorite(postID: Int): ResultData<Boolean> {
        return when (val result = repo.toggleFavorite(postID)) {
            is ResultData.Error -> result
            is ResultData.Success -> {
                pendingFavourite = null
                postID?.toInt()?.let {
                    local.toggleFavorite(it)
                } ?: run {
                    ResultData.Error(
                        DataError.Local.InternalCacheException(
                            Exception("Post id is null")
                        )
                    )
                }
            }
        }
    }

    // ... other methods
}
```

**Example 5: Reactive Pattern (Flow/StateFlow)**

```kotlin
// Interface
interface UserLoginStateFlowUseCase {
    operator fun invoke(): StateFlow<String?>
}

// Implementation
class UserLoginStateFlowUseCaseImp(
    private val appPref: AppPref
) : UserLoginStateFlowUseCase {
    override fun invoke(): StateFlow<String?> = appPref.isUserLogged()
}
```

---

### Repository Interfaces

Repository interfaces define data access contracts. They are always suspended functions returning `ResultData<T>`.

#### Naming Conventions

- **Interface**: `[Domain]Repo` (e.g., `HomeRepo`, `CheckoutRepo`, `FavoriteRepo`)
- **Methods**: `fetch*()`, `submit*()`, `create*()`, `get*()`, etc.

#### Repository Examples

**Example 1: Simple Repository**

```kotlin
interface HomeRepo {
    suspend fun fetchHomeData(): ResultData<HomeData>
    suspend fun fetchCarRecommendationData(
        params: Map<String, String?>?
    ): ResultData<List<SearchResultItemData>?>
    suspend fun fetchDealsData(): ResultData<List<Deal>>
}
```

**Example 2: Complex Repository with Multiple Operations**

```kotlin
interface CheckoutRepo {
    suspend fun submitCashForm(
        cashFormSubmitData: CashFormSubmitData
    ): ResultData<String>

    suspend fun submitPromoCode(
        payLoad: Map<String, String>
    ): ResultData<PromoCodeData>

    suspend fun createFinanceOrder(
        financePersonalInfoRequestData: FinancePersonalInfoFormRequest
    ): ResultData<FinanceSubmitFormResponse>

    suspend fun getFinanceUploadDocuments(
        orderUUID: String
    ): ResultData<RequiredDocumentsModel>

    suspend fun submitJobInfo(
        data: FinanceSubmitJobInfoRequest
    ): ResultData<FinanceSubmitFormResponse>

    suspend fun getPresignedUrl(
        params: JsonObject
    ): ResultData<Map<String, String>?>

    suspend fun sendUploadDocumentsToBackend(
        jsonObject: JsonObject
    ): ResultData<Boolean?>
}
```

---

### Domain Models

Domain models are pure data classes representing business entities.

#### Characteristics

- Use `@Serializable` for serialization support
- All fields should have default values where appropriate
- Use `@SerialName` for JSON mapping (if needed)
- Immutable (val properties)
- No platform-specific dependencies

#### Examples

**Example 1: User Model**

```kotlin
@Serializable
data class UserData(
    @SerialName("phone_number") val phoneNumber: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("city_id") val city: Int? = null,
    @SerialName("token") val token: String? = null,
    @SerialName("id") var id: Int? = null,
    @SerialName("is_verified") var hasAccount: Boolean? = false,
    @SerialName("opt_whatsapp_interest")
    var isSubscribedToWhatsappNotification: Boolean = false,
    @SerialName("favorites") var favoriteList: List<String>? = null
)
```

**Example 2: Complex Nested Model**

```kotlin
data class HomeData(
    val make: List<HomeBrowse>? = null,
    val millage: List<HomeBrowse>? = null,
    val price: List<HomeBrowse>? = null,
    val gridCards: List<HomeGridCard>? = null,
    val banner: List<HeroBannerItem> = emptyList(),
    val uspsWarranty: USPS? = null,
    val uspsEasyBuy: USPS? = null,
    val posts: HomePost? = null,
    val hotDeals: List<HomePost>? = null,
    val installmentRanges: HomePost? = null,
    val homeOffer: HomeOffer? = null,
    val userFeedback: List<UserFeedback>? = null,
    val faq: List<FaqData>? = null
)
```

---

### Error Handling

The architecture uses a sealed `ResultData<T>` type for type-safe error handling.

#### ResultData Definition

```kotlin
sealed interface ResultData<out T> {
    data class Success<out T>(val value: T) : ResultData<T>
    data class Error(val error: DataError) : ResultData<Nothing>
}

// Extension functions for convenience
inline fun <T, R> ResultData<T>.ifSuccess(action: (T) -> R): R? {
    return when (this) {
        is ResultData.Success -> action(value)
        is ResultData.Error -> null
    }
}

inline fun <T, R> ResultData<T>.ifError(action: (DataError) -> R): R? {
    return when (this) {
        is ResultData.Success -> null
        is ResultData.Error -> action(error)
    }
}
```

#### DataError Hierarchy

```kotlin
sealed interface DataError {

    sealed class Remote : DataError {
        data object EmptyResponse : DataError.Remote()
        data object RequestTimeOut : DataError.Remote()
        data object TooManyRequest : DataError.Remote()
        data object NoInternet : DataError.Remote()
        data object Server : DataError.Remote()
        data object Unknown : DataError.Remote()
        data object Serialization : DataError.Remote()
        data class Validation(
            val errors: List<ValidationError>?
        ) : DataError.Remote()
    }

    sealed class Local : DataError {
        data object Memory : DataError.Local()
        data class SaveToCache(val exception: Exception) : DataError.Local()
        data object ResultMappingException : DataError.Local()
        data class InternalCacheException(
            val exception: Exception
        ) : DataError.Local()
        data class IncorrectUserDataException(
            val message: String
        ) : DataError.Local()
    }
}
```

#### Error Handling Patterns

**Pattern 1: Direct Error Propagation**
```kotlin
class CheckAppVersionUseCaseImp(
    private val appServiceRepo: AppServiceRepo
) : CheckAppVersionUseCase {
    override suspend fun isAppNeedUpdates(): ResultData<String?> {
        return appServiceRepo.getAppVersion()
    }
}
```

**Pattern 2: When-Based Error Handling**
```kotlin
override suspend fun toggleFavorite(postID: Int): ResultData<Boolean> {
    return when (val result = repo.toggleFavorite(postID)) {
        is ResultData.Error -> result
        is ResultData.Success -> {
            pendingFavourite = null
            postID?.toInt()?.let {
                local.toggleFavorite(it)
            } ?: run {
                ResultData.Error(
                    DataError.Local.InternalCacheException(
                        Exception("Post id is null")
                    )
                )
            }
        }
    }
}
```

---

### Domain Layer Directory Structure

```
domain/src/commonMain/kotlin/
├── model/                    # Domain models
│   ├── UserData.kt
│   ├── ResultData.kt
│   ├── DataError.kt
│   └── home/
│       └── HomeData.kt
├── repo/                     # Repository interfaces
│   ├── HomeRepo.kt
│   ├── CheckoutRepo.kt
│   └── FavoriteRepo.kt
└── usecase/                  # Use cases
    ├── appVersion/
    │   ├── CheckAppVersionUseCase.kt
    │   └── CheckAppVersionUseCaseImp.kt
    ├── home/
    │   ├── HomeUseCase.kt
    │   └── HomeUseCaseImp.kt
    └── favorite/
        ├── FavoriteUseCase.kt
        └── FavoriteUseCaseImp.kt
```

---

## Data Layer

### Repository Implementations

Repository implementations coordinate between remote data sources, mappers, and error handling.

#### Pattern Structure

```kotlin
class [Domain]RepoImp(
    private val apiService: [Domain]RemoteDataSource,
    private val mapper: Mapper<RemoteModel, DomainModel>,
    // Additional mappers as needed
) : [Domain]Repo {
    // Implementation
}
```

#### Repository Examples

**Example 1: Simple Mapping Pattern**

```kotlin
class HomeRepoImp(
    private val apiService: HomeRemoteDataSource,
    private val homeDataMapper: Mapper<HomeDataRemote, HomeData>,
    private val dealMapper: Mapper<DealsDataRemote, Deal>
) : HomeRepo {

    override suspend fun fetchHomeData(): ResultData<HomeData> {
        val include = "home,testimonial_videos,contact_us,faq,deal_offers,..."
        return apiService.fetchHomeData(include).map {
            homeDataMapper.mapTo(it)
        }
    }

    override suspend fun fetchDealsData(): ResultData<List<Deal>> {
        return apiService.fetchDealsData().map {
            it?.deals?.map { dealRemote ->
                dealMapper.mapTo(dealRemote)
            } ?: emptyList()
        }
    }
}
```

**Example 2: Complex Multi-Mapper Pattern**

```kotlin
class CheckoutRepoImp(
    private val checkoutService: PaymentRemoteDataSource,
    private val cashFormDataRemoteMapper: Mapper<CashFormSubmitData, CashFormSubmitRemoteData>,
    private val requiredDocumentRemoteMapper: Mapper<RequiredDocumentsRemoteModel, RequiredDocumentsModel>,
    private val promoCodeRemoteMapper: Mapper<PromoCodeRemoteData, PromoCodeData>,
    private val financeFormResponseRemoteMapper: Mapper<FinanceFormResponseRemote, FinanceSubmitFormResponse>
) : CheckoutRepo {

    override suspend fun submitCashForm(
        cashFormSubmitData: CashFormSubmitData
    ): ResultData<String> {
        return checkoutService.submitCashForm(
            cashFormDataRemoteMapper.mapTo(cashFormSubmitData)
        ).map {
            it?.orderId ?: "N/A"
        }
    }

    override suspend fun createFinanceOrder(
        financePersonalInfoRequestData: FinancePersonalInfoFormRequest
    ): ResultData<FinanceSubmitFormResponse> {
        return checkoutService.createFinanceOrder(
            FinancePersonalInfoFormRequestRemoteData(
                gender = financePersonalInfoRequestData.gender ?: "",
                fullName = financePersonalInfoRequestData.applicantName ?: "",
                // ... field mapping ...
            )
        ).map {
            financeFormResponseRemoteMapper.mapTo(it)
        }
    }
}
```

---

### Remote Data Sources

Remote data sources encapsulate HTTP calls using Ktor and return `ResultData<T>`.

#### Pattern Structure

```kotlin
class [Domain]RemoteDataSourceImp(
    private val httpClient: HttpClient
) : [Domain]RemoteDataSource {
    // HTTP calls
}
```

#### Remote Data Source Examples

**Example 1: GET Requests**

```kotlin
class HomeRemoteDataSourceImp(
    private val httpClient: HttpClient
) : HomeRemoteDataSource {

    override suspend fun fetchHomeData(
        include: String
    ): ResultData<HomeDataRemote?> {
        val endpoint = "/$apiV1/site/new-home"
        return httpClient.call<HomeDataRemote> {
            url(endpoint)
            parameter("include", include)
            method = HttpMethod.Get
        }
    }

    override suspend fun fetchCarRecommendationData(
        params: Map<String, String?>?
    ): ResultData<List<SearchResultItemDataRemote>?> {
        val endpoint = "/$apiSearchVersion/search/recommended"
        return httpClient.call<List<SearchResultItemDataRemote>> {
            url(endpoint)
            params?.map { parameter(it.key, it.value) }
            method = HttpMethod.Get
        }
    }
}
```

**Example 2: POST Requests with JSON Body**

```kotlin
class PaymentRemoteDataSourceImp(
    private val httpClient: HttpClient
) : PaymentRemoteDataSource {

    override suspend fun submitCashForm(
        cashFormSubmitData: CashFormSubmitRemoteData
    ): ResultData<CashFormSubmitResponseRemoteData?> {
        val endpoint = "/$apiV1/order/create"
        return httpClient.call<CashFormSubmitResponseRemoteData> {
            contentType(ContentType.Application.Json)
            url(endpoint)
            setBody(cashFormSubmitData)
            method = HttpMethod.Post
        }
    }

    override suspend fun getPresignedUrl(
        params: JsonObject
    ): ResultData<Map<String, String>?> {
        val endpoint = "/$apiV1/order-tracking/get-presigned-url"
        return httpClient.call<Map<String, String>?> {
            contentType(ContentType.Application.Json)
            url(endpoint)
            setBody(params)
            method = HttpMethod.Post
        }
    }
}
```

---

### Mappers

Mappers implement a generic `Mapper<T, I>` interface to transform DTOs to domain models.

#### Mapper Interface

```kotlin
interface Mapper<T, I> {
    fun mapTo(t: T?): I
}
```

#### Mapper Examples

**Example 1: Simple Mapper**

```kotlin
class PromoCodeRemoteMapper : Mapper<PromoCodeRemoteData, PromoCodeData> {
    override fun mapTo(t: PromoCodeRemoteData?): PromoCodeData {
        return PromoCodeData(
            errorMessage = if (t?.errorMessage.isNullOrBlank())
                null
            else
                t?.errorMessage,
            title = t?.title,
            promoCodeId = t?.referralCodeId,
            isValid = t?.isValid == true,
            cashReward = RewardItemModel(
                amount = t?.rewardsRemoteModel?.cashRewardRemoteModel?.amount,
                id = t?.rewardsRemoteModel?.cashRewardRemoteModel?.id,
                status = t?.rewardsRemoteModel?.cashRewardRemoteModel?.status,
                type = t?.rewardsRemoteModel?.cashRewardRemoteModel?.type,
            )
        )
    }
}
```

**Example 2: Complex Composite Mapper**

```kotlin
class HomeDataMapper(
    private val makeRemoteMapper: Mapper<MakesHomeDataRemote, HomeBrowse>,
    private val priceBrowseRemoteMapper: Mapper<PriceRangeDataRemote, HomeBrowse>,
    private val millageBrowseRemoteMapper: Mapper<MilageRangeDataRemote, HomeBrowse>,
    private val heroRemoteMapper: Mapper<HerosAreaDataRemote, HeroBannerItem>,
    private val postMapper: Mapper<HomeSectionDataRemote, HomePost>,
    private val homeOfferMapper: Mapper<HomeSectionOfferDataRemote, HomeOffer>,
    private val uspsRemoteMapper: Mapper<USPSRemote, USPS>,
    private val userFeedbackMapper: Mapper<SurveyData, UserFeedback>,
    private val homeGridMapper: Mapper<HomeGridCardDataRemote, HomeGridCard>,
    private val faqMapper: Mapper<FaqDataRemote, FaqData>
) : Mapper<HomeDataRemote, HomeData> {

    override fun mapTo(remote: HomeDataRemote?): HomeData {
        return HomeData(
            gridCards = remote?.gridCards?.map {
                homeGridMapper.mapTo(it)
            },
            make = remote?.makes?.values?.map {
                makeRemoteMapper.mapTo(it)
            }?.toMutableList()?.apply {
                add(HomeBrowse(isAllButton = true))
            },
            price = remote?.priceRanges?.map {
                priceBrowseRemoteMapper.mapTo(it)
            }?.toMutableList()?.apply {
                add(HomeBrowse(isAllButton = true))
            },
            banner = remote?.herosArea?.map {
                heroRemoteMapper.mapTo(it)
            } ?: emptyList(),
            uspsWarranty = uspsRemoteMapper.mapTo(
                remote?.homepageSliders?.get(0)
            ),
            userFeedback = remote?.usersFeedback?.surveyList?.map {
                userFeedbackMapper.mapTo(it)
            },
            faq = remote?.faq?.map { faqMapper.mapTo(it) }
        )
    }
}
```

---

### DTO Models (Remote Models)

DTOs represent network response structures. They use `@Serializable` with `@SerialName` for JSON mapping.

#### DTO Characteristics

- All fields nullable with default `null`
- Use `@SerialName` for snake_case JSON fields
- Kotlin properties use camelCase
- Nested data classes for complex structures

#### DTO Examples

**Example 1: Simple DTO**

```kotlin
@Serializable
data class PromoCodeRemoteData(
    @SerialName("code_id")
    val codeId: Int? = null,
    @SerialName("error")
    val errorMessage: String? = null,
    @SerialName("icon")
    val icon: String? = null,
    @SerialName("referral_code_id")
    val referralCodeId: Int? = null,
    @SerialName("rewards")
    val rewardsRemoteModel: RewardsRemoteModel? = null,
    @SerialName("title")
    val title: String? = null,
    @SerialName("valid")
    val isValid: Boolean? = null
)
```

**Example 2: Complex Nested DTO**

```kotlin
@Serializable
data class HomeDataRemote(
    @SerialName("deal_offers")
    val dealOffers: HomeSectionOfferDataRemote? = null,
    @SerialName("all_posts")
    val defaultPost: HomeSectionDataRemote? = null,
    @SerialName("hot_deals")
    val hotDeals: List<HomeSectionDataRemote>? = null,
    @SerialName("installment_ranges")
    val installmentRanges: HomeSectionDataRemote? = null,
    @SerialName("faq")
    val faq: List<FaqDataRemote>? = null,
    @SerialName("heros_area_v2")
    val herosArea: List<HerosAreaDataRemote>? = null,
    @SerialName("homepage_sliders")
    val homepageSliders: List<USPSRemote>? = null,
    @SerialName("makes")
    val makes: MakeHomeValueDataRemote? = null,
    @SerialName("search_section")
    val gridCards: List<HomeGridCardDataRemote>? = null,
    @SerialName("odometer_ranges")
    val milageRanges: List<MilageRangeDataRemote>? = null,
    @SerialName("price_ranges")
    val priceRanges: List<PriceRangeDataRemote>? = null,
    @SerialName("users_feedback")
    val usersFeedback: UsersFeedbackDataRemote? = null,
)
```

---

### Network Error Handling

Network errors are automatically mapped to `DataError` types through HTTP client extensions.

#### HTTP Client Extension

```kotlin
suspend inline fun <reified T> HttpClient.call(
    block: HttpRequestBuilder.() -> Unit
): ResultData<T?> = try {
    val response = request(block)
    val apiResponse = response.body<ApiResponse<T>>()

    when {
        response.status.isSuccess() && apiResponse.success == true -> try {
            ResultData.Success(apiResponse.data)
        } catch (e: Exception) {
            ResultData.Error(DataError.Remote.Serialization)
        }

        response.status in arrayOf(
            UnprocessableEntity,
            HttpStatusCode.Forbidden
        ) || apiResponse.code == 230 || apiResponse.code == 200 -> {
            ResultData.Error(
                apiResponse.validationErrors?.let {
                    parseValidationError(it)
                } ?: apiResponse.errors?.let {
                    parseErrors(apiResponse.errors)
                } ?: DataError.Remote.Serialization
            )
        }

        response.status.value == 408 -> {
            ResultData.Error(DataError.Remote.RequestTimeOut)
        }

        response.status.value in 500..599 -> {
            ResultData.Error(DataError.Remote.Server)
        }

        else -> ResultData.Error(DataError.Remote.Unknown)
    }
} catch (e: SocketTimeoutException) {
    logException(e.message ?: "uncaught exception", e)
    ResultData.Error(DataError.Remote.RequestTimeOut)
} catch (e: UnresolvedAddressException) {
    logException(e.message ?: "uncaught exception", e)
    ResultData.Error(DataError.Remote.NoInternet)
} catch (e: Exception) {
    val message: String = e.message?.ifEmpty { "" } ?: ""
    when {
        message.contains("Unable to resolve host") ||
        message.contains("1009") ||
        message.contains("host could not be found") ->
            ResultData.Error(DataError.Remote.NoInternet)
        message.contains("timed out") ->
            ResultData.Error(DataError.Remote.RequestTimeOut)
        else -> ResultData.Error(DataError.Remote.Unknown)
    }
}
```

#### ResultData Map Extension

```kotlin
fun <T, R> ResultData<T>.map(
    transform: (T) -> R
): ResultData<R> {
    return when (this) {
        is ResultData.Success -> try {
            ResultData.Success(transform(this.value))
        } catch (e: Exception) {
            ResultData.Error(DataError.Local.ResultMappingException)
        }
        is ResultData.Error -> this
    }
}
```

---

### Data Layer Directory Structure

```
data/src/commonMain/kotlin/
├── network/
│   ├── remoteDataSource/
│   │   ├── home/
│   │   │   ├── HomeRemoteDataSource.kt
│   │   │   └── HomeRemoteDataSourceImp.kt
│   │   └── checkout/
│   │       ├── PaymentRemoteDataSource.kt
│   │       └── PaymentRemoteDataSourceImp.kt
│   ├── model/
│   │   ├── response/
│   │   │   └── home/
│   │   │       └── HomeDataRemote.kt
│   │   └── payment/
│   │       └── PromoCodeRemoteData.kt
│   ├── mapper/
│   │   ├── Mapper.kt
│   │   ├── home/
│   │   │   └── HomeDataMapper.kt
│   │   └── checkout/
│   │       └── PromoCodeRemoteMapper.kt
│   └── ktor/
│       ├── KtorSetup.kt
│       └── HTTPClientExt.kt
└── repo/
    ├── HomeRepoImp.kt
    └── CheckoutRepoImp.kt
```

---

## Presentation Layer

### ViewModel Architecture

The presentation layer uses **AndroidX ViewModel** with coroutines and flows for state management.

#### ViewModel Pattern Structure

```kotlin
class [Feature]ViewModel(
    private val useCase: [Feature]UseCase,
    // Other dependencies
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState
        .onStart {
            // Initial loading
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = _uiState.value
        )

    private val _effect: Channel<Effect> = Channel()
    val effect = _effect.receiveAsFlow()

    fun actionTrigger(action: UIAction) {
        when (action) {
            // Handle actions
        }
    }

    data class UiState(/* state properties */)

    sealed class UIAction {
        // Action definitions
    }

    sealed interface Effect {
        // Effect definitions
    }
}
```

---

### UiState Data Class

UiState represents the entire UI state as an immutable data class.

#### UiState Characteristics

- Immutable (all `val` properties)
- All state in single data class
- Default values for all properties
- Updated using `.copy()`
- Can include nested state objects

#### UiState Examples

**Example 1: Simple UiState**

```kotlin
data class UIState(
    val isLoading: LoadingType = LoadingType.NONE,
    val phoneNumber: String = "",
    val loginBanner: LoginBanner = LoginBanner(),
    val loginScreenStep: LoginScreenStep = LoginScreenStep.PHONE_NUMBER_SCREEN,
    val loginRequestReferral: LoginRequestReferral = LoginRequestReferral.Account,
    val referralLocation: AnalyticEventLocationReferral = AnalyticEventLocationReferral.ACCOUNT,
    val validationError: List<ValidationError> = emptyList(),
    val isBottomSheet: Boolean = false,
)
```

**Example 2: Complex UiState with Nested Objects**

```kotlin
data class UiState(
    val isLoading: Boolean = true,
    val isUserLoggedIn: Boolean = false,
    val isUploadDocumentsFailed: Boolean? = null,
    val isFromFinanceCalculator: Boolean? = null,
    val currentTab: Int = 0,
    val g4Data: JsonObject? = null,
    val cashTotalPrice: String? = null,
    val isSubmitLoading: Boolean = false,
    val isNewPost: Boolean? = null,
    val showWarningEditNumberBottomSheet: Boolean = false,
    val name: String = "",
    val backButtonScreenName: String? = null,
    val paymentUrl: String = "",
    val promoCode: String? = "",
    val promoCodeId: String? = null,
    val orderId: String = "",
    val postId: String = "",
    val phoneNumber: String = "",
    val cities: List<CityUIData> = emptyList(),
    val selectedCity: CityUIData? = null,
    val postCity: Int? = null,
    val cashReward: RewardItemModel? = null,
    val promoCodeSuccessMessage: String? = null,
    val paymentPostData: PaymentPostData? = null,
    val storylyUiStyleId: Int = 0,
    val showFinanceCheckoutOption: Boolean = true,
    val financeType: FinanceTypeEnum = FinanceTypeEnum.NONE,
    val snackBarHostState: SnackbarHostState = SnackbarHostState(),
    val listOfPostDetails: List<Price>? = null,
    val validationError: List<ValidationError> = emptyList(),
    val requiredDocumentsModel: RequiredDocumentsModel? = null,
    val selectedFiles: Map<String?, DocumentData?> = mapOf(),
    val financeCalculatorData: CarFinanceCalculatorTypes? = null,
)
```

**Example 3: UiState with Callbacks (Advanced)**

```kotlin
@Immutable
data class FinanceFormState(
    val firstName: String = "",
    val familyName: String = "",
    val mobileNumber: String = "",
    val originalMobileNumber: String = "",
    val isLoading: Boolean = true,
    val g4Data: JsonObject? = null,
    val whatsAppVerificationChecked: Boolean = false,
    val selectedCity: DropDownItem? = null,
    val isFormValid: Boolean = false,
    val validationErrors: List<ValidationError> = emptyList(),
    val cities: List<DropDownItem> = emptyList(),
    val error: DataError? = null,
    val userId: String? = null,
    val postId: String? = null,
    val isTamaraLoading: Boolean = false,
    val tamaraResult: TamaraFormModel? = null,
    val tamaraError: DataError? = null,
    val postDetails: PaymentPostData? = null,
    val listOfPostDetails: List<Price>? = null,
    val isUserLoggedIn: Boolean = false,
    val showLoginBottomSheet: Boolean = false,
    val showWarningEditNumberBottomSheet: Boolean = false,
    val totalAmount: String? = null
)
```

---

### UIAction Sealed Class

UIActions represent user intents and events that trigger state changes.

#### UIAction Characteristics

- Sealed class or sealed interface
- Each action is a data class or object
- Named with descriptive verbs
- Can carry payload data

#### UIAction Examples

**Example 1: Comprehensive Action Set**

```kotlin
sealed class UIAction {
    data class LoadPostData(
        val postId: String,
        val financePriceData: FinancePriceData? = null,
    ) : UIAction()

    data class OnCityChange(val cityId: Int) : UIAction()

    data class OnFaqClicked(val questionNumber: String) : UIAction()

    data class SubmitPromoCode(val promoCode: String) : UIAction()

    data class RemoveValidation(val formField: FormField) : UIAction()

    data object RefreshAndGoBack : UIAction()

    data class SubmitCashForm(
        val cashFormSubmitData: CashFormSubmitData
    ) : UIAction()

    data object GoToPreviousTab : UIAction()

    data object HideWarningEditNumberBottomSheet : UIAction()

    data object NavigateToFinanceFlow : UIAction()

    data object HideLoginBottomSheet : UIAction()

    data class CheckChangedMobileNumber(
        val cashFormSubmitData: CashFormSubmitData
    ) : UIAction()

    data object OpenVATPDF : UIAction()

    data object GoToNextTab : UIAction()

    data object NavigateBackToPostDetails : UIAction()

    data object NavigateBack : UIAction()

    data object NavigateToTrackingOrder : UIAction()

    data object NavigateToHome : UIAction()

    data class LoadRequiredDocument(val orderUUID: String) : UIAction()

    data object OTPVerified : UIAction()

    data class NavigateToCheckoutScreen(
        val paymentMethod: PaymentMethod,
        val postId: String,
        val actionData: ActionUiData? = null
    ) : UIAction()

    data class OnTrailingContentAction(
        val actionData: ActionUiData
    ) : UIAction()

    data object LoadSiteData : UIAction()

    data class LogoutThenRedirectToOtpTab(
        val cashFormSubmitData: CashFormSubmitData
    ) : UIAction()

    data object LoadFinancingConditionsRequiredDocuments : UIAction()

    data object OTPChangePhoneNumber : UIAction()

    data object SubmitFinanceForm : UIAction()

    data class NavigateToScreen(
        val screenName: String,
        val payload: String? = null
    ) : UIAction()

    data class OpenUri(val uri: String) : UIAction()

    data object CloseUploadDocumentsStatusDialog : UIAction()

    data class SelectFileToUpload(val file: DocumentData) : UIAction()

    data class RemoveUploadFile(val documentKey: String) : UIAction()

    data class OpenPdfPreview(val file: Any) : UIAction()

    class SetBackButtonScreen(val screenName: String?) : UIAction()
}
```

**Example 2: Intent Pattern (Alternative Naming)**

```kotlin
sealed interface Intent {
    data object SendOTP : Intent
    data object ReSendOTP : Intent
    data class Login(val phoneNumber: String) : Intent
    data class GoTo(val effect: Effect) : Intent
    data class VerifyOTP(val otp: String) : Intent
    data object ShowTermsAndCondition : Intent
    data object ShowPrivacyPolicy : Intent
    data object EditMobileNumber : Intent
}
```

**Example 3: Form Actions (Granular Updates)**

```kotlin
sealed class FinancePersonalInfoFormAction {
    data class UpdateName(val name: String) : FinancePersonalInfoFormAction()

    data class UpdateFinancePricing(
        val financePriceData: FinancePriceData
    ) : FinancePersonalInfoFormAction()

    data class UpdateGender(
        val selectedGender: MultiButtonsSelectionFieldOptionData
    ) : FinancePersonalInfoFormAction()

    data class UpdateIdNumber(val idNumber: String) : FinancePersonalInfoFormAction()

    data class UpdateSelectedGender(
        val selectedGender: MultiButtonsSelectionFieldOptionData
    ) : FinancePersonalInfoFormAction()

    data class UpdateIsWhatsappUpdatesSelected(
        val isWhatsappUpdatesSelected: Boolean
    ) : FinancePersonalInfoFormAction()

    data class UpdateSelectedCity(
        val selectedCity: DropDownItem?
    ) : FinancePersonalInfoFormAction()

    data class UpdateCities(
        val cities: List<DropDownItem>
    ) : FinancePersonalInfoFormAction()

    data class UpdateNameErrors(
        val nameErrors: List<String>
    ) : FinancePersonalInfoFormAction()

    data object ClearForm : FinancePersonalInfoFormAction()

    data class LogoutThenRedirectToOtp(
        val phoneNumber: String
    ) : FinancePersonalInfoFormAction()

    data class SubmitForm(val phoneNumber: String) : FinancePersonalInfoFormAction()
}
```

---

### Effect Sealed Interface

Effects represent one-time events or side effects (navigation, toasts, dialogs).

#### Effect Characteristics

- Sealed interface (preferred) or sealed class
- Represent non-state UI events
- Consumed once by the UI
- Often navigation or user feedback

#### Effect Examples

**Example 1: Comprehensive Effect Set**

```kotlin
sealed interface Effect {
    data class NavigateToCheckout(
        val paymentMethod: PaymentMethod,
        val postId: String
    ) : Effect

    data class NavigateToCashFlow(val postId: String) : Effect

    data class NavigateToFinanceFlow(val postId: String) : Effect

    data class NavigateToAribFinanceFlow(val aribUrl: String) : Effect

    data class NavigateToIntegrationFlow(
        val postId: String,
        val totalAmount: String
    ) : Effect

    data class ShowToast(val error: DataError) : Effect

    data object NavigateBack : Effect

    data object NavigateBackToPostDetails : Effect

    data object NavigateToHome : Effect

    data class NavigateToTrackingOrder(val url: String) : Effect

    data class OpenPdfPreview(val file: Any) : Effect

    data class UploadDocumentPreviewFile(
        val title: String,
        val imageBitmap: SerializedImageBitmap,
    ) : Effect

    data class NavigateToRejectedByBankScreen(
        val rejScreen: RejScreen
    ) : Effect

    data class NavigateToActiveOrderScreen(
        val rejScreen: RejScreen
    ) : Effect

    data class GoToWhatsApp(val mobileNumber: String) : Effect

    data class GoToDialer(val mobileNumber: String) : Effect

    data class NavigateToWebView(val url: String) : Effect

    data object ShowHowToFinanceBottomSheet : Effect

    data class ShowTamaraDialog(val amount: String) : Effect

    data class NavigateToStoryly(
        val storylyId: String,
        val storyType: String
    ) : Effect

    data object None : Effect
}
```

**Example 2: Simple Effect Set**

```kotlin
sealed interface Effect {
    data object NONE : Effect
    data object OTPVerified : Effect
    data object DismissDialog : Effect
    data object SuccessCallback : Effect
    data class GoToTermsAndCondition(val url: String) : Effect
    data class GoToPrivacyPolicy(val url: String) : Effect
    data class ShowToast(val error: DataError) : Effect
}
```

---

### State Flow Management

State is managed using `MutableStateFlow` with `StateIn` operator for lifecycle-aware collection.

#### State Flow Setup Pattern

```kotlin
private val _uiState = MutableStateFlow(UiState())

val uiState: StateFlow<UiState> = _uiState
    .onStart {
        // Initial loading logic
        actionTrigger(UIAction.LoadPostData(postId, financePriceData))
        actionTrigger(UIAction.LoadSiteData)
        observeUserStatus()

        _uiState.update {
            it.copy(
                financeType = runCatching {
                    FinanceTypeEnum.fromValue(remoteConfig.getFinanceType())
                }.getOrDefault(FinanceTypeEnum.NONE),
                storylyUiStyleId = if (condition) 0 else 1
            )
        }
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = _uiState.value
    )
```

#### Effect Channel Setup

```kotlin
private val _effect: Channel<Effect> = Channel()
val effect = _effect.receiveAsFlow()
```

Or with StateIn for UI collection:

```kotlin
private val _effect: Channel<FinanceFormEffect?> = Channel()
val effect = _effect.receiveAsFlow().stateIn(
    scope = viewModelScope,
    started = SharingStarted.Lazily,
    initialValue = null
)
```

---

### Action Trigger Function

The `actionTrigger` function acts as the central dispatcher for all user actions.

#### Pattern Structure

```kotlin
fun actionTrigger(action: UIAction) {
    when (action) {
        is UIAction.SomeAction -> {
            // Handle synchronously
        }

        is UIAction.AsyncAction -> {
            viewModelScope.launch {
                // Handle asynchronously
                handleResult(
                    result = useCase.someMethod(action.params),
                    onSuccess = { data ->
                        _uiState.update {
                            it.copy(/* update state */)
                        }
                        _effect.send(Effect.Success)
                    },
                    onError = { error ->
                        _effect.send(Effect.ShowToast(error))
                    }
                )
            }
        }
    }
}
```

#### Complete Action Trigger Example

```kotlin
fun actionTrigger(action: UIAction) {
    when (action) {
        is UIAction.OnFaqClicked -> {
            analyticsEventDispatcher.dispatchEvent(
                FinanceFaqClicked(questionNumber = action.questionNumber)
            )
        }

        is UIAction.NavigateToFinanceFlow -> {
            viewModelScope.launch {
                analyticsEventDispatcher.dispatchEvent(
                    ContinueFinanceClicked(
                        postId = postId,
                        carModel = uiState.value.g4Data?.getValue("post_model").toString(),
                        carMake = uiState.value.g4Data?.getValue("post_make").toString(),
                        carYear = uiState.value.g4Data?.getValue("post_year").toString(),
                    )
                )
                _effect.send(
                    Effect.NavigateToFinanceFlow(postId = postId)
                )
            }
        }

        is UIAction.SubmitCashForm -> {
            viewModelScope.launch {
                _uiState.update {
                    it.copy(isSubmitLoading = true)
                }

                actionTrigger(UIAction.HideWarningEditNumberBottomSheet)

                handleResult(
                    result = cashFormSubmitUseCase.invoke(
                        action.cashFormSubmitData.apply {
                            postId = this@PaymentViewModel.postId
                            promoCodeId = uiState.value.promoCodeId
                        }
                    ),
                    onSuccess = { orderId ->
                        _uiState.update {
                            it.copy(
                                name = action.cashFormSubmitData.fullName ?: "",
                                phoneNumber = action.cashFormSubmitData.phoneNumber ?: "",
                                selectedCity = it.cities.firstOrNull { city ->
                                    city.id.toString() == action.cashFormSubmitData.cityId
                                },
                                isSubmitLoading = false,
                                orderId = orderId,
                                validationError = emptyList(),
                            )
                        }

                        actionTrigger(UIAction.GoToNextTab)

                        analyticsEventDispatcher.dispatchEvent(
                            CashFormSubmitEvent(
                                orderId = orderId,
                                postId = action.cashFormSubmitData.postId,
                                totalAmount = uiState.value.cashTotalPrice,
                                userCity = uiState.value.selectedCity?.enName,
                                discountCode = uiState.value.promoCode,
                                g4Data = uiState.value.g4Data
                            )
                        )
                    },
                    onError = { error ->
                        errorHandler(error)
                    }
                )
            }
        }

        is UIAction.LoadPostData -> {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }

                handleResult(
                    result = paymentPostDataUseCase.invoke(action.postId),
                    onSuccess = { data ->
                        _uiState.update {
                            it.copy(
                                paymentPostData = data,
                                g4Data = data.g4Data,
                                isLoading = false
                            )
                        }
                    },
                    onError = { error ->
                        _uiState.update { it.copy(isLoading = false) }
                        _effect.send(Effect.ShowToast(error))
                    }
                )
            }
        }
    }
}
```

---

### State Update Patterns

#### Pattern 1: Using Flow.update() (Preferred)

```kotlin
_uiState.update {
    it.copy(
        isSubmitLoading = true,
        showWarningEditNumberBottomSheet = false
    )
}
```

#### Pattern 2: Direct Value Assignment (Less Common)

```kotlin
_uiState.value = _uiState.value.copy(
    isLoading = LoadingType.NONE,
    validationError = it.errors.orEmpty()
)
```

#### Pattern 3: Conditional Updates

```kotlin
_uiState.update { currentState ->
    if (condition) {
        currentState.copy(field1 = value1)
    } else {
        currentState.copy(field2 = value2)
    }
}
```

---

### Effect Channel Usage

#### Emitting Effects from ViewModel

```kotlin
viewModelScope.launch {
    _effect.send(Effect.NavigateToFinanceFlow(postId = postId))
}
```

Or:

```kotlin
viewModelScope.launch {
    _effect.send(Effect.ShowToast(error))
}
```

#### Collecting Effects in Composables

```kotlin
@Composable
fun PaymentScreen(
    viewModel: PaymentViewModel,
    navigation: Navigator?
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val effect by viewModel.effect.collectAsStateWithLifecycle(Effect.None)

    LaunchedEffect(effect) {
        when (effect) {
            is Effect.NavigateToCheckout -> {
                navigation?.navigate(
                    CheckoutDest(
                        paymentMethod = (effect as Effect.NavigateToCheckout).paymentMethod,
                        postId = (effect as Effect.NavigateToCheckout).postId
                    )
                )
            }

            is Effect.ShowToast -> {
                ToastHandler.showToast(
                    (effect as Effect.ShowToast).error.mapResourceMessage(),
                    ToastDuration.SHORT
                )
            }

            is Effect.NavigateBack -> {
                navigation?.navigateUp()
            }

            is Effect.OpenPdfPreview -> {
                // Handle PDF preview
            }

            Effect.None -> {
                // Do nothing
            }
        }
    }

    // UI content
}
```

---

### Advanced Pattern: Nested State Management

For complex forms with sub-states, use extension functions for action processing.

#### Nested State Extension Function

```kotlin
fun FinancePersonalInfoFormState.actionTrigger(
    action: FinancePersonalInfoFormAction,
    onFinancePriceUpdate: (FinancePriceData) -> Unit
): FinancePersonalInfoFormState {
    return when (action) {
        is FinancePersonalInfoFormAction.UpdateFinancePricing -> {
            onFinancePriceUpdate.invoke(action.financePriceData)
            copy(financePriceData = action.financePriceData)
        }

        is FinancePersonalInfoFormAction.UpdateName -> copy(
            name = action.name,
            nameErrors = emptyList()
        )

        is FinancePersonalInfoFormAction.UpdateSelectedCity -> copy(
            selectedCity = action.selectedCity,
            cityErrors = emptyList()
        )

        is FinancePersonalInfoFormAction.ClearForm -> {
            copy(
                name = "",
                idNumber = "",
                phoneNumber = "",
                selectedGender = null,
                isWhatsappUpdatesSelected = false,
                selectedCity = null,
                nameErrors = emptyList(),
                idNumberErrors = emptyList(),
                phoneNumberErrors = emptyList(),
                cityErrors = emptyList(),
                genderErrors = emptyList(),
            )
        }

        is FinancePersonalInfoFormAction.SubmitForm -> {
            checkChangedMobileNumberBeforeSubmit(action.phoneNumber)
            this
        }
    }
}
```

#### Parent ViewModel Integration

```kotlin
private fun financePersonalInfoFormActionsTriggers() {
    viewModelScope.launch {
        _financePersonalInfoFormActions.collect { intent ->
            _financePersonalInfoFormState.update {
                it.actionTrigger(intent) { financePricing ->
                    _financePaymentDetailsSectionState.update { state ->
                        state.copy(
                            financePaymentDetails = financePricing.copy(
                                header = state.financePaymentDetails.header
                            ),
                        )
                    }
                }
            }
        }
    }
}
```

---

### Presentation Layer Best Practices

1. **Single Source of Truth**: All UI state in one `UiState` data class
2. **Immutability**: Never mutate state directly, always use `.copy()`
3. **Effects for One-Time Events**: Use Effect channel for navigation, toasts
4. **Action Trigger**: Single entry point for all user actions
5. **Coroutine Scoping**: Use `viewModelScope` for all async operations
6. **Error Handling**: Always handle both success and error cases
7. **Analytics**: Dispatch analytics events within action handlers
8. **State Updates**: Use `.update { }` for thread-safe state mutations
9. **Lifecycle Awareness**: Use `collectAsStateWithLifecycle()` in composables
10. **Default Values**: Always provide defaults in UiState

---

## Dependency Injection

### Koin Configuration

The project uses Koin for dependency injection across all layers.

#### Module Structure

```
di/
├── DomainModuleDI.kt        # Use case bindings
├── RepoModuleDI.kt          # Repository bindings
├── MapperModule.kt          # Mapper bindings with qualifiers
├── RemoteDataSourceModule.kt # Data source bindings
└── ViewModelModule.kt       # ViewModel bindings
```

#### Repository Module Example

```kotlin
val repoModule = module {
    single<HomeRepo> {
        HomeRepoImp(
            apiService = get(),
            homeDataMapper = get(qualifier = qualifier(HOME_DATA_MAPPER)),
            dealMapper = get(qualifier = qualifier(DEAL_MAPPER))
        )
    }

    single<CheckoutRepo> {
        CheckoutRepoImp(
            checkoutService = get(),
            cashFormDataRemoteMapper = get(qualifier(SUBMIT_CASH_FORM_MAPPER)),
            requiredDocumentRemoteMapper = get(qualifier(REQUIRED_DOCUMENT_MAPPER)),
            promoCodeRemoteMapper = get(qualifier(PROMO_CODE_MAPPER)),
            financeFormResponseRemoteMapper = get(qualifier(FINANCE_JOB_INFO_MAPPER)),
        )
    }

    single<FavoriteRepo> {
        FavoriteRepoImp(
            apiService = get(),
            favoriteDataMapper = get(qualifier(FAVORITE_DATA_MAPPER))
        )
    }
}
```

#### Mapper Module with Qualifiers

```kotlin
const val HOME_DATA_MAPPER = "home_data_mapper"
const val DEAL_MAPPER = "deal_mapper"
const val PROMO_CODE_MAPPER = "promo_code_mapper"
const val FAVORITE_DATA_MAPPER = "favorite_data_mapper"

val mapperModule = lazyModule {
    single<Mapper<HomeDataRemote, HomeData>>(qualifier(HOME_DATA_MAPPER)) {
        HomeDataMapper(
            makeRemoteMapper = get(qualifier(HOME_MAKE_BROWSE_MAPPER)),
            priceBrowseRemoteMapper = get(qualifier(HOME_PRICE_BROWSE_MAPPER)),
            millageBrowseRemoteMapper = get(qualifier(HOME_MILLAGE_BROWSE_MAPPER)),
            heroRemoteMapper = get(qualifier(HOME_HERO_MAPPER)),
            postMapper = get(qualifier(HOME_POST_MAPPER)),
            homeOfferMapper = get(qualifier(HOME_OFFER_MAPPER)),
            uspsRemoteMapper = get(qualifier(HOME_USPS_MAPPER)),
            userFeedbackMapper = get(qualifier(HOME_USER_FEEDBACK_MAPPER)),
            homeGridMapper = get(qualifier(HOME_GRID_MAPPER)),
            faqMapper = get(qualifier(HOME_FAQ_MAPPER))
        )
    }

    single<Mapper<PromoCodeRemoteData, PromoCodeData>>(qualifier(PROMO_CODE_MAPPER)) {
        PromoCodeRemoteMapper()
    }

    single<Mapper<DealsDataRemote, Deal>>(qualifier(DEAL_MAPPER)) {
        DealMapper()
    }
}
```

#### Use Case Module Example

```kotlin
val domainModule = module {
    single<CheckAppVersionUseCase> {
        CheckAppVersionUseCaseImp(appServiceRepo = get())
    }

    single<HomeUseCase> {
        HomeUseCaseImp(repo = get())
    }

    single<FavoriteUseCase> {
        FavoriteUseCaseImp(
            repo = get(),
            local = get()
        )
    }

    single<PromoCodeUseCase> {
        PromoCodeUseCaseImp(checkoutRepo = get())
    }
}
```

#### ViewModel Module Example

```kotlin
val viewModelModule = module {
    viewModel {
        PaymentViewModel(
            postId = it.get(),
            financePriceData = it.getOrNull(),
            cashFormSubmitUseCase = get(),
            promoCodeUseCase = get(),
            paymentPostDataUseCase = get(),
            citiesDataUseCase = get(),
            analyticsEventDispatcher = get(),
            // ... other dependencies
        )
    }

    viewModel {
        LoginViewModel(
            loginUseCase = get(),
            loginScreenData = it.get(),
            remoteConfigPlatform = get(),
            analyticsEventDispatcher = get(),
        )
    }
}
```

#### Remote Data Source Module

```kotlin
val remoteDataSourceModule = module {
    single<HomeRemoteDataSource> {
        HomeRemoteDataSourceImp(httpClient = get())
    }

    single<PaymentRemoteDataSource> {
        PaymentRemoteDataSourceImp(httpClient = get())
    }
}
```

---

## Feature Implementation Checklist

Use this checklist when implementing a new feature following Clean Architecture.

### 1. Domain Layer

- [ ] **Define Domain Model**
  - [ ] Create data class in `domain/model/`
  - [ ] Add `@Serializable` annotation
  - [ ] Use appropriate `@SerialName` for JSON fields
  - [ ] Provide default values

- [ ] **Create Repository Interface**
  - [ ] Create interface in `domain/repo/`
  - [ ] Name as `[Feature]Repo`
  - [ ] Define suspend functions returning `ResultData<T>`

- [ ] **Create Use Case Interface**
  - [ ] Create interface in `domain/usecase/[feature]/`
  - [ ] Name as `[Feature]UseCase`
  - [ ] Define business operation methods

- [ ] **Implement Use Case**
  - [ ] Create implementation class with `Imp` suffix
  - [ ] Inject repository dependencies
  - [ ] Implement business logic
  - [ ] Handle errors appropriately

### 2. Data Layer

- [ ] **Create DTO Models**
  - [ ] Create remote data classes in `data/network/model/`
  - [ ] Add `@Serializable` and `@SerialName`
  - [ ] Make all fields nullable with defaults

- [ ] **Create Mapper**
  - [ ] Create mapper class in `data/network/mapper/`
  - [ ] Implement `Mapper<RemoteModel, DomainModel>`
  - [ ] Handle null safety and transformations

- [ ] **Create Remote Data Source Interface**
  - [ ] Create interface in `data/network/remoteDataSource/`
  - [ ] Define HTTP operation methods

- [ ] **Implement Remote Data Source**
  - [ ] Create implementation with `Imp` suffix
  - [ ] Inject HttpClient
  - [ ] Use `httpClient.call<T>` for requests
  - [ ] Return `ResultData<T>`

- [ ] **Implement Repository**
  - [ ] Create implementation in `data/repo/`
  - [ ] Name as `[Feature]RepoImp`
  - [ ] Inject data source and mappers
  - [ ] Use `.map()` to transform DTOs to domain models

### 3. Presentation Layer

- [ ] **Define UiState**
  - [ ] Create immutable data class
  - [ ] Add all UI-related state properties
  - [ ] Provide default values

- [ ] **Define UIAction**
  - [ ] Create sealed class/interface
  - [ ] Add all possible user actions
  - [ ] Use descriptive names with payloads

- [ ] **Define Effect**
  - [ ] Create sealed interface
  - [ ] Add one-time events (navigation, toasts)
  - [ ] Include `None` or default effect

- [ ] **Create ViewModel**
  - [ ] Extend `androidx.lifecycle.ViewModel`
  - [ ] Inject use cases and dependencies
  - [ ] Create `MutableStateFlow<UiState>`
  - [ ] Create `Channel<Effect>`
  - [ ] Expose flows with `.stateIn()`
  - [ ] Implement `actionTrigger(action: UIAction)`
  - [ ] Handle all actions in when expression

- [ ] **Create Composable Screen**
  - [ ] Collect `uiState` with `collectAsStateWithLifecycle()`
  - [ ] Collect `effect` with `LaunchedEffect`
  - [ ] Handle effects (navigation, toasts)
  - [ ] Call `viewModel.actionTrigger()` for user events

### 4. Dependency Injection

- [ ] **Register Mapper**
  - [ ] Add to `MapperModule.kt`
  - [ ] Use named qualifier if needed
  - [ ] Inject sub-mappers for complex mappers

- [ ] **Register Data Source**
  - [ ] Add to `RemoteDataSourceModule.kt`
  - [ ] Inject HttpClient

- [ ] **Register Repository**
  - [ ] Add to `RepoModuleDI.kt`
  - [ ] Inject data source and mappers

- [ ] **Register Use Case**
  - [ ] Add to `DomainModuleDI.kt`
  - [ ] Inject repository

- [ ] **Register ViewModel**
  - [ ] Add to `ViewModelModule.kt`
  - [ ] Use `viewModel { }` scope
  - [ ] Inject use cases and dependencies

### 5. Testing & Verification

- [ ] **Unit Tests**
  - [ ] Test use case logic
  - [ ] Test mapper transformations
  - [ ] Test repository error handling

- [ ] **Integration Tests**
  - [ ] Test ViewModel state changes
  - [ ] Test action processing
  - [ ] Test effect emissions

- [ ] **Manual Testing**
  - [ ] Verify UI updates correctly
  - [ ] Verify navigation works
  - [ ] Verify error states display
  - [ ] Verify loading states

---

## Summary

This architecture guide provides:

1. **Domain Layer**: Pure business logic with use cases, repository interfaces, and domain models
2. **Data Layer**: Data access with repositories, data sources, DTOs, and mappers
3. **Presentation Layer**: UI state management with ViewModels, UiState, Actions, and Effects
4. **Error Handling**: Type-safe `ResultData<T>` with comprehensive `DataError` hierarchy
5. **Dependency Injection**: Koin modules for all layers with named qualifiers
6. **Best Practices**: Immutability, single source of truth, clear separation of concerns

**Key Takeaways:**

- **Dependencies flow inward**: Presentation → Domain ← Data
- **Use cases** encapsulate single business operations
- **Repositories** abstract data sources
- **Mappers** transform DTOs to domain models
- **ViewModels** manage UI state with flows and channels
- **Effects** handle one-time UI events
- **ResultData** provides type-safe error handling

Follow these patterns consistently for maintainable, testable, and scalable KMP applications.