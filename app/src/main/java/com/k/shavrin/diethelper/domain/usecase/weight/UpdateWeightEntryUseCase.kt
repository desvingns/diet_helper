package com.k.shavrin.diethelper.domain.usecase.weight

import com.k.shavrin.diethelper.domain.model.WeightEntry
import com.k.shavrin.diethelper.domain.repository.WeightRepository
import javax.inject.Inject

class UpdateWeightEntryUseCase @Inject constructor(private val repository: WeightRepository) {
    suspend operator fun invoke(entry: WeightEntry) {
        require(entry.id > 0 && entry.weightKg.isFinite() && entry.weightKg > 0f && entry.weightKg <= 500f)
        repository.updateEntry(entry)
    }
}
