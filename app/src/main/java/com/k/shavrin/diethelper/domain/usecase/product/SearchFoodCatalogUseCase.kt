package com.k.shavrin.diethelper.domain.usecase.product

import com.k.shavrin.diethelper.domain.repository.FoodCatalogRepository
import com.k.shavrin.diethelper.domain.repository.FoodMarket
import javax.inject.Inject

class SearchFoodCatalogUseCase @Inject constructor(private val repository: FoodCatalogRepository) {
    suspend operator fun invoke(
        query: String,
        market: FoodMarket,
        lidlOnly: Boolean
    ) = repository.search(query, market, lidlOnly)
}
