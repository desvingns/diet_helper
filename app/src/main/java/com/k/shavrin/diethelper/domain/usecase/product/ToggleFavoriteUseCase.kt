package com.k.shavrin.diethelper.domain.usecase.product

import com.k.shavrin.diethelper.domain.repository.ProductRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(productId: Long, isFavorite: Boolean) =
        repository.toggleFavorite(productId, isFavorite)
}
