package com.k.shavrin.diethelper.domain.usecase.savedmeal

import com.k.shavrin.diethelper.domain.model.SavedMealItem
import com.k.shavrin.diethelper.domain.repository.SavedMealRepository
import javax.inject.Inject

class SaveMealUseCase @Inject constructor(
    private val repository: SavedMealRepository
) {
    suspend operator fun invoke(name: String, items: List<SavedMealItem>) {
        repository.saveMeal(name, items)
    }
}
