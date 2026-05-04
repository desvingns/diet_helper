package com.k.shavrin.diethelper.domain.usecase.weight

import com.k.shavrin.diethelper.domain.model.WeightEntry
import com.k.shavrin.diethelper.domain.repository.WeightRepository
import javax.inject.Inject

class DeleteWeightEntryUseCase @Inject constructor(
    private val repository: WeightRepository
) {
    suspend operator fun invoke(entry: WeightEntry) = repository.deleteEntry(entry)
}
