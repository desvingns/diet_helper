package com.k.shavrin.diethelper.domain.usecase.weight

import com.k.shavrin.diethelper.domain.repository.WeightRepository
import java.time.LocalDate
import javax.inject.Inject

class UpsertWeightEntryUseCase @Inject constructor(
    private val repository: WeightRepository
) {
    suspend operator fun invoke(date: LocalDate, weightKg: Float) =
        repository.upsertEntry(date, weightKg)
}
