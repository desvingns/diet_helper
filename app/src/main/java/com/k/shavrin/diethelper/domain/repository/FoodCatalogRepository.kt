package com.k.shavrin.diethelper.domain.repository

import com.k.shavrin.diethelper.domain.model.Product

enum class FoodMarket(val label: String, val tag: String) {
    SERBIA("Сербия", "serbia"), RUSSIA("Россия", "russia"), WORLD("Весь мир", "")
}

interface FoodCatalogRepository {
    suspend fun search(query: String, market: FoodMarket, lidlOnly: Boolean): List<Product>
    suspend fun importProduct(product: Product): Product
}
