package com.k.shavrin.diethelper.domain.usecase.product

import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.repository.FoodCatalogRepository
import javax.inject.Inject

class ImportCatalogProductUseCase @Inject constructor(private val repository: FoodCatalogRepository) {
    suspend operator fun invoke(product: Product) = repository.importProduct(product)
}
