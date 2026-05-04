package com.k.shavrin.diethelper.presentation.screen.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.usecase.foodentry.AddFoodEntryUseCase
import com.k.shavrin.diethelper.domain.usecase.product.SearchProductsUseCase
import com.k.shavrin.diethelper.domain.usecase.product.ToggleFavoriteUseCase
import com.k.shavrin.diethelper.presentation.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val addFoodEntryUseCase: AddFoodEntryUseCase
) : ViewModel() {

    private val date: LocalDate = LocalDate.parse(
        savedStateHandle.get<String>(Routes.ARG_DATE).orEmpty()
    )
    private val mealType: MealType = enumValueOf(
        savedStateHandle.get<String>(Routes.ARG_MEAL_TYPE).orEmpty()
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val uiState: StateFlow<ProductUiState> = _searchQuery
        .debounce { value -> if (value.isEmpty()) 0L else 300L }
        .flatMapLatest { query ->
            searchProductsUseCase(query).map { products ->
                ProductUiState.Success(
                    date = date,
                    mealType = mealType,
                    query = query,
                    products = products,
                    hasExactMatch = query.isNotBlank() &&
                            products.any { it.name.equals(query.trim(), ignoreCase = true) }
                ) as ProductUiState
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ProductUiState.Loading
        )

    fun setQuery(value: String) {
        _searchQuery.value = value
    }

    fun toggleFavorite(product: Product) {
        viewModelScope.launch {
            toggleFavoriteUseCase(product.id, !product.isFavorite)
        }
    }

    fun addEntry(product: Product, grams: Float, onComplete: () -> Unit) {
        viewModelScope.launch {
            addFoodEntryUseCase(
                FoodEntry(
                    productId = product.id,
                    product = product,
                    date = date,
                    mealType = mealType,
                    multiplier = grams / 100f
                )
            )
            onComplete()
        }
    }
}
