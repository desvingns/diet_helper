package com.k.shavrin.diethelper.data.mapper

import com.k.shavrin.diethelper.data.local.entity.WeightEntryEntity
import com.k.shavrin.diethelper.domain.model.WeightEntry

fun WeightEntryEntity.toDomain(): WeightEntry = WeightEntry(
    id = id,
    date = date,
    weightKg = weightKg
)

fun WeightEntry.toEntity(): WeightEntryEntity = WeightEntryEntity(
    id = id,
    date = date,
    weightKg = weightKg
)

@JvmName("weightEntriesToDomain")
fun List<WeightEntryEntity>.toDomain(): List<WeightEntry> = map { it.toDomain() }
