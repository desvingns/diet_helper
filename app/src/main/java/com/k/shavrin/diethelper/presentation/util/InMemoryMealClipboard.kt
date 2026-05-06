package com.k.shavrin.diethelper.presentation.util

import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.MealType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

data class ClipboardSnapshot(
    val entries: List<FoodEntry>,
    val sourceMealType: MealType,
    val sourceDate: LocalDate
)

@Singleton
class InMemoryMealClipboard @Inject constructor() {
    private val _state = MutableStateFlow<ClipboardSnapshot?>(null)
    val state: StateFlow<ClipboardSnapshot?> = _state.asStateFlow()

    fun copy(snapshot: ClipboardSnapshot) {
        _state.value = snapshot
    }

    fun clear() {
        _state.value = null
    }
}
