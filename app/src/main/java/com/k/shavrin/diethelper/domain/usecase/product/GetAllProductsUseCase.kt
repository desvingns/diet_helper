package com.k.shavrin.diethelper.domain.usecase.product

import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(): Flow<List<Product>> = repository.getAllProducts()
}
