package com.k.shavrin.diethelper.domain.usecase.weight

import com.k.shavrin.diethelper.domain.model.WeightEntry
import com.k.shavrin.diethelper.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllWeightEntriesUseCase @Inject constructor(
    private val repository: WeightRepository
) {
    operator fun invoke(): Flow<List<WeightEntry>> = repository.getAllEntries()
}
