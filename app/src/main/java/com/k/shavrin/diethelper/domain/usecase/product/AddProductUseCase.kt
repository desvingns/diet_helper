package com.k.shavrin.diethelper.domain.usecase.product

import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.repository.ProductRepository
import javax.inject.Inject

class AddProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(product: Product): Long = repository.addProduct(product)
}
