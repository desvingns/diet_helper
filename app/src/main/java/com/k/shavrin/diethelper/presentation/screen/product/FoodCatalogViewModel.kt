package com.k.shavrin.diethelper.presentation.screen.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.repository.FoodMarket
import com.k.shavrin.diethelper.domain.usecase.foodentry.AddFoodEntryUseCase
import com.k.shavrin.diethelper.domain.usecase.product.ImportCatalogProductUseCase
import com.k.shavrin.diethelper.domain.usecase.product.SearchFoodCatalogUseCase
import com.k.shavrin.diethelper.presentation.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject

data class FoodCatalogUiState(
    val query: String = "",
    val market: FoodMarket = FoodMarket.SERBIA,
    val lidlOnly: Boolean = false,
    val loading: Boolean = false,
    val searched: Boolean = false,
    val products: List<Product> = emptyList(),
    val saving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FoodCatalogViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val searchCatalog: SearchFoodCatalogUseCase,
    private val importProduct: ImportCatalogProductUseCase,
    private val addEntry: AddFoodEntryUseCase
) : ViewModel() {
    private val date = LocalDate.parse(savedStateHandle.get<String>(Routes.ARG_DATE))
    private val meal = MealType.valueOf(savedStateHandle.get<String>(Routes.ARG_MEAL_TYPE).orEmpty())
    private val _state = MutableStateFlow(FoodCatalogUiState())
    val state = _state.asStateFlow()
    private var searchJob: Job? = null

    fun query(value: String) = change(_state.value.copy(query = value.take(120)))
    fun market(value: FoodMarket) = change(_state.value.copy(market = value))
    fun lidl(value: Boolean) = change(_state.value.copy(lidlOnly = value))

    private fun change(value: FoodCatalogUiState) {
        searchJob?.cancel()
        _state.value = value.copy(products = emptyList(), loading = false, searched = false, error = null)
    }

    fun search() {
        val request = _state.value
        if (request.query.trim().length < 2 || request.loading) return
        searchJob?.cancel()
        _state.value = request.copy(loading = true, error = null, searched = true)
        searchJob = viewModelScope.launch {
            try {
                val result = searchCatalog(request.query, request.market, request.lidlOnly)
                _state.value = _state.value.copy(products = result, loading = false)
            } catch (error: CancellationException) {
                throw error
            } catch (error: IOException) {
                _state.value = _state.value.copy(loading = false,
                    error = if (error.message?.startsWith("Каталог") == true) error.message
                    else "Не удалось подключиться к каталогу. Проверьте интернет или используйте вкладку «Моя база».")
            } catch (_: Exception) {
                _state.value = _state.value.copy(loading = false, error = "Не удалось прочитать ответ каталога. Попробуйте позже.")
            }
        }
    }

    fun add(product: Product, amount: Float, onComplete: () -> Unit) {
        if (_state.value.saving || !amount.isFinite() || amount <= 0) return
        _state.value = _state.value.copy(saving = true)
        viewModelScope.launch {
            try {
                val local = importProduct(product)
                addEntry(FoodEntry(productId = local.id, product = local, date = date, mealType = meal,
                    multiplier = local.multiplierForAmount(amount)))
                _state.value = _state.value.copy(saving = false)
                onComplete()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _state.value = _state.value.copy(saving = false, error = "Не удалось сохранить продукт. Повторите добавление.")
            }
        }
    }
}
