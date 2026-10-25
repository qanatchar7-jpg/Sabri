package com.example.data.model

data class CartItem(
    val product: ProductItem,
    val quantity: Int = 1
) {
    val lineTotal: Double
        get() = product.price * quantity
}
