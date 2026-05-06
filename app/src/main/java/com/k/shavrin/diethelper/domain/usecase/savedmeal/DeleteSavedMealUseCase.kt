package com.k.shavrin.diethelper.domain.usecase.savedmeal

import com.k.shavrin.diethelper.domain.repository.SavedMealRepository
import javax.inject.Inject

class DeleteSavedMealUseCase @Inject constructor(
    private val repository: SavedMealRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteMeal(id)
    }
}
